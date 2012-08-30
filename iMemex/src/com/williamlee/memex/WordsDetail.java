package com.williamlee.memex;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WordsDetail extends Activity {
	
	private Long mRowId;
	private AlertDialog.Builder alertDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.words_detail);
		setTitle(R.string.title_words_detail);
        
        this.fillData();
    	
		this.alertDialog = new AlertDialog.Builder(this);
        
        Button deleteButton = (Button) super.findViewById(R.id.btn_delete);
        deleteButton.setOnClickListener(this.deleteListener);
        
        Button cancelButton = (Button) super.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(this.cancelListener);
    }
    
    @Override
	public void onBackPressed() {
		this.cancel();
	}

	private void fillData() {
    	TextView mContent = (TextView) super.findViewById(R.id.txt_detail_content);
    	TextView mTags = (TextView) super.findViewById(R.id.txt_detail_tags);
    	TextView mTime = (TextView) super.findViewById(R.id.txt_detail_time);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    String content = extras.getString(NotesDbAdapter.KEY_CONTENT);
		    String tags = extras.getString(NotesDbAdapter.KEY_TAGS);
		    String time = extras.getString(NotesDbAdapter.KEY_TIME);
		    this.mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);

		    if (content != null)
		    	mContent.setText(content);
		    if (tags != null)
		    	mTags.setText(tags);
		    if (time != null)
		    	mTime.setText(time);
		}
    }
    
    private OnClickListener deleteListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			alertDialog
				.setTitle("Confirmation")
				.setMessage("Are you sure you want to delete this entry?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			        @Override
			        public void onClick(DialogInterface dialog, int which) {
				    	Bundle bundle = new Bundle();
				    	if (mRowId != null) {
				    	    bundle.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
				    	}
				    	
				    	Intent mIntent = new Intent();
				    	mIntent.putExtras(bundle);
				    	setResult(RESULT_FIRST_USER, mIntent);
				    	finish();
			        }
			    })
				.setNegativeButton("No", null)
				.show();
		}
    };
    
    private OnClickListener cancelListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			cancel();
		}
    };
    
    private void cancel() {
    	Intent mIntent = new Intent();
    	setResult(RESULT_CANCELED, mIntent);
    	finish();
    }
}
