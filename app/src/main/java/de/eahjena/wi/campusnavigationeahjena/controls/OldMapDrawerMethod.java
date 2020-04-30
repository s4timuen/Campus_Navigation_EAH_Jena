package de.eahjena.wi.campusnavigationeahjena.controls;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import de.eahjena.wi.campusnavigationeahjena.R;

public class OldMapDrawerMethod {

    /**

    //Draw image of floor plan, onw location room, destination location room, transitions and route
    @SuppressLint("LongLogTag")
    public void drawNavigation() {

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
            floorPlan.setImageResource(getResources().getIdentifier("drawable/" + getFloorPlan(startLocation), null, getPackageName()));
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
                for (int j = 1; j < cellsToWalk.size(); j++){

                    ImageView pathCellIcon = new ImageView(getApplicationContext());
                    pathCellIcon.setImageResource(R.drawable.path_cell_icon);
                    pathCellIcon.setX(cellsToWalk.get(j).getXCoordinate() * X_SCALING);
                    pathCellIcon.setY(cellsToWalk.get(j).getYCoordinate() * Y_SCALING);
                    constraintLayoutIcons.addView(pathCellIcon, layoutParamsIcons);

                }
            }
        } catch (Exception e) {
            Log.e("Error drawing route", String.valueOf(e));
        }
    }

    **/
}
