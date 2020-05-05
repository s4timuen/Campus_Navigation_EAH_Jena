package de.eahjena.wi.campusnavigationeahjena.models;

import java.util.ArrayList;

public class Transition extends Cell{

    private static final String TAG = "Transition"; //$NON-NLS

    //Variables
    private String typeOfTransition; //stair, elevator, crossing
    private ArrayList<Cell> connectedCells;

    //Constructor
    public Transition() {
    }

    //Getter
    public String getTypeOfTransition() {
        return typeOfTransition;
    }

    public ArrayList<Cell> getReachableBuildings() {
        return connectedCells;
    }

    public ArrayList<Cell> getReachableFloors() {
        return connectedCells;
    }

    //Setter
    public void setTypeOfTransition(String typeOfTransition) {
        this.typeOfTransition = typeOfTransition;
    }

    public void setReachableBuildings(ArrayList<Cell> connectedCells) {
        this.connectedCells = connectedCells;
    }

    public void setReachableFloors(ArrayList<Cell> connectedCells) {
        this.connectedCells = connectedCells;
    }
}
