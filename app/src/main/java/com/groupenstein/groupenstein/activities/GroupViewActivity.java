package com.groupenstein.groupenstein.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groupenstein.groupenstein.R;
import com.groupenstein.groupenstein.models.GroupEventModel;
import com.groupenstein.groupenstein.models.GroupMessageModel;
import com.groupenstein.groupenstein.models.UserGroupModel;
import com.groupenstein.groupenstein.models.UserMobileModel;
import com.groupenstein.groupenstein.models.ValidationModel;
import com.groupenstein.groupenstein.services.GroupensteinConnect;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class GroupViewActivity extends Activity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    UserGetGroupsTask mAuthTask = null;
    public static UserMobileModel globalUserMobileModel = new UserMobileModel();


    //Used for GCM registration
    private Context context = null;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String EXTRA_MESSAGE = "message";
    private final static String TAG = "LaunchActivity";
    protected String SENDER_ID = "212857445805";
    private GoogleCloudMessaging gcm = null;
    private String regid = null;
    private static int appVersion = 0;


    //End GCM specific code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GroupensteinConnect groupensteinConnect = new GroupensteinConnect(this);
        if (!groupensteinConnect.IsLoggedInUser()) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        if (mAuthTask == null) {
            mAuthTask = new UserGetGroupsTask();
            mAuthTask.execute((Void) null);
        }

        context = getApplicationContext();
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty() || regid == "") {
                registerInBackground();
            } else {
                Log.d(TAG, "No valid Google Play Services APK found.");
            }
        }

        setContentView(R.layout.activity_group_view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_view, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_manage_groups:
                startActivity(new Intent(this, ManageGroupActivity.class));
                return true;
            case R.id.action_sign_out:
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.action_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.action_refresh:
                finish();
                startActivity(getIntent());
                return true;
            case R.id.action_full_site:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.groupenstein.com"));
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class UserGetGroupsTask extends AsyncTask<Void, Void, UserMobileModel> {

        UserGetGroupsTask() {
        }

        @Override
        protected UserMobileModel doInBackground(Void... params) {

            UserMobileModel userMobileModel = LoadGroups();
            return userMobileModel;
        }

        @Override
        protected void onPostExecute(final UserMobileModel success) {
            mAuthTask = null;


        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;

        }

        public UserMobileModel LoadGroups() {
            GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());
            String userToken = groupensteinConnect.GetPreference(GroupensteinConnect.LoggedInUserTokenKey);

            Map<String, String> comment = new HashMap<String, String>();
            comment.put("Token", userToken);
            String json = new GsonBuilder().create().toJson(comment, Map.class);

            String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/getusergroups", json);

            Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
            UserMobileModel userMobileModel = new UserMobileModel();

            try {
                userMobileModel = gson.fromJson(jsonResponse, UserMobileModel.class);
                globalUserMobileModel = userMobileModel;

                // Create the adapter that will return a fragment for each of the three
                // primary sections of the activity.
                mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

                // Set up the ViewPager with the sections adapter.
                mViewPager = (ViewPager) findViewById(R.id.pager);
                mViewPager.setAdapter(mSectionsPagerAdapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return userMobileModel;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            int size = 1;
            if (globalUserMobileModel != null) {
                size = globalUserMobileModel.Groups.size();
            }
            return size;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment //implements SwipeRefreshLayout.OnRefreshListener
    {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private RelativeLayout topTextView;
        private CardView recentCard, upcomingCard, messageCard, eventsCard;
        private LinearLayout recentList, upcomingList, messageList, eventsList;
        private TextView itemCircle, upcomingItemCircle;
        private TextView recentNoMessages, upcomingNoEvents, yourNoMessages, eventsNoMessages;

        private static final String ARG_SECTION_NUMBER = "section_number";
        String groupDetail;
        String groupName;
        String groupNameAbbreviation;
        Boolean isMessageAdmin;
        TextView groupText;
        // private SwipeRefreshLayout mSwipeRefreshLayout;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_group_view, container, false);

            topTextView = (RelativeLayout) rootView.findViewById(R.id.text_layout);
            messageCard = (CardView) rootView.findViewById(R.id.messages_card);
            recentCard = (CardView) rootView.findViewById(R.id.recent_card);
            upcomingCard = (CardView) rootView.findViewById(R.id.upcoming_card);
            eventsCard = (CardView) rootView.findViewById(R.id.events_card);

            messageList = (LinearLayout) rootView.findViewById(R.id.message_list);
            recentList = (LinearLayout) rootView.findViewById(R.id.recent_list);
            upcomingList = (LinearLayout) rootView.findViewById(R.id.upcoming_list);
            eventsList = (LinearLayout) rootView.findViewById(R.id.events_list);

            itemCircle = (TextView) rootView.findViewById(R.id.message_count);
            upcomingItemCircle = (TextView) rootView.findViewById(R.id.upcoming_message_count);

            recentNoMessages = (TextView) rootView.findViewById(R.id.recent_no_messages);
            upcomingNoEvents = (TextView) rootView.findViewById(R.id.upcoming_no_messages);
            yourNoMessages = (TextView) rootView.findViewById(R.id.your_no_messages);
            eventsNoMessages = (TextView) rootView.findViewById(R.id.your_events_no_messages);

            Bundle args = getArguments();
            int arrayIndex = args.getInt(ARG_SECTION_NUMBER);

            if (arrayIndex > 0) {
                arrayIndex = arrayIndex - 1;
            }

            if (savedInstanceState != null) {
                groupDetail = savedInstanceState.getString("GroupDetail");
                groupName = savedInstanceState.getString("GroupName");
                isMessageAdmin = savedInstanceState.getBoolean("IsMessageAdmin");

            } else {
                LoadView(arrayIndex);
            }

            TextView groupNameText = (TextView) rootView.findViewById(R.id.txtGroupName);
            groupNameText.setText(groupName);

            TextView groupNameTextIcon = (TextView) rootView.findViewById(R.id.group_name_circle);
            if (groupNameAbbreviation != null) {
                if (groupNameAbbreviation.length() > 1) {
                    groupNameTextIcon.setText(groupNameAbbreviation.substring(0, 2));
                } else {
                    groupNameTextIcon.setText(groupNameAbbreviation.substring(0, 1));
                }
                groupNameTextIcon.setVisibility(View.VISIBLE);
            }

            Button createMessage = (Button) rootView.findViewById(R.id.buttonSendMsg);

            if (isMessageAdmin) {
                createMessage.setOnClickListener(new CreateMessageClickListener(arrayIndex));
            } else {
                createMessage.setVisibility(View.GONE);
            }

           /* mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipecontainer);
            mSwipeRefreshLayout.setOnRefreshListener(this);
*/
            return rootView;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("GroupDetail", groupDetail);
            outState.putString("GroupName", groupName);
            outState.putBoolean("IsMessageAdmin", isMessageAdmin);
        }

        /*@Override
        public void onRefresh() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }, 2000);
        }
*/
        public void LoadView(int arrayIndex) {

            GroupViewActivity activity = (GroupViewActivity) getActivity();
            UserMobileModel userMobileModel = activity.globalUserMobileModel;

            if (userMobileModel.Groups.size() > 0) {
                UserGroupModel currentGroup = userMobileModel.Groups.get(arrayIndex);
                if (currentGroup.OrganizationId == -1) {
                    recentCard.setVisibility(View.VISIBLE);
                    upcomingCard.setVisibility(View.VISIBLE);
                    topTextView.setVisibility(View.GONE);
                    messageCard.setVisibility(View.GONE);
                    eventsCard.setVisibility(View.GONE);

                    if (currentGroup.GroupMessages.size() > 0) {
                        itemCircle.setText(String.valueOf(currentGroup.GroupMessages.size()));
                        for (GroupMessageModel messageModel : currentGroup.GroupMessages) {
                            StringBuilder sb = new StringBuilder();
                            for (String s : messageModel.GroupName.split(" ")) {
                                sb.append(s.charAt(0));
                            }
                            String groupNameAbbreviation = sb.toString();
                            String circleText;
                            if (groupNameAbbreviation.length() > 1) {
                                circleText = groupNameAbbreviation.substring(0, 2);
                            } else {
                                circleText = groupNameAbbreviation.substring(0, 1);
                            }

                            DateFormat dateFormat = new SimpleDateFormat("MMM dd, E");

                            LayoutInflater layoutInflater =
                                    (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            final View addView = layoutInflater.inflate(R.layout.group_main_item_element_view, null);
                            TextView nameText = (TextView) addView.findViewById(R.id.group_name);
                            TextView dateText = (TextView) addView.findViewById(R.id.group_date);
                            TextView descriptionText = (TextView) addView.findViewById(R.id.group_description);
                            TextView titleText = (TextView) addView.findViewById(R.id.group_title);
                            TextView circleTextView = (TextView) addView.findViewById(R.id.text_circle);

                            nameText.setVisibility(View.VISIBLE);
                            nameText.setText(messageModel.GroupName);
                            descriptionText.setText(messageModel.Message);
                            titleText.setText(messageModel.Title);
                            circleTextView.setText(circleText);
                            dateText.setText(dateFormat.format(LoadDate(messageModel.DateAdded)));
                            recentList.addView(addView);
                        }
                    } else {
                        recentNoMessages.setVisibility(View.VISIBLE);
                        itemCircle.setText("0");
                    }

                    if (currentGroup.GroupEvents.size() > 0) {
                        for (GroupEventModel eventModel : currentGroup.GroupEvents) {
                            upcomingItemCircle.setText(String.valueOf(currentGroup.GroupEvents.size()));
                            StringBuilder sb = new StringBuilder();
                            for (String s : eventModel.GroupName.split(" ")) {
                                sb.append(s.charAt(0));
                            }
                            String groupNameAbbreviation = sb.toString();
                            String circleText;
                            if (groupNameAbbreviation.length() > 1) {
                                circleText = groupNameAbbreviation.substring(0, 2);
                            } else {
                                circleText = groupNameAbbreviation.substring(0, 1);
                            }

                            DateFormat dateFormat = new SimpleDateFormat("MMM dd, E, hh:mm a");

                            LayoutInflater layoutInflater =
                                    (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            final View addView = layoutInflater.inflate(R.layout.group_main_item_element_view, null);
                            TextView nameText = (TextView) addView.findViewById(R.id.group_name);
                            TextView dateText = (TextView) addView.findViewById(R.id.group_date);
                            TextView descriptionText = (TextView) addView.findViewById(R.id.group_description);
                            TextView titleText = (TextView) addView.findViewById(R.id.group_title);
                            TextView circleTextView = (TextView) addView.findViewById(R.id.text_circle);

                            nameText.setVisibility(View.VISIBLE);
                            nameText.setText(eventModel.GroupName);
                            descriptionText.setText(eventModel.Description);
                            titleText.setText(eventModel.Name);
                            circleTextView.setText(circleText);
                            dateText.setText(dateFormat.format(LoadDate(eventModel.EventDate)));
                            upcomingList.addView(addView);
                        }

                    } else {
                        upcomingNoEvents.setVisibility(View.VISIBLE);
                        upcomingItemCircle.setText("0");
                    }

                } else {
                    recentCard.setVisibility(View.GONE);
                    upcomingCard.setVisibility(View.GONE);
                    topTextView.setVisibility(View.VISIBLE);
                    messageCard.setVisibility(View.VISIBLE);
                    eventsCard.setVisibility(View.VISIBLE);

                    groupName = currentGroup.GroupName;
                    StringBuilder sb = new StringBuilder();
                    for (String s : groupName.split(" ")) {
                        sb.append(s.charAt(0));
                    }
                    groupNameAbbreviation = sb.toString();

                    if (currentGroup.GroupMessages.size() > 0) {
                        itemCircle.setText(String.valueOf(currentGroup.GroupMessages.size()));
                        DateFormat circleDateFormat = new SimpleDateFormat("MMM");
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd, E");
                        String previousDate = circleDateFormat.format(LoadDate(currentGroup.GroupMessages.get(0).DateAdded));
                        boolean firstRun = true;
                        for (GroupMessageModel messageModel : currentGroup.GroupMessages) {


                            LayoutInflater layoutInflater =
                                    (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            final View addView = layoutInflater.inflate(R.layout.group_item_element_view, null);
                            TextView dateText = (TextView) addView.findViewById(R.id.group_date);
                            TextView descriptionText = (TextView) addView.findViewById(R.id.group_description);
                            TextView titleText = (TextView) addView.findViewById(R.id.group_title);
                            TextView circleTextView = (TextView) addView.findViewById(R.id.text_circle);

                            descriptionText.setText(messageModel.Message);
                            titleText.setText(messageModel.Title);
                            if (firstRun || !previousDate.equals(circleDateFormat.format(LoadDate(messageModel.DateAdded)))) {
                                circleTextView.setText(circleDateFormat.format(LoadDate(messageModel.DateAdded)));
                                previousDate = circleDateFormat.format(LoadDate(messageModel.DateAdded));
                                firstRun = false;
                            } else {
                                circleTextView.setVisibility(View.INVISIBLE);
                            }
                            dateText.setText(dateFormat.format(LoadDate(messageModel.DateAdded)));
                            messageList.addView(addView);
                        }
                    } else {
                        yourNoMessages.setVisibility(View.VISIBLE);
                    }

                    if (currentGroup.GroupEvents.size() > 0) {
                        DateFormat circleDateFormat = new SimpleDateFormat("MMM");
                        DateFormat dateFormat = new SimpleDateFormat("MMM dd, E, hh:mm a");
                        String previousDate = circleDateFormat.format(LoadDate(currentGroup.GroupEvents.get(0).EventDate));
                        boolean firstRun = true;
                        for (GroupEventModel eventModel : currentGroup.GroupEvents) {

                            LayoutInflater layoutInflater =
                                    (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            final View addView = layoutInflater.inflate(R.layout.group_item_element_view, null);
                            TextView dateText = (TextView) addView.findViewById(R.id.group_date);
                            TextView descriptionText = (TextView) addView.findViewById(R.id.group_description);
                            TextView titleText = (TextView) addView.findViewById(R.id.group_title);
                            TextView circleTextView = (TextView) addView.findViewById(R.id.text_circle);

                            descriptionText.setText(eventModel.Description);
                            titleText.setText(eventModel.Name);
                            if (firstRun || !previousDate.equals(circleDateFormat.format(LoadDate(eventModel.EventDate)))) {
                                circleTextView.setText(circleDateFormat.format(LoadDate(eventModel.EventDate)));
                                previousDate = circleDateFormat.format(LoadDate(eventModel.EventDate));
                                firstRun = false;
                            } else {
                                circleTextView.setVisibility(View.INVISIBLE);
                            }
                            dateText.setText(dateFormat.format(LoadDate(eventModel.EventDate)));
                            eventsList.addView(addView);
                        }

                    } else {
                        eventsNoMessages.setVisibility(View.VISIBLE);
                    }


                }
                isMessageAdmin = currentGroup.IsGroupMessageAdmin;
            }
        }

        private Date LoadDate(String json) {
            String timeString = json.substring(json.indexOf("(") + 1, json.indexOf(")"));
            String[] timeSegments;
            if (timeString.indexOf("+") > 0) {
                timeSegments = timeString.split("\\+");
            } else {
                timeSegments = timeString.split("\\-");
            }
            // May have to handle negative timezones

            int timeZoneOffSet = 0;
            if (timeSegments.length > 1) {
                timeZoneOffSet = Integer.valueOf(timeSegments[1]) * 36000; // (("0100" / 100) * 3600 * 1000)
            }
            long millis = Long.valueOf(timeSegments[0]);
            Date time = new Date(millis + timeZoneOffSet);
            return time;
        }


        private class CreateMessageClickListener implements View.OnClickListener {
            private int mPosition;

            CreateMessageClickListener(int position) {
                mPosition = position;
            }

            @Override
            public void onClick(View arg0) {
                UserGroupModel tempValues = (UserGroupModel) globalUserMobileModel.Groups.get(mPosition);
                int organizationId = tempValues.OrganizationId;
                int groupId = tempValues.GroupId;
                if (organizationId > 0) {
                    Intent i = new Intent();
                    i.putExtra("OrganizationId", organizationId);
                    i.putExtra("GroupId", groupId);
                    i.setClass(getActivity(), SendGroupMessage.class);
                    startActivity(i);
                }
            }
        }

    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.d(TAG, "This device is not supported - Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());
        String registrationId = groupensteinConnect.GetPreference(GroupensteinConnect.PROPERTY_REG_ID);
        if (registrationId.isEmpty()) {
            Log.d(TAG, "Registration ID not found.");
            return "";
        }
        String appVersion = groupensteinConnect.GetPreference(GroupensteinConnect.PROPERTY_APP_VERSION);
        int registeredVersion = Integer.parseInt(appVersion);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.d(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            appVersion = packageInfo.versionCode;
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    Log.d(TAG, "########################################");
                    Log.d(TAG, "Current Device's Registration ID is: " + msg);

                    GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());
                    String userToken = groupensteinConnect.GetPreference(GroupensteinConnect.LoggedInUserTokenKey);
                    Map<String, String> comment = new HashMap<String, String>();
                    comment.put("Token", userToken);
                    comment.put("DeviceId", regid);
                    comment.put("DeviceOs", "1");
                    String json = new GsonBuilder().create().toJson(comment, Map.class);
                    String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/registerdevice", json);
                    Gson gson = new GsonBuilder().create();
                    ValidationModel validationModel = gson.fromJson(jsonResponse, ValidationModel.class);
                    groupensteinConnect.SavePreference(GroupensteinConnect.PROPERTY_REG_ID, regid);
                    groupensteinConnect.SavePreference(GroupensteinConnect.PROPERTY_APP_VERSION, String.valueOf(appVersion));

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                } catch (Exception ex1) {
                    msg = ex1.getMessage();
                }
                return null;
            }

            protected void onPostExecute(Object result) {
                //to do here
            }

        }.execute(null, null, null);
    }

}
