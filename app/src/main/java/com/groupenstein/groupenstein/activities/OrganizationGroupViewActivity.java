package com.groupenstein.groupenstein.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groupenstein.groupenstein.adapters.AddGroupAdapter;
import com.groupenstein.groupenstein.services.GroupensteinConnect;
import com.groupenstein.groupenstein.R;
import com.groupenstein.groupenstein.models.GroupDetailModel;
import com.groupenstein.groupenstein.models.OrganizationModel;

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

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            OrgId = extras.getInt("OrganizationId");
        }

        CustomListView = this;

        GetOrganizationData();

        ImageView imageView = (ImageView) findViewById(R.id.back_arrow);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sign_out:
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.action_home:
                startActivity(new Intent(this, GroupViewActivity.class));
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
