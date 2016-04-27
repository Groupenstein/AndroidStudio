package com.groupenstein.groupenstein.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.groupenstein.groupenstein.R;
import com.groupenstein.groupenstein.activities.GroupViewActivity;
import com.groupenstein.groupenstein.activities.OrganizationGroupViewActivity;
import com.groupenstein.groupenstein.models.GroupDetailModel;
import com.groupenstein.groupenstein.services.GroupensteinConnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/********* Adapter class extends with BaseAdapter and implements with OnClickListener ************/
public class AddGroupAdapter extends BaseAdapter implements View.OnClickListener {

    /*********** Declare Used Variables *********/
    private Activity activity;
    private ArrayList data;
    private static LayoutInflater inflater=null;
    public Resources res;
    GroupDetailModel tempValues=null;
    int i=0;

    /*************  CustomAdapter Constructor *****************/
    public AddGroupAdapter(Activity a, ArrayList d, Resources resLocal) {

        /********** Take passed values **********/
        activity = a;
        data=d;
        res = resLocal;

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    /******** What is the size of Passed Arraylist Size ************/
    public int getCount() {

        if(data.size()<=0)
            return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{

        public TextView groupName;
        public TextView groupDescription;
        public Button joinButton;

    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.activity_organization_group_add, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.groupName = (TextView) vi.findViewById(R.id.textGroupName);
            holder.groupDescription = (TextView) vi.findViewById(R.id.textGroupDescription);
            holder.joinButton = (Button) vi.findViewById(R.id.buttonJoin);

            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        }
        else
            holder=(ViewHolder)vi.getTag();

        if(data.size()<=0)
        {
            holder.groupName.setText("No groups to join");
            holder.joinButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            /***** Get each Model object from Arraylist ********/
            tempValues=null;
            tempValues = ( GroupDetailModel ) data.get( position );

            /************  Set Model values in Holder elements ***********/

            holder.groupName.setText(tempValues.Name);
            holder.groupDescription.setText( tempValues.Description );

            //****** Set button click
            holder.joinButton.setOnClickListener(new JoinButtonClickListener(position));
        }
        return vi;
    }

    @Override
    public void onClick(View v) {
        notifyDataSetChanged();
    }

    /********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {
        }
    }

    private class JoinButtonClickListener  implements View.OnClickListener {
        private int mPosition;

        JoinButtonClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {


            OrganizationGroupViewActivity sct = (OrganizationGroupViewActivity)activity;

            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
            GroupDetailModel currentGroup = sct.CustomListViewValuesArr.get(mPosition);
            UserAddGroupTask addTask = new UserAddGroupTask(currentGroup);
            addTask.execute((Void) null);
        }

        public class UserAddGroupTask extends AsyncTask<Void, Void, GroupDetailModel> {

            private GroupDetailModel GroupToAdd = new GroupDetailModel();

            UserAddGroupTask(GroupDetailModel groupDetailModel) {
                GroupToAdd = groupDetailModel;
            }

            @Override
            protected GroupDetailModel doInBackground(Void... params) {

                GroupDetailModel groupDetailModel = new GroupDetailModel();
                AddGroup();
                return groupDetailModel;
            }

            @Override
            protected void onCancelled() {

            }

            public void AddGroup()
            {
                GroupensteinConnect groupensteinConnect = new GroupensteinConnect(activity.getApplicationContext());
                String userToken = groupensteinConnect.GetPreference(GroupensteinConnect.LoggedInUserTokenKey);

                Map<String, String> comment = new HashMap<String, String>();
                comment.put("Token",userToken);
                comment.put("GroupId", String.valueOf(GroupToAdd.Id));
                comment.put("OrganizationId", String.valueOf(GroupToAdd.OrganizationId));

                String json = new GsonBuilder().create().toJson(comment, Map.class);

                String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/joingroup", json);

                try {
                    activity.startActivity(new Intent(activity.getApplicationContext(), GroupViewActivity.class));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



}