package org.zeus.arena;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import org.zeus.arena.R;

public class HelpDetails extends Activity{
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helpdetails);
		TextView details = (TextView) findViewById(R.id.details);
		Bundle extras = getIntent().getExtras();
		if(extras != null)
		{
			details.setText(extras.getString("details"));
		}
	}

}
