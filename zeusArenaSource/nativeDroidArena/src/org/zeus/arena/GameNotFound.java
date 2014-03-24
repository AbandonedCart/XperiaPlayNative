package org.zeus.arena;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GameNotFound extends Activity{
	
	private Context context;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamenotfound);
		Button download = (Button) findViewById(R.id.downloadGame);
		context = this;
		download.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(context, Downloader.class);			
				startActivity(intent);
				finish();
			}
		});
		Button cancel = (Button) findViewById(R.id.cancelGame);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

}
