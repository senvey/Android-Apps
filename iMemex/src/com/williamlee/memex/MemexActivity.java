package com.williamlee.memex;

import info.shengweili.memex.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MemexActivity extends Activity {
    
    private static final String FILENAME = "memex.txt";
	
//	private boolean mExternalStorageAvailable = false;
//	private boolean mExternalStorageWriteable = false;
//	private String state;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button addButton = (Button) super.findViewById(R.id.btn_add);
        addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addWords();
			}
		});

        Button loadButton = (Button) super.findViewById(R.id.btn_load);
        loadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadWords();
			}
		});
    }
    
    private void loadWords() {
    	StringBuilder text = new StringBuilder();
		try {
			FileInputStream fis = super.openFileInput(FILENAME);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			
			String line;
            while ((line = br.readLine()) != null) {
            	text.append(line);
            }
            fis.close();
	        Toast.makeText(getApplication(), "Text loaded!", 1000).show();
		} catch (FileNotFoundException e) {
	        Toast.makeText(getApplication(), "ERROR! FileNotFoundException", 1000).show();
		} catch (IOException e) {
	        Toast.makeText(getApplication(), "ERROR! IOException", 1000).show();
		}
    	
    	TextView tv = (TextView) super.findViewById(R.id.txt_all);
    	tv.setText(text);
    }
    
    private void addWords() {
        TextView tv = (TextView) super.findViewById(R.id.txt_words);
        String words = tv.getText().toString();
        String record = Calendar.getInstance().toString() + "," + words;

        try {
        	FileOutputStream fos = super.openFileOutput(FILENAME, Context.MODE_APPEND);
	        fos.write(record.getBytes());
	        fos.close();
	        Toast.makeText(getApplication(), "Text added!", 1000).show();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
	        Toast.makeText(getApplication(), "ERROR! FileNotFoundException", 1000).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
	        Toast.makeText(getApplication(), "ERROR! IOException", 1000).show();
		}
    }
    
//    private void updateExternalStorageState() {
//    	state = Environment.getExternalStorageState();
//    	
//    	if (Environment.MEDIA_MOUNTED.equals(state)) {
//    	    // We can read and write the media
//    	    mExternalStorageAvailable = mExternalStorageWriteable = true;
//    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//    	    // We can only read the media
//    	    mExternalStorageAvailable = true;
//    	    mExternalStorageWriteable = false;
//    	} else {
//    	    // Something else is wrong. It may be one of many other states, but all we need
//    	    //  to know is we can neither read nor write
//    	    mExternalStorageAvailable = mExternalStorageWriteable = false;
//    	}
//    }
}