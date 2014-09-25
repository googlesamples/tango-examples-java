package com.projecttango.areadescriptionjava;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ToggleButton;
import android.view.View;

public class ADStartActivity extends Activity implements View.OnClickListener{
	
	public static String USE_AREA_LEARNING = "com.projecttango.areadescriptionjava.usearealearning";
	public static String LOAD_ADF = "com.projecttango.areadescriptionjava.loadadf";
	private ToggleButton mLearningModeToggleButton;
	private ToggleButton mLoadADFToggleButton;
	private Button mStartButton;
	private boolean mIsUseAreaLearning;
	private boolean mIsLoadADF;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
		mLearningModeToggleButton = (ToggleButton)findViewById(R.id.learningmode);
		mLoadADFToggleButton= (ToggleButton)findViewById(R.id.loadadf);
		mStartButton=(Button)findViewById(R.id.start);
		mLearningModeToggleButton.setOnClickListener(this);
		mLoadADFToggleButton.setOnClickListener(this);
		mStartButton.setOnClickListener(this);
		mIsUseAreaLearning = mLearningModeToggleButton.isChecked();
		mIsLoadADF = mLoadADFToggleButton.isChecked();
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.loadadf:
			mIsLoadADF = mLoadADFToggleButton.isChecked();
			break;
		case R.id.learningmode:
			mIsUseAreaLearning = mLearningModeToggleButton.isChecked();
			break;
		case R.id.start:
			StartAreaDescriptionActivity();
			break;
		}
		
	}
	
	private void StartAreaDescriptionActivity(){
		Intent startADIntent = new Intent(this,AreaDescription.class);
		startADIntent.putExtra(USE_AREA_LEARNING, mIsUseAreaLearning);
		startADIntent.putExtra(LOAD_ADF, mIsLoadADF);
		startActivity(startADIntent);
	}

}
