package com.example.logcollector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class LogCollectorService extends Service {
	
	private static final String TAG = "LogCollectorService";
	private boolean isServiceDestroyed = false;
	private static String[] format = null;
	private static int id = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
	}

	private void collectLogInBackground() {
		// TODO Auto-generated method stub
		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!isServiceDestroyed)
					execCommand();
			}
			
		}.start();
	}

	protected void execCommand() {
		// TODO Auto-generated method stub
		Log.d(TAG, "########execCammand");
		try {
			Process p = Runtime.getRuntime().exec(format);
			writeLogToLocal(p.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void writeLogToLocal(InputStream inputStream) {
		// TODO Auto-generated method stub
		BufferedWriter writer = null;
		BufferedReader buffReader = null;
		File path = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_DOWNLOADS);
	    File file = new File(path, "log" + (id++) + ".txt");
		Log.d(TAG, "writeLogToLocal");
		try {
			path.mkdirs();
			writer = new BufferedWriter(new FileWriter(file));
			buffReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while((line = buffReader.readLine()) != null) {
				writer.write(line + "\n");
				if(isServiceDestroyed)
					break;
				Thread.yield();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				buffReader.close();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		isServiceDestroyed = true;
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		format = intent.getStringArrayExtra("format");
		collectLogInBackground();
		return Service.START_STICKY;
	}

}
