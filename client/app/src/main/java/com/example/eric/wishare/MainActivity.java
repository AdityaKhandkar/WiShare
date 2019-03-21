package com.example.eric.wishare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.eric.wishare.dialog.WiAddNetworkDialog;
import com.example.eric.wishare.dialog.WiInvitationAcceptDeclineDialog;
import com.example.eric.wishare.dialog.WiInvitationListDialog;
import com.example.eric.wishare.dialog.WiManageContactsDialog;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;
import com.example.eric.wishare.model.WiInvitation;
import com.example.eric.wishare.view.WiConfiguredNetworkListView;
import com.example.eric.wishare.view.WiMyInvitationsButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private WiConfiguredNetworkListView mConfiguredNetworkList;

    private Button btnShowNotification;

    private WiMyInvitationsButton btnMyInvitations;
    private Button btnAddNetwork;
    private Button btnManageContacts;

    private WiInvitationListDialog mInvitationListDialog;
    private WiAddNetworkDialog mAddNetworkDialog;
    private WiManageContactsDialog mContactListDialog;
    private SQLiteDatabase mDatabase;

    private RequestQueue mRequestQueue;


    @SuppressLint("ApplySharedPref")
    private void registerDevice(){
        if(!WiUtils.isDeviceRegistered(this)){
            WiMessagingService.registerDevice(
                    WiUtils.getDeviceToken(this),
                    WiUtils.getDevicePhone(this)
            );

            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean("registered", true)
                    .commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        registerDevice();

        WiDataMessage.setToken(WiUtils.getDeviceToken(this));
        WiContactList.getInstance(this).synchronizeContacts(); // async...

        mRequestQueue = Volley.newRequestQueue(this);

        System.out.println("DEVICE TOKEN IS: ");
        System.out.println(WiUtils.getDeviceToken(this));

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, MainActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        btnShowNotification = findViewById(R.id.btn_show_notification);
        btnAddNetwork = findViewById(R.id.btn_add_network);
        btnManageContacts = findViewById(R.id.btn_manage_contacts);
        btnMyInvitations = findViewById(R.id.btn_my_invitations);

        mConfiguredNetworkList = findViewById(R.id.configured_network_list);

        mInvitationListDialog = new WiInvitationListDialog(this, btnMyInvitations);
        WiContact contact1 = new WiContact("Eric Pratt", "1");
        WiContact contact2 = new WiContact("Eric Pratt", "2");
        WiContact contact3 = new WiContact("Eric Pratt", "3");
        WiContact contact4 = new WiContact("Eric Pratt", "+12223334444");
        mInvitationListDialog.add(new WiInvitation("belkin-622", contact1, "Never", "127 hours", "10GB"));
        mInvitationListDialog.add(new WiInvitation("belkin-048", contact2, "2/28/2019", "36 hours", "5GB"));
        mInvitationListDialog.add(new WiInvitation("home-255", contact3, "3/15/2019", "Never", "None"));
        mInvitationListDialog.add(new WiInvitation("home-200", contact4, "3/15/2019", "24 hours", "3GB"));

        //addContacts(MainActivity.this);
        mContactListDialog = new WiManageContactsDialog(this, btnManageContacts);
        //mContactListDialog.loadContactsAsync(); // start loading the contacts asynchronously.

        mContactListDialog.setOnContactSelectedListener(new WiManageContactsDialog.OnContactSelectedListener() {
            @Override
            public void onContactSelected(WiContact contact){
                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                intent.putExtra("contact", contact);
                System.out.println("STARTING CONTACT ACTIVITY");
                startActivity(intent);
            }
        });

        mAddNetworkDialog = new WiAddNetworkDialog(this, btnAddNetwork);
        mAddNetworkDialog.setOnPasswordEnteredListener(onPasswordEntered());


        btnShowNotification.setOnClickListener(sendNotification());
    }

    private View.OnClickListener sendNotification(){
        return new View.OnClickListener(){
            @Override
            public void onClick(View v){

                //WiDataMessage msg = new WiDataMessage(null);
                //mRequestQueue.add(msg.send());

                /*
                // Wifi
                WiNetworkManager mNetworkManager = WiNetworkManager.getInstance(MainActivity.this);
                mNetworkManager.testConnection("305");

                final MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this).progress(true, 100).content("Testing connection...").show();

                mNetworkManager.setOnTestConnectionCompleteListener(new WiNetworkManager.OnTestConnectionCompleteListener() {
                    @Override
                    public void onTestConnectionComplete(boolean success) {
                        dialog.dismiss();
                        new MaterialDialog.Builder(MainActivity.this).title("Connection successful!").positiveText("Ok").show();
                    }
                });

                WiNotificationInviteReceived notification = new WiNotificationInviteReceived(MainActivity.this, "Test Notification", "This is test description");
                notification.show();
                */
            }
        };
    }


    private WiAddNetworkDialog.OnPasswordEnteredListener onPasswordEntered(){
        return new WiAddNetworkDialog.OnPasswordEnteredListener() {
            @Override
            public void OnPasswordEntered(WiConfiguration config) {
                mConfiguredNetworkList.addView(config);
            }
        };
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        mAddNetworkDialog.refresh(this);
        mInvitationListDialog.refresh(this);

        if(mContactListDialog != null) {
            mContactListDialog.refresh(this);
        }


        if(getIntent().getStringExtra("inviteNetwork") != null){
            Intent intent = getIntent();

            String networkName = intent.getStringExtra("network_name");

            String dataLimit = intent.getStringExtra("data_limit");
            String expires = intent.getStringExtra("expires");

            String temp = intent.getStringExtra("owner");
            String name = "";
            String phone = "";
            String other = "";

            if(temp != null){
                try {
                    JSONObject t2 = new JSONObject(temp);
                    name = t2.getString("name");
                    phone = t2.getString("phone");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                networkName = "Sample Network";
                name = "Joe Schmoe";
                phone = "12345";
                expires = "Never";
                other = "";
                dataLimit = "5 Gb";
            }

            intent.removeExtra("inviteNetwork");

            WiInvitation inv = new WiInvitation(networkName, new WiContact(name, phone), expires, other, dataLimit);

            /*
            WiInvitation invitation = null;

            for (WiInvitation invite: mInvitationListDialog.getInvitations()){
                if (invite.getNetworkName().equals(networkName))
                    invitation = invite;
            }
            */


            if (inv != null){
                WiInvitationAcceptDeclineDialog mAcceptDeclineDialog = new WiInvitationAcceptDeclineDialog(this, inv);
                mAcceptDeclineDialog.show();
            }
            else{
                Toast.makeText(this, "Error: Invitation expired or does not exist", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addContacts(final Context context){
        WiSQLiteDatabase.getInstance(context).getWritableDatabase(new WiSQLiteDatabase.OnDBReadyListener() {
            @Override
            public void onDBReady(SQLiteDatabase db) {
                ContentResolver resolver = context.getContentResolver();
                Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

                mDatabase = db;
                while(cursor != null && cursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phone = WiContact.formatPhoneNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    values.put("name", name);
                    values.put("phone", phone);
                    values.put("token", "iAmAToken");
                    if (phone.contains("+")){
                        System.out.println("foo");
                    }
                    mDatabase.insert("SynchronizedContacts", null, values);

                }
                cursor.close();
            }
        });
    }
}
