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

package com.projecttango.pointcloudjava;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Main Activity class for the Point Cloud Sample.  Handles the connection to the {@link Tango}
 * service and propagation of Tango XyzIj data to OpenGL and Layout views.  OpenGL rendering logic
 * is delegated to the {@link PCrenderer} class.
 */
public class JPointCloud extends Activity implements OnClickListener {
	
	private static final String TAG = JPointCloud.class.getSimpleName();
	
	private Tango mTango;
	private TangoConfig mConfig;
	
	private PCRenderer mRenderer;
	private GLSurfaceView mGLView;
	private TextView mPointCount;
	private TextView mVersion;
	private TextView mAverageZ;
	private TextView mFrequency;
	private TextView mTimeStamp;
	private Button mFirstPersonButton;
	private Button mThirdPersonButton;
	private Button mTopDownButton;
	
	
	private float mPreviousTimeStamp;
	private float mCurrentTimeStamp;
	private String mServiceVersion;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jpoint_cloud);
        
        mRenderer = new PCRenderer();
        mGLView = (GLSurfaceView)findViewById(R.id.gl_surface_view);
        mPointCount = (TextView) findViewById(R.id.vertexCount);
        mVersion=  (TextView) findViewById(R.id.version);
        mAverageZ= (TextView) findViewById(R.id.averageZ);
        mFrequency= (TextView) findViewById(R.id.frequency);
        mTimeStamp = (TextView) findViewById(R.id.timestamp);
        mFirstPersonButton = (Button) findViewById(R.id.first_person_button);
        mFirstPersonButton.setOnClickListener(this);
        mThirdPersonButton = (Button) findViewById(R.id.third_person_button);
        mThirdPersonButton.setOnClickListener(this);
        mTopDownButton = (Button) findViewById(R.id.top_down_button);
        mTopDownButton.setOnClickListener(this);
        
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);	
        
        mTango = new Tango(this);
        mConfig = new TangoConfig();
        mTango.getConfig(TangoConfig.CONFIG_TYPE_CURRENT, mConfig);
        mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        
    	// Display the version of Tango Service
        mServiceVersion = mConfig.getString("tango_service_library_version");
		mVersion.setText(mServiceVersion);

		// Configure the Tango coordinate frame pair
		ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
        		TangoPoseData.COORDINATE_FRAME_DEVICE));
		mPreviousTimeStamp = 0;
		// Listen for new Tango data
		int statusCode = mTango.connectListener(framePairs, new OnTangoUpdateListener() {
			
        	@Override
        	public void onPoseAvailable(final TangoPoseData pose) {
        		// Ignoring pose data
        		  mRenderer.getTrajectory().updateTrajectory(pose.translation);
        		  mRenderer.getModelMatCalculator().updateModelMatrix(pose.translation, pose.rotation);     
        		  mRenderer.updateViewMatrix();
        		  mGLView.requestRender();
        	}

			@Override
			public void onXyzIjAvailable(final TangoXyzIjData xyzIj) {
				mCurrentTimeStamp = (float) xyzIj.timestamp;
				final float frequency = 1/(mCurrentTimeStamp - mPreviousTimeStamp);
				mPreviousTimeStamp = mCurrentTimeStamp;
            		if(mPreviousTimeStamp==0){
            			mPreviousTimeStamp = mCurrentTimeStamp;
            		}

    				byte[] buffer = new byte[xyzIj.xyzParcelFileDescriptorSize];
                	FileInputStream fileStream = new FileInputStream(
                    xyzIj.xyzParcelFileDescriptor.getFileDescriptor());
                	try {
                		fileStream.read(buffer, xyzIj.xyzParcelFileDescriptorOffset,
                        xyzIj.xyzParcelFileDescriptorSize);
                		fileStream.close();
                		} catch (IOException e) {
                		e.printStackTrace();
                		}
                mRenderer.getPointCloud().UpdatePoints(buffer);
            	mRenderer.getPointCloud().setModelMatrix(mRenderer.getModelMatCalculator().getModelMatrix());
            	
            	// Must run UI changes on the UI thread.  Running in the Tango service thread
            	//	will result in an error.
            	runOnUiThread(new Runnable() {
            		DecimalFormat twoFormat = new DecimalFormat("0.00");
					@Override
					public void run() {
						// Display number of points in the point cloud
						mPointCount.setText(Integer.toString(xyzIj.xyzCount));
						mTimeStamp.setText(""+twoFormat.format(xyzIj.timestamp));
						mFrequency.setText(""+twoFormat.format(frequency)+"Hz");
						mAverageZ.setText(""+twoFormat.format(mRenderer.getPointCloud().getAverageZ()) + " meters");
					}
				});
            	
			}

			@Override
			public void onTangoEvent(TangoEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        });
		
		Log.i(TAG, "Status Code: " + statusCode);
   }
    
    @Override
	protected void onPause() {
		super.onPause();
		mTango.unlockConfig();
		mTango.disconnect();
	}
	
	@Override
	protected void onResume() {	
		super.onResume();
		mTango.lockConfig(mConfig);
		mTango.connect();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTango.unlockConfig();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.first_person_button:
			mRenderer.setFirstPersonView();
			break;
		case R.id.third_person_button:
			mRenderer.setThirdPersonView();
			break;
		case R.id.top_down_button:
			mRenderer.setTopDownView();
			break;
		default:
			Log.w(TAG, "Unrecognized button click.");
			return;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mRenderer.onTouchEvent(event);  
		}
}
