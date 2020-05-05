package de.eahjena.wi.campusnavigationeahjena.controls;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Room;
import de.eahjena.wi.campusnavigationeahjena.models.Transition;

class AStarAlgorithm {

    private static final String TAG = "AStarAlgorithm"; //$NON-NLS

    //Constants
    private static final int COSTS_CELL = 1;
    private static final int COSTS_ROOM = 1;
    private static final int COSTS_TRANSITION = 2;

    //Variables
    private PriorityQueue<Cell> open;
    private boolean[][] closed;
    private Cell startCell;
    private Cell endCell;
    private ArrayList<ArrayList<Cell>> grid;
    private boolean keepCalculating = true;
    private int costPerCell;

    //Constructor
    AStarAlgorithm(Cell startCell, Cell endCell, ArrayList<ArrayList<Cell>> grid) {
        this.startCell = startCell;
        this.endCell = endCell;
        this.grid = grid;
    }

    //Calculation of cells to walk on one grid
    @SuppressLint("LongLogTag")
    ArrayList<Cell> getNavigationCellsOnGrid() {

        ArrayList<Cell> navigationCells = new ArrayList<>();

        try {
            //Set priority queue with comparator
            open = new PriorityQueue<>(4, new Comparator<Cell>() {
                @Override
                public int compare(Cell cellOne, Cell cellTwo) {
                    return Integer.compare(cellOne.getFinalCost(), cellTwo.getFinalCost());
                }
            });

            //Set startCell for priority queue, set size of closed array and run A* algorithm
            open.add(startCell);
            closed = new boolean[grid.size()][grid.get(0).size()];
            aStar(); //TODO: FIX: trace back returns null, no parent, updateAndCheckCosts parent available ???

            Log.i("_____TEST_Star_01_01_____", String.valueOf(grid.get(endCell.getXCoordinate()).get(endCell.getYCoordinate()).getParent()));
            Log.i("_____TEST_Star_01_02_____", String.valueOf(endCell.getXCoordinate()));
            Log.i("_____TEST_Star_01_03_____", String.valueOf(endCell.getYCoordinate()));
            Log.i("_____TEST_Star_01_04_____", String.valueOf(grid.size()));
            Log.i("_____TEST_Star_01_05_____", String.valueOf(grid.get(6).get(7)));

            //Trace back path
            Cell current = grid.get(endCell.getXCoordinate()).get(endCell.getYCoordinate());
            Log.i("_____TEST_Star_02_____", String.valueOf(current.getParent()));

            while (current.getParent() != null) {
                navigationCells.add(current);
                Log.i("_____TEST_Star_03_____", String.valueOf(navigationCells.size()));
                current = current.getParent();
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

            if (currentCell != null && currentCell.getWalkability()) {
                closed[currentCell.getXCoordinate()][currentCell.getYCoordinate()] = true;

                if (currentCell.getXCoordinate() != endCell.getXCoordinate() && currentCell.getYCoordinate() != endCell.getYCoordinate()) {

                    //Check left
                    if (currentCell.getXCoordinate() - 1 >= 0) {
                        setCostPerCell(grid.get(currentCell.getXCoordinate() - 1).get(currentCell.getYCoordinate()));
                        checkAndUpdateCost(currentCell, grid.get(currentCell.getXCoordinate() - 1).get(currentCell.getYCoordinate()), currentCell.getFinalCost() + costPerCell);
                    }

                    //Check right
                    if (currentCell.getXCoordinate() + 1 < grid.size()) {
                        setCostPerCell(grid.get(currentCell.getXCoordinate() + 1).get(currentCell.getYCoordinate()));
                        checkAndUpdateCost(currentCell, grid.get(currentCell.getXCoordinate() + 1).get(currentCell.getYCoordinate()), currentCell.getFinalCost() + costPerCell);
                    }

                    //Check below
                    if (currentCell.getYCoordinate() - 1 >= 0) {
                        setCostPerCell(grid.get(currentCell.getXCoordinate()).get(currentCell.getYCoordinate() - 1));
                        checkAndUpdateCost(currentCell, grid.get(currentCell.getXCoordinate()).get(currentCell.getYCoordinate() - 1), currentCell.getFinalCost() + costPerCell);
                    }

                    //Check above
                    if (currentCell.getYCoordinate() + 1 < grid.get(0).size()) {
                        setCostPerCell(grid.get(currentCell.getXCoordinate()).get(currentCell.getYCoordinate() + 1));
                        checkAndUpdateCost(currentCell, grid.get(currentCell.getXCoordinate()).get(currentCell.getYCoordinate() + 1), currentCell.getFinalCost() + costPerCell);
                    }
                }
            } else {
                keepCalculating = false;
            }
        }
    }

    //Check and update cost of a cell
    private void checkAndUpdateCost(Cell current, Cell test, int cost) {
        if (test.getWalkability() && !closed[test.getXCoordinate()][test.getYCoordinate()]) {

            int testFinalCost = test.getHeuristicCost() + cost;
            boolean inOpen = open.contains(test);

            if (!inOpen || testFinalCost < test.getFinalCost()) {
                test.setFinalCost(cost);
                test.setParent(current);
                Log.i("_____TEST_Star_00_____", String.valueOf(test.getParent()));

                if (!inOpen) {
                    open.add(test);
                }
            }
        }
    }

    //Set cost of the cell to check
    private void setCostPerCell(Cell test) {
        Class<? extends Cell> aClass = test.getClass();

        Cell compareCellClass = new Cell();
        Room compareRoomClass = new Room();
        Transition compareTransitionClass = new Transition();

        if (aClass.equals(compareCellClass.getClass())) {
            costPerCell = COSTS_CELL;
        }
        if (aClass.equals(compareRoomClass.getClass())) {
            costPerCell = COSTS_ROOM;
        }
        if (aClass.equals(compareTransitionClass.getClass())) {
            costPerCell = COSTS_TRANSITION;
        }
    }
}
