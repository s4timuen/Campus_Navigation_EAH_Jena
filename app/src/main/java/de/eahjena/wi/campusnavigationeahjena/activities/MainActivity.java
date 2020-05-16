package de.eahjena.wi.campusnavigationeahjena.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

import de.eahjena.wi.campusnavigationeahjena.R;
import de.eahjena.wi.campusnavigationeahjena.controls.JSONHandler;
import de.eahjena.wi.campusnavigationeahjena.models.Room;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity"; //$NON-NLS

    //Constants
    private static final String JUST_LOCATION = "just own location";
    private static final String JSON_FILE_ROOMS = "rooms.json";

    //Variables
    private String destinationQRCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get lists of rooms and names for spinners
        ArrayList<Room> rooms = new ArrayList<>();
        JSONHandler jsonHandler = new JSONHandler();
        String json;

        try {
            json = jsonHandler.readJsonFromAssets(this, JSON_FILE_ROOMS);
            rooms = jsonHandler.parseJsonRooms(json);
        } catch (Exception e) {
            Log.e(TAG + " error reading or parsing JSON file", String.valueOf(e));
        }

        //Get lists of room names and persons for spinners
        Resources resource = getResources();
        String defaultSelection = resource.getString(R.string.select_from_spinner);
        ArrayList<String> roomNames = new ArrayList<>();
        roomNames.add(defaultSelection);
        ArrayList<String> persons = new ArrayList<>();
        persons.add(defaultSelection);

        try {
            for (int i = 0; i < rooms.size(); i++) {

                String name;
                name = rooms.get(i).getRoomName();
                roomNames.add(name);

                if (!rooms.get(i).getPersons().isEmpty()) {
                    for (int j = 0; j < rooms.get(i).getPersons().size(); j++) {
                        String person;
                        person = rooms.get(i).getPersons().get(j);
                        persons.add(person);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG + " error creating lists for spinners", String.valueOf(e));
        }

        final ArrayList<Room> finalRooms = rooms;

        //Spinner for room search intent
        final Spinner searchByRoom = findViewById(R.id.spinner_by_room);
        ArrayAdapter<String> searchByRoomAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, roomNames);
        searchByRoom.setAdapter(searchByRoomAdapter);
        searchByRoom.setSelection(0, false);
        searchByRoom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
                Object item = adapterView.getItemAtPosition(index);
                if (item != null && index != 0) {
                    destinationQRCode = finalRooms.get(index - 1).getQRCode();
                    try {
                        Intent intentScannerActivity = new Intent(view.getContext(), ScannerActivity.class);
                        intentScannerActivity.putExtra("destinationQRCode", destinationQRCode);
                        startActivity(intentScannerActivity);
                    } catch (Exception e) {
                        Log.e(TAG + " intend exception", String.valueOf(e));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

        //Spinner for person search intents
        Spinner searchByPerson = findViewById(R.id.spinner_by_person);
        ArrayAdapter<String> searchByPersonAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, persons);
        searchByPerson.setAdapter(searchByPersonAdapter);
        searchByPerson.setSelection(0, false);
        searchByPerson.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
                Object item = adapterView.getItemAtPosition(index);
                if (item != null && index != 0) {
                    for (int i = 0; i < finalRooms.size(); i++) {
                        for (int j = 0; j < finalRooms.get(i).getPersons().size(); j++) {
                            if (item.equals(finalRooms.get(i).getPersons().get(j))) {
                                destinationQRCode = finalRooms.get(i).getQRCode();
                            }
                        }
                    }
                    try {
                        Intent intentScannerActivity = new Intent(view.getContext(), ScannerActivity.class);
                        intentScannerActivity.putExtra("destinationQRCode", destinationQRCode);
                        startActivity(intentScannerActivity);
                    } catch (Exception e) {
                        Log.e(TAG + " intend exception", String.valueOf(e));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

        //Find own location button
        Button findOwnLocation = findViewById(R.id.button_location);
        findOwnLocation.setOnClickListener(this);

    }

    @Override
    public void onClick (View view){

        if (view.getId() == R.id.button_location) {
            destinationQRCode = JUST_LOCATION;
        }
        try {
            Intent intentScannerActivity = new Intent(this, ScannerActivity.class);
            intentScannerActivity.putExtra("destinationLocation", destinationQRCode);
            startActivity(intentScannerActivity);
        } catch (Exception e) {
            Log.e(TAG + " intend exception", String.valueOf(e));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
