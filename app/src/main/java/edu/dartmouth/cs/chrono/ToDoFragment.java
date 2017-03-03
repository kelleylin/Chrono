package edu.dartmouth.cs.chrono;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class ToDoFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<ArrayList<Task>> {

    public ArrayList<Task> entries;
    private TaskViewAdapter taskAdapter;
    private LoaderManager loaderManager;
    private TaskDbHelper taskDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load entries
        loaderManager = this.getLoaderManager();
        loaderManager.initLoader(1, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_to_do, container, false);
        setRetainInstance(true);

        entries = new ArrayList<Task>();
        taskDatabase = new TaskDbHelper(getActivity());

        // Load entries
        loaderManager = this.getLoaderManager();
        loaderManager.initLoader(1, null, this);

        // Set up list view
        ListView entryView = (ListView)v.findViewById(R.id.task_list);
        taskAdapter = new TaskViewAdapter(getActivity(), entries);
        entryView.setAdapter(taskAdapter);

        return v;
    }

    /**
     * Update entries when the tab is selected
     */
    public void setUserVisibleHint(boolean userVisibleHint) {
        if (userVisibleHint) {
            updateEntries();
        }
    }

    /**
     * Update entries after there has been a change
     */
    public void updateEntries() {
        if(loaderManager != null)
            loaderManager.initLoader(1, null, this).forceLoad();
    }

    @Override
    public Loader<ArrayList<Task>> onCreateLoader(int id, Bundle args) {
        return new LoadTasks(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Task>> loader, ArrayList<Task> data) {
        // Clear and reload list to be displayed
        entries.clear();
        entries.addAll(data);
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Task>> loader) {
        // Clear all data
        entries.clear();
        taskAdapter.notifyDataSetChanged();
    }
}
