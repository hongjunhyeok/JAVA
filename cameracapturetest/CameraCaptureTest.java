package com.example.cameracapturetest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;



public class CameraCaptureTest extends Activity {
	private static final int CAMERA_CAPTURE = 0;
	public static int count = 0;
	int whiteValue = 0;
	int blackValue = 255;
	int red_h = 253;
	int green_h = 3;
	int blue_h = 3;
	final static int threshold = 40;
	
	
	
	//추가
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private Uri mImageCaptureUri;
	//추가 끝
	
	String[] data = new String[9999];
	String[] avergenum = new String[10];//하락 부분의 수치를 담을 그릇

	
	
	int k;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		Button captureButton = (Button) findViewById(R.id.capture);
		Button processButton = (Button) findViewById(R.id.process);
		final Button resultButton = (Button) findViewById(R.id.result);
		resultButton.setEnabled(false);

		
		captureButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
				
				startActivityForResult(i, PICK_FROM_CAMERA);//추가값
				
				
				
			}
		});
		processButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				resultButton.setEnabled(true);
				Bitmap captureBmp = null;

				long now = System.currentTimeMillis();
				Date date = new Date(now);
				SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 시간 포맷으로 만든다.
				String strNow = sdfNow.format(date);
				
				
				System.out.println(strNow);
			
				File file = new File("/sdcard/image.jpg");
				try {
					captureBmp = Images.Media.getBitmap(getContentResolver(),
							Uri.fromFile(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				int width = captureBmp.getWidth();
				int height = captureBmp.getHeight();
				int [][] gray = new int [width][height];
				int [][] red3 = new int [width][height];
				int [][] green3 = new int [width][height];
				int [][] blue3 = new int [width][height];
				Bitmap tmpBmp = captureBmp.copy(Bitmap.Config.ARGB_8888, true);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int value = captureBmp.getPixel(x, y);
						 int alpha = ( value & 0xFF000000 ); 
	                      int red = (value & 0x00FF0000) >> 16;  // 여기 red green blue 부분 보면 하나씩 분리해서 값 저장할수있게한거야.
	                      int green = (value & 0x0000FF00) >>  8;
	                      int blue = (value & 0x000000FF);
	                      double red2 = red * 0.2125;
	                      double green2 = green * 0.7154;
	                      double blue2 = blue * 0.0721;
	                      int newRGB = (int)( red2+green2+blue2 );///3
	                      int eachNewRGB = alpha | (newRGB << 16) | (newRGB << 8) | newRGB;
	                      gray[x][y] = newRGB;
	                      red3[x][y] = red;
	                      green3[x][y] = green;
	                      blue3[x][y] = blue;
	                      tmpBmp.setPixel(x, y, eachNewRGB);
	                      
	                      
						
					}
				}
				for (int i = 0; i < width; i++) { // 임계차 구했음.
                    for (int j = 0; j < height; j++) {
                          
                            if (red_h-3<red3[i][j]&& red3[i][j]<=red_h+2) {// 임계치
                               
                         	   
                         	   
                         	   if(green_h-3<green3[i][j]&&green3[i][j]<=green_h+2)
                         	   {
                         		   if(blue_h-3<blue3[i][j]&&blue3[i][j]<=blue_h+2)
                         		   {
                         			  tmpBmp.setPixel(i, j, 0xff000000);
                                		count++;
                         		   }
                         	   }
                                                                


                        }
                            else {
                            	tmpBmp.setPixel(i, j, 0xffffffff);
                            }
                    }
                   
            }
				
				for (int i = 0; i < width; i++) { // 임계차 구했음.
                    for (int j = 0; j < height; j++) {
                            if (gray[i][j] < threshold) {// 임계치
                                    tmpBmp.setPixel(i, j, 0xff000000);
                                    count++;


                            } else {
                                    tmpBmp.setPixel(i, j, 0xffffffff);
                            }
                    }
            }
            
				
				//String[] data = new String[height];
				
				ImageView imgView = (ImageView) findViewById(R.id.imageview);
				imgView.setImageBitmap(tmpBmp);
				
				
				int m = 0;
				int mm = 0;
				int jj = 0;
				for(int k=0; k<height; k++){
					   data[k] = Integer.toString(0);
				}
					
					for(int i = 0; i<height; i++){
						for(int j=0; j<width; j++){
								m = m + gray[j][i];//그 줄의 평균값 계산을 위한 변수
								mm = mm + gray[j][i];//전체의 평균값 계산을 위한 변수
						}
						int l = m/width;//해당 줄의 gray값 총 합을 width로 나눈다.
						data[i] = Integer.toString(l);//각 줄의 평균값 계산
						m = 0;
					}
					mm=mm/(width*height);
					k = mm;
					
					
					//그나마 가장 비슷하게 나옴//
					//최대값과 최소값을 구함//
					int Max = 0, Min = 255;
					for(int i=0; i<height; i++){
						if(Integer.parseInt(data[i])>Max)
							Max = Integer.parseInt(data[i]);//최대값 계산
						if(Integer.parseInt(data[i])<Min)
							Min = Integer.parseInt(data[i]);//최소값 계산
					}
					int MM = Max - Min;
					MM = (int) (MM * 0.7);//최대값과 최소값 차의 70%를 구함.
					System.out.println("Max - Min의 50% : " + MM);
					//
					double wa = 0;//앞의 5%분량의 평균값
					int HE = (int) (height*0.05);//5%

					for(int i=0; i<HE; i++){
						wa = wa + Double.parseDouble(data[i]);//앞의 5%의 수치의 합
					}
					wa = wa/HE;//5% 수치의 평균

					int setnum = 1;//몇번째인지를 측정
					int setswitch = 0;//그래프에서 하락부분 체크용 스위치
					double setaverge = 0;//하락한 부분의 평균값 계산을 위한 합
					int setcount = 0;//하락한 부분의 숫자
					int setsw = 0;//계산을 하기 위한 변수

					//String[] avergenum = new String[10];//하락 부분의 수치를 담을 그릇

					for(int i = HE; i<height; i++){//평균값 계산 이후의 범위를 차례로 증가

						if(setswitch == 1)//스위치가 1이면
						{

							if(Double.parseDouble(data[i]) + MM > wa){
								//data[i]+MM이 평균값보다 커진다면 => 그래프가 평균값수준 혹은 그 이상으로 올라간다면
								setswitch = 0;//스위치를 0으로 변경
							}
							else{
								setsw = 1;//계산하기 위한 스위치를 1로 변경
								setaverge = setaverge + Double.parseDouble(data[i]);//setaverge에 값을 더함
								setcount = setcount + 1;//나눌 숫자도 함께 더함
							}

						}
						else{

							if(Double.parseDouble(data[i]) + MM < wa){
								//data[i] + MM의 값이 앞의 평균값보다 작은 경우
								// = > 그래프가 하락하는 부분이라면
								setswitch = 1;//스위치를 1로 바꿈.
							}

							if(setsw == 1){//계산하기 위한 조건이 만족되고, setswitch가 0이면
								setsw = 0;//setsw는 초기화
								int kkkk = (int) ((setaverge/setcount) + 0.5);//평균 계산
								avergenum[setnum] = Double.toString(kkkk);//배열에 평균값을 넣음
								System.out.println("평균값 :" + kkkk);//확인용
								System.out.println("끝나는 구간 : " + i);//확인용2
								setaverge = 0;//다음 계산을 위한 초기화
								setcount = 0;//다음 계산을 위한 초기화
								setnum = setnum + 1;//다음 배열에 넣기위해 +1
							}

						}

					}
					
				
				
				

			}
		});
		resultButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(CameraCaptureTest.this, Graph.class);

				
				i.putExtra("Average", k);//전체 평균값
				i.putExtra("data", data);//각 줄의 gray 평균값.
				i.putExtra("avergenum", avergenum);//하락한 부분의 값
				startActivity(i);
				
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		
		//밑으로 추가//
		if (resultCode == RESULT_OK && requestCode == PICK_FROM_CAMERA){
			// 이후의 처리가 카메라와 같으므로 일단  break없이 진행합니다.
            // 실제 코드에서는 좀더 합리적인 방법을 선택하시기 바랍니다.
			
			Log.w("tag", "pick");
			
		
            mImageCaptureUri = intent.getData();
            File original_file = getImageFile(mImageCaptureUri);
             
            mImageCaptureUri = createSaveCropFile();
            File cpoy_file = new File(mImageCaptureUri.getPath()); 
            // SD카드에 저장된 파일을 이미지 Crop을 위해 복사한다.
            copyFile(original_file , cpoy_file);
            
			Intent pickcamera = new Intent("com.android.camera.action.CROP");
            pickcamera.setDataAndType(mImageCaptureUri, "image/*"); 
            
            
            
            //pickcamera.putExtra("outputX", 100);
            //pickcamera.putExtra("outputY", 100);
            //pickcamera.putExtra("aspectX", 0);
            //pickcamera.putExtra("aspectY", 0);
            pickcamera.putExtra("scale",true);
            pickcamera.putExtra("return-data", true);
            
            // Crop한 이미지를 저장할 Path
            pickcamera.putExtra("output", mImageCaptureUri);
            // Return Data를 사용하면 번들 용량 제한으로 크기가 큰 이미지는
            // 넘겨 줄 수 없다.
            
            startActivityForResult(pickcamera, CROP_FROM_CAMERA);
			
		}
		else if(resultCode == RESULT_OK && requestCode == CROP_FROM_CAMERA){
			Log.w("tag", "crop");
			
            ImageView imgView = (ImageView) findViewById(R.id.imageview);
      	
            Bitmap testBmp = null;
                   
        	File file = new File("/sdcard/image.jpg");
        		
        	try {
        		testBmp = Images.Media.getBitmap(getContentResolver(),
        						Uri.fromFile(file));
        		
        	} catch (FileNotFoundException e) {
        		e.printStackTrace();
        				
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        	imgView.setImageBitmap(testBmp);
        		
            mImageCaptureUri = null;
           
		}
		
		
	}	
	
	
	
	
	 private Uri createSaveCropFile(){
	        Uri uri;

	        String url = "image.jpg";
	        uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
	        
	        return uri;
	    }
	
	 private File getImageFile(Uri uri) {
	        String[] projection = { MediaStore.Images.Media.DATA };
	        if (uri == null) {
	            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	        }
	 
	        Cursor mCursor = getContentResolver().query(uri, projection, null, null, 
	                MediaStore.Images.Media.DATE_MODIFIED + " desc");
	        if(mCursor == null || mCursor.getCount() < 1) {
	            return null; // no cursor or no record
	        }
	        int column_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        mCursor.moveToFirst();
	 
	        String path = mCursor.getString(column_index);
	 
	        if (mCursor !=null ) {
	            mCursor.close();
	            mCursor = null;
	        }
	 
	        return new File(path);
	    }
	
	 
	 
	 public static boolean copyFile(File srcFile, File destFile) {
	        boolean result = false;
	        try {
	            InputStream in = new FileInputStream(srcFile);
	            try {
	                result = copyToFile(in, destFile);
	            } finally  {
	                in.close();
	            }
	        } catch (IOException e) {
	            result = false;
	        }
	        return result;
	    }
	 
	    /**
	     * Copy data from a source stream to destFile.
	     * Return true if succeed, return false if failed.
	     */
	    private static boolean copyToFile(InputStream inputStream, File destFile) {
	        try {
	            OutputStream out = new FileOutputStream(destFile);
	            try {
	                byte[] buffer = new byte[4096];
	                int bytesRead;
	                while ((bytesRead = inputStream.read(buffer)) >= 0) {
	                    out.write(buffer, 0, bytesRead);
	                }
	            } finally {
	                out.close();
	            }
	            return true;
	        } catch (IOException e) {
	            return false;
	        }
	    }
}