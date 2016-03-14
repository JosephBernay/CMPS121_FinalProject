package com.example.sidneysmall.finalproject121;

import android.content.Context;
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
import android.widget.TextView;

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

    /*private class MyAdapter extends ArrayAdapter<ScheduleView.SlotElement> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<ScheduleView.SlotElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newSlot;

            ScheduleView.SlotElement singleSlot = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newSlot = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource, newSlot, true);
            } else {
                newSlot = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView startTime = (TextView) newSlot.findViewById(R.id.startTime);
            TextView endTime = (TextView) newSlot.findViewById(R.id.endTime);
            TextView reservedStatus = (TextView) newSlot.findViewById(R.id.isReserved);
            TextView reserver = (TextView) newSlot.findViewById(R.id.occupant);
            startTime.setText(singleSlot.startTime);
            endTime.setText(singleSlot.endTime);
            if (singleSlot.occupant == null) {
                reservedStatus.setText("Open");
                reserver.setText("");
            } else {
                final String userEmail = appInfo.email;
                reservedStatus.setText("Reserved");
                reserver.setText(userEmail);
                if (singleSlot.occupant.compareTo(userEmail) != 0) {
                    newSlot.setBackgroundColor(ContextCompat.getColor(context, R.color.theirSchedule));
                } else {
                    newSlot.setBackgroundColor(ContextCompat.getColor(context, R.color.yourSchedule));
                }
            }

            return newSlot;
        }
    }*/

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private ArrayList<ScheduleView.SlotElement> scheduleList;

    AppInfo appInfo;
    private String nickname;
    SharedPreferences settings;

    //private ScheduleView.MyAdapter adapter;
    List<String> daysOfTheWeek = new ArrayList<String>(Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
    String dateFormat = "MM-dd-yyyy";
    SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
    String curDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.the_actual_activity_schedule);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        //appInfo = AppInfo.getInstance(this);
        orderDays();
        curDay = daysOfTheWeek.get(0).substring(0, 3);
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

        for (int numDay = 0; numDay < daysOfTheWeek.size(); ++numDay) {
            LinearLayout dayList = (LinearLayout) findViewById(R.id.dayList);
            Button currDayButton = (Button) dayList.getChildAt(numDay);
            currDayButton.setText(daysOfTheWeek.get(numDay).substring(0, 3));
        }
    }

    public void changeDay (View v) {
        curDay = ((Button)v).getText().toString();
        getSchedule();
    }

    public void askIfPost (View v) {
        TextView timeSlot = (TextView)v;
        Drawable background = timeSlot.getBackground();
        int color = ((ColorDrawable) background).getColor();
        if(!(color == ContextCompat.getColor(ScheduleView.this, R.color.theirSchedule))) {
            if(color == ContextCompat.getColor(ScheduleView.this, R.color.yourSchedule)) {

            } else {
                Log.d("DEBUG", "Got it!");
            }
        }
    }

    private void getDefaultSchedule() {

    }

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
                if (response.body().response.compareTo("ok") == 0) {
                    List<ScheduleInfo> resultList = response.body().scheduleInfo;

                    scheduleList = new ArrayList<ScheduleView.SlotElement>();
                    /*adapter = new ScheduleView.MyAdapter(ScheduleView.this, R.layout.time_slot, scheduleList);
                    ListView myListView = (ListView) findViewById(R.id.timeList);
                    myListView.setAdapter(adapter);*/
                    // Read from appInfo the list of friends, and sets it.
                    AppInfo appInfo = AppInfo.getInstance(ScheduleView.this);
                    //gather messages in order of timestamp, newest on the bottom
                    for (int i = resultList.size() - 1; i >= 0; --i) {
                        Date today;
                        Date otherDay;
                        try {
                            today = dateFormatter.parse(new SimpleDateFormat(dateFormat).format(new Date()));
                            otherDay = dateFormatter.parse(new SimpleDateFormat(dateFormat).format(resultList.get(i).dateReserved));
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

                                if (curDay == resDayOfWeek) {
                                    ScheduleView.SlotElement le = new ScheduleView.SlotElement(resultList.get(i).beginTime,
                                            resultList.get(i).endTime,
                                            resultList.get(i).email);
                                    scheduleList.add(le);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace(); /*******************UNHANDLED*******************/
                        }
                    }
                    //adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }
        });
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
        @GET("post_message/")
        Call<SchedulePost> postSchedule(@Query("computerName") String computerName,
                                        @Query("beginTime") String beginTime,
                                        @Query("endTime") String endTime,
                                        @Query("dateReserved") String dateReserved,
                                        @Query("email") String email,
                                        @Query("key") String key);
    }
}
