package com.example.logcollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public class LogCollectorTestActivity extends Activity implements OnClickListener, OnItemSelectedListener{
	
	private static final String TAG = "LogCollectorTestActivity";
	
	private Button exec, disable, clear, addFilter;
	private EditText command;
	private TextView result;
	private ScrollView scroll;
	private RadioButton path;
	private Spinner globlePriority;
	private String globlePriorityvalue = "*:V";
	
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
        setListeners();
        command.setText("logcat -v threadtime");
        command.setEnabled(false);
        command.setFocusable(false);
    }

	private void setListeners() {
		// TODO Auto-generated method stub
		exec.setOnClickListener(this);
		disable.setOnClickListener(this);
		clear.setOnClickListener(this);
		globlePriority.setOnItemSelectedListener(this);
	}

	protected void setCatchingLogMode(boolean b) {
		// TODO Auto-generated method stub
		stopPrintLog = b;
		exec.setEnabled(b);
		clear.setEnabled(b);
		globlePriority.setEnabled(b);
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
			ArrayList<String> array = new ArrayList<String>();
			String[] items = command.getText().toString().split(" ");
			for(String item : items) 
				array.add(item);
			array.add(globlePriorityvalue);
			Process p = Runtime.getRuntime().exec(array.toArray(new String[0]));
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
		addFilter = (Button)findViewById(R.id.add_filter);
		globlePriority = (Spinner)findViewById(R.id.globle_priority);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log_collector_test, menu);
        return true;
    }


	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()) {
		case R.id.exec:
			startServiceToSaveLog();
			break;
		case R.id.disable_log_collect:
			stopToSaveLog();
			break;
		case R.id.clear_log:
			removeSavedLog();
			break;
		case R.id.add_filter:
			addLogFilter();
			break;
		}
	}

	private void addLogFilter() {
		// TODO Auto-generated method stub
		
	}

	private void stopToSaveLog() {
		// TODO Auto-generated method stub
		Intent stopService = new Intent(LogCollectorTestActivity.this, LogCollectorService.class);
		LogCollectorTestActivity.this.stopService(stopService);
		setCatchingLogMode(true);
	}


	private void startServiceToSaveLog() {
		// TODO Auto-generated method stub
		result.setText("");
		String path = getAvailablePath();
		if(path != null) {
			Intent request = new Intent(LogCollectorTestActivity.this, LogCollectorService.class);
			Log.d(TAG, command.getText().toString());
			String[] param = command.getText().toString().split(" ");
			request.putExtra("format", param);
			request.putExtra("path", path);
			request.putExtra("globle_priority", globlePriorityvalue);
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

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		Log.d(TAG, "globlePriorityvalue: "  + globlePriorityvalue + " [arg2] " + arg2);
		globlePriorityvalue = "*:" + this.getResources().getStringArray(R.array.priority_array)[arg2];
		Log.d(TAG, "globlePriorityvalue: "  + globlePriorityvalue + " [arg2] " + arg2);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		stopToSaveLog();
	}
    
}
