package com.projecttango.jmotiontrackingsample.Renderables;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import java.lang.Math;

import com.projecttango.jmotiontrackingsample.MTGLRenderer;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class CameraFrustum {

	private float[] mTranslation = new float[3];
	private float[] mQuaternion = new float[4];

	private FloatBuffer mVertexBuffer, mColorBuffer;
	private final String vertexShaderCode =
			"uniform mat4 uMVPMatrix;"
			+ "attribute vec4 vPosition;"
			+ "attribute vec4 aColor;"
			+ "varying vec4 vColor;"
			+ "void main() {"+
			"  vColor=aColor;" 
			+ "gl_Position = uMVPMatrix * vPosition;"
			+ "}";

	private final String fragmentShaderCode = 
			"precision mediump float;"
			+ "varying vec4 vColor;" 
			+ "void main() {"
			+ "gl_FragColor = vec4(0.8,0.5,0.8,1);" + 
			"}";

	private float vertices[] = {   0.0f, 0.0f, 0.0f,
		    -0.4f, 0.3f, -0.5f,

		    0.0f, 0.0f, 0.0f,
		    0.4f, 0.3f, -0.5f,

		    0.0f, 0.0f, 0.0f,
		    -0.4f, -0.3f, -0.5f,

		    0.0f, 0.0f, 0.0f,
		    0.4f, -0.3f, -0.5f,

		    -0.4f, 0.3f, -0.5f,
		    0.4f, 0.3f, -0.5f,

		    0.4f, 0.3f, -0.5f,
		    0.4f, -0.3f, -0.5f,

		    0.4f, -0.3f, -0.5f,
		    -0.4f, -0.3f, -0.5f,

		    -0.4f, -0.3f, -0.5f,
		    -0.4f, 0.3f, -0.5f  };

	private float colors[] = { 1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f,
			
			1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f};

	private float[] modelMatrix = new float[16];
	private float[] mvMatrix = new float[16];
	private float[] mvpMatrix = new float[16];

	private final int mProgram;
	private int mPosHandle, mColorHandle;
	static final int COORDS_PER_VERTEX = 3;
	private int mMVPMatrixHandle;

	public CameraFrustum() {
		Matrix.setIdentityM(modelMatrix, 0);

		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuf.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		ByteBuffer cByteBuff = ByteBuffer.allocateDirect(colors.length * 4);
		cByteBuff.order(ByteOrder.nativeOrder());
		mColorBuffer = cByteBuff.asFloatBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);

		int vertexShader = MTGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
				vertexShaderCode);
		int fragShader = MTGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);
		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragShader);
		GLES20.glLinkProgram(mProgram);
	}

	public void updateModelMatrix(float[] translation, float[] quaternion) {

		mTranslation = translation;
		mQuaternion = quaternion;
		
		float[] openglQuaternion = MathUtils.ConvertQuaternionToOpenGl(quaternion);
		float[] quaternionMatrix = new float[16];
		
		quaternionMatrix = MathUtils.quaternionM(openglQuaternion);
		//quaternionMatrix = MathUtils.quaternionM(quaternion);		
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, translation[0], translation[2],
				-translation[1]);

		float[] mTempMatrix = new float[16];
		Matrix.setIdentityM(mTempMatrix, 0);

		if (quaternionMatrix != null) {
			Matrix.multiplyMM(mTempMatrix, 0, quaternionMatrix, 0, modelMatrix,
					0);

			modelMatrix = mTempMatrix;
		}
	};

	public void updateViewMatrix(float[] viewMatrix) {
		Matrix.setLookAtM(viewMatrix, 0,0,
				 5.0f,5.0f,
				mTranslation[0], mTranslation[1], mTranslation[2], 0, 1, 0);
	}

	public void draw(float[] viewMatrix, float[] projectionMatrix) {

		GLES20.glUseProgram(mProgram);
		//updateViewMatrix(viewMatrix);

		Matrix.setIdentityM(mvMatrix, 0);
		Matrix.setIdentityM(mvpMatrix, 0);
		Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0);

		mPosHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		GLES20.glVertexAttribPointer(mPosHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, 0, mVertexBuffer);
		GLES20.glEnableVertexAttribArray(mPosHandle);

		mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
		GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false,
				0, mColorBuffer);
		GLES20.glEnableVertexAttribArray(mColorHandle);

		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
		GLES20.glLineWidth(5);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, 16
				);

	}
	
}