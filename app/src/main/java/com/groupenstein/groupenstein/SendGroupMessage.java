package com.groupenstein.groupenstein;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;


public class SendGroupMessage extends FragmentActivity {

    public static TextView msgDate;
    public static TextView msgExpirationDate;
    public static Calendar msgCalendar;
    public static Calendar msgExpirationCalendar;
    public static Spinner spinner;

    private View mProgressView;
    private View mMessageFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_group_message);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setLogo(R.drawable.ic_logo);
        getActionBar().setDisplayUseLogoEnabled(true);

        msgDate = (TextView) findViewById(R.id.txtMsgDate);
        msgExpirationDate = (TextView) findViewById(R.id.txtExpirationDate);

        msgCalendar  = new GregorianCalendar();
        msgDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(msgCalendar.getTime()));

        msgExpirationCalendar  = new GregorianCalendar();
        msgExpirationCalendar.add(Calendar.DATE,3);
        msgExpirationDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(msgExpirationCalendar.getTime()));

        TextView cancelText = (TextView) findViewById(R.id.txtCancelMsg);
        cancelText.setOnClickListener(new CancelClickListener());

        Button sendMsgButton = (Button) findViewById(R.id.buttonSendMsg);
        sendMsgButton.setOnClickListener(new SendMsgClickListener());

        mMessageFormView = findViewById(R.id.send_message_form);
        mProgressView = findViewById(R.id.send_message_progress);

        spinner = (Spinner) findViewById(R.id.spinnerMessageType);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.message_types, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                Object item = parent.getItemAtPosition(pos);

                System.out.println("it works...   ");

            }

            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_group_message, menu);
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
            case R.id.action_home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void showDatePickerDialog(View v) {

        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(),"datepicker");
    }

    public void showExpirationDatePickerDialog(View v) {

        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        DialogFragment newFragment = new ExpirationDatePickerFragment();
        newFragment.show(getFragmentManager(),"datepicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = msgCalendar.get(Calendar.YEAR);
            int month = msgCalendar.get(Calendar.MONTH);
            int day = msgCalendar.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            msgCalendar.set(Calendar.YEAR,year);
            msgCalendar.set(Calendar.MONTH,month);
            msgCalendar.set(Calendar.DAY_OF_MONTH,day);
            msgDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(msgCalendar.getTime()));

        }
    }

    public static class ExpirationDatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = msgExpirationCalendar.get(Calendar.YEAR);
            int month = msgExpirationCalendar.get(Calendar.MONTH);
            int day = msgExpirationCalendar.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            msgExpirationCalendar.set(Calendar.YEAR,year);
            msgExpirationCalendar.set(Calendar.MONTH,month);
            msgExpirationCalendar.set(Calendar.DAY_OF_MONTH,day);
            msgExpirationDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(msgExpirationCalendar.getTime()));

        }
    }

    private class CancelClickListener  implements View.OnClickListener {
        CancelClickListener(){

        }

        @Override
        public void onClick(View arg0) {
            startActivity(new Intent(getApplicationContext(), GroupViewActivity.class));
        }
    }


    private class SendMsgClickListener  implements View.OnClickListener {
        UserSendGroupMessageTask mAuthTask = null;
        int orgId = 0;
        int groupId = 0;

        SendMsgClickListener(){

        }

        @Override
        public void onClick(View arg0) {

            Bundle extras = getIntent().getExtras();
            if(extras !=null) {
                orgId = extras.getInt("OrganizationId");
                groupId = extras.getInt("GroupId");
            }

            if (orgId > 0 && groupId > 0 && mAuthTask == null)
            {
                showProgress(true);
                mAuthTask = new UserSendGroupMessageTask();
                mAuthTask.execute((Void) null);
            }
        }

        public class UserSendGroupMessageTask extends AsyncTask<Void, Void, OrganizationModel> {

            UserSendGroupMessageTask() {
            }

            @Override
            protected OrganizationModel doInBackground(Void... params) {

                OrganizationModel organizationModel = new OrganizationModel();
                PostMessage();
                return organizationModel;
            }

            @Override
            protected void onPostExecute(final OrganizationModel success) {
                mAuthTask = null;
                showProgress(false);
                startActivity(new Intent(getApplicationContext(), GroupViewActivity.class));
            }

            @Override
            protected void onCancelled() {
                mAuthTask = null;
                showProgress(false);
            }

            public ValidationModel PostMessage()
            {
                GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());
                String userToken = groupensteinConnect.GetPreference(GroupensteinConnect.LoggedInUserTokenKey);

                EditText msgTitle = (EditText) findViewById(R.id.txtMsgTitle);
                EditText msg = (EditText) findViewById(R.id.txtMsg);
                String priority = spinner.getSelectedItem().toString().toLowerCase();
                int priorityValue = 4;
                if (priority.equals("alert")) {
                    priorityValue = 1;
                }
                else if (priority.equals("notice")) {
                    priorityValue = 2;
                }
                else if (priority.equals("news")) {
                    priorityValue = 3;
                }

                Map<String, String> comment = new HashMap<String, String>();
                comment.put("OrganizationId", String.valueOf(orgId));
                comment.put("GroupId", String.valueOf(groupId));
                comment.put("Token", userToken);
                comment.put("Title", msgTitle.getText().toString());
                comment.put("Message", msg.getText().toString());
                comment.put("Priority", String.valueOf(priorityValue));
                comment.put("DateAdded", "/Date(" + msgCalendar.getTimeInMillis() + ")/");
                comment.put("ExpirationDate", "/Date(" + msgExpirationCalendar.getTimeInMillis() + ")/");

                String json = new GsonBuilder().create().toJson(comment, Map.class);

                String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/sendgroupmessage", json);

                Gson gson = new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create();
                ValidationModel validationModel = new ValidationModel();

                try {
                    validationModel = gson.fromJson(jsonResponse, ValidationModel.class);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return validationModel;
            }
        }


        /**
         * Shows the progress UI and hides the login form.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        public void showProgress(final boolean show) {
            // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
            // for very easy animations. If available, use these APIs to fade-in
            // the progress spinner.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                mMessageFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                mMessageFormView.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mMessageFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgressView.animate().setDuration(shortAnimTime).alpha(
                        show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
            } else {
                // The ViewPropertyAnimator APIs are not available, so simply show
                // and hide the relevant UI components.
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mMessageFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }

    }

}
