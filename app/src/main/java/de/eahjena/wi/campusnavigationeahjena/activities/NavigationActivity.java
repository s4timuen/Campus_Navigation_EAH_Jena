package de.eahjena.wi.campusnavigationeahjena.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.eahjena.wi.campusnavigationeahjena.R;
import de.eahjena.wi.campusnavigationeahjena.controls.JSONHandler;
import de.eahjena.wi.campusnavigationeahjena.controls.MapDrawer;
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
    private String destinationQRCode;
    private String scannedQRCode;
    private Room startLocation;
    private Room destinationLocation;

    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Transition> transitions = new ArrayList<>();
    private ArrayList<Cell> cellsToWalk = new ArrayList<>();

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Spinner select floor plans
        final ArrayList<String> floorPlans = new ArrayList<>();
        floorPlans.addAll(getItemsSpinner());

        final Spinner floorPlansSpinner = findViewById(R.id.spinner_floor_plans);
        ArrayAdapter<String> floorPlansAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, (List<String>) floorPlansSpinner);
        floorPlansSpinner.setAdapter(floorPlansAdapter);
        floorPlansSpinner.setSelection(0, false);
        floorPlansSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
                Object item = adapterView.getItemAtPosition(index);
                if (item != null && index != 0) {
                    try {
                        MapDrawer mapDrawer = new MapDrawer(floorPlans.get(index));
                        mapDrawer.drawNavigation();
                    } catch (Exception e) {
                        Log.e("NavigationActivity MapDrawer Error", String.valueOf(e));
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
        MapDrawer mapDrawer = new MapDrawer(startLocation);
        mapDrawer.drawNavigation();
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
    @SuppressLint("LongLogTag")
    private void getRoomsAndTransitions() {
        try {
            JSONHandler jsonHandler = new JSONHandler();
            String json;

            json = jsonHandler.readJsonFromAssets(this, JSON_FILE_ROOMS);
            rooms = jsonHandler.parseJsonRooms(json);

            json = jsonHandler.readJsonFromAssets(this, JSON_FILE_TRANSITIONS);
            transitions = jsonHandler.parseJsonTransitions(json);

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


    //Calculate route (get ArrayList<Cell> of cells to walk through buildings and floors)
    @SuppressLint("LongLogTag")
    private void getRoute() {
        try {
            RouteCalculator routeCalculator = new RouteCalculator(this, startLocation, destinationLocation, transitions);
            cellsToWalk = routeCalculator.getNavigationCells();

        } catch (Exception e) {
            Log.e("Error calculating route " + TAG, String.valueOf(e));
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
}