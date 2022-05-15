package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.model.Wizard;

/**
 * This class represent the message sent by the client to the server containing the deck chosen
 */
public class ChosenDeckMessage extends Message{
    Wizard deck = null;

    public ChosenDeckMessage() {
        this.object = "deck";
    }

    public void setDeck(Wizard deck) {
        this.deck = deck;
    }

    public Wizard getDeck() {
        return deck;
    }
}