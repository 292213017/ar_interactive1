package com.example.ar_interactive;

import org.json.JSONArray;
import org.json.JSONObject;
import com.karics.library.zxing.android.CaptureActivity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

public class JoinGroup extends Activity {

	private static final String TAG = "JoinGroup";
	private static final String[] wayselect = { "On-Site Teaching", "Distance Teaching" };
	private Spinner spinnerways;
	private ArrayAdapter<String> adapterways;
	Button button_joingroup_go, button_joingroup_back;
	EditText joingroupcode;
	private static final int RESULT_OK = -1;
	private static final int REQUEST_CODE_SCAN = 0x0000;
	private static final String DECODED_CONTENT_KEY = "codedContent";
	String inputcode;
	String getdeviceblemac = "";
	String iptmp = "";
	int teachingselect = 0;
	int ThreadRun1 = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joingroup);
		Log.e(TAG, TAG);
		spinnerways = (Spinner) findViewById(R.id.joingroupspinnerways);
		spinnerways.getBackground().setAlpha(64);
		adapterways = new ArrayAdapter<String>(this, android.R.layout.simple_gallery_item, wayselect);
		adapterways.setDropDownViewResource(android.R.layout.preference_category);
		spinnerways.setAdapter(adapterways);
		spinnerways.setOnItemSelectedListener(new WaysSceneSpinnerSelectedListener());
		spinnerways.setVisibility(View.VISIBLE);
		button_joingroup_go = (Button) findViewById(R.id.button_joingroup_go);
		button_joingroup_back = (Button) findViewById(R.id.button_joingroup_back);
		button_joingroup_go.getBackground().setAlpha(64);
		button_joingroup_back.getBackground().setAlpha(64);
		joingroupcode = (EditText) findViewById(R.id.joingroupcode);
		button_joingroup_go.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (teachingselect == 1) {
					String wificonnect=isWifiConnect();
					if (wificonnect.equals("false")) {
						Toast.makeText(JoinGroup.this, "Please connect WiFi", Toast.LENGTH_SHORT).show();
					} else {
						iptmp = wificonnect;
						inputcode = joingroupcode.getText().toString();
						if (ThreadRun1 == 0) {
							if (inputcode.length() == 0)
								Toast.makeText(JoinGroup.this, "Join failed: Incomplete information", Toast.LENGTH_SHORT).show();
							else
								new JoinThread().start();
						}
					}
				} else {
					inputcode = joingroupcode.getText().toString();
					if (ThreadRun1 == 0) {
						if (inputcode.length() == 0)
							Toast.makeText(JoinGroup.this, "Join failed: Incomplete information", Toast.LENGTH_SHORT).show();
						else
							new JoinThread().start();
					}
				}
			}
		});
		button_joingroup_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
			if (data != null) {
				String getgloveqrcode = data.getStringExtra(DECODED_CONTENT_KEY);
				String[] getgloveqrcodecut = getgloveqrcode.split("-");
				if (getgloveqrcodecut.length == 2 && getgloveqrcodecut[0].equals("AR Device ")) {
					String getgloveblemactmp = getgloveqrcodecut[1].substring(1, getgloveqrcodecut[1].length());
					if (getgloveblemactmp.length() == 17 && getgloveblemactmp.charAt(2) == ':' && getgloveblemactmp.charAt(5) == ':' && getgloveblemactmp.charAt(8) == ':' && getgloveblemactmp.charAt(11) == ':' && getgloveblemactmp.charAt(14) == ':') {
						getdeviceblemac = getgloveblemactmp;
						JoinGroupHandler.obtainMessage(1).sendToTarget();
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
					} else
						JoinGroupHandler.obtainMessage(2).sendToTarget();
				} else
					JoinGroupHandler.obtainMessage(2).sendToTarget();
			}
		}
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

	class JoinThread extends Thread {
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
					Intent intent = new Intent(JoinGroup.this, CaptureActivity.class);
					startActivityForResult(intent, REQUEST_CODE_SCAN);
					Toast.makeText(JoinGroup.this, "Please scan the QR code", Toast.LENGTH_LONG).show();
				} else if (err.equals("datanone")) {
					JoinGroupHandler.obtainMessage(3).sendToTarget();
				} else if (err.equals("timeouterror"))
					JoinGroupHandler.obtainMessage(4).sendToTarget();
				else
					JoinGroupHandler.obtainMessage(5).sendToTarget();
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ThreadRun1 = 0;
		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler JoinGroupHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(JoinGroup.this, "Connecting to Device", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(JoinGroup.this, "QR code error", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(JoinGroup.this, "Failed to join group: Code error", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(JoinGroup.this, "Failed to join group: Network anomaly, timeout", Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(JoinGroup.this, "Failed to join group: System error", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public void onBackPressed() {
		startActivity(new Intent(JoinGroup.this, MenuScene.class));
		JoinGroup.this.finish();
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
