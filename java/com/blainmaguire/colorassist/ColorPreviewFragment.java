
package com.blainmaguire.colorassist;
 
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ColorPreviewFragment extends Fragment {

    private CameraViewMode cameraViewMode;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;

        if (context instanceof Activity){
            activity=(Activity) context;
            try {
                cameraViewMode = (CameraViewMode) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement cameraViewMode");
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.preview_fragment, container, false);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && cameraViewMode!=null)
            cameraViewMode.GetCameraViewMode(CommonConstants.ViewMode.RGBA);

    }

}
