package com.example.drhello.ui.hardware;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.example.drhello.R;
import com.example.drhello.ui.alarm.ReminderEditActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Calendar;

public class WakefulReceiver extends BroadcastReceiver {
    AlarmManager mAlarmManager;
    PendingIntent mPendingIntent;
    public static final String CHANEL_ID = "Chanel_id";
    public static final String CHANEL_NAME = "Chanel_name";
    public static final String CHANEL_DESC = "Chanel_description";
    public static Ringtone r;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context,"Hsknkdsfnkjdgu",Toast.LENGTH_LONG).show();
        int mReceivedID = Integer.parseInt(intent.getStringExtra(ReminderEditActivity.EXTRA_REMINDER_ID));


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Hardware hardware = dataSnapshot.getValue(Hardware.class);
                Toast.makeText(context, hardware.getHeart_Rate().toString(), Toast.LENGTH_SHORT).show();


                // Create intent to open ReminderEditActivity on notification click
                Intent editIntent = new Intent(context, TestHardwareActivity.class);
                editIntent.putExtra(ReminderEditActivity.EXTRA_REMINDER_ID, Integer.toString(mReceivedID));
                @SuppressLint("UnspecifiedImmutableFlag") PendingIntent mClick = PendingIntent.getActivity(context,
                        mReceivedID, editIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Create Notification
                NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // only active for android o and higher because it need NotificationChannel
                    @SuppressLint("WrongConstant")
                    NotificationChannel channel = new NotificationChannel(CHANEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_MAX);

                    // configure the notification channel
                    channel.setDescription(CHANEL_DESC);
                    channel.enableLights(true);
                    channel.setLightColor(Color.RED);
                    channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    channel.enableVibration(true);
                    nManager.createNotificationChannel(channel);
                }

                @SuppressLint("ResourceAsColor") NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANEL_ID)
                        .setAutoCancel(false)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setSmallIcon(R.drawable.ic_baseline_alarm_on_white_24dp)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setColor(R.color.appColor)
                        .setContentText(hardware.getHeart_Rate()+"")
                        .setContentIntent(mClick);
                nManager.notify(mReceivedID, mBuilder.build());

                try {
                    Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.morning_piano);
                    r = RingtoneManager.getRingtone(context, soundUri);
                    r.play();
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                // Log.e("Failed to read value.", error.toException().toString());
            }
        });



    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public void setRepeatAlarm(Context context, Calendar calendar, int ID, long RepeatTime) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Put Reminder ID in Intent Extra
        Intent intent = new Intent(context, com.example.drhello.ui.hardware.WakefulReceiver.class);
        intent.putExtra(ReminderEditActivity.EXTRA_REMINDER_ID, Integer.toString(ID));
        mPendingIntent = PendingIntent.getBroadcast(context, ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        // Calculate notification timein
        Calendar c = Calendar.getInstance();
        long currentTime = c.getTimeInMillis();
        long diffTime = calendar.getTimeInMillis() - currentTime;
        // Start alarm using initial notification time and repeat interval time
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + diffTime,
                RepeatTime, mPendingIntent);
        // Restart alarm if device is rebooted
        ComponentName receiver = new ComponentName(context, com.example.drhello.ui.hardware.BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }
}


