package edu.dartmouth.cs.chrono;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ToDoFragment toDoFragment;
    //SettingsFragment settingsFragment;
    ViewPager viewPager;
    TabLayout tabLayout;
    MyFragmentPagerAdapter myFragmentPagerAdapter;
    ArrayList<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        tabLayout = (TabLayout)findViewById(R.id.tab);

        toDoFragment = new ToDoFragment();
        //settingsFragment = new SettingsFragment();

        fragments = new ArrayList<Fragment>();
        fragments.add(toDoFragment);
        //fragments.add(settingsFragment);

        myFragmentPagerAdapter = new MyFragmentPagerAdapter(getFragmentManager(), fragments);
        viewPager.setAdapter(myFragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // start relevant activity
        if (id == R.id.action_task) {
            Intent intent = new Intent(this, AddTaskActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.sync) {
            if (toDoFragment != null) {
                toDoFragment.updateEntries();
            }
            return true;
        } else if (id == R.id.action_recalc) {
            if (toDoFragment != null) {
                EntryUpdateWorker entryUpdateWorker = new EntryUpdateWorker();
                entryUpdateWorker.execute();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Worker thread to delete entry in the database in the background
     * without interfering with interface
     */
    class EntryUpdateWorker extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            TaskDbHelper taskDatabase = new TaskDbHelper(getApplicationContext());
            ArrayList<Task> current = taskDatabase.fetchEntries();
            ArrayList<Long> optimal = ScoreFunction.computeSchedule(current);

            if (optimal != null) {
                ArrayList<Task> present = taskDatabase.fetchEntries();
                for (int i = 0; i < optimal.size(); i++) {
                    taskDatabase.updateTaskWithStartTime(present.get(i).getId(), optimal.get(i));
                }
            }

            return "added";
        }

        // Confirm entry deleted
        @Override
        protected void onPostExecute(String message) {
            if (toDoFragment != null) {
                if (toDoFragment.taskAdapter != null)
                    toDoFragment.taskAdapter.notifyDataSetChanged();
                toDoFragment.updateEntries();
            }
            Toast.makeText(getApplicationContext(), "Schedule recalculated.", Toast.LENGTH_SHORT).show();
        }
    }
}
