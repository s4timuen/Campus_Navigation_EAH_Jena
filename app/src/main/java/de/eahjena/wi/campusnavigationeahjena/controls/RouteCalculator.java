package de.eahjena.wi.campusnavigationeahjena.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Room;
import de.eahjena.wi.campusnavigationeahjena.models.Transition;

import static java.lang.Integer.parseInt;

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
    private static final int GRID_Y = 27;

    private static final String JSON = ".json";

    //Variables
    private Context context;
    private ArrayList<Transition> transitions;
    Cell startLocation;
    Cell destinationLocation;

    ArrayList<ArrayList<ArrayList<Cell>>> grids = null;

    //Constructor
    public RouteCalculator(Context context, Room startLocation, Room destinationLocation, ArrayList<Transition> transitions) {
        this.context = context;
        this.startLocation = startLocation;
        this.destinationLocation = destinationLocation;
        this.transitions = transitions;
    }

    public ArrayList<Cell> getNavigationCells() {

        ArrayList<ArrayList<ArrayList<Cell>>> grids;
        ArrayList<Cell> cellsToWalk = null;

            grids = navigationBuildings();

            //just use next transition till end grid
            //add transition for all floors its going through
            //Nav aStar for all floor grids

            cellsToWalk.addAll();

        return cellsToWalk;
    }

    //Get grids of floors in buildings to use (high level navigation)
    private ArrayList<ArrayList<ArrayList<Cell>>> navigationBuildings() {

        ArrayList<ArrayList<ArrayList<Cell>>> grids = null;

        int startFloorInteger = startLocation.getFloorAsInteger();
        int destinationFloorInteger = destinationLocation.getFloorAsInteger();
        int startBuildingInteger = startLocation.getBuildingAsInteger();
        int destinationBuildingInteger = destinationLocation.getBuildingAsInteger();

        switch (startBuildingInteger){
            case 4 :
                startBuildingInteger = 1;
                break;
            case 3 :
                startBuildingInteger = 2;
                break;
            case 5 :
                startBuildingInteger = 3;
                break;
            default:
                break;
        }

        switch (destinationBuildingInteger){
            case 4 :
                destinationBuildingInteger = 1;
                break;
            case 3 :
                destinationBuildingInteger = 2;
                break;
            case 5 :
                destinationBuildingInteger = 3;
                break;
            default:
                break;
        }

        if (startBuildingInteger == destinationBuildingInteger) {
            ArrayList<Integer> floors = getFloorsToUseWithinBuilding(startFloorInteger, destinationFloorInteger);
            for (int index = startFloorInteger; index < floors.size(); index++) {
                grids.add(buildGrid(startLocation.getBuilding(), getCurrentFloor(index)));
            }
        }
        if (startBuildingInteger != destinationBuildingInteger) {

            if (startBuildingInteger < destinationBuildingInteger) {
                for (int i = startBuildingInteger; i <= destinationBuildingInteger; i++) {

                    //TODO: grids.add(floor grids of all buildings to walk through)
                    // -> from start floor to respective destination floor and from next respective start floor to etc or destination floor
                    if (i == 1) {
                        destinationFloorInteger = -1;
                    }
                    if (i == 2 && i < destinationBuildingInteger) {
                        destinationFloorInteger = 1;
                    }
                    if (i == 2 && i == destinationBuildingInteger) {
                        destinationFloorInteger = destinationLocation.getFloorAsInteger();
                    }
                    for (int index = )
                }
            }
            if (destinationBuildingInteger < startBuildingInteger) {

                for (int i = startBuildingInteger; i >= destinationBuildingInteger; i--) {

                    //TODO: grids.add(floor grids of all buildings to walk through)
                    if (i == 3) {
                        destinationFloorInteger = 1;
                    }
                    if (i == 2 && i > destinationBuildingInteger) {
                        destinationFloorInteger = 0;
                    }
                    if (i == 2 && i == destinationBuildingInteger) {
                        destinationFloorInteger = destinationLocation.getFloorAsInteger();
                    }
                    //buildingsAndFloors.add(getFloorsToUseWithinBuilding(startFloorInteger, destinationFloorInteger));
                }

            }
        }
        return grids;
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

    //Get the floors which have to be used within a building in respective order
    private ArrayList<Integer> getFloorsToUseWithinBuilding(int startFloor, int destinationFloor) {

        ArrayList<Integer> floors = null;

        if (startFloor == destinationFloor) {
            floors.add(startFloor);
        }
        if (startFloor < destinationFloor) {
            for (int i = startFloor; i <= destinationFloor; i++) {
                floors.add(i);
            }
        }
        if (destinationFloor < startFloor) {
            for (int i = startFloor; i >= destinationFloor; i--) {
                floors.add(destinationFloor);
            }
        }
        return floors;
    }


    //Build walkability grid for a floor plan
    @SuppressLint("LongLogTag")
    private ArrayList<ArrayList<Cell>> buildGrid(String building, String floor) {
        ArrayList<ArrayList<Cell>> grid = null;

        try {
            JSONHandler jsonHandler = new JSONHandler();
            String json;

            //Get floor plan JSON from assets
            json = jsonHandler.readJsonFromAssets(context,getFloorPlan(building, floor) + JSON);
            ArrayList<Cell> walkableCells = jsonHandler.parseJsonGrid(json);

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
                        grid.get(x).add(new Cell(x, y, building, floor,false));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Error getting the floor grid", String.valueOf(e));
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
