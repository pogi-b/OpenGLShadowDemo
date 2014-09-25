package com.projectI.openglshadowdemo;

import com.example.openglshadowdemo.R;

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
		float shadowRatio = 0.0f;
		
	    switch (item.getItemId()) {
	        case R.id.shadow_type_simple:
	            renderer.setmShadowType(0.0f);
	        	item.setChecked(true);
	        	return true;
	        case R.id.shadow_type_pcf:
	        	renderer.setmShadowType(1.0f);
	        	item.setChecked(true);
	            return true;
	        case R.id.bias_type_constant:
	        	renderer.setmBiasType(0.0f);
	        	item.setChecked(true);
	        	return true;
	        case R.id.bias_type_dynamic:
	        	renderer.setmBiasType(1.0f);
	        	item.setChecked(true);
	        	return true;
	        case R.id.depth_map_size_0:
	        	renderer.setmShadowMapRatio(0.5f);
	        	
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
	        	renderer.setmShadowMapRatio(1.0f);
	        	
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
	        	renderer.setmShadowMapRatio(1.5f);
	        	
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
	        	renderer.setmShadowMapRatio(2.0f);
	        	
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
}
