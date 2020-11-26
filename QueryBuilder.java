package com.salvation.emi.sql;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Dear Android, why do I have to write this class myself? Is this to discourage SQL use?
 */
public class QueryBuilder {

    private String[] groupBy;
    private String[] columns;
    private String[] distinct;
    private String[] _values;

    private ArrayList<ContentValues> where;
    private ArrayList<ContentValues> orWhere;
    private ArrayList<ContentValues> whereIn;
    private ArrayList<String> join;
    private ArrayList<String> orderBy;

    private SQLiteDatabase _db;
    private String _sqlType;
    private String _sql;
    private String _columns;
    private String _distincts;
    private String _groupBy;
    private String _having;
    private String _limit;
    private String _orderBy;
    private String _table;
    private String _where;
    private String _orWhere;
    private String _join;
    private String _whereClause;

    private ContentValues contentValues;

    public QueryBuilder(SQLiteDatabase db) {
        _db = db;
        reset();
    }

    private String _compile() {

        switch (_sqlType) {
            case "SELECT":
                _select();
                break;
            case "INSERT":
                _insert();
                break;
            case "UPDATE":
                _update();
                break;
            case "DELETE":
                _delete();
                break;
            default:
                break;
        }
        return _sql;
    }

    private void _insert() {
        _sql = "INSERT INTO ";
    }

    private void _delete() {
        _setWhere();
        _setWhereIn();
        _setOrWhere();
        _sql = "DELETE FROM " + _table + " " +_where;
    }

    private void _update() {
        _sql = "UPDATE " + _table;
    }

    private void _select() {
        _setColumns();
        _setDistinct();
        _setWhere();
        _setWhereIn();
        _setOrWhere();
        _setJoins();
        _setOrderBy();
        _setGroupBy();
        _sql = "SELECT " + _distincts + _columns + " FROM " + _table + _join + _where + _orderBy + _limit + _groupBy;
    }

    private void _setJoins() {
        if (join == null) return;
        for (String s : join) {
            _join += " " + s + " ";
        }
    }

    private void _setOrderBy() {
        if (orderBy == null) return;
        for (String s : orderBy) {
            _orderBy += " " + s + " ";
        }
    }


    private String _whereInClause(String[] whereIn) {
        String usableValue = "";
        for (int i = 0; i < whereIn.length; i++) {
            if (i == 0 )usableValue += " ( ";
            usableValue +=  whereIn[i].trim();
            usableValue += (i < whereIn.length - 1) ? " , " : " ) ";
        }
        return usableValue;
    }

    private void _setWhere() {
        if (where == null || where.size() <= 0) return;

        for (int i = 0; i < where.size(); i++) {
            _where += (i == 0) ? " WHERE " + _joinWhere(where.get(i)) : " " + _joinWhere(where.get(i)) + " ";
            if (i < where.size() - 1) _where += " AND ";
        }
    }
    private void _setWhereIn() {
        if (whereIn == null || whereIn.size() <= 0) return;

        for (int i = 0; i < whereIn.size(); i++) {
            _where += (i == 0) ? " WHERE " + _joinWhereIn(whereIn.get(i)) : " " + _joinWhereIn(whereIn.get(i)) + " ";
            if (i < whereIn.size() - 1) _where += " AND ";
        }
    }

    private void _setOrWhere() {
        if (where == null || where.size() <= 0) return;
        for (int i = 0; i < orWhere.size(); i++) {
            if (where.size() <= 0 && i <= 0) {
                _orWhere += " WHERE ( " + _joinWhere(orWhere.get(i));
            } else if (i <= 0) {
                _orWhere += " AND ( " + _joinWhere(orWhere.get(i));
            } else {
                _orWhere += " " + _joinWhere(orWhere.get(i)) + " ";
            }
            _orWhere += (i < orWhere.size() - 1) ? " OR " : " ) ";
        }
        _where += _orWhere;
    }

    private void _setColumns() {
        if (columns == null || columns.length <= 0) {
            _columns = " * ";
            return;
        }
        _columns = " ";
        for (int i = 0; i < columns.length; i++) {
            _columns += columns[i];
            if (i < columns.length - 1) _columns += ", ";
        }
    }

    private void _setGroupBy() {
        if (groupBy == null || groupBy.length <= 0) return;
        for (int i = 0; i < groupBy.length; i++) {
            _groupBy += " " + groupBy[i] + " ";
            if (i < groupBy.length - 1) _groupBy += ", ";
        }
    }

    private void _setDistinct() {
        _distincts = " ";
        if (distinct == null || distinct.length <= 0) return;
        for (int i = 0; i < distinct.length; i++) {
            _distincts += columns[i];
            if (i < distinct.length - 1) _distincts += " , ";
        }
    }

    private String _joinWhere(ContentValues contentValues) {
        return " " + contentValues.getAsString("key") + contentValues.getAsString("delimeter") + "'" + contentValues.getAsString("value") + "' ";
    }

    private String _joinWhereIn(ContentValues contentValues) {
        return " " + contentValues.getAsString("key") + contentValues.getAsString("delimeter") + contentValues.getAsString("value");
    }

    public QueryBuilder table(String table) {
        _table = table;
        return this;
    }

    public QueryBuilder select(String... columns) {
        this.columns = columns;
        return this;
    }

    public QueryBuilder distinct(String... distinct) {
        this.distinct = distinct;
        return this;
    }

    public QueryBuilder from(String table) {
        _table = table;
        return this;
    }


    public QueryBuilder whereIn(String key, String[] inputIds) {
        ContentValues c = new ContentValues();
        c.put("key", key);
        c.put("delimeter", " IN ");
        c.put("value", _whereInClause(inputIds));
        whereIn.add(c);
        return this;
    }

    public QueryBuilder whereNotIn(String key, String[] inputIds) {
        ContentValues c = new ContentValues();
        c.put("key", key);
        c.put("delimeter", " NOT IN ");
        c.put("value", _whereInClause(inputIds));
        whereIn.add(c);
        return this;
    }

    public QueryBuilder where(String key, String value) {
        ContentValues c = new ContentValues();
        c.put("key", key);
        c.put("delimeter", "=");
        c.put("value", value);
        where.add(c);
        return this;
    }

    public QueryBuilder where(String key, String delimeter, String value) {
        ContentValues c = new ContentValues();
        c.put("key", key);
        c.put("delimeter", delimeter);
        c.put("value", value);
        where.add(c);
        return this;
    }

    public QueryBuilder orWhere(String key, String value) {
        ContentValues c = new ContentValues();
        c.put("key", key);
        c.put("delimeter", "=");
        c.put("value", value);
        orWhere.add(c);
        return this;
    }

    public QueryBuilder orWhere(String key, String delimeter, String value) {
        ContentValues c = new ContentValues();
        c.put("key", key);
        c.put("delimeter", delimeter);
        c.put("value", value);
        orWhere.add(c);
        return this;
    }

    public QueryBuilder limit(String limit) {
        _limit = " LIMIT " + limit;
        return this;
    }

    public QueryBuilder join(String tableName, String link1, String link2, String joinType) {
        join.add(" " + joinType + " " + tableName + " ON " + link1 + " = " + link2 + " ");
        return this;
    }

    public QueryBuilder groupBy(String... groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public QueryBuilder having(String having) {
        _having = having;
        return this;
    }

    public QueryBuilder orderBy(String column, String orderType) {
        orderBy.add(" ORDER BY " + column + " " + orderType + " ");
        return this;
    }

    public String getSql(String sqlType) {
        _sqlType = sqlType;
        _compile();
        reset();
        return _sql;
    }

    public String getSql() {
        _sqlType = "SELECT";
        _compile();
        String sql = _sql;
        reset();
        return sql;
    }

    public Map<String, String> getOne() {
        _sqlType = "SELECT";
        limit("1");
        _compile();
        Map<String, String> map = new HashMap<>();
        Cursor cursor = _db.rawQuery(_sql, null);
        while (cursor.moveToNext()) {
            map = new HashMap<>();
            for (String column : cursor.getColumnNames()) {
                map.put(column, cursor.getString(cursor.getColumnIndex(column)));
            }
            cursor.moveToNext();
        }
        cursor.close();
        reset();
        return map;
    }


    public boolean isTableExists() {
        boolean isExist = false;
        _sqlType = "SELECT";
        distinct(_table);
        table("sqlite_master");
        where("tbl_name", _table);
        _compile();
        Cursor cursor = _db.rawQuery(_sql, null);
//        Cursor cursor = _db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                isExist = true;
            }
            cursor.close();
        }
        reset();
        return isExist;
    }

    public ArrayList<Map<String, String>> getAll() {
        _sqlType = "SELECT";
        _compile();
        ArrayList<Map<String, String>> array_list = new ArrayList<Map<String, String>>();
        Cursor cursor = _db.rawQuery(_sql, null);
        cursor.moveToFirst();
        Map<String, String> map;
        while (!cursor.isAfterLast()) {
            map = new HashMap<>();
            for (String column : cursor.getColumnNames()) {
                map.put(column, cursor.getString(cursor.getColumnIndex(column)));
            }
            array_list.add(map);
            cursor.moveToNext();
        }
        cursor.close();
        reset();
        return array_list;
    }

    public LiveData<ArrayList<Map<String, String>>> getAllArrayListLiveData() {
        _sqlType = "SELECT";
        _compile();
        ArrayList<Map> arrayList = new ArrayList<>();
        Cursor cursor = _db.rawQuery(_sql, null);
        Map map;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            map = new HashMap<String, String>();
            for (String column : cursor.getColumnNames()) {
                map.put(column, cursor.getString(cursor.getColumnIndex(column)));
            }
            arrayList.add(map);
            cursor.moveToNext();
        }
        cursor.close();
        reset();
        MutableLiveData mutableLiveData = new MutableLiveData<ArrayList<Map>>();
        mutableLiveData.postValue(arrayList);
        return  mutableLiveData;
    }

    public Map<String, String> allFlat(String key, String value) {
        _sqlType = "SELECT";
        _compile();
        Cursor cursor = _db.rawQuery(_sql, null);
        Map<String,String> map = new HashMap<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            map.put(cursor.getString(cursor.getColumnIndex(key)), cursor.getString(cursor.getColumnIndex(value)));
            cursor.moveToNext();
        }
        cursor.close();
        reset();
        return map;
    }

    public LiveData<Map<String, String>> getOneLiveData() {
        _sqlType = "SELECT";
        limit("1");
        _compile();
        Map<String, String> map = new HashMap<>();
        Cursor cursor = _db.rawQuery(_sql, null);
        while (cursor.moveToNext()) {
            map = new HashMap<>();
            for (String column : cursor.getColumnNames()) {
                map.put(column, cursor.getString(cursor.getColumnIndex(column)));
            }
            cursor.moveToNext();
        }
        cursor.close();
        reset();
        MutableLiveData mutableLiveData =  new MutableLiveData<Map>();
        mutableLiveData.postValue(map);
        return mutableLiveData;
    }

    public String insert(ContentValues contentValues) {
        _sqlType = "INSERT";
        _compile();
        _sql += _table + " ( "+ contentValues.toString() + " ) ";
        String id = _db.insert(_table, null, contentValues) + "";
        reset();
        return id;
    }

    public String update(ContentValues contentValues) {
        this.contentValues = contentValues;
        _sql += this.contentValues.toString();
        _setWhereClause(contentValues);
        String result = _db.update(_table, this.contentValues, _whereClause, _values) + "";
        _compile();
        reset();
        return result;
    }

    private void _setWhereClause(ContentValues contentValues) {
        ArrayList<ContentValues> mergeWhere = where;
        if (orWhere != null && orWhere.size() > 0) {
            mergeWhere.addAll(orWhere);
        }
        _values = new String[mergeWhere.size()];
        for (int i = 0; i < mergeWhere.size(); i++) {
            _whereClause += mergeWhere.get(i).getAsString("key") + " = ? ";
            if (i < mergeWhere.size() - 1) _whereClause += " AND ";
            _values[i] = mergeWhere.get(i).getAsString("value");
        }
    }


    public int numberOfRows() {
        _sqlType = "SELECT";
        _compile();
        Cursor cursor = _db.rawQuery(_sql, null);
        int rows = cursor.getCount();
        reset();
        cursor.close();
        return rows;
    }

    public void reset() {

        where = new ArrayList<>();
        orWhere = new ArrayList<>();
        whereIn = new ArrayList<>();
        contentValues = new ContentValues();

        join = new ArrayList<>();
        orderBy = new ArrayList<>();

        columns = null;
        distinct = null;
        groupBy = null;
        _values = null;

        _sqlType = "";
        _groupBy = "";
        _having = "";
        _limit = "";
        _orderBy = "";
        _table = "";
        _where = "";
        _orWhere = "";
        _join = "";
        _groupBy = "";
        _orderBy = "";
        _whereClause = "";
    }

    public  boolean delete() {
        _sqlType = "DELETE";
        _compile();
        _db.rawQuery(_sql, null);
        reset();
        return true;
    }


}