package com.projectI.openglshadowdemo;

import com.projectI.openglshadowdemo.R;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ShadowsActivity extends Activity {

    private ShadowsGLSurfaceView mGLView;
    private ShadowsRenderer renderer;
	/**
	 * Type of shadow bias to reduce unnecessary shadows
	 * 	- constant bias
	 * 	- bias value is variable according to slope
	 */
	private float mBiasType = 0.0f;
	/**
	 * Type of shadow algorithm
	 * 	- simple shadow (shadow value is only two state (yes/no) so aliasing is visible, no blur effect is possible)
	 *  - Percentage Closer Filtering (PCF)
	 */
	private float mShadowType = 0.0f;
	/**
	 * Shadow map size: 
	 * 	- displayWidth * SHADOW_MAP_RATIO
	 * 	- displayHeight * SHADOW_MAP_RATIO			
	 */
	private float mShadowMapRatio = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = new ShadowsGLSurfaceView(this);
        
        // Create an OpenGL ES 2.0 context.
        mGLView.setEGLContextClientVersion(2);
        
		renderer = new ShadowsRenderer(this);
		mGLView.setRenderer(renderer);
        
        setContentView(mGLView);
        
        Toast.makeText(this, R.string.user_hint, Toast.LENGTH_SHORT).show();
    }

    /*
	 * Creates the menu and populates it via xml
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.opengl_shadow_menu, menu);
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
	    switch (item.getItemId()) {
	        case R.id.shadow_type_simple:
	            this.setmShadowType(0.0f);
	        	item.setChecked(true);
	        	return true;
	        case R.id.shadow_type_pcf:
	        	this.setmShadowType(1.0f);
	        	item.setChecked(true);
	            return true;
	        case R.id.bias_type_constant:
	        	this.setmBiasType(0.0f);
	        	item.setChecked(true);
	        	return true;
	        case R.id.bias_type_dynamic:
	        	this.setmBiasType(1.0f);
	        	item.setChecked(true);
	        	return true;
	        case R.id.depth_map_size_0:
	        	this.setmShadowMapRatio(0.5f);
	        	
	        	// we need to run opengl calls on GLSurface thread
	        	mGLView.queueEvent(new Runnable() {
	                @Override
	                public void run() {
	                	renderer.generateShadowFBO();
	                }
	            });
	        	
	        	item.setChecked(true);
	        	return true;
	        case R.id.depth_map_size_1:
	        	this.setmShadowMapRatio(1.0f);
	        	
	        	// we need to run opengl calls on GLSurface thread
	        	mGLView.queueEvent(new Runnable() {
	                @Override
	                public void run() {
	                	renderer.generateShadowFBO();
	                }
	            });
	        	
	        	item.setChecked(true);
	        	return true;
	        case R.id.depth_map_size_2:
	        	this.setmShadowMapRatio(1.5f);
	        	
	        	// we need to run opengl calls on GLSurface thread
	        	mGLView.queueEvent(new Runnable() {
	                @Override
	                public void run() {
	                	renderer.generateShadowFBO();
	                }
	            });
	        	
	        	item.setChecked(true);
	        	return true;
	        case R.id.depth_map_size_3:
	        	this.setmShadowMapRatio(2.0f);
	        	
	        	// we need to run opengl calls on GLSurface thread
	        	mGLView.queueEvent(new Runnable() {
	                @Override
	                public void run() {
	                	renderer.generateShadowFBO();
	                }
	            });
	        	
	        	item.setChecked(true);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
    
	public float getmBiasType() {
		return mBiasType;
	}

	private void setmBiasType(float mBiasType) {
		this.mBiasType = mBiasType;
	}

	public float getmShadowType() {
		return mShadowType;
	}

	private void setmShadowType(float mShadowType) {
		this.mShadowType = mShadowType;
	}

	public float getmShadowMapRatio() {
		return mShadowMapRatio;
	}

	private void setmShadowMapRatio(float mShadowMapRatio) {
		this.mShadowMapRatio = mShadowMapRatio;
	}
}
