package com.example.sidneysmall.finalproject121;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sidneysmall.finalproject121.response.AddUserResponse;
import com.example.sidneysmall.finalproject121.response.GetUserResponse;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ErrorReportActivity extends AppCompatActivity {

    String LOG = "Print: ";

    String email;
    String comp;
    String room;
    Session session = null;
    String rec, subject, textMessage;
    String error;
    String key;
    String details;
    AppInfo appInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report);
        Spinner dropdown = (Spinner) findViewById(R.id.spinner);
        SpinnerActivity spinnerActivity = new SpinnerActivity();
        dropdown.setOnItemSelectedListener(spinnerActivity);
        String[] items = new String[]{"Select One", "Randomly Shuts Down", "Display Driver Failure", "Graphics Card Failure", "Randomly Enters Power Save Mode", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        Intent intent = getIntent();
        appInfo = AppInfo.getInstance(this);
        email = appInfo.email;
        comp = intent.getStringExtra("currentComputer");
        room = intent.getStringExtra("roomNumber");
        key = getString(R.string.KEY);
    }

    public void back(View v) {
        Intent myIntent = new Intent(ErrorReportActivity.this, LabView.class);
        startActivity(myIntent);
    }

    public void send(View v) {
        if (error != null && !error.equals("Select One")) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://glcs-1251.appspot.com/welcome/default/")
                    .addConverterFactory(GsonConverterFactory.create())    //parse Gson string
                    .client(httpClient)    //add logging
                    .build();

            PostMessageService service = retrofit.create(PostMessageService.class);

            TextView dets = (TextView) findViewById(R.id.editText);
            details = dets.getText().toString();

            Call<AddUserResponse> queryResponseCall =
                    service.postMessage(comp, room, details, error, key);

            queryResponseCall.enqueue(new Callback<AddUserResponse>() {
                @Override
                public void onResponse(Response<AddUserResponse> response) {
                    if (response.body().getResponse().equals("ok")) {
                        Toast.makeText(getApplicationContext(), "Error Posted To Server. Sending To BELS...", Toast.LENGTH_LONG).show();
                        rec = "jbernay@ucsc.edu";
                        subject = "GLCS: Error Report";
                        StringBuilder sb = new StringBuilder();
                        sb.append("<b>Room:</b> " + room);
                        sb.append("<br/>");
                        sb.append("<br/>");
                        sb.append("<b>Computer:</b> " + comp);
                        sb.append("<br/>");
                        sb.append("<br/>");
                        sb.append("<b>Error:</b> " + error);
                        sb.append("<br/>");
                        sb.append("<br/>");
                        sb.append("<b>Details:</b> " + details);
                        sb.append("<br/>");
                        sb.append("<br/>");
                        sb.append("<b>Follow-up address:</b> " + email);
                        sb.append("<br/>");
                        sb.append("<br/>");
                        sb.append("When the computer has been fixed, please hit the following link: <br/>");
                        sb.append("http://glcs-1251.appspot.com/welcome/default/post_message/?computerName=" + comp + "&computerNumber=" + room + "&messageData=Serviced%20by%20BELS&problem=fixed&key=" + getString(R.string.KEY));
                        textMessage = sb.toString();
                        //textMessage = "Computer: " + comp + "Room: " + room + "Error: " + error + "Details: " + details;
                        Properties props = new Properties();
                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "465");

                        session = Session.getDefaultInstance(props, new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication("glcs.ucsc@gmail.com", "Okay. What now, Lauren?");
                            }
                        });

                        RetrieveFeedTask task = new RetrieveFeedTask();
                        task.execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please select an error type", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    // Log error here since request failed
                }
            });


        }
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("glcs.ucsc@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rec));
                message.setSubject(subject);
                message.setContent(textMessage, "text/html; charset=utf-8");
                Transport.send(message);
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
        }
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            error = parent.getItemAtPosition(pos).toString();
            //Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public interface PostMessageService {
        @GET("post_message")
        Call<AddUserResponse> postMessage(@Query("computerName") String computerName,
                                          @Query("computerNumber") String computerNumber,
                                          @Query("messageData") String messageData,
                                          @Query("problem") String problem,
                                          @Query("key") String key
        );
    }
}