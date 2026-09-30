import java.util.HashMap;

public abstract class Cryptogram {

    /* attributes */

    protected String phrase;
    protected String cryptogramAlphabet;
    protected int[] frequency;

    /* constructor */
    
    public Cryptogram(String phrase, String cryptogramAlphabet) {
        this.phrase = phrase;
        this.cryptogramAlphabet = cryptogramAlphabet;
        this.frequency = new int[26];
    }

    public abstract char getPlainLetter(Object cipher);
    public abstract void putCipherLetter(Object cipher, char letter);
    public abstract void undoCipherLetter(Object cipher);
    public abstract void displayEncryptedToReal();
    public abstract boolean correctGuessCheck(Object cipher, char letter);

    /* getter setters */
    public String getPhrase() {
        return phrase;
    }

    public int[] getFrequencies() {
        return frequency;
    }
    
    public int getFrequency(char letter) {
        letter = Character.toUpperCase(letter);
        return frequency[letter - 'A'];
    }

    public void incrementFrequency(char letter) {
        letter = Character.toUpperCase(letter);
        frequency[letter - 'A']++;
    }

    public void decrementFrequency(char letter) {
        letter = Character.toUpperCase(letter);
        frequency[letter - 'A']--;
    }

    public String getCryptogramAlphabet() {
        return cryptogramAlphabet;
    }

    public abstract void display(HashMap<String, Character> playerGameMapping);
}