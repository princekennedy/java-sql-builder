package com.salvation.emi.sql;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseEngine {

    private SqliteHandler sqliteHandler;
    QueryBuilder queryBuilder;

    public DatabaseEngine(Context context) {
        this.sqliteHandler = new SqliteHandler(context);
        queryBuilder = this.sqliteHandler.queryBuilder;
    }


    public Map<String, String> authUser( String username, String password){
        return  this.sqliteHandler.queryBuilder
                .table("users")
                .where("password", password.trim())
                .orWhere("username", username.trim())
                .orWhere("email", username.trim())
                .getOne();
    }

    public ArrayList<Map<String, String>> getFormData(String formName){
        return this.sqliteHandler.queryBuilder.table("tbl_inputs")
                .select("tbl_inputs.*", "tbl_forms.name as form_name")
                .join("tbl_forms", "tbl_forms.id", " tbl_inputs.form_id" ,"INNER JOIN " )
                .where("tbl_forms.name", formName)
                .orderBy("tbl_inputs.position" ,"ASC")
                .getAll();
    }

}
