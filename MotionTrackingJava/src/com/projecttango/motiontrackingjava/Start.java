package com.projecttango.motiontrackingjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class Start extends Activity implements View.OnClickListener{
	
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
			StartMotionTracking();
			break;
		case R.id.autoresetbutton:
			mUseAutoReset = mAutoResetButton.isChecked();
			break;
		}
	}
	
	private void StartMotionTracking(){
		Intent startmotiontracking = new Intent(this,MotionTracking.class);
		startmotiontracking.putExtra("com.projecttango.motiontrackingjava.useautoreset",mUseAutoReset);
		startActivity(startmotiontracking);
	}
}
