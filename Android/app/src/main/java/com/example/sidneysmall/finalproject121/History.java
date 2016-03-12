package com.example.sidneysmall.finalproject121;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
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


/**
 * Created by SidneySmall on 3/10/2016.
 */
public class History extends AppCompatActivity{
    int sumnum = LabView.ViewedSummaryNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
    }

    protected void onResume(){
        final TextView summaryText = (TextView) findViewById(R.id.textView);
        Intent intent = getIntent();
        String comp = intent.getStringExtra(LabView.ViewedComputer);
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

        final CompInfo service = retrofit.create(CompInfo.class);

        Call<ComputerResponse> queryResponseCall =
                service.getInfo(comp);

        //Call retrofit asynchronously
        queryResponseCall.enqueue(new Callback<ComputerResponse>() {
            @Override
            public void onResponse(Response<ComputerResponse> cResponse) {
                Log.i("result", cResponse.body().response);
                if (cResponse.body().response.equals("ok")) {
                    MessageInfo summary = cResponse.body().messageInfo.get(sumnum);
                    String temp = summary.computerName + "\n"
                            +summary.computerNumber + "\n"
                            +summary.timeCreated + "\n"
                            +summary.problem + "\n"
                            +summary.messageData;
                    summaryText.setText(temp);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }

        });

        super.onResume();
    }
    public interface CompInfo {
        @GET("get_messages")
        Call<ComputerResponse> getInfo(@Query("computerName") String currentComputer);
    }

}
