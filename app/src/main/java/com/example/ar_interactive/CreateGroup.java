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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

public class CreateGroup extends Activity {

	private static final String TAG = "CreateGroup";
	private static final String[] wayselect = { "On-Site Teaching", "Distance Teaching" };
	private Spinner spinnerscene, spinnerways;
	private ArrayAdapter<String> adapterscene, adapterways;
	private static final int RESULT_OK = -1;
	private static final int REQUEST_CODE_SCAN = 0x0000;
	private static final String DECODED_CONTENT_KEY = "codedContent";
	Button button_creategroup_go, button_creategroup_back;
	int modelselect = 0, teachingselect = 0;
	String getdeviceblemac = "";
	String iptmp = "";
	int createscenethreadrun = 0;
	String groupcode = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.creategroup);
		Log.e(TAG, TAG);
		button_creategroup_go = (Button) findViewById(R.id.button_creategroup_go);
		button_creategroup_back = (Button) findViewById(R.id.button_creategroup_back);
		button_creategroup_go.getBackground().setAlpha(64);
		button_creategroup_back.getBackground().setAlpha(64);
		spinnerscene = (Spinner) findViewById(R.id.creategroupspinnerscene);
		spinnerscene.getBackground().setAlpha(64);
		adapterscene = new ArrayAdapter<String>(this, android.R.layout.simple_gallery_item, MenuScene.sceneselect);
		adapterscene.setDropDownViewResource(android.R.layout.preference_category);
		spinnerscene.setAdapter(adapterscene);
		spinnerscene.setOnItemSelectedListener(new SceneSpinnerSelectedListener());
		spinnerscene.setVisibility(View.VISIBLE);
		spinnerways = (Spinner) findViewById(R.id.creategroupspinnerways);
		spinnerways.getBackground().setAlpha(64);
		adapterways = new ArrayAdapter<String>(this, android.R.layout.simple_gallery_item, wayselect);
		adapterways.setDropDownViewResource(android.R.layout.preference_category);
		spinnerways.setAdapter(adapterways);
		spinnerways.setOnItemSelectedListener(new WaysSceneSpinnerSelectedListener());
		spinnerways.setVisibility(View.VISIBLE);
		if (MenuScene.CreateGroupHandlerobtainMessage == 2)
			Toast.makeText(CreateGroup.this, "Failed to load scene: No scene", Toast.LENGTH_SHORT).show();
		else if (MenuScene.CreateGroupHandlerobtainMessage == 3)
			Toast.makeText(CreateGroup.this, "Failed to load scene: Network anomaly, timeout", Toast.LENGTH_SHORT).show();
		else if (MenuScene.CreateGroupHandlerobtainMessage == 4)
			Toast.makeText(CreateGroup.this, "Failed to load scene: System error", Toast.LENGTH_SHORT).show();
		button_creategroup_go.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (teachingselect == 1) {
					String wificonnect = isWifiConnect();
					if (wificonnect.equals("false")) {
						Toast.makeText(CreateGroup.this, "Please connect WiFi", Toast.LENGTH_SHORT).show();
					} else {
						iptmp = wificonnect;
						if (modelselect == 0)
							Toast.makeText(CreateGroup.this, "Failed to load scene: No scene", Toast.LENGTH_SHORT).show();
						else {
							if (createscenethreadrun == 0)
								new ShowSceneThread().start();
						}
					}
				} else {
					if (modelselect == 0)
						Toast.makeText(CreateGroup.this, "Failed to load scene: No scene", Toast.LENGTH_SHORT).show();
					else {
						if (createscenethreadrun == 0)
							new ShowSceneThread().start();
					}
				}
			}
		});
		button_creategroup_back.setOnClickListener(new OnClickListener() {
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
						CreateGroupHandler.obtainMessage(1).sendToTarget();
						Intent intent = new Intent();
						intent = getPackageManager().getLaunchIntentForPackage("com.AR.Teaching");
						Bundle mBundle = new Bundle();
						mBundle.putString("teachingip", iptmp);
						mBundle.putString("teachingselect", "" + teachingselect);
						mBundle.putString("getdeviceblemac", getdeviceblemac);
						mBundle.putString("groupcode", groupcode);
						mBundle.putString("LoginUser", Login.LoginUser);
						intent.putExtras(mBundle);
						startActivity(intent);
						onBackPressed();
					} else
						CreateGroupHandler.obtainMessage(2).sendToTarget();
				} else
					CreateGroupHandler.obtainMessage(2).sendToTarget();
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

	class ShowSceneThread extends Thread {
		@Override
		public void run() {
			createscenethreadrun = 1;
			Log.e(TAG, "POST CMD");
			try {
				String mobilecmd = "creategroup";
				String mobilecmdsql = MenuScene.sceneselect[modelselect - 1];
				String strResult = HttpPost.submitPostData(mobilecmd, mobilecmdsql, teachingselect, iptmp);
				JSONArray jsonArray = new JSONArray(strResult);
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				String err = jsonObject.getString("err");
				Log.e(TAG, "POST: " + err);
				if (err.equals("ok")) {
					String datajson = jsonObject.getString("data");
					groupcode = datajson;
					Log.e(TAG, "groupcode:" + groupcode);
					Intent intent = new Intent(CreateGroup.this, CaptureActivity.class);
					startActivityForResult(intent, REQUEST_CODE_SCAN);
					Toast.makeText(CreateGroup.this, "Please scan the QR code", Toast.LENGTH_LONG).show();
				} else if (err.equals("timeouterror")) {
					CreateGroupHandler.obtainMessage(3).sendToTarget();
				} else {
					CreateGroupHandler.obtainMessage(4).sendToTarget();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			createscenethreadrun = 0;
		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler CreateGroupHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(CreateGroup.this, "Connecting to Device", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(CreateGroup.this, "QR code error", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(CreateGroup.this, "Failed to create group: Network anomaly, timeout", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(CreateGroup.this, "Failed to create group: System error", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	class SceneSpinnerSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if (MenuScene.sceneselectall != 0) {
				modelselect = arg2 + 1;
				Log.e(TAG, "modelselect:" + modelselect);
				Log.e(TAG, "modelselect:" + MenuScene.sceneselect[modelselect - 1]);
			} else
				modelselect = 0;
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	class WaysSceneSpinnerSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			teachingselect = arg2 + 1;
			Log.e(TAG, "teachingselect:" + teachingselect);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	public void onBackPressed() {
		startActivity(new Intent(CreateGroup.this, MenuScene.class));
		CreateGroup.this.finish();
	}

}
