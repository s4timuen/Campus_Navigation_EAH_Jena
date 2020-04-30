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
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.eahjena.wi.campusnavigationeahjena.R;
import de.eahjena.wi.campusnavigationeahjena.controls.JSONHandler;
import de.eahjena.wi.campusnavigationeahjena.controls.RouteCalculator;
import de.eahjena.wi.campusnavigationeahjena.fragments.NavigationMapFragment;
import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Room;
import de.eahjena.wi.campusnavigationeahjena.models.Transition;

public class NavigationActivity extends AppCompatActivity {

    private static final String TAG = "NavigationActivity"; //$NON-NLS

    //Constants
    private static final String JSON_FILE_ROOMS = "rooms.json";
    private static final String JSON_FILE_TRANSITIONS = "transitions.json";
    private static final String JUST_LOCATION = "just own location";

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
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                        Bundle transitionsAsBundle = new Bundle();
                        Bundle cellsToWalkAsBundle = new Bundle();
                        transitionsAsBundle.putSerializable("transitions", transitions);
                        cellsToWalkAsBundle.putSerializable("cellsToWalk", cellsToWalk);
                        NavigationMapFragment navigationMapFragment = NavigationMapFragment.newInstance(floorPlans.get(index),
                                null, null, transitionsAsBundle, cellsToWalkAsBundle);
                        fragmentTransaction.replace(R.id.map_fragment_placeholder, new NavigationMapFragment());
                        fragmentTransaction.commit();
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
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Bundle transitionsAsBundle = new Bundle();
        Bundle cellsToWalkAsBundle = new Bundle();
        transitionsAsBundle.putSerializable("transitions", transitions);
        cellsToWalkAsBundle.putSerializable("cellsToWalk", cellsToWalk);
        NavigationMapFragment navigationMapFragment = NavigationMapFragment.newInstance(null,
                startLocation.getBuilding(), startLocation.getFloor(), transitionsAsBundle, cellsToWalkAsBundle);
        fragmentTransaction.replace(R.id.map_fragment_placeholder, new NavigationMapFragment());
        fragmentTransaction.commit();
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
}