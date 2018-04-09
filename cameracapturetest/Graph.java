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
	  
	  //�Ʒ��� ������ ����//
	  
	  HttpPost po;
	  HttpResponse re;
	  HttpClient cl;
	  List<NameValuePair> nameValuePairs;
	  ProgressDialog dialog = null;
	   
	//private static final String SERVER_ADDRESS = "http://175.214.102.177:8080";
		  
	  //����������//

	  int num=-1;
	  int size=-1;
	  int a1, a2;
	  
	  //�׷��� �̹��� �̸�
	  
	  Date date;//���� �ð�, ��¥�� ������ date ����
	  String strNow;//���� date�� string�� �ֱ� ���� ����
	  
	  String[] avergenum = new String[10];//�϶� �κ��� ��ġ�� ���� �׸�

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
	    
	    Button VG_BT = (Button)findViewById(R.id.data);//�׷��� �׸���.
		final Button DATA_BT = (Button)findViewById(R.id.graph);//�׷��� ����.
		final Button dataleave = (Button)findViewById(R.id.dataleave);//������ ����
		 
		final Button graphsave = (Button)findViewById(R.id.graphsave);//�׷��� �̹��� ����
		graphsave.setEnabled(false);
		
		final Intent i;
		i = getIntent();//����Ʈ�� �޾ƿ�
		
		final TextView avg1 = (TextView)findViewById(R.id.textView1);
		final TextView avg2 = (TextView)findViewById(R.id.textView2);
		avg1.setText("1st Crop Averge is : ");
		avg2.setText(""); 
		
		// �׷��� �Ӽ� ����. ���̶���� ũ������ �׷��͵�..
	    mRenderer.setApplyBackgroundColor(true);
	    mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
	    mRenderer.setAxisTitleTextSize(16);
	    mRenderer.setChartTitleTextSize(20);
	    mRenderer.setLabelsTextSize(20);
	    mRenderer.setLegendTextSize(20);
	    mRenderer.setYAxisMax(250);//Y�� �ִ밪 250
	    mRenderer.setYAxisMin(50);//Y�� �ּҰ� 50
	    mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
	    mRenderer.setZoomButtonsVisible(false);
	    mRenderer.setZoomEnabled(false, false);//Ȯ�� ����
	    mRenderer.setZoomEnabled(false);//Ȯ�� ����
	    mRenderer.setPanEnabled(false);//�׷��� �� �̵� ����
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

				int height = captureBmp.getHeight();//�̹����� ���� ũ�⸦ �޾ƿ��� ����.(�׷����� x��)
				
				graphsave.setEnabled(true);//��ư Ȱ��ȭ
	    		int pp1;
	        double x = 0;
	        double y = 0;
	        int average = i.getIntExtra("Average", 0);//��հ��� ����Ʈ�� �޾ƿ�

		    String[] data = new String[height];//�̹����� ���� ũ�⸸ŭ�� data array����
	
		    data = i.getStringArrayExtra("data");//data��̿� ���� ���� ����.
		    pp1 = average;
		    
		    avergenum = i.getStringArrayExtra("avergenum");//�϶��κ��� ���� ����.
		    
		    System.out.println("��հ� :" + avergenum[1] + " " + avergenum[2]);
		    Log.w("testest", "testest");
		    
		    a1 = average;
		   
		    for(int j=0; j<height; j++){//height��ŭ �ݺ���.
	    	try {
		          x = x+1;//�̺κ��� x���� 1�� �߰��ϴ� �κ�.
		        } catch (NumberFormatException e) {
		          
		          return;
		        }
		        try {
		        	y =  Double.parseDouble(data[j].toString());
		        	//y�࿡ �Է��� ������ �ִ� �κ�. Array. for�����ν� �Է��� ����ŭ ���.
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
				// TODO �ڵ� ������ �޼ҵ� ����
				graphsave.setEnabled(false);
				dataleave.setEnabled(true);//��ư Ȱ��ȭ
				
				//�׷��� �̸�
				long now = System.currentTimeMillis();//1970�� 1�� 1�Ϻ��� ����� �ð���  long ������ 1/1000�ʷ� �޴´�.
				date = new Date(now);//���� ����� �ð������� ������ �ð������� �����Ѵ�.
				SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHHmmss"); // �ð� �������� �����.
				strNow = sdfNow.format(date);//�ð��������� �ٲ� ���� string������ �ٽ� ��ȯ�Ѵ�.
				
				//�׷����̸� ����
				RelativeLayout savegraph = (RelativeLayout)findViewById(R.id.graphview);
				
				int width_container = savegraph.getWidth() ;//ĸ���� ���̾ƿ� ũ��
				int height_container = savegraph.getHeight() ;//ĸ���� ���̾ƿ� ũ��
				 
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
	    
	    //�Ʒ� ������ ����/
	    
	    dataleave.setEnabled(false);
	    dataleave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO �ڵ� ������ �޼ҵ� ����
				dialog = ProgressDialog.show(Graph.this,"", "���� ����", true);
				
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
	    
	    //���� ��
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
	  
	  void ftp(){//�׸� ������ �ø��� ���� ftp �κ�
		  try {
				 String files = strNow + ".jpg";

			     FTPClient mFTP = new FTPClient();

			     mFTP.setControlEncoding("euc-kr"); // �ѱ� encoding....
			     FTPClientConfig config = new FTPClientConfig();
			     mFTP.configure(config);
			     
			     try{
			     mFTP.connect("", 21);  // ftp�� ���� 
			     mFTP.login("",""); // ftp �α��� ����/���
			     }
			     catch(SocketException e){
			    	 e.printStackTrace();
			     }
			     catch(IOException e){
			    	 e.printStackTrace();
			     }
			     mFTP.setFileType(FTP.BINARY_FILE_TYPE); // ���̳ʸ� ����
			     mFTP.setBufferSize(983 * 721); // ���� ������ 
			     mFTP.enterLocalPassiveMode(); //�нú� ���� ����

			     System.out.println("send");
					
			     File graphimage = new File(Environment.getExternalStorageDirectory().toString()+"/"+strNow + ".jpg");//"/capture.jpg"); // ���ε� �� ������ �ִ� ���(������ sdī�� ���� ����)
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
	    void login(){//������ �����͸� ������ �κ�
	    	try{
	    		cl = new DefaultHttpClient();
	    		po = new HttpPost("http://54.65.237.226/cameradata.php");//������ ���� �ּ�
	            nameValuePairs = new ArrayList<NameValuePair>(8);
	            nameValuePairs.add(new BasicNameValuePair("first",Integer.toString(a1)));  //������ ����
	            nameValuePairs.add(new BasicNameValuePair("second",Integer.toString(a2)));  //������ ����
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
	            System.out.println("Response :" + response); //�޽��� ��û�� ����� ����� Ȯ�ο�!
	            
	            runOnUiThread(new Runnable() {
	                public void run() {
	                    dialog.dismiss(); 
	                }
	            });
	            Log.e("set", "set");
	            if(response.equalsIgnoreCase("Success")){
	                runOnUiThread(new Runnable() {
	                    public void run() {
	                        Toast.makeText(Graph.this,"����", Toast.LENGTH_SHORT).show();
	                        //�α��ο� �����ϸ� �佺Ʈ�޽��� ����ϰ�,
	                    }
	                });
	                finish();
	            }else{
	                Toast.makeText(Graph.this,"������ ���� ����", Toast.LENGTH_SHORT).show();                
	            }
	        }catch(Exception e){
	            dialog.dismiss();
	            System.out.println("Exception : " + e.getMessage());
	        }
	    }
}

