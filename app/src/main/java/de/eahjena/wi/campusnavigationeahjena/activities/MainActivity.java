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
    private static final String JUST_LOCATION = "just own location";
    private static final String JSON_FILE_ROOMS = "rooms.json";

    //Variables
    private String destinationQRCode;
    ArrayList<Room> rooms = new ArrayList<>();
    TextInputLayout findOwnLocationLayoutText;
    TextInputEditText findOwnLocationEditText;

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
        }

        //Sort rooms and persons alphabetically
        Collections.sort(roomNames, new Comparator<String>() {
            @Override
            public int compare(String stringOne, String stringTwo) {
                return stringOne.compareTo(stringTwo);
            }});
        Collections.sort(persons, new Comparator<String>() {
            @Override
            public int compare(String stringOne, String stringTwo) {
                return stringOne.compareTo(stringTwo);
            }});

        //Default elements
        String defaultSelection = resource.getString(R.string.select_from_spinner);
        roomNames.add(0, defaultSelection);
        persons.add(0, defaultSelection);

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
                    for (int i = 0; i < rooms.size(); i++) {
                        //item 03.03.33 qr-code 030333
                        String checkQRCode = item.toString();
                        checkQRCode.replace(".", "");
                        if (checkQRCode.equals(rooms.get(i).getRoomName())) {
                            destinationQRCode = rooms.get(i).getQRCode();
                        }
                    }

                    try {
                        Intent intentScannerActivity = new Intent(view.getContext(), ScannerActivity.class);
                        intentScannerActivity.putExtra("destinationQRCode", destinationQRCode);
                        roomNames.remove(0);
                        intentScannerActivity.putExtra("availableRooms", roomNames);
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
                    for (int i = 0; i < rooms.size(); i++) {
                        for (int j = 0; j < rooms.get(i).getPersons().size(); j++) {
                            if (item.equals(rooms.get(i).getPersons().get(j))) {
                                destinationQRCode = rooms.get(i).getQRCode();
                            }
                        }
                    }
                    try {
                        Intent intentScannerActivity = new Intent(view.getContext(), ScannerActivity.class);
                        intentScannerActivity.putExtra("destinationQRCode", destinationQRCode);
                        roomNames.remove(0);
                        intentScannerActivity.putExtra("availableRooms", roomNames);
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

        //Find own location input field
        findOwnLocationLayoutText = findViewById(R.id.input_field_search_room_layout);
        findOwnLocationEditText = findViewById(R.id.input_field_search_room_edit);
        findOwnLocationEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    handleUserInputErrorAndIntent();
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

                //Intent
                destinationQRCode = JUST_LOCATION;
                Intent intentScannerActivity = new Intent(this, ScannerActivity.class);
                intentScannerActivity.putExtra("destinationLocation", destinationQRCode);
                intentScannerActivity.putExtra("skipScanner", false);
                startActivity(intentScannerActivity);
            }

            //Search button
            if (view.getId() == R.id.button_location_text) {

                handleUserInputErrorAndIntent();
            }
        } catch (Exception e) {
            Log.e(TAG + " onClick exception", String.valueOf(e));
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

    //Handle user input valid rooms
    private void handleUserInputErrorAndIntent() {

        String userInput = Objects.requireNonNull(findOwnLocationEditText.getText()).toString().replace(".", "");

        ArrayList<String> roomNames = new ArrayList<>();

        //Check valid input
        for (int index = 0; index < rooms.size(); index++) {

            roomNames.add(rooms.get(index).getRoomName());
        }
        if (!roomNames.contains(findOwnLocationEditText.getText().toString())) {

            findOwnLocationLayoutText.setError(getText(R.string.error_message_room_input));
        }
        if (roomNames.contains(findOwnLocationEditText.getText().toString())) {

            findOwnLocationLayoutText.setError(null);
        }

        //Intent
        if (findOwnLocationLayoutText.getError() == null) {
            destinationQRCode = JUST_LOCATION;
            Intent intentScannerActivity = new Intent(this, ScannerActivity.class);
            intentScannerActivity.putExtra("destinationLocation", destinationQRCode);
            intentScannerActivity.putExtra("ownLocation", userInput);
            intentScannerActivity.putExtra("skipScanner", true);
            startActivity(intentScannerActivity);
        }
    }
}
