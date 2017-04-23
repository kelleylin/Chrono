package edu.dartmouth.cs.chrono;

/**
 * Created by kelle on 3/3/2017.
 */

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

/**
 * Used to help load tasks in background
 */
public class LoadTasks extends AsyncTaskLoader<ArrayList<Task>> {
    Context c;

    public LoadTasks(Context context) {
        super(context);
        c = context;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * Load entries in background
     * @return
     */
    @Override
    public ArrayList<Task> loadInBackground() {
        TaskDbHelper taskDatabase = new TaskDbHelper(c);
        return taskDatabase.fetchEntriesInOrder();
    }
}
