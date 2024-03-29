package it.polimi.ingsw.Client;

import it.polimi.ingsw.model.Creature;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Tower;

import java.util.HashMap;

/**
 * This class represents a miniature of the archipelago class in the model.
 */
public class ArchipelagoView {
    /**
     * This attribute is the identifier of the archipelago.
     */
    private int archipelagoID;

    /**
     * This attribute is the number of single island-tiles that belong to the archipelago.
     */
    private int numberOfIsland;

    /**
     * This attribute tells if mother nature is on the island or not.
     */
    private boolean motherNaturePresence;

    /**
     * This attribute is the color of the Towers currently on the archipelago, or 'null' if there is no
     * tower built on it
     */
    private Tower towerColor;

    /**
     * This attribute is the number of towers on an island.
     */
    private int numberOfTower;

    /**
     * This attribute is the player that currently has the higher influence
     * over the archipelago
     */
    private int masterOfArchipelago;

    /**
     * This attribute is the population of students of each kind currently on the island.
     */
    private HashMap<Creature, Integer> studentsPopulation;

    /**
     * This attribute is the number of no-entry-tiles currently on the archipelago
     */
    private int noEntryTiles;


    /**
     * This constructor creates a new archipelago (as a single island).
     * @param archipelagoID is the ID of the archipelago.
     */
    public ArchipelagoView(int archipelagoID){
        this.archipelagoID = archipelagoID;
        this.numberOfIsland = 1;
        this.motherNaturePresence = false;
        this.masterOfArchipelago = -1;

        this.studentsPopulation = new HashMap<Creature, Integer>();
        this.studentsPopulation.put(Creature.DRAGON, 0);
        this.studentsPopulation.put(Creature.FAIRY, 0);
        this.studentsPopulation.put(Creature.FROG, 0);
        this.studentsPopulation.put(Creature.UNICORN, 0);
        this.studentsPopulation.put(Creature.GNOME, 0);
        this.numberOfTower = 0;
    }

    /**
     * Add one single student on the archipelago.
     * @param c type of student.
     */
    public void addStudent(Creature c){
        int previousValue = studentsPopulation.get(c);
        studentsPopulation.put(c, previousValue + 1);
    }



    /**
     * Adds only one no-entry-tile to the archipelago, and increases noEntryTiles by 1
     */
    public void addNoEntryTile(){
        noEntryTiles ++;
    }

    /**
     * Adds more than one no-entry-tile to the archipelago and increases noEntryTile accordingly
     * @param quantityToAdd number of no-entry-tile to add
     */
    public void addNoEntryTiles(int quantityToAdd){
        noEntryTiles += quantityToAdd;
    }

    /**
     * Removes one single no-entry-tile from the archipelago and decreases noEntryTile by 1
     */
    public void removeNoEntryTile(){
        if(noEntryTiles > 0){
            noEntryTiles --;
        }
    }

    /**
     * Compute the number of students of a particular type on the archipelago
     * @param c type of student
     * @return number of students of the specified type
     */
    public int getStudentsOfType(Creature c){
        return studentsPopulation.get(c);
    }

    /**
     * Computes the total number of students on the archipelago
     * @return total number of students
     */
    public int getTotalNumberOfStudents(){
        int sum = 0;

        for(Creature c: Creature.values()){
            sum += studentsPopulation.get(c);
        }
        return sum;
    }

    public int getArchipelagoID() {
        return archipelagoID;
    }

    public void setArchipelagoID(int archipelagoID) {
        this.archipelagoID = archipelagoID;
    }


    public int getNumberOfIsland() {
        return numberOfIsland;
    }

    public void setNumberOfIsland(int numberOfIsland) {
        this.numberOfIsland = numberOfIsland;
    }


    public boolean isMotherNaturePresence() {
        return motherNaturePresence;
    }

    public void setMotherNaturePresence(boolean motherNaturePresence) {
        this.motherNaturePresence = motherNaturePresence;
    }

    public void setStudentsPopulation(HashMap<Creature, Integer> studentsPopulation) {
        this.studentsPopulation = studentsPopulation;
    }

    public Tower getTowerColor() {
        return towerColor;
    }

    public void setTowerColor(Tower towerColorPassed){
        this.towerColor = towerColorPassed ;
    }

    public HashMap<Creature, Integer> getStudentsPopulation() {
        return studentsPopulation;
    }

    public int getNumberOfTower() {
        return numberOfTower;
    }

    public void setNumberOfTower(int numberOfTower) {
        this.numberOfTower = numberOfTower;
    }

    public int getMasterOfArchipelago() {
        return masterOfArchipelago;
    }

    public void setMasterOfArchipelago(int masterOfArchipelago) {
        this.masterOfArchipelago = masterOfArchipelago;
    }

    public int getNoEntryTiles() {
        return noEntryTiles;
    }
}
