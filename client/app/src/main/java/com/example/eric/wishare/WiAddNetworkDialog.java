package com.example.eric.wishare;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class WiAddNetworkDialog extends WiDialog {
    private WiNetworkManager mManager;
    private ArrayList<String> mNetworks;
    private OnPasswordEnteredListener mListener;
    private WeakReference<Context> mContext;

    interface OnPasswordEnteredListener {
        void OnPasswordEntered(WiConfiguration config);
    }

    public void setOnPasswordEnteredListener(OnPasswordEnteredListener listener) {
        mListener = listener;
    }

    @Override
    public MaterialDialog build() {
        updateNetworks();
        return new MaterialDialog.Builder(context.get())
                .title("Select a Network")
                .items(mNetworks)
                .itemsCallback(onNetWorkSelect())
                .negativeText("Cancel")
                //.theme(context.get().getTheme())
                .build();
    }

    private void updateNetworks() {
        ArrayList<String> temp = new ArrayList<>();

        for(WifiConfiguration config : mManager.getNotConfiguredNetworks()) {
            temp.add(config.SSID.replace("\"", ""));
        }

        mNetworks = temp;
    }

    public WiAddNetworkDialog(Context context, Button btnAddNetwork){
        super(context);
        mContext = new WeakReference<>(context);
        mManager = WiNetworkManager.getInstance();
        List<WifiConfiguration> wifiList = WiNetworkManager.getConfiguredNetworks(context);

        mNetworks = new ArrayList<>();

        for(WifiConfiguration config : wifiList) {
            mNetworks.add(config.SSID.replace("\"", ""));
        }

        btnAddNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiAddNetworkDialog.this.show();
            }
        });

        build();
    }

    private MaterialDialog.ListCallback onNetWorkSelect() {
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, final CharSequence wifiName) {
                new MaterialDialog.Builder(context.get())
                        .title("Enter Password")
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                        .input("Password", "", false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence password) {
                                WiConfiguration config = new WiConfiguration(wifiName.toString(), password.toString());
                                mListener.OnPasswordEntered(config);
                                mManager.addConfiguredNetwork(config);
                                mManager.removeNotConfiguredNetwork(config);
                                Toast.makeText(mContext.get(), "Wifi name " + wifiName, Toast.LENGTH_LONG).show();
                            }})
                        .show();
            }
        };
    }
}
