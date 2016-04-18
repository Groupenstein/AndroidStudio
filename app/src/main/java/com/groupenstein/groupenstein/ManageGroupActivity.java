package com.groupenstein.groupenstein;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ManageGroupActivity extends Activity {

    ListView list;
    RemoveGroupAdapter adapter;
    public  ManageGroupActivity CustomListView = null;
    public ArrayList<UserGroupModel> CustomListViewValuesArr = new ArrayList<UserGroupModel>();

    UserGetGroupsTask mAuthTask = null;
    public UserMobileModel globalUserMobileModel = new UserMobileModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_group);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setLogo(R.drawable.ic_logo);
        getActionBar().setDisplayUseLogoEnabled(true);

        CustomListView = this;
        setListData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_back:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /****** Function to set data in ArrayList *************/
    public void setListData()
    {
        if (mAuthTask == null && globalUserMobileModel.Groups.size() == 0) {
            mAuthTask = new UserGetGroupsTask();
            mAuthTask.execute((Void) null);
        }


    }


    /*****************  This function used by adapter ****************/
    public void onItemClick(int mPosition)
    {
        UserGroupModel tempValues = ( UserGroupModel ) CustomListViewValuesArr.get(mPosition);

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

            for (UserGroupModel group : globalUserMobileModel.Groups)
            {
                CustomListViewValuesArr.add( group );
            }

            Resources res =getResources();
            list = ( ListView )findViewById( R.id.listGroups );  // List defined in XML ( See Below )

            /**************** Create Custom Adapter *********/
            adapter=new RemoveGroupAdapter( CustomListView, CustomListViewValuesArr,res );
            list.setAdapter( adapter );
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }

        public UserMobileModel LoadGroups()
        {
            GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());
            String userToken = groupensteinConnect.GetPreference(GroupensteinConnect.LoggedInUserTokenKey);

            Map<String, String> comment = new HashMap<String, String>();
            comment.put("Token",userToken);
            String json = new GsonBuilder().create().toJson(comment, Map.class);

            String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/getusergrouplist", json);

            Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
            UserMobileModel userMobileModel = new UserMobileModel();

            try {
                userMobileModel = gson.fromJson(jsonResponse, UserMobileModel.class);
                globalUserMobileModel = userMobileModel;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return userMobileModel;
        }
    }

}
