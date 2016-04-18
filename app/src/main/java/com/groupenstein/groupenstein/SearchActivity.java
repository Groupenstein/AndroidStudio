package com.groupenstein.groupenstein;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SearchActivity extends Activity {

    ListView list;
    SearchAdapter adapter;
    UserSearchGroupsTask mAuthTask = null;
    public SearchResultsModel globalSearchResultsModel = new SearchResultsModel();
    public  SearchActivity CustomListView = null;
    public ArrayList<SearchResultsDetailModel> CustomListViewValuesArr = new ArrayList<SearchResultsDetailModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setLogo(R.drawable.ic_logo);
        getActionBar().setDisplayUseLogoEnabled(true);

        Button searchButton = (Button) findViewById(R.id.buttonSearch);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                searchGroups();
            }
        });

        Spinner dropdown = (Spinner)findViewById(R.id.spinRadius);
        String[] items = new String[]{"0", "1", "5","10","25"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(0,true);

        CustomListView = this;

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                Object item = parent.getItemAtPosition(pos);
                parent.setSelection(pos);

                System.out.println("it works...   ");

            }

            public void onNothingSelected(AdapterView<?> parent)
            {
                System.out.println("it works...   ");
            }
        });
    }

    public void searchGroups() {
        if (mAuthTask == null && globalSearchResultsModel.Organizations.size() == 0) {
            mAuthTask = new UserSearchGroupsTask();
            mAuthTask.execute((Void) null);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
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
            case R.id.action_manage_groups:
                startActivity(new Intent(this, com.groupenstein.groupenstein.ManageGroupActivity.class));
                return true;
            case R.id.action_sign_out:
                finish();
                startActivity(new Intent(this, com.groupenstein.groupenstein.LoginActivity.class));
                return true;
            case R.id.action_home:
                //startActivity(new Intent(this, com.groupenstein.groupenstein.GroupViewActivity.class));
                finish();
                return true;
            case R.id.action_full_site:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.groupenstein.com"));
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onItemClick(int mPosition)
    {
        SearchResultsDetailModel tempValues = ( SearchResultsDetailModel ) CustomListViewValuesArr.get(mPosition);
        int organizationId = tempValues.OrganizationId;
        if (organizationId > 0) {
            Intent i = new Intent();
            i.putExtra("OrganizationId", organizationId);
            i.setClass(this, OrganizationGroupViewActivity.class);
            startActivity(i);
        }
    }

    public class UserSearchGroupsTask extends AsyncTask<Void, Void, SearchResultsModel> {

        UserSearchGroupsTask() {
        }

        @Override
        protected SearchResultsModel doInBackground(Void... params) {

            SearchResultsModel searchResultsModel = LoadGroups();
            return searchResultsModel;
        }

        @Override
        protected void onPostExecute(final SearchResultsModel success) {
            mAuthTask = null;

            for (SearchResultsDetailModel org : globalSearchResultsModel.Organizations)
            {
                CustomListViewValuesArr.add( org );
            }

            Resources res =getResources();
            list = (ListView)findViewById( R.id.listSearchResults );  // List defined in XML ( See Below )

            /**************** Create Custom Adapter *********/
            adapter=new SearchAdapter( CustomListView, CustomListViewValuesArr,res );
            list.setAdapter( adapter );
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }

        public SearchResultsModel LoadGroups()
        {
            GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());
            String userToken = groupensteinConnect.GetPreference(GroupensteinConnect.LoggedInUserTokenKey);
            TextView searchText = (TextView) findViewById( R.id.textSearch );
            TextView locationText = (TextView) findViewById( R.id.textLocation );
            Spinner radiusSpin = (Spinner) findViewById( R.id.spinRadius );

            Map<String, String> comment = new HashMap<String, String>();
            comment.put("SearchText",searchText.getText().toString());
            comment.put("LocationText",locationText.getText().toString());
            comment.put("Radius",radiusSpin.getSelectedItem().toString());

            String json = new GsonBuilder().create().toJson(comment, Map.class);

            String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/search", json);

            Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
            SearchResultsModel searchResultsModel = new SearchResultsModel();

            try {
                searchResultsModel = gson.fromJson(jsonResponse, SearchResultsModel.class);
                globalSearchResultsModel = searchResultsModel;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return searchResultsModel;
        }
    }
}
