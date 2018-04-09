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
	
	
	
	//�߰�
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private Uri mImageCaptureUri;
	//�߰� ��
	
	String[] data = new String[9999];
	String[] avergenum = new String[10];//�϶� �κ��� ��ġ�� ���� �׸�

	
	
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
				
				startActivityForResult(i, PICK_FROM_CAMERA);//�߰���
				
				
				
			}
		});
		processButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				resultButton.setEnabled(true);
				Bitmap captureBmp = null;

				long now = System.currentTimeMillis();
				Date date = new Date(now);
				SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // �ð� �������� �����.
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
	                      int red = (value & 0x00FF0000) >> 16;  // ���� red green blue �κ� ���� �ϳ��� �и��ؼ� �� �����Ҽ��ְ��Ѱž�.
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
				for (int i = 0; i < width; i++) { // �Ӱ��� ������.
                    for (int j = 0; j < height; j++) {
                          
                            if (red_h-3<red3[i][j]&& red3[i][j]<=red_h+2) {// �Ӱ�ġ
                               
                         	   
                         	   
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
				
				for (int i = 0; i < width; i++) { // �Ӱ��� ������.
                    for (int j = 0; j < height; j++) {
                            if (gray[i][j] < threshold) {// �Ӱ�ġ
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
								m = m + gray[j][i];//�� ���� ��հ� ����� ���� ����
								mm = mm + gray[j][i];//��ü�� ��հ� ����� ���� ����
						}
						int l = m/width;//�ش� ���� gray�� �� ���� width�� ������.
						data[i] = Integer.toString(l);//�� ���� ��հ� ���
						m = 0;
					}
					mm=mm/(width*height);
					k = mm;
					
					
					//�׳��� ���� ����ϰ� ����//
					//�ִ밪�� �ּҰ��� ����//
					int Max = 0, Min = 255;
					for(int i=0; i<height; i++){
						if(Integer.parseInt(data[i])>Max)
							Max = Integer.parseInt(data[i]);//�ִ밪 ���
						if(Integer.parseInt(data[i])<Min)
							Min = Integer.parseInt(data[i]);//�ּҰ� ���
					}
					int MM = Max - Min;
					MM = (int) (MM * 0.7);//�ִ밪�� �ּҰ� ���� 70%�� ����.
					System.out.println("Max - Min�� 50% : " + MM);
					//
					double wa = 0;//���� 5%�з��� ��հ�
					int HE = (int) (height*0.05);//5%

					for(int i=0; i<HE; i++){
						wa = wa + Double.parseDouble(data[i]);//���� 5%�� ��ġ�� ��
					}
					wa = wa/HE;//5% ��ġ�� ���

					int setnum = 1;//���°������ ����
					int setswitch = 0;//�׷������� �϶��κ� üũ�� ����ġ
					double setaverge = 0;//�϶��� �κ��� ��հ� ����� ���� ��
					int setcount = 0;//�϶��� �κ��� ����
					int setsw = 0;//����� �ϱ� ���� ����

					//String[] avergenum = new String[10];//�϶� �κ��� ��ġ�� ���� �׸�

					for(int i = HE; i<height; i++){//��հ� ��� ������ ������ ���ʷ� ����

						if(setswitch == 1)//����ġ�� 1�̸�
						{

							if(Double.parseDouble(data[i]) + MM > wa){
								//data[i]+MM�� ��հ����� Ŀ���ٸ� => �׷����� ��հ����� Ȥ�� �� �̻����� �ö󰣴ٸ�
								setswitch = 0;//����ġ�� 0���� ����
							}
							else{
								setsw = 1;//����ϱ� ���� ����ġ�� 1�� ����
								setaverge = setaverge + Double.parseDouble(data[i]);//setaverge�� ���� ����
								setcount = setcount + 1;//���� ���ڵ� �Բ� ����
							}

						}
						else{

							if(Double.parseDouble(data[i]) + MM < wa){
								//data[i] + MM�� ���� ���� ��հ����� ���� ���
								// = > �׷����� �϶��ϴ� �κ��̶��
								setswitch = 1;//����ġ�� 1�� �ٲ�.
							}

							if(setsw == 1){//����ϱ� ���� ������ �����ǰ�, setswitch�� 0�̸�
								setsw = 0;//setsw�� �ʱ�ȭ
								int kkkk = (int) ((setaverge/setcount) + 0.5);//��� ���
								avergenum[setnum] = Double.toString(kkkk);//�迭�� ��հ��� ����
								System.out.println("��հ� :" + kkkk);//Ȯ�ο�
								System.out.println("������ ���� : " + i);//Ȯ�ο�2
								setaverge = 0;//���� ����� ���� �ʱ�ȭ
								setcount = 0;//���� ����� ���� �ʱ�ȭ
								setnum = setnum + 1;//���� �迭�� �ֱ����� +1
							}

						}

					}
					
				
				
				

			}
		});
		resultButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(CameraCaptureTest.this, Graph.class);

				
				i.putExtra("Average", k);//��ü ��հ�
				i.putExtra("data", data);//�� ���� gray ��հ�.
				i.putExtra("avergenum", avergenum);//�϶��� �κ��� ��
				startActivity(i);
				
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		
		//������ �߰�//
		if (resultCode == RESULT_OK && requestCode == PICK_FROM_CAMERA){
			// ������ ó���� ī�޶�� �����Ƿ� �ϴ�  break���� �����մϴ�.
            // ���� �ڵ忡���� ���� �ո����� ����� �����Ͻñ� �ٶ��ϴ�.
			
			Log.w("tag", "pick");
			
		
            mImageCaptureUri = intent.getData();
            File original_file = getImageFile(mImageCaptureUri);
             
            mImageCaptureUri = createSaveCropFile();
            File cpoy_file = new File(mImageCaptureUri.getPath()); 
            // SDī�忡 ����� ������ �̹��� Crop�� ���� �����Ѵ�.
            copyFile(original_file , cpoy_file);
            
			Intent pickcamera = new Intent("com.android.camera.action.CROP");
            pickcamera.setDataAndType(mImageCaptureUri, "image/*"); 
            
            
            
            //pickcamera.putExtra("outputX", 100);
            //pickcamera.putExtra("outputY", 100);
            //pickcamera.putExtra("aspectX", 0);
            //pickcamera.putExtra("aspectY", 0);
            pickcamera.putExtra("scale",true);
            pickcamera.putExtra("return-data", true);
            
            // Crop�� �̹����� ������ Path
            pickcamera.putExtra("output", mImageCaptureUri);
            // Return Data�� ����ϸ� ���� �뷮 �������� ũ�Ⱑ ū �̹�����
            // �Ѱ� �� �� ����.
            
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