package edu.dartmouth.cs.chrono;

/**
 * Created by kelle on 3/3/2017.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Adapter to allow for multi-line views in to-do list fragment
 */
public class TaskViewAdapter extends ArrayAdapter<Task> {

    private Context mContext;
    private ArrayList<Task> entryList;
    private static LayoutInflater inflater = null;

    public TaskViewAdapter(Context c, ArrayList<Task> viewedList) {
        super(c, R.layout.two_lines_list_item, viewedList);
        mContext = c;
        entryList = viewedList;

        inflater = (LayoutInflater) c.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return entryList.size();
    }

    @Override
    public Task getItem(int position) {
        return entryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void onDeleteTask() {
        Toast.makeText(getContext(), "Task view.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            v = inflater.inflate(R.layout.two_lines_list_item, null);
        }

        TextView title = (TextView) v.findViewById(R.id.task_title);
        TextView text = (TextView) v.findViewById(R.id.task_time);
        TextView deadlineText = (TextView) v.findViewById(R.id.task_deadline);

        final Task entry = entryList.get(position);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss MMM dd yyyy ");
        Long startTime = entry.getStartTime();
        String dateString = format.format(startTime);
        Long deadline = entry.getDeadline();
        String deadlineString = format.format(deadline);

        int duration = entry.getDuration();
        int min = duration / 60;
        int secs = duration % 60;
        String durationString = min + " hours, " + secs + " minutes";

        String textString = dateString + ": " + durationString;

        title.setText(entry.getTaskName());
        text.setText(textString);
        deadlineText.setText(deadlineString);

        // Hide buttons
        Button deleteButton = (Button)v.findViewById(R.id.task_delete);

        if (entry.getTaskType() == 0) {
            deleteButton.setVisibility(View.GONE);
        } else if (entry.getTaskType() == 1) {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EntryDeleteWorker entryDeleteWorker = new EntryDeleteWorker();
                    entryDeleteWorker.execute(entry.getId());
                    entryList.remove(position);
                    notifyDataSetChanged();
                }
            });
        }

        return v;
    }

    /**
     * Worker thread to delete entry in the database in the background
     * without interfering with interface
     */
    class EntryDeleteWorker extends AsyncTask<Long, Void, String> {
        @Override
        protected String doInBackground(Long... params) {
            TaskDbHelper taskDatabase = new TaskDbHelper(getContext());
            taskDatabase.removeEntry(params[0]);
            ArrayList<Task> current = taskDatabase.fetchEntries();
            double score = ScoreFunction.scoreSchedule(current);
            Log.d("SCORE", "DELETED TASK | Score of schedule: " + Math.round(score));
            return String.valueOf(params[0]);
        }

        // Confirm entry deleted
        @Override
        protected void onPostExecute(String newId) {
            Toast.makeText(getContext(), "Task completed.", Toast.LENGTH_SHORT).show();
        }
    }
}

