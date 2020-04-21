package de.eahjena.wi.campusnavigationeahjena.models;

import java.util.ArrayList;

public class Transition extends Cell{

    private static final String TAG = "Transition"; //$NON-NLS

    //Variables
    private int id;
    private String typeOfTransition; //stair, elevator, crossing
    private ArrayList<String> reachableFloors; //ug, 00, 01, 02, 03, 04

    //Constructor
    public Transition() {
    }

    //TODO: Transitions without stairs or elevators

    //Getter
    public int getId() {
        return id;
    }

    public String getTypeOfTransition() {
        return typeOfTransition;
    }

    public ArrayList<String> getReachableFloors() {
        return reachableFloors;
    }

    //Setter
    public void setId(int id) {
        this.id = id;
    }

    public void setTypeOfTransition(String typeOfTransition) {
        this.typeOfTransition = typeOfTransition;
    }

    public void setReachableFloors(ArrayList<String> reachableFloors) {
        this.reachableFloors = reachableFloors;
    }
}
