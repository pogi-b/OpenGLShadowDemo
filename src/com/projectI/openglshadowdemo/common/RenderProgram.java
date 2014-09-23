/**
 * Represents a shader object
 * from Shayan Javed
 * http://blog.shayanjaved.com/2011/03/13/shaders-android/
 * 
 * modified!
 */

package com.projectI.openglshadowdemo.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

public class RenderProgram {
	/************************
	 * PROPERTIES
	 **********************/
	private static final String TAG = "RenderProgram";
	
	// program/vertex/fragment handles
	private int mProgram, mVertexShader, mPixelShader;

	// The shaders
	private String mVertexS, mFragmentS;

	/************************
	 * CONSTRUCTOR(S)
	 *************************/
	// Takes in Strings directly
	public RenderProgram(String vertexS, String fragmentS) {
		setup(vertexS, fragmentS);
	}

	// Takes in ids for files to be read
	public RenderProgram(int vID, int fID, Context context) {
		StringBuffer vs = new StringBuffer();
		StringBuffer fs = new StringBuffer();

		// read the files
		try {
			// Read the file from the resource
			//Log.d("loadFile", "Trying to read vs");
			// Read VS first
			InputStream inputStream = context.getResources().openRawResource(vID);
			// setup Bufferedreader
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

			String read = in.readLine();
			while (read != null) {
				vs.append(read + "\n");
				read = in.readLine();
			}

			vs.deleteCharAt(vs.length() - 1);

			// Now read FS
			inputStream = context.getResources().openRawResource(fID);
			// setup Bufferedreader
			in = new BufferedReader(new InputStreamReader(inputStream));

			read = in.readLine();
			while (read != null) {
				fs.append(read + "\n");
				read = in.readLine();
			}

			fs.deleteCharAt(fs.length() - 1);
		} catch (Exception e) {
			Log.d(TAG, "Could not read shader: " + e.getLocalizedMessage());
		}


		// Setup everything
		setup(vs.toString(), fs.toString());
	}


	/**************************
	 * OTHER METHODS
	 *************************/

	/** 
	 * Sets up everything
	 * @param vs the vertex shader
	 * @param fs the fragment shader 
	 */
	private void setup(String vs, String fs) {
		this.mVertexS = vs;
		this.mFragmentS = fs;

		// create the program
		if (createProgram() != 1) {
			throw new RuntimeException("Error at creating shaders");
		};
	}

	/**
	 * Creates a shader program.
	 * @param vertexSource
	 * @param fragmentSource
	 * @return returns 1 if creation successful, 0 if not
	 */
	private int createProgram() {
		// Vertex shader
		mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, mVertexS);
		if (mVertexShader == 0) {
			return 0;
		}

		// pixel shader
		mPixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentS);
		if (mPixelShader == 0) {
			return 0;
		}

		// Create the program
		mProgram = GLES20.glCreateProgram();
		if (mProgram != 0) {
			GLES20.glAttachShader(mProgram, mVertexShader);
			//checkGlError("glAttachShader VS " + this.toString());
			GLES20.glAttachShader(mProgram, mPixelShader);
			//checkGlError("glAttachShader PS");
			GLES20.glLinkProgram(mProgram);
			int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] != GLES20.GL_TRUE) {
				Log.e(TAG, "Could not link _program: ");
				Log.e(TAG, GLES20.glGetProgramInfoLog(mProgram));
				GLES20.glDeleteProgram(mProgram);
				mProgram = 0;
				return 0;
			}
		}
		else
			Log.d("CreateProgram", "Could not create program");

		return 1;
	}

	/**
	 * Loads a shader (either vertex or pixel) given the source
	 * @param shaderType VERTEX or PIXEL
	 * @param source The string data representing the shader code
	 * @return handle for shader
	 */
	private int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				Log.e(TAG, "Could not compile shader " + shaderType + ":");
				Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

	/***************************
	 * GET/SET
	 *************************/
	public int getProgram() {
		return mProgram;
	}
}
