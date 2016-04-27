package com.groupenstein.groupenstein.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.groupenstein.groupenstein.R;
import com.groupenstein.groupenstein.models.UserTokenModel;
import com.groupenstein.groupenstein.models.ValidationModel;
import com.groupenstein.groupenstein.services.GroupensteinConnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_login);

        GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());
        String userToken = groupensteinConnect.GetPreference(GroupensteinConnect.LoggedInUserTokenKey);
        groupensteinConnect.SavePreference(GroupensteinConnect.LoggedInUserTokenKey,"");

        /*log out a user when they click sign out, unregister device*/
        if (userToken != "") {
            UserLogoutTask logoutTask = new UserLogoutTask(userToken);
            logoutTask.execute((Void) null);
        }
        //End log out code

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        TextView createAccountLink = (TextView) findViewById(R.id.textCreateAccount);
        //createAccountLink.setText(Html.fromHtml("<a href='#'>Create New Account</a>"));
        createAccountLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                RedirectToCreateAccount();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    public void RedirectToCreateAccount()
    {
        startActivity(new Intent(this.getApplicationContext(), CreateAccountActivity.class));
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
        //return password.length() > 4;
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            boolean validLogin = TryLogin();
            return validLogin;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        public boolean TryLogin()
        {
            Map<String, String> comment = new HashMap<String, String>();

            comment.put("Email", mEmail);
            comment.put("Password", mPassword);
            String json = new GsonBuilder().create().toJson(comment, Map.class);
            GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());

            String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/login", json);

            Gson gson = new GsonBuilder().create();
            UserTokenModel userTokenModel = gson.fromJson(jsonResponse, UserTokenModel.class);
            String validToken = userTokenModel.Token;
            if (validToken.startsWith("0000") || validToken.equals(""))
            {
                return  false;
            }
            else
            {
                groupensteinConnect.SavePreference(GroupensteinConnect.LoggedInUserTokenKey,validToken);
                startActivity(new Intent(getApplicationContext(), GroupViewActivity.class));
                return true;
            }
        }



    }


    public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

        private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        private GoogleCloudMessaging gcm =null;
        private String regid = null;
        private String localUserToken = null;
        UserLogoutTask(String userToken) {
            localUserToken = userToken;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (checkPlayServices())
            {
                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                regid = getRegistrationId(getApplicationContext());

                if (!regid.isEmpty() && regid != "")
                {
                    TryLogout();
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }

        @Override
        protected void onCancelled() {

        }

        public boolean TryLogout()
        {
            GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());
            String userToken = localUserToken;
            Map<String, String> comment = new HashMap<String, String>();
            comment.put("Token",userToken);
            comment.put("DeviceId",regid);
            comment.put("DeviceOs","1");
            String json = new GsonBuilder().create().toJson(comment, Map.class);
            String jsonResponse = groupensteinConnect.MakeRequest("https://www.groupenstein.com/groupensteinmobileapi/logout", json);
            Gson gson = new GsonBuilder().create();
            ValidationModel validationModel = gson.fromJson(jsonResponse, ValidationModel.class);
            groupensteinConnect.SavePreference(GroupensteinConnect.PROPERTY_REG_ID,"");
            groupensteinConnect.SavePreference(GroupensteinConnect.PROPERTY_APP_VERSION,"");
            return true;
        }


        private boolean checkPlayServices() {

            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
            if (resultCode != ConnectionResult.SUCCESS) {
                return false;
            }
            return true;
        }

        private String getRegistrationId(Context context)
        {
            GroupensteinConnect groupensteinConnect = new GroupensteinConnect(getApplicationContext());
            String registrationId = groupensteinConnect.GetPreference(groupensteinConnect.PROPERTY_REG_ID);
            if (registrationId.isEmpty()) {
                return "";
            }
            String appVersion = groupensteinConnect.GetPreference(groupensteinConnect.PROPERTY_APP_VERSION);
            int registeredVersion =  Integer.parseInt(appVersion);
            int currentVersion = getAppVersion(context);
            if (registeredVersion != currentVersion) {
                return "";
            }
            return registrationId;
        }

        private int getAppVersion(Context context)
        {
            try
            {
                PackageInfo packageInfo = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0);
                return packageInfo.versionCode;
            }
            catch (PackageManager.NameNotFoundException e)
            {
                throw new RuntimeException("Could not get package name: " + e);
            }
        }

    }
    }



