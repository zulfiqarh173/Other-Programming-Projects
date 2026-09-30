import java.util.HashMap;

public class NumberCryptogram extends Cryptogram {

    protected char[] cipherToLetter;
    protected int[] letterToNumber;

    /* constructors */

    public NumberCryptogram(String phrase, String cryptogramAlphabet, int[] letterToNumber) {
        super(phrase, cryptogramAlphabet);
        this.cipherToLetter = new char[27];
        this.letterToNumber = letterToNumber;
    }

    public NumberCryptogram(String filePath) {
        /* should load from file */
        super(" ", " ");
        this.cipherToLetter = new char[27];
        this.letterToNumber = new int[26];
    }

    /* getter setters */

    public int[] getLetterToNumber() {
        return letterToNumber;
    }

    @Override
    public void putCipherLetter(Object cipherObj, char letter) {

        int cipher = (Integer)cipherObj;
        letter = Character.toUpperCase(letter);

        cipherToLetter[cipher] = letter;
        incrementFrequency(letter);
    }

    @Override
    public void undoCipherLetter(Object cipherObj) {

        int cipher = (Integer)cipherObj;
        char letter = cipherToLetter[cipher];

        if (letter != 0) { 
            cipherToLetter[cipher] = 0;
            decrementFrequency(letter);
        }
    }

    @Override
    public char getPlainLetter(Object cipherObj) {

        int cipher = (Integer)cipherObj;
        return cipherToLetter[cipher];
    }

    public boolean correctGuessCheck(Object cipherObj,char letter) {

                try {
                    int cipher =  Integer.valueOf(cipherObj.toString());
                    letter = Character.toUpperCase(letter);

                    if((cipherToLetter[cipher] == letter) && letterToNumber[letter - 'A']==cipher){
                        return true;
                    }
                    return false;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid integer input");
                    return false;
                }

    }

    @Override
    public void display(HashMap<String, Character> playerGameMapping) {
        // Printing the encrypted line
        System.out.print("Encrypted Numbers: ");
        for (char c : phrase.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                System.out.printf("%-3d", letterToNumber[c - 'A']);
            } else {
                System.out.print(" | ");
            }
        }

        // Printing the guess line
        System.out.print("\nCurrent guesses:   ");
        for (char c : phrase.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                int cipher = letterToNumber[c - 'A'];
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
    
    @Override
    public void displayEncryptedToReal(){
    }

    public char getCorrectPlainLetter(int cipher) {
        for (int i = 0; i < 26; i++) {
            if (letterToNumber[i] == cipher) return (char) ('A' + i);
        }
        return 0;
    }
}