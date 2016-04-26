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
import com.groupenstein.groupenstein.services.GroupensteinConnect;
import com.groupenstein.groupenstein.R;
import com.groupenstein.groupenstein.activities.GroupViewActivity;
import com.groupenstein.groupenstein.activities.ManageGroupActivity;
import com.groupenstein.groupenstein.models.UserGroupModel;
import com.groupenstein.groupenstein.models.UserMobileModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/********* Adapter class extends with BaseAdapter and implements with OnClickListener ************/
public class RemoveGroupAdapter extends BaseAdapter implements View.OnClickListener {

    /*********** Declare Used Variables *********/
    private Activity activity;
    private ArrayList data;
    private static LayoutInflater inflater=null;
    public Resources res;
    UserGroupModel tempValues=null;
    int i=0;

    /*************  CustomAdapter Constructor *****************/
    public RemoveGroupAdapter(Activity a, ArrayList d, Resources resLocal) {

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

        public TextView text;
        public TextView circleText;
        public Button removeButton;

    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.activitygroupremove, null);

            /****** View Holder Object to contain tabitem.xml file elements ******/

            holder = new ViewHolder();
            holder.text = (TextView) vi.findViewById(R.id.text);
            holder.circleText = (TextView) vi.findViewById(R.id.circle_text);
            holder.removeButton = (Button) vi.findViewById(R.id.removeButton);

            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        }
        else
            holder=(ViewHolder)vi.getTag();

        if(data.size()<=0)
        {
            holder.text.setText("No groups to manage");
            holder.removeButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            /***** Get each Model object from Arraylist ********/
            tempValues=null;
            tempValues = ( UserGroupModel ) data.get( position );

            /************  Set Model values in Holder elements ***********/

            holder.text.setText( tempValues.GroupName );

            StringBuilder sb = new StringBuilder();
            for(String s : tempValues.GroupName.split(" ")){
                sb.append(s.charAt(0));
            }
            String groupNameAbbreviation = sb.toString();
            if (groupNameAbbreviation.length() > 1) {
                holder.circleText.setText(groupNameAbbreviation.substring(0, 2));
            } else {
                holder.circleText.setText(groupNameAbbreviation.substring(0, 1));
            }

            //****** Set button click
            holder.removeButton.setOnClickListener(new RemoveButtonClickListener(position));
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


            ManageGroupActivity sct = (ManageGroupActivity)activity;

            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/

            sct.onItemClick(mPosition);
        }
    }

    private class RemoveButtonClickListener  implements View.OnClickListener {
        private int mPosition;

        RemoveButtonClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {


            ManageGroupActivity sct = (ManageGroupActivity)activity;

            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
            UserGroupModel currentGroup = sct.CustomListViewValuesArr.get(mPosition);
            UserRemoveGroupTask removeTask = new UserRemoveGroupTask(currentGroup);
            removeTask.execute((Void) null);
            sct.CustomListViewValuesArr.remove(mPosition);
        }

        public class UserRemoveGroupTask extends AsyncTask<Void, Void, UserMobileModel> {

            private UserGroupModel GroupToRemove = new UserGroupModel();

            UserRemoveGroupTask(UserGroupModel userGroupModel) {
                GroupToRemove = userGroupModel;
            }

            @Override
            protected UserMobileModel doInBackground(Void... params) {

                UserMobileModel userMobileModel = new UserMobileModel();
                RemoveGroup();
                return userMobileModel;
            }

            @Override
            protected void onPostExecute(final UserMobileModel success) {

            }

            @Override
            protected void onCancelled() {

            }

            public void RemoveGroup()
            {
                GroupensteinConnect groupensteinConnect = new GroupensteinConnect(activity.getApplicationContext());
                String userToken = groupensteinConnect.GetPreference(GroupensteinConnect.LoggedInUserTokenKey);

                Map<String, String> comment = new HashMap<String, String>();
                comment.put("Token",userToken);
                comment.put("GroupId", String.valueOf(GroupToRemove.GroupId));
                comment.put("OrganizationId", String.valueOf(GroupToRemove.OrganizationId));

                String json = new GsonBuilder().create().toJson(comment, Map.class);

                String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/removegroup", json);

                try {
                    activity.startActivity(new Intent(activity.getApplicationContext(), GroupViewActivity.class));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



}