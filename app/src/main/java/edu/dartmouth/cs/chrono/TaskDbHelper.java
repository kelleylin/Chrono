package edu.dartmouth.cs.chrono;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by kelle on 3/2/2017.
 */

public class TaskDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "TasksDB";

    public static final String TABLE_NAME_ENTRIES = "Tasks";
    public static final int DATABASE_VERSION = 1;

    public static final String KEY_ID = "_id";
    public static final String KEY_TASK_NAME= "_task_name";
    public static final String KEY_START_TIME = "_start_time";
    public static final String KEY_DEADLINE = "_deadline";
    public static final String KEY_BLOCK_ID = "_block_id";
    public static final String KEY_URGENCY = "_urgency";
    public static final String KEY_IMPORTANCE = "_importance";
    public static final String KEY_DURATION = "_DURATION";
    public static final String KEY_TASK_TYPE = "_TASK_TYPE";

    private static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME_ENTRIES + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_TASK_NAME + " TEXT, "
            + KEY_START_TIME + " LONG, "
            + KEY_DEADLINE + " LONG, "
            + KEY_BLOCK_ID + " INTEGER, "
            + KEY_URGENCY + " INTEGER, "
            + KEY_IMPORTANCE + " INTEGER, "
            + KEY_DURATION + " INTEGER, "
            + KEY_TASK_TYPE + " INTEGER "
            + "); ";

    public static final String[] columns = new String[]{KEY_ID, KEY_TASK_NAME, KEY_START_TIME,
            KEY_DEADLINE, KEY_BLOCK_ID, KEY_URGENCY, KEY_IMPORTANCE, KEY_DURATION, KEY_TASK_TYPE };


    public TaskDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ENTRIES);
        onCreate(db);
    }

    public long insertTask(Task entry){
        ContentValues value = new ContentValues();

        value.put(KEY_ID, entry.getId());
        value.put(KEY_TASK_NAME, entry.getTaskName());
        value.put(KEY_START_TIME, entry.getStartTime());
        value.put(KEY_DEADLINE, entry.getDeadline());
        value.put(KEY_BLOCK_ID, entry.getBlockID());
        value.put(KEY_URGENCY, entry.getTaskUrgency());
        value.put(KEY_IMPORTANCE, entry.getTaskImportance());
        value.put(KEY_DURATION, entry.getDuration());
        value.put(KEY_TASK_TYPE, entry.getTaskType());

        SQLiteDatabase database = getWritableDatabase();
        long id = database.insert(TABLE_NAME_ENTRIES, null, value);
        database.close();
        return id;
    }

    public long updateTask(long id) {
        Task entry = fetchEntryByIndex(id);

        ContentValues value = new ContentValues();

        value.put(KEY_ID, entry.getId());
        value.put(KEY_TASK_NAME, entry.getTaskName());
        value.put(KEY_START_TIME, entry.getStartTime());
        value.put(KEY_DEADLINE, entry.getDeadline());
        value.put(KEY_BLOCK_ID, entry.getBlockID());
        value.put(KEY_URGENCY, entry.getTaskUrgency());
        value.put(KEY_IMPORTANCE, entry.getTaskImportance());
        value.put(KEY_DURATION, entry.getDuration());
        value.put(KEY_TASK_TYPE, entry.getTaskType());

        SQLiteDatabase database = getWritableDatabase();
        database.update(TABLE_NAME_ENTRIES, value, KEY_ID + "=" + id, null);
        database.close();

        return id;
    }

    public void removeEntry(long index){
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_NAME_ENTRIES, KEY_ID + "=" + index, null);
        database.close();
    }

    public Task fetchEntryByIndex(long id) throws SQLException {
        SQLiteDatabase database = getReadableDatabase();
        Task entry = null;

        Cursor cursor = database.query(true, TABLE_NAME_ENTRIES, columns, KEY_ID + "="
                + id, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            entry = cursorToEntry(cursor);
        }

        cursor.close();
        database.close();
        return entry;
    }

    public ArrayList<Task> fetchEntries() {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<Task> entryList = new ArrayList<Task>();

        Cursor cursor = database.query(TABLE_NAME_ENTRIES, columns,
                null, null, null, null, null);

        while(cursor.moveToNext()) {
            Task entry = cursorToEntry(cursor);
            entryList.add(entry);
        }

        cursor.close();
        database.close();

        return entryList;
    }

    private Task cursorToEntry(Cursor cursor) {
        Task entry = new Task();

        entry.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
        entry.setTaskName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
        entry.setStartTime(cursor.getLong(cursor.getColumnIndex(KEY_START_TIME)));
        entry.setDeadline(cursor.getLong(cursor.getColumnIndex(KEY_DEADLINE)));
        entry.setBlockID(cursor.getInt(cursor.getColumnIndex(KEY_BLOCK_ID)));
        entry.setTaskUrgency(cursor.getInt(cursor.getColumnIndex(KEY_URGENCY)));
        entry.setTaskImportance(cursor.getInt(cursor.getColumnIndex(KEY_IMPORTANCE)));
        entry.setDuration(cursor.getInt(cursor.getColumnIndex(KEY_DURATION)));
        entry.setTaskType(cursor.getInt(cursor.getColumnIndex(KEY_TASK_TYPE)));

        return entry;
    }
}
