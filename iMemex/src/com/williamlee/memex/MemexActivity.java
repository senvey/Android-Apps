package com.williamlee.memex;

import com.williamlee.memex.NotificationService.NotificationBinder;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MemexActivity extends ListActivity {
	
    private NotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;
    
    private TextView wordsView;
    
    private NotificationService mService;
    private boolean mBound;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        this.loadWords();

        this.wordsView = (TextView) super.findViewById(R.id.txt_words);

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
		        Toast.makeText(getApplication(), "All data loaded!", 1000).show();
			}
		});
    }
    
    @Override
    public void onStart () {
        super.onStart();

        // Bind to the service
        Intent intent = new Intent(this, NotificationService.class);
        super.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    	Log.d("BeforeNotifService", "onStart" + " " + mBound);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    
    private void loadWords() {
        mNotesCursor = mDbHelper.fetchAllNotes();
        super.startManagingCursor(mNotesCursor);
        String[] from = new String[] {
        		NotesDbAdapter.KEY_CONTENT,
        		NotesDbAdapter.KEY_TIME
		};
        int[] to = new int[] {
        		R.id.txt_content,
        		R.id.txt_time
		};

        SimpleCursorAdapter notes = 
        	    new SimpleCursorAdapter(this, R.layout.word_entry, mNotesCursor, from, to);
    	super.setListAdapter(notes);
    }
    
    private void addWords() {
        String words = this.wordsView.getText().toString();
        
        if (words.isEmpty())
        	return;
        
        mDbHelper.createNote(words);
        Toast.makeText(getApplication(), "Text added!", 1000).show();

        if (this.mBound) {
        	this.mService.setNotification(super.getApplication(), words, Utils.getNotifIntervals());
        	Log.d("MemexActivity", "Added notifications for word: " + words);
        }
    }
    
    public void onClickTag(View v) {
    	String newTag = ((TextView) v).getText().toString();
    	TextView tags = (TextView) super.findViewById(R.id.txt_tags);
    	String currentTags = tags.getText().toString();
    	if (currentTags.isEmpty())
    		tags.setText(newTag);
    	else if (currentTags.indexOf(newTag) == -1)
    		tags.setText(tags.getText().toString() + ", " + newTag);
    }
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            NotificationBinder binder = (NotificationBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    /********** Menu ***********/
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, Menu.FIRST, 0, R.string.menu_intervals);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case Menu.FIRST:
        	this.showIntervals();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
    
    private void showIntervals() {
    	Intent i = new Intent(this, IntervalList.class);
    	super.startActivity(i);
    }
}