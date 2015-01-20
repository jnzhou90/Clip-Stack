package com.catchingnow.tinyclipboards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by heruoxin on 14/12/9.
 */
public class Storage {
    private static final String TABLE_NAME = "cliphistory";
    private static final String CLIP_STRING = "history";
    private static final String CLIP_DATE = "date";
    private StorageHelper dbHelper;
    private SQLiteDatabase db;
    private Context c;
    private List<String> clipsInMemory;
    private boolean isClipsInMemoryChanged = true;

    public Storage(Context context) {
        c = context;
        dbHelper = new StorageHelper(c);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }
    public void close() {
            db.close();
    }

    public List<String> getClipHistory() {
        return getClipHistory("");
    }
    public List<String> getClipHistory(String queryString) {
        if (isClipsInMemoryChanged) {
            open();
            String sortOrder = CLIP_DATE + " DESC";
            String[] COLUMNS = {CLIP_STRING};
            Cursor c;
            if (queryString == null) {
                c = db.query(TABLE_NAME, COLUMNS, null, null, null, null, sortOrder);
            } else {
                c = db.query(TABLE_NAME, COLUMNS, CLIP_STRING+" LIKE '%"+queryString+"%'", null, null, null, sortOrder);
            }
            clipsInMemory = new ArrayList<String>();
            while(c.moveToNext()) {
                clipsInMemory.add(c.getString(0));
            }
            c.close();
            close();
            isClipsInMemoryChanged = false;
        }
        return clipsInMemory;
    }
    public List<String> getClipHistory(int n) {
        List<String> ClipHistory = getClipHistory();
        List<String> thisClips = new ArrayList<String>();
        n = (n > ClipHistory.size() ? ClipHistory.size() : n);
        for (int i=0; i < n; i++) {
            thisClips.add(ClipHistory.get(i));
        }
        return thisClips;
    }
    public boolean deleteClipHistory(String s) {
        open();
        int row_id = db.delete(TABLE_NAME, CLIP_STRING+"='"+s+"'", null);
        close();
        if (row_id == -1) {
            Log.e("Storage", "write db error: deleteClipHistory " + s);
            return false;
        }
        return true;
    }
    public boolean deleteClipHistoryBefore( float days) {
        Date date = new Date();
        long timestamp = (long) (date.getTime() - days*86400000);
        open();
        int row_id = db.delete(TABLE_NAME, CLIP_DATE+"<'"+timestamp+"'", null);
        close();
        if (row_id == -1) {
            Log.e("Storage", "write db error: deleteClipHistoryBefore " + days);
            return false;
        }
        return true;
    }
    public boolean addClipHistory(String currentString) {
        List<String> tmpClips = getClipHistory();
        for (String str: tmpClips) {
            if (str.contains(currentString)) {
                deleteClipHistory(str);
            }
        }
        open();
        Date date = new Date();
        long timestamp = date.getTime();
        ContentValues values = new ContentValues();
        values.put(CLIP_DATE, timestamp);
        values.put(CLIP_STRING, currentString);
        long row_id = db.insert(TABLE_NAME, null, values);
        close();
        if (row_id == -1) {
            Log.e("Storage", "write db error: addClipHistory " + currentString);
            return false;
        }
        isClipsInMemoryChanged = true;
        return true;
    }

//    public void printClips(int n) {
//        for (int i=0; i<n; i++){
//            String s = getClipHistory(n);
//            Log.v("printClips", s);
//        }
//    }
}
