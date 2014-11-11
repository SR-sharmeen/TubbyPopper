package com.sm.tubbypopper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class GameOver extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gameover);
		Intent i=getIntent();
		String score=i.getStringExtra("score");
		TextView scoreTW=(TextView) findViewById(R.id.textView1);
		scoreTW.setText("score: "+score);
		if(Integer.parseInt(i.getStringExtra("life"))!=0)
				{
					ImageView img=(ImageView) findViewById(R.id.imageView1);
					img.setImageResource(R.drawable.winner);
				}
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

			
}
