package com.jonah.srun3000;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonah on 13-6-17.
 */
public class LoginMessage{
    public LoginMessage(Context context){
        this.context = context;
        init();
    }
    private Context context ;
    private Map<String,String> messages = new HashMap<String, String>();

    private void init(){
        messages.put("login_ok", context.getResources().getString(R.string.login_ok));
        messages.put("user_tab_error", context.getResources().getString(R.string.user_tab_error));
        messages.put("username_error", context.getResources().getString(R.string.username_error));
        messages.put("non_auth_error", context.getResources().getString(R.string.non_auth_error));
        messages.put("password_error", context.getResources().getString(R.string.password_error));
        messages.put("status_error", context.getResources().getString(R.string.status_error));
        messages.put("available_error", context.getResources().getString(R.string.available_error));
        messages.put("ip_exist_error", context.getResources().getString(R.string.ip_exist_error));
        messages.put("usernum_error", context.getResources().getString(R.string.usernum_error));
        messages.put("online_num_error", context.getResources().getString(R.string.online_num_error));
        messages.put("mode_error", context.getResources().getString(R.string.mode_error));
        messages.put("time_policy_error", context.getResources().getString(R.string.time_policy_error));
        messages.put("flux_error" , context.getResources().getString(R.string.flux_error));
        messages.put("minutes_error" , context.getResources().getString(R.string.minutes_error));
        messages.put("ip_error" , context.getResources().getString(R.string.ip_error));
        messages.put("mac_error" , context.getResources().getString(R.string.mac_error));
        messages.put("sync_error" , context.getResources().getString(R.string.sync_error));
    }


    public String getMessage(String key){
        return messages.get(key);
    }



}
