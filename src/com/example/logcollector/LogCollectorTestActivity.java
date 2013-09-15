package com.example.logcollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

public class LogCollectorTestActivity extends Activity {
	
	private static final String TAG = "LogCollectorTestActivity";
	
	private Button exec, disable, clear;
	private EditText command;
	private TextView result;
	private ScrollView scroll;
	private RadioButton path;
	
	private boolean stopPrintLog = false;
	
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
        command.setText("logcat -v threadtime");
        command.setEnabled(false);
        command.setFocusable(false);
    }


    private void setOnclickListenerOnButton() {
		// TODO Auto-generated method stub
		exec.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				result.setText("");
				String path = getAvailablePath();
				if(path != null) {
					Intent request = new Intent(LogCollectorTestActivity.this, LogCollectorService.class);
					Log.d(TAG, command.getText().toString());
					String[] param = command.getText().toString().split(" ");
					request.putExtra("format", param);
					request.putExtra("path", path);
					LogCollectorTestActivity.this.startService(request);
					setCatchingLogMode(false);
				}
				new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						execCammand();
					}
					
				}.start();
				
			}
		});
		
		disable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent stopService = new Intent(LogCollectorTestActivity.this, LogCollectorService.class);
				LogCollectorTestActivity.this.stopService(stopService);
				setCatchingLogMode(true);
			}
			
		});
		
		clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				removeSavedLog();
			}
			
		});
	}

	protected void setCatchingLogMode(boolean b) {
		// TODO Auto-generated method stub
		stopPrintLog = b;
		exec.setEnabled(b);
		clear.setEnabled(b);
	}


	protected void removeSavedLog() {
		// TODO Auto-generated method stub
		Log.d(TAG, "remove all save logs.");
		String path = getAvailablePath();
		if(path == null)
			return;
		File logsDir = new File(path);
		File[] logFiles = logsDir.listFiles();
		int count = logFiles.length;
		for(int i = 0; i < count; i ++)
			logFiles[i].delete();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		this.moveTaskToBack(true);
	}


	protected String getAvailablePath() {
		// TODO Auto-generated method stub
		String state = Environment.getExternalStorageState();
		String path = null;
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
			path = Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_DOWNLOADS).getPath() + "/log";
			
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
			result.setText("Read only sd card.");
		} else {
			result.setText("Cannot find sd card.");
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		}
		return path;
	}


	protected void execCammand() {
		// TODO Auto-generated method stub
		Log.d(TAG, "########execCammand");
		
		//ActivityManager:I LogCollectorTestActivity:D *:S new String[] {"logcat","-d -v", "threadtime"}
		try {
			Process p = Runtime.getRuntime().exec(command.getText().toString().split(" "));
			printResult(p.getInputStream());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private void printResult(InputStream inputStream) {
		// TODO Auto-generated method stub
		Log.d(TAG, "########printResult ++++++++++++");
		
		InputStreamReader inReader = new InputStreamReader(inputStream);
		BufferedReader buffReader = new BufferedReader(inReader);
		int temp = -1;
		
		String str = null;
		try {
			char[] buff = new char[256];
			do {
				temp = buffReader.read(buff);
//				str = buffReader.readLine();
				if(temp != -1) {
					final char[] b = buff;
					final int count = temp;
					this.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							result.setText("");
							result.append(new String(b, 0, count));
						}
					});
				}
				Thread.yield();
				if(stopPrintLog)
					break;
			} while(temp != -1);
			
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
		disable = (Button)findViewById(R.id.disable_log_collect);
		clear = (Button)findViewById(R.id.clear_log);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log_collector_test, menu);
        return true;
    }
    
}
