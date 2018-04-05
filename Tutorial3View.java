package org.opencv.samples.tutorial2;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

public class Tutorial3View extends JavaCameraView{
	private Camera.Parameters params;
    public Tutorial3View(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void flashOn(){                     //플래시를 켜는 함수
        params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(params);
    }
    public void flashOff(){                    //플래시를 끄는 함수
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
    }
    
    public void SetAutoWhiteBalanceLock(boolean set)       //화이트 밸런스를 끄는 함수
    {
    	Camera.Parameters params = mCamera.getParameters();
        params.setAutoWhiteBalanceLock(set);
        mCamera.setParameters(params);
    }
     
    public void SetAutoExposureLock(boolean set)           //조리개를 끄는 함수
    {
    	Camera.Parameters params = mCamera.getParameters();
        params.setAutoExposureLock(set);
        mCamera.setParameters(params);
    }
   
       
}
