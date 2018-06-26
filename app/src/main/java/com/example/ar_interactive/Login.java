package com.example.ar_interactive;

import java.security.MessageDigest;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class Login extends Activity {

	private static final String TAG = "Login";
	public static String LoginUser = "";
	EditText loginedit_user, loginedit_password;
	Button button_login, button_goregister, button_exit;
	String inputuser, inputpassword;
	int ThreadRun = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Log.e(TAG, TAG);
		loginedit_user = (EditText) findViewById(R.id.loginedit_user);
		loginedit_password = (EditText) findViewById(R.id.loginedit_password);
		button_login = (Button) findViewById(R.id.button_login);
		button_goregister = (Button) findViewById(R.id.button_goregister);
		button_exit = (Button) findViewById(R.id.button_exit);
		button_login.getBackground().setAlpha(64);
		button_goregister.getBackground().setAlpha(64);
		button_exit.getBackground().setAlpha(64);
		button_login.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				inputuser = loginedit_user.getText().toString();
				inputpassword = loginedit_password.getText().toString();
				if (ThreadRun == 0) {
					if (inputuser.length() == 0 || inputpassword.length() == 0)
						Toast.makeText(Login.this, "Login failed: Incomplete information", Toast.LENGTH_SHORT).show();
					else
						new LoginThread().start();
				}
			}
		});
		button_goregister.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Login.this, Register.class));
				Login.this.finish();
			}
		});
		button_exit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	class LoginThread extends Thread {
		@Override
		public void run() {
			ThreadRun = 1;
			Log.e(TAG, "POST CMD");
			try {
				String mobilecmd = "login";
				String mobilecmdsql = "select * from User where Name = '" + inputuser + "'";
				String strResult = HttpPost.submitPostData(mobilecmd, mobilecmdsql, 2, "");
				JSONArray jsonArray = new JSONArray(strResult);
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				String err = jsonObject.getString("err");
				Log.e(TAG, "POST: " + err);
				if (err.equals("ok")) {
					JSONArray datajson = jsonObject.getJSONArray("data");
					JSONObject jsonObjecttmp = datajson.getJSONObject(0);
					String returnpwd = jsonObjecttmp.getString("Password");
					MessageDigest md5 = MessageDigest.getInstance("MD5");
					md5.update(inputpassword.getBytes());
					byte[] bytes = md5.digest();
					String pwdmd5 = "";
					for (byte b : bytes) {
						String temp = Integer.toHexString(b & 0xff);
						if (temp.length() == 1)
							temp = "0" + temp;
						pwdmd5 += temp;
					}
					if (pwdmd5.equals(returnpwd)) {
						Login.LoginUser = inputuser;
						LoginHandler.obtainMessage(1).sendToTarget();
						startActivity(new Intent(Login.this, MenuScene.class));
						Login.this.finish();
					} else
						LoginHandler.obtainMessage(2).sendToTarget();
				} else if (err.equals("datanone"))
					LoginHandler.obtainMessage(3).sendToTarget();
				else if (err.equals("timeouterror"))
					LoginHandler.obtainMessage(4).sendToTarget();
				else
					LoginHandler.obtainMessage(5).sendToTarget();
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ThreadRun = 0;
		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler LoginHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(Login.this, "Login successfully", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(Login.this, "Login failed: Password error", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(Login.this, "Login failed: User does not exist", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(Login.this, "Login failed: Network anomaly, timeout", Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(Login.this, "Login failed: System error", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("Exit Application").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Login.this.finish();
			}
		}).setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
	}

}
