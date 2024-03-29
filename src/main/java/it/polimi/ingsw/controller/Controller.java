package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.characterCards.*;
import it.polimi.ingsw.controller.characterCards.Character;
import it.polimi.ingsw.model.Creature;
import it.polimi.ingsw.model.Match;
import it.polimi.ingsw.messages.serverMessages.AckMessage;
import it.polimi.ingsw.messages.serverMessages.MatchStartMessage;
import it.polimi.ingsw.messages.Message;
import it.polimi.ingsw.server.ClientHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class Controller {
    /**
     * This attribute tells if the match has already started (no more player allowed in) -> [1]
     * or if it's waiting for other players to join -> [0]
     */
    private boolean playing;
    /**
     * This attribute is the current state of the match
     */
    private ControllerState state = null;
    /**
     * This attribute is the reference to the Model of the match
     */
    private Match match = null;
    /**
     * This attribute is the reference to the characters' manager of the match
     */
    private CharactersManager charactersManager = null;
    /**
     * This attribute is the number of players needed to start the match
     */
    private int numberOfPlayers;
    /**
     * This attribute tells if the match must ber played in expert mode or not
     */
    private boolean expertMode;
    /**
     * This attribute is the identifier of the match
     */
    private final int match_ID;
    /**
     * This attribute is the last message received from the client
     */
    private String msg_in = null;
    /**
     * This attribute is an array of references to all the ClientHandler of the players playing this match
     */
    private ArrayList<ClientHandler> clientHandlers;
    /**
     * This attribute is the list of names chosen by the player
     */
    private ArrayList<String> playersNickname;
    /**
     * This attribute is the counter of players added to match
     */
    private int playersAddedCounter;
    /**
     * This attribute represents the playing order of the action phase;
     * it was determined inside the 'ChooseAssistantCard' state
     */
    private ArrayList<Integer> actionPhaseOrder;
    /**
     * This attribute says if we are playing the action phase (TRUE) or the planning phase (FALSE);
     * it's used in the 'nextPlayer()' method
     */
    private boolean actionPhase;
    /**
     * This attribute is the ID of the current player playing its action phase
     */
    private int actionPhaseCurrentPlayer = -1;
    /**
     * This attribute is the list of disconnected players where the indexes correspond to the IDs of the players
     */
    private ArrayList<Boolean> playersDisconnected;
    /**
     * This attribute is set to true if the match is ended and false otherwise
     */
    private boolean matchEnded;

    /**
     * Controller constructor
     * @param ID the ID of the match
     */
    public Controller(int ID){
        this.match_ID = ID;
        this.playing = false;
        this.clientHandlers = new ArrayList<ClientHandler>();
        this.playersNickname = new ArrayList<String>();
        this.actionPhaseOrder = new ArrayList<Integer>();
        this.state = new MatchCreating();
        this.playersAddedCounter = 0;
        this.actionPhase = false;

        this.playersDisconnected = new ArrayList<Boolean>();

        matchEnded = false;
    }

    /**
     * This method adds a player to the match and controls if there are enough players to start playing.
     * Furthermore, it sends the notify message "MatchWaiting" to the player that is just been added
     * @param playerHandler reference to the ClientHandler of the added player
     * @param nickname nickname chosen by the player (client) and approved by the ClientHandler
     * @return ID of the just added player
     */
    public int addPlayerHandler(ClientHandler playerHandler, String nickname){
        this.clientHandlers.add(playerHandler);
        this.playersNickname.add(nickname);

        playersAddedCounter++;

        /*while for the first player, that is the one who chose the match settings, the notification message "MatchWaiting" is
        sent from the MatchCreation state execution, we must notify the other players too; here we send the message to the
        added player*/
        if(playersAddedCounter > 1){
            AckMessage ack = new AckMessage();
            ack.setSubObject("waiting");
            sendMessageToPlayer(playersAddedCounter-1, ack);
        }

        if(playersAddedCounter == numberOfPlayers){
            startMatch();
        }

        return clientHandlers.size() - 1;
    }
    /**
     * This method lets the match start, chooses the first player of the match randomly, and finally it
     * sends the message to all the players through the ClientHandlers
     */
    public void startMatch(){

        this.match = new Match(this.match_ID, this.numberOfPlayers, expertMode);
        //adds all the players
        for(String s: playersNickname){
            this.match.addPlayer(s);
        }

        //chooses the first player of the match
        Random random = new Random();
        int firstPlayer_ID = random.nextInt(numberOfPlayers);

        //set the current player inside the model
        match.setCurrentPlayer(firstPlayer_ID);

        // get initial position of mother nature
        int motherNatureInitialPosition = match.getPositionOfMotherNature();

        // create the startOfMatch message
        MatchStartMessage startMessage = new MatchStartMessage(firstPlayer_ID, motherNatureInitialPosition, numberOfPlayers, expertMode);
        startMessage.setNicknames(this.playersNickname);

        // add characters if the match will be played in expert mode
        if(expertMode){
            this.charactersManager = new CharactersManager(this);
            Set<String> characters = charactersManager.chooseCharacter();
            startMessage.setCharacters(characters);

            HashMap<String, Character> cards = charactersManager.getCards();

            if(cards.containsKey("monk")){
                Monk ch = (Monk)cards.get("monk");
                startMessage.setMonkStudents(ch.getStudents());
            }

            if(cards.containsKey("jester")){
                Jester ch = (Jester) cards.get("jester");
                startMessage.setJesterStudents(ch.getStudentsOnCard());
            }

            if(cards.containsKey("princess")){
                Princess ch = (Princess) cards.get("princess");
                startMessage.setPrincessStudents(ch.getStudentsOnPrincess());
            }
        }
        // add the students in the entrance of each player
        for(int i = 0; i < clientHandlers.size(); i++){
            ArrayList<Creature> students = match.getPlayerByID(i).getSchoolBoard().getEntrance().getStudentsInTheEntrance();
            startMessage.setStudentsInEntrance(i, students);
        }

        // add initial students put on each island
        ArrayList<Creature> studentOnEachIsland = match.getInitialStudentsOnEachIsland();
        startMessage.setStudentsOnIslands(studentOnEachIsland);

        sendMessageAsBroadcast(startMessage);
        playing = true;
        nextState();
    }

    /**
     * This methode sends a message to one particular player
     * @param player_ID ID of the addressee
     * @param msg msg that must be sent to the player
     */
    public void sendMessageToPlayer(int player_ID, Message msg){
        ClientHandler addressee = clientHandlers.get(player_ID);
        addressee.messageToSerialize(msg);
    }

    /**
     * This method sends a message to all players of the match
     * @param msg the message that must be sent to the players
     */
    public void sendMessageAsBroadcast(Message msg){
        for(ClientHandler p: clientHandlers){
            p.messageToSerialize(msg);
        }
    }

    /**
     * This method hands the message received by the client over to the state
     * @param msg message sent by the client
     */
    public void manageMsg(String msg){
        msg_in = msg;

        // the state will control the type of message and will execute the actions required
        state.controlMessageAndExecute(this);
    }

    /**
     * This method calls the current state asking for the next one
     */
    public void nextState(){
        state.nextState(this);
    }

    /**
     * This method sets the new state of the controller; it called exclusively by the current state of the controller.
     * controller.nextState() --> state.nextState() --> controller.setState()
     * @param cs the new state of the controller
     */
    public void setState(ControllerState cs){
        this.state = cs;
    }

    /**
     * This method sets the player as disconnected and if the match has not already begun then it
     * sends an EndOfMatchMessage, while if the match has already begun then it calls the endMatch
     * method in SupportFunctions to compute the winner if necessary and to send the EndOfMatchMessage
     * @param playerID ID of the player who disconnected
     */
    public void onePlayerDisconnected(int playerID){
        playersDisconnected.set(playerID, true);

        if(!playing){
            this.matchEnded = true;
            sendMessageAsBroadcast(new EndOfMatchMessage(-1, "", "disconnection_in_waiting"));
            this.playing = false;
        }else if(playing){

            if(numberOfPlayers == 2){
                int lastPlayer_ID = -1;
                // take the ID of the player that did not disconnect
                for(int i = 0; i < numberOfPlayers; i++){
                    if(!playersDisconnected.get(i)){
                        assert lastPlayer_ID == -1 : "There should be only one player still connected";
                        lastPlayer_ID = i;
                    }
                }
                // end match with winner
                SupportFunctions.endMatch(this, "disconnection", lastPlayer_ID);
            }else if(numberOfPlayers == 3){
                // end match with winner computation
                SupportFunctions.endMatch(this, "disconnection");
            }
            this.playing = false;
        }
    }

    /**
     * This method increases of 1 the number of players because one player reconnected to match
     * @param playerID ID of the player who reconnected
     */
    public void onePlayerReconnected(int playerID){
        playersDisconnected.set(playerID, false);
    }

    /**
     * This method computes the ID of the next player to make his move
     * @param currentPlayer ID of the current player
     * @return ID of the next player
     */
    public int nextPlayer(int currentPlayer){
        int nextPlayerID;

        if(actionPhase){
            int indexOfCurrentPlayer = actionPhaseOrder.indexOf(currentPlayer);
            int i = 1;

            while(playersDisconnected.get(actionPhaseOrder.get((indexOfCurrentPlayer + i) % numberOfPlayers))){
                i++;
            }
            nextPlayerID = actionPhaseOrder.get((indexOfCurrentPlayer + 1) % numberOfPlayers);

        }else{
            nextPlayerID = ((currentPlayer + 1) % numberOfPlayers);
        }

        return nextPlayerID;
    }

    public int getMatchID(){
        return this.match_ID;
    }

    public String getMsg(){
        return msg_in;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean getPlayingStatus(){
        return playing;
    }

    public void setExpertMode(boolean mode){
        this.expertMode = mode;
    }

    public boolean isExpertMode() {
        return expertMode;
    }

    public ArrayList<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public void setActionPhaseOrder(ArrayList<Integer> actionPhaseOrder) {
        this.actionPhaseOrder = actionPhaseOrder;
    }

    public ArrayList<Integer> getActionPhaseOrder() {
        return actionPhaseOrder;
    }

    public void setActionPhase(boolean actionPhase) {
        this.actionPhase = actionPhase;
    }

    public boolean isActionPhase() {
        return actionPhase;
    }

    public int getPlayersAddedCounter() {
        return playersAddedCounter;
    }

    public Match getMatch(){
        return this.match;
    }

    public int getNumberOfPlayers(){
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers){
        this.numberOfPlayers = numberOfPlayers;
        // set to false the values of disconnections' array
        for(int i = 0; i < numberOfPlayers; i++){
            playersDisconnected.add(false);
        }
    }

    public void setActionPhaseCurrentPlayer(int actionPhaseCurrentPlayer) {
        this.actionPhaseCurrentPlayer = actionPhaseCurrentPlayer;
    }

    public int getActionPhaseCurrentPlayer() {
        return actionPhaseCurrentPlayer;
    }

    public ArrayList<String> getPlayersNickname() {
        return playersNickname;
    }

    public ArrayList<Boolean> getPlayersDisconnected() {
        return playersDisconnected;
    }

    public void setMatchEnded(boolean matchEnded) {
        this.matchEnded = matchEnded;
    }

    public boolean isMatchEnded() {
        return matchEnded;
    }

    public CharactersManager getCharactersManager() {
        return charactersManager;
    }
}
