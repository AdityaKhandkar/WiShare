package com.example.eric.wishare;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.Comparator;

public class WiInvitableContactsView extends LinearLayout {

    private LinearLayout mItems;
    private ArrayList<WiInvitableContactListItem> mInvitableContacts;

    private CheckBox mHeaderSelectAll;
    private Button mHeaderName;

    private boolean mAscendingName;

    public WiInvitableContactsView(Context context) {
        super(context);

        init();
    }

    public WiInvitableContactsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_invitable_contacts, this);
        mInvitableContacts = new ArrayList<>();

        mItems = findViewById(R.id.items);
        mHeaderSelectAll = findViewById(R.id.cb_select_all);
        mHeaderName = findViewById(R.id.btn_name);

        mHeaderSelectAll.setOnCheckedChangeListener(onSelectAll());
    }

    public void add(WiContact contact){
        WiInvitableContactListItem item = new WiInvitableContactListItem(getContext(), contact);

        mInvitableContacts.add(item);
        mItems.addView(item);
    }

    public void filter(String searchString) {
        for(WiInvitableContactListItem contact: mInvitableContacts){
            contact.setVisibility(contact.mContact.name.toLowerCase().contains(searchString.toLowerCase()) ? VISIBLE : GONE);
        }
    }

    private CompoundButton.OnCheckedChangeListener onSelectAll(){
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(WiInvitableContactListItem child: mInvitableContacts){
                    child.mCheckBox.setChecked(isChecked);
                }
            }
        };
    }

    private View.OnClickListener sortName(){
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                sortName(mAscendingName);
            }
        };
    }

    public void sortName(final boolean ascending){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mInvitableContacts.sort(new Comparator<WiInvitableContactListItem>() {
                @Override
                public int compare(WiInvitableContactListItem o1, WiInvitableContactListItem o2) {
                    return ascending ?
                            o1.mContact.name.compareTo(o2.mContact.name) :
                            o2.mContact.name.compareTo(o1.mContact.name);
                }
            });
        }

        mAscendingName = !ascending;
        mItems.removeAllViewsInLayout();
        for(WiInvitableContactListItem contact: mInvitableContacts){
            mItems.addView(contact);
        }
    }

    private class WiInvitableContactListItem extends LinearLayout {
        private WiContact mContact;

        private CheckBox mCheckBox;
        private Button mName;
        private ExpandableLayout mExpandableLayout;

        private TextView mTitle;
        private LinearLayout mItems;
        private Button mInvite;
        private Button mVisitProfile;

        public WiInvitableContactListItem(Context context) {
            super(context);
        }

        public WiInvitableContactListItem(Context context, WiContact contact){
            super(context);

            mContact = contact;
            init();
        }

        public WiInvitableContactListItem(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        private void init(){
            inflate(getContext(), R.layout.layout_invitable_contacts_list_item, this);

            mCheckBox = findViewById(R.id.cb_select);
            mName = findViewById(R.id.btn_name);

            mExpandableLayout = findViewById(R.id.eric);
            mTitle = findViewById(R.id.title);
            mItems = findViewById(R.id.items);
            mInvite = findViewById(R.id.btn_invite_contact);
            mVisitProfile = findViewById(R.id.btn_visit_profile);

            mName.setText(mContact.name);
            mTitle.setText(mContact.name + " has access to these networks");

            mName.setOnClickListener(expand());
            mInvite.setOnClickListener(displayInvitationDialog());
            mVisitProfile.setOnClickListener(startContactActivity());
        }

        private View.OnClickListener expand(){
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mExpandableLayout.isExpanded()){
                        mExpandableLayout.collapse();
                    }
                    else{
                        mExpandableLayout.expand();
                    }
                }
            };
        }

        private View.OnClickListener displayInvitationDialog(){
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new WiCreateInvitationDialog(getContext()).show();
                }
            };
        }

        private View.OnClickListener startContactActivity(){
            return new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), ContactActivity.class);
                    intent.putExtra("contact", mContact);
                    getContext().startActivity(intent);
                }
            };
        }
    }
}
