package it.polimi.ingsw.network.messages.serverMessages;

import it.polimi.ingsw.model.Wizard;
import it.polimi.ingsw.network.messages.Message;

import java.util.ArrayList;

public class NackMessage extends Message {

    /**
     * This attribute is the second object of the message, and it tells which data are stored in this message
     */
    String subObject = "";
    /**
     * This attribute contains a string message that can be directly printed by the client as explanation
     * of the error generated by the player's move
     */
    private String explanationMessage;

    public NackMessage(){
        this.object = "nack";
    }

    public void setSubObject(String subObject) {
        this.subObject = subObject;

        switch (subObject){
            case "assistant":
                this.explanationMessage = "Another player already chose this assistant, please choose another assistant!";
                break;
            case "invalid_mother_nature_movement":
                this.explanationMessage = "Mother nature can't move so far, please choose retry the movement!";
                break;
            case "invalid_cloud":
                this.explanationMessage = "Another player already took the students from this cloud, please choose another cloud!";
                break;
            case "character":
                this.explanationMessage = "I see yuo are not rich enough to buy the character, come back when you have more coins!";
                break;
            case "herbalist":
                this.explanationMessage = "There is a limited number of no-entry-tiles (just four) in the realm and all of them are being " +
                                          "used somewhere else right now.\nErgo you can't use a no-entry-tile!";
                break;
            case "monk":
                this.explanationMessage = "The monk has no more students, so he can't give you one student anymore." +
                                          "Sorry for the inconvenience.";
        }
    }

    public String getSubObject() {
        return subObject;
    }

    public String getExplanationMessage() {
        return explanationMessage;
    }
}

/*POSSIBLE VALUES OF "subObject":
*   1. assistant:
*      it means that the assistant card chosen is not legit because another player has already chosen it and the current
*      player has other cards that can choose
*
*   2. invalid_mother_nature_movement:
*      it means that the movement of mother nature is not legit because it does not respect the maximum distance
*      feasible for mother nature which is written on the last used assistant card
*
*   3. invalid_cloud:
*      it means that the cloud chosen had been already chosen by another player and now is empty of students
*
*   4. character_price:
*      it means that the player has not enough coins to use the character card
*
*   5. herbalist:
*      it means that there wasn't any no-entry-tile on the character, all the cards were already in use
*
*   6. monk:
*      it means that the character card monk has no more students on it*/
