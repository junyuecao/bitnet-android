package com.jonah.srun3000.http;

import android.app.Activity;
import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jonah on 13-6-14.
 */
public class Client {

    private String loginURL = "http://10.0.0.55/cgi-bin/do_login";
    private String logoutURL = "http://10.0.0.55/cgi-bin/do_logout";
    private String forceLogoutURL = "http://10.0.0.55/cgi-bin/force_logout";
    private SharedPreferences preferences;
    private String result ;

    private String username;
    private String password;
    private boolean free;

    public String getUid() {
        return uid;
    }

    private String uid;

    public String getLoginResult() {
        return loginResult;
    }

    private String loginResult;

    public String getLogoutResult() {
        return logoutResult;
    }

    private String logoutResult;

    private String forceLogoutResult;

    public String getForceLogoutResult(){
        return forceLogoutResult;
    }


    public Client(String username, String password, boolean free,Activity source){
        this.username = username;
        this.password = password;
        this.free = free;
        preferences = source.getSharedPreferences("current", 0);

    }


    public boolean login(){

        Pattern pattern = Pattern.compile("(^[ ]+)|([ ]+$)");
        Matcher userMatcher = pattern.matcher(username);
        Matcher passMatcher = pattern.matcher(password);
        this.username = userMatcher.replaceAll("");
        this.password = passMatcher.replaceAll("");


        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            md5.update(password.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] bs = md5.digest();
        StringBuffer sb = new StringBuffer();

        for(int i=0;i<bs.length;i++){
            int v = bs[i]&0xff;
            if(v<16){
                sb.append(0);
            }
            sb.append(Integer.toHexString(v));
        }
        String pass2 = sb.substring(8,24);

        String drop = free?"1":"0";
        String data = "username="+username+"&password="+pass2+"&drop=" + drop +"&type=1&n=100";
        HttpUtility httpUtility = new HttpUtility();

        pattern = Pattern.compile("^[\\d]+$",Pattern.CASE_INSENSITIVE);

        try {
            result = httpUtility.doPost(loginURL,data);
            Matcher matcher = pattern.matcher(result);

            if(!matcher.find()){
                this.loginResult = result;
                return false;
            }
            this.loginResult = "login_ok";
            this.uid = result;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.loginResult = "unknown_error";
        return false;
    }


    public boolean forceLogout(){

        Pattern pattern = Pattern.compile("(^[ ]+)|([ ]+$)");
        Matcher userMatcher = pattern.matcher(username);
        Matcher passMatcher = pattern.matcher(password);
        this.username = userMatcher.replaceAll("");
        this.password = passMatcher.replaceAll("");

        String data = "username="+username+"&password="+password+"&drop=0&type=1&n=1";

        HttpUtility httpUtility = new HttpUtility();
        try {
            result = httpUtility.doPost(forceLogoutURL,data);
            if(!result.equals("logout_ok")){
                this.logoutResult = result;
                return false;
            }
            this.logoutResult = result;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.logoutResult = "unknown_error";
        return false;
    }

    public boolean logout(){
        uid = preferences.getString("current_uid",null);

        if(uid == null){
            this.logoutResult = "not_login";
            return false;
        }

        String data = "uid="+uid;

        HttpUtility httpUtility = new HttpUtility();
        try {
            result = httpUtility.doPost(logoutURL,data);
            if(!result.equals("logout_ok")){
                this.logoutResult = result;
                return false;
            }
            this.logoutResult = result;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.logoutResult = "unknown_error";
        return false;
    }

}
