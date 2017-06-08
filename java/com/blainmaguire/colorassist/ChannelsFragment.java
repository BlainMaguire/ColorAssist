
package com.blainmaguire.colorassist;
import com.blainmaguire.colorassist.CommonConstants.ViewMode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class ChannelsFragment extends Fragment {

    boolean[] channelsOn = new boolean[]{true, true, true};

    private CheckBox redCheckbox;
    private CheckBox blueCheckbox;
    private CheckBox greenCheckbox;

    public interface OnChannelsChangedListener {
        void channelsChanged(boolean red, boolean green, boolean blue);
    }

    private CameraViewMode cameraViewMode;

    private OnChannelsChangedListener channelsChangedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;

        if (context instanceof Activity){
            activity=(Activity) context;
            try {
                cameraViewMode = (CameraViewMode) activity;
                channelsChangedListener = (OnChannelsChangedListener) activity;


            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement cameraViewMode, OnChannelsChangedListener");
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.channels_fragment, container, false);

        CheckBox[] checkBoxes = new CheckBox[]{
                (CheckBox)view.findViewById(R.id.recChannel),
                (CheckBox)view.findViewById(R.id.greenChannel),
                (CheckBox)view.findViewById(R.id.blueChannel)
        };

        for (int i=0; i < checkBoxes.length; i++) {
            final int finalI = i;
            checkBoxes[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    channelsOn[finalI] = !channelsOn[finalI];
                    channelsChangedListener.channelsChanged(channelsOn[0], channelsOn[1], channelsOn[2]);
                }
            });
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && cameraViewMode!=null)
            cameraViewMode.GetCameraViewMode(ViewMode.CHANNELS);

    }


}
