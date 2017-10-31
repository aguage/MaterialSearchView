package com.science.materialsearch.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.science.materialsearch.bean.SearchItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 幸运Science
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2016/9/23
 */

public class SearchHistoryTable {

    private static String TAG = SearchHistoryTable.class.getSimpleName();

    private static int mConnectionCount = 0;
    private final SearchHistoryDbHelper dbHelper;
    private SQLiteDatabase db;

    public SearchHistoryTable(Context context) {
        dbHelper = new SearchHistoryDbHelper(context);
    }

    // FOR onResume AND onPause
    public void open() throws SQLException {
        if (mConnectionCount == 0) {
            db = dbHelper.getWritableDatabase();
        }
        mConnectionCount++;
    }

    public void close() {
        mConnectionCount--;
        if (mConnectionCount == 0) {
            dbHelper.close();
        }
    }

    /**
     * 搜索框输入确认后添加一条记录
     *
     * @param item
     */
    public void addItem(SearchItem item) {
        open();
        ContentValues values = new ContentValues();
        if (!checkText(item.getText().toString())) {
            values.put(SearchHistoryDbHelper.SEARCH_HISTORY_COLUMN_TEXT, item.getText().toString());
            db.insert(SearchHistoryDbHelper.SEARCH_HISTORY_TABLE, null, values);
        } else {
            values.put(SearchHistoryDbHelper.SEARCH_HISTORY_COLUMN_ID, getLastItemId() + 1);
            db.update(SearchHistoryDbHelper.SEARCH_HISTORY_TABLE, values, SearchHistoryDbHelper.SEARCH_HISTORY_COLUMN_ID
                    + " = ? ", new String[]{Integer.toString(getItemId(item))});
        }
        close();
    }

    /**
     * 根据输入文字得到id
     *
     * @param item
     * @return
     */
    private int getItemId(SearchItem item) {
        open();
        String query = "SELECT " + SearchHistoryDbHelper.SEARCH_HISTORY_COLUMN_ID +
                " FROM " + SearchHistoryDbHelper.SEARCH_HISTORY_TABLE +
                " WHERE " + SearchHistoryDbHelper.SEARCH_HISTORY_COLUMN_TEXT + " = ?";
        Cursor res = db.rawQuery(query, new String[]{item.getText().toString()});
        res.moveToFirst();
        int id = res.getInt(0);
        close();
        res.close();
        return id;
    }

    /**
     * 得到表中最后一个item记录的id
     *
     * @return
     */
    private int getLastItemId() {
        open();
        String sql = "SELECT " + SearchHistoryDbHelper.SEARCH_HISTORY_COLUMN_ID + " FROM "
                + SearchHistoryDbHelper.SEARCH_HISTORY_TABLE;
        Cursor res = db.rawQuery(sql, null);
        res.moveToLast();
        int count = res.getInt(0);
        close();
        res.close();
        return count;
    }

    /**
     * 检查当前输入的文字是否存在表中
     *
     * @param text
     * @return true：存在
     */
    private boolean checkText(String text) {
        open();

        String query = "SELECT * FROM " + SearchHistoryDbHelper.SEARCH_HISTORY_TABLE + " WHERE "
                + SearchHistoryDbHelper.SEARCH_HISTORY_COLUMN_TEXT + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{text});

        boolean hasObject = false;

        if (cursor.moveToFirst()) {
            hasObject = true;
        }

        close();
        cursor.close();
        return hasObject;
    }

    public List<SearchItem> getAllItems() {
        open();
        List<SearchItem> list = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + SearchHistoryDbHelper.SEARCH_HISTORY_TABLE;
        selectQuery += " ORDER BY " + SearchHistoryDbHelper.SEARCH_HISTORY_COLUMN_ID + " DESC";

        if (db != null) {
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    SearchItem item = new SearchItem();
                    item.setText(cursor.getString(1));
                    list.add(item);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } else {
            Log.e(TAG, "getAllItems>>>>>>>>>>>>>>");
        }
        close();
        return list;
    }

    public void clearDatabase() {
        open();
        db.delete(SearchHistoryDbHelper.SEARCH_HISTORY_TABLE, null, null);
        close();
    }

    public int getItemsCount() {
        open();
        String countQuery = "SELECT * FROM " + SearchHistoryDbHelper.SEARCH_HISTORY_TABLE;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        close();
        return count;
    }
}
