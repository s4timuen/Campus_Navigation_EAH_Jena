package de.eahjena.wi.campusnavigationeahjena.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import de.eahjena.wi.campusnavigationeahjena.R;
import de.eahjena.wi.campusnavigationeahjena.controls.JSONHandler;
import de.eahjena.wi.campusnavigationeahjena.controls.RouteCalculator;
import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Room;
import de.eahjena.wi.campusnavigationeahjena.models.Transition;

public class NavigationActivity extends AppCompatActivity {

    private static final String TAG = "NavigationActivity"; //$NON-NLS

    //Constants

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

    private static final String JSON_FILE_ROOMS = "rooms.json";
    private static final String JSON_FILE_TRANSITIONS = "transitions.json";
    private static final String JUST_LOCATION = "just own location";
    private static final int X_SCALING = 10; //TODO: scaling after mapping JSONs
    private static final int Y_SCALING = 12; //TODO: scaling after mapping JSONs

    //Variables
    private String destinationQRCode;
    private String scannedQRCode;
    private Room startLocation;
    private Room destinationLocation;

    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Transition> transitions = new ArrayList<>();
    private ArrayList<Cell> cellsToWalk = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Spinner select floor plans
        final ArrayList<String> floorPlans = new ArrayList<>(getItemsSpinner());

        final Spinner floorPlansSpinner = findViewById(R.id.spinner_floor_plans);
        ArrayAdapter<String> floorPlansAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, floorPlans);
        floorPlansSpinner.setAdapter(floorPlansAdapter);
        floorPlansSpinner.setSelection(0, false);
        floorPlansSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
                Object item = adapterView.getItemAtPosition(index);
                if (item != null && index != 0) {
                    try {
                        ArrayList<String> helperBuildingAndFloor = getBuildingAndFloor((String) item);
                        drawNavigation(helperBuildingAndFloor.get(0), helperBuildingAndFloor.get(1));
                    } catch (Exception e) {
                        Log.e(TAG + "error change map", String.valueOf(e));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

        //Get extra from parent
        Intent intendScannerActivity = getIntent();
        scannedQRCode = intendScannerActivity.getStringExtra("rawResultAsString");
        destinationQRCode = intendScannerActivity.getStringExtra("destinationQRCode");

        //Get rooms, stairs, elevators and crossings from JSON
        getRoomsAndTransitions();

        //Get own location room
        getOwnLocation();

        //Get destination location room
        if (!JUST_LOCATION.equals(destinationQRCode)) {
            getDestinationLocation();
        }

        //Calculate route (get ArrayList<Cell> of cells to walk)
        if (!JUST_LOCATION.equals(destinationQRCode)) {
            getRoute();
        }

        //Draw navigation stuff of current floor in fragment
        drawNavigation(startLocation.getBuilding(), startLocation.getFloor());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //Get items for spinner floor plans
    private ArrayList<String> getItemsSpinner() {

        ArrayList<String> spinnerItems = new ArrayList<>();
        Resources resource = getResources();
        String defaultSelection = resource.getString(R.string.select_from_spinner);

        spinnerItems.add(defaultSelection);
        spinnerItems.add(resource.getString(R.string.building_03_02_01_floor_ug));
        spinnerItems.add(resource.getString(R.string.building_03_02_01_floor_00));
        spinnerItems.add(resource.getString(R.string.building_03_02_01_floor_01));
        spinnerItems.add(resource.getString(R.string.building_03_02_01_floor_02));
        spinnerItems.add(resource.getString(R.string.building_03_02_01_floor_03));
        spinnerItems.add(resource.getString(R.string.building_03_02_01_floor_04));
        spinnerItems.add(resource.getString(R.string.building_04_floor_ug));
        spinnerItems.add(resource.getString(R.string.building_04_floor_00));
        spinnerItems.add(resource.getString(R.string.building_04_floor_01));
        spinnerItems.add(resource.getString(R.string.building_04_floor_02));
        spinnerItems.add(resource.getString(R.string.building_04_floor_03));
        spinnerItems.add(resource.getString(R.string.building_05_floor_ug));
        spinnerItems.add(resource.getString(R.string.building_05_floor_00));
        spinnerItems.add(resource.getString(R.string.building_05_floor_01));
        spinnerItems.add(resource.getString(R.string.building_05_floor_02));
        spinnerItems.add(resource.getString(R.string.building_05_floor_03));

        return spinnerItems;
    }

    //Get rooms, stairs, elevators and crossings from JSON
    private void getRoomsAndTransitions() {
        try {
            JSONHandler jsonHandler = new JSONHandler();
            String json;

            json = jsonHandler.readJsonFromAssets(this, JSON_FILE_ROOMS);
            rooms = jsonHandler.parseJsonRooms(json);

            json = jsonHandler.readJsonFromAssets(this, JSON_FILE_TRANSITIONS);
            transitions = jsonHandler.parseJsonTransitions(json);

        } catch (IOException e) {
            Log.e(TAG + "error reading or parsing JSON files", String.valueOf(e));
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
            Log.e(TAG + "QR-Code invalid", String.valueOf(e));
        }
    }

    //Get destination location room
    private void getDestinationLocation() {

        try {
            for (int i = 0; i < rooms.size(); i++) {

                if (rooms.get(i).getQRCode().equals(destinationQRCode)) {
                    destinationLocation = rooms.get(i);
                }
            }
        } catch (Exception e) {
            Log.e(TAG + "error getting destination location room", String.valueOf(e));
        }

    }

    //Calculate route (get ArrayList<Cell> of cells to walk through buildings and floors)
    private void getRoute() {
        try {
            RouteCalculator routeCalculator = new RouteCalculator(this, startLocation, destinationLocation, transitions);
            cellsToWalk.addAll(routeCalculator.getNavigationCells());
        } catch (Exception e) {
            Log.e(TAG + "error calculating route ", String.valueOf(e));
        }
    }

    //Draw graphical output
    private void drawNavigation(String building, String floor) {

        //Constraint layouts to add views to
        @SuppressLint("CutPasteId") ConstraintLayout constraintLayoutFloorPlan = findViewById(R.id.navigation_placeholder);
        ConstraintLayout.LayoutParams layoutParamsFloorPlan = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        @SuppressLint("CutPasteId") ConstraintLayout constraintLayoutIcons = findViewById(R.id.navigation_placeholder);
        ConstraintLayout.LayoutParams layoutParamsIcons = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        //Remove views from layouts before redraw
        if (constraintLayoutFloorPlan != null) {
            constraintLayoutFloorPlan.removeAllViews();
        }
        if (constraintLayoutIcons != null) {
            constraintLayoutIcons.removeAllViews();
        }

        //Add floor plan JPEG from drawable to ConstraintLayout as ImageView
        try {
            ImageView floorPlan = new ImageView(this);
            floorPlan.setImageResource(getResources().getIdentifier("drawable/" + getFloorPlan(building, floor), null, getPackageName()));
            if (constraintLayoutFloorPlan != null) {
                constraintLayoutFloorPlan.addView(floorPlan, layoutParamsFloorPlan);
            }

        } catch (Exception e) {
            Log.e(TAG + "error drawing floor plan", String.valueOf(e));
        }

        //Add own location room icon to Overlay
        try {
            if (building.equals(startLocation.getBuilding()) && floor.equals(startLocation.getFloor())) {
                ImageView startIcon = new ImageView(this);
                startIcon.setImageResource(R.drawable.start_icon);
                startIcon.setX(startLocation.getXCoordinate() * X_SCALING);
                startIcon.setY(startLocation.getYCoordinate() * Y_SCALING);
                if (constraintLayoutIcons != null) {
                    constraintLayoutIcons.addView(startIcon, layoutParamsIcons);
                }
            }
        } catch (Exception e) {
            Log.e(TAG + "error drawing own location room", String.valueOf(e));
        }

        //Add destination location room icon to ConstraintLayout
        try {
            if (!destinationQRCode.equals(JUST_LOCATION) && building.equals(destinationLocation.getBuilding()) && floor.equals(destinationLocation.getFloor())) {
                ImageView destinationIcon = new ImageView(this);
                destinationIcon.setImageResource(R.drawable.destination_icon);
                destinationIcon.setX(destinationLocation.getXCoordinate() * X_SCALING);
                destinationIcon.setY(destinationLocation.getYCoordinate() * Y_SCALING);
                if (constraintLayoutIcons != null) {
                    constraintLayoutIcons.addView(destinationIcon, layoutParamsIcons);
                }
            }
        } catch (Exception e) {
            Log.e(TAG + "error drawing destination location room", String.valueOf(e));
        }

        //Add transitions icons to ConstraintLayout
            try {
            for (int i = 0; i < transitions.size(); i++) {
                for (int j = 0; j < transitions.get(i).getConnectedCells().size(); j++) {
                    if (transitions.get(i).getConnectedCells().get(j).getBuilding().equals(building)
                            && transitions.get(i).getConnectedCells().get(j).getFloor().equals(floor)) {

                        if (transitions.get(i).getTypeOfTransition().equals("stair")) {

                            ImageView stairIcon = new ImageView(this);
                            stairIcon.setImageResource(R.drawable.stair_icon);
                            stairIcon.setX(transitions.get(i).getConnectedCells().get(j).getXCoordinate() * X_SCALING);
                            stairIcon.setY(transitions.get(i).getConnectedCells().get(j).getXCoordinate() * Y_SCALING);
                            if (constraintLayoutIcons != null) {
                                constraintLayoutIcons.addView(stairIcon, layoutParamsIcons);
                            }
                        }
                        if (transitions.get(i).getTypeOfTransition().equals("elevator")) {

                            ImageView elevatorIcon = new ImageView(this);
                            elevatorIcon.setImageResource(R.drawable.elevator_icon);
                            elevatorIcon.setX(transitions.get(i).getConnectedCells().get(j).getXCoordinate() * X_SCALING);
                            elevatorIcon.setY(transitions.get(i).getConnectedCells().get(j).getXCoordinate() * Y_SCALING);
                            if (constraintLayoutIcons != null) {
                                constraintLayoutIcons.addView(elevatorIcon, layoutParamsIcons);
                            }
                        }
                        if (transitions.get(i).getTypeOfTransition().equals("crossing")) {

                            ImageView crossingIcon = new ImageView(this);
                            crossingIcon.setImageResource(R.drawable.crossing_icon);
                            crossingIcon.setX(transitions.get(i).getConnectedCells().get(j).getXCoordinate() * X_SCALING);
                            crossingIcon.setY(transitions.get(i).getConnectedCells().get(j).getXCoordinate() * Y_SCALING);
                            if (constraintLayoutIcons != null) {
                                constraintLayoutIcons.addView(crossingIcon, layoutParamsIcons);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG + "error drawing transitions", String.valueOf(e));
        }

        //Add route path to ConstraintLayout
        try {
            if (!destinationQRCode.equals(JUST_LOCATION)) {
                for (int j = 1; j < cellsToWalk.size(); j++) {
                    if (cellsToWalk.get(j).getBuilding().equals(building) && cellsToWalk.get(j).getFloor().equals(floor)) {

                        ImageView pathCellIcon = new ImageView(this);
                        pathCellIcon.setImageResource(R.drawable.path_cell_icon);
                        pathCellIcon.setX(cellsToWalk.get(j).getXCoordinate() * X_SCALING);
                        pathCellIcon.setY(cellsToWalk.get(j).getYCoordinate() * Y_SCALING);
                        if (constraintLayoutIcons != null) {
                            constraintLayoutIcons.addView(pathCellIcon, layoutParamsIcons);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG + "error drawing route", String.valueOf(e));
        }
    }

    //Get floor plan String without ending (.jpeg) from building and floor
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
            case "03.04":
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

    //Get building and floor Strings from floor plan String
    private ArrayList<String> getBuildingAndFloor(String in) {

        ArrayList<String> helperBuildingAndFloor = new ArrayList<>();

        Locale currentLocale = getResources().getConfiguration().getLocales().get(0);

        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_03_02_01_floor_ug))) {
            helperBuildingAndFloor.add("03");
            helperBuildingAndFloor.add("ug");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_03_02_01_floor_00))) {
            helperBuildingAndFloor.add("03");
            helperBuildingAndFloor.add("00");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_03_02_01_floor_01))) {
            helperBuildingAndFloor.add("03");
            helperBuildingAndFloor.add("01");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_03_02_01_floor_02))) {
            helperBuildingAndFloor.add("03");
            helperBuildingAndFloor.add("02");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_03_02_01_floor_03))) {
            helperBuildingAndFloor.add("03");
            helperBuildingAndFloor.add("03");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_03_02_01_floor_04))) {
            helperBuildingAndFloor.add("03");
            helperBuildingAndFloor.add("04");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_04_floor_ug))) {
            helperBuildingAndFloor.add("04");
            helperBuildingAndFloor.add("ug");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_04_floor_00))) {
            helperBuildingAndFloor.add("04");
            helperBuildingAndFloor.add("00");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_04_floor_01))) {
            helperBuildingAndFloor.add("04");
            helperBuildingAndFloor.add("01");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_04_floor_02))) {
            helperBuildingAndFloor.add("04");
            helperBuildingAndFloor.add("02");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_04_floor_03))) {
            helperBuildingAndFloor.add("04");
            helperBuildingAndFloor.add("03");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_05_floor_ug))) {
            helperBuildingAndFloor.add("05");
            helperBuildingAndFloor.add("ug");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_05_floor_00))) {
            helperBuildingAndFloor.add("05");
            helperBuildingAndFloor.add("00");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_05_floor_01))) {
            helperBuildingAndFloor.add("05");
            helperBuildingAndFloor.add("01");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_05_floor_02))) {
            helperBuildingAndFloor.add("05");
            helperBuildingAndFloor.add("02");
        }
        if (in.equals(getLocaleStringResource(currentLocale, R.string.building_05_floor_03))) {
            helperBuildingAndFloor.add("05");
            helperBuildingAndFloor.add("03");
        }
        return helperBuildingAndFloor;
    }

    //Get locale
    private String getLocaleStringResource(Locale currentLocale, int floorPlan) {

        String localeString;

        Configuration configuration = new Configuration(this.getResources().getConfiguration());
        configuration.setLocale(currentLocale);
        localeString = this.createConfigurationContext(configuration).getString(floorPlan);

        return localeString;
    }
}
