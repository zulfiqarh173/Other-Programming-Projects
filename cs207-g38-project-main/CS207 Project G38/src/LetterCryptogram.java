import javax.swing.*;
import java.util.HashMap;

public class LetterCryptogram extends Cryptogram {

    /* attributes */

    protected char[] cipherToLetter;
    protected HashMap<Character, Character> encryptedToRealLetter;
    protected char[] letterToCipher;

    /* constructors */

    public LetterCryptogram(String phrase, String cryptogramAlphabet, char[] letterToCipher) {
        super(phrase, cryptogramAlphabet);
        this.cipherToLetter = new char[26];
        this.letterToCipher = letterToCipher;
        this.encryptedToRealLetter = generateEncryptedToRealLetter();

    }

    public LetterCryptogram(String filePath) {
        /* should load from file */
        super(" ", " ");
        this.cipherToLetter = new char[26];
        this.letterToCipher = new char[26];
    }

    /* getter setters */

    public char[] getLetterToCipher() {
        return letterToCipher;
    }

    @Override
    public void putCipherLetter(Object cipherObj, char letter) {

        char cipher = Character.toUpperCase((char)cipherObj);
        letter = Character.toUpperCase(letter);

        cipherToLetter[cipher - 'A'] = letter;

        incrementFrequency(letter);
    }
    private HashMap<Character, Character> generateEncryptedToRealLetter(){
        HashMap<Character, Character> map = new HashMap<>();
        char[] alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K','L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        for (int i = 0; i < this.letterToCipher.length; i++){
            map.put(alphabet[i], this.letterToCipher[i]);
        }
        return map;
    }


    @Override
    public void undoCipherLetter(Object cipherObj) {

        char cipher = Character.toUpperCase((char)cipherObj);
        char letter = cipherToLetter[cipher - 'A'];

        if (letter != 0) { 
            cipherToLetter[cipher - 'A'] = 0;
            decrementFrequency(letter);
        }
    }



    @Override
    public char getPlainLetter(Object cipherObj) {

        char cipher = Character.toUpperCase((char)cipherObj);
        return cipherToLetter[cipher - 'A'];
    }

    public boolean correctGuessCheck(Object cipherObj,char letter) {

        char cipher = Character.toUpperCase((char)cipherObj);
        letter = Character.toUpperCase(letter);

        if((cipherToLetter[cipher - 'A'] == letter) && letterToCipher[letter - 'A']==cipher){
            return true;
        }
        return false;


    }




    @Override
    public void displayEncryptedToReal(){
        this.encryptedToRealLetter.forEach( (k, v) -> {
            System.out.println(k + " : " + v);
        } );
    }

    @Override
    public void display(HashMap<String, Character> playerGameMapping) {
        // Printing the encrypted line
        System.out.print("Encrypted Letters: ");
        for (char c : phrase.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                System.out.printf("%-3c", letterToCipher[c - 'A']);
            } else {
                System.out.print(" | ");
            }
        }

        // Printing the guess line
        System.out.print("\nCurrent guesses:   ");
        for (char c : phrase.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                char cipher = letterToCipher[c - 'A'];
                Character guess = playerGameMapping.get(String.valueOf(cipher));
                if (guess != null) {
                    System.out.printf("%-3c", guess);
                } else {
                    System.out.printf("%-3c", '_');
                }
            } else {
                System.out.print(" | ");
            }
        }
        System.out.println();
    }

    public char getCorrectPlainLetter(char cipher) {
        cipher = Character.toUpperCase(cipher);
        for (int i = 0; i < 26; i++) {
            if (letterToCipher[i] == cipher) return (char) ('A' + i);
        }
        return 0;
    }
}
