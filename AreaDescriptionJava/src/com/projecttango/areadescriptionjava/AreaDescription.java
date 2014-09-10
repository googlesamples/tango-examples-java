/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projecttango.areadescriptionjava;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoCoordinateFramePair;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main Activity class for the Area Learning API Sample. Handles the
 * connection to the Tango service and propagation of Tango pose data to OpenGL
 * and Layout views. OpenGL rendering logic is delegated to the
 * {@link ADRenderer} class.
 */
public class AreaDescription extends Activity implements View.OnClickListener {

	private static final String TAG = AreaDescription.class.getSimpleName();
	private static final int SECONDS_TO_MILLI = 1000;
	private Tango mTango;
	private TangoConfig mConfig;
	
	private TextView mTangoEventTextView;
	private TextView mStart2DeviceTranslationTextView;
	private TextView mAdf2DeviceTranslationTextView;
	private TextView mAdf2StartTranslationTextView;
	private TextView mStart2DeviceQuatTextView;
	private TextView mAdf2DeviceQuatTextView;
	private TextView mAdf2StartQuatTextView;
	private TextView mVersionTextView;
	private TextView mUUIDTextView;
	private TextView mStart2DevicePoseStatusTextView;
	private TextView mAdf2DevicePoseStatusTextView;
	private TextView mAdf2StartPoseStatusTextView;
	private TextView mStart2DevicePoseCountTextView;
	private TextView mAdf2DevicePoseCountTextView;
	private TextView mAdf2StartPoseCountTextView;
	private TextView mStart2DevicePoseDeltaTextView;
	private TextView mAdf2DevicePoseDeltaTextView;
	private TextView mAdf2StartPoseDeltaTextView;
	
	private Button mSaveAdf;
	private Button mFirstPersonButton;
	private Button mThirdPersonButton;
	private Button mTopDownButton;
	
	private int mStart2DevicePoseCount;
	private int mAdf2DevicePoseCount;
	private int mAdf2StartPoseCount;
    
	private double mStart2DevicePoseDelta;
	private double mAdf2DevicePoseDelta;
	private double mAdf2StartPoseDelta;
	private double mStart2DevicePreviousPoseTimeStamp;
	private double mAdf2DevicePreviousPoseTimeStamp;
	private double mAdf2StartPreviousPoseTimeStamp;
	
	private boolean mIsRelocalized;
	private boolean mIsLearningMode;
	private boolean mIsConstantSpaceRelocalize;
	
	private ADRenderer mRenderer;
	private GLSurfaceView mGLView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_area_learning);
		
		mTangoEventTextView = (TextView) findViewById(R.id.tangoevent);
		
		mAdf2DeviceTranslationTextView = (TextView) findViewById(R.id.adf2devicePose);
		mStart2DeviceTranslationTextView = (TextView) findViewById(R.id.start2devicePose);
		mAdf2StartTranslationTextView = (TextView) findViewById(R.id.adf2startPose);
		mAdf2DeviceQuatTextView = (TextView) findViewById(R.id.adf2deviceQuat);
		mStart2DeviceQuatTextView = (TextView) findViewById(R.id.start2deviceQuat);
		mAdf2StartQuatTextView = (TextView) findViewById(R.id.adf2startQuat);
		
		mAdf2DevicePoseStatusTextView = (TextView) findViewById(R.id.adf2deviceStatus);
		mStart2DevicePoseStatusTextView = (TextView) findViewById(R.id.start2deviceStatus);
		mAdf2StartPoseStatusTextView = (TextView) findViewById(R.id.adf2startStatus);
		
		mAdf2DevicePoseCountTextView = (TextView) findViewById(R.id.adf2devicePosecount);
		mStart2DevicePoseCountTextView = (TextView) findViewById(R.id.start2devicePosecount);
		mAdf2StartPoseCountTextView = (TextView) findViewById(R.id.adf2startPosecount);
		
		mAdf2DevicePoseDeltaTextView = (TextView) findViewById(R.id.adf2deviceDeltatime);
		mStart2DevicePoseDeltaTextView = (TextView) findViewById(R.id.start2deviceDeltatime);
		mAdf2StartPoseDeltaTextView = (TextView) findViewById(R.id.adf2startDeltatime);
		
		mFirstPersonButton = (Button) findViewById(R.id.first_person_button);
		mThirdPersonButton = (Button) findViewById(R.id.third_person_button);
		mTopDownButton = (Button) findViewById(R.id.top_down_button);
		
		mVersionTextView = (TextView) findViewById(R.id.version);
		mGLView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

		mSaveAdf = (Button) findViewById(R.id.saveAdf);
		mUUIDTextView = (TextView) findViewById(R.id.uuid);

	
	
		mSaveAdf.setVisibility(View.GONE); 
		// Set up button click listeners
		mFirstPersonButton.setOnClickListener(this);
		mThirdPersonButton.setOnClickListener(this);
		mTopDownButton.setOnClickListener(this);
		
		// Configure OpenGL renderer
		mRenderer = new ADRenderer();
		mGLView.setEGLContextClientVersion(2);
		mGLView.setRenderer(mRenderer);
		mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		// Instantiate the Tango service
		mTango = new Tango(this);
		mIsRelocalized = false;
		
		Intent intent = getIntent();
		mIsLearningMode = intent.getBooleanExtra(ADStartActivity.USE_AREA_LEARNING, false);
		mIsConstantSpaceRelocalize = intent.getBooleanExtra(ADStartActivity.LOAD_ADF, false);
		Log.i("Area Description and Load Adf",	""+mIsLearningMode +" " + mIsConstantSpaceRelocalize);
		setTangoConfig();
	}

	private void setTangoConfig() {
		mConfig = new TangoConfig();
		mTango.getConfig(TangoConfig.CONFIG_TYPE_CURRENT, mConfig);
		// Check if learning mode
		if (mIsLearningMode) { 
			//Set learning mode to config.
			mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true); 
			// Set the ADF save button visible.
			mSaveAdf.setVisibility(View.VISIBLE); 
			mSaveAdf.setOnClickListener(this);

		}
		// Check for Load ADF/Constant Space relocalization mode
		if (mIsConstantSpaceRelocalize) { 
			ArrayList<String> fullUUIDList = new ArrayList<String>();  
			// Returns a list of ADFs with their UUIDs
			mTango.listAreaDescriptions(fullUUIDList); 
			if (fullUUIDList.size() == 0) {
				mUUIDTextView.setText("No UUIDs");
			}
			
			// Load the latest ADF if ADFs are found.
			if (fullUUIDList.size() > 0) {
				mConfig.putString(TangoConfig.KEY_STRING_AREADESCRIPTION,
						fullUUIDList.get(fullUUIDList.size() - 1));
				mUUIDTextView.setText("No of UUIDs : " + fullUUIDList.size()
						+ ", Latest is :"
						+ fullUUIDList.get(fullUUIDList.size() - 1));
			}
		}
		
		//Set the number of loop closures to zero at start.
		mStart2DevicePoseCount = 0;
		mAdf2DevicePoseCount = 0;
		mAdf2StartPoseCount = 0;
		
		//Lock config
		mTango.lockConfig(mConfig);
		mVersionTextView.setText(mConfig.getString("tango_service_library_version"));
		
		setUpTangoListeners();
		mTango.connect();
	}


	private void setUpTangoListeners() {
		
		// Set Tango Listeners for Poses Device wrt Start of Service, Device wrt ADF and Start of Service wrt ADF
		ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
		framePairs.add(new TangoCoordinateFramePair(
				TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
				TangoPoseData.COORDINATE_FRAME_DEVICE));
		framePairs.add(new TangoCoordinateFramePair(
				TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
				TangoPoseData.COORDINATE_FRAME_DEVICE));
		framePairs.add(new TangoCoordinateFramePair(
				TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
				TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));
		
		mTango.connectListener(framePairs, new OnTangoUpdateListener() {
			@Override
			public void onXyzIjAvailable(TangoXyzIjData xyzij) {
				// Not using XyzIj data for this sample
			}
			
			// Listen to Tango Events for Relocalization 
			@Override
			public void onTangoEvent(final TangoEvent event) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mTangoEventTextView.setText(event.description);
					}
				});
			}

			@Override
			public void onPoseAvailable(TangoPoseData pose) {
				
				//Update the text views with Pose info.
				updateTextViewWith(pose);
				boolean updateRenderer = false;
				if (mIsRelocalized) {
					if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
							&& pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
						updateRenderer = true;
					}
				} else {
					if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE
							&& pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
						updateRenderer = true;
					}
				}
				
				// Update the trajectory, model matrix, and view matrix, then render the scene again
				if (updateRenderer) {
					mRenderer.getTrajectory().updateTrajectory(pose.translation);
					mRenderer.getModelMatCalculator().updateModelMatrix(
							pose.translation, pose.rotation);
					mRenderer.updateViewMatrix();
					mGLView.requestRender();
				}
			}
		});
	}

	private void saveAdf() {
		ArrayList<String> uuids = new ArrayList<String>();
		mTango.saveAreaDescription(uuids);
		Toast.makeText(getApplicationContext(), "Adf saved with UUID: "+uuids.get(0), 3).show();
	}

	/**
	 * Updates the text view in UI screen with the Pose. Each pose is associated with 
	 * Target and Base Frame. We need to check for that pair ad update our views accordingly.
	 * @param pose
	 */
	private void updateTextViewWith(final TangoPoseData pose) {
		final DecimalFormat twoDec = new DecimalFormat("0.00");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				DecimalFormat twoDec = new DecimalFormat("0.00");
				String translationString = "[" + twoDec.format(pose.translation[0]) + ","
						+ twoDec.format(pose.translation[1]) + ","
						+ twoDec.format(pose.translation[2]) + "] ";
				
				String quaternionString = "[" + twoDec.format(pose.rotation[0]) + ","
						+ twoDec.format(pose.rotation[1]) + ","
						+ twoDec.format(pose.rotation[2]) + ","
						+ twoDec.format(pose.rotation[3]) +"] ";
				
				if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
						&& pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
					mAdf2DevicePoseCount++;
					mAdf2DevicePoseDelta = (pose.timestamp - mAdf2DevicePreviousPoseTimeStamp) * SECONDS_TO_MILLI;
					mAdf2DevicePreviousPoseTimeStamp = pose.timestamp;
					mAdf2DeviceTranslationTextView.setText(translationString);
					mAdf2DeviceQuatTextView.setText(quaternionString);
					mAdf2DevicePoseStatusTextView.setText(getPoseStatus(pose));
					mAdf2DevicePoseCountTextView.setText(Integer.toString(mAdf2DevicePoseCount));
					mAdf2DevicePoseDeltaTextView.setText(twoDec.format(mAdf2DevicePoseDelta));
				}

				if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE
						&& pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
					mStart2DevicePoseCount++;
					mStart2DevicePoseDelta = (pose.timestamp - mStart2DevicePreviousPoseTimeStamp)* SECONDS_TO_MILLI;
					mStart2DevicePreviousPoseTimeStamp = pose.timestamp;
					mStart2DeviceTranslationTextView.setText(translationString);
					mStart2DeviceQuatTextView.setText(quaternionString);
					mStart2DevicePoseStatusTextView.setText(getPoseStatus(pose));
					mStart2DevicePoseCountTextView.setText(Integer.toString(mStart2DevicePoseCount));
					mStart2DevicePoseDeltaTextView.setText(twoDec.format(mStart2DevicePoseDelta));
					if(pose.statusCode == TangoPoseData.POSE_INVALID){
						mIsRelocalized = true;
					}
					else{
						mIsRelocalized = false;
					}
				}
				
				if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
						&& pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE) {
					mAdf2StartPoseCount++;
					mAdf2StartPoseDelta = (pose.timestamp - mAdf2StartPreviousPoseTimeStamp)* SECONDS_TO_MILLI;
					mAdf2StartPreviousPoseTimeStamp = pose.timestamp;
					mAdf2StartTranslationTextView.setText(translationString);
					mAdf2StartQuatTextView.setText(quaternionString);
					mAdf2StartPoseStatusTextView.setText(getPoseStatus(pose));
					mAdf2StartPoseCountTextView.setText(Integer.toString(mAdf2StartPoseCount));
					mAdf2StartPoseDeltaTextView.setText(twoDec.format(mAdf2StartPoseDelta));
				}
			}

		});

	}

	private String getPoseStatus(TangoPoseData pose) {
		switch (pose.statusCode) {
		case TangoPoseData.POSE_INITIALIZING:
			return "Initializing";
		case TangoPoseData.POSE_INVALID:
			return "Invalid";
		case TangoPoseData.POSE_VALID:
			return "Valid";
		default:
			return "Unknown";
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	// OnClick Button Listener for all the buttons
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.first_person_button:
			mRenderer.setFirstPersonView();
			break;
		case R.id.top_down_button:
			mRenderer.setTopDownView();
			break;
		case R.id.third_person_button:
			mRenderer.setThirdPersonView();
			break;
		case R.id.saveAdf:
			saveAdf();
			break;
		default:
			Log.w(TAG, "Unknown button click");
			return;
		}
	}

}
