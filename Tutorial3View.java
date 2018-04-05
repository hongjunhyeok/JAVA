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

    public void flashOn(){                     //�÷��ø� �Ѵ� �Լ�
        params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(params);
    }
    public void flashOff(){                    //�÷��ø� ���� �Լ�
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
    }
    
    public void SetAutoWhiteBalanceLock(boolean set)       //ȭ��Ʈ �뷱���� ���� �Լ�
    {
    	Camera.Parameters params = mCamera.getParameters();
        params.setAutoWhiteBalanceLock(set);
        mCamera.setParameters(params);
    }
     
    public void SetAutoExposureLock(boolean set)           //�������� ���� �Լ�
    {
    	Camera.Parameters params = mCamera.getParameters();
        params.setAutoExposureLock(set);
        mCamera.setParameters(params);
    }
   
       
}
