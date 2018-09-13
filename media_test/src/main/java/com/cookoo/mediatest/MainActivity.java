package com.cookoo.mediatest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.cookoo.mediatest.image.ActivityImage;
import com.cookoo.mediatest.music.ActivityMusic;
import com.cookoo.mediatest.video.ActivityVideo;
import com.cookoo.musicsdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import carnetapp.usbmediadata.utils.PermissionUtils;

/**
 *
 * @author kobe
 * @date 20180416
 */
public class MainActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = "MainActivity";
	private List<String> permissions = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtils.print(TAG," ------>>onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
		permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		findViewById(R.id.music).setOnClickListener(this);
		findViewById(R.id.vedio).setOnClickListener(this);
		findViewById(R.id.image).setOnClickListener(this);
		findViewById(R.id.edit_main).setOnClickListener(this);
		PermissionUtils.getInstance().requestPermission(this,permissions);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.music:
				startActivity(new Intent(MainActivity.this, ActivityMusic.class));
				break;
			case R.id.vedio:
				startActivity(new Intent(MainActivity.this, ActivityVideo.class));
				break;
			case R.id.image:
				startActivity(new Intent(MainActivity.this, ActivityImage.class));
				break;
			case R.id.edit_main:
				finish();
				break;
			default:
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		LogUtils.print(TAG," ------>>onStop()");
	}

	@Override
	protected void onDestroy() {
		LogUtils.print(TAG," ------>>onDestroy()");
		super.onDestroy();
	}

	@Override
	public Activity getChildContext() {
		return this;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		PermissionUtils.getInstance().onRequestPermissionsResult(this,requestCode, permissions, grantResults);
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

}
