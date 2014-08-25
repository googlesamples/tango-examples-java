package com.projecttango.jmotiontrackingsample;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.TextView;

public class MotionTracking extends Activity {

	private TextView poseX;
	private TextView poseY;
	private TextView poseZ;
	private TextView poseQuaternion0;
	private TextView poseQuaternion1;
	private TextView poseQuaternion2;
	private TextView poseQuaternion3;
	public MTGLRenderer mRenderer;
	public GLSurfaceView mGLView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_motion_tracking);

		poseX = (TextView) findViewById(R.id.poseX);
		poseY = (TextView) findViewById(R.id.poseY);
		poseZ = (TextView) findViewById(R.id.poseZ);
		poseQuaternion0 = (TextView) findViewById(R.id.Quaternion1);
		poseQuaternion1 = (TextView) findViewById(R.id.Quaternion2);
		poseQuaternion2 = (TextView) findViewById(R.id.Quaternion3);
		poseQuaternion3 = (TextView) findViewById(R.id.Quaternion4);
		mGLView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

		mRenderer = new MTGLRenderer();
		mGLView.setEGLContextClientVersion(2);
		mGLView.setRenderer(mRenderer);
		mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	@Override
	protected void onResume()
	{	
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
}
