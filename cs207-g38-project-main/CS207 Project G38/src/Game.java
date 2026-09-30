import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Game {

    private int uniqueCorrectCounter;
    private int uniqueGuessesCounter;
    private Cryptogram cryptogram;
    private HashMap<String, Character> playerGameMapping = new HashMap<>();
    char letterToCipher[] = new char[26];
    private Player currentPlayer;
    private Players currentPlayers;

    public Game(){
        currentPlayers = new Players();
        this.currentPlayer = currentPlayers.loginPlayer();
        //this.currentPlayer = p;
    }
    public Game(Player p){
        this.currentPlayer = p;
    }

    public String getInputAndValidate() {
        Scanner userInput = new Scanner(System.in);
        return userInput.nextLine().trim().toUpperCase();
    }

    public void generateCryptogram(String cryptogramType) {

        List<String> phrases = loadPhrases();

        if (phrases == null || phrases.isEmpty()) {
            System.out.println("Error with loading file for phrases or it is empty.");
            throw new IllegalStateException("Phrases.txt file does not exist");
        }

        // Choosing a random phrase from the file
        String phrase = phrases.get((int)(Math.random() * phrases.size()));

        List<Character> uniqueLetters = new ArrayList<>();
        Set<Character> seen = new HashSet<>();
        for (char c : phrase.toCharArray()) {
            if (c >= 'A' && c <= 'Z' && seen.add(c)) {
                uniqueLetters.add(c);
            }
        }
 
        playerGameMapping.clear();

        if (cryptogramType.equalsIgnoreCase("L") || cryptogramType.equalsIgnoreCase("Letter")) {
            generateLetterCryptogram(phrase, uniqueLetters);
        } else if (cryptogramType.equalsIgnoreCase("N") || cryptogramType.equalsIgnoreCase("Number")){
            generateNumberCryptogram(phrase, uniqueLetters);
        }

        currentPlayer.incrementCryptogramsPlayed();

        displayCurrentState();
    }
    
    public void generateCryptogramAlph(String phrase, String cryptogramAlphabet) {

        List<String> phrases = loadPhrases();
        phrase = phrase.toUpperCase();
        cryptogramAlphabet = cryptogramAlphabet.toUpperCase();

        List<Character> available = new ArrayList<>();
        for (char c : cryptogramAlphabet.toCharArray()) {
            available.add(c);
        }

        for (char c : phrase.toUpperCase().toCharArray()) {

            if (c < 'A' || c > 'Z') {
                continue;
            }

            int idx = c - 'A';
            if (letterToCipher[idx] != 0) {
                continue;
            }

            int randomIndex = (int)(Math.random() * available.size());
            char cipher = available.remove(randomIndex);

            letterToCipher[idx] = cipher;
            playerGameMapping.put(String.valueOf(cipher), null);
        }
        cryptogram = new LetterCryptogram(phrase, cryptogramAlphabet, letterToCipher);
        System.out.println(playerGameMapping.entrySet());
    }


    private void generateLetterCryptogram(String phrase, List<Character> uniqueLetters) {
        char[] letterToCipher = new char[26];
        List<Character> available;
        boolean valid = false;

        while (!valid) {
            // Start with arraylist of all 26 letters as available cipher letters
            letterToCipher = new char[26];
            available = new ArrayList<>();
            for (char c = 'A'; c <= 'Z'; c++) {
                available.add(c);
            }
            valid = true;
            for (char plain : uniqueLetters) {
                // Creates a copy of available cipher letters then remove the plain letter from the copy
                List<Character> validCiphers = new ArrayList<>(available);
                validCiphers.remove(Character.valueOf(plain));

                if (validCiphers.isEmpty()) {
                    valid = false;
                    break;
                }

                // Choose a random letter from validCiphers to use as the new cipher
                char cipher = validCiphers.get((int) (Math.random() * validCiphers.size()));
                available.remove(Character.valueOf(cipher));

                letterToCipher[plain - 'A'] = cipher; // Store the plain letter as the cipher letter mapping in the array
                playerGameMapping.put(String.valueOf(cipher), null);
            }
            if (!valid) {
                playerGameMapping.clear();
            }
        }
        cryptogram = new LetterCryptogram(phrase, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", letterToCipher);
    }

    private void generateNumberCryptogram(String phrase, List<Character> uniqueLetters) {
        int[] letterToNumber = new int[26];

        // Start with arraylist of all 26 numbers as available cipher numbers
        List<Integer> available = new ArrayList<>();
        for (int i = 1; i <= 26; i++) available.add(i);

        for (char plain : uniqueLetters) {
            int number = available.remove((int)(Math.random() * available.size()));
            letterToNumber[plain - 'A'] = number;
            playerGameMapping.put(String.valueOf(number), null);
        }
        cryptogram = new NumberCryptogram(phrase, "1-26", letterToNumber);
    }

    /* would need a way for the cryptograms to pass these values to 
       putCipherLetter and undoCipherLetter */

    

    public boolean enterLetter(String encryptedLetterPlace, String userGuess) {
        char guess = userGuess.charAt(0);

        if(playerGameMapping.containsKey(encryptedLetterPlace)&&(playerGameMapping.get(encryptedLetterPlace) != null)) {
            System.out.println("Do you want to override your guess (Y/N)?");
            String overrideGuess = getInputAndValidate();
            while ((!overrideGuess.equals("Y") && !overrideGuess.equals("N"))) {
                System.out.println("Invalid, Enter Y/N to override your guess");
                overrideGuess = getInputAndValidate();
            }
            if (overrideGuess.equals("Y")) {
                undoLetter(encryptedLetterPlace, true, false);
            }
            else {
                return false;
            }
        }

        if (!playerGameMapping.containsKey(encryptedLetterPlace)){
            System.out.println("Encrypted letter/number is invalid\n");
            return false;
        }
        if (cryptogram.getFrequency(guess) != 0) {
            System.out.println("Letter already used");
            return false;
        }

        playerGameMapping.put(encryptedLetterPlace, guess);

        if (cryptogram instanceof LetterCryptogram) {
            cryptogram.putCipherLetter(encryptedLetterPlace.charAt(0), guess);
            if(cryptogram.correctGuessCheck(encryptedLetterPlace.charAt(0), guess)) {
                currentPlayer.incrementCorrectGuesses();
                uniqueCorrectCounter++;
            }
        }

        else {
            cryptogram.putCipherLetter(Integer.parseInt(encryptedLetterPlace), guess);
            if(cryptogram.correctGuessCheck(encryptedLetterPlace, guess)) {
                currentPlayer.incrementCorrectGuesses();
                uniqueCorrectCounter++;
            }
        }

        uniqueGuessesCounter++;
        currentPlayer.incrementTotalGuesses();
        if(currentPlayers!=null) {
            currentPlayers.updatePlayerAfterGuess(currentPlayer);
        }
        //displayCurrentState();
        return true;
    }

    public void undoLetter(String encryptedLetterPlace, boolean override, boolean playerGaveUp) {

        if (!override) {
            System.out.println("Enter the encrypted letter/number you want to remove from your guess:");
            encryptedLetterPlace = getInputAndValidate();
        }

        if (!playerGameMapping.containsKey(String.valueOf(encryptedLetterPlace))){
            System.out.println("Encrypted Letter does not exist");
            return;
        }

        Character c = playerGameMapping.get(String.valueOf(encryptedLetterPlace));
        if (c == null){
            if (!playerGaveUp){
                System.out.println("Encrypted Letter has no character stored there");
            }

            return;
        }

        playerGameMapping.put(String.valueOf(encryptedLetterPlace), null);
        if (cryptogram instanceof LetterCryptogram) {
            if (cryptogram.correctGuessCheck(encryptedLetterPlace.charAt(0), c))
                uniqueCorrectCounter--;
            cryptogram.undoCipherLetter(encryptedLetterPlace.charAt(0));

        } else {
            if (cryptogram.correctGuessCheck(encryptedLetterPlace, c))
                uniqueCorrectCounter--;
            cryptogram.undoCipherLetter(Integer.parseInt(String.valueOf(encryptedLetterPlace)));
        }
        if(!override) {
            displayCurrentState();
            uniqueGuessesCounter--;
        }
        if(currentPlayers!=null) {
            currentPlayers.updatePlayerAfterGuess(currentPlayer);
        }

    }

    public void displayCurrentState() {
        if (cryptogram == null) {
            System.out.println("No cryptogram loaded.");
            return;
        }
        System.out.println("*************************************************************");
        System.out.println("Current Cryptogram State:");
        cryptogram.display(playerGameMapping);
        System.out.println();

    }

    public void completeCryptogram(){
        List<String> keys = playerGameMapping.keySet().stream().toList();
        for (String key : keys){
            undoLetter(key, true, true);
        }

        LetterCryptogram lc = (LetterCryptogram) getCryptogram();
        HashMap<String, Character> mapping = getPlayerGameMapping();



        java.util.List<String[]> pairs = new java.util.ArrayList<>();
        for (char plain = 'A'; plain <= 'Z'; plain++) {
            char cipher = lc.letterToCipher[plain - 'A'];
            if (cipher == 0) continue;
            if (mapping.containsKey(String.valueOf(cipher)))
                pairs.add(new String[]{ String.valueOf(cipher), String.valueOf(plain) });
        }


        for (int i = 0; i < pairs.size() - 1; i++) {
            enterLetter(pairs.get(i)[0], pairs.get(i)[1]);
        }


       enterLetter(pairs.get(pairs.size() - 1)[0], pairs.get(pairs.size() - 1)[1]);
    }
    
    public void displayPlayerMapping(){
        ArrayList<String> encryptedLetters = new ArrayList<>();
        ArrayList<Character> guess = new ArrayList<>();
        playerGameMapping.forEach( (k, v) -> { encryptedLetters.add(k); guess.add(v);} );

        for (int i = 0; i < guess.size(); i++){
            if (guess.get(i) == null){
                System.out.print(" ");
            }
            else{
                System.out.print(guess.get(i) + "  ");
            }

        }
        System.out.println();

        for (int i = 0; i < encryptedLetters.size(); i++){
            System.out.print(encryptedLetters.get(i) + "  ");
        }
        System.out.println();
    }

    public void displayEncryptedToReal(){
        cryptogram.displayEncryptedToReal();
    }

    public boolean checkGameCompletion(){
        if (playerGameMapping.size() == uniqueCorrectCounter) {
            System.out.println();
            System.out.println("*************************************************************");
            System.out.println("You have won the game!");
            System.out.println("*************************************************************");
            currentPlayer.incrementCryptogramsCompleted();
            if(currentPlayers!=null) {
                currentPlayers.updatePlayerAfterCrypto(currentPlayer);
                currentPlayers.savePlayers();
            }
            return true;
        }

        System.out.println("The cryptogram isn't quite right");
        return false;
    }

    private List<String> loadPhrases() {
        List<String> phrases = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("phrases.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) phrases.add(line);
            }
        } catch (IOException e) {
            return null;
        }
        return phrases;
    }

    public void savePlayers(){
        currentPlayers.savePlayers();
    }

    public void showLeaderboard(){currentPlayers.generateLeaderboard();}

    public void displayPlayerStats(String playerName){
        currentPlayers.displayPlayer(playerName);
    }

    public Cryptogram getCryptogram() {
        return cryptogram;
    }
    public HashMap<String, Character> getPlayerGameMapping() {
        return playerGameMapping;
    }
    public int getUniqueGuessesCounter() {
        return uniqueGuessesCounter;
    }
    public int getUniqueCorrectCounter() {
        return uniqueCorrectCounter;
    }

    public void saveGame() {

        /* Check if the file already exists */

        File saveFile = new File("savegame.txt");
        if (saveFile.exists()) {

            System.out.println("A save file already exists. Overwrite it? (Y/N):");
            String response = getInputAndValidate();
            while (!response.equals("Y") && !response.equals("N")) {

                System.out.println("Invalid input. Enter Y or N:");
                response = getInputAndValidate();
            }

            if (response.equals("N")) {
                System.out.println("Save cancelled.");
                return;
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("savegame.txt"))) {

            /* Figure out what the cryptogram is so we can setup its saved state */
        
            String type = (cryptogram instanceof LetterCryptogram) ? "L" : "N";
            bw.write("TYPE:" + type);
            bw.newLine();

            bw.write("PHRASE:" + cryptogram.getPhrase());
            bw.newLine();
        
            /* Build the cipher map */

            if (cryptogram instanceof LetterCryptogram lc) {

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 26; i++) {

                    sb.append(lc.getLetterToCipher()[i] == 0 ? '_' : lc.getLetterToCipher()[i]);

                    if (i < 25) {
                        sb.append(",");
                    }
                }

                bw.write("CIPHER_MAP:" + sb);

            } else {

                NumberCryptogram nc = (NumberCryptogram) cryptogram;
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < 26; i++) {

                    sb.append(nc.getLetterToNumber()[i]);

                    if (i < 25) {
                        sb.append(",");
                    }
                }

                bw.write("CIPHER_MAP:" + sb);
            }

            bw.newLine();

            /* set the guesses, if the guess is null we can say its blank via _ */
            StringBuilder guesses = new StringBuilder();

            for (Map.Entry<String, Character> entry : playerGameMapping.entrySet()) {

                guesses.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue() == null ? "_" : entry.getValue())
                        .append(";");
            }

            bw.write("GUESSES:" + guesses);
            bw.newLine();

            bw.write("CORRECT:" + uniqueCorrectCounter);
            bw.newLine();
            bw.write("TOTAL:" + uniqueGuessesCounter);
            bw.newLine();

            System.out.println("Game saved successfully.");

        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }



    public boolean loadGame() {

        File saveFile = new File("savegame.txt");
        if (!saveFile.exists()) {
            System.out.println("No save file found.");
            return false;
        }

        /* If a game is already ongoing, prompt the user */
    
        if (cryptogram != null) {

            System.out.println("A game is already in progress. Load saved game anyway? (Y/N):");

            String response = getInputAndValidate();

            while (!response.equals("Y") && !response.equals("N")) {
                System.out.println("Invalid input. Enter Y or N:");
                response = getInputAndValidate();
            }

            if (response.equals("N")) {
                System.out.println("Load cancelled.");
                return false;
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader("savegame.txt"))) {

            String type = null, phrase = null, cipherMapRaw = null, guessesRaw = null;
            int correct = 0, total = 0;

            String line;
            while ((line = br.readLine()) != null) {

                if (line.startsWith("TYPE:")) {

                    type = line.substring(5);

                } else if (line.startsWith("PHRASE:")) {

                    phrase = line.substring(7);

                } else if (line.startsWith("CIPHER_MAP:")) {

                    cipherMapRaw = line.substring(11);

                } else if (line.startsWith("GUESSES:")) {

                    guessesRaw = line.substring(8);

                } else if (line.startsWith("CORRECT:")) {

                    correct = Integer.parseInt(line.substring(8));

                } else if (line.startsWith("TOTAL:")) {

                    total = Integer.parseInt(line.substring(6));
                }
            }

            if (type == null || phrase == null || cipherMapRaw == null || guessesRaw == null) {
                System.out.println("Save file is corrupted or incomplete.");
                return false;
            }

            /* We need to build the game back up */
        
            String[] mapTokens = cipherMapRaw.split(",");

            if (type.equals("L")) {

                char[] ltc = new char[26];
                for (int i = 0; i < 26; i++) {
                    ltc[i] = mapTokens[i].equals("_") ? 0 : mapTokens[i].charAt(0);
                }

                this.letterToCipher = ltc;
                cryptogram = new LetterCryptogram(phrase, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", ltc);

            } else {
            
                int[] ltn = new int[26];
                for (int i = 0; i < 26; i++) {
                    ltn[i] = Integer.parseInt(mapTokens[i]);
                }

                cryptogram = new NumberCryptogram(phrase, "1-26", ltn);
            }

            /* Rebuild the guesses */
        
            playerGameMapping.clear();
            if (!guessesRaw.isEmpty()) {

                for (String entry : guessesRaw.split(";")) {

                    if (entry.isEmpty()) {
                        continue;
                    }

                    String[] parts = entry.split("=");

                    String key = parts[0];
                    Character value = (parts.length > 1 && !parts[1].equals("_"))
                            ? parts[1].charAt(0)
                            : null;
                    playerGameMapping.put(key, value);

                    /* Apply the guesses */
                
                    if (value != null) {

                        if (cryptogram instanceof LetterCryptogram) {
                            cryptogram.putCipherLetter(key.charAt(0), value);
                        } else {
                            cryptogram.putCipherLetter(Integer.parseInt(key), value);
                        }
                    }
                }
            }

            /* Rebuild the counters */
            uniqueCorrectCounter = correct;
            uniqueGuessesCounter = total;

            System.out.println("Game loaded successfully.");
            displayCurrentState();
            return true;

        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading game: " + e.getMessage());
            return false;
        }
    }

    public String displayCryptogramPhrase(){
        return cryptogram.getPhrase();
    }


    public boolean getHint() {
        // Look for cipher values that has not been correctly mapped yet
        String cipherHintValue = null;
        for (String cipherValue : playerGameMapping.keySet()) {
            char correctPlain = getCorrectPlainLetter(cipherValue);
            Character guessed = playerGameMapping.get(cipherValue);

            // Only hint an unmapped or incorrectly mapped slot
            if (guessed == null || guessed != correctPlain) {
                cipherHintValue = cipherValue;
                break;
            }
        }

        // If no more unmapped values left then cryptogram is completed
        if (cipherHintValue == null) {
            return checkGameCompletion();
        }

        char correctPlain = getCorrectPlainLetter(cipherHintValue);

        // If correct letter is already mapped in wrong place then remove that mapping, and set it to null before applying the hint
        for (String cipherValue : playerGameMapping.keySet()) {
            Character guessed = playerGameMapping.get(cipherValue);
            if (guessed != null && guessed == correctPlain && !cipherValue.equals(cipherHintValue)) {
                System.out.println("\nHint: removing incorrect mapping '" + cipherValue  + "' = '" + correctPlain + "'");

                // Undo the wrong mapping of correct letter
                if (cryptogram instanceof LetterCryptogram) {
                    // Undo for letter cryptograms
                    if (cryptogram.correctGuessCheck(cipherValue.charAt(0), correctPlain)) {
                        uniqueCorrectCounter--;
                    }
                    cryptogram.undoCipherLetter(cipherValue.charAt(0));
                } else {
                    // Undo for number cryptograms
                    if (cryptogram.correctGuessCheck(cipherValue, correctPlain)) {
                        uniqueCorrectCounter--;
                    }
                    cryptogram.undoCipherLetter(Integer.parseInt(cipherValue));
                }
                // Set cipher value to null
                playerGameMapping.put(cipherValue, null);
                break;
            }
        }

        // Remove any existing mapping on the hint cipher itself if there is one, and set it to null before applying the hint
        if (playerGameMapping.get(cipherHintValue) != null) {
            if (cryptogram instanceof LetterCryptogram) {
                // Undo for letter cryptograms
                cryptogram.undoCipherLetter(cipherHintValue.charAt(0));
            } else {
                // Undo for number cryptograms
                cryptogram.undoCipherLetter(Integer.parseInt(cipherHintValue));
            }
            // Set cipher value to null
            playerGameMapping.put(cipherHintValue, null);
        }

        // Apply the correct mapping to the hint cipher
        playerGameMapping.put(cipherHintValue, correctPlain);
        if (cryptogram instanceof LetterCryptogram) {
            // Apply hint for letter cryptogram
            cryptogram.putCipherLetter(cipherHintValue.charAt(0), correctPlain);
        } else {
            // Apply hint for number cryptogram
            cryptogram.putCipherLetter(Integer.parseInt(cipherHintValue), correctPlain);
        }
        uniqueCorrectCounter++;

        System.out.println("\nHint: Encrypted letter '" + cipherHintValue + "' = '" + correctPlain + "'");
        displayCurrentState();

        // Check if the game is now complete
        if (uniqueCorrectCounter == playerGameMapping.size()) {
            return checkGameCompletion();
        }
        return false;
    }

    // Helper method to get the correct plain letter for a given cipher value
    private char getCorrectPlainLetter(String cipherValue) {
        if (cryptogram instanceof LetterCryptogram) {
            return ((LetterCryptogram) cryptogram).getCorrectPlainLetter(cipherValue.charAt(0));
        } else {
            return ((NumberCryptogram) cryptogram).getCorrectPlainLetter(Integer.parseInt(cipherValue));
        }
    }

    public void displayFrequencies() {

        if (cryptogram instanceof LetterCryptogram lc) {
            
            int[] cipherCounts = new int[26];
            for (char c : lc.getPhrase().toCharArray()) {

                if (c >= 'A' && c <= 'Z') {

                    char cipher = lc.getLetterToCipher()[c - 'A'];

                    if (cipher != 0) {
                        cipherCounts[cipher - 'A']++;
                    }
                }
            }

            for (int i = 0; i < 26; i++) {
                if (cipherCounts[i] > 0) {
                    System.out.printf("%c: %d%n", (char)('A' + i), cipherCounts[i]);
                }
            }

        } else {
            NumberCryptogram nc = (NumberCryptogram) cryptogram;

            int[] cipherCounts = new int[27];
            for (char c : nc.getPhrase().toCharArray()) {

                if (c >= 'A' && c <= 'Z') {

                    int cipher = nc.getLetterToNumber()[c - 'A'];

                    if (cipher != 0) {
                        cipherCounts[cipher]++;
                    }
                }
            }
        
            for (int i = 1; i <= 26; i++) {
                if (cipherCounts[i] > 0) {
                    System.out.printf("%d: %d%n", i, cipherCounts[i]);
                }
            }
        }
    }
}