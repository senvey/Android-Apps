package com.williamlee.memex;

import java.util.concurrent.TimeUnit;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MemexActivity extends ListActivity {
	
	private static final int NOTIFICATION_ID = 1;
	
    private NotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;
    
    private TextView wordsView;
    
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
			}
		});

        Button temp = (Button) super.findViewById(R.id.tmp);
        temp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				regNotifs();
			}
		});
    }
    
    private void loadWords() {
        mNotesCursor = mDbHelper.fetchAllNotes();
        super.startManagingCursor(mNotesCursor);
        String[] from = new String[]{
        		NotesDbAdapter.KEY_CONTENT,
        		NotesDbAdapter.KEY_TIME
		};
        int[] to = new int[]{
        		R.id.txt_content,
        		R.id.txt_time
		};

        SimpleCursorAdapter notes = 
        	    new SimpleCursorAdapter(this, R.layout.word_entry, mNotesCursor, from, to);
    	super.setListAdapter(notes);
    }
    
    private void addWords() {
        String words = this.wordsView.getText().toString();
        Toast.makeText(getApplication(), "All data loaded!", 1000).show();
        
        mDbHelper.createNote(words);
        Toast.makeText(getApplication(), "Text added!", 1000).show();
    }
    
    private void regNotifs() {
    	int icon = R.drawable.icon;
    	CharSequence tickerText = "iMemex";
    	long when = System.currentTimeMillis();
    	System.out.println(String.format("%d hour %d min %d sec",
    			TimeUnit.MILLISECONDS.toHours(when),
    			TimeUnit.MILLISECONDS.toMinutes(when),
    			TimeUnit.MILLISECONDS.toSeconds(when)));
    	
    	Context context = super.getApplicationContext();
    	CharSequence contentTitle = "Words reminder";
    	CharSequence contentText = this.wordsView.getText().toString();
    	Intent notificationIntent = new Intent(this, WordsDetail.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	NotificationManager mNotificationManager =
    			(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	for (Long interval : Utils.getNotifIntervals()) {
	    	Notification notification = new Notification(icon, tickerText, when + interval);
	    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	    	mNotificationManager.notify(NOTIFICATION_ID + (int) (interval / 1000), notification);
    	}
    }
    
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