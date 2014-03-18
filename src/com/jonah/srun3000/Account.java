package com.jonah.srun3000;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by jonah on 14-3-16.
 */
@DatabaseTable
public class Account {

    @DatabaseField(id = true, canBeNull = false)
    private String username;

    @DatabaseField(canBeNull = false)
    private String password;

    public Account(){

    }

    public Account(String username, String password){
        if(username.equals("")){
            username = null;
        }
        if(password.equals("")){
            password = null;
        }
        this.username = username;
        this.password = password;

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
