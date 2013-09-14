package com.example.logcollector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;

public class LogCollectorService extends Service {
	
	private static final String TAG = "LogCollectorService";
	private boolean isServiceDestroyed = false;
	private static String[] format = null;
	private static String path = null;
	private static int id = 1000;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Intent notifyIntent =
		        new Intent(this, LogCollectorTestActivity.class);
		// Sets the Activity to start in a new, empty task
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// Creates the PendingIntent
		PendingIntent pIntent =
		        PendingIntent.getActivity(
		        this,
		        0,
		        notifyIntent, 0
		);

		// Puts the PendingIntent into the notification builder
		Notification noti = new NotificationCompat.Builder(this)
        .setContentTitle("LogCollector")
        .setContentText("LogCollector is working.")
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentIntent(pIntent)
        .build();
		noti.flags |= Notification.FLAG_NO_CLEAR;
		
		this.startForeground(id, noti);
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
		Log.d(TAG, "writeLogToLocal");
		try {
			writer = getNewWriter();
			buffReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			char[] buf = new char[256];
			int length = 0;
			int times = 8 * 1024;
			while((length = buffReader.read(buf)) != -1) {
				writer.write(buf, 0, length);
				if(isServiceDestroyed)
					break;
				if(times -- == 0){
					times = 8 * 1024;
					writer.close();
					writer = getNewWriter();
				}
					
				Thread.yield();
			}
			Log.d(TAG, "catch log over!!!!!!");
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
	
	private BufferedWriter getNewWriter() {
		// TODO Auto-generated method stub
		File fpath = new File(path);
		fpath.mkdirs();
		String fileName = getFileName();
		Log.d(TAG, fileName);
		Log.d(TAG, fpath.getPath());
	    File file = new File(fpath, fileName + ".txt");
	    BufferedWriter writer = null;
		try {
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer;
	}

	private String getFileName() {
		// TODO Auto-generated method stub
		Date d = new Date();
		CharSequence s  = DateFormat.format("yyyy-MM-dd hh:mm:ss", d.getTime());
		return "device_log_" + s.toString().replace(" ", "_").replace(":", "-");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		isServiceDestroyed = true;
		this.stopForeground(true);
		Log.d(TAG, "service is onDestroy");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		format = intent.getStringArrayExtra("format");
		path = intent.getStringExtra("path");
		collectLogInBackground();
		return Service.START_STICKY;
	}

}
