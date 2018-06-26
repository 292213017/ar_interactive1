package com.example.ar_interactive;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class BeginShow extends Activity {

	private static final String TAG = "BeginShow";
	private ImageView WelcomeImg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.beginshow);
		Log.e(TAG, TAG);
		WelcomeImg = (ImageView) findViewById(R.id.WelcomeImg);
		AlphaAnimation anima = new AlphaAnimation(0.3f, 1.0f);
		anima.setDuration(800);
		WelcomeImg.startAnimation(anima);
		anima.setAnimationListener(new AnimationImpl());
	}

	private class AnimationImpl implements AnimationListener {

		@Override
		public void onAnimationStart(Animation animation) {
			WelcomeImg.setBackgroundResource(R.drawable.background);
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			skip();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

	}

	private void skip() {
		startActivity(new Intent(BeginShow.this, Login.class));
		BeginShow.this.finish();
	}
}
