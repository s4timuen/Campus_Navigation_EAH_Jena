package de.eahjena.wi.campusnavigationeahjena.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.IOException;
import java.util.ArrayList;

import de.eahjena.wi.campusnavigationeahjena.R;
import de.eahjena.wi.campusnavigationeahjena.controls.JSONHandler;
import de.eahjena.wi.campusnavigationeahjena.controls.RouteCalculator;
import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Room;
import de.eahjena.wi.campusnavigationeahjena.models.Transition;

public class NavigationActivity extends AppCompatActivity {

    private static final String TAG = "NavigationActivity"; //$NON-NLS

    //Constants
    private static final String JSON_FILE_ROOMS = "rooms.json";
    private static final String JSON_FILE_TRANSITIONS = "transitions.json";
    private static final String JUST_LOCATION = "just own location";
    private static final String JSON = ".json";

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

    private static final int GRID_WIDTH = 27;
    private static final int GRID_HEIGHT = 40;
    private static final int X_SCALING = 50; //TODO: scaling after real data is available
    private static final int Y_SCALING = 50; //TODO: scaling after real data is available

    //Variables
    private String destinationQRCode;
    private String scannedQRCode;
    private Room startLocation;
    private Room destinationLocation;


    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Transition> transitions = new ArrayList<>();
    private ArrayList<Cell> navigationCells = new ArrayList<>();
    private ArrayList<ArrayList<Cell>> grid = new ArrayList<>();

    //TODO: Transitions without stairs or elevators

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get extra from parent
        Intent intendScannerActivity = getIntent();
        scannedQRCode = intendScannerActivity.getStringExtra("rawResultAsString");
        destinationQRCode = intendScannerActivity.getStringExtra("destinationQRCode");

        //Get rooms, stairs, elevators and crossings from JSON
        getRoomsStairsElevatorsCrossings();

        //Get own location room
        getOwnLocation();

        //Get destination location room
        if (!JUST_LOCATION.equals(destinationQRCode)) {
            getDestinationLocation();
        }

        //Get grid of the floor of the own location
        getFloorGrid();

        //Calculate route (get ArrayList<Cell> of cells to walk)
        if (!JUST_LOCATION.equals(destinationQRCode)) {
            getRoute();
        }

        //Draw image, onw location room, destination location room, stairs, elevators, crossings and route
        drawNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Helper methods
     **/

    //Get rooms, stairs, elevators and crossings from JSON
    @SuppressLint("LongLogTag")
    private void getRoomsStairsElevatorsCrossings() {
        try {
            JSONHandler jsonHandler = new JSONHandler();
            String json;

            json = jsonHandler.readJsonFromAssets(this, JSON_FILE_ROOMS);
            rooms = jsonHandler.parseJsonRooms(json);

            json = jsonHandler.readJsonFromAssets(this, JSON_FILE_TRANSITIONS);
            transitions = jsonHandler.parseJsonStairs(json);

        } catch (IOException e) {
            Log.e("Error reading or parsing JSON files", String.valueOf(e));
        }
    }

    //get own location room
    private void getOwnLocation() {
        try {
            for (int i = 0; i < rooms.size(); i++) {

                if (rooms.get(i).getQRCode().equals(scannedQRCode)) {
                    startLocation = rooms.get(i);
                }
            }
        } catch (
                Exception e) {
            Log.e("QR-Code invalid", String.valueOf(e));
        }
    }

    //Get destination location room
    @SuppressLint("LongLogTag")
    private void getDestinationLocation() {

        try {
            for (int i = 0; i < rooms.size(); i++) {

                if (rooms.get(i).getQRCode().equals(destinationQRCode)) {
                    destinationLocation = rooms.get(i);
                }
            }
        } catch (Exception e) {
            Log.e("Error getting destination location room", String.valueOf(e));
        }

    }

    //Get grid of the floor of the own location
    @SuppressLint("LongLogTag")
    private void getFloorGrid() {
        try {
            JSONHandler jsonHandler = new JSONHandler();
            String json;

            //Get floor plan JSON from assets
            json = jsonHandler.readJsonFromAssets(this, getFloorPlan() + JSON);
            ArrayList<Cell> walkableCells = jsonHandler.parseJsonGrid(json);

            for (int x = 0; x < GRID_WIDTH; x++) {
                grid.add(new ArrayList<Cell>());

                for (int y = 0; y < GRID_HEIGHT; y++) {
                    boolean walkable = false;

                    for (int i = 0; i < walkableCells.size(); i++) {

                        if (walkableCells.get(i).getXCoordinate() == x && walkableCells.get(i).getYCoordinate() == y) {
                            grid.get(x).add(new Cell(x, y, true));
                            walkable = true;
                        }
                    }

                    if (startLocation.getXCoordinate() == x && startLocation.getYCoordinate() == y) {
                        grid.get(x).add(new Cell(x, y, true));
                        walkable = true;
                    }

                    if (destinationLocation.getXCoordinate() == x && destinationLocation.getYCoordinate() == y) {
                        grid.get(x).add(new Cell(x, y, true));
                        walkable = true;
                    }

                    if (!walkable) {
                        grid.get(x).add(new Cell(x, y, false));
                    }
                }
            }

        } catch (Exception e) {
            Log.e("Error getting the floor grid", String.valueOf(e));
        }
    }

    //Calculate route (get ArrayList<Cell> of cells to walk)
    @SuppressLint("LongLogTag")
    private void getRoute() {
        //TODO: Add transitions support (Stairs, Elevators, diff floors and buildings)
        try {
            RouteCalculator routeCalculator = new RouteCalculator(startLocation, destinationLocation, grid);
            navigationCells = routeCalculator.getNavigationCells();

        } catch (Exception e) {
            Log.e("Error calculating route " + TAG, String.valueOf(e));
        }
    }

    //Draw image, onw location room, destination location room, stairs, elevators, crossings and route
    @SuppressLint("LongLogTag")
    private void drawNavigation() {

        //Constraint layouts
        ConstraintLayout constraintLayoutFloorPlan = findViewById(R.id.constraint_layout_navigation_activity);
        ConstraintLayout.LayoutParams layoutParamsFloorPlan = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        ConstraintLayout constraintLayoutIcons = findViewById(R.id.constraint_layout_navigation_activity);
        ConstraintLayout.LayoutParams layoutParamsIcons = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        //Add floor plan JPEG from drawable to ConstraintLayout as ImageView
        try {
            ImageView floorPlan = new ImageView(getApplicationContext());
            floorPlan.setImageResource(getResources().getIdentifier("drawable/" + getFloorPlan(), null, getPackageName()));
            constraintLayoutFloorPlan.addView(floorPlan, layoutParamsFloorPlan);

        } catch (Exception e) {
            Log.e("Error drawing floor plan", String.valueOf(e));
        }

        //Add own location room icon to Overlay
        try {

            ImageView startIcon = new ImageView(getApplicationContext());
            startIcon.setImageResource(R.drawable.start_icon);
            startIcon.setX(startLocation.getXCoordinate() * X_SCALING);
            startIcon.setY(startLocation.getYCoordinate() * Y_SCALING);
            constraintLayoutIcons.addView(startIcon, layoutParamsIcons);

        } catch (Exception e) {
            Log.e("Error drawing own location room", String.valueOf(e));
        }

        //Add destination location room icon to ConstraintLayout
        try {
            if (!destinationQRCode.equals(JUST_LOCATION)) {

                ImageView destinationIcon = new ImageView(getApplicationContext());
                destinationIcon.setImageResource(R.drawable.destination_icon);
                destinationIcon.setX(destinationLocation.getXCoordinate() * X_SCALING);
                destinationIcon.setY(destinationLocation.getYCoordinate() * Y_SCALING);
                constraintLayoutIcons.addView(destinationIcon, layoutParamsIcons);
            }
        } catch (Exception e) {
            Log.e("Error drawing destination location room", String.valueOf(e));
        }

        //Add transitions icons to ConstraintLayout
        try {
            for (int i = 0; i < transitions.size(); i++) {

                if (transitions.get(i).getTypeOfTransition().equals("stair")) {

                    ImageView stairIcon = new ImageView(getApplicationContext());
                    stairIcon.setImageResource(R.drawable.stair_icon);
                    stairIcon.setX(transitions.get(i).getXCoordinate() * X_SCALING);
                    stairIcon.setY(transitions.get(i).getYCoordinate() * Y_SCALING);
                    constraintLayoutIcons.addView(stairIcon, layoutParamsIcons);
                }
                if (transitions.get(i).getTypeOfTransition().equals("elevator")) {

                    ImageView elevatorIcon = new ImageView(getApplicationContext());
                    elevatorIcon.setImageResource(R.drawable.elevator_icon);
                    elevatorIcon.setX(transitions.get(i).getXCoordinate() * X_SCALING);
                    elevatorIcon.setY(transitions.get(i).getYCoordinate() * Y_SCALING);
                    constraintLayoutIcons.addView(elevatorIcon, layoutParamsIcons);
                }
                if (transitions.get(i).getTypeOfTransition().equals("crossing")) {

                    ImageView crossingIcon = new ImageView(getApplicationContext());
                    crossingIcon.setImageResource(R.drawable.crossing_icon);
                    crossingIcon.setX(transitions.get(i).getXCoordinate() * X_SCALING);
                    crossingIcon.setY(transitions.get(i).getYCoordinate() * Y_SCALING);
                    constraintLayoutIcons.addView(crossingIcon, layoutParamsIcons);
                }
            }
        } catch (Exception e) {
            Log.e("Error drawing transitions", String.valueOf(e));
        }

        //Add route path to ConstraintLayout
        try {
            if (!destinationQRCode.equals(JUST_LOCATION)) {
                for (int j = 1; j < navigationCells.size(); j++){

                    ImageView pathCellIcon = new ImageView(getApplicationContext());
                    pathCellIcon.setImageResource(R.drawable.path_cell_icon);
                    pathCellIcon.setX(navigationCells.get(j).getXCoordinate() * X_SCALING);
                    pathCellIcon.setY(navigationCells.get(j).getYCoordinate() * Y_SCALING);
                    constraintLayoutIcons.addView(pathCellIcon, layoutParamsIcons);

                }
            }
        } catch (Exception e) {
            Log.e("Error drawing route", String.valueOf(e));
        }
    }

    //Get floor plan String without ending (.json / .jpeg)
    private String getFloorPlan() {
        String floorPlan;

        switch (startLocation.getBuilding() + "." + startLocation.getFloor()) {
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
