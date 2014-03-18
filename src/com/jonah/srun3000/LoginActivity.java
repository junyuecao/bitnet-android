package com.jonah.srun3000;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.jonah.srun3000.http.Client;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends OrmLiteBaseActivity<AccountDatabaseHelper> {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private LoginLogoutTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mUsername;
    private String mPassword;
    private boolean mFreeMode;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
//    private CheckBox mFreeModeView;
    private CheckBox mSavePassView;

    //ListView Drawer
    private List<String> mDrawerTitles;
    private ArrayAdapter<String> adapter;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private Dao<Account, String> mAccountDao;
    private SharedPreferences current;

    //WifiManager
    private WifiManager wifiManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        mAccountDao = getHelper().getDao();
        current = getSharedPreferences("current",0);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
//        mFreeModeView = (CheckBox) findViewById(R.id.free_mode);
        mSavePassView = (CheckBox) findViewById(R.id.save_pass);

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
        mUsernameView.setText(current.getString("username",""));
        mPasswordView.setText(current.getString("password",""));
        mSavePassView.setChecked(true);

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);


        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                attemptLogout();
            }
        });
        findViewById(R.id.info_button).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                openInfoActivity();
            }
        });
        findViewById(R.id.force_logout_button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(R.string.logout_confirm)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                attemptForceLogout();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                                dialog.dismiss();
                            }
                        });
                // Create the AlertDialog object and return it
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //Set up the listview

        setUpDrawerList();

        mTitle = getTitle();
        mDrawerTitle = getText(R.string.prompt_drawer_title);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close){
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                setUpDrawerList();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        findViewById(R.id.save_to_db_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameView.getText().toString();
                String password = mPasswordView.getText().toString();

                Account account = new Account(username,password);
                try {
                    mAccountDao.createOrUpdate(account);
                    mDrawerLayout.openDrawer(mDrawerList);
                } catch (SQLException e) {
                    Toast.makeText(LoginActivity.this,R.string.incomplete,1000).show();
                    e.printStackTrace();
                }

            }
        });

        registerForContextMenu(mDrawerList);

//        getActionBar().;
//        getActionBar().setHomeButtonEnabled(true);
    }

    private void openInfoActivity() {
        String uid = current.getString("uid","null");
        String infoURL ="http://10.0.0.55/user_info.php?uid=";
        if(uid == null){
            Toast.makeText(this,R.string.not_login,500).show();
            return;
        }
        Uri uri = Uri.parse(infoURL+uid);
        Intent intent = new Intent(this,InfoActivity.class);
        intent.putExtra("uri",uri);
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.delete_context, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteAccount(info.targetView);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteAccount(View view) {
        String username = ((TextView)view).getText().toString();

        if(mDrawerTitles.remove(username)){
            try {
                mAccountDao.delete(mAccountDao.queryForId(username));
                Toast.makeText(this,getString(R.string.delete_ok),1000).show();
                adapter.notifyDataSetChanged();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpDrawerList(){
        mDrawerTitles = new ArrayList<String>();
        for (Account account : mAccountDao) {
            mDrawerTitles.add(account.getUsername());
        }

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setItemChecked(0,true);
        adapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerTitles);
        adapter.setNotifyOnChange(true);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    }
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_add).setVisible(!drawerOpen);
        MenuItem item= menu.findItem(R.id.action_wifi);
        MenuItem autoFinishMenuItem = menu.findItem(R.id.auto_finish);

        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        boolean isWifiEnabled = !(wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED);

        item.setChecked(isWifiEnabled);
        item.setTitle(isWifiEnabled?R.string.opened_wifi:R.string.open_wifi);
        item.setEnabled(!isWifiEnabled);

        SharedPreferences settings = getSharedPreferences("settings", 0);
        boolean isAutoFinish = settings.getBoolean("autoFinish", false);

        autoFinishMenuItem.setChecked(isAutoFinish);

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle item selection
        switch (item.getItemId()) {
//            case R.id.action_forgot_password:
//                Toast.makeText(this,getText(R.string.action_forgot_password),1000).show();
//                return true;
            case R.id.action_about:
                showAboutDialog();
                return true;
            case R.id.action_wifi:
                openWifi(item);
                return true;
            case R.id.auto_finish:
                saveAutoFinishPref(item.isChecked());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openWifi(MenuItem item) {
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED){
            wifiManager.setWifiEnabled(true);
            item.setChecked(true);
            item.setTitle(R.string.opened_wifi);
            item.setEnabled(false);
        }
    }

    private void saveAutoFinishPref(Boolean isChecked){
        // set preference
        SharedPreferences settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("autoFinish", !isChecked);
        editor.commit();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about)
                .setMessage(R.string.help)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString().trim();
        mPassword = mPasswordView.getText().toString().trim();
//        mFreeMode = mFreeModeView.isChecked();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mAuthTask = new LoginLogoutTask();
            mAuthTask.execute("login");// Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);

        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptForceLogout() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString().trim();
        mPassword = mPasswordView.getText().toString().trim();
//        mFreeMode = mFreeModeView.isChecked();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mAuthTask = new LoginLogoutTask();
            mAuthTask.execute("force_logout");// Show a progress spinner, and kick off a background task to
//            mAuthTask.execute((Void) null);// Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_force_logout);
            showProgress(true);

        }
    }

    public void attemptLogout() {
        if (mAuthTask != null) {
            return;
        }

            mAuthTask = new LoginLogoutTask();
            mAuthTask.execute("logout");// Show a progress spinner, and kick off a background task to
//            mAuthTask.execute((Void) null);// Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_logout);
            showProgress(true);

    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            //             and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class LoginLogoutTask extends AsyncTask<String, Void, Boolean> {
        private String action;
        private Client client;
        private LoginMessage loginMessage;
        private LogoutMessage logoutMessage;
        @Override
        protected Boolean doInBackground(String... params) {
            action = params[0];

            // TODO: attempt authentication against a network service.
            client = new Client(mUsername, mPassword , LoginActivity.this);
            if(action.equals("login")){
                try {
                    if(client.login()){
                        SharedPreferences.Editor editor = current.edit();
                        editor.putString("username", String.valueOf(mUsernameView.getText()));
                        editor.putString("password", String.valueOf(mPasswordView.getText()));
                        editor.putString("uid",client.getUid());
                        editor.commit();
                        if(mSavePassView.isChecked()){
                            mAccountDao.createOrUpdate(new Account(mUsername,mPassword));
                        }
                        return true;
                    }else{
                        return false;
                    }
                    } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }else if(action.equals("force_logout")){
                try {

                    if(client.forceLogout()){
                        return true;
                    }else{
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }else if(action.equals("logout")){
                try {

                    if(client.logout()){
                        return true;
                    }else{
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }else{
                return false;
            }


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if(action.equals("login")){
                if (success) {
                    showNotification();
                    saveCurrent(client.getUid());
                    finishAsSettings();
                } else {
                    loginMessage = new LoginMessage(LoginActivity.this);
                    String message = loginMessage.getMessage(client.getLoginResult());
                    Toast.makeText(LoginActivity.this,message==null?getString(R.string.unknown_error):message,500).show();
                    //如果是密码错误的话

                    if(message!=null&&message.equals("password_error")){
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                }
            }else if(action.equals("force_logout")){
                if (success) {
                    Toast.makeText(LoginActivity.this,getString(R.string.force_logout_ok),1500).show();
//                finish();
                } else {
                    logoutMessage = new LogoutMessage(LoginActivity.this);
                    String message = logoutMessage.getMessage(client.getLogoutResult());
                    Toast.makeText(LoginActivity.this,message==null?getString(R.string.unknown_error):message,500).show();
                    //如果是密码错误的话

                    if(message!=null && message.equals("password_error")){
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                }
            }else if(action.equals("logout")){
                if (success) {
                    Toast.makeText(LoginActivity.this,getString(R.string.logout_ok),1500).show();
//                finish();
                } else {
                    logoutMessage = new LogoutMessage(LoginActivity.this);
                    String message = logoutMessage.getMessage(client.getLogoutResult());
                    Toast.makeText(LoginActivity.this,message==null?getString(R.string.unknown_error):message,500).show();
                    //如果是密码错误的话

                    if(message!=null && message.equals("password_error")){
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                    }
                }
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void saveCurrent(String uid) {
        SharedPreferences.Editor editor = current.edit();
        editor.putString("username",mUsername);
        editor.putString("password",mPassword);
        editor.putString("uid",uid);

    }

    private void finishAsSettings() {

        SharedPreferences settings = getSharedPreferences("settings",0);
        Boolean isAutoFinish = settings.getBoolean("autoFinish",false);
        if(isAutoFinish){
            finish();
        }
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(LoginActivity.this)
                        .setSmallIcon(R.drawable.launcher)
                        .setContentTitle("")
                        .setContentText("")
                        .setTicker("登录成功")
                        .setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
        mNotificationManager.cancel(1);
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            Account account = null;
            TextView textView = (TextView)view;
            String username = textView.getText().toString();
            try {
                account = mAccountDao.queryForId(username);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(account != null){

                mUsernameView.setText(account.getUsername());
                mPasswordView.setText(account.getPassword());
                mDrawerList.setItemChecked(position, true);

                mSavePassView.setChecked(true);

                SharedPreferences.Editor editor = current.edit();
                editor.putString("username",account.getUsername());
                editor.putString("password",account.getPassword());
                editor.commit();
            }

            mDrawerLayout.closeDrawer(mDrawerList);

        }
    }

    private class ConfirmDialogFragment extends android.support.v4.app.DialogFragment{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.logout_confirm)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


}
