package com.groupenstein.groupenstein.activities;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groupenstein.groupenstein.services.GroupensteinConnect;
import com.groupenstein.groupenstein.R;
import com.groupenstein.groupenstein.models.GroupEventModel;
import com.groupenstein.groupenstein.models.GroupMessageModel;
import com.groupenstein.groupenstein.models.UserGroupModel;
import com.groupenstein.groupenstein.models.UserMobileModel;
import com.groupenstein.groupenstein.models.ValidationModel;


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

            WebView webView = (WebView) rootView.findViewById(R.id.webMsgAndEvents);
            webView.loadData(groupDetail, "text/html", "UTF-8");
            webView.getSettings().setJavaScriptEnabled(true);

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
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) groupNameText.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.section_label);
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
                    groupName = "Recent Message and Upcoming Events";
                } else {
                    groupName = currentGroup.GroupName;
                    StringBuilder sb = new StringBuilder();
                    for (String s : groupName.split(" ")) {
                        sb.append(s.charAt(0));
                    }
                    groupNameAbbreviation = sb.toString();
                }
                isMessageAdmin = currentGroup.IsGroupMessageAdmin;


                StringBuilder sb = new StringBuilder();
                Calendar calendar = new GregorianCalendar();

                sb.append("<html><head><style>");
                sb.append(".mg_group_details { display: inline-block; } ");
                sb.append(".mg_group_detail_events { display: block; margin-bottom: 30px; width:90%; } ");
                sb.append(".mg_group_detail_events h3 { text-align: center; background-color:#f8f8f8; margin-top: 20px;margin-bottom: 20px; padding-top:10px; padding-bottom:10px; } ");
                sb.append(".mg_group_detail_event_month { text-align: center;margin-top: 10px;margin-bottom: 20px;border-bottom-color: rgba(136,136,135, 0.45);border-bottom: dotted;border-width: thin;}");
                sb.append(".mg_group_detail_event_day_info{min-height: 60px; margin-bottom: 20px; padding-top: 10px;padding-bottom: 10px; background-color:#fff;}");
                sb.append(".mg_group_detail_event_info{margin-left:55px; margin-bottom: 10px; background-color:#fff;}");

                sb.append(".mg_group_detail_events_container{text-align: left;padding-bottom: 20px;padding-top: 10px;}");
                sb.append(".mg_group_detail_container .mg_group_detail_event_day{text-align: center;padding-bottom: 7px;padding-left:5px;padding-right:5px;padding-top: 7px;color: #ffffff;margin-left:10px; width: 40px;display: inline-block;}");

                sb.append(".btn {display: inline-block;padding: 6px 12px;margin-bottom: 0;font-size: 14px;font-weight: normal;line-height: 1.428571429;text-align: center;white-space: nowrap;vertical-align: middle;cursor: pointer;border: 1px solid transparent;border-radius: 4px;-webkit-user-select: none;-moz-user-select: none;-ms-user-select: none;-o-user-select: none;user-select: none;}");
                sb.append(".btn:focus {outline: thin dotted #333;outline: 5px auto -webkit-focus-ring-color;outline-offset: -2px;}");
                sb.append(".btn:hover,.btn:focus {color: #333333;text-decoration: none;}");
                sb.append(".btn:active,.btn.active {background-image: none;outline: 0;-webkit-box-shadow: inset 0 3px 5px rgba(0, 0, 0, 0.125);box-shadow: inset 0 3px 5px rgba(0, 0, 0, 0.125);}");
                sb.append(".btn.disabled,.btn[disabled],fieldset[disabled] .btn {pointer-events: none;cursor: not-allowed;opacity: 0.65;filter: alpha(opacity=65);-webkit-box-shadow: none;box-shadow: none;}");
                sb.append(".btn-default {color: #333333;background-color: #ffffff;border-color: #cccccc;}");
                sb.append(".btn-default:hover,.btn-default:focus,.btn-default:active,.btn-default.active,.open .dropdown-toggle.btn-default {color: #333333;background-color: #ebebeb;border-color: #adadad;}");
                sb.append(".btn-default:active,.btn-default.active,.open .dropdown-toggle.btn-default {background-image: none;}");
                sb.append(".btn-default.disabled,.btn-default[disabled],fieldset[disabled] .btn-default,.btn-default.disabled:hover,.btn-default[disabled]:hover,fieldset[disabled] .btn-default:hover,.btn-default.disabled:focus,.btn-default[disabled]:focus,fieldset[disabled] .btn-default:focus,.btn-default.disabled:active,.btn-default[disabled]:active,fieldset[disabled] .btn-default:active,.btn-default.disabled.active,.btn-default[disabled].active,fieldset[disabled] .btn-default.active {background-color: #ffffff;border-color: #cccccc;}");
                sb.append(".btn-primary {color: #ffffff;background-color: #428bca;border-color: #357ebd;}");
                sb.append(".btn-primary:hover,.btn-primary:focus,.btn-primary:active,.btn-primary.active,.open .dropdown-toggle.btn-primary {color: #ffffff;background-color: #3276b1;border-color: #285e8e;}");
                sb.append(".btn-primary:active,.btn-primary.active,.open .dropdown-toggle.btn-primary {background-image: none;}");
                sb.append(".btn-primary.disabled,.btn-primary[disabled],fieldset[disabled] .btn-primary,.btn-primary.disabled:hover,.btn-primary[disabled]:hover,fieldset[disabled] .btn-primary:hover,.btn-primary.disabled:focus,.btn-primary[disabled]:focus,fieldset[disabled] .btn-primary:focus,.btn-primary.disabled:active,.btn-primary[disabled]:active,fieldset[disabled] .btn-primary:active,.btn-primary.disabled.active,.btn-primary[disabled].active,fieldset[disabled] .btn-primary.active {background-color: #428bca;border-color: #357ebd;}");
                sb.append(".mg_float_left{float: left;}.mg_float_right{float: right;}.mg_font_18{font-size: 1.4em;}.mg_font_16{font-size: 1.2em;}.mg_font_14{font-size: 1em;}.mg_font_10{font-size: .8em;}.mg_font_8{font-size: .6em;}.mg_text_right{text-align: right;}.mg_text_center{text-align: center !important;}.mg_bold{font-weight: 700;}.mg_nobold{font-weight: normal;} .mg_high_priority {background-color: #fa1c1c !important; }");

                sb.append("</style><head><body>");

                sb.append("<div class='mg_group_detail_events'>");
                sb.append("<h3>Group Messages</h3>");
                if (currentGroup.GroupMessages.size() > 0) {
                    for (GroupMessageModel msg : currentGroup.GroupMessages) {
                        Log.d("<><>", "Message: Date " + msg.DateAdded + " Message: " + msg.Message  + " Title: " + msg.Title + " Name: " + msg.GroupName);
                        calendar.setTime(LoadDate(msg.DateAdded));

                        sb.append("<div class=\"mg_group_detail_event_day_info \">");
                        if (msg.Priority < 3) {
                            sb.append("<div class=\"mg_group_detail_event_day btn btn-primary mg_float_left mg_font_10 mg_high_priority \">");
                        } else {
                            sb.append("<div class=\"mg_group_detail_event_day btn btn-primary mg_float_left mg_font_10\">");
                        }

                        sb.append(new SimpleDateFormat("MMM").format(calendar.getTime()));
                        sb.append("<br />");
                        sb.append(new SimpleDateFormat("d").format(calendar.getTime()));
                        sb.append("<br />");
                        sb.append(new SimpleDateFormat("E").format(calendar.getTime()));

                        sb.append("</div>");
                        sb.append("<div class=\"mg_group_detail_event_info  \">");
                        sb.append("<b>");
                        if (currentGroup.OrganizationId == -1) {
                            sb.append(msg.GroupName);
                            sb.append("<br/>");
                        }
                        sb.append(msg.Title);
                        sb.append("<br/> (" + new SimpleDateFormat("hh:mm a").format(calendar.getTime()) + ") ");
                        sb.append("</b><br/>");
                        sb.append(msg.Message);
                        sb.append("</div>");
                        sb.append("</div>");
                    }
                } else {
                    sb.append("<div class='mg_bold mg_text_center'>");
                    sb.append("No Recent Messages");
                    sb.append("</div>");
                }
                sb.append("</div>");


                sb.append("<div class=\"mg_group_detail_events\">");
                sb.append("<h3>Group Events</h3>");
                sb.append("<div class=\"mg_group_detail_events_container\">");
                if (currentGroup.GroupEvents.size() > 0) {
                    String lastMonth = "";

                    for (GroupEventModel e : currentGroup.GroupEvents) {
                        calendar.setTime(LoadDate(e.EventDate));
                        String curMonth = new SimpleDateFormat("MM").format(calendar.getTime());
                        if (!curMonth.equals(lastMonth)) {
                            sb.append("<div class=\"mg_group_detail_event_month mg_font_18 mg_bold\">");
                            sb.append(new SimpleDateFormat("MMM").format(calendar.getTime()));
                            sb.append("</div>");
                        }
                        sb.append("<div class=\"mg_group_detail_event_day_info \">");
                        sb.append("<div class=\"mg_group_detail_event_day btn btn-primary mg_float_left mg_font_10\">");
                        sb.append(new SimpleDateFormat("MMM").format(calendar.getTime()));
                        sb.append("<br />");
                        sb.append(new SimpleDateFormat("dd").format(calendar.getTime()));
                        sb.append("<br />");
                        sb.append(new SimpleDateFormat("E").format(calendar.getTime()));

                        sb.append("</div>");
                        sb.append("<div class=\"mg_group_detail_event_info  \">");
                        sb.append("<b>");
                        if (currentGroup.OrganizationId == -1) {
                            sb.append(e.GroupName);
                            sb.append("<br/>");
                        }
                        sb.append(e.Name);
                        if (!e.EventTime.equals("")) {
                            sb.append(" (" + e.EventTime + ")");
                        }
                        sb.append("</b><br/>");
                        sb.append(e.Description);
                        sb.append("</div>");
                        sb.append("</div>");

                        lastMonth = curMonth;
                    }
                    sb.append("</div>");
                } else {
                    sb.append("<div class=\"mg_bold mg_text_center\">");
                    sb.append("No Events Scheduled");
                    sb.append("</div>");

                }
                sb.append("</div>");
                sb.append("</div>");

                sb.append("</body></html>");
                groupDetail = sb.toString();
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

            ;
        }.execute(null, null, null);
    }

    private class ListArrayAdapter extends ArrayAdapter {

        private int count;

        public ListArrayAdapter(Context context, int resource, int count) {
            super(context, resource);
            setNotifyOnChange(true);
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.group_list_element, null);
            }

            TextView name = (TextView) convertView.findViewById(R.id.group_name);
            TextView date = (TextView) convertView.findViewById(R.id.group_date);
            TextView description = (TextView) convertView.findViewById(R.id.group_description);
            TextView title = (TextView) convertView.findViewById(R.id.group_title);
            TextView circle = (TextView) convertView.findViewById(R.id.text_circle);

            return convertView;
        }

    }
}
