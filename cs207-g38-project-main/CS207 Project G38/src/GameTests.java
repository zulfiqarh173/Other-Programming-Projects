import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

public class GameTests {

    //
    // AC 1 - Generate Cryptogram
    //

    @Test
    @DisplayName("Scenario: Player requests letters cryptogram")
    public void testGenerateLetterCryptogram() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        assertInstanceOf(LetterCryptogram.class, game.getCryptogram());
        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();

        // Every letter in the phrase must have a unique cipher mapping
        Set<Character> usedCiphers = new HashSet<>();
        Set<Character> seenPlain = new HashSet<>();
        for (char c : lc.getPhrase().toCharArray()) {
            if (c < 'A' || c > 'Z') continue;
            if (!seenPlain.add(c)) continue;
            char cipher = lc.letterToCipher[c - 'A'];
            assertNotEquals((char) 0, cipher, "Plain letter '" + c + "' has no cipher mapping");
            assertTrue(usedCiphers.add(cipher), "Cipher '" + cipher + "' maps to more than one plain letter");
        }
    }

    @Test
    @DisplayName("Scenario: Player requests numbers cryptogram")
    public void testGenerateNumberCryptogram() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("N");
        assertInstanceOf(NumberCryptogram.class, game.getCryptogram());
        NumberCryptogram nc = (NumberCryptogram) game.getCryptogram();

        // Every letter in the phrase must map to a unique number in range 1-26
        Set<Integer> usedNumbers = new HashSet<>();
        Set<Character> seenPlain = new HashSet<>();
        for (char c : nc.getPhrase().toCharArray()) {
            if (c < 'A' || c > 'Z') continue;
            if (!seenPlain.add(c)) continue; // skip duplicate plain letters in the phrase
            int num = nc.letterToNumber[c - 'A'];
            assertTrue(num >= 1 && num <= 26, "Number for '" + c + "' must be 1-26, was " + num);
            assertTrue(usedNumbers.add(num), "Number " + num + " maps to more than one plain letter");
        }
    }

    @Test
    @DisplayName("Scenario: Player requests a cryptogram but no phrases file exists")
    public void testNoPhraseExists() throws Exception {
        // Temporarily save the file to a string before deleting the phrases.txt file
        Path p = Path.of("phrases.txt");
        String saved = Files.readString(p);
        Files.delete(p);

        try {
            Player playerTest = new Player("TEST");
            Game game = new Game(playerTest);
            assertThrows(IllegalStateException.class, () -> game.generateCryptogram("L"), "Phrases.txt file does not exist");
        } finally {
            // Writes the string for file content back to file
            Files.writeString(p, saved);
        }
    }

    //
    // AC2 - Enter letter
    //

    @Test
    @DisplayName("Scenario: player enters a letter")
    public void testEnterLetter() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        // Find the first unmapped cipher slot and its correct plain letter
        String cipherKey = null;
        String correctPlain = null;
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            String key = String.valueOf(cipher);
            if (mapping.containsKey(key) && mapping.get(key) == null) {
                cipherKey = key;
                correctPlain = String.valueOf(plain);
                break;
            }
        }
        assertNotNull(cipherKey, "There must be at least one unmapped slot");

        int guessesBefore = game.getUniqueGuessesCounter();
        int correctBefore  = game.getUniqueCorrectCounter();

        game.enterLetter(cipherKey, correctPlain);

        // Mapping must be updated
        assertEquals(correctPlain.charAt(0), mapping.get(cipherKey), "Plain letter must be stored in playerGameMapping");
        // Counters must increment
        assertEquals(guessesBefore + 1, game.getUniqueGuessesCounter(), "uniqueGuessesCounter must increment by 1");
        assertEquals(correctBefore + 1, game.getUniqueCorrectCounter(), "uniqueCorrectCounter must increment by 1 for a correct guess");
    }

    @Test
    @DisplayName("Scenario: player selects a cryptogram value which has already been mapped")
    public void testOverrideValue() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        // Find a cipher slot and enter a correct guess
        String cipherKey = null;
        String correctPlain = null;
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            String key = String.valueOf(cipher);
            if (mapping.containsKey(key) && mapping.get(key) == null) {
                cipherKey = key;
                correctPlain = String.valueOf(plain);
                break;
            }
        }
        assertNotNull(cipherKey);
        game.enterLetter(cipherKey, correctPlain);
        char original = mapping.get(cipherKey);

        // Find a plain letter not used in the phrase to use as a letter to override guess
        char overrideChar = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            if (lc.getFrequency(c) == 0) { overrideChar = c; break; }
        }
        assumeTrue(overrideChar != 0, "Need an unused plain letter for replacement");

        // Answer N – original mapping must be preserved
        System.setIn(new ByteArrayInputStream("N\n".getBytes()));
        game.enterLetter(cipherKey, String.valueOf(overrideChar));
        assertEquals(original, mapping.get(cipherKey), "Answering N must keep the original mapping");

        // Answer Y – mapping must be replaced
        System.setIn(new ByteArrayInputStream("Y\n".getBytes()));
        game.enterLetter(cipherKey, String.valueOf(overrideChar));
        assertEquals(overrideChar, mapping.get(cipherKey), "Answering Y must replace the mapping");
    }

    @Test
    @DisplayName("Scenario: player selects a plain letter which they have already mapped")
    public void testPlainLetterAlreadyUsed() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        // Map the first available cipher slot
        String firstKey = null;
        String correctPlain = null;
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            String key = String.valueOf(cipher);
            if (mapping.containsKey(key) && mapping.get(key) == null) {
                firstKey = key;
                correctPlain = String.valueOf(plain);
                break;
            }
        }
        assertNotNull(firstKey);
        game.enterLetter(firstKey, correctPlain);

        // Find a second unmapped slot and try to reuse the same plain letter
        String secondKey = null;
        for (String k : mapping.keySet()) {
            if (!k.equals(firstKey) && mapping.get(k) == null) { secondKey = k; break; }
        }
        assumeTrue(secondKey != null, "Need a second unmapped cipher slot");
        game.enterLetter(secondKey, correctPlain);
        assertNull(mapping.get(secondKey), "Second cipher slot must remain unmapped when plain letter is already used");
    }

    @Test
    @DisplayName("Scenario: player enters the last value to be mapped and successfully completes the cryptogram")
    public void testLastCorrectMapping() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        List<String[]> pairs = new ArrayList<>();
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            if (mapping.containsKey(String.valueOf(cipher)))
                pairs.add(new String[]{ String.valueOf(cipher), String.valueOf(plain) });
        }

        // Enter all but the last correctly
        for (int i = 0; i < pairs.size() - 1; i++) {
            game.enterLetter(pairs.get(i)[0], pairs.get(i)[1]);
        }

        // Enter the last correct mapping
        game.enterLetter(pairs.get(pairs.size() - 1)[0], pairs.get(pairs.size() - 1)[1]);

        // All guesses correct means the game was won
        assertEquals(mapping.size(), game.getUniqueCorrectCounter(), "All mappings must be correct when the cryptogram is successfully completed");
    }

    @Test
    @DisplayName("Scenario: player enters the last value to be mapped and unsuccessfully completes the cryptogram")
    public void testLastIncorrectMapping() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        List<String[]> pairs = new ArrayList<>();
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            if (mapping.containsKey(String.valueOf(cipher)))
                pairs.add(new String[]{ String.valueOf(cipher), String.valueOf(plain) });
        }
        assumeTrue(pairs.size() >= 2, "Need at least two cipher slots");

        // Enter all but the last correctly
        for (int i = 0; i < pairs.size() - 1; i++) {
            game.enterLetter(pairs.get(i)[0], pairs.get(i)[1]);
        }

        // Find a wrong plain letter (not used in the phrase)
        String[] last = pairs.get(pairs.size() - 1);
        char wrongPlain = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            if (c != last[1].charAt(0)) { wrongPlain = c; break; }
        }
        assumeTrue(wrongPlain != 0, "Need an unused plain letter differing from the correct answer");

        game.enterLetter(last[0], String.valueOf(wrongPlain));

        // Correct counter must be less than total mappings means cryptogram is not solved
        assertTrue(game.getUniqueCorrectCounter() < mapping.size(), "uniqueCorrectCounter must be less than total mappings when last guess is wrong");
    }

    @Test
    @DisplayName("Scenario: player enters a cryptogram value which is not used in the cryptogram")
    public void testInvalidCipherValue() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        int guessesBefore = game.getUniqueGuessesCounter();
        game.enterLetter("99", "A");

        // No guess should be recorded if the cipher value is invalid
        assertEquals(guessesBefore, game.getUniqueGuessesCounter(), "uniqueGuessesCounter must not increment for an invalid cipher value");
    }

    //
    // AC3 - Undo Letter
    //

    @Test
    @DisplayName("Scenario: player wants to undo a mapped letter")
    public void testUndoMappedLetter() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        // Find and enter the first correct mapping
        String cipherKey = null;
        String correctPlain = null;
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            String key = String.valueOf(cipher);
            if (mapping.containsKey(key) && mapping.get(key) == null) {
                cipherKey = key;
                correctPlain = String.valueOf(plain);
                break;
            }
        }
        assertNotNull(cipherKey);
        game.enterLetter(cipherKey, correctPlain);
        assertNotNull(mapping.get(cipherKey), "Letter must be mapped before undo");

        // undoLetter with override=false reads the key from Scanner
        System.setIn(new ByteArrayInputStream((cipherKey + "\n").getBytes()));
        game.undoLetter("", false, false);

        assertNull(mapping.get(cipherKey), "After undo the playerGameMapping entry should be null");
    }

    @Test
    @DisplayName("Scenario: player selects a letter in the cryptogram which they have not mapped")
    public void testUndoUnmappedLetter() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        // Every slot is null on a fresh game — pick any key
        String unmappedKey = mapping.keySet().iterator().next();

        System.setIn(new ByteArrayInputStream((unmappedKey + "\n").getBytes()));
        game.undoLetter("", false, false);

        // Slot must still be null — nothing was removed
        assertNull(mapping.get(unmappedKey), "Unmapped slot must remain null after attempting to undo it");
    }

    //
    // AC4 - Save Cryptogram
    //

    @Test
    @DisplayName("Scenario: player saves cryptogram")
    public void testSaveGame() {

        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        new File("savegame.txt").delete();
        game.saveGame();

        assertTrue(new File("savegame.txt").exists(), "savegame.txt must exist after saveGame()");

        String content = "";
        try { 
            content = Files.readString(Path.of("savegame.txt")); 
        } catch (IOException e) { 
            fail("Could not read save file"); 
        }

        assertTrue(content.contains("TYPE:L"), "Save file must contain TYPE:L");
        assertTrue(content.contains("PHRASE:"), "Save file must contain PHRASE:");
        assertTrue(content.contains("CIPHER_MAP:"), "Save file must contain CIPHER_MAP:");
        assertTrue(content.contains("GUESSES:"),"Save file must contain GUESSES:");
        assertTrue(content.contains("CORRECT:"),"Save file must contain CORRECT:");
        assertTrue(content.contains("TOTAL:"), "Save file must contain TOTAL:");
    }

    @Test
    @DisplayName("Scenario: player already has a saved cryptogram")
    public void testSaveGameAlreadyExists() throws Exception {
        Files.writeString(Path.of("savegame.txt"), "EXISTING CONTENT");

        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        System.setIn(new ByteArrayInputStream("N\n".getBytes()));
        game.saveGame();
        assertEquals("EXISTING CONTENT", Files.readString(Path.of("savegame.txt")),
                    "Save file must not be modified when the player cancels the overwrite");

        System.setIn(new ByteArrayInputStream("Y\n".getBytes()));
        game.saveGame();
        String content = Files.readString(Path.of("savegame.txt"));
        assertTrue(content.contains("TYPE:"),
                "Save file must be overwritten with new game state after confirming");
        assertFalse(content.contains("EXISTING CONTENT"),
                "Old save content must be gone after confirmed overwrite");
    }

    //
    // AC5 - Load Cryptogram
    //

    @Test
    @DisplayName("Scenario: player loads their saved game")
    public void testLoadGame() {

        Player playerTest = new Player("TEST");
        Game saveGame = new Game(playerTest);
        saveGame.generateCryptogram("L");

        LetterCryptogram lc = (LetterCryptogram) saveGame.getCryptogram();
        HashMap<String, Character> mapping = saveGame.getPlayerGameMapping();
        String savedPhrase = lc.getPhrase();
    
        String cipherKey = null;
        String correctPlain = null;
        for (char plain = 'A'; plain <= 'Z'; plain++) {

            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) {
                continue;
            }

            String key = String.valueOf(cipher);
            if (mapping.containsKey(key) && mapping.get(key) == null) {
                cipherKey = key;
                correctPlain = String.valueOf(plain);
                break;
            }
        }
        
        assertNotNull(cipherKey);
        saveGame.enterLetter(cipherKey, correctPlain);
        String data = "Y";
        System.setIn(new ByteArrayInputStream(data.getBytes()));

        saveGame.saveGame();

        Game loadGame = new Game(playerTest);
        boolean result = loadGame.loadGame();

        assertTrue(result, "loadGame() must return true on a valid save file");
        assertInstanceOf(LetterCryptogram.class, loadGame.getCryptogram(),
                "Loaded cryptogram must be a LetterCryptogram");
        assertEquals(savedPhrase, loadGame.getCryptogram().getPhrase(),
                "Loaded phrase must match the saved phrase");
        assertEquals(correctPlain.charAt(0), loadGame.getPlayerGameMapping().get(cipherKey),
                "Player guess must be restored after load");
        assertEquals(1, loadGame.getUniqueCorrectCounter(),
                "uniqueCorrectCounter must be restored to 1");
        assertEquals(1, loadGame.getUniqueGuessesCounter(),
                "uniqueGuessesCounter must be restored to 1");
    }

    @Test
    @DisplayName("Scenario: player has no previously saved game")
    public void testLoadGameNoSaveFile() {
        new File("savegame.txt").delete();

        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        boolean result = game.loadGame();

        assertFalse(result, "loadGame() must return false when no save file exists");
        assertNull(game.getCryptogram(), "Cryptogram must remain null when no save file exists");
    }

    @Test
    @DisplayName("Scenario: error when loading saved game")
    public void testLoadGameCorruptedFile() throws Exception {
        Files.writeString(Path.of("savegame.txt"), "CORRUPT DATA\nNOTHING:VALID\n");

        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        boolean result = game.loadGame();

        assertFalse(result, "loadGame() must return false for a corrupted save file");
        assertNull(game.getCryptogram(), "Cryptogram must remain null after a failed load");
    }

    //
    // AC8 - Store Player Details
    //

    @Test
    @DisplayName("Scenario: Given a player has been created - details are saved on exit")
    public void testPlayerDetailsSavedOnExit() throws Exception {
        Files.deleteIfExists(Paths.get("playersTest.txt"));

        Players players = new Players("playersTest.txt");
        players.addPlayer(new Player("TEST"));
        players.savePlayers();

        assertTrue(new File("playersTest.txt").exists(), "playersTest.txt should be created after savePlayers()");
        String content = Files.readString(Paths.get("playersTest.txt"));
        assertTrue(content.contains("TEST"), "Saved file should contain the player's username");

        Files.deleteIfExists(Paths.get("playersTest.txt"));
    }

    @Test
    @DisplayName("Scenario: Given a player has been created - player stats are saved correctly")
    public void testPlayerStatsSavedCorrectly() throws Exception {
        Files.deleteIfExists(Paths.get("playersTest.txt"));

        Players players = new Players("playersTest.txt");
        Player player = new Player("TEST");
        player.setTotalGuesses(20);
        player.setCorrectGuesses(10);
        player.setCryptogramPlayed(2);
        player.setCryptogramCompleted(1);
        players.addPlayer(player);
        players.savePlayers();

        String content = Files.readString(Paths.get("playersTest.txt"));
        assertTrue(content.contains("TEST"),"File should contain username");
        assertTrue(content.contains("20"),"File should contain totalGuesses");
        assertTrue(content.contains("10"),"File should contain correctGuesses");
        assertTrue(content.contains("2"),"File should contain cryptogramsPlayed");
        assertTrue(content.contains("1"),"File should contain cryptogramsCompleted");

        Files.deleteIfExists(Paths.get("playersTest.txt"));
    }

    @Test
    @DisplayName("Scenario: Given a player has been created - accuracy is calculated before saving")
    public void testAccuracyCalculatedOnSave() throws Exception {
        Files.deleteIfExists(Paths.get("playersTest.txt"));

        Players players = new Players("playersTest.txt");
        Player player = new Player("TEST");
        player.setTotalGuesses(10);
        player.setCorrectGuesses(5);
        players.addPlayer(player);
        players.savePlayers();

        assertEquals(50.0, player.getAccuracy(), 0.01, "Accuracy should be 50%");

        Files.deleteIfExists(Paths.get("playersTest.txt"));
    }

    // AC 9 - As a player I want the software to track the number of cryptograms I have successfully completed
    @Test
    @DisplayName("Scenario: cryptogram is solved")
    public void succesfullyCompletedCryptogram(){
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        assumeTrue(playerTest.getNumCryptogramCompleted() == 0);
        assumeTrue(playerTest.getNumCryptogramPlayed() == 1);

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        List<String[]> pairs = new ArrayList<>();
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            if (mapping.containsKey(String.valueOf(cipher)))
                pairs.add(new String[]{ String.valueOf(cipher), String.valueOf(plain) });
        }

        // Enter all but the last correctly
        for (int i = 0; i < pairs.size() - 1; i++) {
            game.enterLetter(pairs.get(i)[0], pairs.get(i)[1]);
        }

        // Enter the last correct mapping
        game.enterLetter(pairs.get(pairs.size() - 1)[0], pairs.get(pairs.size() - 1)[1]);

        // All guesses correct means the game was won
        assertEquals(mapping.size(), game.getUniqueCorrectCounter(), "All mappings must be correct when the cryptogram is successfully completed");
        assumeTrue(game.checkGameCompletion());
        assumeTrue(playerTest.getNumCryptogramCompleted() == 1);
        assumeTrue(playerTest.getNumCryptogramPlayed() == 1);
    }

    @Test
    @DisplayName("Scenario: cryptogram entered by user is wrong")
    public void failedCompletedCryptogram(){
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        assertTrue(playerTest.getNumCryptogramCompleted() == 0);
        assertTrue(playerTest.getNumCryptogramPlayed() == 1);

        assumeTrue(playerTest.getNumCryptogramCompleted() == 0);
        assumeTrue(playerTest.getNumCryptogramPlayed() == 1);

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        List<String[]> pairs = new ArrayList<>();
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            if (mapping.containsKey(String.valueOf(cipher)))
                pairs.add(new String[]{ String.valueOf(cipher), String.valueOf(plain) });
        }

        // Enter all but the last correctly
        for (int i = 0; i < pairs.size() - 1; i++) {
            game.enterLetter(pairs.get(i)[0], pairs.get(i)[1]);
        }

        String[] lastPair = pairs.get(pairs.size() - 1);
        String lastCipher = lastPair[0];
        String correctAnswer = lastPair[1];
        String wrongGuess;

        if (correctAnswer.equals("Z")) {
            wrongGuess = "A";
        } else {
            wrongGuess = "Z";
        }

        // Enter the incorrect correct mapping
        game.enterLetter(lastCipher, wrongGuess);
        assertFalse(game.checkGameCompletion());
        assertTrue(playerTest.getNumCryptogramCompleted() == 0);
        assertTrue(playerTest.getNumCryptogramPlayed() == 1);


    }




    // AC 10 - As a player I want the software to track the number of cryptograms I have played so I can
    // see how many games I’ve attempted

    @Test
    @DisplayName("Scenario: new cryptogram played")
    public void testIncrementCryptogramForPlayer(){
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);

        assumeTrue(playerTest.getNumCryptogramPlayed() == 0);
        game.generateCryptogram("L");
        assumeTrue(playerTest.getNumCryptogramPlayed() == 1);
    }

    // AC11 - As a player I want the software to track the number of correct guesses
    //  I have made, so I can see how accurate I am as a percentage of my total number of guesses
    // 26
    @Test
    @DisplayName("Scenario: correct guess made")
    public void testCorrectGuesses(){
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        assumeTrue(playerTest.getCorrectGuesses() == 0);

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        // Find and enter the first correct mapping
        String cipherKey = null;
        String correctPlain = null;
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            String key = String.valueOf(cipher);
            if (mapping.containsKey(key) && mapping.get(key) == null) {
                cipherKey = key;
                correctPlain = String.valueOf(plain);
                break;
            }
        }

        assertNotNull(cipherKey);
        game.enterLetter(cipherKey, correctPlain);

        assumeTrue(playerTest.getCorrectGuesses() == 1);


    }

    @Test
    @DisplayName("Scenario: correct guess made")
    public void testIncorrectGuesses(){
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("L");

        assumeTrue(playerTest.getCorrectGuesses() == 0);

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        // Find the first correct mapping
        String cipherKey = null;
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            String key = String.valueOf(cipher);
            if (mapping.containsKey(key) && mapping.get(key) == null) {
                cipherKey = key;
                break;
            }
        }

        assertNotNull(cipherKey);
        game.enterLetter(cipherKey, "Z");

        assumeTrue(playerTest.getCorrectGuesses() == 0);


    }

    //US 12 - As a player I want to load my details so I can track my game play statistics.

    //US 12 AC 1 - Given a player has previously played at least one cryptogram game
    // When the player identifies themselves
    // Then the player’s details are loaded

    @Test
    @DisplayName("Loading new Player (displaying details)")
    public void testCreateNewPlayer(){

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Players players = new Players("playersTest.txt");
        players.addPlayer(new Player("TEST",100.0,112,23,1,13));

        players.displayPlayer("TEST");


        String expectedOutput  = "*************************************************************\n"+
                "TEST's stats Details:\n"+
                "Accuracy: 100.0\n"+
                "Total guesses: 112\n"+
                "Correct guesses: 13\n"+
                "Cryptograms played: 23\n"+
                "Cryptograms completed: 1\n"+
                "*************************************************************\n";
        assertEquals(expectedOutput, outContent.toString());
    }

    @Test
    @DisplayName("Error loading player details")
    public void testLoadPlayerDetails(){
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Players players = new Players("playersTest.txt");
        players.displayPlayer("I WAS NOT HERE");

        String expectedOutput  = "\n Player not found :(\n";

        assertEquals(expectedOutput, outContent.toString());
    }

    //US 12 AC 3 - Given a player tries to load details
    // When the player details have not been stored
    // Then show an error message to the player and create a new player

    @Test
    @DisplayName("Creating new player")
    public void testCreateNewPlayerError(){
        Players players = new Players("playersTest.txt");
        String data = "NewTestPlayer"+"\nY";
        System.setIn(new ByteArrayInputStream(data.getBytes()));

        players.loginPlayer();

        assertEquals(players.findPlayer("NewTestPlayer").getUsername(), "NewTestPlayer");
    }

    //
    // AC14 - Get a hint for a letter
    //

    @Test
    @DisplayName("Scenario: the letter identified has not been mapped by the user")
    public void testHintMapsUnmappedCipher() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("l");

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();
        game.getHint();

        // Hint should map one cipher letter value to its correct position
        int count = 0;
        for (String cipherKey : mapping.keySet()) {
            Character guessed = mapping.get(cipherKey);
            if (guessed != null) {
                count++;
                char correct = lc.getCorrectPlainLetter(cipherKey.charAt(0));
                assertEquals(correct, guessed, "Hint must map the cipher to the correct plain letter");
            }
        }
        assertEquals(1, count, "Exactly one slot should be filled after one hint");
    }

    @Test
    @DisplayName("Scenario: the letter identified has already been mapped by the user")
    public void testHintRemovesAndCorrectsWrongMapping() {
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("l");

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();
        HashMap<String, Character> mapping = game.getPlayerGameMapping();

        // Get two distinct cipher letters - one for unmapped letter, the other for wrong letter
        List<String> cipherLetters = new ArrayList<>(mapping.keySet());
        assumeTrue(cipherLetters.size() >= 2, "Need at least two cipher slots");
        String targetCipher = cipherLetters.get(0);  // Will remain unmapped initially
        String wrongCipher = cipherLetters.get(1);   // Will be incorrectly mapped

        char correctPlainTarget = lc.getCorrectPlainLetter(targetCipher.charAt(0));

        // Fill every cipher correctly except for the target and wrong one
        for (String letter : cipherLetters) {
            if (letter.equals(targetCipher) || letter.equals(wrongCipher)) {
                continue;
            }
            char correctPlain = lc.getCorrectPlainLetter(letter.charAt(0));
            game.enterLetter(letter, String.valueOf(correctPlain));
        }

        // Map wrongCipher incorrectly to the correct plain letter of targetCipher
        game.enterLetter(wrongCipher, String.valueOf(correctPlainTarget));
        assertNotNull(mapping.get(wrongCipher), "Wrong cipher should be mapped before hint");

        game.getHint();

        // After hint, targetCipher should be correctly mapped, and wrongCipher should be cleared
        assertEquals(correctPlainTarget, mapping.get(targetCipher), "Hint should correctly map targetCipher");
        assertNull(mapping.get(wrongCipher), "Wrong mapping should be removed by hint");
    }


    @Test
    @DisplayName("Show Working leaderboard")
    public void displayWorkingLeaderboard(){
        Players players = new Players("playersTest.txt");




        players.addPlayer(new Player("TEST",50.0,112,50,25,56));
        players.addPlayer(new Player("Hello",75.0,100,18,18,75));
        players.addPlayer(new Player("Mate",0.0,30,3,0,0));
        players.addPlayer(new Player("TEST2",50.0,112,50,26,56));
        assertEquals(players.getPos("TEST"),0);

        players.generateLeaderboard();

        assertEquals(2,players.getPos("TEST"));
        assertEquals(0,players.getPos("Hello"));
        assertEquals(3,players.getPos("Mate"));
        assertEquals(1,players.getPos("TEST2"));
        //checks if sorting worked




    }
    @Test
    @DisplayName("Show leaderboard error")
    public void displayFailingLeaderboard(){
        Players players = new Players("playersTest.txt");
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        players.generateLeaderboard();

        String expectedOutput = "No players added yet\n"; //error message;

        assertEquals(expectedOutput, outContent.toString());

        //checks if it prints the error message correctly




    }

    //
    // AC15 - view cryptogram frequencies
    //
    @Test
    @DisplayName("Scenario: player views the frequencies of letters in a letter cryptogram")
    public void testDisplayFrequenciesLetterCryptogram() {
        System.setIn(new ByteArrayInputStream("testplayer\nY\n".getBytes()));
        Game game = new Game();
        game.generateCryptogram("L");

        LetterCryptogram lc = (LetterCryptogram) game.getCryptogram();

        int[] expectedCounts = new int[26];
        for (char c : lc.getPhrase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {

                char cipher = lc.getLetterToCipher()[c - 'A'];

                if (cipher != 0) {
                    expectedCounts[cipher - 'A']++;
                }
            }
        }
  
        for (char c : lc.getPhrase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                char cipher = lc.getLetterToCipher()[c - 'A'];
                assertTrue(expectedCounts[cipher - 'A'] > 0,
                           "Cipher letter '" + cipher + "' must have a frequency > 0");
            }
        }

        for (int i = 0; i < 26; i++) {
            char cipher = (char)('A' + i);
            boolean inPhrase = false;
        
            for (char c : lc.getPhrase().toCharArray()) {
                if (c >= 'A' && c <= 'Z' && lc.getLetterToCipher()[c - 'A'] == cipher) {
                    inPhrase = true;
                    break;
                }
            }

            if (!inPhrase) {
                assertEquals(0, expectedCounts[i],
                            "Cipher letter '" + cipher + "' must have frequency 0 if not in phrase");
            }
        }
  
        int totalCipher = 0;
        for (int count : expectedCounts) {
            totalCipher += count;
        }

        long totalPlain = lc.getPhrase().chars().filter(c -> c >= 'A' && c <= 'Z').count();
        assertEquals(totalPlain, totalCipher,
                    "Sum of all cipher frequencies must equal total letters in the phrase");
    }

    @Test
    @DisplayName("Scenario: player views frequencies of letters in a number cryptogram")
    public void testDisplayFrequenciesNumberCryptogram() {
        System.setIn(new ByteArrayInputStream("testplayer\nY\n".getBytes()));
        Game game = new Game();
        game.generateCryptogram("N");

        NumberCryptogram nc = (NumberCryptogram) game.getCryptogram();

        int[] expectedCounts = new int[27];
        for (char c : nc.getPhrase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {

                int cipher = nc.getLetterToNumber()[c - 'A'];

                if (cipher != 0) {
                    expectedCounts[cipher]++;
                }
            }
        }

        for (char c : nc.getPhrase().toCharArray()) {
        
            if (c >= 'A' && c <= 'Z') {
            
                int cipher = nc.getLetterToNumber()[c - 'A'];
                assertTrue(expectedCounts[cipher] > 0,
                            "Cipher number " + cipher + " must have a frequency > 0");
            }
        }

        for (char c : nc.getPhrase().toCharArray()) {

            if (c >= 'A' && c <= 'Z') {
                int cipher = nc.getLetterToNumber()[c - 'A'];
                assertTrue(cipher >= 1 && cipher <= 26,
                            "Cipher number must be in range 1-26, was " + cipher);
            }
        }

        int totalCipher = 0;
        for (int count : expectedCounts) {
            totalCipher += count;
        }

        long totalPlain = nc.getPhrase().chars().filter(c -> c >= 'A' && c <= 'Z').count();
        assertEquals(totalPlain, totalCipher,
                    "Sum of all cipher frequencies must equal total letters in the phrase");
    }

    @Test
    @DisplayName("Scenario: player gives up and wants to know the solution")
    public void testCompleteCryptogram(){
        Player playerTest = new Player("TEST");
        Game game = new Game(playerTest);
        game.generateCryptogram("l");

        game.completeCryptogram();
        game.displayCurrentState();

        HashMap<String, Character> mapping = game.getPlayerGameMapping();
        List<Character> phraseChars = game.displayCryptogramPhrase().chars().mapToObj(c -> (char) c).collect(Collectors.toList());

        phraseChars.removeIf( c -> c == ' ');



        for (char phraseChar : phraseChars) {
            assumeTrue(mapping.containsValue(phraseChar));
        }



    }

}