package com.groupenstein.groupenstein.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groupenstein.groupenstein.services.GroupensteinConnect;
import com.groupenstein.groupenstein.R;
import com.groupenstein.groupenstein.adapters.RemoveGroupAdapter;
import com.groupenstein.groupenstein.models.UserGroupModel;
import com.groupenstein.groupenstein.models.UserMobileModel;

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

        CustomListView = this;
        setListData();

        ImageView imageView = (ImageView) findViewById(R.id.back_arrow);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
