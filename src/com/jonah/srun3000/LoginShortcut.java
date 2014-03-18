package com.jonah.srun3000;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.jonah.srun3000.http.Client;

/**
 * Created by jonah on 14-3-17.
 */
public class LoginShortcut extends Activity {

    private SharedPreferences current;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        current = getSharedPreferences("current",0);
        String username = current.getString("username","");
        String password = current.getString("password","");

        AsyncTask loginTask = new LoginTask();
        loginTask.execute(username,password);
        // create shortcut if requested
        Intent.ShortcutIconResource icon =
                Intent.ShortcutIconResource.fromContext(this, R.drawable.launcher);

        Intent intent = new Intent();

        Intent launchIntent = new Intent(this,LoginShortcut.class);

        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "立即登录");
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

        setResult(RESULT_OK, intent);


    }
    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(LoginShortcut.this)
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

    private void saveUid(String uid){
        SharedPreferences.Editor editor = current.edit();
        editor.putString("uid",uid);
        editor.commit();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class LoginTask extends AsyncTask<Object, Void, Boolean> {
        private Client client;

        @Override
        protected Boolean doInBackground(Object... strings) {
            String username = (String) (strings[0]);
            String password = (String) (strings[1]);

            client = new Client(username, password, LoginShortcut.this);

            return client.login();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                showNotification();
                saveUid(client.getUid());
                finish();
            }else{
                LoginMessage message = new LoginMessage(LoginShortcut.this);
                Toast.makeText(LoginShortcut.this, message.getMessage(client.getLoginResult()),Toast.LENGTH_SHORT).show();

                finish();
            }

        }
    }

}