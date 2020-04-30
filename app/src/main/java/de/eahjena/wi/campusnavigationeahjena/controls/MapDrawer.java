package de.eahjena.wi.campusnavigationeahjena.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

import de.eahjena.wi.campusnavigationeahjena.R;
import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Transition;

public class MapDrawer {

    private static final String TAG = "MapDrawer"; //$NON-NLS

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

    private static final int X_SCALING = 50; //TODO: scaling after real data is available
    private static final int Y_SCALING = 50; //TODO: scaling after real data is available

    //Variables
    String floorPlan;
    String startLocationBuilding;
    String startLocationFloor;
    private ArrayList<Transition> transitions = new ArrayList<>();
    private ArrayList<Cell> cellsToWalk = new ArrayList<>();


    //Constructors
    public MapDrawer(String floorPlan, String startLocationBuilding, String startLocationFloor, ArrayList<Transition> transitions, ArrayList<Cell> cellsToWalk) {
        this.floorPlan = floorPlan;
        this.startLocationBuilding = startLocationBuilding;
        this.startLocationFloor = startLocationFloor;
        this.transitions = transitions;
        this.cellsToWalk = cellsToWalk;
    }

    //TODO: Draw stuff
    //Draw image of floor plan, onw location room, destination location room, transitions and route
    @SuppressLint("LongLogTag")
    public void drawNavigation() {

        //Draw floor plan JPEG
        try {

        } catch (Exception e) {
            Log.e("Error drawing floor plan", String.valueOf(e));
        }

        //Draw own location room icon
        try {


        } catch (Exception e) {
            Log.e("Error drawing own location room", String.valueOf(e));
        }

        //Draw destination location room icon if available
        try {

        } catch (Exception e) {
            Log.e("Error drawing destination location room", String.valueOf(e));
        }

        //Draw transitions icons
        try {

        } catch (Exception e) {
            Log.e("Error drawing transitions", String.valueOf(e));
        }

        //Draw route path if available
        try {

        } catch (Exception e) {
            Log.e("Error drawing route", String.valueOf(e));
        }
    }

    //Get floor plan String without ending (.json / .jpeg)
    private String getFloorPlan(Cell location) {
        String floorPlan;

        switch (location.getBuilding() + "." + location.getFloor()) {
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

    //TODO: Helper method mapping
    /**
    //Helper method for mapping, which draws all elements available
    public void drawHelperMapping() {    }
     **/
}

