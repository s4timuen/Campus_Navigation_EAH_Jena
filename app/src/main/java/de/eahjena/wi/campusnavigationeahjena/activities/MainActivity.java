package de.eahjena.wi.campusnavigationeahjena.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import de.eahjena.wi.campusnavigationeahjena.R;
import de.eahjena.wi.campusnavigationeahjena.controls.JSONHandler;
import de.eahjena.wi.campusnavigationeahjena.models.Room;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity"; //$NON-NLS

    //Constants
    private static final String JUST_LOCATION = "location";
    private static final String JSON_FILE_ROOMS = "rooms.json";

    //Variables
    private String destinationQRCode;
    private ArrayList<Room> rooms = new ArrayList<>();
    private TextInputLayout findStartLocationLayoutText;
    private TextInputEditText findStartLocationEditText;
    private TextInputLayout findDestinationLocationLayoutText;
    private TextInputEditText findDestinationLocationEditText;
    private int roomsIndex = 0;
    private int personsIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get lists of rooms and names for spinners
        JSONHandler jsonHandler = new JSONHandler();
        String json;

        try {
            json = jsonHandler.readJsonFromAssets(this, JSON_FILE_ROOMS);
            rooms = jsonHandler.parseJsonRooms(json);
        } catch (Exception e) {
            Log.e(TAG + " error reading or parsing JSON file", String.valueOf(e));
            e.printStackTrace();
        }

        //Get lists of room names and persons for spinners
        Resources resource = getResources();
        final ArrayList<String> roomNames = new ArrayList<>();
        ArrayList<String> persons = new ArrayList<>();

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
            e.printStackTrace();
        }

        //Sort rooms and persons alphabetically
        Collections.sort(roomNames, new Comparator<String>() {
            @Override
            public int compare(String stringOne, String stringTwo) {
                return stringOne.compareTo(stringTwo);
            }
        });
        Collections.sort(persons, new Comparator<String>() {
            @Override
            public int compare(String stringOne, String stringTwo) {
                return stringOne.compareTo(stringTwo);
            }
        });

        //Default elements
        String defaultSelection = resource.getString(R.string.select_from_spinner);
        roomNames.add(0, defaultSelection);
        persons.add(0, defaultSelection);

        //Spinners
        final Spinner searchByRoomSpinner = findViewById(R.id.spinner_by_room);
        final Spinner searchByPersonSpinner = findViewById(R.id.spinner_by_person);

        //Spinner for room search
        ArrayAdapter<String> searchByRoomAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, roomNames);
        searchByRoomSpinner.setAdapter(searchByRoomAdapter);
        searchByRoomSpinner.setSelection(0, false);
        searchByRoomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {

                Object item = adapterView.getItemAtPosition(index);

                if (item != null && index != 0) {

                    for (int i = 0; i < rooms.size(); i++) {

                        String checkQRCode = item.toString();
                        checkQRCode.replace(".", "");

                        if (checkQRCode.equals(rooms.get(i).getRoomName())) {

                            destinationQRCode = rooms.get(i).getQRCode();
                            roomsIndex = i;
                            searchByPersonSpinner.setSelection(0);
                            findDestinationLocationEditText.setText("");
                        }
                    }
                }

                if (item != null && index == 0) {

                    roomsIndex = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

        //Spinner for person search
        ArrayAdapter<String> searchByPersonAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, persons);
        searchByPersonSpinner.setAdapter(searchByPersonAdapter);
        searchByPersonSpinner.setSelection(0, false);
        searchByPersonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {

                Object item = adapterView.getItemAtPosition(index);

                if (item != null && index != 0) {

                    for (int i = 0; i < rooms.size(); i++) {

                        for (int j = 0; j < rooms.get(i).getPersons().size(); j++) {

                            if (item.equals(rooms.get(i).getPersons().get(j))) {

                                destinationQRCode = rooms.get(i).getQRCode();
                                personsIndex = i;
                                searchByRoomSpinner.setSelection(0);
                                findDestinationLocationEditText.setText("");
                            }
                        }
                    }
                }

                if (item != null && index == 0) {

                    personsIndex = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

        //Start location input field
        findStartLocationLayoutText = findViewById(R.id.input_field_search_start_room_layout);
        findStartLocationEditText = findViewById(R.id.input_field_search_start_room_edit);
        findStartLocationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    handleUserInputErrorAndIntent(textView);
                }
                return false;
            }
        });

        //Destination location input field
        findDestinationLocationLayoutText = findViewById(R.id.input_field_search_destination_room_layout);
        findDestinationLocationEditText = findViewById(R.id.input_field_search_destination_room_edit);
        findDestinationLocationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                searchByRoomSpinner.setSelection(0);
                searchByPersonSpinner.setSelection(0);
                roomsIndex = 0;
                personsIndex = 0;

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    handleUserInputErrorAndIntent(textView);
                }
                return false;
            }
        });

        //Find own location button qr-code
        Button findOwnLocationButtonQR = findViewById(R.id.button_location_qr);
        findOwnLocationButtonQR.setOnClickListener(this);

        //Find own location button text
        Button findOwnLocationButtonText = findViewById(R.id.button_location_text);
        findOwnLocationButtonText.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        try {

            //QR button
            if (view.getId() == R.id.button_location_qr) {

                handleUserInputErrorAndIntent(view);
            }

            //Search button
            if (view.getId() == R.id.button_location_text) {

                handleUserInputErrorAndIntent(view);
            }
        } catch (Exception e) {
            Log.e(TAG + " onClick exception", String.valueOf(e));
            e.printStackTrace();
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

    //User input error handling and input combinations error handling
    private void handleUserInputErrorAndIntent(View view) {

        String userInputStartLocation = Objects.requireNonNull(findStartLocationEditText.getText()).toString().replace(".", "");
        String userInputDestinationLocation = Objects.requireNonNull(findDestinationLocationEditText.getText()).toString().replace(".", "");

        ArrayList<String> roomNames = new ArrayList<>();

        //Get available rooms
        for (int index = 0; index < rooms.size(); index++) {

            roomNames.add(rooms.get(index).getRoomName());
        }

        //User start location input error handling
        if (!findStartLocationEditText.getText().toString().equals("")
                && !roomNames.contains(findStartLocationEditText.getText().toString())) {

            findStartLocationLayoutText.setError(getText(R.string.error_message_room_input));
        }

        if (findStartLocationEditText.getText().toString().equals("")
                || roomNames.contains(findStartLocationEditText.getText().toString())) {

            findStartLocationLayoutText.setError(null);
        }

        //User destination location input error handling
        if (!findDestinationLocationEditText.getText().toString().equals("")
                && !roomNames.contains(findDestinationLocationEditText.getText().toString())) {

            findDestinationLocationLayoutText.setError(getText(R.string.error_message_room_input));
        }

        if (findDestinationLocationEditText.getText().toString().equals("")
                || roomNames.contains(findDestinationLocationEditText.getText().toString())) {

            findDestinationLocationLayoutText.setError(null);
        }

        //Input combinations error handling
        if (findStartLocationLayoutText.getError() == null && findDestinationLocationLayoutText.getError() == null) {

            //Use start QR-Code to show own position
            if (roomsIndex == 0 && personsIndex == 0 && userInputStartLocation.equals("")
                    && userInputDestinationLocation.equals("") && view.getId() == R.id.button_location_qr) {

                destinationQRCode = JUST_LOCATION;

                doIntent(userInputStartLocation, roomNames, false);
            }

            //Use user start input to show own location
            if (roomsIndex == 0 && personsIndex == 0 && !userInputStartLocation.equals("")
                    && userInputDestinationLocation.equals("") && view.getId() == R.id.button_location_text) {

                destinationQRCode = JUST_LOCATION;

                doIntent(userInputStartLocation, roomNames, true);
            }

            //Use start QR-Code and destination selection to perform navigation
            if ((roomsIndex != 0 || personsIndex != 0) && userInputStartLocation.equals("")
                    && userInputDestinationLocation.equals("") && view.getId() == R.id.button_location_qr) {

                doIntent(userInputStartLocation, roomNames, false);
            }

            //Use start QR-Code and user destination input to perform navigation
            if (roomsIndex == 0 && personsIndex == 0 && userInputStartLocation.equals("")
                    && !userInputDestinationLocation.equals("") && view.getId() == R.id.button_location_qr) {

                destinationQRCode = userInputDestinationLocation;

                doIntent(userInputStartLocation, roomNames, false);
            }

            //Use user start input and destination selection to perform navigation
            if ((roomsIndex != 0 || personsIndex != 0) && !userInputStartLocation.equals("")
                    && userInputDestinationLocation.equals("") && view.getId() == R.id.button_location_text) {

                doIntent(userInputStartLocation, roomNames, true);
            }

            //Use user start and destination input to perform navigation
            if (roomsIndex == 0 && personsIndex == 0 && !userInputStartLocation.equals("")
                    && !userInputDestinationLocation.equals("") && view.getId() == R.id.button_location_text) {

                destinationQRCode = userInputDestinationLocation;

                doIntent(userInputStartLocation, roomNames, true);
            }
        }
    }

    //Intent
    private void doIntent(String userInputStartLocation, ArrayList<String> roomNames, boolean skipScanner) {

        Intent intentScannerActivity = new Intent(this, ScannerActivity.class);
        intentScannerActivity.putExtra("destinationLocation", destinationQRCode);
        intentScannerActivity.putExtra("startLocation", userInputStartLocation);
        intentScannerActivity.putExtra("skipScanner", skipScanner);
        intentScannerActivity.putExtra("availableRooms", roomNames);
        startActivity(intentScannerActivity);
    }
}
