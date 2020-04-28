package de.eahjena.wi.campusnavigationeahjena.controls;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import de.eahjena.wi.campusnavigationeahjena.models.Cell;

public class AStarAlgorithm {

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
    ArrayList<ArrayList<Cell>> grid;
    private boolean keepCalculating = true;
    private int costPerCell;

    //Constructor
    public AStarAlgorithm(Cell startCell, Cell endCell, ArrayList<ArrayList<Cell>> grid) {
        this.startCell = startCell;
        this.endCell = endCell;
        this.grid = grid;
    }

    //Calculation of cells to walk on one grid
    @SuppressLint("LongLogTag")
    private ArrayList<Cell> getNavigationCellsOnGrid() {

        ArrayList<Cell> navigationCells = new ArrayList<>();

        try {
            //Set priority queue with comparator
            open = new PriorityQueue<>(16, new Comparator<Cell>() {
                @Override
                public int compare(Cell cellOne, Cell cellTwo) {
                    return Integer.compare(cellOne.getFinalCost(), cellTwo.getFinalCost());
                }
            });

            //Set startCell for priority queue, set size of closed array and run A* algorithm
            open.add(startCell);
            closed = new boolean[grid.size()][grid.get(0).size()];
            aStar(grid, endCell);

            //Trace back path
            if (closed[this.endCell.getXCoordinate()][endCell.getYCoordinate()]) {
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
    private void aStar(ArrayList<ArrayList<Cell>> grid, Cell endCell) {
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

    //Check and update cost of a cell
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
            costPerCell = COSTS_CELL;
        }
        if (aClass.equals("Room")) {
            costPerCell = COSTS_ROOM;
        }
        if (aClass.equals("Transition")) {
            costPerCell = COSTS_TRANSITION;
        }
    }

}
