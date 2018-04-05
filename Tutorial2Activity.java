package org.opencv.samples.tutorial2;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Tutorial2Activity extends Activity implements CvCameraViewListener2 {
    private static final String    TAG = "OCVSample::Activity";

    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;

    public  boolean  			   first = true;
    private int                    mViewMode;
    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;
    private Mat 				   current;
    private Mat 				   previous = null;
    private Mat 				   difference;

    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGray;
    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;
    
    private TextView mTitle;
    private Button mFlashButton;
    
    private int 				scale =  11;    // Number of Roi .... odd Number
    private int[]   gray = new int[scale]; //standard gray value
    
    private Tutorial3View mOpenCvCameraView;  //2.4에서 쓰던 방식 , 그냥 써도 무관
    
    //private CameraBridgeViewBase   mOpenCvCameraView;  //3.0에서 사용하던 방식 
    
    private String text;
    
    // Debugging
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
 // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    private BluetoothChatService mChatService = null;
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    //System.loadLibrary("mixed_sample");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Tutorial2Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
 
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.tutorial2_surface_view);
 
        
        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
           
        
        //3.0에서 사용하는 코드 
        /*
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        */
        
        mTitle = (TextView) findViewById(R.id.textView1);
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    
    
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if (mChatService != null) mChatService.stop();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        previous = new Mat(width, height, CvType.CV_64FC4);        //차프레임 구할때 쓰는 프레임 RESULT BUTTON
        current = new Mat(width, height, CvType.CV_64FC4);         //차프레임 구할때 쓰는 프레임 RESULT BUTTON
        difference = new Mat(width, height, CvType.CV_64FC4);      //차프레임 구할때 쓰는 프레임 RESULT BUTTON  
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
        current.release();
        previous.release();
        difference.release();
    }

    public Mat onCameraFrame(final CvCameraViewFrame inputFrame) {
    	
        Button InitBtn = (Button)findViewById(R.id.button1) ; //플래시 ON
        Button StartBtn = (Button)findViewById(R.id.button2) ; //조리개 고정 
        Button StopBtn = (Button)findViewById(R.id.button3) ;  //모든 기능 OFF
        Button ForwardBtn = (Button)findViewById(R.id.button4) ; //사용 안함
        Button BackwardBtn = (Button)findViewById(R.id.button5) ; //블루투스 팝업메뉴
        Button ResultBtn = (Button)findViewById(R.id.button6) ; //차프레임
        
        InitBtn.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mViewMode = VIEW_MODE_RGBA;
				mOpenCvCameraView.flashOn();							
			}
			});
        
        StartBtn.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mViewMode = VIEW_MODE_GRAY;
				mOpenCvCameraView.SetAutoWhiteBalanceLock(true);
				mOpenCvCameraView.SetAutoExposureLock(true);
			}
		});
        
        StopBtn.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// TODO Auto-generated method stub
        		  mViewMode = VIEW_MODE_RGBA;		
        		  mOpenCvCameraView.flashOff();
  				  mOpenCvCameraView.SetAutoExposureLock(false);    //조리개 고정
  				  first = true;
        		  
        		  if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED){
  		            String con = new String("");         
  		            
  		          try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
  					sendMessage(con); //아두이노로 메세지 전송 (해당 코드에서는 사용안함)
  		            }	            
		}
        });
        
        ForwardBtn.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// TODO Auto-generated method stub
        		mViewMode = VIEW_MODE_RGBA;
        		
        		if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED){
  		            String con = new String("");
  		          try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
  					sendMessage(con); //아두이노로 메세지 전송 (해당 코드에서는 사용안함)
  		            }
        	}
        });
        
        BackwardBtn.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// TODO Auto-generated method stub
        		//mViewMode = VIEW_MODE_RGBA;
        		openOptionsMenu(); //블루투스 메뉴 팝업
        	}
        });
        
        ResultBtn.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// TODO Auto-generated method stub
        		mViewMode = VIEW_MODE_CANNY;   
        	}
        });
        
    	final int viewMode = mViewMode;
    	
    	Scalar color = new Scalar(255,255,255); 	   	    
   	    int length = 400; // length of Roibar 가로길이
   	    int center = 750; // Center of screen 가로 중심 위치
   	          	    
   	    int height = 10; //Roi width 세로길이
   	    int width = length/scale; //Each Roi's width
   	    
   	    int startX = center-(length/2)-(height/2); //가로 시작위치
   	    int startY = 400;                          //세로위치
   	    
   	    Rect[] roibars = new Rect[scale]; 	
   	    int[][] avgRGB = new int[scale][4];
   	     	    
   	    for(int i=0; i<scale; i++) 
   		roibars[i] = new Rect(startX+(i*width), startY ,width, height); //Roibar 생성
   	    
   	    int threshold = 10; //용액 변화 감지 문턱값 
   	    int count = 0;      
  
        switch (viewMode) {  //START
        case VIEW_MODE_GRAY:   
  	     		
        	mRgba = inputFrame.rgba();
        	
       	    for(int i=0; i<scale; i++) 	
       		for(int j=0;j<4;j++)        			
           	    avgRGB[i][j] = calcRGB(roibars[i])[j]; //Roibar의 RGB Gray값을 계산하고 2차원 배열에 저장   
       	    
       	    
       	    if(first) {    	//기준이될 Gray RGB값 저장
       	    int[] temp = Getcol(avgRGB,3);
       	    for(int i=0; i<scale; i++)  
       	    gray[i] = temp[i];
       	    
       	    first = false;
       	    return mRgba; //기준값 저장후 현재 프레임에서 할일이 없음
       	    }
       	    
       	     	    
       	    int[] temp = Getcol(avgRGB,3);  //기준값 저장 이후 프레임의 gray값
       	    
       	    for(int i=0; i<scale; i++)  {	
       	    temp[i] = Math.abs(gray[i]-temp[i]); //기준값 대비 차이     	    
       	    Imgproc.rectangle(mRgba, new Point(roibars[i].x,roibars[i].y), new Point(roibars[i].x+width,roibars[i].y+height), color, 1); //test용으로 출력 (지우면 처리속도 증가)
    	    Imgproc.putText(mRgba,new String("g"+temp[i]+" "), new Point(roibars[i].x,roibars[i].y-80+200*(i%2)), 1,2,color,2);
    	    
    	    if(temp[i]>=threshold) //roi 변화를 체킹하는 부분
    	    	count++;    	    	
       	    }
       	    
       	    
       	   double ratio = (double)count/(double)scale; //값이 변한 비율 계산      	    
       	    
       	    

       	 if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED)  //아두이노로 전송
       		sendMessage(ratio+"\n");
 		 
            break;
            
        case VIEW_MODE_RGBA:  //STOP, FORWARD, BACKWARD 
            mRgba = inputFrame.rgba();
            break;
           
        case VIEW_MODE_CANNY: //RESULT         
        	
        	
        	current = inputFrame.rgba();
        	
        	if(first){//first is true at the first time
        		current.copyTo(previous);
                first = false;
            }
        	        	
            Core.absdiff(current, previous, difference);   

            current.copyTo(previous);
            
            try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            mRgba = difference;
           	
            break;                       
        }

        return mRgba;       
    }
    
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    
	public int[] Getcol(int[][] arr, int col) //2차원 배열에서 특정 열을 1차원 배열로 리턴
	{
		int length = arr.length;		
		int[] arr1 = new int[length];
		
		for(int i=0;i<length;i++)
		arr1[i] = arr[i][col];
		
		return arr1;
	}
	
	public int[] calcRGB(Rect Roi)   //Roi를 전달받아서 RGB Gray 평균값을 계산하고 배열을 리턴 [R G B Gray]
	{
		int avg1=0,avg2=0,avg3=0,avg4=0;
		
	 for(int i = Roi.x; i< Roi.x+Roi.width ;i++)
    	for(int j = Roi.y; j<Roi.y+Roi.height;j++)
    	{
            double[] rgbV = mRgba.get(j, i);
            avg1 += rgbV[0] ;
            avg2 += rgbV[1] ;
            avg3 += rgbV[2] ;
    	}
     avg1 /= (Roi.width * Roi.height) ;    //RED
     avg2 /= (Roi.width * Roi.height) ;    //Green
     avg3 /= (Roi.width * Roi.height) ;    //BLUE
     avg4 = (avg1+avg2+avg3)/3;
    	
     int arr[] = {avg1,avg2,avg3,avg4}; //RED,Green,Blue,Gray
     
     return arr;
	}	
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private void setupChat() {
        Log.d(TAG, "setupChat()");
        
        mFlashButton = (Button) findViewById(R.id.button5);
        mFlashButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {        	
            	
            }});

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	 switch (item.getItemId()) {
         case R.id.scan:
             // Launch the DeviceListActivity to see devices and do scan
             Intent serverIntent = new Intent(this, DeviceListActivity.class);
             startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
             return true;      
         }
         return false;
    }
    
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
    	byte[] send = message.getBytes();
        mChatService.write(send);
    }
    
    //아두이노에서 메세지를 받았을 경우 처리하는 부분 테스트 안 해봄
    /*                           
    private byte readMessage() {   	
    	byte readBuf = msg.obj;
    	String readMessage = new String(readBuf, 0, msg.arg1);
    	
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return -1; //error
        }        
        return  mChatService.
        // Check that there's actually something to send
    }
    */
    

    //유사한 특징을 가지는 블록을 찾는 함수 jni에서 처리되는 부분 (사용안함)
    //public native void FindFeatures(long matAddrGr, long matAddrRgba);
    //public native void Labling(long matAddrGr, long matAddrRgba);    
}


