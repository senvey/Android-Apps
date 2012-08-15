package com.williamlee.memex;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class IntervalList extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interval_list);

    	LinearLayout parent = (LinearLayout) findViewById(R.id.interval_list_parent);
    	
        for (Long interval : Utils.getNotifIntervals()) {
        	TextView tv = new TextView(super.getApplicationContext(), null);
        	LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        	tv.setLayoutParams(params);
        	tv.setText(interval.toString());
        	parent.addView(tv);
        }
    }
}
