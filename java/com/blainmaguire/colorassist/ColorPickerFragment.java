
package com.blainmaguire.colorassist;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.blainmaguire.colorassist.CommonConstants.ViewMode;

import java.util.ArrayList;

public class ColorPickerFragment extends Fragment implements ColorPickerDialog.OnColorChangedListener {

    private char[][] savedColors = new char[2*4][3];
    private int selectedColor = 0;

    private ColorNameLookup colorNameLookup = new ColorNameLookup();

    private Button setColorButton;
    private Button useCameraButton;
    private TextView huePane;
    private TextView hueName;
    private TextView hueRGB;
    private ArrayList<View> touchables;

    private CameraViewMode cameraViewMode;

    public interface onColorChangedListener {
        public void onColorChanged(char r, char g, char b);
        public char[] getColorFromCamera();
    }

    onColorChangedListener colorChangedListener;

    private ColorPickerDialog dialog;
    private Context fragmentContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        fragmentContext = context;
        Activity activity;

        if (context instanceof Activity){
            activity=(Activity) context;
            try {
                cameraViewMode = (CameraViewMode) activity;
                colorChangedListener=(onColorChangedListener) activity;

            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement cameraViewMode, colorChangedListner");
            }
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.picker_fragment, container, false);

        setColorButton = (Button)view.findViewById(R.id.setCustom);
        useCameraButton = (Button)view.findViewById(R.id.fromCamera);

        touchables = view.findViewById(R.id.colorPickerTable).getTouchables();
        boolean foundFirstButton = false;

        for (int i=0; i< touchables.size(); i++) {
            View touchable = touchables.get(i);

            if (touchable instanceof ToggleButton) {

                if (!foundFirstButton) {
                    ((ToggleButton) touchable).setChecked(true);
                    foundFirstButton = true;
                }
                touchable.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View button, MotionEvent event) {
                        int count = 0;
                        if (event.getAction()==MotionEvent.ACTION_DOWN) {
                            for (View touchable : touchables) {
                                if (button.equals(touchable)) {
                                    selectedColor = count;
                                    char[] selectedRGB = savedColors[selectedColor];
                                    colorChangedListener.onColorChanged(selectedRGB[0], selectedRGB[1], selectedRGB[2]);
                                    setPreview();
                                }
                                if (touchable instanceof ToggleButton) {
                                    ((ToggleButton) touchable).setChecked(false);
                                    count++;
                                }
                            }

                            ((ToggleButton) button).setChecked(true);
                        }
                        return true;
                    }
                });
            }
        }

        huePane = (TextView)view.findViewById(R.id.previewPane);
        hueName = (TextView)view.findViewById(R.id.previewName);
        hueRGB = (TextView)view.findViewById(R.id.previewRGB);
        setPreview();

        //dialog = new ColorPickerDialog(getApplicationContext(), this, 0);
        dialog = new ColorPickerDialog(fragmentContext, this,0);

        setColorButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View button) {
                dialog.show();

            }

        });

        useCameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View button) {
                savedColors[selectedColor] = colorChangedListener.getColorFromCamera();
                setPreview();
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && cameraViewMode!=null) {
            cameraViewMode.GetCameraViewMode(ViewMode.SIMILARITY);
            setPreview();
        }
    }

    @Override
    public void colorChanged(int color) {

        char red = (char) Color.red(color);
        char green = (char)Color.green(color);
        char blue = (char)Color.blue(color);

        savedColors[selectedColor] = new char[] { red, green, blue };
        setPreview();

    }

    public void setPreview() {
        char[] currentColor = savedColors[selectedColor];
        hueRGB.setText("R: "+Math.round((currentColor[0]/255.0)*100)
                     +"% G: "+Math.round((currentColor[1]/255.0)*100)
                     +"% B: "+Math.round((currentColor[2]/255.0)*100) + "%");
        huePane.setBackgroundColor(Color.argb(255, currentColor[0], currentColor[1], currentColor[2]));
        colorChangedListener.onColorChanged(currentColor[0], currentColor[1], currentColor[2]);
        hueName.setText(colorNameLookup.closestColor(currentColor[0],currentColor[1], currentColor[2]));

    }

}
