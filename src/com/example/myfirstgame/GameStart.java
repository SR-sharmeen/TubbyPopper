package com.example.myfirstgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class GameStart extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startscreen);
		ImageView playButton=(ImageView) findViewById(R.id.playButton);
		playButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				Intent i=new Intent(getBaseContext(),MainActivity.class);
				startActivity(i);
				
			}
			
		}
				);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
			Intent i=new Intent(this,About.class);
			startActivity(i);
			return true;
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

			
}
