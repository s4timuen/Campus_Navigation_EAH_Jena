package de.eahjena.wi.campusnavigationeahjena.models;

public class Cell {

    private static final String TAG = "Cell"; //$NON-NLS

    //Variables
    int heuristicCost = 0;
    int finalCost = 0;
    private String building;
    private String floor;
    int xCoordinate;
    int yCoordinate;
    Cell parent;
    boolean walkable;

    //Constructors
    public Cell() {
    }
    public Cell(int xCoordinate, int yCoordinate, boolean walkable) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.walkable = walkable;
    }

    //Getter
    public int getHeuristicCost() {
        return heuristicCost;
    }

    public int getFinalCost() {
        return finalCost;
    }

    public String getBuilding() {
        return building;
    }

    public String getFloor() {
        return floor;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public Cell getParent() {
        return parent;
    }

    public boolean getWalkability() {return walkable;}

    //Setter
    public void setHeuristicCost(int heuristicCost) {
        this.heuristicCost = heuristicCost;
    }

    public void setFinalCost(int finalCost) {
        this.finalCost =+ finalCost;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public void setXCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public void setYCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public void setParent(Cell parent) {
        this.parent = parent;
    }

    public void setWalkability(boolean walkable) {
        this.walkable = walkable;
    }

}
