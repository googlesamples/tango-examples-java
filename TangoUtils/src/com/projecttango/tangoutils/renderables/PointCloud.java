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

package com.projecttango.tangoutils.renderables;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;


/**
 * {@link Renderable} OpenGL showing a PointCloud obtained from Tango XyzIj data.  The point count
 * can vary over as the information is updated.
 */
public class PointCloud extends Renderable {
	
	private static final int COORDS_PER_VERTEX = 3;
	
	private static final String sVertexShaderCode = 
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
             "varying vec4 vColor;"+
            "void main() {" +
            "gl_PointSize = 2.0;"+
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  vColor = vPosition;" +
            "}";
	private static final String sFragmentShaderCode="precision mediump float;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vec4(-vColor);" +
            "}";
	
	private static final int BYTES_PER_FLOAT = 4;
	public boolean misUpdateInProgress;
	private FloatBuffer mVertexBuffer;
	private FloatBuffer mCurrentVertexBuffer;
	private final int mProgram;
	private int mPosHandle;
	private int mMVPMatrixHandle;
	private int mPointCount;
	private float mAverageZ;
	public PointCloud() {
		mAverageZ=0;
		int vertexShader = RenderUtils.loadShader(GLES20.GL_VERTEX_SHADER,sVertexShaderCode);
		int fragShader = RenderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, sFragmentShaderCode);
		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragShader);
		GLES20.glLinkProgram(mProgram);
		Matrix.setIdentityM(getModelMatrix(), 0);
	}
	
	public void UpdatePoints(byte[] byteArray)
	{	
		misUpdateInProgress = true;
		if(mVertexBuffer!=null)
		{
			mCurrentVertexBuffer = mVertexBuffer.duplicate();
		}
		
		FloatBuffer mPointCloudFloatBuffer;
		mPointCloudFloatBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.nativeOrder()).asFloatBuffer(); 
		mPointCount = mPointCloudFloatBuffer.capacity()/3;
		mVertexBuffer = ByteBuffer.allocateDirect(mPointCloudFloatBuffer.capacity()*BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mVertexBuffer.clear();
		mVertexBuffer.position(0);
		float totalZ=0;
		for(int i=0; i< mPointCloudFloatBuffer.capacity();i=i+3){
			if(i+3 < mPointCloudFloatBuffer.capacity())
			{
				mVertexBuffer.put(mPointCloudFloatBuffer.get(i));
				mVertexBuffer.put(-mPointCloudFloatBuffer.get(i+1));
				mVertexBuffer.put(-mPointCloudFloatBuffer.get(i+2));
				totalZ = totalZ+mPointCloudFloatBuffer.get(i+2);
			}
		}
		mAverageZ = totalZ/mPointCount;
		misUpdateInProgress =false;
	}
	
	@Override
	public void draw(float[] viewMatrix, float[] projectionMatrix) {
		if (mPointCount > 0 && !misUpdateInProgress) {
			Log.i("Using BUffer", "mVertexBuffer");
				mVertexBuffer.position(0);
				GLES20.glUseProgram(mProgram);
				updateMvpMatrix(viewMatrix, projectionMatrix);			
				mPosHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");	
			    GLES20.glVertexAttribPointer(mPosHandle, COORDS_PER_VERTEX,GLES20.GL_FLOAT, false,0, mVertexBuffer);		
			    GLES20.glEnableVertexAttribArray(mPosHandle);	
			    mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");	
			    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, getMvpMatrix(), 0);	
			    GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mPointCount); 
		}

		else if(mPointCount > 0 && misUpdateInProgress){
			if(mCurrentVertexBuffer!=null){
				Log.i("Using BUffer", "mCurrentBuffer");
				mCurrentVertexBuffer.position(0);
				GLES20.glUseProgram(mProgram);
				updateMvpMatrix(viewMatrix, projectionMatrix);			
				mPosHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");	
			    GLES20.glVertexAttribPointer(mPosHandle, COORDS_PER_VERTEX,GLES20.GL_FLOAT, false,0, mCurrentVertexBuffer);		
			    GLES20.glEnableVertexAttribArray(mPosHandle);	
			    mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");	
			    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, getMvpMatrix(), 0);	
			    GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mPointCount); 
			}
		}
	}
	
	public float getAverageZ(){
		return mAverageZ;
	}
	public int getPointCount(){
		return mPointCount;
	}
}
