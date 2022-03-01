package com.example.drhello.ui.hardware;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.drhello.ui.alarm.AlarmReceiver;
import java.util.Calendar;


public class BootReceiver  extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Calendar mCalendar = Calendar.getInstance();
            AlarmReceiver mAlarmReceiver = new AlarmReceiver();
                Toast.makeText(context,"BOOT_COMPLETED",Toast.LENGTH_SHORT).show();
            mAlarmReceiver.setRepeatAlarm(context, mCalendar, 2, 1);

        }
        }
    }
