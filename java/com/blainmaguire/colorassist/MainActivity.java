package com.blainmaguire.colorassist;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.blainmaguire.colorassist.CommonConstants.ViewMode;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraViewMode, ColorPickerDialog.OnColorChangedListener, ColorPickerFragment.onColorChangedListener, CameraBridgeViewBase.CvCameraViewListener, ChannelsFragment.OnChannelsChangedListener, SettingsFragment.OnSettingsChangedListener {

    private static final String TAG = "ColorAssist::Activity";

    private ViewMode currentViewMode = ViewMode.SIMILARITY;
    
    private char[] camColor = new char[]{255, 255, 0};
    private char[] currColor = new char[3];

    private boolean redOn = true;
    private boolean greenOn = true;
    private boolean blueOn = true;

    private boolean invert = true;

    private int	displaySize = 2;
    ColorPickerDialog dialog;

    private Mat                    mRgba;

    private CameraBridgeViewBase   mOpenCvCameraView;
    private SampleFragmentPagerAdapter fragmentPagerAdapter;

    private BaseLoaderCallback _baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load ndk built module, as specified in moduleName in build.gradle
                    // after opencv initialization
                    System.loadLibrary("native-lib");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        dialog = new ColorPickerDialog(getApplicationContext(), this, 0);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.assist_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setZOrderMediaOverlay(false);


        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        fragmentPagerAdapter = new SampleFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this);
        viewPager.setAdapter(fragmentPagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, _baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            _baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public native char RGB2Hue(char r, char g, char b);
    public native void ShowColor(long matAddrRgba, char r, char g, char b, int size, boolean invert);
    public native char[] ColorPicker(long matAddrRgba);
    public native void Channels(long matAddrRgba, boolean red, boolean green, boolean blue, int size, boolean invert);

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(Mat inputFrame) {


        switch (currentViewMode) {
            case RGBA:
                inputFrame.copyTo(mRgba);
                ShowColor(mRgba.getNativeObjAddr(),
                        currColor[0], currColor[1], currColor[2],
                        displaySize, invert);
                break;
            case SIMILARITY:
                inputFrame.copyTo(mRgba);
                camColor = ColorPicker(mRgba.getNativeObjAddr());
                break;
            case CHANNELS:
                inputFrame.copyTo(mRgba);
                Channels(mRgba.getNativeObjAddr(), redOn, greenOn, blueOn, displaySize, invert);
                break;
            default:
                inputFrame.copyTo(mRgba);
                break;
        }
        return mRgba;
    }

    @Override
    public void colorChanged(int color) {

        char red = (char)Color.red(color);
        char green = (char)Color.green(color);
        char blue = (char)Color.blue(color);

    }

    @Override
    public void GetCameraViewMode(ViewMode viewMode) {
        currentViewMode = viewMode;
    }

    @Override
    public void onColorChanged(char r, char g, char b) {
        currColor[0] = r;
        currColor[1] = g;
        currColor[2] = b;
    }

    @Override
    public char[] getColorFromCamera() {
        return camColor;
    }

    @Override
    public void channelsChanged(boolean red, boolean green, boolean blue) {
        redOn = red;
        greenOn = green;
        blueOn = blue;
    }

    @Override
    public void invertPreviewChanged(boolean isChecked) {
        invert = !isChecked;
    }

    @Override
    public void previewSizeChanged(int size) {
        displaySize = size;
    }
}