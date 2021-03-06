package com.example.eric.wishare.dialog;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.model.WiContact;

import java.util.ArrayList;
import java.util.HashMap;

public class WiManageContactsDialog extends WiDialog{
    private WiContactList mContactList;
    private OnContactSelectedListener mOnContactSelectedListener;
    private ArrayList<WiContact> mContacts;

    public interface OnContactSelectedListener{
        void onContactSelected(WiContact contact);
    }

    public WiManageContactsDialog(final Context context){
        super(context);
        mContactList = WiContactList.getInstance(context);
        mContactList.setOnContactListReadyListener(onContactListReady());
    }

    private WiContactList.OnContactListReadyListener onContactListReady(){
        return new WiContactList.OnContactListReadyListener() {
            @Override
            public void onContactListReady(HashMap<String, WiContact> contacts) {
                build();
            }
        };
    }

    private MaterialDialog.ListCallback onContactClicked(){
        return new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                mOnContactSelectedListener.onContactSelected(mContacts.get(position));
            }
        };
    }

    public void setOnContactSelectedListener(OnContactSelectedListener listener){
        mOnContactSelectedListener = listener;
    }

    public MaterialDialog build(){
        ArrayList<String> strings = new ArrayList<>();
        mContacts = new ArrayList(mContactList.getWiContacts().values());

        for (WiContact contact : mContacts) {
            strings.add(contact.getName() + " " + contact.getPhone());
        }

        return new MaterialDialog.Builder(context.get())
                .title("Select a Contact")
                .items(strings)
                .itemsCallback(onContactClicked())
                .negativeText("Cancel")
                .build();
    }
}
