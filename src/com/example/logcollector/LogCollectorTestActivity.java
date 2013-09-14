package com.example.logcollector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class LogCollectorTestActivity extends Activity {
	
	private static final String TAG = "LogCollectorTestActivity";
	
	private Button exec;
	private EditText command;
	private TextView result;
	private ScrollView scroll;
	
	private enum STATUS{
		OK,
		ERROR
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_collector_test);
        
        findView();
        setOnclickListenerOnButton();
    }


    private void setOnclickListenerOnButton() {
		// TODO Auto-generated method stub
		exec.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				result.setText("");
				new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						execCammand();
					}
					
				}.start();
				
				new Thread() {
					int count = 0;
					public void run() {
						while(true) {
							Log.d(TAG, "test log ### count = " + count ++);
							Thread.yield();
						}
					}
				}.start();
			}
		});
	}


	protected void execCammand() {
		// TODO Auto-generated method stub
		Log.d(TAG, "########execCammand");
		
		//ActivityManager:I LogCollectorTestActivity:D *:S
		try {
			Process p = Runtime.getRuntime().exec(new String[] {"logcat","-d", "ActivityManager:I LogCollectorTestActivity:D *:S"});
//			int s = p.waitFor();
//			Log.d(TAG, "########execCammand : " + s);
			STATUS status = checkResultStatus(p.getErrorStream());
			if(status == STATUS.OK)
				printResult(p.getInputStream());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} /*catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	private void printResult(InputStream inputStream) {
		// TODO Auto-generated method stub
		Log.d(TAG, "########printResult ++++++++++++");
		
		InputStreamReader inReader = new InputStreamReader(inputStream);
		BufferedReader buffReader = new BufferedReader(inReader);
		int temp = -1;
		char[] buff = new char[256];
		String str = null;
		try {
			do {
				temp = buffReader.read(buff);
//				str = buffReader.readLine();
				if(temp != -1) {
					final char[] b = buff;
					this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							result.setText("");
							result.append(new String(b));
//							int offset = result.getMeasuredHeight() - scroll.getHeight();
//							scroll.setSmoothScrollingEnabled(true);
//							scroll.fullScroll(View.FOCUS_DOWN);
							scroll.scrollTo(0, 10000000);
						}
					});
				}
				Thread.yield();
			} while(true);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				buffReader.close();
				inReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.d(TAG, "########printResult ---------");
	}


	private STATUS checkResultStatus(InputStream errorStream) {
		// TODO Auto-generated method stub
		Log.d(TAG, "########checkResultStatus");
		
		InputStreamReader inReader = new InputStreamReader(errorStream);
		BufferedReader buffReader = new BufferedReader(inReader);
		int temp = -1;
		char[] buff = new char[32];
		String str = null;
		STATUS status = STATUS.OK;
		try {
			do {
//				temp = buffReader.read(buff);
				str = buffReader.readLine();
				if(str != null) {
//					result.append(new String(buff));
					final String s = str;
					this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							result.append(s);
						}
						
					});
					
					status = STATUS.ERROR;
				}
				
			} while(str != null);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				buffReader.close();
				inReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Log.d(TAG, "########checkResultStatus : " + status);
		return status;
	}


	private void findView() {
		// TODO Auto-generated method stub
		exec = (Button)findViewById(R.id.exec);
		command = (EditText)findViewById(R.id.command);
		result = (TextView)findViewById(R.id.result);
		scroll = (ScrollView)findViewById(R.id.view_result_in_scroll);
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log_collector_test, menu);
        return true;
    }
    
}