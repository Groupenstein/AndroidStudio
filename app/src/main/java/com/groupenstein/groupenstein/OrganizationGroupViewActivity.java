package com.groupenstein.groupenstein;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OrganizationGroupViewActivity extends Activity {

    ListView list;
    AddGroupAdapter adapter;
    public  OrganizationGroupViewActivity CustomListView = null;
    public ArrayList<GroupDetailModel> CustomListViewValuesArr = new ArrayList<GroupDetailModel>();

    UserAddGroupTask mAuthTask = null;
    private int OrgId = 0;
    public OrganizationModel globalOrganizationModel = new OrganizationModel();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_group_view);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setLogo(R.drawable.ic_logo);
        getActionBar().setDisplayUseLogoEnabled(true);

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            OrgId = extras.getInt("OrganizationId");
        }

        CustomListView = this;

        GetOrganizationData();
    }

    public void GetOrganizationData()
    {
        if (mAuthTask == null && globalOrganizationModel.Groups.size() == 0) {
            mAuthTask = new UserAddGroupTask();
            mAuthTask.execute((Void) null);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_organization_group_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                finish();
                return true;
            case R.id.action_sign_out:
                finish();
                startActivity(new Intent(this, com.groupenstein.groupenstein.LoginActivity.class));
                return true;
            case R.id.action_home:
                startActivity(new Intent(this, com.groupenstein.groupenstein.GroupViewActivity.class));
                return true;
            case R.id.action_full_site:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.groupenstein.com"));
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class UserAddGroupTask extends AsyncTask<Void, Void, OrganizationModel> {

        UserAddGroupTask() {
        }

        @Override
        protected OrganizationModel doInBackground(Void... params) {

            OrganizationModel organizationModel = LoadGroups();
            return organizationModel;
        }

        @Override
        protected void onPostExecute(final OrganizationModel success) {
            mAuthTask = null;
            for (GroupDetailModel group : globalOrganizationModel.Groups)
            {
                CustomListViewValuesArr.add( group );
            }

            Resources res =getResources();
            list = ( ListView )findViewById( R.id.listOrganizationGroups );  // List defined in XML ( See Below )

            TextView orgName = (TextView)findViewById( R.id.textOrganizationName );
            orgName.setText(globalOrganizationModel.Name);
            /**************** Create Custom Adapter *********/
            adapter=new AddGroupAdapter( CustomListView, CustomListViewValuesArr,res );
            list.setAdapter( adapter );


        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }

        public OrganizationModel LoadGroups()
        {
            GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());

            Map<String, String> comment = new HashMap<String, String>();
            comment.put("OrganizationId", String.valueOf(OrgId));
            String json = new GsonBuilder().create().toJson(comment, Map.class);

            String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/getorganizationdetail", json);

            Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
            OrganizationModel organizationModel = new OrganizationModel();

            try {
                organizationModel = gson.fromJson(jsonResponse, OrganizationModel.class);
                globalOrganizationModel = organizationModel;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return organizationModel;
        }


    }
}
