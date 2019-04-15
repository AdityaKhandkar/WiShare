package com.example.eric.wishare.model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.eric.wishare.MainActivity;
import com.example.eric.wishare.NetworkActivity;
import com.example.eric.wishare.WiUtils;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.model.WiNotification;

import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class WiInvitationNotification extends WiNotification {
    private Map<String,String> mData;
    private String TAG = "WiInvitationNotification";

    private WiInvitation mInvitation;

    public WiInvitationNotification(Context context, String title, String text, Map<String, String> json, int type) {
        super(context, title, text, type);

        mData = json;
    }

    public WiInvitationNotification(Context context, WiInvitation invitation, int type){
        super(context, "WiShare Invitation", "Invitation to " + invitation.getNetworkName(), type);
        mInvitation = invitation;
    }

    public void onNotificationClick() {
        Log.d(TAG, "The notification was clicked!");
        Intent intent = new Intent(mContext, MainActivity.class);
        Intent intent1  = new Intent(mContext, NetworkActivity.class);
        
        //Log.d(TAG, "Current Activity " + WiUtils.getCurrentActivity());
        intent.putExtra("invitation", mInvitation);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);


        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent contentIntent2 = PendingIntent.getActivity(mContext, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mNotification.setContentIntent(contentIntent);
        }
        else{
            mOldBuilder.setContentIntent(contentIntent);
        }
    }

}
