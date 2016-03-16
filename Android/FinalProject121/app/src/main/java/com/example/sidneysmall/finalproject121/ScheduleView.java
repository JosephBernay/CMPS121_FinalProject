package com.example.sidneysmall.finalproject121;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.sidneysmall.finalproject121.response.ScheduleCancel;
import com.example.sidneysmall.finalproject121.response.ScheduleInfo;
import com.example.sidneysmall.finalproject121.response.SchedulePost;
import com.example.sidneysmall.finalproject121.response.ScheduleResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Joseph on 3/9/2016.
 */
public class ScheduleView extends AppCompatActivity {

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
    Object representing data to be entered into a time slot in the schedule
     */
    private class SlotElement {
        SlotElement() {};

        SlotElement(String start, String end, String reservedBy) {
            startTime = start;
            endTime = end;
            occupant = reservedBy;
        }

        public String startTime;
        public String endTime;
        public String occupant;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private ArrayList<SlotElement> scheduleList;

    AppInfo appInfo;
    SharedPreferences settings;

    List<String> daysOfTheWeek = new ArrayList<String>(Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
    String dateFormat = "MM-dd-yyyy";
    SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
    String curDay;
    int numDay;

    List<String> timeSegments = new ArrayList<String>(Arrays.asList("12:00am -,12:30am", "12:30am -,1:00am", "1:00am -,1:30am", "1:30am -,2:00am",
                                                                    "2:00am -,2:30am", "2:30am -,3:00am", "3:00am -,3:30am", "3:30am -,4:00am",
                                                                    "4:00am -,4:30am", "4:30am -,5:00am", "5:00am -,5:30am", "5:30am -,6:00am",
                                                                    "6:00am -,6:30am", "6:30am -,7:00am", "7:00am -,7:30am", "7:30am -,8:00am",
                                                                    "8:00am -,8:30am", "8:30am -,9:00am", "9:00am -,9:30am", "9:30am -,10:00am",
                                                                    "10:00am -,10:30am", "10:30am -,11:00am", "11:00am -,11:30am", "11:30am -,12:00pm",
                                                                    "12:00pm -,12:30pm", "12:30pm -,1:00pm", "1:00pm -,1:30pm", "1:30pm -,2:00pm",
                                                                    "2:00pm -,2:30pm", "2:30pm -,3:00pm", "3:00pm -,3:30pm", "3:30pm -,4:00pm",
                                                                    "4:00pm -,4:30pm", "4:30pm -,5:00pm", "5:00pm -,5:30pm", "5:30pm -,6:00pm",
                                                                    "6:00pm -,6:30pm", "6:30pm -,7:00pm", "7:00pm -,7:30pm", "7:30pm -,8:00pm",
                                                                    "8:00pm -,8:30pm", "8:30pm -,9:00pm", "9:00pm -,9:30pm", "9:30pm -,10:00pm",
                                                                    "10:00pm -,10:30pm", "10:30pm -,11:00pm", "11:00pm -,11:30pm", "11:30pm -,12:00am"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        appInfo = AppInfo.getInstance(this);

        //label screen with name of chosen computer
        ((TextView)findViewById(R.id.compName)).setText(appInfo.computerName);
        orderDays();

        //appropriately display the chosen day's button
        curDay = daysOfTheWeek.get(0).substring(0, 3);
        numDay = 0;
        LinearLayout dayList = (LinearLayout)findViewById(R.id.dayList);
        Button firstDayButton = (Button)dayList.getChildAt(numDay);
        firstDayButton.setBackgroundResource(R.drawable.day_button_current);

        //display today's schedule data
        orderTimes();
        getSchedule();
    }

    /*
    Find the correct order of the next seven days, starting with today
     */
    private void orderDays() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date date = new Date();
        String dayOfTheWeek = sdf.format(date);

        while (!daysOfTheWeek.get(0).equals(dayOfTheWeek)) {
            String notToday = daysOfTheWeek.get(0);
            daysOfTheWeek.remove(0);
            daysOfTheWeek.add(notToday);
        }

        for (int numDayOfWeek = 0; numDayOfWeek < daysOfTheWeek.size(); ++numDayOfWeek) {
            LinearLayout dayList = (LinearLayout) findViewById(R.id.dayList);
            Button currDayButton = (Button) dayList.getChildAt(numDayOfWeek);
            currDayButton.setText(daysOfTheWeek.get(numDayOfWeek).substring(0, 3));
        }
    }

    /*
    Give the schedule's time slots their default settings
     */
    private void orderTimes() {

        for(int numSlot = 0; numSlot < timeSegments.size(); ++numSlot) {
            LinearLayout slot = (LinearLayout) findViewById(getResources().getIdentifier("timeSlot" + numSlot, "id",
                    this.getPackageName()));
            LinearLayout duration = (LinearLayout)slot.getChildAt(0);
            TextView startTime = (TextView)duration.getChildAt(0);
            TextView endTime = (TextView)duration.getChildAt(1);
            String[] times = timeSegments.get(numSlot).split(",");
            startTime.setText(times[0]);
            endTime.setText(times[1]);

            LinearLayout reservedData = (LinearLayout)slot.getChildAt(1);
            TextView reservedStatus = (TextView)reservedData.getChildAt(0);
            reservedStatus.setText("Available");
            TextView reserver = (TextView)reservedData.getChildAt(1);
            reserver.setText("");

            slot.setBackgroundColor(ContextCompat.getColor(ScheduleView.this, R.color.emptySchedule));
        }

    }

    /*
    Change the currently chosen day to that corresponding to the day button pressed by the user,
        display that day's schedule, and scroll to the top
     */
    public void changeDay (View v) {
        LinearLayout dayList = (LinearLayout)findViewById(R.id.dayList);
        Button oldDayButton = (Button)dayList.getChildAt(numDay);
        if(numDay % 2 == 0) {
            oldDayButton.setBackgroundResource(R.drawable.day_button_1);
        } else {
            oldDayButton.setBackgroundResource(R.drawable.day_button_2);
        }
        curDay = ((Button)v).getText().toString();
        String numDayString = ((Button) v).getTag().toString();
        numDay = Integer.parseInt(numDayString.substring(numDayString.length() - 1, numDayString.length())) - 1;
        Button newDayButton = (Button)dayList.getChildAt(numDay);
        newDayButton.setBackgroundResource(R.drawable.day_button_current);
        getSchedule();
        ScrollView scrollView = (ScrollView)findViewById(R.id.timeList);
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    /*
    If the clicked time slot is available, ask the user if they would like to reserve that time slot.
    If the clicked time slot is reserved by the same user, ask the user if they would like to cancel
        the reservation.
     */
    public void askIfPost (View v) {
        LinearLayout timeSlot = (LinearLayout)v;

        if((((TextView)((LinearLayout)timeSlot.getChildAt(1)).getChildAt(0)).getText().equals("Available"))) {
            //if the clicked time slot is available, ask the user if they would like to reserve that time slot
            LinearLayout timeLayout = (LinearLayout)timeSlot.getChildAt(0);
            final String startTime = ((TextView)(timeLayout).getChildAt(0)).getText().toString().substring(0,
                    ((TextView)(timeLayout).getChildAt(0)).getText().toString().length() - 2);
            final String endTime = ((TextView)(timeLayout).getChildAt(1)).getText().toString();

            Date today = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            c.add(Calendar.DATE, numDay);
            final String chosenDate = new SimpleDateFormat(dateFormat).format(c.getTime());

            StringBuilder sb = new StringBuilder();
            sb.append("Would you like to schedule ");
            sb.append(appInfo.computerName);
            sb.append(" from ");
            sb.append(startTime);
            sb.append(" to ");
            sb.append(endTime);
            sb.append(" on ");
            sb.append(chosenDate);
            sb.append("?");
            String message = sb.toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleView.this);
            builder.setMessage(message)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            postSchedule(startTime, endTime, chosenDate);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            builder.create().show();
        } else if(((TextView)((LinearLayout)timeSlot.getChildAt(1)).getChildAt(1)).getText().equals(appInfo.email)) {
            //if the clicked time slot is reserved by the same user, ask the user if they would like to cancel
            //  the reservation
            LinearLayout timeLayout = (LinearLayout)timeSlot.getChildAt(0);
            final String startTime = ((TextView)(timeLayout).getChildAt(0)).getText().toString().substring(0,
                    ((TextView)(timeLayout).getChildAt(0)).getText().toString().length() - 2);
            final String endTime = ((TextView)(timeLayout).getChildAt(1)).getText().toString();

            Date today = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            c.add(Calendar.DATE, numDay);
            final String chosenDate = new SimpleDateFormat(dateFormat).format(c.getTime());

            StringBuilder sb = new StringBuilder();
            sb.append("Would you like to cancel your reservation of ");
            sb.append(appInfo.computerName);
            sb.append(" from ");
            sb.append(startTime);
            sb.append(" to ");
            sb.append(endTime);
            sb.append(" on ");
            sb.append(chosenDate);
            sb.append("?");

            String message = sb.toString();
            AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleView.this);
            builder.setMessage(message)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            cancelSchedule(startTime, endTime, chosenDate);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            builder.create().show();
        }
    }

    /*
    Send new schedule data to the server
     */
    private void postSchedule(String beginTime, String endTime, String dateReserved) {
        String computerName = appInfo.computerName;
        String email = appInfo.email;
        String key = appInfo.key;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://glcs-1251.appspot.com/welcome/default/")
                .addConverterFactory(GsonConverterFactory.create())    //parse Gson string
                .client(httpClient)    //add logging
                .build();

        PostService service = retrofit.create(PostService.class);

        Call<SchedulePost> queryResponseCall =
                service.postSchedule(computerName, beginTime, endTime, dateReserved, email, key);

        //Call retrofit asynchronously
        queryResponseCall.enqueue(new Callback<SchedulePost>() {
            @Override
            public void onResponse(Response<SchedulePost> response) {
                if(response.body().response.compareTo("ok") == 0) {

                    //retrieve new schedule data from the server
                    getSchedule();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }
        });
    }

    /*
    Remove chosen schedule data from the server
     */
    private void cancelSchedule(String beginTime, String endTime, String dateReserved) {
        String computerName = appInfo.computerName;
        String email = appInfo.email;
        String key = appInfo.key;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://glcs-1251.appspot.com/welcome/default/")
                .addConverterFactory(GsonConverterFactory.create())    //parse Gson string
                .client(httpClient)    //add logging
                .build();

        CancelService service = retrofit.create(CancelService.class);

        Call<ScheduleCancel> queryResponseCall =
                service.cancelSchedule(computerName, beginTime, endTime, dateReserved, email, key);

        //Call retrofit asynchronously
        queryResponseCall.enqueue(new Callback<ScheduleCancel>() {
            @Override
            public void onResponse(Response<ScheduleCancel> response) {
                if(response.body().response.compareTo("ok") == 0) {
                    getSchedule();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }
        });
    }

    /*
    Retrieve schedule data from the server
     */
    private void getSchedule() {
        String key = appInfo.key;
        String computerName = appInfo.computerName;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://glcs-1251.appspot.com/welcome/default/")
                .addConverterFactory(GsonConverterFactory.create())    //parse Gson string
                .client(httpClient)    //add logging
                .build();

        ScheduleService service = retrofit.create(ScheduleService.class);

        Call<ScheduleResponse> queryResponseCall =
                service.getSchedule(computerName, key);

        //Call retrofit asynchronously
        queryResponseCall.enqueue(new Callback<ScheduleResponse>() {
            @Override
            public void onResponse(Response<ScheduleResponse> response) {
                if (response.body().response.compareTo("ok") == 0) {
                    List<ScheduleInfo> resultList = response.body().scheduleInfo;

                    scheduleList = new ArrayList<SlotElement>();

                    for (int i = resultList.size() - 1; i >= 0; --i) {
                        Date today;
                        Date otherDay;
                        try {
                            today = dateFormatter.parse(new SimpleDateFormat(dateFormat).format(new Date()));
                            otherDay = dateFormatter.parse(resultList.get(i).dateReserved);

                            //if today is the same day as that in the retrieved schedule data, add it
                            //  to the schedule list
                            if (today.compareTo(otherDay) <= 0) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(otherDay);

                                String resDayOfWeek = "";

                                switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                                    case (Calendar.SUNDAY):
                                        resDayOfWeek = "Sun";
                                        break;
                                    case (Calendar.MONDAY):
                                        resDayOfWeek = "Mon";
                                        break;
                                    case (Calendar.TUESDAY):
                                        resDayOfWeek = "Tue";
                                        break;
                                    case (Calendar.WEDNESDAY):
                                        resDayOfWeek = "Wed";
                                        break;
                                    case (Calendar.THURSDAY):
                                        resDayOfWeek = "Thu";
                                        break;
                                    case (Calendar.FRIDAY):
                                        resDayOfWeek = "Fri";
                                        break;
                                    case (Calendar.SATURDAY):
                                        resDayOfWeek = "Sat";
                                        break;
                                    default:
                                        break;
                                }

                                if (curDay.equals(resDayOfWeek)) {
                                    SlotElement le = new SlotElement(resultList.get(i).beginTime,
                                            resultList.get(i).endTime,
                                            resultList.get(i).email);
                                    scheduleList.add(le);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace(); /*******************UNHANDLED*******************/
                        }
                    }

                    updateSchedules();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }
        });
    }

    /*
    Update the display with newest schedule data
     */
    private void updateSchedules() {
        orderTimes();

        for(int numSchedule = 0; numSchedule < scheduleList.size(); ++numSchedule) {
            String timeSegment = scheduleList.get(numSchedule).startTime + " -," + scheduleList.get(numSchedule).endTime;
            int numSlot = timeSegments.indexOf(timeSegment);

            LinearLayout scrollLayout = (LinearLayout)findViewById(R.id.scrollLayout);
            LinearLayout schedule = (LinearLayout)scrollLayout.getChildAt(numSlot * 2);
            LinearLayout timeSlot = (LinearLayout)schedule.getChildAt(0);
            TextView startTime = (TextView)timeSlot.getChildAt(0);
            startTime.setText(scheduleList.get(numSchedule).startTime + " -");
            TextView endTime = (TextView)timeSlot.getChildAt(1);
            endTime.setText(scheduleList.get(numSchedule).endTime);
            LinearLayout reservedData = (LinearLayout)schedule.getChildAt(1);
            TextView reservedStatus = (TextView)reservedData.getChildAt(0);
            reservedStatus.setText("Reserved");
            TextView reserver = (TextView)reservedData.getChildAt(1);
            reserver.setText(scheduleList.get(numSchedule).occupant);

            if(reserver.getText().toString().equals(appInfo.email)) {
                schedule.setBackgroundColor(ContextCompat.getColor(ScheduleView.this, R.color.yourSchedule));
            } else {
                schedule.setBackgroundColor(ContextCompat.getColor(ScheduleView.this, R.color.theirSchedule));
            }
        }
    }

    /*
    Go back to the lab view
     */
    public void backToLabView(View v) {
        Intent intent = new Intent(this, LabView.class);
        startActivity(intent);
    }

    /*
    If the device's back button is pressed, close this activity
     */
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Foursquare api https://developer.foursquare.com/docs/venues/search
     */
    public interface ScheduleService {
        @GET("get_schedule/")
        Call<ScheduleResponse> getSchedule(@Query("computerName") String computerName,
                                           @Query("key") String key);
    }

    public interface PostService {
        @GET("schedule_computer/")
        Call<SchedulePost> postSchedule(@Query("computerName") String computerName,
                                        @Query("beginTime") String beginTime,
                                        @Query("endTime") String endTime,
                                        @Query("dateReserved") String dateReserved,
                                        @Query("email") String email,
                                        @Query("key") String key);
    }

    public interface CancelService {
        @GET("cancel_reservation/")
        Call<ScheduleCancel> cancelSchedule(@Query("computerName") String computerName,
                                            @Query("beginTime") String beginTime,
                                            @Query("endTime") String endTime,
                                            @Query("dateReserved") String dateReserved,
                                            @Query("email") String email,
                                            @Query("key") String key);
    }
}
