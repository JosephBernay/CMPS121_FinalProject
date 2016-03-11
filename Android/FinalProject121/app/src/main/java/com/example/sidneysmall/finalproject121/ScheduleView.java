package com.example.sidneysmall.finalproject121;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

//import com.cmps121.hw3.localchat.response.ChatPost;
//import com.cmps121.hw3.localchat.response.ChatResponse;
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
public class ScheduleView {
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private class ListElement {
        ListElement() {};

        ListElement( String msg, String name, boolean isOwn) {
            message = msg;
            nickname = name;
            isYours = isOwn;
        }

        public String message;
        public String nickname;
        public boolean isYours;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private class MyAdapter extends ArrayAdapter<ListElement> {

        int resource;
        Context context;
        List<ResultList> response;

        public MyAdapter(Context _context, int _resource, List<ListElement> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newMsg;

            ListElement singleMsg = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newMsg = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newMsg, true);
            } else {
                newMsg = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView itemMessage = (TextView)newMsg.findViewById(R.id.itemMessage);
            TextView itemSender = (TextView)newMsg.findViewById(R.id.itemSender);
            itemMessage.setText(singleMsg.message);
            //if the sender of the message is not the user
            if(!singleMsg.isYours) {
                //label the message with the sender's nickname
                itemMessage.setGravity(Gravity.LEFT);
                itemSender.setGravity(Gravity.LEFT);
                itemSender.setText(singleMsg.nickname);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)itemMessage.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                itemMessage.setLayoutParams(params);
                params = (RelativeLayout.LayoutParams)itemSender.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                itemSender.setLayoutParams(params);
            }
            //if the sender of the message IS the user
            else {
                //label the message with the user's name, shift the message to the right side
                itemMessage.setGravity(Gravity.RIGHT);
                itemSender.setGravity(Gravity.RIGHT);
                itemSender.setText(singleMsg.nickname + " (You)");
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)itemMessage.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                itemMessage.setLayoutParams(params);
                params = (RelativeLayout.LayoutParams)itemSender.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                itemSender.setLayoutParams(params);
            }

            return newMsg;
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
}
