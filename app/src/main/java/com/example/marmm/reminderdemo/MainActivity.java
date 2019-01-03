package com.example.marmm.reminderdemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    //Local variables

    public static final String EXTRA_REMINDER = "Reminder";

    //Constants used when calling the update activity
    public static final int REQUESTCODE = 1234;
    public final static int TASK_GET_ALL_REMINDERS = 0;
    public final static int TASK_DELETE_REMINDER = 1;
    public final static int TASK_UPDATE_REMINDER = 2;
    public final static int TASK_INSERT_REMINDER = 3;
    public static AppDatabase db;
    private ReminderAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private List mReminders;
    private EditText mNewReminderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db = AppDatabase.getInstance(this);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mNewReminderText = findViewById(R.id.editText_main);
        mReminders = new ArrayList<>();
        new ReminderAsyncTask(TASK_GET_ALL_REMINDERS).execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = mNewReminderText.getText().toString();
                if (!(TextUtils.isEmpty(text)) && (TextUtils.isDigitsOnly(text))) {
                    requestData();
                    updateUI();
                    mNewReminderText.setText("");
                } else {
                    Snackbar.make(view, "Please enter some numbers in the textfield", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {
        if (mAdapter == null) {
            mAdapter = new ReminderAdapter(mReminders);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUESTCODE) {
            if (resultCode == RESULT_OK) {
                Reminder updatedReminder = data.getParcelableExtra(MainActivity.EXTRA_REMINDER);
                // New timestamp: timestamp of update
                new ReminderAsyncTask(TASK_UPDATE_REMINDER).execute(updatedReminder);
            }
        }
    }

    public void onReminderDbUpdated(List list) {
        mReminders = list;
        updateUI();
    }

    private void requestData() {
        NumbersApiService service = NumbersApiService.retrofit.create(NumbersApiService.class);
        String numberText = mNewReminderText.getText().toString();
        int number = Integer.parseInt(numberText);
        /**
         * Make an a-synchronous call by enqueing and definition of callbacks.
         */
        Call<DayQuoteItem> call = service.getTodaysQuote(number);
        call.enqueue(new Callback<DayQuoteItem>() {
            @Override
            public void onResponse(Call<DayQuoteItem> call, Response<DayQuoteItem> response) {
                DayQuoteItem dayQuoteItem = response.body();
                Reminder newReminder = new Reminder(dayQuoteItem.getText());
                mReminders.add(newReminder);
                new ReminderAsyncTask(TASK_INSERT_REMINDER).execute(newReminder);
            }

            @Override
            public void onFailure(Call<DayQuoteItem> call, Throwable t) {
            }
        });
    }

    public class ReminderAsyncTask extends AsyncTask<Reminder, Void, List> {

        private int taskCode;

        public ReminderAsyncTask(int taskCode) {
            this.taskCode = taskCode;
        }

        @Override
        protected List doInBackground(Reminder... reminders) {
            switch (taskCode) {
                case TASK_DELETE_REMINDER:
                    db.reminderDao().deleteReminders(reminders[0]);
                    break;
                case TASK_UPDATE_REMINDER:
                    db.reminderDao().updateReminders(reminders[0]);
                    break;
                case TASK_INSERT_REMINDER:
                    db.reminderDao().insertReminders(reminders[0]);
                    break;
            }
            //To return a new list with the updated data, we get all the data from the database again.
            return db.reminderDao().getAllReminders();
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);
            onReminderDbUpdated(list);
        }
    }
}
