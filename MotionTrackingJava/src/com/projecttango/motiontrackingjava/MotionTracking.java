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

package com.projecttango.motiontrackingjava;

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

/**
 * Main Activity class for the Motion Tracking API Sample. Handles the
 * connection to the Tango service and propagation of Tango pose data to OpenGL
 * and Layout views. OpenGL rendering logic is delegated to the
 * {@link MTGLRenderer} class.
 */
public class MotionTracking extends Activity implements View.OnClickListener {

	private static String TAG = MotionTracking.class.getSimpleName();

	private Tango mTango;
	private TangoConfig mConfig;
	private TextView mDelta;
	private TextView mPoseCount;
	private TextView mPose;
	private TextView mQuat;
	private TextView mPoseStatus;
	private TextView mVersion;
	private TextView mTangoEvent;
	
	private Button mMotionReset;
	private float mPreviousTimeStamp;
	private int count;
	private float mDeltaTime;
	private boolean mIsAutoReset;
	private MTGLRenderer mRenderer;
	private GLSurfaceView mGLView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_motion_tracking);
		Intent intent = getIntent();
		mIsAutoReset = intent.getBooleanExtra(StartActivity.KEY_MOTIONTRACKING_AUTORESET, false);
		// Text views for displaying translation and rotation data
		mPose = (TextView) findViewById(R.id.pose);
		mQuat = (TextView) findViewById(R.id.quat);
		mPoseCount =(TextView) findViewById(R.id.posecount);
		mDelta =(TextView) findViewById(R.id.deltatime);
		mTangoEvent =(TextView) findViewById(R.id.tangoevent);
		// Buttons for selecting camera view and Set up button click listeners
		findViewById(R.id.first_person_button).setOnClickListener(this);
		findViewById(R.id.third_person_button).setOnClickListener(this);
		findViewById(R.id.top_down_button).setOnClickListener(this);
		
		// Button to reset motion tracking
		mMotionReset = (Button) findViewById(R.id.resetmotion);
		
		// Text views for the status of the pose data and Tango library versions
		mPoseStatus = (TextView) findViewById(R.id.status);
		mVersion = (TextView) findViewById(R.id.version);
		
		// OpenGL view where all of the graphics are drawn
		mGLView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

		// Set up button click listeners
		mMotionReset.setOnClickListener(this);

		// Configure OpenGL renderer
		mRenderer = new MTGLRenderer();
		mGLView.setEGLContextClientVersion(2);
		mGLView.setRenderer(mRenderer);
		mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		mMotionReset.setVisibility(View.GONE);
		// Instantiate the Tango service
		mTango = new Tango(this);
		startMotionTracking();
	}

	/**
	 * Set up the TangoConfig and the listeners for the Tango service, then begin using the
	 * Motion Tracking API.  This is called in response to the user clicking the 'Start' Button.
	 */
	private void startMotionTracking() {
		// Create a new Tango Configuration and enable the MotionTracking API
		mConfig = new TangoConfig();
		mTango.getConfig(TangoConfig.CONFIG_TYPE_CURRENT, mConfig);
		mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);

		// The Auto-Reset ToggleButton sets a boolean variable to determine if the
		//	Tango service should automatically reset when MotionTracking enters an
		//	invalid state.
		if (mIsAutoReset) {
			mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORESET, true);
			mMotionReset.setVisibility(View.GONE);
		} else {
			mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORESET, false);
			mMotionReset.setVisibility(View.VISIBLE);
		}
		
		// Display the library version for debug purposes
		mVersion.setText(mConfig.getString("tango_service_library_version"));

		// Lock configuration and connect to Tango
		mTango.lockConfig(mConfig);
		mTango.connect();
		
		// Select coordinate frame pairs
		final ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
		framePairs.add(new TangoCoordinateFramePair(
				TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
				TangoPoseData.COORDINATE_FRAME_DEVICE));
		count=0;
		mPreviousTimeStamp=0;
		// Listen for new Tango data
		int statusCode = mTango.connectListener(framePairs, new OnTangoUpdateListener() {

					@Override
					public void onPoseAvailable(final TangoPoseData pose) {
						// Log whenever Motion Tracking enters an invalid state
						if (!mIsAutoReset && (pose.statusCode == TangoPoseData.POSE_INVALID)) {
							Log.w(TAG, "Invalid State");
						}
						 mDeltaTime = (float) (pose.timestamp - mPreviousTimeStamp);
						 mPreviousTimeStamp = (float) pose.timestamp;
						Log.i(TAG,"Delta Time is: "+mDeltaTime);
						count++;
						// Update the OpenGL renderable objects with the new Tango Pose data
						mRenderer.getTrajectory().updateTrajectory(pose.translation);
						mRenderer.getModelMatCalculator().updateModelMatrix(
								pose.translation, pose.rotation);
						mRenderer.updateViewMatrix();
						mGLView.requestRender();
						
						// Update the UI with TangoPose information
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								DecimalFormat threeDec = new DecimalFormat("0.000");
								String translationString = "[" + threeDec.format(pose.translation[0]) + ","
										+ threeDec.format(pose.translation[1]) + ","
										+ threeDec.format(pose.translation[2]) + "] ";
								String quaternionString = "[" + threeDec.format(pose.rotation[0]) + ","
										+ threeDec.format(pose.rotation[1]) + ","
										+ threeDec.format(pose.rotation[2]) + ","
										+ threeDec.format(pose.rotation[2]) +"] ";

								// Display pose data on screen in TextViews
								mPose.setText(translationString);
								mQuat.setText(quaternionString);
								mPoseCount.setText(Integer.toString(count));
								mDelta.setText(threeDec.format(mDeltaTime));
								if (pose.statusCode == TangoPoseData.POSE_VALID) {
									mPoseStatus.setText("Valid");
								} else if (pose.statusCode == TangoPoseData.POSE_INVALID) {
									mPoseStatus.setText("Invalid");
								} else if (pose.statusCode == TangoPoseData.POSE_INITIALIZING) {
									mPoseStatus.setText("Initializing");
								} else if (pose.statusCode == TangoPoseData.POSE_UNKNOWN) {
									mPoseStatus.setText("Unknown");
								}
							}

	
						});
					}

					@Override
					public void onXyzIjAvailable(TangoXyzIjData arg0) {
						// We are not using TangoXyzIjData for this application
					}

					@Override
					public void onTangoEvent(final TangoEvent event) {
						runOnUiThread(new Runnable(){

							@Override
							public void run() {
								mTangoEvent.setText(event.description);
							}
							
						});

					}
				});

		// Log status code for debug purposes
		Log.i(TAG, "Status: " + statusCode);
	}

	private void motionReset() {
		mTango.resetMotionTracking();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mTango.unlockConfig();
		mTango.disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTango.unlockConfig();
	}

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
		case R.id.resetmotion:
			motionReset();
			break;
		default:
			Log.w(TAG, "Unknown button click");
			return;
		}
	}
}
