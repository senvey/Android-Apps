package com.williamlee.memex;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MemexActivity extends ListActivity {

    private NotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        try {
        	this.loadWords();
        } catch (Exception ex) {
        	
        }

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
        TextView tv = (TextView) super.findViewById(R.id.txt_words);
        String words = tv.getText().toString();
        
        mDbHelper.createNote(words);
        Toast.makeText(getApplication(), "Text added!", 1000).show();
    }
    
}