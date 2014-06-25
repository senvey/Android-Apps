package com.williamlee.memex;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class NotificationService extends Service {
    
    private static final int NOTIFICATION_ID = 1;
    
    private final IBinder mBinder = new NotificationBinder();
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }
    
    public List<TimerTask> setNotification(Context context, String words,
            List<Long> intervals) {
        Timer t = new Timer();
        List<TimerTask> notifTasks = new ArrayList<TimerTask>();
        
        for (Long interval : intervals) {
            NotificationTask notifTask = new NotificationTask(context, words);
            t.schedule(notifTask, interval);
            notifTasks.add(notifTask);
        }
        
        return notifTasks;
    }
    
    public class NotificationBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }
    
    private class NotificationTask extends TimerTask {
        
        private Context context;
        private String text;
        
        public NotificationTask(Context context, String text) {
            this.context = context;
            this.text = text;
        }

        @Override
        public void run() {
            int icon = R.drawable.icon;
            String tickerText = "iMemex";
            long when = System.currentTimeMillis();
            String contentTitle = "Words Reminder";
            Intent notificationIntent = new Intent(this.context, WordsDetail.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, 0);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(icon, tickerText, when);
            notification.setLatestEventInfo(this.context, contentTitle, this.text, contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
        
    }

}
