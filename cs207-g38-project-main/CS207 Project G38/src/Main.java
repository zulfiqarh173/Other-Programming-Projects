import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Game game = new Game();

        // Prompting player to select a cryptogram type
        System.out.println("*************************************************************");
        System.out.println("Would you like to play a Letter or Number cryptogram? (L/N): ");
        System.out.println("*************************************************************");

        String type = scanner.nextLine().trim().toUpperCase();

        if (type.equals("L") || type.equals("N")) {
            game.generateCryptogram(type);
        } else {
            System.out.println("Invalid choice. Exiting.");
            return;
        }

        // Main gameplay loop
        boolean gameplay = true;
        while (gameplay) {
            System.out.println("--- MENU ---");
            System.out.println("1. Enter a letter");
            System.out.println("2. Undo a letter");
            System.out.println("3. Display current state");
            System.out.println("4. Get a hint");
            System.out.println("5. Load stats");
            System.out.println("6. Save game");
            System.out.println("7. Load game");
            System.out.println("8. View leaderboard");
            System.out.println("9. Quit");
            System.out.println("10. Complete Cryptogram and Show solution");
            System.out.println("11. View cryptogram frequencies");
            System.out.print("Enter input from menu (1-11): ");

            String input = scanner.nextLine().trim();

            switch (input) {

                case ("1"):
                    System.out.println("Enter the encrypted letter/number you want to enter your guess input into");
                    String encrypLett = game.getInputAndValidate();
                    System.out.println("Enter the encrypted letter/number you want to enter your guess input into");

                    String userGuess = game.getInputAndValidate();
                    boolean validLetterMapping = game.enterLetter(encrypLett, userGuess);
                    boolean won = game.checkGameCompletion();

                    if (won) {
                        gameplay = false;
                    }
                    break;

                case ("2"):
                    game.undoLetter("", false, false);
                    break;

                case ("3"):
                    game.displayCurrentState();
                    break;

                case ("4"):

                    won = game.getHint();
                    if (won) {
                        gameplay = false;
                    }
                    break;

                case ("5"):

                    System.out.println("Enter the user you want to view the stats of: ");
                    game.displayPlayerStats(scanner.nextLine());
                    break;

                case ("6"):

                    game.saveGame();
                    break;

                case ("7"):

                    game.loadGame();
                    break;

                case ("8"):
                    game.showLeaderboard();
                    break;

                case ("9"):

                    game.savePlayers();
                    gameplay = false;
                    break;

                case ("10"):

                    game.completeCryptogram();
                    game.displayCurrentState();
                    System.out.println(game.displayCryptogramPhrase());
                    gameplay = false;
                    break;

                case ("11"):
                    game.displayFrequencies();
                    break;

                default:
                    System.out.println("*************************************************************");
                    System.out.println("Invalid option. Please choose 1-11.");
                    System.out.println("*************************************************************");
            }
        }
    }
}