package com.projectI.openglshadowdemo;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.projectI.openglshadowdemo.R;
import com.projectI.openglshadowdemo.ShadowsActivity;
import com.projectI.openglshadowdemo.common.RenderConstants;
import com.projectI.openglshadowdemo.common.FPSCounter;
import com.projectI.openglshadowdemo.common.RenderProgram;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class ShadowsRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "ShadowsRenderer";

    private final ShadowsActivity mShadowsActivity;
    
    private FPSCounter mFPSCounter;
    
    /**
	 * Handles to vertex and fragment shader programs
	 */
	private RenderProgram mSimpleShadowProgram;
	private RenderProgram mPCFShadowProgram;
	private RenderProgram mSimpleShadowDynamicBiasProgram;
	private RenderProgram mPCFShadowDynamicBiasProgram;
	
	/**
	 * The vertex and fragment shader to render depth map
	 */
	private RenderProgram mDepthMapProgram;
	
	private int mActiveProgram;
	
    private final float[] mMVPMatrix = new float[16];
    private final float[] mMVMatrix = new float[16];
    private final float[] mNormalMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];

    private final float[] mCubeRotation = new float[16];
    
    /**
     * MVP matrix used at rendering shadow map for stationary objects
     */
    private final float[] mLightMvpMatrix_staticShapes = new float[16];
    
    /**
     * MVP matrix used at rendering shadow map for the big cube in the center
     */
    private final float[] mLightMvpMatrix_dynamicShapes = new float[16];
    
    /**
     * Projection matrix from point of light source
     */
    private final float[] mLightProjectionMatrix = new float[16];
    
    /**
     * View matrix of light source
     */
    private final float[] mLightViewMatrix = new float[16];
    
    /**
     * Position of light source in eye space
     */
    private final float[] mLightPosInEyeSpace = new float[16];
    
    /**
     * Light source position in model space
     */
    private final float[] mLightPosModel = new float []
    		{-5.0f, 9.0f, 0.0f, 1.0f};
    
    private float[] mActualLightPosition = new float[4];
    
    /**
     * Current X,Y axis rotation of center cube
     */
    private float mRotationX;
    private float mRotationY;

    /**
     * Current display sizes
     */
	private int mDisplayWidth;
	private int mDisplayHeight;
	
	/**
	 * Current shadow map sizes
	 */
	private int mShadowMapWidth;
	private int mShadowMapHeight;
    
	private boolean mHasDepthTextureExtension = false;
	
	int[] fboId;
	int[] depthTextureId;
	int[] renderTextureId;
	
	// Uniform locations for scene render program
	private int scene_mvpMatrixUniform;
	private int scene_mvMatrixUniform;
	private int scene_normalMatrixUniform;
	private int scene_lightPosUniform;
	private int scene_schadowProjMatrixUniform;
	private int scene_textureUniform;
	private int scene_mapStepXUniform;
	private int scene_mapStepYUniform;
	
	// Uniform locations for shadow render program
	private int shadow_mvpMatrixUniform;
	
	// Shader program attribute locations
	private int scene_positionAttribute;
	private int scene_normalAttribute;
	private int scene_colorAttribute;
	
	private int shadow_positionAttribute;
		
	private int texture_mvpMatrixUniform;
	private int texture_positionAttribute;
	private int texture_texCoordAttribute;
	private int texture_textureUniform;
	
	// Shapes that will be displayed
	private Cube mCube;
	private Cube mSmallCube0;
	private Cube mSmallCube1;
	private Cube mSmallCube2;
	private Cube mSmallCube3;
	
	private Plane mPlane;

	public ShadowsRenderer(final ShadowsActivity shadowsActivity) {
		mShadowsActivity = shadowsActivity;
	}
	
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		mFPSCounter = new FPSCounter();
    	
		// Test OES_depth_texture extension
		String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
		
		if (extensions.contains("OES_depth_texture"))
			mHasDepthTextureExtension = true;
		
        //Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        //Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		GLES20.glEnable(GLES20.GL_CULL_FACE);
        
        //arrange scene
        //center cube
        mCube = new Cube(new float[] {0.0f, 0.0f, 0.0f}, 3.0f, new float[] {0.0f, 0.0f, 1.0f, 1.0f});
        
        //4 small cubes on the ground plane
        mSmallCube0 = new Cube(new float[] {-4.0f, -3.9f, 4.0f}, 2.0f, new float[] {1.0f, 0.0f, 0.0f, 1.0f});
        mSmallCube1 = new Cube(new float[] {4.0f, -3.9f, 4.0f}, 2.0f, new float[] {0.0f, 1.0f, 0.0f, 1.0f});
        mSmallCube2 = new Cube(new float[] {4.0f, -3.9f, -4.0f}, 2.0f, new float[] {0.0f, 1.0f, 1.0f, 1.0f});
        mSmallCube3 = new Cube(new float[] {-4.0f, -3.9f, -4.0f}, 2.0f, new float[] {1.0f, 0.0f, 1.0f, 1.0f});
        
        //ground
        mPlane = new Plane();
        
        //Set view matrix from eye position
        Matrix.setLookAtM(mViewMatrix, 0, 
        					//eyeX, eyeY, eyeZ, 
        					0, 4, -12,
        					//lookX, lookY, lookZ,
        					0, 0, 0,
        					//upX, upY, upZ
        					0, 1, 0);
        
        //Load shaders and create program used by OpenGL for rendering
        if(!mHasDepthTextureExtension){
        	// If there is no OES_depth_texture extension depth values must be coded in rgba texture and later decoded at calculation of shadow
	        mSimpleShadowProgram = new RenderProgram(R.raw.v_with_shadow,
	        												R.raw.f_with_simple_shadow, mShadowsActivity);
	        
	        mPCFShadowProgram = new RenderProgram(R.raw.v_with_shadow,
					R.raw.f_with_pcf_shadow, mShadowsActivity);
	        
	        mSimpleShadowDynamicBiasProgram = new RenderProgram(R.raw.v_with_shadow,
					R.raw.f_with_simple_shadow_dynamic_bias, mShadowsActivity);
	
			mPCFShadowDynamicBiasProgram = new RenderProgram(R.raw.v_with_shadow,
					R.raw.f_with_pcf_shadow_dynamic_bias, mShadowsActivity);
	        
	        mDepthMapProgram = new RenderProgram(R.raw.v_depth_map,
	        												R.raw.f_depth_map, mShadowsActivity);
        }
        else {
        	// OES_depth_texture is available -> shaders are simplier
        	mSimpleShadowProgram = new RenderProgram(R.raw.depth_tex_v_with_shadow,
					R.raw.depth_tex_f_with_simple_shadow, mShadowsActivity);
        	
        	mPCFShadowProgram = new RenderProgram(R.raw.depth_tex_v_with_shadow,
					R.raw.depth_tex_f_with_pcf_shadow, mShadowsActivity);
	        
	        mSimpleShadowDynamicBiasProgram = new RenderProgram(R.raw.depth_tex_v_with_shadow,
					R.raw.depth_tex_f_with_simple_shadow_dynamic_bias, mShadowsActivity);
	
			mPCFShadowDynamicBiasProgram = new RenderProgram(R.raw.depth_tex_v_with_shadow,
					R.raw.depth_tex_f_with_pcf_shadow_dynamic_bias, mShadowsActivity);
        	
        	mDepthMapProgram = new RenderProgram(R.raw.depth_tex_v_depth_map,
					R.raw.depth_tex_f_depth_map, mShadowsActivity);
        }
        	
        
        mActiveProgram = mSimpleShadowProgram.getProgram();
    }

    /**
	 * Sets up the framebuffer and renderbuffer to render to texture
	 */
	public void generateShadowFBO()
	{
		mShadowMapWidth = Math.round(mDisplayWidth * mShadowsActivity.getmShadowMapRatio());
		mShadowMapHeight = Math.round(mDisplayHeight * mShadowsActivity.getmShadowMapRatio());
		
		fboId = new int[1];
		depthTextureId = new int[1];
		renderTextureId = new int[1];
		
		// create a framebuffer object
		GLES20.glGenFramebuffers(1, fboId, 0);
		
		// create render buffer and bind 16-bit depth buffer
		GLES20.glGenRenderbuffers(1, depthTextureId, 0);
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthTextureId[0]);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mShadowMapWidth, mShadowMapHeight);
		
		// Try to use a texture depth component
		GLES20.glGenTextures(1, renderTextureId, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId[0]);
		
		// GL_LINEAR does not make sense for depth texture. However, next tutorial shows usage of GL_LINEAR and PCF. Using GL_NEAREST
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
		
		// Remove artifact on the edges of the shadowmap
		GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );
		GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);
		
		if (!mHasDepthTextureExtension) {
			GLES20.glTexImage2D( GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mShadowMapWidth, mShadowMapHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
			
			// specify texture as color attachment
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTextureId[0], 0);
			
			// attach the texture to FBO depth attachment point
			// (not supported with gl_texture_2d)
			GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthTextureId[0]);
	    }
		else {
			// Use a depth texture
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, mShadowMapWidth, mShadowMapHeight, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);
	 
	        // Attach the depth texture to FBO depth attachment point
			GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, renderTextureId[0], 0);
		}
		
		// check FBO status
		int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if(FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
			Log.e(TAG, "GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
			throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
		}
	}
    
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
    	mDisplayWidth = width;
		mDisplayHeight = height;
    	
    	// Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);

        // Generate buffer where depth values are saved for shadow calculation
        generateShadowFBO();
        
        float ratio = (float) mDisplayWidth / mDisplayHeight;

        // this projection matrix is applied at rendering scene
        // in the onDrawFrame() method
        float bottom = -1.0f;
        float top = 1.0f;
        float near = 1.0f;
        float far = 100.0f;
        
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, bottom, top, near, far);
        
        // this projection matrix is used at rendering shadow map
        Matrix.frustumM(mLightProjectionMatrix, 0, -1.1f*ratio, 1.1f*ratio, 1.1f*bottom, 1.1f*top, near, far);
        //Matrix.frustumM(mLightProjectionMatrix, 0, -ratio, ratio, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
    	// Write FPS information to console
    	mFPSCounter.logFrame();
    	
    	setRenderProgram();
    	
		// Set program handles for cube drawing.
		scene_mvpMatrixUniform = GLES20.glGetUniformLocation(mActiveProgram, RenderConstants.MVP_MATRIX_UNIFORM);
		scene_mvMatrixUniform = GLES20.glGetUniformLocation(mActiveProgram, RenderConstants.MV_MATRIX_UNIFORM);
		scene_normalMatrixUniform = GLES20.glGetUniformLocation(mActiveProgram, RenderConstants.NORMAL_MATRIX_UNIFORM);
		scene_lightPosUniform = GLES20.glGetUniformLocation(mActiveProgram, RenderConstants.LIGHT_POSITION_UNIFORM);
		scene_schadowProjMatrixUniform = GLES20.glGetUniformLocation(mActiveProgram, RenderConstants.SHADOW_PROJ_MATRIX);
		scene_textureUniform = GLES20.glGetUniformLocation(mActiveProgram, RenderConstants.SHADOW_TEXTURE);
		scene_positionAttribute = GLES20.glGetAttribLocation(mActiveProgram, RenderConstants.POSITION_ATTRIBUTE);
		scene_normalAttribute = GLES20.glGetAttribLocation(mActiveProgram, RenderConstants.NORMAL_ATTRIBUTE);
		scene_colorAttribute = GLES20.glGetAttribLocation(mActiveProgram, RenderConstants.COLOR_ATTRIBUTE);
		scene_mapStepXUniform = GLES20.glGetUniformLocation(mActiveProgram, RenderConstants.SHADOW_X_PIXEL_OFFSET);
		scene_mapStepYUniform = GLES20.glGetUniformLocation(mActiveProgram, RenderConstants.SHADOW_Y_PIXEL_OFFSET);
		
		//shadow handles
		int shadowMapProgram = mDepthMapProgram.getProgram();
		shadow_mvpMatrixUniform = GLES20.glGetUniformLocation(shadowMapProgram, RenderConstants.MVP_MATRIX_UNIFORM);
		shadow_positionAttribute = GLES20.glGetAttribLocation(shadowMapProgram, RenderConstants.SHADOW_POSITION_ATTRIBUTE);
		
		//display texture program handles (for debugging depth texture)
		//texture_mvpMatrixUniform = GLES20.glGetUniformLocation(textureProgram, RenderConstants.MVP_MATRIX_UNIFORM);
		//texture_positionAttribute = GLES20.glGetAttribLocation(textureProgram, RenderConstants.POSITION_ATTRIBUTE);
		//texture_texCoordAttribute = GLES20.glGetAttribLocation(textureProgram, RenderConstants.TEX_COORDINATE);
		//texture_textureUniform = GLES20.glGetUniformLocation(textureProgram, RenderConstants.TEXTURE_UNIFORM);

		//--------------- calc values common for both renderers
		
		// light rotates around Y axis in every 12 seconds
        long elapsedMilliSec = SystemClock.elapsedRealtime();
        long rotationCounter = elapsedMilliSec % 12000L;
        
        float lightRotationDegree = (360.0f / 12000.0f) * ((int)rotationCounter);
        
        float[] rotationMatrix = new float[16];
        
        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, lightRotationDegree, 0.0f, 1.0f, 0.0f);

        Matrix.multiplyMV(mActualLightPosition, 0, rotationMatrix, 0, mLightPosModel, 0);
	
        Matrix.setIdentityM(mModelMatrix, 0);
        
        //Set view matrix from light source position
        Matrix.setLookAtM(mLightViewMatrix, 0, 
        					//lightX, lightY, lightZ, 
        					mActualLightPosition[0], mActualLightPosition[1], mActualLightPosition[2],
        					//lookX, lookY, lookZ,
        					//look in direction -y
        					mActualLightPosition[0], -mActualLightPosition[1], mActualLightPosition[2],
        					//upX, upY, upZ
        					//up vector in the direction of axisY
        					-mActualLightPosition[0], 0, -mActualLightPosition[2]);
        
        //Cube rotation with touch events
        float[] cubeRotationX = new float[16];
        float[] cubeRotationY = new float[16];
        
        Matrix.setRotateM(cubeRotationX, 0, mRotationX, 0, 1.0f, 0); 
        Matrix.setRotateM(cubeRotationY, 0, mRotationY, 1.0f, 0, 0); 
        
        Matrix.multiplyMM(mCubeRotation, 0, cubeRotationX, 0, cubeRotationY, 0);
        
        //------------------------- render depth map --------------------------
        
        // Cull front faces for shadow generation to avoid self shadowing
     	GLES20.glCullFace(GLES20.GL_FRONT); 
        
     	renderShadowMap();
     	
        //------------------------- render scene ------------------------------
        
     	// Cull back faces for normal render
     	GLES20.glCullFace(GLES20.GL_BACK);
     	
     	renderScene();
        
        // Print openGL errors to console
        int debugInfo = GLES20.glGetError();
		
		if (debugInfo != GLES20.GL_NO_ERROR) {
			String msg = "OpenGL error: " + debugInfo;
			Log.w(TAG, msg);
		}
		
    }

    private void renderShadowMap() {
    	// bind the generated framebuffer
    	GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId[0]);
    	
		GLES20.glViewport(0, 0, mShadowMapWidth, 
								mShadowMapHeight);
		
		// Clear color and buffers
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
		// Start using the shader
		GLES20.glUseProgram(mDepthMapProgram.getProgram());

		float[] tempResultMatrix = new float[16];
		
		// Calculate matrices for standing objects
		
		// View matrix * Model matrix value is stored
		Matrix.multiplyMM(mLightMvpMatrix_staticShapes, 0, mLightViewMatrix, 0, mModelMatrix, 0);

		// Model * view * projection matrix stored and copied for use at rendering from camera point of view
		Matrix.multiplyMM(tempResultMatrix, 0, mLightProjectionMatrix, 0, mLightMvpMatrix_staticShapes, 0);
		System.arraycopy(tempResultMatrix, 0, mLightMvpMatrix_staticShapes, 0, 16);
		
		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(shadow_mvpMatrixUniform, 1, false, mLightMvpMatrix_staticShapes, 0);

		// Render all stationary shapes on scene
		mPlane.render(shadow_positionAttribute, 0, 0, true);
		mSmallCube0.render(shadow_positionAttribute, 0, 0, true);
		mSmallCube1.render(shadow_positionAttribute, 0, 0, true);
		mSmallCube2.render(shadow_positionAttribute, 0, 0, true);
		mSmallCube3.render(shadow_positionAttribute, 0, 0, true);
		
		// Calculate matrices for moving objects
		
		// Rotate the model matrix with current rotation matrix
		Matrix.multiplyMM(tempResultMatrix, 0, mModelMatrix, 0, mCubeRotation, 0);
		
		// View matrix * Model matrix value is stored
		Matrix.multiplyMM(mLightMvpMatrix_dynamicShapes, 0, mLightViewMatrix, 0, tempResultMatrix, 0);

		// Model * view * projection matrix stored and copied for use at rendering from camera point of view
		Matrix.multiplyMM(tempResultMatrix, 0, mLightProjectionMatrix, 0, mLightMvpMatrix_dynamicShapes, 0);
		System.arraycopy(tempResultMatrix, 0, mLightMvpMatrix_dynamicShapes, 0, 16);
		
		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(shadow_mvpMatrixUniform, 1, false, mLightMvpMatrix_dynamicShapes, 0);
		
		// Render all moving shapes on scene
		mCube.render(shadow_positionAttribute, 0, 0, true);
	}
    
    private void renderScene() {
    	// bind default framebuffer
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		GLES20.glUseProgram(mActiveProgram);
		
		GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
		
		//pass stepsize to map nearby points properly to depth map texture - used in PCF algorithm
		GLES20.glUniform1f(scene_mapStepXUniform, (float) (1.0 / mShadowMapWidth));
		GLES20.glUniform1f(scene_mapStepYUniform, (float) (1.0/ mShadowMapHeight));
		
        float[] tempResultMatrix = new float[16];
        
        float bias[] = new float [] {
      		  0.5f, 0.0f, 0.0f, 0.0f,
      		  0.0f, 0.5f, 0.0f, 0.0f,
      		  0.0f, 0.0f, 0.5f, 0.0f,
      		  0.5f, 0.5f, 0.5f, 1.0f};
      
        float[] depthBiasMVP = new float[16];
        
        //calculate MV matrix
        Matrix.multiplyMM(tempResultMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, mMVMatrix, 0, 16);
        
        //pass in MV Matrix as uniform
        GLES20.glUniformMatrix4fv(scene_mvMatrixUniform, 1, false, mMVMatrix, 0);
        
        //calculate Normal Matrix as uniform (invert transpose MV)
        Matrix.invertM(tempResultMatrix, 0, mMVMatrix, 0);
        Matrix.transposeM(mNormalMatrix, 0, tempResultMatrix, 0);
        
        //pass in Normal Matrix as uniform
        GLES20.glUniformMatrix4fv(scene_normalMatrixUniform, 1, false, mNormalMatrix, 0);
        
        //calculate MVP matrix
        Matrix.multiplyMM(tempResultMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, mMVPMatrix, 0, 16);
        
        //pass in MVP Matrix as uniform
        GLES20.glUniformMatrix4fv(scene_mvpMatrixUniform, 1, false, mMVPMatrix, 0);
        
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mActualLightPosition, 0);
        //pass in light source position
        GLES20.glUniform3f(scene_lightPosUniform, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
        
        if (mHasDepthTextureExtension){
        	Matrix.multiplyMM(depthBiasMVP, 0, bias, 0, mLightMvpMatrix_staticShapes, 0);
        	System.arraycopy(depthBiasMVP, 0, mLightMvpMatrix_staticShapes, 0, 16);
        }
        
        //MVP matrix that was used during depth map render
        GLES20.glUniformMatrix4fv(scene_schadowProjMatrixUniform, 1, false, mLightMvpMatrix_staticShapes, 0);
        
        //pass in texture where depth map is stored
 		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTextureId[0]);
 		GLES20.glUniform1i(scene_textureUniform, 0);
 		
        mSmallCube0.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false);
        mSmallCube1.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false);
        mSmallCube2.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false);
        mSmallCube3.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false);     
        mPlane.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false);	
        
        // Pass uniforms for moving objects (center cube) which are different from previously used uniforms
        // - MV matrix
        // - MVP matrix
        // - Normal matrix
        // - Light MVP matrix for dynamic objects
        
        // Rotate the model matrix with current rotation matrix
     	Matrix.multiplyMM(tempResultMatrix, 0, mModelMatrix, 0, mCubeRotation, 0);
        
     	//calculate MV matrix
        Matrix.multiplyMM(tempResultMatrix, 0, mViewMatrix, 0, tempResultMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, mMVMatrix, 0, 16);
        
        //pass in MV Matrix as uniform
        GLES20.glUniformMatrix4fv(scene_mvMatrixUniform, 1, false, mMVMatrix, 0);
        
        //calculate Normal Matrix as uniform (invert transpose MV)
        Matrix.invertM(tempResultMatrix, 0, mMVMatrix, 0);
        Matrix.transposeM(mNormalMatrix, 0, tempResultMatrix, 0);
        
        //pass in Normal Matrix as uniform
        GLES20.glUniformMatrix4fv(scene_normalMatrixUniform, 1, false, mNormalMatrix, 0);
        
        //calculate MVP matrix
        Matrix.multiplyMM(tempResultMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        System.arraycopy(tempResultMatrix, 0, mMVPMatrix, 0, 16);
        
        //pass in MVP Matrix as uniform
        GLES20.glUniformMatrix4fv(scene_mvpMatrixUniform, 1, false, mMVPMatrix, 0);
        
        if (mHasDepthTextureExtension){
        	Matrix.multiplyMM(depthBiasMVP, 0, bias, 0, mLightMvpMatrix_dynamicShapes, 0);
        	System.arraycopy(depthBiasMVP, 0, mLightMvpMatrix_dynamicShapes, 0, 16);
        }
        
        //MVP matrix that was used during depth map render
        GLES20.glUniformMatrix4fv(scene_schadowProjMatrixUniform, 1, false, mLightMvpMatrix_dynamicShapes, 0);
        
        mCube.render(scene_positionAttribute, scene_normalAttribute, scene_colorAttribute, false);
	}

    /**
     * Changes render program after changes in menu
     */
    private void setRenderProgram() {
		if (mShadowsActivity.getmShadowType() < 0.5)
			if (mShadowsActivity.getmBiasType() < 0.5)
				mActiveProgram = mSimpleShadowProgram.getProgram();
			else
				mActiveProgram = mSimpleShadowDynamicBiasProgram.getProgram();
		else
			if (mShadowsActivity.getmBiasType() < 0.5)
				mActiveProgram = mPCFShadowProgram.getProgram();
			else
				mActiveProgram = mPCFShadowDynamicBiasProgram.getProgram();	
	}
    
    /**
     * Returns the X rotation angle of the cube.
     *
     * @return - A float representing the rotation angle.
     */
    public float getRotationX() {
        return mRotationX;
    }

    /**
     * Sets the X rotation angle of the cube.
     */
    public void setRotationX(float rotationX) {
        mRotationX = rotationX;
    }
    
    /**
     * Returns the Y rotation angle of the cube.
     *
     * @return - A float representing the rotation angle.
     */
    public float getRotationY() {
        return mRotationY;
    }

    /**
     * Sets the Y rotation angle of the cube.
     */
    public void setRotationY(float rotationY) {
        mRotationY = rotationY;
    }
}
