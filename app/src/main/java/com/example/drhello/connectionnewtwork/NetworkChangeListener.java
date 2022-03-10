package com.example.drhello.connectionnewtwork;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class NetworkChangeListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = CheckNetwork.getConnectivityStatusString(context);

       // if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            Log.e(" network reciever", "network reciever  " + status);
            if (status == CheckNetwork.NETWORK_STATUS_NOT_CONNECTED) {
                Log.e("network : ", "false");
                Toast.makeText(context,"Please, Check Your Network",Toast.LENGTH_SHORT).show();
             //   onReceive(context,intent);
            } else {
                Toast.makeText(context,"Connected",Toast.LENGTH_SHORT).show();
                return ;
            }
      //  }
    }


}


