package com.salvation.emi.sql;
//import  android.database.sqlite;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.salvation.emi.Config;
import com.salvation.emi.api.APIConnection;
import com.salvation.emi.api.APIInterface;
import com.salvation.emi.helper.HelperMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.salvation.emi.helper.HelperMethods.log;

public class SqliteHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyDBName.db";
    private String sqlFile = "sql.json";
    private Boolean syncLocal = true;
    private Boolean syncInternet = false;
    private Context context;
    public QueryBuilder queryBuilder;

    public SqliteHandler(Context context) {
        super(context, DATABASE_NAME , null, 1);
        queryBuilder = new QueryBuilder(getReadableDatabase());
        this.context = context;
    }

    public void init() {
        getSyncInternet();
        getSyncLocal();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        init();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void getSyncLocal(){

        try{
            queryBuilder.table("users").isTableExists();
        }catch (Exception e){
            Log.e("prince", "First run." );
            setSyncLocal(true);
        }
        if(syncLocal.equals(false)) return;
        freshDatabase(loadJSONFromAsset(context, sqlFile));
    }

    public void getSyncInternet(){
        if(syncInternet.equals(false)) return;
        APIConnection apiConnection = new APIConnection(new APIInterface() {
            @Override
            public void onSuccess(Object object) {
                freshDatabase(object.toString());
            }
            @Override
            public void OnError(String message) {
                log(message);
            }
        });

        Map<String, String> req = new HashMap<>();
//        req.put("username", "salvatiokn");
//        req.put("pass", "123");
        apiConnection.connect(req);
    }

    private Boolean freshDatabase(String jsonString){
        SQLiteDatabase db = getReadableDatabase();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            if( jsonObject.has("flat")){
                JSONArray flatSql = jsonObject.getJSONArray("flat");

                for (int i = 0; i < flatSql.length(); i++) {
                    db.execSQL(flatSql.getString(i));
                }

                db.close();
            }else{
                JSONArray sqlDrop = jsonObject.getJSONArray("drop");
                JSONArray sqlCreate = jsonObject.getJSONArray("create");
                JSONArray sqlInsert = jsonObject.getJSONArray("insert");

                for (int i = 0; i < sqlDrop.length(); i++) {
                    db.execSQL(sqlDrop.getString(i));
                }

                for (int i = 0; i < sqlCreate.length(); i++) {
                    db.execSQL(sqlCreate.getString(i));
                }

                for (int i = 0; i < sqlInsert.length(); i++) {
                    db.execSQL(sqlInsert.getString(i));
                }
                db.close();
            }
            setSyncLocal(false);
            setSyncInternet(false);
            return true;
        } catch (JSONException e) {
        // e.printStackTrace();
            log(e.getMessage());
            return false;
        }
    }

    public void setSyncLocal(Boolean syncLocal) {
        this.syncLocal = syncLocal;
    }

    public void setSyncInternet(Boolean syncInternet) {
        this.syncInternet = syncInternet;
    }

    public QueryBuilder  builder(){
        return  queryBuilder;
    }

    private String loadJSONFromAsset(Context context, String path) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(path);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}