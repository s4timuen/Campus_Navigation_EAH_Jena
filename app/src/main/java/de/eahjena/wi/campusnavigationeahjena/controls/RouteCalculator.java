package de.eahjena.wi.campusnavigationeahjena.controls;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Room;

public class RouteCalculator {

    private static final String TAG = "RouteCalculator"; //$NON-NLS

    //Variables
    private Room startLocation;
    private Room destinationLocation;
    private ArrayList<Cell> navigationCells = new ArrayList<>();

    private ArrayList<ArrayList<Cell>> grid;
    private PriorityQueue<Cell> open;
    private boolean closed[][];
    private Cell startCell = new Cell();
    private Cell endCell = new Cell();
    private boolean keepCalculating = true;
    private int costPerCell;

    //TODO: Add transitions support (Stairs, Elevators, diff floors and buildings)
    //TODO: Transitions without stairs or elevators

    //Constructor
    public RouteCalculator(Room startLocation, Room destinationLocation, ArrayList<ArrayList<Cell>> grid) {
        this.startLocation = startLocation;
        this.destinationLocation = destinationLocation;
        this.grid = grid;
    }

    //Calculation of cells to walk
    @SuppressLint("LongLogTag")
    public ArrayList<Cell> getNavigationCells() {

        try {
            //Set start and end cells from rooms
            startCell.setXCoordinate(startLocation.getXCoordinate());
            startCell.setYCoordinate(startLocation.getYCoordinate());
            startCell.setWalkability(startLocation.getWalkability());
            endCell.setXCoordinate(destinationLocation.getXCoordinate());
            endCell.setYCoordinate(destinationLocation.getYCoordinate());
            endCell.setWalkability(destinationLocation.getWalkability());

            //Set priority queue with comparator
            open = new PriorityQueue<Cell>(16, new Comparator<Cell>() {
                @Override
                public int compare(Cell cellOne, Cell cellTwo) {
                    return Integer.compare(cellOne.getFinalCost(), cellTwo.getFinalCost());
                }
            });

            //Set startCell for priority queue, set size of closed array and run A* algorithm
            open.add(startCell);
            closed = new boolean[grid.size()][grid.get(0).size()];
            aStar();

            //Trace back path
            if (closed[endCell.getXCoordinate()][endCell.getYCoordinate()]) {
                Cell current = grid.get(endCell.getXCoordinate()).get(endCell.getYCoordinate());
                while (current.getParent() != null) {
                    navigationCells.add(current);
                    current = current.getParent();
                }
            }
        } catch (Exception e) {
            Log.e("Error calculating route " + TAG, String.valueOf(e));
        }

        return navigationCells;
    }

    //A* algorithm
    private void aStar() {
        while (keepCalculating) {
            Cell currentCell = open.poll();

            if (currentCell.getWalkability()) {
                closed[currentCell.getXCoordinate()][currentCell.getYCoordinate()] = true;

                if (!currentCell.equals(grid.get(endCell.getXCoordinate()).get(endCell.getYCoordinate()))) {
                    Cell testCell;

                    //Check left
                    if (currentCell.getXCoordinate() - 1 >= 0) {
                        testCell = grid.get(currentCell.getXCoordinate() - 1).get(currentCell.getYCoordinate());
                        setCostPerCell(testCell);
                        checkAndUpdateCost(currentCell, testCell, currentCell.getFinalCost() + costPerCell);

                        /**
                        //Check left below
                        if (currentCell.getYCoordinate() - 1 >= 0) {
                            testCell = grid.get(currentCell.getXCoordinate() - 1).get(currentCell.getYCoordinate());
                            setCostPerCell(testCell);
                            checkAndUpdateCost(currentCell, testCell, currentCell.getFinalCost() + COST_PER_CELL);
                        }

                        //Check left above
                        if (currentCell.getYCoordinate() + 1 < grid.get(0).size()) {
                            testCell = grid.get(currentCell.getXCoordinate() - 1).get(currentCell.getYCoordinate() + 1);
                            setCostPerCell(testCell);
                            checkAndUpdateCost(currentCell, testCell, currentCell.getFinalCost() + COST_PER_CELL);
                        }
                         **/
                    }

                    //Check right
                    if (currentCell.getXCoordinate() + 1 < grid.size()) {
                        testCell = grid.get(currentCell.getXCoordinate() + 1).get(currentCell.getYCoordinate());
                        setCostPerCell(testCell);
                        checkAndUpdateCost(currentCell, testCell, currentCell.getFinalCost() + costPerCell);

                        /**
                        //Check right below
                        if (currentCell.getYCoordinate() - 1 >= 0) {
                            testCell = grid.get(currentCell.getXCoordinate() + 1).get(currentCell.getYCoordinate() - 1);
                            setCostPerCell(testCell);
                            checkAndUpdateCost(currentCell, testCell, currentCell.getFinalCost() + COST_PER_CELL);
                        }

                        //Check right above
                        if (currentCell.getYCoordinate() + 1 < grid.get(0).size()) {
                            testCell = grid.get(currentCell.getXCoordinate() + 1).get(currentCell.getYCoordinate() + 1);
                            setCostPerCell(testCell);
                            checkAndUpdateCost(currentCell, testCell, currentCell.getFinalCost() + COST_PER_CELL);
                        }
                         **/
                    }

                    //Check below
                    if (currentCell.getYCoordinate() - 1 >= 0) {
                        testCell = grid.get(currentCell.getXCoordinate()).get(currentCell.getYCoordinate() - 1);
                        setCostPerCell(testCell);
                        checkAndUpdateCost(currentCell, testCell, currentCell.getFinalCost() + costPerCell);
                    }

                    //Check above
                    if (currentCell.getYCoordinate() + 1 < grid.get(0).size()) {
                        testCell = grid.get(currentCell.getXCoordinate()).get(currentCell.getYCoordinate() + 1);
                        setCostPerCell(testCell);
                        checkAndUpdateCost(currentCell, testCell, currentCell.getFinalCost() + costPerCell);
                    }
                } else {
                    keepCalculating = false;
                }
            }
        }
    }

    //Helper methods
    private void checkAndUpdateCost(Cell current, Cell test, int cost) {
        if (test.getWalkability() && !closed[test.getXCoordinate()][test.getYCoordinate()]) {

            int testFinalCost = test.getHeuristicCost() + cost;
            boolean inOpen = open.contains(test);

            if (!inOpen || testFinalCost < test.getFinalCost()) {
                test.setFinalCost(cost);
                test.setParent(current);

                if (!inOpen) {
                    open.add(test);
                }
            }
        }
    }

    //Set cost of the cell to check
    private void setCostPerCell(Cell test) {
        Class<? extends Cell> aClass = test.getClass();

        if (aClass.equals("Cell")) {
            costPerCell = 1;
        }
        if (aClass.equals("Room")) {
            costPerCell = 1;
        }
        if (aClass.equals("Stair")) {
            costPerCell = 2;
        }
        if (aClass.equals("Elevator")) {
            costPerCell = 1;
        }
    }

}
