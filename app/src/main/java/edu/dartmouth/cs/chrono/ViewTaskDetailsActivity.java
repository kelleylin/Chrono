package edu.dartmouth.cs.chrono;

import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ViewTaskDetailsActivity extends AppCompatActivity {

    DatePicker datePicker;
    TimePicker timePicker;
    boolean editable = false;
    TextView editStartTitle;
    EditText editStart;
    EditText nameText;
    RatingBar importanceBar;
    EditText durationHoursText;
    EditText durationMinutesText;
    Long id;
    boolean initialLoad = true;

    int initialDuration;
    long initialDeadline;
    int initialImportance;
    String initialName;

    boolean meaningfulEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        id = intent.getLongExtra("entry_id", -1);
        initialName = intent.getStringExtra("entry_name");
        initialDeadline = intent.getLongExtra("entry_deadline", -1);
        initialImportance = intent.getIntExtra("entry_importance", -1);
        initialDuration = intent.getIntExtra("entry_duration", -1);

        // Name
        nameText = (EditText)findViewById(R.id.edit_task_name);

        // Deadline
        datePicker = (DatePicker) findViewById(R.id.edit_task_deadline_date);
        timePicker = (TimePicker) findViewById(R.id.edit_task_deadline_time);

        Calendar cal = Calendar.getInstance();
        datePicker.setMinDate(cal.getTimeInMillis());

        // Current Proposed Time
        editStartTitle = (TextView) findViewById(R.id.edit_start_time_title);
        editStart = (EditText) findViewById(R.id.edit_start_time);

        // Importance
        importanceBar = (RatingBar) findViewById(R.id.edit_task_importance_bar);

        // Get duration
        durationHoursText = (EditText)findViewById(R.id.edit_task_duration_hours);
        durationMinutesText = (EditText)findViewById(R.id.edit_task_duration_minutes);

        if (savedInstanceState != null) {
            if (savedInstanceState.getString("editable").equals("true")) {
                initialLoad = false;
                editable = true;
                makeEditable();
            } else {
                initialLoad = true;
                editable = false;
                makeUneditable();
            }
        }

        if (initialLoad) {
            long startTime = intent.getLongExtra("entry_start", -1);
            SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy hh:mm a");
            String startTimeString = format.format(startTime);
            editStart.setText(startTimeString);

            setInitialInfo();
        }

        datePicker.setEnabled(editable);
        timePicker.setEnabled(editable);
    }

    /*
    Make all fields editable and hide current proposed start time
     */
    private void makeEditable() {
        datePicker.setEnabled(editable);
        timePicker.setEnabled(editable);

        editStartTitle.setVisibility(View.GONE);
        editStart.setVisibility(View.GONE);

        durationMinutesText.setFocusable(true);
        durationMinutesText.setFocusableInTouchMode(true);
        durationHoursText.setFocusable(true);
        durationHoursText.setFocusableInTouchMode(true);
        nameText.setFocusable(true);
        nameText.setFocusableInTouchMode(true);

        importanceBar.setIsIndicator(false);
    }

    private void setInitialInfo() {
        nameText.setText(initialName);

        importanceBar.setRating(initialImportance);

        durationHoursText.setText(Integer.toString(initialDuration / 60));
        durationMinutesText.setText(Integer.toString(initialDuration % 60));

        Calendar deadline = Calendar.getInstance();
        deadline.setTimeInMillis(initialDeadline);
        datePicker.updateDate(deadline.get(Calendar.YEAR), deadline.get(Calendar.MONTH),
                deadline.get(Calendar.DAY_OF_MONTH));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            timePicker.setHour(deadline.get(Calendar.HOUR_OF_DAY));
            timePicker.setMinute(deadline.get(Calendar.MINUTE));
        } else {
        }
    }

    /*
    Make all fields uneditable and show current proposed start time
     */
    private void makeUneditable() {
        datePicker.setEnabled(editable);
        timePicker.setEnabled(editable);

        editStartTitle.setVisibility(View.VISIBLE);
        editStart.setVisibility(View.VISIBLE);

        durationMinutesText.setFocusable(false);
        durationMinutesText.setFocusableInTouchMode(false);
        durationHoursText.setFocusable(false);
        durationHoursText.setFocusableInTouchMode(false);
        nameText.setFocusable(false);
        nameText.setFocusableInTouchMode(false);

        importanceBar.setIsIndicator(true);

        setInitialInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!editable)
            getMenuInflater().inflate(R.menu.menu_edit, menu);
        else
            getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);

        if (editable)
            savedInstanceState.putString("editable", "true");
        else
            savedInstanceState.putString("editable", "false");

    }

    private void updateTask() {

        // Check duration
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

        // Check deadline
        Calendar inputDeadline = Calendar.getInstance();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            inputDeadline.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                    timePicker.getHour(), timePicker.getMinute());
        } else {
            inputDeadline.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                    timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        }

        Calendar initial = Calendar.getInstance();
        initial.setTimeInMillis(initialDeadline);

        boolean deadlineDiff = false;
        if (inputDeadline.get(Calendar.YEAR) != initial.get(Calendar.YEAR) |
                inputDeadline.get(Calendar.MONTH) != initial.get(Calendar.MONTH) |
                inputDeadline.get(Calendar.DAY_OF_MONTH) != initial.get(Calendar.DAY_OF_MONTH) |
                inputDeadline.get(Calendar.HOUR_OF_DAY) != initial.get(Calendar.HOUR_OF_DAY) |
                inputDeadline.get(Calendar.MINUTE) != initial.get(Calendar.MINUTE)) {
            deadlineDiff = true;
        }

        Calendar current = Calendar.getInstance();

        if (initialImportance != (int)importanceBar.getRating() |
                initialDuration != durationMinutes | deadlineDiff) {
            meaningfulEdit = true;
        }

        EntryUpdateWorker entryUpdateWorker = new EntryUpdateWorker();
        entryUpdateWorker.execute(id.toString(), nameText.getText().toString(),
                String.valueOf(inputDeadline.getTimeInMillis()),
                String.valueOf((int)importanceBar.getRating()), String.valueOf(durationMinutes)
                );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // start relevant activity
        if (id == R.id.edit_edit) {
            initialLoad = false;
            editable = true;
            makeEditable();
            invalidateOptionsMenu();
        } else if (id == R.id.edit_save) {
            updateTask();
            editable = false;
            updateTask();
            finish();
        } else if (id == R.id.edit_cancel) {
            editable = false;
            initialLoad = true;
            makeUneditable();
            invalidateOptionsMenu();
        } else if (id == R.id.edit_cancel_main) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Worker thread to edit entry in the database in the background
     * without interfering with interface
     */
    class EntryUpdateWorker extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            TaskDbHelper taskDatabase = new TaskDbHelper(getApplicationContext());

            Long newID = Long.parseLong(params[0]);
            String newName = params[1];
            Long newDeadline = Long.parseLong(params[2]);
            int newImportance = Integer.parseInt(params[3]);
            int newDuration = Integer.parseInt(params[4]);


            long database_id = taskDatabase.updateTask(newID, newName, newDeadline, newImportance,
                    newDuration);

            if (meaningfulEdit) {
                ArrayList<Task> current = taskDatabase.fetchEntries();
                double score = ScoreFunction.scoreSchedule(current);
                Log.d("SCORE", "UPDATED TASK | Score of schedule: " + Math.round(score));
                //ArrayList<Long> optimal = ScoreFunction.optimize(current);
                ArrayList<Long> optimal = ScoreFunction.computeSchedule(current);

                if (optimal != null) {
                    ArrayList<Task> present = taskDatabase.fetchEntries();
                    for (int i = 0; i < optimal.size(); i++) {
                        taskDatabase.updateTaskWithStartTime(present.get(i).getId(), optimal.get(i));
                    }
                }
            }

            return String.valueOf(database_id);
        }

        @Override
        protected void onPostExecute(String newId) {
            Toast.makeText(getApplicationContext(),	"Task was updated.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
