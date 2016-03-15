package com.example.sidneysmall.finalproject121;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.sidneysmall.finalproject121.response.ScheduleCancel;
import com.example.sidneysmall.finalproject121.response.ScheduleInfo;
import com.example.sidneysmall.finalproject121.response.SchedulePost;
import com.example.sidneysmall.finalproject121.response.ScheduleResponse;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

//import com.cmps121.hw3.localchat.response.ChatPost;
//import com.cmps121.hw3.localchat.response.ScheduleResponse;
//import com.cmps121.hw3.localchat.response.ResultList;

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
    private String nickname;
    SharedPreferences settings;

    //private ScheduleView.MyAdapter adapter;
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
        setContentView(R.layout.the_actual_activity_schedule);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        appInfo = AppInfo.getInstance(this);
        ((TextView)findViewById(R.id.compName)).setText(appInfo.computerName);
        orderDays();
        curDay = daysOfTheWeek.get(0).substring(0, 3);
        numDay = 0;
        LinearLayout dayList = (LinearLayout)findViewById(R.id.dayList);
        Button firstDayButton = (Button)dayList.getChildAt(numDay);
        firstDayButton.setBackgroundResource(R.drawable.day_button_current);
        orderTimes();
        getSchedule();
    }

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

    public void changeDay (View v) {
        LinearLayout dayList = (LinearLayout)findViewById(R.id.dayList);
        Button oldDayButton = (Button)dayList.getChildAt(numDay);
        Log.d("DEBUG", Integer.toString(numDay));
        if(numDay % 2 == 0) {
            oldDayButton.setBackgroundResource(R.drawable.day_button_1);
        } else {
            oldDayButton.setBackgroundResource(R.drawable.day_button_2);
        }
        curDay = ((Button)v).getText().toString();
        String numDayString = Integer.toString(((Button) v).getId());
        numDay = Integer.parseInt(numDayString.substring(numDayString.length() - 1, numDayString.length()));
        Log.d("DEBUG", Integer.toString(numDay));
        Button newDayButton = (Button)dayList.getChildAt(numDay);
        newDayButton.setBackgroundResource(R.drawable.day_button_current);
        getSchedule();
        ScrollView scrollView = (ScrollView)findViewById(R.id.timeList);
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    public void askIfPost (View v) {
        LinearLayout timeSlot = (LinearLayout)v;

        if((((TextView)((LinearLayout)timeSlot.getChildAt(1)).getChildAt(0)).getText().equals("Available"))) {
            LinearLayout timeLayout = (LinearLayout)timeSlot.getChildAt(0);
            final String startTime = ((TextView)(timeLayout).getChildAt(0)).getText().toString().substring(0,
                    ((TextView)(timeLayout).getChildAt(0)).getText().toString().length() - 2);
            final String endTime = ((TextView)(timeLayout).getChildAt(1)).getText().toString();
            Date today = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            Log.d("DEBUG", "today: " + c.getTime().toString());
            c.add(Calendar.DATE, numDay);
            Log.d("DEBUG", "otherDay: " + c.getTime().toString());
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
            LinearLayout timeLayout = (LinearLayout)timeSlot.getChildAt(0);
            final String startTime = ((TextView)(timeLayout).getChildAt(0)).getText().toString().substring(0,
                    ((TextView)(timeLayout).getChildAt(0)).getText().toString().length() - 2);
            final String endTime = ((TextView)(timeLayout).getChildAt(1)).getText().toString();
            Date today = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            Log.d("DEBUG", "today: " + c.getTime().toString());
            c.add(Calendar.DATE, numDay);
            Log.d("DEBUG", "otherDay: " + c.getTime().toString());
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
                .baseUrl("http://lauren.pythonanywhere.com/welcome/default/")
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
                    Log.d("DEBUG", "Should be calling getSchedule here");

                    getSchedule();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }
        });
    }

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
                .baseUrl("http://lauren.pythonanywhere.com/welcome/default/")
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

    private void getSchedule() {
        Log.d("DEBUG", "Got into getSchedule");
        String key = appInfo.key;
        String computerName = appInfo.computerName;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://lauren.pythonanywhere.com/welcome/default/")
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
                Log.d("DEBUG", "Got into onResponse");
                if (response.body().response.compareTo("ok") == 0) {
                    Log.d("DEBUG", "Got a good response");
                    List<ScheduleInfo> resultList = response.body().scheduleInfo;

                    Log.d("DEBUG", "Result List: " + resultList.toString());

                    scheduleList = new ArrayList<SlotElement>();

                    for (int i = resultList.size() - 1; i >= 0; --i) {
                        Date today;
                        Date otherDay;
                        try {
                            Log.d("DEBUG", "Got into the try block");
                            today = dateFormatter.parse(new SimpleDateFormat(dateFormat).format(new Date()));
                            otherDay = dateFormatter.parse(resultList.get(i).dateReserved);
                            if (today.compareTo(otherDay) <= 0) {
                                Log.d("DEBUG", "Got the same day");
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(otherDay);

                                String resDayOfWeek = "";

                                switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                                    case (Calendar.SUNDAY):
                                        resDayOfWeek = "Sun";
                                        Log.d("DEBUG", "Sun");
                                        break;
                                    case (Calendar.MONDAY):
                                        resDayOfWeek = "Mon";
                                        Log.d("DEBUG", "Mon");
                                        break;
                                    case (Calendar.TUESDAY):
                                        resDayOfWeek = "Tue";
                                        Log.d("DEBUG", "Tue");
                                        break;
                                    case (Calendar.WEDNESDAY):
                                        resDayOfWeek = "Wed";
                                        Log.d("DEBUG", "Wed");
                                        break;
                                    case (Calendar.THURSDAY):
                                        resDayOfWeek = "Thu";
                                        Log.d("DEBUG", "Thu");
                                        break;
                                    case (Calendar.FRIDAY):
                                        resDayOfWeek = "Fri";
                                        Log.d("DEBUG", "Fri");
                                        break;
                                    case (Calendar.SATURDAY):
                                        resDayOfWeek = "Sat";
                                        Log.d("DEBUG", "Sat");
                                        break;
                                    default:
                                        break;
                                }

                                Log.d("DEBUG", "curDay: " + curDay);

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

    private void updateSchedules() {
        Log.d("DEBUG", "Got into updateSchedules");
        orderTimes();

        for(int numSchedule = 0; numSchedule < scheduleList.size(); ++numSchedule) {
            Log.d("DEBUG", "Got into the update for loop");
            String timeSegment = scheduleList.get(numSchedule).startTime + " -," + scheduleList.get(numSchedule).endTime;
            int numSlot = timeSegments.indexOf(timeSegment);

            LinearLayout scrollLayout = (LinearLayout)findViewById(R.id.scrollLayout);
            LinearLayout schedule = (LinearLayout)scrollLayout.getChildAt(numSlot * 2);
            LinearLayout timeSlot = (LinearLayout)schedule.getChildAt(0);
            TextView startTime = (TextView)timeSlot.getChildAt(0);
            startTime.setText(scheduleList.get(numSchedule).startTime);
            TextView endTime = (TextView)timeSlot.getChildAt(1);
            endTime.setText(scheduleList.get(numSchedule).endTime);
            LinearLayout reservedData = (LinearLayout)schedule.getChildAt(1);
            TextView reservedStatus = (TextView)reservedData.getChildAt(0);
            reservedStatus.setText("Reserved");
            TextView reserver = (TextView)reservedData.getChildAt(1);
            reserver.setText(scheduleList.get(numSchedule).occupant);

            Log.d("DEBUG", reserver.getText().toString() + ", " + appInfo.email);

            if(reserver.getText().toString().equals(appInfo.email)) {
                schedule.setBackgroundColor(ContextCompat.getColor(ScheduleView.this, R.color.yourSchedule));
            } else {
                schedule.setBackgroundColor(ContextCompat.getColor(ScheduleView.this, R.color.theirSchedule));
            }
        }
    }

    public void backToLabView(View v) {
        Intent intent = new Intent(this, LabView.class);
        startActivity(intent);
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
