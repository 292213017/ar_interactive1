package com.example.ar_interactive;

import org.json.JSONArray;
import org.json.JSONObject;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class MenuScene extends Activity {

	private static final String TAG = "MenuScene";
	TextView helloname;
	Button button_creategroup, button_joingroup, button_seevideo, button_logout;
	String[] sceneselecttmp;
	public static String[] sceneselect = { "None" };
	public static int sceneselectall = 0;
	public static int CreateGroupHandlerobtainMessage = 0;
	int showscenethreadrun = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menuscene);
		Log.e(TAG, TAG);
		helloname = (TextView) findViewById(R.id.helloname);
		helloname.setText("Hello, " + Login.LoginUser);
		button_creategroup = (Button) findViewById(R.id.button_creategroup);
		button_joingroup = (Button) findViewById(R.id.button_joingroup);
		button_seevideo = (Button) findViewById(R.id.button_seevideo);
		button_logout = (Button) findViewById(R.id.button_logout);
		button_creategroup.getBackground().setAlpha(64);
		button_joingroup.getBackground().setAlpha(64);
		button_seevideo.getBackground().setAlpha(64);
		button_logout.getBackground().setAlpha(64);
		button_creategroup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (showscenethreadrun == 0) {
					sceneselectall = 0;
					new ShowSceneThread().start();
				}
			}
		});
		button_joingroup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MenuScene.this, JoinGroup.class));
				MenuScene.this.finish();
			}
		});
		button_seevideo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MenuScene.this, SeeVideo.class));
				MenuScene.this.finish();
			}
		});
		button_logout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	class ShowSceneThread extends Thread {
		@Override
		public void run() {
			showscenethreadrun = 1;
			Log.e(TAG, "POST CMD");
			try {
				String mobilecmd = "searchscene";
				String mobilecmdsql = "none";
				String strResult = HttpPost.submitPostData(mobilecmd, mobilecmdsql, 2, "");
				JSONArray jsonArray = new JSONArray(strResult);
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				String err = jsonObject.getString("err");
				Log.e(TAG, "POST: " + err);
				if (err.equals("ok")) {
					JSONArray datajson = jsonObject.getJSONArray("data");
					sceneselecttmp = new String[1000];
					for (int i = 0; i < datajson.length(); i++) {
						String scenealltmp = datajson.getString(i);
						String houzuiget = scenealltmp.substring(scenealltmp.lastIndexOf(".") + 1);
						if (houzuiget.equals("assetbundle")) {
							sceneselecttmp[sceneselectall] = scenealltmp.substring(0, scenealltmp.lastIndexOf("."));
							sceneselectall++;
						}
					}
					if (sceneselectall == 0) {
						sceneselect = new String[1];
						sceneselect[0] = "None";
						CreateGroupHandlerobtainMessage = 2;
						startActivity(new Intent(MenuScene.this, CreateGroup.class));
						MenuScene.this.finish();
					} else {
						sceneselect = new String[sceneselectall];
						for (int j = 0; j < sceneselectall; j++)
							sceneselect[j] = sceneselecttmp[j];
						CreateGroupHandlerobtainMessage = 1;
						startActivity(new Intent(MenuScene.this, CreateGroup.class));
						MenuScene.this.finish();
					}
				} else if (err.equals("datanone")) {
					sceneselect = new String[1];
					sceneselect[0] = "None";
					CreateGroupHandlerobtainMessage = 2;
					startActivity(new Intent(MenuScene.this, CreateGroup.class));
					MenuScene.this.finish();
				} else if (err.equals("timeouterror")) {
					sceneselect = new String[1];
					sceneselect[0] = "None";
					CreateGroupHandlerobtainMessage = 3;
					startActivity(new Intent(MenuScene.this, CreateGroup.class));
					MenuScene.this.finish();
				} else {
					sceneselect = new String[1];
					sceneselect[0] = "None";
					CreateGroupHandlerobtainMessage = 4;
					startActivity(new Intent(MenuScene.this, CreateGroup.class));
					MenuScene.this.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			showscenethreadrun = 0;
		}
	}

	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("Sign Out").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Login.LoginUser = "";
				startActivity(new Intent(MenuScene.this, Login.class));
				MenuScene.this.finish();
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
	}

}
