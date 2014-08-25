package com.projecttango.jmotiontrackingsample.Renderables;

public class MathUtils {
	
	public static float[] ConvertQuaternionToOpenGl(float[] quaternion)
	{
		double[] xAxis = {1,0,0};
		float[] rotation_offsetX = RotateQuaternionWithAngleAxis(quaternion, 1.57079, xAxis );
		double[] zAxis = {0,0,-1};
		float[] rotation_offsetZ = RotateQuaternionWithAngleAxis(rotation_offsetX, 1.57079, zAxis );
		float[] rotation_inversed = InvertQuaternion(rotation_offsetZ);
		float[] openglQuaternion = {rotation_inversed[3],rotation_inversed[1],-rotation_inversed[0],rotation_inversed[2]};
		//Log.e("Opengl Rotation Quaternion:",""+openglQuaternion[0]+" "+openglQuaternion[1]+" " +openglQuaternion[2]+" "+ openglQuaternion[3]+" ");
		return openglQuaternion;		
	}
	
	public static float[] InvertQuaternion(float[] Quaternion)
	{	
		float sqNorm = (float) (Math.pow(Quaternion[0], 2) + Math.pow(Quaternion[1], 2) + Math.pow(Quaternion[2], 2)+ Math.pow(Quaternion[3], 2));
		float[] inversedQ = new float[4];
		inversedQ[0] = Quaternion[0]/sqNorm;
		inversedQ[1] = Quaternion[1]/sqNorm;
		inversedQ[2] = Quaternion[2]/sqNorm;
		inversedQ[3] = Quaternion[3]/sqNorm;
		 //Log.e("INverse Rotated:",""+inversedQ[0]+" "+inversedQ[1]+" " +inversedQ[2]+" "+ inversedQ[3]+" ");
		return inversedQ;
	}
	
	public static float[] RotateQuaternionWithAngleAxis(float[] quaternion, double angleInRadians,double[] axisVector)
	{
		float norm = (float) Math.sqrt(Math.pow(axisVector[0], 2) + Math.pow(axisVector[1], 2) + Math.pow(axisVector[2], 2));
		float sin_half_angle = (float) Math.sin(angleInRadians / 2.0f);
        float x = (float) (sin_half_angle * axisVector[0] / norm);
        float y = (float) (sin_half_angle * axisVector[1] / norm);
        float z = (float) (sin_half_angle * axisVector[2] / norm);
        float w = (float)Math.cos(angleInRadians / 2.0f);
        float[] rotatedQuaternion = {x,y,z,w};
        float[] multiQuaternion = multiplyQuarternions(rotatedQuaternion, quaternion);
       // Log.e("Rotated:",""+multiQuaternion[0]+" "+multiQuaternion[1]+" " +multiQuaternion[2]+" "+ multiQuaternion[3]+" ");
        return multiQuaternion;
	}
	
	public static float[] multiplyQuarternions(float[] a,float[] b)
	{
		float[] multipliedQuaternion = new float[4];
		multipliedQuaternion[0] = a[0]*b[0] - a[1]*b[1] - a[2]*b[2] - a[3]*b[3];
		multipliedQuaternion[1] = a[0]*b[1] + a[1]*b[0] + a[2]*b[3] - a[3]*b[2];
		multipliedQuaternion[2] = a[0]*b[2] - a[1]*b[3] + a[2]*b[0] + a[3]*b[1];
		multipliedQuaternion[3] = a[0]*b[3] + a[1]*b[2] - a[2]*b[1] + a[3]*b[3];
		return multipliedQuaternion;
		
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
