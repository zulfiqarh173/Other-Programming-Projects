public class Player {
    String username;
    Double accuracy ;
    int totalGuesses ;
    int cryptogramPlayed ;
    int cryptogramCompleted ;
    int correctGuesses;

    // Player Class Constructor
    public Player(String username, Double accuracy, Integer totalGuesses, Integer cryptogramPlayed, Integer cryptogramCompleted, Integer correctGuesses) {
        this.username = username;
        this.accuracy = accuracy;
        this.totalGuesses = totalGuesses;
        this.cryptogramPlayed = cryptogramPlayed;
        this.cryptogramCompleted = cryptogramCompleted;
        this.correctGuesses = correctGuesses;
    }

    public Player(String username){
        this.username = username;
        this.accuracy = 0.0;
        this.totalGuesses = 0;
        this.cryptogramPlayed = 0;
        this.cryptogramCompleted = 0;
        this.correctGuesses = 0;
    }

    /*
    Player Class Methods
     */
    public void updateAccuracy() {
        if (totalGuesses == 0) {
            accuracy = 0.0;
        } else {
            accuracy = ((double) correctGuesses / totalGuesses) * 100.0;
        }
    }

    public void incrementCryptogramsCompleted() {cryptogramCompleted++;}
    public void incrementCryptogramsPlayed() {cryptogramPlayed++;}
    public void incrementCorrectGuesses(){correctGuesses++;}
    public void incrementTotalGuesses(){totalGuesses++;}

    /*
     Getters / Setters Methods
     */
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public Double getAccuracy() {
        return accuracy;
    }
    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getTotalGuesses() {
        return totalGuesses;
    }
    public void setTotalGuesses(int totalGuesses) {
        this.totalGuesses = totalGuesses;
    }

    public Integer getNumCryptogramPlayed() {
        return cryptogramPlayed;
    }
    public void setCryptogramPlayed(int cryptogramPlayed) {
        this.cryptogramPlayed = cryptogramPlayed;
    }

    public Integer getNumCryptogramCompleted() {
        return cryptogramCompleted;
    }
    public void setCryptogramCompleted(int cryptogramCompleted) {
        this.cryptogramCompleted = cryptogramCompleted;
    }

    public Integer getCorrectGuesses() {
        return correctGuesses;
    }
    public void setCorrectGuesses(int correctGuesses) {this.correctGuesses = correctGuesses;}

}