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
import android.content.Intent;

public class Register extends Activity {

	private static final String TAG = "Register";
	EditText registeredit_user, registeredit_password;
	Button button_register, button_back;
	String inputuser, inputpassword;
	int ThreadRun1 = 0, ThreadRun2 = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		Log.e(TAG, TAG);
		registeredit_user = (EditText) findViewById(R.id.registeredit_user);
		registeredit_password = (EditText) findViewById(R.id.registeredit_password);
		button_register = (Button) findViewById(R.id.button_register);
		button_back = (Button) findViewById(R.id.button_back);
		button_register.getBackground().setAlpha(64);
		button_back.getBackground().setAlpha(64);
		button_register.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				inputuser = registeredit_user.getText().toString();
				inputpassword = registeredit_password.getText().toString();
				if (ThreadRun1 == 0) {
					if (inputuser.length() == 0 || inputpassword.length() == 0)
						Toast.makeText(Register.this, "Register failed: Incomplete information", Toast.LENGTH_SHORT).show();
					else
						new RegisterThread1().start();
				}
			}
		});
		button_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	class RegisterThread1 extends Thread {
		@Override
		public void run() {
			ThreadRun1 = 1;
			Log.e(TAG, "POST CMD");
			String mobilecmd = "login";
			String mobilesql = "select * from User where Name = '" + inputuser + "'";
			String strResult = HttpPost.submitPostData(mobilecmd, mobilesql, 2, "");
			try {
				JSONArray jsonArray = new JSONArray(strResult);
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				String err = jsonObject.getString("err");
				Log.e(TAG, "POST: " + err);
				if (err.equals("ok"))
					RegisterHandler.obtainMessage(2).sendToTarget();
				else if (err.equals("datanone")) {
					if (ThreadRun2 == 0)
						new RegisterThread2().start();
				} else if (err.equals("timeouterror"))
					RegisterHandler.obtainMessage(3).sendToTarget();
				else
					RegisterHandler.obtainMessage(4).sendToTarget();
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ThreadRun1 = 0;
		}
	}

	class RegisterThread2 extends Thread {
		@Override
		public void run() {
			ThreadRun2 = 1;
			Log.e(TAG, "POST CMD");
			try {
				MessageDigest md5;
				md5 = MessageDigest.getInstance("MD5");
				md5.update(inputpassword.getBytes());
				byte[] bytes = md5.digest();
				String pwdmd5 = "";
				for (byte b : bytes) {
					String temp = Integer.toHexString(b & 0xff);
					if (temp.length() == 1)
						temp = "0" + temp;
					pwdmd5 += temp;
				}
				String mobilecmd = "register";
				String mobilesql = "insert into User (Name,Password) values ('" + inputuser + "','" + pwdmd5 + "')";
				String strResult = HttpPost.submitPostData(mobilecmd, mobilesql, 2, "");
				JSONArray jsonArray = new JSONArray(strResult);
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				String err = jsonObject.getString("err");
				Log.e(TAG, "POST: " + err);
				if (err.equals("ok")) {
					Login.LoginUser = inputuser;
					RegisterHandler.obtainMessage(1).sendToTarget();
					startActivity(new Intent(Register.this, MenuScene.class));
					Register.this.finish();
				} else if (err.equals("timeouterror"))
					RegisterHandler.obtainMessage(3).sendToTarget();
				else
					RegisterHandler.obtainMessage(4).sendToTarget();
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ThreadRun2 = 0;
		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler RegisterHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(Register.this, "Register successfully", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(Register.this, "Register failed: User has already existed", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(Register.this, "Register failed: Network anomaly, timeout", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(Register.this, "Register failed: System error", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public void onBackPressed() {
		startActivity(new Intent(Register.this, Login.class));
		Register.this.finish();
	}

}
