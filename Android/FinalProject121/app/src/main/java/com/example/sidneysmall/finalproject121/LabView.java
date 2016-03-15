package com.example.sidneysmall.finalproject121;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sidneysmall.finalproject121.response.ComputerResponse;
import com.example.sidneysmall.finalproject121.response.MessageInfo;
import com.example.sidneysmall.finalproject121.response.StatusResponse;
import com.google.android.gms.appdatasearch.GetRecentContextCall;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class LabView extends AppCompatActivity {


    String email;
    public static String ViewedComputer = "null";
    public static int ViewedSummaryNum = 0;
    private String currentComputer;
    private Dictionary<String, String> CompLocation = new Hashtable<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Intent myIntent = getIntent();
        email = myIntent.getStringExtra("email");
    }

    private MyAdapter adp;
    private ArrayList<ListElement> aList;

    RelativeLayout relLab;
    RelativeLayout relCur;

    protected void onResume(){
        relLab = (RelativeLayout)findViewById(R.id.RelativeLab);
        relCur = (RelativeLayout)findViewById(R.id.RelativeCurrentComputer);
        relCur.setVisibility(View.INVISIBLE);
        //relCur.layout(0, relLab.getHeight(), 0, 0);
        CompLocation.put("SNOW", "364");
        CompLocation.put("LIGHTNING", "364");
        CompLocation.put("SEPHIROTH", "368");
        CompLocation.put("ZELDA", "368");
        CompLocation.put("SEAMUS", "368");
        CompLocation.put("LINK", "368");
        CompLocation.put("CMDRKEEN", "368");
        CompLocation.put("VICVIPER", "368");
        CompLocation.put("MARIO", "368");
        CompLocation.put("BRAID", "368");
        CompLocation.put("DONKEYKONG", "368");
        CompLocation.put("MASSEFFECT", "366");
        CompLocation.put("HALFLIFE", "366");
        CompLocation.put("CALLOFDUTY", "366");
        CompLocation.put("LEFT4DEAD", "366");
        GetAllStatus();
        aList = new ArrayList<ListElement>();
        adp = new MyAdapter(this, R.layout.list_element, aList);
        ListView problemList = (ListView) findViewById(R.id.listView);
        problemList.setAdapter(adp);
        adp.notifyDataSetChanged();

        super.onResume();
    }

    public void GetAllStatus(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://lauren.pythonanywhere.com/welcome/default/")
                .addConverterFactory(GsonConverterFactory.create())	//parse Gson string
                .client(httpClient)	//add logging
                .build();

        for (Enumeration e = CompLocation.keys(); e.hasMoreElements();) {

            currentComputer = (String)e.nextElement();
            final int currentComputerID = getResources().getIdentifier(currentComputer, "id", "com.example.sidneysmall.finalproject121");
            CompStatus service = retrofit.create(CompStatus.class);

            Call<StatusResponse> queryResponseCall =
                    service.getStatus(currentComputer, getString(R.string.KEY));

            //Call retrofit asynchronously
            queryResponseCall.enqueue(new Callback<StatusResponse>() {
                @Override
                public void onResponse(Response<StatusResponse> cResponse) {
                    Log.i("result", cResponse.body().response);
                    if (cResponse.body().response.equals("ok")) {
                        if (cResponse.body().problemInfo.currentStatus.equals("working")){
                            //if working put no picture
                            findViewById(currentComputerID).setBackgroundResource(R.drawable.greencheck);
                            findViewById(currentComputerID).setClickable(true);
                        }else{
                            //put red X
                            findViewById(currentComputerID).setBackgroundResource(R.drawable.redx);
                            findViewById(currentComputerID).setClickable(true);
                        }
                    }else{
                        //there was an error, put a little error picture
                        findViewById(currentComputerID).setBackgroundResource(R.drawable.errorpic);
                        findViewById(currentComputerID).setClickable(false);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    // Log error here since request failed
                }

            });
        }
    }

    public void ComputerInfo(View v){
        aList.clear();
        //relCur.layout(0,0,0,0);
        currentComputer = (String)v.getTag();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://lauren.pythonanywhere.com/welcome/default/")
                .addConverterFactory(GsonConverterFactory.create())	//parse Gson string
                .client(httpClient)	//add logging
                .build();

        CompInfo service = retrofit.create(CompInfo.class);

        Call<ComputerResponse> queryResponseCall =
                service.getInfo(currentComputer,getString(R.string.KEY));

        //Call retrofit asynchronously
        queryResponseCall.enqueue(new Callback<ComputerResponse>() {
            @Override
            public void onResponse(Response<ComputerResponse> cResponse) {
                Log.i("result", cResponse.body().response);
                if (cResponse.body().response.equals("ok")) {
                    ((TextView) findViewById(R.id.ClickedCompName)).setText(currentComputer);
                    relCur.setVisibility(View.VISIBLE);
                    for (int i = ((cResponse.body().messageInfo.size())-1); i >= 0; --i) {
                        String ts = cResponse.body().messageInfo.get(i).timeCreated;
                        String pr = cResponse.body().messageInfo.get(i).problem;
                        ListElement temp = new ListElement(ts, pr, i);
                        aList.add(temp);
                        adp.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }

        });
    }

    public void Report(View v){
        Intent intent = new Intent(this, ErrorReportActivity.class);
        Log.i("Test: ", currentComputer);
        Log.i("Test: ", CompLocation.get(currentComputer));
        intent.putExtra("currentComputer", currentComputer);
        intent.putExtra("email", email);
        intent.putExtra("roomNumber", CompLocation.get(currentComputer));
        startActivity(intent);
    }

    public void Reserve(View v){
        /*Intent intent = new Intent(this, Reserve.class);
        intent.putExtra(currentComputer,currentComputer);
        startActivity(intent);*/
    }

    public void History(View v){
        Intent intent = new Intent(this, History.class);
        intent.putExtra(ViewedComputer, currentComputer);
        ViewedSummaryNum = (int)v.getTag();
        startActivity(intent);
    }

    public void BackToView(View v){
        //relLab.layout(0,0,0,0);
        relCur.setVisibility(View.INVISIBLE);
    }

    public interface CompInfo {
        @GET("get_messages")
        Call<ComputerResponse> getInfo(@Query("computerName") String currentComputer,
                                       @Query("key") String key);
    }

    public interface CompStatus {
        @GET("get_status")
        Call<StatusResponse> getStatus(@Query("computerName") String currentComputer,
                                       @Query("key") String key);

    }
}
