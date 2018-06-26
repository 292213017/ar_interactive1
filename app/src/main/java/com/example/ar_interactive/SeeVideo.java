package com.example.ar_interactive;

import org.json.JSONArray;
import org.json.JSONObject;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class SeeVideo extends Activity {

	private static final String TAG = "SeeVideo";
	private static final String[] wayselect = { "On-Site Teaching", "Distance Teaching" };
	private Spinner spinnerways;
	private ArrayAdapter<String> adapterways;
	Button button_seevideo_go, button_seevideo_back;
	int teachingselect = 0;
	EditText seevideocode;
	String inputcode;
	String getdeviceblemac = "";
	String iptmp = "";
	int ThreadRun1 = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.seevideo);
		Log.e(TAG, TAG);
		spinnerways = (Spinner) findViewById(R.id.seevideospinnerways);
		spinnerways.getBackground().setAlpha(64);
		adapterways = new ArrayAdapter<String>(this, android.R.layout.simple_gallery_item, wayselect);
		adapterways.setDropDownViewResource(android.R.layout.preference_category);
		spinnerways.setAdapter(adapterways);
		spinnerways.setOnItemSelectedListener(new WaysSceneSpinnerSelectedListener());
		spinnerways.setVisibility(View.VISIBLE);
		button_seevideo_go = (Button) findViewById(R.id.button_seevideo_go);
		button_seevideo_back = (Button) findViewById(R.id.button_seevideo_back);
		button_seevideo_go.getBackground().setAlpha(64);
		button_seevideo_back.getBackground().setAlpha(64);
		seevideocode = (EditText) findViewById(R.id.seevideocode);
		button_seevideo_go.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (teachingselect == 1) {
					String wificonnect=isWifiConnect();
					if (wificonnect.equals("false")) {
						Toast.makeText(SeeVideo.this, "Please connect WiFi", Toast.LENGTH_SHORT).show();
					} else {
						iptmp = wificonnect;
						inputcode = seevideocode.getText().toString();
						if (ThreadRun1 == 0) {
							if (inputcode.length() == 0)
								Toast.makeText(SeeVideo.this, "Video failed: Incomplete information", Toast.LENGTH_SHORT).show();
							else
								new VideoThread().start();
						}
					}
				} else {
					inputcode = seevideocode.getText().toString();
					if (ThreadRun1 == 0) {
						if (inputcode.length() == 0)
							Toast.makeText(SeeVideo.this, "Video failed: Incomplete information", Toast.LENGTH_SHORT).show();
						else
							new VideoThread().start();
					}
				}
			}
		});
		button_seevideo_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	public String isWifiConnect() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi.isConnected()) {
			WifiManager wifiService = (WifiManager) getSystemService(WIFI_SERVICE);
			WifiInfo wifiinfo = wifiService.getConnectionInfo();
			return intToIp(wifiinfo.getIpAddress());
		} else
			return String.valueOf(mWifi.isConnected());
	}

	public String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
	}

	class VideoThread extends Thread {
		@Override
		public void run() {
			ThreadRun1 = 1;
			Log.e(TAG, "POST CMD");
			String mobilecmd = "searchgroup";
			String mobilesql = inputcode;
			String strResult = HttpPost.submitPostData(mobilecmd, mobilesql, teachingselect, iptmp);
			try {
				JSONArray jsonArray = new JSONArray(strResult);
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				String err = jsonObject.getString("err");
				Log.e(TAG, "POST: " + err);
				if (err.equals("ok")) {
					Intent intent = new Intent();
					intent = getPackageManager().getLaunchIntentForPackage("com.AR.Teaching");
					Bundle mBundle = new Bundle();
					mBundle.putString("teachingip", iptmp);
					mBundle.putString("teachingselect", "" + teachingselect);
					mBundle.putString("getdeviceblemac", getdeviceblemac);
					mBundle.putString("groupcode", inputcode);
					mBundle.putString("LoginUser", Login.LoginUser);
					intent.putExtras(mBundle);
					startActivity(intent);
					onBackPressed();
				} else if (err.equals("datanone")) {
					SeeVideoHandler.obtainMessage(1).sendToTarget();
				} else if (err.equals("timeouterror"))
					SeeVideoHandler.obtainMessage(2).sendToTarget();
				else
					SeeVideoHandler.obtainMessage(3).sendToTarget();
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ThreadRun1 = 0;
		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler SeeVideoHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(SeeVideo.this, "Failed to see video: Code error", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(SeeVideo.this, "Failed to see video: Network anomaly, timeout", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(SeeVideo.this, "Failed to see video: System error", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public void onBackPressed() {
		startActivity(new Intent(SeeVideo.this, MenuScene.class));
		SeeVideo.this.finish();
	}

	class WaysSceneSpinnerSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			teachingselect = arg2 + 1;
			Log.e(TAG, "teachingselect:" + teachingselect);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

}
