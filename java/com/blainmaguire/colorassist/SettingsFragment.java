
package com.blainmaguire.colorassist;
 
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import static android.R.attr.checked;

public class SettingsFragment extends Fragment {

    private CheckBox invertCheckbox;
    private RadioGroup sizeGroup;
    private int displaySize = 2;

    private Context fragmentContext;
    private CameraViewMode cameraViewMode;

    public interface OnSettingsChangedListener {
        void invertPreviewChanged(boolean invert);
        void previewSizeChanged(int size);
    }
    public OnSettingsChangedListener settingsChangedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        fragmentContext = context;
        Activity activity;

        if (context instanceof Activity){
            activity=(Activity) context;
            try {
                cameraViewMode = (CameraViewMode) activity;
                settingsChangedListener = (OnSettingsChangedListener) activity;

            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement cameraViewMode, OnSettingsChangedListener");
            }
        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && cameraViewMode!=null)
            cameraViewMode.GetCameraViewMode(CommonConstants.ViewMode.SETTINGS);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        sizeGroup = (RadioGroup) view.findViewById(R.id.sizeRadioGroup);

        sizeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.smallRadioButton:
                        displaySize = 3;
                        break;
                    case R.id.mediumRadioButton:
                        displaySize = 2;
                        break;
                    case R.id.largeRadioButton:
                        displaySize = 1;
                        break;
                }
                settingsChangedListener.previewSizeChanged(displaySize);

            }

        });


        invertCheckbox = (CheckBox) view.findViewById(R.id.invertCheckbox);
        invertCheckbox.setChecked(false);
        invertCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsChangedListener.invertPreviewChanged(isChecked);
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        builder.setMessage("About Color Assist")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        final Dialog about = new Dialog(getActivity());
        about.setContentView(R.layout.about);
        about.setTitle("About Color Assist");

        Button aboutButton = (Button)view.findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about.show();
            }
        });



        return view;
    }

}
