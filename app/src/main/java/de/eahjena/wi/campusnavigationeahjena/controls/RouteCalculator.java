package de.eahjena.wi.campusnavigationeahjena.controls;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;

import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Room;
import de.eahjena.wi.campusnavigationeahjena.models.Transition;

public class RouteCalculator {

    private static final String TAG = "RouteCalculator"; //$NON-NLS

    //Constants
    private static final String BUILDING_03_02_01_FLOOR_UG = "building_03_02_01_floor_ug";
    private static final String BUILDING_03_02_01_FLOOR_00 = "building_03_02_01_floor_00";
    private static final String BUILDING_03_02_01_FLOOR_01 = "building_03_02_01_floor_01";
    private static final String BUILDING_03_02_01_FLOOR_02 = "building_03_02_01_floor_02";
    private static final String BUILDING_03_02_01_FLOOR_03 = "building_03_02_01_floor_03";
    private static final String BUILDING_03_02_01_FLOOR_04 = "building_03_02_01_floor_04";
    private static final String BUILDING_04_FLOOR_UG = "building_04_floor_ug";
    private static final String BUILDING_04_FLOOR_00 = "building_04_floor_00";
    private static final String BUILDING_04_FLOOR_01 = "building_04_floor_01";
    private static final String BUILDING_04_FLOOR_02 = "building_04_floor_02";
    private static final String BUILDING_04_FLOOR_03 = "building_04_floor_03";
    private static final String BUILDING_05_FLOOR_UG = "building_05_floor_ug";
    private static final String BUILDING_05_FLOOR_00 = "building_05_floor_00";
    private static final String BUILDING_05_FLOOR_01 = "building_05_floor_01";
    private static final String BUILDING_05_FLOOR_02 = "building_05_floor_02";
    private static final String BUILDING_05_FLOOR_03 = "building_05_floor_03";

    private static final int GRID_X = 40;
    private static final int GRID_Y = 30;

    private static final String JSON = ".json";

    //Variables
    private Context context;
    private ArrayList<Transition> transitions;
    private Cell startLocation;
    private Cell destinationLocation;

    private ArrayList<Cell> cellsToWalk = new ArrayList<>();

    //Constructor
    public RouteCalculator(Context context, Room startLocation, Room destinationLocation, ArrayList<Transition> transitions) {
        this.context = context;
        this.startLocation = startLocation;
        this.destinationLocation = destinationLocation;
        this.transitions = transitions;
    }

    //Get all cells to walk from start to destination location
    public ArrayList<Cell> getNavigationCells() {

        Cell startCell = new Cell(startLocation.getXCoordinate(), startLocation.getYCoordinate(), startLocation.getBuilding(), startLocation.getFloor(), startLocation.getWalkability());
        Cell endCell = new Cell();

        //Get grids of floors to use
        ArrayList<ArrayList<ArrayList<Cell>>> grids = navigationBuildings(); //TODO:FIX: grids wrong


        Log.i("_____TEST_RC__01____", String.valueOf(grids.size()));
        for (int index = 0; index < grids.size(); index++) {
            Log.i("_____TEST_RC__01____", getFloorPlan(grids.get(index).get(0).get(0).getBuilding(), grids.get(index).get(0).get(0).getFloor()));
        }



        try {
            //Get paths through all grids
            for (int index = 0; index < grids.size(); index++) {

                ArrayList<Transition> reachableTransitions = new ArrayList<>();

                //Set endCell
                if (startCell.getBuilding().equals(destinationLocation.getBuilding()) && startCell.getFloor().equals(destinationLocation.getFloor())) {

                    endCell = destinationLocation;
                }
                if (!startCell.getBuilding().equals(destinationLocation.getBuilding()) && !startCell.getFloor().equals(destinationLocation.getFloor())) {

                    //get all reachable transitions on floor
                    for (int j = 0; j < transitions.size(); j++) {
                        for (int k = 0; k < transitions.get(j).getConnectedCells().size(); k++) {
                            if (transitions.get(j).getConnectedCells().get(k).getBuilding().equals(startCell.getBuilding())
                                    && transitions.get(j).getConnectedCells().get(k).getFloor().equals(startCell.getFloor())) {

                                reachableTransitions.add(transitions.get(j));
                            }
                        }
                    }

                    //TODO: if in same building, else get respective transition
                    //aStar all reachable transitions, get costs of each and set endCell
                    for (int j = 0; j < reachableTransitions.size(); j++) {

                        AStarAlgorithm aStarAlgorithm = new AStarAlgorithm(startCell, reachableTransitions.get(j), grids.get(index));
                        ArrayList<Cell> navigationCells = aStarAlgorithm.getNavigationCellsOnGrid();

                        reachableTransitions.get(j).setFinalCost(navigationCells.size());

                        reachableTransitions.sort(new Comparator<Transition>() {
                            public int compare(Transition TransitionOne, Transition TransitionTwo) {
                                return Integer.compare(TransitionOne.getFinalCost(), TransitionTwo.getFinalCost());
                            }
                        });
                    }
                    endCell = reachableTransitions.get(0).getSingleCell(startCell.getBuilding(), startCell.getFloor());
                }

                //Get path through floor
                AStarAlgorithm aStarAlgorithm = new AStarAlgorithm(startCell, endCell, grids.get(index));
                cellsToWalk.addAll(aStarAlgorithm.getNavigationCellsOnGrid());

                Log.i("_____TEST_end cell building_____", endCell.getBuilding());
                Log.i("_____TEST_end cell floor_____", endCell.getFloor());

                try {
                    //Set next startCell
                    if (!endCell.getBuilding().equals(destinationLocation.getBuilding()) && !endCell.getFloor().equals(destinationLocation.getFloor())) {

                        //TODO:FIX: if in same building, else get respective transition
                        startCell = reachableTransitions.get(0).getSingleCell(grids.get(index + 1).get(0).get(0).getBuilding(), grids.get(index + 1).get(0).get(0).getFloor());

                    }
                } catch (Exception e) {
                    Log.e("_____TEST_RC__02____", String.valueOf(e));
                }
            }
        } catch (Exception e) {
            Log.e(TAG + " error getting navigation cells", String.valueOf(e));
        }
        return cellsToWalk;
    }

    //Get grids of floors in buildings to use (high level navigation)
    private ArrayList<ArrayList<ArrayList<Cell>>> navigationBuildings() {

        ArrayList<ArrayList<ArrayList<Cell>>> gridsToAdd = new ArrayList<>();

        int startFloorInteger = startLocation.getFloorAsInteger();
        int destinationFloorInteger = destinationLocation.getFloorAsInteger();
        int startBuildingInteger = startLocation.getBuildingAsInteger();
        int destinationBuildingInteger = destinationLocation.getBuildingAsInteger();

        switch (startBuildingInteger) {
            case 4:
                startBuildingInteger = 1;
                break;
            case 1:
            case 2:
            case 3:
                startBuildingInteger = 2;
                break;
            case 5:
                startBuildingInteger = 3;
                break;
            default:
                break;
        }

        switch (destinationBuildingInteger) {
            case 4:
                destinationBuildingInteger = 1;
                break;
            case 1:
            case 2:
            case 3:
                destinationBuildingInteger = 2;
                break;
            case 5:
                destinationBuildingInteger = 3;
                break;
            default:
                break;
        }

        try {
            //Start and end location in same building
            if (startBuildingInteger == destinationBuildingInteger) {

                //Start floor > destination floor
                if (startFloorInteger > destinationFloorInteger) {
                    for (int index = startFloorInteger; index >= destinationFloorInteger; index--) {
                        gridsToAdd.add(buildGrid(startLocation.getBuilding(), getCurrentFloor(index)));
                    }
                }
                //Start floor < destination floor
                if (startFloorInteger < destinationFloorInteger) {
                    for (int index = startFloorInteger; index <= destinationFloorInteger; index++) {
                        gridsToAdd.add(buildGrid(startLocation.getBuilding(), getCurrentFloor(index)));
                    }
                }
                //Start floor = destination floor
                if (startFloorInteger == destinationFloorInteger) {
                    gridsToAdd.add(buildGrid(startLocation.getBuilding(), startLocation.getFloor()));
                }
            }

            //Start and end location in different building
            if (startBuildingInteger != destinationBuildingInteger) {

                //From building 4 to 3 or 4 to 5 or 3 to 5
                if (startBuildingInteger < destinationBuildingInteger) {

                    //From building 4 to 3
                    if (startBuildingInteger == 1 && destinationBuildingInteger == 2) {
                        //Start floor to -1
                        for (int index = startFloorInteger; index >= -1; index--) {
                            gridsToAdd.add(buildGrid("04", getCurrentFloor(index)));
                        }
                        //Destination floor > 0
                        if (destinationFloorInteger > 0) {
                            for (int index = 0; index <= destinationFloorInteger; index++) {
                                gridsToAdd.add(buildGrid("03", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor < 0
                        if (destinationFloorInteger < 0) {
                            for (int index = 0; index >= destinationFloorInteger; index--) {
                                gridsToAdd.add(buildGrid("03", getCurrentFloor(index)));
                            }
                        }
                        //destination floor = 0
                        if (destinationFloorInteger == 0) {
                            gridsToAdd.add(buildGrid("03", getCurrentFloor(0)));
                        }
                    }

                    //From building 3 to 5
                    if (startBuildingInteger == 2 && destinationBuildingInteger == 3) {
                        //Start floor >= 1 to 1
                        if (startFloorInteger >= 1) {
                            for (int index = startFloorInteger; index >= 1; index--) {
                                gridsToAdd.add(buildGrid("03", getCurrentFloor(index)));
                            }
                        }
                        //Start floor < 1 to 1
                        if (startFloorInteger < 1) {
                            for (int index = startFloorInteger; index <= 1; index++){
                                gridsToAdd.add(buildGrid("03", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor > 1
                        if (destinationFloorInteger > 1) {
                            for (int index = 1; index <= destinationFloorInteger; index++) {
                                gridsToAdd.add(buildGrid("05", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor < 1
                        if (destinationFloorInteger < 1) {
                            for (int index = 1; index >= destinationFloorInteger; index--) {
                                gridsToAdd.add(buildGrid("05", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor = 1
                        if (destinationFloorInteger == 1) {
                            gridsToAdd.add(buildGrid("05", getCurrentFloor(1)));
                        }
                    }

                    //From building 4 to 5
                    if (startBuildingInteger == 1 && destinationBuildingInteger == 3) {
                        //Start floor to -1
                        for (int index = startFloorInteger; index >= -1; index--) {
                            gridsToAdd.add(buildGrid("04", getCurrentFloor(index)));
                        }
                        //Building 3 floor 0 to 1
                        gridsToAdd.add(buildGrid("03", getCurrentFloor(0)));
                        gridsToAdd.add(buildGrid("03", getCurrentFloor(1)));
                        //Destination floor > 1
                        if (destinationFloorInteger > 1) {
                            for (int index = 1; index <= destinationFloorInteger; index++) {
                                gridsToAdd.add(buildGrid("05", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor < 1
                        if (destinationFloorInteger < 1) {
                            for (int index = 1; index >= destinationFloorInteger; index--) {
                                gridsToAdd.add(buildGrid("05", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor = 1
                        if (destinationFloorInteger == 1) {
                            gridsToAdd.add(buildGrid("05", getCurrentFloor(1)));
                        }
                    }
                }

                //From building 5 to 3 or 3 to 4 or 5 to 4
                if (destinationBuildingInteger < startBuildingInteger) {

                    //From building 5 to 3
                    if (startBuildingInteger == 3 && destinationBuildingInteger == 2) {
                        //Start floor >= 1
                        if (startFloorInteger >= 1) {
                            for (int index = startFloorInteger; index >= 1; index--) {
                                gridsToAdd.add(buildGrid("05", getCurrentFloor(index)));
                            }
                        }
                        //Start floor < 1
                        if (startFloorInteger < 1) {
                            for (int index = startFloorInteger; index <= 1; index++) {
                                gridsToAdd.add(buildGrid("05", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor > 1
                        if (destinationFloorInteger > 1) {
                            for (int index = 1; index <= destinationFloorInteger; index++) {
                                gridsToAdd.add(buildGrid("03", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor < 1
                        if (destinationFloorInteger < 1) {
                            for (int index = 1; index >= destinationFloorInteger; index--) {
                                gridsToAdd.add(buildGrid("03", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor = 1
                        if (destinationFloorInteger == 1) {
                            gridsToAdd.add(buildGrid("03", getCurrentFloor(1)));
                        }
                    }

                    //From building 3 to 4
                    if (startBuildingInteger == 2 && destinationBuildingInteger == 1) {
                        //Start floor >= 0
                        if (startFloorInteger >= 0) {
                            for (int index = startFloorInteger; index >= 0; index--) {
                                gridsToAdd.add(buildGrid("03", getCurrentFloor(index)));
                            }
                        }
                        //Start floor < 0
                        if (startFloorInteger < 0) {
                            for (int index = startFloorInteger; index <= 0; index++) {
                                gridsToAdd.add(buildGrid("03", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor > -1
                        if (destinationFloorInteger > -1) {
                            for (int index = -1; index <= destinationFloorInteger; index++) {
                                gridsToAdd.add(buildGrid("04", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor = -1
                        if (destinationFloorInteger == -1) {
                            gridsToAdd.add(buildGrid("04", getCurrentFloor(-1)));
                        }
                    }

                    //From building 5 to 4
                    if (startBuildingInteger == 3 && destinationBuildingInteger == 1) {
                        //Start floor >= 1
                        if (startFloorInteger >= 1) {
                            for (int index = startFloorInteger; index >= 1; index--) {
                                gridsToAdd.add(buildGrid("05", getCurrentFloor(index)));
                            }
                        }
                        //Start floor < 1
                        if (startFloorInteger < 1) {
                            for (int index = startFloorInteger; index <= 1; index++) {
                                gridsToAdd.add(buildGrid("05", getCurrentFloor(index)));
                            }
                        }
                        //Building 3 floor 1 to 0
                        gridsToAdd.add(buildGrid("03", getCurrentFloor(1)));
                        gridsToAdd.add(buildGrid("03", getCurrentFloor(0)));
                        //Destination floor > -1
                        if (destinationFloorInteger > -1) {
                            for (int index = -1; index <= destinationFloorInteger; index++) {
                                gridsToAdd.add(buildGrid("04", getCurrentFloor(index)));
                            }
                        }
                        //Destination floor = -1
                        if (destinationFloorInteger == -1) {
                            gridsToAdd.add(buildGrid("04", getCurrentFloor(-1)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG + " error navigating through buildings", String.valueOf(e));
        }
        return gridsToAdd;
    }

    //Integer to String for floors
    private String getCurrentFloor(int index) {

        String currentFloor = null;

        switch (index) {
            case -1:
                currentFloor = "ug";
                break;
            case 0:
                currentFloor = "00";
                break;
            case 1:
                currentFloor = "01";
                break;
            case 2:
                currentFloor = "02";
                break;
            case 3:
                currentFloor = "03";
                break;
            case 4:
                currentFloor = "04";
                break;
            default:
                break;
        }
        return currentFloor;
    }

    //Build walkability grid for a floor plan
    private ArrayList<ArrayList<Cell>> buildGrid(String building, String floor) {
        ArrayList<ArrayList<Cell>> grid = new ArrayList<>();

        try {
            JSONHandler jsonHandler = new JSONHandler();
            String json;

            //Get floor plan JSON from assets
            json = jsonHandler.readJsonFromAssets(context, getFloorPlan(building, floor) + JSON);
            ArrayList<Cell> walkableCells = jsonHandler.parseJsonWalkableCells(json);

            for (int x = 0; x < GRID_X; x++) {
                grid.add(new ArrayList<Cell>());

                for (int y = 0; y < GRID_Y; y++) {
                    boolean walkable = false;

                    for (int i = 0; i < walkableCells.size(); i++) {

                        if (walkableCells.get(i).getXCoordinate() == x && walkableCells.get(i).getYCoordinate() == y) {
                            grid.get(x).add(new Cell(x, y, building, floor, true));
                            walkable = true;
                        }
                    }
                    if (!walkable) {
                        grid.get(x).add(new Cell(x, y, building, floor, false));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG + " error building the floor grid", String.valueOf(e));
        }
        return grid;
    }

    //Get floor plan String without ending (.json / .jpeg)
    private String getFloorPlan(String building, String floor) {
        String floorPlan;

        switch (building + "." + floor) {
            case "01.ug":
            case "02.ug":
            case "03.ug":
                floorPlan = BUILDING_03_02_01_FLOOR_UG;
                break;
            case "01.00":
            case "02.00":
            case "03.00":
                floorPlan = BUILDING_03_02_01_FLOOR_00;
                break;
            case "01.01":
            case "02.01":
            case "03.01":
                floorPlan = BUILDING_03_02_01_FLOOR_01;
                break;
            case "01.02":
            case "02.02":
            case "03.02":
                floorPlan = BUILDING_03_02_01_FLOOR_02;
                break;
            case "01.03":
            case "02.03":
            case "03.03":
                floorPlan = BUILDING_03_02_01_FLOOR_03;
                break;
            case "01.04":
            case "02.04":
                floorPlan = BUILDING_03_02_01_FLOOR_04;
                break;
            case "04.ug":
                floorPlan = BUILDING_04_FLOOR_UG;
                break;
            case "04.00":
                floorPlan = BUILDING_04_FLOOR_00;
                break;
            case "04.01":
                floorPlan = BUILDING_04_FLOOR_01;
                break;
            case "04.02":
                floorPlan = BUILDING_04_FLOOR_02;
                break;
            case "04.03":
                floorPlan = BUILDING_04_FLOOR_03;
                break;
            case "05.ug":
                floorPlan = BUILDING_05_FLOOR_UG;
                break;
            case "05.00":
                floorPlan = BUILDING_05_FLOOR_00;
                break;
            case "05.01":
                floorPlan = BUILDING_05_FLOOR_01;
                break;
            case "05.02":
                floorPlan = BUILDING_05_FLOOR_02;
                break;
            case "05.03":
                floorPlan = BUILDING_05_FLOOR_03;
                break;
            default:
                floorPlan = null;
        }
        return floorPlan;
    }
}
