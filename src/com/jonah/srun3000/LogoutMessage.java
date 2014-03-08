package com.jonah.srun3000;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonah on 13-6-17.
 */
public class LogoutMessage {
    public LogoutMessage(Context context){
        this.context = context;
        init();
    }
    private Context context ;
    private Map<String,String> messages = new HashMap<String, String>();

    private void init(){
        messages.put("user_tab_error", context.getResources().getString(R.string.user_tab_error));
        messages.put("username_error", context.getResources().getString(R.string.username_error));
        messages.put("password_error", context.getResources().getString(R.string.password_error));
        messages.put("force_logout_ok",context.getResources().getString(R.string.force_logout_ok));
        messages.put("logout_ok", context.getResources().getString(R.string.logout_ok));
        messages.put("logout_error", context.getResources().getString(R.string.logout_error));

    }


    public String getMessage(String key){
        return messages.get(key);
    }



}
