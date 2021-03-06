package com.example.eric.wishare.view;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eric.wishare.R;
import com.example.eric.wishare.WiContactList;
import com.example.eric.wishare.WiSQLiteDatabase;
import com.example.eric.wishare.model.WiConfiguration;
import com.example.eric.wishare.model.WiContact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class WiTabbedScrollView extends LinearLayout {

    private WiTabbedScrollViewPager mViewPager;
    private WiPagerAdapter mPagerAdapter;
    private WiContactList mContactList;
    private WeakReference<Context> mContext;


    private WiPermittedContactsView mPermittedContactsView;
    private WiInvitableContactsView mInvitableContactsView;

    private Button mLhs;
    private Button mRhs;

    public WiTabbedScrollView(Context context){
        super(context.getApplicationContext());
        mContext = new WeakReference<>(context.getApplicationContext());
        init();
    }

    public WiTabbedScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = new WeakReference<>(context.getApplicationContext());
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_tabbed_scroll_view, this);
        mLhs = findViewById(R.id.btn_lhs);
        mRhs = findViewById(R.id.btn_rhs);

        mContactList = WiContactList.getInstance(mContext.get().getApplicationContext());

        mViewPager = findViewById(R.id.view_pager);
    }

    public void setWiConfiguration(WiConfiguration config){
        mPermittedContactsView = new WiPermittedContactsView(getContext(), mLhs, mRhs, config);
        mInvitableContactsView = new WiInvitableContactsView(getContext(), mLhs, mRhs, config);

        for(WiContact contact: mContactList.getWiContacts().values()){
            if(contact.hasAccessTo(config.SSID)){
                mPermittedContactsView.addPermittedContact(contact);
            }
            else{
                mInvitableContactsView.add(contact);
            }
        }

        mPagerAdapter = new WiPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);

        mInvitableContactsView.sortName(true);

        mPagerAdapter.addView(mPermittedContactsView);
        mPagerAdapter.notifyDataSetChanged();

        mPagerAdapter.addView(mInvitableContactsView);
        mPagerAdapter.notifyDataSetChanged();

        final TabLayout mTabs = findViewById(R.id.tab_layout);
        mTabs.setupWithViewPager(mViewPager);

        System.out.println(mTabs.getTabCount());
        mTabs.getTabAt(0).setText("Permitted Contacts");
        mTabs.getTabAt(1).setText("Invite Contacts");

        mTabs.addOnTabSelectedListener(onTabSelected());

        mPermittedContactsView.sort(WiPermittedContactsView.COL_NAME); //descending order
        mPermittedContactsView.sort(WiPermittedContactsView.COL_NAME); //ascending order

        mPermittedContactsView.display();

        mPermittedContactsView.setOnInviteContactsButtonClickedListener(new WiPermittedContactsView.OnInviteContactsButtonClickedListener() {
            @Override
            public void onInviteContactsButtonClicked() {
                mTabs.getTabAt(1).select();
            }
        });
    }

    private TabLayout.OnTabSelectedListener onTabSelected(){
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) mPermittedContactsView.display();
                if(tab.getPosition() == 1) mInvitableContactsView.display();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
    }

    public void filter(String searchString) {
        mPermittedContactsView.filter(searchString);
        mInvitableContactsView.filter(searchString);
    }

    private class WiPagerAdapter extends PagerAdapter {
        private ArrayList<View> pages = new ArrayList<>();

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  Called when ViewPager needs a page to display; it is our job
        // to add the page to the container, which is normally the ViewPager itself.  Since
        // all our pages are persistent, we simply retrieve it from our "views" ArrayList.
        @Override
        public Object instantiateItem (ViewGroup container, int position)
        {
            View v = pages.get (position);

            final TextView tv2 = new TextView(getContext());
            tv2.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            tv2.setText("Haha wtf?");

            tv2.setHeight(100);
            //((LinearLayout) ((ScrollView) temp.getChildAt(0)).getChildAt(0)).addView(tv2);

            container.addView (v);
            return v;
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  Called when ViewPager no longer needs a page to display; it
        // is our job to remove the page from the container, which is normally the
        // ViewPager itself.  Since all our pages are persistent, we do nothing to the
        // contents of our "views" ArrayList.
        @Override
        public void destroyItem (ViewGroup container, int position, Object object)
        {
            container.removeView (pages.get (position));
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager; can be used by app as well.
        // Returns the total number of pages that the ViewPage can display.  This must
        // never be 0.
        @Override
        public int getCount ()
        {
            return pages.size();
        }


        //-----------------------------------------------------------------------------
        // Used by ViewPager.
        @Override
        public boolean isViewFromObject (View view, Object object) {
            return view == object;
        }

        //-----------------------------------------------------------------------------
        // Add "view" to right end of "views".
        // Returns the position of the new view.
        // The app should call this to add pages; not used by ViewPager.
        public int addView (View v) {
            return addView (v, pages.size());
        }

        //-----------------------------------------------------------------------------
        // Add "view" at "position" to "views".
        // Returns position of new view.
        // The app should call this to add pages; not used by ViewPager.
        public int addView (View v, int position) {
            pages.add (position, v);
            return position;
        }

        //-----------------------------------------------------------------------------
        // Removes the "view" at "position" from "views".
        // Retuns position of removed view.
        // The app should call this to remove pages; not used by ViewPager.
        public int removeView (ViewPager pager, int position) {
            // ViewPager doesn't have a delete method; the closest is to set the adapter
            // again.  When doing so, it deletes all its views.  Then we can delete the view
            // from from the adapter and finally set the adapter to the pager again.  Note
            // that we set the adapter to null before removing the view from "views" - that's
            // because while ViewPager deletes all its views, it will call destroyItem which
            // will in turn cause a null pointer ref.

            pager.setAdapter (null);
            pages.remove (position);
            pager.setAdapter (this);

            return position;
        }
    }

}

