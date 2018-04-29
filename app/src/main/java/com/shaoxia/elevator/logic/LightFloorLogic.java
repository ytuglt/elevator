package com.shaoxia.elevator.logic;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.shaoxia.elevator.R;
import com.shaoxia.elevator.model.MDevice;

import java.util.ArrayList;
import java.util.List;

public class LightFloorLogic {
    public static void updateFloorsView(Activity activity, MDevice device, int lightPos) {
        TextView tvOne = activity.findViewById(R.id.floor_one);
        TextView tvTwo = activity.findViewById(R.id.floor_two);
        TextView tvThree = activity.findViewById(R.id.floor_three);
        TextView tvFour = activity.findViewById(R.id.floor_four);
        TextView tvFive = activity.findViewById(R.id.floor_five);

        List<String> floors;
        List<String> allFloors = device.getFloors();
        if (allFloors.size() >= 5) {
            floors = getFloors(device, lightPos);
            tvOne.setText(floors.get(0));
            tvTwo.setText(floors.get(1));
            tvThree.setText(floors.get(2));
            tvFour.setText(floors.get(3));
            tvFive.setText(floors.get(4));
        } else {
            floors = allFloors;
            switch (allFloors.size()) {
                case 2:
                    tvOne.setText(floors.get(0));
                    tvTwo.setText(floors.get(1));
                    tvThree.setVisibility(View.GONE);
                    tvFour.setVisibility(View.GONE);
                    tvFive.setVisibility(View.GONE);
                    break;
                case 3:
                    tvOne.setText(floors.get(0));
                    tvTwo.setText(floors.get(1));
                    tvThree.setText(floors.get(2));
                    tvFour.setVisibility(View.GONE);
                    tvFive.setVisibility(View.GONE);
                    break;
                case 4:
                    tvOne.setText(floors.get(0));
                    tvTwo.setText(floors.get(1));
                    tvThree.setText(floors.get(2));
                    tvFour.setText(floors.get(3));
                    tvFive.setVisibility(View.GONE);
                    break;
            }
        }

        int pos = floors.indexOf(device.getFloors().get(lightPos));
        switch (pos) {
            case 0:
                lightCurFloor(activity,tvOne);
                break;
            case 1:
                lightCurFloor(activity,tvTwo);
                break;
            case 2:
                lightCurFloor(activity,tvThree);
                break;
            case 3:
                lightCurFloor(activity,tvFour);
                break;
            case 4:
                lightCurFloor(activity,tvFive);
                break;
        }
    }
    private static void lightCurFloor(Activity activity, TextView textView) {
        textView.setBackgroundResource(R.drawable.floor_back_sel);
        textView.setTextColor(activity.getResources().getColor(R.color.floor_light, null));
        textView.setShadowLayer(40, 0, 0, activity.getResources().getColor(R.color.floor_light, null));
    }

    private static List<String> getFloors(MDevice device, int lightPos) {
        List<String> floors = new ArrayList<>();
        List<String> allFloors = device.getFloors();
        int curpos = lightPos;

        int size = allFloors.size();
        if (curpos - 2 <= 0) {
            for (int i = 0; i < 5; i++) {
                floors.add(allFloors.get(i));
            }
        } else if (curpos + 2 >= (size - 1)) {
            for (int i = size - 5; i < size; i++) {
                floors.add(allFloors.get(i));
            }
        } else {
            for (int i = curpos - 2; i <= curpos + 2; i++) {
                floors.add(allFloors.get(i));
            }
        }

        return floors;
    }

}
