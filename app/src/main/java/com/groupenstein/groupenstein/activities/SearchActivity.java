package com.groupenstein.groupenstein.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groupenstein.groupenstein.R;
import com.groupenstein.groupenstein.adapters.SearchAdapter;
import com.groupenstein.groupenstein.models.SearchResultsDetailModel;
import com.groupenstein.groupenstein.models.SearchResultsModel;
import com.groupenstein.groupenstein.services.GroupensteinConnect;

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

        ImageView imageView = (ImageView) findViewById(R.id.back_arrow);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
