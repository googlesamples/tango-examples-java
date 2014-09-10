package com.projecttango.motiontrackingjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class StartActivity extends Activity implements View.OnClickListener{
	public static final String KEY_MOTIONTRACKING_AUTORESET = "com.projecttango.motiontrackingjava.useautoreset";
	private ToggleButton mAutoResetButton;
	private Button mStartButton;
	private boolean mUseAutoReset;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		mAutoResetButton = (ToggleButton) findViewById(R.id.autoresetbutton);
		mStartButton = (Button) findViewById(R.id.startbutton);
		mAutoResetButton.setOnClickListener(this);
		mStartButton.setOnClickListener(this);
		mUseAutoReset = mAutoResetButton.isChecked();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.startbutton:
			startMotionTracking();
			break;
		case R.id.autoresetbutton:
			mUseAutoReset = mAutoResetButton.isChecked();
			break;
		}
	}
	
	private void startMotionTracking(){
		Intent startmotiontracking = new Intent(this,MotionTracking.class);
		startmotiontracking.putExtra(KEY_MOTIONTRACKING_AUTORESET,mUseAutoReset);
		startActivity(startmotiontracking);
	}
}
