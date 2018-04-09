package com.example.cameracapturetest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Graph extends Activity {

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  private XYSeries mCurrentSeries;
	  private XYSeriesRenderer mCurrentRenderer;
	  
	  private GraphicalView mChartView;
	  
	  //아래는 데이터 전송//
	  
	  HttpPost po;
	  HttpResponse re;
	  HttpClient cl;
	  List<NameValuePair> nameValuePairs;
	  ProgressDialog dialog = null;
	   
	//private static final String SERVER_ADDRESS = "http://175.214.102.177:8080";
		  
	  //데이터전송//

	  int num=-1;
	  int size=-1;
	  int a1, a2;
	  
	  //그래프 이미지 이름
	  
	  Date date;//현재 시간, 날짜를 저장할 date 변수
	  String strNow;//위의 date를 string에 넣기 위한 변수
	  
	  String[] avergenum = new String[10];//하락 부분의 수치를 담을 그릇

	  @Override
	  protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    // save the current data, for instance when changing screen orientation
	    outState.putSerializable("dataset", mDataset);
	    outState.putSerializable("renderer", mRenderer);
	    outState.putSerializable("current_series", mCurrentSeries);
	    outState.putSerializable("current_renderer", mCurrentRenderer);
	  }

	  @Override
	  protected void onRestoreInstanceState(Bundle savedState) {
	    super.onRestoreInstanceState(savedState);
	    // restore the current data, for instance when changing the screen
	    // orientation
	    mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
	    mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
	    mCurrentSeries = (XYSeries) savedState.getSerializable("current_series");
	    mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
	  }
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.graph);
	
	    // TODO Auto-generated method stub
	    
	    Button VG_BT = (Button)findViewById(R.id.data);//그래프 그리기.
		final Button DATA_BT = (Button)findViewById(R.id.graph);//그래프 생성.
		final Button dataleave = (Button)findViewById(R.id.dataleave);//데이터 전송
		 
		final Button graphsave = (Button)findViewById(R.id.graphsave);//그래프 이미지 저장
		graphsave.setEnabled(false);
		
		final Intent i;
		i = getIntent();//인턴트를 받아옴
		
		final TextView avg1 = (TextView)findViewById(R.id.textView1);
		final TextView avg2 = (TextView)findViewById(R.id.textView2);
		avg1.setText("1st Crop Averge is : ");
		avg2.setText(""); 
		
		// 그래프 속성 설정. 색이라던가 크기라던가 그런것들..
	    mRenderer.setApplyBackgroundColor(true);
	    mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
	    mRenderer.setAxisTitleTextSize(16);
	    mRenderer.setChartTitleTextSize(20);
	    mRenderer.setLabelsTextSize(20);
	    mRenderer.setLegendTextSize(20);
	    mRenderer.setYAxisMax(250);//Y축 최대값 250
	    mRenderer.setYAxisMin(50);//Y축 최소값 50
	    mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
	    mRenderer.setZoomButtonsVisible(false);
	    mRenderer.setZoomEnabled(false, false);//확대 방지
	    mRenderer.setZoomEnabled(false);//확대 방지
	    mRenderer.setPanEnabled(false);//그래프 뷰 이동 방지
	    mRenderer.setPointSize(5);
	    
	    
	    DATA_BT.setOnClickListener(new View.OnClickListener() {
		      public void onClick(View v) {
		        String seriesTitle = "Crop Image Gray Value";
		        // create a new series of data
		        XYSeries series = new XYSeries(seriesTitle);
		        mDataset.addSeries(series);
		        mCurrentSeries = series;
		        // create a new renderer for the new series
		        XYSeriesRenderer renderer = new XYSeriesRenderer();
		        mRenderer.addSeriesRenderer(renderer);
		        // set some renderer properties
		        renderer.setPointStyle(PointStyle.CIRCLE);
		        renderer.setFillPoints(true);
		        renderer.setDisplayChartValues(true);
		        renderer.setDisplayChartValuesDistance(10);
		        
		        Random mRandom = new Random();
		        int color = mRandom.nextInt(5);
		        
		        switch(color){
	        	case 0 :
	        		renderer.setColor(Color.YELLOW);
	        		break;
	        	case 1 :
	        		renderer.setColor(Color.BLUE);
	        		break;
	        	case 2 :
	        		renderer.setColor(Color.GREEN);
	        		break;
	        	case 3 :
	        		renderer.setColor(Color.WHITE);
	        		break;
	        	case 4 :
	        		renderer.setColor(Color.CYAN);
	        		break;
	        }
		        mCurrentRenderer = renderer;
		        setSeriesWidgetsEnabled(true);
		        mChartView.repaint();
		       
		        DATA_BT.setEnabled(false);
		        
		      }
		    });
	   
	    VG_BT.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		Bitmap captureBmp = null;
				File file = new File("/sdcard/image.jpg");
				try {
					captureBmp = Images.Media.getBitmap(getContentResolver(),
							Uri.fromFile(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					
				} catch (IOException e) {
					e.printStackTrace();
				}

				int height = captureBmp.getHeight();//이미지의 가로 크기를 받아오기 위함.(그래프의 x축)
				
				graphsave.setEnabled(true);//버튼 활성화
	    		int pp1;
	        double x = 0;
	        double y = 0;
	        int average = i.getIntExtra("Average", 0);//평균값의 인턴트를 받아옴

		    String[] data = new String[height];//이미지의 가로 크기만큼의 data array선언
	
		    data = i.getStringArrayExtra("data");//data어레이에 전의 값은 넣음.
		    pp1 = average;
		    
		    avergenum = i.getStringArrayExtra("avergenum");//하락부분의 값을 받음.
		    
		    System.out.println("평균값 :" + avergenum[1] + " " + avergenum[2]);
		    Log.w("testest", "testest");
		    
		    a1 = average;
		   
		    for(int j=0; j<height; j++){//height만큼 반복함.
	    	try {
		          x = x+1;//이부분은 x축을 1씩 추가하는 부분.
		        } catch (NumberFormatException e) {
		          
		          return;
		        }
		        try {
		        	y =  Double.parseDouble(data[j].toString());
		        	//y축에 입력한 값들을 넣는 부분. Array. for문으로써 입력한 수만큼 출력.
		        } catch (NumberFormatException e) {
		          
		          return;
		        }
		        // add a new data point to the current series
		        mCurrentSeries.add(x, y);
		        mChartView.repaint();
		        }
		        avg1.setText("Crop Averge is : " + pp1);
	    	}
	    });
	    graphsave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 자동 생성된 메소드 스텁
				graphsave.setEnabled(false);
				dataleave.setEnabled(true);//버튼 활성화
				
				//그래프 이름
				long now = System.currentTimeMillis();//1970년 1월 1일부터 경과한 시간을  long 값으로 1/1000초로 받는다.
				date = new Date(now);//위에 저장된 시간단위를 현재의 시간단위로 저장한다.
				SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHHmmss"); // 시간 포맷으로 만든다.
				strNow = sdfNow.format(date);//시간포맷으로 바꾼 값을 string변수로 다시 변환한다.
				
				//그래프이름 종료
				RelativeLayout savegraph = (RelativeLayout)findViewById(R.id.graphview);
				
				int width_container = savegraph.getWidth() ;//캡쳐할 레이아웃 크기
				int height_container = savegraph.getHeight() ;//캡쳐할 레이아웃 크기
				 
				savegraph.setDrawingCacheEnabled(true);
				savegraph.buildDrawingCache(true);
				
				Bitmap captureView = Bitmap.createBitmap(savegraph.getMeasuredWidth(), savegraph.getMeasuredHeight(), Bitmap.Config.ARGB_8888); 
						 
				Canvas screenShotCanvas = new Canvas(captureView ); 
						 
				savegraph.draw(screenShotCanvas); 
				
				FileOutputStream fos;
		        try {
		            fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/"+strNow+".jpg");
		            captureView.compress(Bitmap.CompressFormat.JPEG, 30, fos);
		            System.out.println(strNow);
					
		            savegraph.setDrawingCacheEnabled(false);    
		        } catch (FileNotFoundException e) {
		            e.printStackTrace();
		        }
		        Toast.makeText(getApplicationContext(), "Captured!", Toast.LENGTH_LONG).show();
				
			}
		});
	    
	    //아래 데이터 전송/
	    
	    dataleave.setEnabled(false);
	    dataleave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 자동 생성된 메소드 스텁
				dialog = ProgressDialog.show(Graph.this,"", "전송 시작", true);
				
				new Thread(new Runnable(){
					public void run() {
						Looper.prepare();
						
						ftp();
						login();
						
						Looper.loop();
					}
				}).start();
			}
		});
	    
	    //전송 끝
	  }

	  @Override
	  protected void onResume() {
	    super.onResume();
	    if (mChartView == null) {
	      RelativeLayout layout = (RelativeLayout) findViewById(R.id.graphview);
	      mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
	      // enable the chart click events
	      mRenderer.setClickEnabled(false);
	      mRenderer.setSelectableBuffer(10);
	      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
	          LayoutParams.FILL_PARENT));
	      boolean enabled = mDataset.getSeriesCount() > 0;
	      setSeriesWidgetsEnabled(enabled);
	    } else {
	      mChartView.repaint();
	    }
	  }

	  /**
	   * Enable or disable the add data to series widgets
	   * 
	   * @param enabled the enabled state
	   */
	  private void setSeriesWidgetsEnabled(boolean enabled) {
		Button VG_BT = (Button)findViewById(R.id.data);
		Button DATA_BT = (Button)findViewById(R.id.graph);
	    VG_BT.setEnabled(enabled);
	  }
	  
	  void ftp(){//그림 파일을 올리기 위한 ftp 부분
		  try {
				 String files = strNow + ".jpg";

			     FTPClient mFTP = new FTPClient();

			     mFTP.setControlEncoding("euc-kr"); // 한글 encoding....
			     FTPClientConfig config = new FTPClientConfig();
			     mFTP.configure(config);
			     
			     try{
			     mFTP.connect("", 21);  // ftp로 접속 
			     mFTP.login("",""); // ftp 로그인 계정/비번
			     }
			     catch(SocketException e){
			    	 e.printStackTrace();
			     }
			     catch(IOException e){
			    	 e.printStackTrace();
			     }
			     mFTP.setFileType(FTP.BINARY_FILE_TYPE); // 바이너리 파일
			     mFTP.setBufferSize(983 * 721); // 버퍼 사이즈 
			     mFTP.enterLocalPassiveMode(); //패시브 모드로 접속

			     System.out.println("send");
					
			     File graphimage = new File(Environment.getExternalStorageDirectory().toString()+"/"+strNow + ".jpg");//"/capture.jpg"); // 업로드 할 파일이 있는 경로(예제는 sd카드 사진 폴더)
			     InputStream in = null;
			     
			     try{
			    	 in = new FileInputStream(graphimage);
			    	 Log.w("sdsdsd", "sdsdsd");
			    	 mFTP.storeFile("html/"+files, in);
			    	 //mFTP.appendFile("../" + files, in);
			    	 Log.w("success", "success");
			    	 System.out.println(in);
			     }
			     catch(FileNotFoundException e){
			    	 e.printStackTrace();
			     }
			     catch(IOException e){
			    	 e.printStackTrace();
			     }
			     finally{
			    	 try{
			    		 in.close();
			    	 }
			    	 catch(IOException e){
			    		 e.printStackTrace();
			    	 }
			     }
			     mFTP.disconnect(); // ftp disconnect 
			     } catch (SocketException e) {
			          // TODO Auto-generated catch block
			          e.printStackTrace();
			     } catch (IOException e) {
			          // TODO Auto-generated catch block
			          e.printStackTrace();
			     }
			dialog.dismiss(); 
	  }
	    void login(){//서버로 데이터를 보내는 부분
	    	try{
	    		cl = new DefaultHttpClient();
	    		po = new HttpPost("http://54.65.237.226/cameradata.php");//데이터 전달 주소
	            nameValuePairs = new ArrayList<NameValuePair>(8);
	            nameValuePairs.add(new BasicNameValuePair("first",Integer.toString(a1)));  //사용되지 않음
	            nameValuePairs.add(new BasicNameValuePair("second",Integer.toString(a2)));  //사용되지 않음
	            nameValuePairs.add(new BasicNameValuePair("image",strNow));
	            nameValuePairs.add(new BasicNameValuePair("Num1",avergenum[1]));
	            nameValuePairs.add(new BasicNameValuePair("Num2",avergenum[2]));
	            nameValuePairs.add(new BasicNameValuePair("Num3",avergenum[3]));
	            nameValuePairs.add(new BasicNameValuePair("Num4",avergenum[4]));
	            nameValuePairs.add(new BasicNameValuePair("Num5",avergenum[5]));
		        po.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	            re=cl.execute(po);
	            ResponseHandler<String> responseHandler = new BasicResponseHandler();
	            final String response = cl.execute(po, responseHandler);
	            System.out.println("Response :" + response); //메시지 요청이 제대로 됬는지 확인용!
	            
	            runOnUiThread(new Runnable() {
	                public void run() {
	                    dialog.dismiss(); 
	                }
	            });
	            Log.e("set", "set");
	            if(response.equalsIgnoreCase("Success")){
	                runOnUiThread(new Runnable() {
	                    public void run() {
	                        Toast.makeText(Graph.this,"성공", Toast.LENGTH_SHORT).show();
	                        //로그인에 성공하면 토스트메시지 출력하고,
	                    }
	                });
	                finish();
	            }else{
	                Toast.makeText(Graph.this,"데이터 전송 실패", Toast.LENGTH_SHORT).show();                
	            }
	        }catch(Exception e){
	            dialog.dismiss();
	            System.out.println("Exception : " + e.getMessage());
	        }
	    }
}

