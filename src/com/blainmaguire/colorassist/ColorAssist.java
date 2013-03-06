package com.blainmaguire.colorassist;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import com.blainmaguire.colorassist.ColorPickerDialog.OnColorChangedListener;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.app.ActionBar;
import com.blainmaguire.colorassist.R;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class ColorAssist extends SherlockFragmentActivity implements CvCameraViewListener, ActionBar.TabListener, OnColorChangedListener {
    private static final String    TAG = "OCVSample::Activity";

    private static final int VIEW_MODE_RGBA     = 0;
    private static final int VIEW_MODE_SIMILARITY = 1;
    private static final int VIEW_MODE_CHANNELS = 2;
    private static final int VIEW_MODE_SETTINGS = 3;
    
    private char[][] savedColors;
    private int selectedColor = 0;
    private char[] camColor = new char[3];
    
    private boolean redOn = true;
    private boolean greenOn = true;
    private boolean blueOn = true;
    
    private boolean invert = true;
    
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    
    private int	mViewMode;
    private int	displaySize = 2;	
    private SeekBar seekBar;
    ColorPickerDialog dialog;
    
    private Mat                    mRgba;

    private CameraBridgeViewBase   mOpenCvCameraView;
    private LinearLayout 		   mColorPickerView;
    private LinearLayout		   mChannelsView;
    private LinearLayout		   mSettingsView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("mixed_sample");

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorAssist() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        buildUI();
        
        Context context = getSupportActionBar().getThemedContext();
        dialog = new ColorPickerDialog(context, this, 0);
        
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        
        ActionBar.Tab tab1 = getSupportActionBar().newTab().setText("Pick Color");
        ActionBar.Tab tab2 = getSupportActionBar().newTab().setText("View Color");
        ActionBar.Tab tab3 = getSupportActionBar().newTab().setText("Channels");        
        ActionBar.Tab tab4 = getSupportActionBar().newTab().setText("Settings");
        
        tab1.setTabListener(this);
        getSupportActionBar().addTab(tab1);
        tab2.setTabListener(this);
        getSupportActionBar().addTab(tab2);
        tab3.setTabListener(this);
        getSupportActionBar().addTab(tab3);
        tab4.setTabListener(this);
        getSupportActionBar().addTab(tab4);
            
    }

    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public Mat onCameraFrame(Mat inputFrame) {
        final int viewMode = mViewMode;

        switch (viewMode) {
        case VIEW_MODE_RGBA:
            inputFrame.copyTo(mRgba);
            camColor = ColorPicker(mRgba.getNativeObjAddr());// savedColors[selectedColor][0]);
            break;
        case VIEW_MODE_SIMILARITY:
            inputFrame.copyTo(mRgba);
            ShowColor(mRgba.getNativeObjAddr(),
            		  savedColors[selectedColor][0], savedColors[selectedColor][1], savedColors[selectedColor][2],
            		  displaySize, invert);
            break;
        case VIEW_MODE_CHANNELS:
        	inputFrame.copyTo(mRgba);
        	Channels(mRgba.getNativeObjAddr(), redOn, greenOn, blueOn, displaySize, invert);
        	break;
        case VIEW_MODE_SETTINGS:
        	mRgba = Mat.zeros(mRgba.rows(), mRgba.cols(), mRgba.type());
        	break;
        }

        return mRgba;
    }


	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (tab.getPosition() == 0 ) {
			mViewMode = VIEW_MODE_RGBA;
			mColorPickerView.setVisibility(View.VISIBLE);
			mChannelsView.setVisibility(View.GONE);
			mSettingsView.setVisibility(View.GONE);
		}
		else if(tab.getPosition() == 1) {
			mViewMode = VIEW_MODE_SIMILARITY;
			mColorPickerView.setVisibility(View.GONE);
			mChannelsView.setVisibility(View.GONE);
			mSettingsView.setVisibility(View.GONE);
		}
		else if (tab.getPosition() == 2 ) {
			mViewMode = VIEW_MODE_CHANNELS;
			mColorPickerView.setVisibility(View.GONE);
			mChannelsView.setVisibility(View.VISIBLE);
			mSettingsView.setVisibility(View.GONE);
		}
		else if (tab.getPosition() == 3 ) {
			mViewMode = VIEW_MODE_SETTINGS;
			mColorPickerView.setVisibility(View.GONE);
			mChannelsView.setVisibility(View.GONE);
			mSettingsView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}
	
    public native char RGB2Hue(char r, char g, char b);
    public native void ShowColor(long matAddrRgba, char r, char g, char b, int size, boolean invert);
    public native char[] ColorPicker(long matAddrRgba);
    public native void Channels(long matAddrRgba, boolean red, boolean green, boolean blue, int size, boolean invert);

	@Override
	public void colorChanged(int color) {
		
        char red = (char)Color.red(color);
        char green = (char)Color.green(color);
        char blue = (char)Color.blue(color);
        
        savedColors[selectedColor] = new char[] { red, green, blue };
        setPreview(savedColors[selectedColor]);
		
	}
	
	public void setPreview(char[] color) {
	
    	//View parent = (View) button.getParent().getParent();
    	View huePane = mColorPickerView.findViewWithTag(Integer.valueOf(10));
    	huePane.setBackgroundColor(Color.argb(255, color[0], color[1], color[2]));
    	
    	TextView hueLabel = (TextView) mColorPickerView.findViewWithTag(Integer.valueOf(11));
    	char hue = RGB2Hue(color[0], color[1], color[2]);
    	hueLabel.setText("Hue: " +(int)hue+
    			         " R: "+  (int)color[0]+
    			         " G: "+  (int)color[1]+
    			         " B: "+  (int)color[2]);
		
	}
	
	public void buildUI() {
		
		/* This could be done a lot better. Namely using Fragments and more XML.
		 * I gave myself a personal deadline of two weeks to publish on the play store
		 * (otherwise it risked becoming one of those half finished side projects that
		 * never get finished).
		 * 
		 * My previous experience with UI in Java has been Swing. I realize the below code
		 * isn't exactly following android guidelines but I felt shipping a working app was
		 * more important. This is also my first android app.
		 */
		
        setContentView(R.layout.assist_surface_view);
		LinearLayout pickGroup = new LinearLayout(getApplicationContext());
		pickGroup.setGravity(Gravity.CENTER_HORIZONTAL);
        pickGroup.setOrientation(LinearLayout.HORIZONTAL);
	
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.assist_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setZOrderMediaOverlay(false);
        
        mColorPickerView = new LinearLayout(getApplicationContext());
        mColorPickerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        mColorPickerView.setGravity(Gravity.CENTER_VERTICAL);
        mColorPickerView.setOrientation(LinearLayout.VERTICAL);
        
		LinearLayout options = new LinearLayout(getApplicationContext());
		options.setGravity(Gravity.CENTER_HORIZONTAL);
        options.setOrientation(LinearLayout.HORIZONTAL);
		
		Button useColor = new Button(getApplicationContext());
		useColor.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		useColor.setText("From Camera");
		useColor.setGravity(Gravity.CENTER_HORIZONTAL);
		useColor.setOnClickListener(new OnClickListener() {

		    public void onClick(View button) {
		    	savedColors[selectedColor] = camColor;
		    	setPreview(camColor);
		    }

		});
        
        Button setColor = new Button(getApplicationContext());
        setColor.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        setColor.setText("Set Custom");
        setColor.setGravity(Gravity.CENTER_HORIZONTAL);
        
        setColor.setOnClickListener(new OnClickListener() {
		    public void onClick(View button) {
		    	dialog.show();
		    }

		});
        
        LinearLayout previewOptions = new LinearLayout(getApplicationContext());
        previewOptions.setGravity(Gravity.CENTER_HORIZONTAL);
        previewOptions.setOrientation(LinearLayout.HORIZONTAL);
        
        TextView colorPreview = new TextView(getApplicationContext());
        colorPreview.setBackgroundColor(Color.argb(255, 255, 255, 255));
        colorPreview.setLayoutParams(new LayoutParams(32,32));
        colorPreview.setGravity(Gravity.CENTER_HORIZONTAL);
        colorPreview.setTag(Integer.valueOf(10));
        previewOptions.addView(colorPreview);
        
        TextView blank = new TextView(getApplicationContext());
        blank.setBackgroundColor(Color.argb(255, 255, 255, 0));
        blank.setLayoutParams(new LayoutParams(32,32));
        blank.setGravity(Gravity.CENTER_HORIZONTAL);
        blank.setVisibility(View.INVISIBLE);
        
        TextView blank2 = new TextView(getApplicationContext());
        blank.setBackgroundColor(Color.argb(255, 255, 255, 0));
        blank2.setGravity(Gravity.CENTER_HORIZONTAL);
        blank2.setVisibility(View.INVISIBLE);
        
        TextView blank4 = new TextView(getApplicationContext());
        blank.setBackgroundColor(Color.argb(255, 255, 255, 0));
        blank4.setGravity(Gravity.CENTER_HORIZONTAL);
        blank4.setVisibility(View.INVISIBLE);
        
        TextView hueLabel = new TextView(getApplication());
        hueLabel.setText("Hue: 0 Red: 255 Green: 255 Blue: 255");
        hueLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        hueLabel.setPadding(0, 32, 0, 0);
		hueLabel.setTag(Integer.valueOf(11));
        
        pickGroup.addView(useColor);
		pickGroup.addView(setColor);
		options.addView(pickGroup);

		mColorPickerView.addView(blank);
        mColorPickerView.addView(hueLabel);
		mColorPickerView.addView(options);
		mColorPickerView.addView(previewOptions);
		mColorPickerView.addView(blank2);
        mColorPickerView.addView(blank4);
		
        LinearLayout colorOptions = new LinearLayout(getApplicationContext());
        colorOptions.setGravity(Gravity.CENTER_HORIZONTAL);
        colorOptions.setOrientation(LinearLayout.VERTICAL);
        
        int colorNumber = 0;
        int row = 3;
        int col = 3;
        savedColors = new char[row*col][3];
		for (; row > 0; row--) {
			LinearLayout buttons = new LinearLayout(getApplicationContext());
	        buttons.setOrientation(LinearLayout.HORIZONTAL);
			buttons.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			buttons.setGravity(Gravity.CENTER_HORIZONTAL);

	        for (col=3; col > 0; col--) {
		        Button button = new Button(getApplicationContext());
		        button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
				button.setText("Color "+(colorNumber+1));
				button.setGravity(Gravity.CENTER_HORIZONTAL);
				button.setId(colorNumber);
				button.setTag(Integer.valueOf(colorNumber));
		        savedColors[colorNumber] = new char[] {255, 255, 255};
				buttons.addView(button);
				button.setOnTouchListener(new OnTouchListener() {
		            @Override
				    public boolean onTouch(View button, MotionEvent event) {
				    	if (event.getAction()==MotionEvent.ACTION_DOWN) {
					    	View parent = (View) button.getParent().getParent();
					    	for (int i=0; i<9; i++) {
						    	View btn = parent.findViewWithTag(Integer.valueOf(i));
						    	btn.setPressed(false);
					    	}
				    		selectedColor = button.getId();
				    		setPreview(savedColors[selectedColor]);
					    	button.setPressed(true);
				    	}
				    	return true;
				    }

				});
				colorNumber++;
	        }
	        colorOptions.addView(buttons);
	        
        }
		colorOptions.findViewWithTag(Integer.valueOf(0)).setPressed(true);
        mColorPickerView.addView(colorOptions);
        addContentView(mColorPickerView, mColorPickerView.getLayoutParams());
        
        mChannelsView = new LinearLayout(getApplicationContext());
        mChannelsView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        mChannelsView.setGravity(Gravity.BOTTOM);
        mChannelsView.setOrientation(LinearLayout.HORIZONTAL);
        
        CheckBox redChannel = new CheckBox(getApplicationContext());
        redChannel.setChecked(true);
        redChannel.setText("Red");
        redChannel.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				redOn = arg1;
				arg0.setChecked(arg1);
			}
 
        });
        CheckBox greenChannel = new CheckBox(getApplicationContext());
        greenChannel.setChecked(true);
        greenChannel.setText("Green");
        greenChannel.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				greenOn = arg1;
				arg0.setChecked(arg1);
			}
 
        });
        CheckBox blueChannel = new CheckBox(getApplicationContext());
        blueChannel.setChecked(true);
        blueChannel.setText("Blue");
        blueChannel.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				blueOn = arg1;
				arg0.setChecked(arg1);
			}
 
        });
        mChannelsView.addView(redChannel);
        mChannelsView.addView(greenChannel);
        mChannelsView.addView(blueChannel);
        mChannelsView.setVisibility(View.GONE);
        
        addContentView(mChannelsView, mChannelsView.getLayoutParams());
        
        mSettingsView = new LinearLayout(getApplicationContext());
        mSettingsView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        mSettingsView.setGravity(Gravity.CENTER);
        mSettingsView.setOrientation(LinearLayout.VERTICAL);
        
        TextView sizeLabel = new TextView(getApplicationContext());
        sizeLabel.setText("Preview Size:");
        mSettingsView.addView(sizeLabel);
        
        RadioGroup sizeGroup= new RadioGroup(getApplicationContext());
        sizeGroup.setLayoutParams(new RadioGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        sizeGroup.setOrientation(LinearLayout.HORIZONTAL);
        OnClickListener radioListener = new OnClickListener(){

			@Override
			public void onClick(View v) {
	    	    boolean checked = ((RadioButton) v).isChecked();
	    	    
	    	    switch(v.getId()) {
	    	        case 12:
	    	            if (checked)
	    	                displaySize = 3;
	    	            break;
	    	        case 13:
	    	            if (checked)
	    	                displaySize = 2;
	    	            break;
	    	        case 14:
	    	            if (checked)
	    	                displaySize = 1;
	    	            break;
	    	    }
				
			}

    	};
        RadioButton btn1 = new RadioButton(getApplicationContext());
        btn1.setOnClickListener(radioListener);
        btn1.setId(12);
        btn1.setText("Small");
        
        RadioButton btn2 = new RadioButton(getApplicationContext());
        btn2.setOnClickListener(radioListener);
        btn2.setId(13);
        btn2.setText("Medium");
        btn2.setChecked(true);
        
        RadioButton btn3 = new RadioButton(getApplicationContext());
        btn3.setOnClickListener(radioListener);
        btn3.setId(14);
        btn3.setText("Large");
        
        
        sizeGroup.addView(btn1);
        sizeGroup.addView(btn2);
        sizeGroup.addView(btn3);
        mSettingsView.addView(sizeGroup);
        
        CheckBox inverted = new CheckBox(getApplicationContext());
        inverted.setChecked(true);
        inverted.setText("Invert Preview ");
        inverted.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				invert = arg1;
				arg0.setChecked(arg1);
			}
 
        });
        mSettingsView.addView(inverted);
        mSettingsView.setVisibility(View.GONE);
        addContentView(mSettingsView, mSettingsView.getLayoutParams());
	
	}
}