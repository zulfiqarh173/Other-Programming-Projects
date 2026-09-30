import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


public class Players {
    private ArrayList<Player> allPlayers= new ArrayList<>();
    File playerFile = new File("players.txt");

    Players(){
        loadPlayerFile();
    }



    public Players(String filename) {
        this.playerFile = new File(filename);
    }

    public void addPlayer(Player player){
        allPlayers.add(player);

    }

    public Player findPlayer(String playerName){
        for(Player player : allPlayers){
            if (player.getUsername().equals(playerName)){

                return player;


            }

        }

        return null;

    }

    public void updatePlayerAfterGuess(Player playerToUpdate){
        for(Player player : allPlayers){
            if (player.getUsername().equals(playerToUpdate.getUsername())){

                player.setTotalGuesses(playerToUpdate.getTotalGuesses());
                player.setCorrectGuesses(playerToUpdate.getCorrectGuesses());
                playerToUpdate.updateAccuracy();
                player.setAccuracy(playerToUpdate.getAccuracy());
                return;

            }

        }
        throw new RuntimeException("Something went wrong");


    }
    public void updatePlayerAfterCrypto(Player playerToUpdate){
        for(Player player : allPlayers){
            if (player.getUsername().equals(playerToUpdate.getUsername())){

                player.setCryptogramCompleted(playerToUpdate.getNumCryptogramCompleted());
                player.setCryptogramPlayed(playerToUpdate.getNumCryptogramPlayed());
                return;
            }

        }
        throw new RuntimeException("Something went wrong");


    }

    public void generateLeaderboard(){
        if (allPlayers.size() <= 0){
            System.out.print("No players added yet\n");
            return;
        }

        for (int i = 1; i < allPlayers.size(); i++){
            int j=i-1;
            if(allPlayers.get(i).getNumCryptogramPlayed()==0){
                allPlayers.get(i).setCryptogramPlayed(1);
            }
            if (allPlayers.get(j).getNumCryptogramPlayed()==0){
                allPlayers.get(j).setCryptogramPlayed(1);
            }

            double currentRatio = (double) allPlayers.get(i).getNumCryptogramCompleted() / allPlayers.get(i).getNumCryptogramPlayed();
            Player currentPlayer= allPlayers.get(i);
                while(j>-1&&currentRatio > (double)allPlayers.get(j).getNumCryptogramCompleted()/ (double)allPlayers.get(j).getNumCryptogramPlayed()) {
                    allPlayers.set(j+1, allPlayers.get(j));
                    j--;

                }
                allPlayers.set(j+1, currentPlayer);

        }

        for (int i = 0; i < 10; i++){
            if (allPlayers.size()<=i){
                System.out.print(i+1+":\n\n");
                continue;

            }

            Player currentPlayer = allPlayers.get(i);
            System.out.print(i+1+":\n"+currentPlayer.getUsername() +" - "+ (double) currentPlayer.getNumCryptogramCompleted()/currentPlayer.getNumCryptogramPlayed()*100 +"% of crpytograms completeted\n");

        }


        /*
        if (allPlayers.size() >= 0){
            System.out.println("No players added yet");
        }
        System.out.println("\n*************************************************************\n");

        double currentMax=Double.MIN_VALUE;
        String currentPlayer = "";

        double previousMax=Double.MAX_VALUE;


        for (int i = 0; i < 10; i++){
            currentMax= Double.MIN_VALUE;
            for(Player player : allPlayers){


                if( player.getNumCryptogramCompleted()/player.getNumCryptogramPlayed()>currentMax&&(currentMax<=previousMax && !currentPlayer.equals(player.getUsername()))){
                    currentMax = player.getNumCryptogramCompleted()/ player.getNumCryptogramPlayed();
                    currentPlayer = player.getUsername();

                }


            }

            previousMax=currentMax;

            if (currentMax==0){
                System.out.println(i+1+":\n"+currentPlayer +" - "+currentMax*100 +"% of crpytograms completeted");
            }

            else if(currentMax==Double.MIN_VALUE){
                System.out.println(i+1+":\n");

            }

            else{
                System.out.println(i+1+":\n"+currentPlayer +" - "+currentMax*100 +"% of crpytograms completeted");
            }



        }
        System.out.println("\n*************************************************************\n");

         */


    }

    public int getPos(String playerName){
        for (int i = 0; i < allPlayers.size(); i++){
            if (allPlayers.get(i).getUsername().equals(playerName)){
                return i;
            }
        }
        return -1;
    }






    public Player loginPlayer(){
        Scanner sc = new Scanner(System.in);
        System.out.println("*************************************************************");
        System.out.println("Enter your username: ");
        System.out.println("*************************************************************");
        String playerName = sc.nextLine();

        Player player = findPlayer(playerName);
       if (player != null){
           System.out.println("Welcome "+ playerName );
           return player;
       }

        System.out.println("Player not found, create new Player? (Y/N)");

        String input = sc.nextLine();
        while (!input.equalsIgnoreCase("Y") && !input.equalsIgnoreCase("N")){
            System.out.println("Invalid input please enter Y or N, create new Player? (Y/N)");
            input = sc.nextLine();

        }
        if (input.equalsIgnoreCase("Y")){
            addPlayer(new Player(playerName));
            return new Player(playerName);
        }
        System.out.println("Player not found or created.");
        System.out.println("Exiting...");
        System.exit(0);

        return null;
    }

    public void displayPlayer(String playerName){
        Player player = findPlayer(playerName);
        if (player == null){
            System.out.print("\n Player not found :(\n");
            return;
        }
        if (player.getNumCryptogramPlayed() == 0){
            System.out.println("This player has played no cryptograms.");
            return;
        }

        System.out.print("*************************************************************\n");
        System.out.print(player.getUsername()+"'s stats Details:"+"\n");
        System.out.print("Accuracy: "+player.getAccuracy()+"\n");
        System.out.print("Total guesses: "+player.getTotalGuesses()+"\n");
        System.out.print("Correct guesses: "+player.getCorrectGuesses()+"\n");
        System.out.print("Cryptograms played: "+player.getNumCryptogramPlayed()+"\n");
        System.out.print("Cryptograms completed: "+player.getNumCryptogramCompleted()+"\n");
        System.out.print("*************************************************************"+"\n");

    }





    public void savePlayers(){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(playerFile))){
            for(Player player : allPlayers){
                player.updateAccuracy();

                bw.write(player.getUsername());
                bw.write(",");
                bw.write(player.getAccuracy().toString());
                bw.write(",");
                bw.write(player.getTotalGuesses().toString());
                bw.write(",");
                bw.write(player.getNumCryptogramPlayed().toString());
                bw.write(",");
                bw.write(player.getNumCryptogramCompleted().toString());
                bw.write(",");
                bw.write(player.getCorrectGuesses().toString());

                bw.newLine();
            }

        }
        catch (FileNotFoundException e){

            RuntimeException re = new RuntimeException("File not found");
        }

        catch (IOException e) {
            throw new RuntimeException(e);
        }


    }




    public void loadAllPlayersAccuracy(){
        for(Player player : allPlayers){
            System.out.println(player.getAccuracy());
        }


    }
    public void loadAllPlayersPlayed(){
        for(Player player : allPlayers){
            System.out.println(player.getNumCryptogramPlayed());
        }


    }
    public void loadAllPlayersCompleted(){
        for(Player player : allPlayers){
            System.out.println(player.getNumCryptogramCompleted());
        }


    }

    private void loadPlayerFile(){

        String currentLine;
        String[] tempArray = new String[6];


        try(BufferedReader br = new BufferedReader(new FileReader(playerFile))){
            while((currentLine = br.readLine()) != null){
                tempArray=currentLine.split(",");

                if (tempArray.length != 6) continue;
                addPlayer(new Player(tempArray[0],Double.parseDouble(tempArray[1]),Integer.parseInt(tempArray[2]), Integer.parseInt(tempArray[3]), Integer.parseInt(tempArray[4]),Integer.parseInt(tempArray[5])));
            }






        }catch (FileNotFoundException e){
            System.out.println("File not found");
            throw new RuntimeException(e);
        }

        catch (IOException e) {
            System.out.println("Error reading file");
            throw new RuntimeException(e);
        }



    }



}