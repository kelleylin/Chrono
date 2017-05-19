package edu.dartmouth.cs.chrono;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    Boolean failed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Do not allow for a deadline before the current time
        DatePicker datePicker = (DatePicker) findViewById(R.id.add_task_deadline_date);
        Calendar cal = Calendar.getInstance();
        datePicker.setMinDate(cal.getTimeInMillis());
    }

    private ArrayList<Task> saveTask() {
        ArrayList<Task> taskList = new ArrayList<>();

        Task newTask = new Task();

        failed = true;

        // Name
        EditText nameText = (EditText)findViewById(R.id.add_task_name);
        newTask.setTaskName(nameText.getText().toString());

        // Only accept date if set after current date
        DatePicker datePicker = (DatePicker) findViewById(R.id.add_task_deadline_date);
        TimePicker timePicker = (TimePicker) findViewById(R.id.add_task_deadline_time);

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();
        int hours, minutes;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hours = timePicker.getHour();
            minutes = timePicker.getMinute();
        } else {
            hours = timePicker.getCurrentHour();
            minutes = timePicker.getCurrentMinute();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hours, minutes);

        Calendar current = Calendar.getInstance();

        if(calendar.getTimeInMillis() > current.getTimeInMillis())
            newTask.setDeadline(calendar.getTimeInMillis());
        else
            newTask.setDeadline(current.getTimeInMillis());

        /*RatingBar urgencyBar = (RatingBar) findViewById(R.id.add_task_urgency_bar);
        newTask.setTaskUrgency((int)urgencyBar.getRating());*/

        // Importance
        RatingBar importanceBar = (RatingBar) findViewById(R.id.add_task_importance_bar);
        newTask.setTaskImportance((int)importanceBar.getRating());

        // Get duration
        EditText durationHoursText = (EditText)findViewById(R.id.add_task_duration_hours);
        EditText durationMinutesText = (EditText)findViewById(R.id.add_task_duration_minutes);

        int durationHours, durationMinutes;
        try {
            durationHours = Integer.parseInt(durationHoursText.getText().toString());
        } catch (NumberFormatException e){
            durationHours = 0;
        }

        try {
            durationMinutes = durationHours * 60 +
                    Integer.parseInt(durationMinutesText.getText().toString());
        } catch (NumberFormatException e) {
            durationMinutes = durationHours * 60;
        }

        // change the unit of total duration from minutes to milliseconds
        int durationMilli = durationMinutes * 60000;

        // check if the task can actually be finished before the deadline
        if (current.getTimeInMillis() + durationMilli > calendar.getTimeInMillis()) {
            failed = false;
            return taskList;
        }

        newTask.setStartTime(current.getTimeInMillis() + 60000);

        // Get min split time
        EditText splitHoursText = (EditText)findViewById(R.id.add_task_min_split_hours);
        EditText splitMinutesText = (EditText)findViewById(R.id.add_task_min_split_minutes);
        int splitHours, splitMinutes;

        try {
            splitHours = Integer.parseInt(splitHoursText.getText().toString());
        } catch (NumberFormatException e) {
            splitHours = 0;
        }

        try {
            splitMinutes = splitHours * 60 +
                    Integer.parseInt(splitMinutesText.getText().toString());
        } catch (NumberFormatException e) {
            splitMinutes = splitHours * 60;
        }

        // Create multiple tasks

        // No split was indicated
        if (splitMinutes == 0 || splitMinutes == durationMinutes ||
                splitMinutes > durationMinutes) {
            newTask.setDuration(durationMinutes);
            taskList.add(newTask);
        } else {
            int splits = durationMinutes / splitMinutes;
            Log.d("hello", splits +"");

            int splitLeftover = durationMinutes % splitMinutes;

            if(splits == 0) {
                newTask.setDuration(splitLeftover);
                taskList.add(newTask);
            } else {

                // i = 0
                newTask.setDuration(splitMinutes);
                taskList.add(newTask);

                Long newStartTime = newTask.getStartTime();
                for (int i = 1; i <= splits; i++) {

                    // Don't add leftover if there is none
                    if (i == splits && splitLeftover == 0) {
                        break;
                    }

                    Task newTask2 = new Task(newTask.getTaskName(), newTask.getTaskUrgency(),
                            newTask.getTaskImportance(), newTask.getDuration());
                    newTask2.setDeadline(newTask.getDeadline());
                    newTask2.setBlockID(i);

                    newStartTime = newStartTime + splitMinutes * 60 * 1000;
                    newTask2.setStartTime(newStartTime);

                    // Last split
                    if (i == splits) {
                        newTask2.setDuration(splitLeftover);
                    }

                    taskList.add(newTask2);
                }
            }
        }

        return taskList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // start relevant activity
        if (id == R.id.menu_add) {
            ArrayList<Task> tasks = saveTask();

            if (!failed) {
                Toast.makeText(getApplicationContext(), "Not a valid task.",
                        Toast.LENGTH_SHORT).show();
                return true;
            }

            else {
                for (int i = 0; i < tasks.size(); i++) {
                    EntryAddWorker entryAddWorker = new EntryAddWorker();
                    entryAddWorker.execute(tasks.get(i));
                }
                finish();
            }


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Worker thread to create new entry in the database in the background
     * without interfering with interface
     */
    class EntryAddWorker extends AsyncTask<Task, Void, String> {
        @Override
        protected String doInBackground(Task... params) {
            TaskDbHelper taskDatabase = new TaskDbHelper(getApplicationContext());
            long id = taskDatabase.insertTask(params[0]);
            ArrayList<Task> current = taskDatabase.fetchEntries();
            double score = ScoreFunction.scoreSchedule(current);
            Log.d("SCORE", "ADDED TASK | Score of schedule: " + Math.round(score));
            //ArrayList<Long> optimal = ScoreFunction.optimize(current);
            ArrayList<Long> optimal = ScoreFunction.computeSchedule(current);

            if (optimal != null) {
                ArrayList<Task> present = taskDatabase.fetchEntries();
                for (int i = 0; i < optimal.size(); i++) {
                    taskDatabase.updateTaskWithStartTime(present.get(i).getId(), optimal.get(i));
                }
            }
            return String.valueOf(id);
        }

        @Override
        protected void onPostExecute(String newId) {

            Toast.makeText(getApplicationContext(), "New task was added.",
                    Toast.LENGTH_SHORT).show();

        }
    }
}
