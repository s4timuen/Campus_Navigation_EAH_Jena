package de.eahjena.wi.campusnavigationeahjena.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import de.eahjena.wi.campusnavigationeahjena.R;
import de.eahjena.wi.campusnavigationeahjena.controls.MapDrawer;
import de.eahjena.wi.campusnavigationeahjena.models.Cell;
import de.eahjena.wi.campusnavigationeahjena.models.Transition;

public class NavigationMapFragment extends Fragment {

    private static final String TAG = "NavigationMapFragment"; //$NON-NLS

    //Variables
    String floorPlan;
    String startLocationBuilding;
    String StartLocationFloor;
    ArrayList<Transition> transitions;
    ArrayList<Cell> cellsToWalk;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        floorPlan = getArguments().getString("floorPlan", null);
        startLocationBuilding = getArguments().getString("startLocationBuilding", null);
        StartLocationFloor = getArguments().getString("startLocationFloor", null);
        transitions = (ArrayList<Transition>)getArguments().getSerializable("transitions");
        cellsToWalk = (ArrayList<Cell>)getArguments().getSerializable("cellsToWalk");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_navigation_map, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        MapDrawer mapDrawer = new MapDrawer(floorPlan, startLocationBuilding, StartLocationFloor, transitions, cellsToWalk);
        mapDrawer.drawNavigation();
    }

    //Get params from activity (transitions, cellsToWalk and ether floor plan String or building and floor Strings)
    public static NavigationMapFragment newInstance(String floorPlan, String startLocationBuilding,
                                                    String startLocationFloor, Bundle transitions, Bundle cellsToWalk) {
        NavigationMapFragment navigationMapFragment = new NavigationMapFragment();
        Bundle args = new Bundle();
        args.putString("floorPlan", floorPlan);
        args.putString("startLocationBuilding", startLocationBuilding);
        args.putString("startLocationFloor", startLocationFloor);
        args.putBundle("transitions", transitions);
        args.putBundle("cellsToWalk", cellsToWalk);
        navigationMapFragment.setArguments(args);
        return navigationMapFragment;
    }
}
