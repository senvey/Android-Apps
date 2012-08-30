package com.williamlee.memex;

import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.williamlee.memex.NotificationService.NotificationBinder;

public class MemexActivity extends ListActivity {

    private static final int ACTIVITY_DETAIL = 1;
    private static final String TAG = "Memex.MemexActivity";
	
    private NotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;
    
    private TextView wordsView;
	private TextView tagsView;
    
    private NotificationService mService;
    private boolean mBound;
    private HashMap<Long, List<TimerTask>> taskList = new HashMap<Long, List<TimerTask>>();
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        this.loadWords();

        this.wordsView = (TextView) super.findViewById(R.id.txt_words);
        this.tagsView = (TextView) super.findViewById(R.id.txt_tags);

        Button addButton = (Button) super.findViewById(R.id.btn_add);
        addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addWords();
		        Toast.makeText(getApplication(), "Words added!", 1000).show();
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
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = mNotesCursor;
        c.moveToPosition(position);
        Intent i = new Intent(this, WordsDetail.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        i.putExtra(NotesDbAdapter.KEY_CONTENT, c.getString(
                c.getColumnIndexOrThrow(NotesDbAdapter.KEY_CONTENT)));
        i.putExtra(NotesDbAdapter.KEY_TAGS, c.getString(
                c.getColumnIndexOrThrow(NotesDbAdapter.KEY_TAGS)));
        i.putExtra(NotesDbAdapter.KEY_TIME, c.getString(
                c.getColumnIndexOrThrow(NotesDbAdapter.KEY_TIME)));
        startActivityForResult(i, ACTIVITY_DETAIL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle extras = intent.getExtras();

        switch(requestCode) {
        case ACTIVITY_DETAIL:
        	if (resultCode == RESULT_FIRST_USER) {
        		// delete the entry
                Long mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
                
                if (mRowId != null) {
                	
                	// cancel all scheduled notifications
                	List<TimerTask> notifTasks = this.taskList.remove(mRowId);
                	for (TimerTask task : notifTasks)
                		if (task.scheduledExecutionTime() > System.currentTimeMillis()
                				&& !task.cancel()) {
                			Log.e(TAG, "Failed to cancel scheduled notifications!");
            		        Toast.makeText(getApplication(), "Failed to remove words!", 1000).show();
                			return;
                		}
                	
                    mDbHelper.deleteNote(mRowId);
                	Log.d(TAG, "Removed words with id " + mRowId);
    		        Toast.makeText(getApplication(), "Words removed!", 1000).show();
                }
        	} else if (resultCode == RESULT_OK) {
        		// update the entry
        	}
            break;
        }
        this.loadWords();
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
        	    new SimpleCursorAdapter(this, R.layout.words_entry, mNotesCursor, from, to);
    	super.setListAdapter(notes);
    }
    
    private void addWords() {
        String words = this.wordsView.getText().toString();
        String tags = this.tagsView.getText().toString();
        
        if (words.isEmpty())
        	return;
        
        Long id = mDbHelper.createNote(words, tags);
    	Log.d(TAG, String.format("Added new words [%s] with id %s.", words, id));
    	
        this.resetUI();
        this.loadWords();

        if (this.mBound) {
			List<TimerTask> notifTasks = this.mService.setNotification(
					super.getApplication(), words, Utils.getNotifIntervals());
			// TODO: clean up this list after notifications are fired
			this.taskList.put(id, notifTasks);
        	Log.d(TAG, "Added notifications for word: " + words);
        }
    }
    
    private void resetUI() {
    	this.wordsView.setText("");
    	this.tagsView.setText("");
    }
    
    /**
     * Adds new tag or removes it if it has already been included in the tag list.
     * 
     * @param v
     * 		the TextView containing tag text.
     */
    public void onClickTag(View v) {
    	String newTag = ((TextView) v).getText().toString();
    	String currentTags = tagsView.getText().toString();
    	
    	if (currentTags.isEmpty()) {
    		// add the first new tag to empty tag list (without comma)
    		tagsView.setText(newTag);
    	} else if (currentTags.indexOf(newTag) == -1) {
    		// the tag is not in the list -- add it (with comma)
    		tagsView.setText(currentTags + ", " + newTag);
    	} else {
    		// the tag is in list -- remove it
    		if (currentTags.endsWith(newTag)) {
    			if (currentTags.startsWith(newTag)) {
    				// the only tag left
    				tagsView.setText("");
    			} else {
    				// remove it as the last tag
    				tagsView.setText(currentTags.replace(", " + newTag, ""));
    			}
    		} else {
    			// not the last one, no matter it is the first tag or in the middle
    			tagsView.setText(currentTags.replace(newTag + ", ", ""));
    		}
    	}
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
        menu.add(Menu.NONE, Menu.FIRST, 0, R.string.menu_intervals);
        menu.add(Menu.NONE, Menu.FIRST + 1, 0, R.string.menu_clean_data);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case Menu.FIRST:
        	this.showIntervals();
            return true;
        case Menu.FIRST + 1:
        	new AlertDialog.Builder(this)
        		.setTitle("Confirmation")
				.setMessage("Are you sure you want to clean all the data?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			        @Override
			        public void onClick(DialogInterface dialog, int which) {
			        	cleanData();
			        }
			    })
				.setNegativeButton("No", null)
				.show();
            return true;
        
        }
        return super.onMenuItemSelected(featureId, item);
    }
    
    private void cleanData() {
    	mDbHelper = mDbHelper.rebuild();
    	super.setListAdapter(null);
    }
    
    private void showIntervals() {
    	Intent i = new Intent(this, IntervalList.class);
    	super.startActivity(i);
    }
}