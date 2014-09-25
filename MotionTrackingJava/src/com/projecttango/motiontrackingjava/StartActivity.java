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
