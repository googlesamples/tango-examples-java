package com.projecttango.tangoutils.renderables;

import java.nio.FloatBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

import android.opengl.GLES20;
import android.opengl.Matrix;

import javax.microedition.khronos.opengles.GL10;

/**
 * Drawing a simple sphere.
 * {@link Renderable}
 * @author Xavier McNulty
 */
public class Sphere extends Renderable {
    private static final int COORDS_PER_VERTEX = 3;

    private static final String sVertexShaderCode = "uniform mat4 uMVPMatrix;"
            + "attribute vec4 vPosition;" + "attribute vec4 aColor;"
            + "varying vec4 vColor;" + "void main() {" + "  vColor=aColor;"
            + "gl_Position = uMVPMatrix * vPosition;" + "}";

    private static final String sFragmentShaderCode = "precision mediump float;"
            + "varying vec4 vColor;"
            + "void main() {"
            + "gl_FragColor = vec4(0.8,0.5,0.8,1);" + "}";

    static private FloatBuffer sphereVertex, sphereColors;

    double _radius;
    double _step;
    //float _verticies;
    private static double DEG = Math.PI / 180; // rad to deg converter
    int _points;

    private final int mProgram;
    private int mPosHandle, mColorHandle;
    private int mMVPMatrixHandle;

    /**
     * Constructs a sphere. Step will define the size of each facet, as well as the number
     * of facets.
     * @param radius
     * @param step
     */
    public Sphere(float radius, float step) {
        // Reset the model matrix to the identity
        Matrix.setIdentityM(getModelMatrix(), 0);

        this._radius = radius;
        this._step = step;
        sphereVertex = FloatBuffer.allocate(40000);
        sphereColors = FloatBuffer.allocate(40000);
        _points = build();

        sphereVertex.position(0);
        sphereColors.position(0);

        // Load the vertex and fragment shaders, then link the program
        int vertexShader = RenderUtils.loadShader(GLES20.GL_VERTEX_SHADER,
                sVertexShaderCode);
        int fragShader = RenderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER,
                sFragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragShader);
        GLES20.glLinkProgram(mProgram);
    }

    /**
     * Draws the sphere
     * @param viewMatrix
     *            the view matrix to map from world space to camera space.
     * @param projectionMatrix
     *            the projection matrix to map from camera space to screen
     */
    @Override
    public void draw(float[] viewMatrix, float[] projectionMatrix) {
        GLES20.glUseProgram(mProgram);
        // updateViewMatrix(viewMatrix);

        // Compose the model, view, and projection matrices into a single mvp
        // matrix
        updateMvpMatrix(viewMatrix, projectionMatrix);

        // Load vertex attribute data
        mPosHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glVertexAttribPointer(mPosHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, sphereVertex);
        GLES20.glEnableVertexAttribArray(mPosHandle);

        // Load color attribute data
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false,
                0, sphereColors);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        // Draw the CameraFrustum
        GLES20.glFrontFace(GLES20.GL_CW);

        //gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GL10.GL_POINTS, 0, _points);
        //GLES20.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    /**
     * Builds the vertex of sphere points.
     * @return
     */
    private int build() {
        /**
         * x = p * sin(phi) * cos(theta)
         * y = p * sin(phi) * sin(theta)
         * z = p * cos(phi)
         */
        double _theta = _step * DEG;
        double _phi = _theta;
        int points = 0;

        for(double phi = -(Math.PI); phi <= Math.PI; phi+=_phi) {
            //for each stage calculating the slices
            for(double theta = 0.0; theta <= (Math.PI * 2); theta+=_theta) {
                // add the verticies to the
                sphereVertex.put((float) (_radius * Math.sin(phi) * Math.cos(theta)) );
                sphereVertex.put((float) (_radius * Math.sin(phi) * Math.sin(theta)) );
                sphereVertex.put((float) (_radius * Math.cos(phi)) );

                // add the colors to each vert.
                sphereColors.put(0.2f);
                sphereColors.put(0.2f);
                sphereColors.put(0.2f);
                points++;

            }
        }
        sphereVertex.position(0);
        return points;
    }
}