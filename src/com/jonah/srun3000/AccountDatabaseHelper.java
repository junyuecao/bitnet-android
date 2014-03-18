package com.jonah.srun3000;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by jonah on 14-3-16.
 */

public class AccountDatabaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "helloAndroid.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 4;

    // the DAO object we use to access the SimpleData table
    private Dao<Account, String> dao = null;

    public AccountDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            Log.i(AccountDatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, Account.class);
        } catch (SQLException e) {
            Log.e(AccountDatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }

    }



    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i2) {
        try {
            TableUtils.dropTable(connectionSource, Account.class, true);
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(AccountDatabaseHelper.class.getName(), "更新数据库失败", e);
            e.printStackTrace();
        }
    }


    public Dao<Account, String> getDao() {
        if (dao == null) {
            try {
                dao = getDao(Account.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        dao = null;
    }
}
