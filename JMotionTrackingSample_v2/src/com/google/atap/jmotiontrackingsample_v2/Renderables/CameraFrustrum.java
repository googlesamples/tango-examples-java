package com.google.atap.jmotiontrackingsample_v2.Renderables;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.google.atap.jmotiontrackingsample_v2.MTGLRenderer;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class CameraFrustrum {

	private float[] mTranslation = new float[3];

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
			+ "gl_FragColor = vColor;" + 
			"}";

	private float vertices[] = { 1.5f, 0, 0, 0, 0, 0,

	0, 1.5f, 0, 0, 0, 0,

	0, 0, 1.5f, 0, 0, 0 };

	private float colors[] = { 1.0f, 0.0f, 0.0f, 1.0f,
			1.0f, 0.0f, 0.0f, 1.0f,
			
			0.0f, 1.0f, 0.0f, 1.0f,
			0.0f, 1.0f, 0.0f, 1.0f,
			
			0.0f, 0.0f, 1.0f, 1.0f,
			0.0f, 0.0f, 1.0f, 1.0f, };

	private float[] modelMatrix = new float[16];
	private float[] mvMatrix = new float[16];
	private float[] mvpMatrix = new float[16];

	private final int mProgram;
	private int mPosHandle, mColorHandle;
	static final int COORDS_PER_VERTEX = 3;
	private int mMVPMatrixHandle;

	public CameraFrustrum() {
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
		float[] quaternionMatrix = new float[16];
		quaternionMatrix = quaternionM(quaternion);

		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, translation[0], translation[1],
				translation[2]);

		float[] mTempMatrix = new float[16];
		Matrix.setIdentityM(mTempMatrix, 0);

		if (quaternionMatrix != null) {
			Matrix.multiplyMM(mTempMatrix, 0, modelMatrix, 0, quaternionMatrix,
					0);

			System.arraycopy(mTempMatrix, 0, modelMatrix, 0, 16);
		}
	};

	public void updateViewMatrix(float[] viewMatrix) {
		Matrix.setLookAtM(viewMatrix, 0, mTranslation[0],
				mTranslation[1] + 5.0f, mTranslation[2] + 5.0f,
				mTranslation[0], mTranslation[1], mTranslation[2], 0, 1, 0);
	}

	public void draw(float[] viewMatrix, float[] projectionMatrix) {

		GLES20.glUseProgram(mProgram);
		updateViewMatrix(viewMatrix);

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
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, 6);

	}

	public static float[] quaternionM(float[] quaternion) {
		float[] matrix = new float[16];
		normalizeVector(quaternion);

		float x = quaternion[0];
		float y = quaternion[1];
		float z = quaternion[2];
		float w = quaternion[3];

		float x2 = x * x;
		float y2 = y * y;
		float z2 = z * z;
		float xy = x * y;
		float xz = x * z;
		float yz = y * z;
		float wx = w * x;
		float wy = w * y;
		float wz = w * z;

		matrix[0] = 1f - 2f * (y2 + z2);
		matrix[1] = 2f * (xy - wz);
		matrix[2] = 2f * (xz + wy);
		matrix[3] = 0f;

		matrix[4] = 2f * (xy + wz);
		matrix[5] = 1f - 2f * (x2 + z2);
		matrix[6] = 2f * (yz - wx);
		matrix[7] = 0f;

		matrix[8] = 2f * (xz - wy);
		matrix[9] = 2f * (yz + wx);
		matrix[10] = 1f - 2f * (x2 + y2);
		matrix[11] = 0f;

		matrix[12] = 0f;
		matrix[13] = 0f;
		matrix[14] = 0f;
		matrix[15] = 1f;
		return matrix;
	}

	public static void normalizeVector(float[] v) {

		float mag2 = v[0] * v[0] + v[1] * v[1] + v[2] * v[2] + v[3] * v[3];
		if (Math.abs(mag2) > 0.00001f && Math.abs(mag2 - 1.0f) > 0.00001f) {
			float mag = (float) Math.sqrt(mag2);
			v[0] /= mag;
			v[1] /= mag;
			v[2] /= mag;
			v[3] /= mag;
		}
	}
}