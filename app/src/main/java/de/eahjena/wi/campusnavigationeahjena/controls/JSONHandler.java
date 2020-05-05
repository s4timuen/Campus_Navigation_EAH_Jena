package de.eahjena.wi.campusnavigationeahjena.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Room;
import de.eahjena.wi.campusnavigationeahjena.models.Transition;

public class JSONHandler {

    private static final String TAG = "JSONHandler"; //$NON-NLS

    //Constructor
    public JSONHandler() {
    }

    //Read JSON from assets
    public String readJsonFromAssets(Context context, String jsonFile) throws IOException {

        AssetManager assetManager = context.getAssets();
        String json = null;

        try {
            InputStream inputStream = assetManager.open(jsonFile);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");

        } catch (Exception e) {
            Log.e("Error reading JSON file", String.valueOf(e));
        }

        return json;
    }

    //Parse JSON to rooms ArrayList<Cell>
    @SuppressLint("LongLogTag")
    public ArrayList<Room> parseJsonRooms(String json) {
        ArrayList<Room> roomData = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                Room entry = new Room();

                JSONObject jEntry = jsonArray.getJSONObject(i);
                entry.setId(jEntry.optInt("id"));
                entry.setRoomNumber(jEntry.optString("roomNumber"));
                entry.setBuilding(jEntry.optString("building"));
                entry.setFloor(jEntry.optString("floor"));
                entry.setQRCode(jEntry.optString("qrCode"));
                entry.setXCoordinate(jEntry.optInt("xCoordinate"));
                entry.setYCoordinate(jEntry.optInt("yCoordinate"));
                entry.setWalkability(jEntry.optBoolean("walkable"));

                JSONArray personsJSON = jEntry.getJSONArray("persons");
                ArrayList<String> persons = new ArrayList<>();
                for (int j = 0; j < personsJSON.length(); j++) {
                    persons.add(personsJSON.getString(j));
                }
                entry.setPersons(persons);

                roomData.add(entry);
            }
        } catch (Exception e) {
            Log.e("Error parsing JSON rooms array", String.valueOf(e));
        }

        return roomData;
    }

    //Parse JSON to walkableCells ArrayList<Cell>
    @SuppressLint("LongLogTag")
    public ArrayList<Cell> parseJsonGrid(String json) {
        ArrayList<Cell> gridData = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                Cell entry = new Cell();

                JSONObject jEntry = jsonArray.getJSONObject(i);
                entry.setXCoordinate(jEntry.optInt("xCoordinate"));
                entry.setYCoordinate(jEntry.optInt("yCoordinate"));
                entry.setWalkability(jEntry.optBoolean("walkable"));

                gridData.add(entry);
            }
        } catch (Exception e) {
            Log.e("Error parsing JSON grid array", String.valueOf(e));
        }

        return gridData;
    }

    //Parse JSON to stairs ArrayList<Cell>
    @SuppressLint("LongLogTag")
    public ArrayList<Transition> parseJsonTransitions(String json) {
        ArrayList<Transition> transitionData = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                Transition entry = new Transition();

                JSONObject jEntry = jsonArray.getJSONObject(i);
                entry.setId(jEntry.optInt("id"));
                entry.setTypeOfTransition(jEntry.optString("type"));
                entry.setXCoordinate(jEntry.optInt("xCoordinate"));
                entry.setYCoordinate(jEntry.optInt("yCoordinate"));
                entry.setWalkability(jEntry.optBoolean("walkable"));

                JSONArray reachableFloors = jEntry.getJSONArray("reachableFloors");
                ArrayList<String> floors = new ArrayList<>();
                for (int j = 0; j < reachableFloors.length(); j++) {
                    floors.add(reachableFloors.getString(j));
                }
                entry.setReachableFloors(floors);

                JSONArray reachableBuildings = jEntry.getJSONArray("reachableBuildings");
                ArrayList<String> buildings = new ArrayList<>();
                for (int j = 0; j < reachableBuildings.length(); j++) {
                    buildings.add(reachableBuildings.getString(j));
                }
                entry.setReachableBuildings(buildings);


                transitionData.add(entry);
            }
        } catch (Exception e) {
            Log.e("Error parsing JSON stairs array", String.valueOf(e));
        }

        return transitionData;
    }
}
