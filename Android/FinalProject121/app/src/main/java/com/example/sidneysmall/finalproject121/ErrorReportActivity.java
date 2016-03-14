package com.example.sidneysmall.finalproject121;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ErrorReportActivity extends AppCompatActivity {

    String LOG = "Print: ";

    String[] stuff;
    String email;
    String comp;
    String room;
    Session session = null;
    String rec, subject, textMessage;
    String error;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report);
        Spinner dropdown = (Spinner)findViewById(R.id.spinner);
        SpinnerActivity spinnerActivity = new SpinnerActivity();
        dropdown.setOnItemSelectedListener(spinnerActivity);
        String[] items = new String[]{"Select One", "Randomly Shuts Down", "Display Driver Failure", "Graphics Card Failure", "Randomly Enters Power Save Mode", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        comp = intent.getStringExtra("currentComputer");
        room = intent.getStringExtra("roomNumber");

    }

    public void back(View v){
        Intent myIntent = new Intent(ErrorReportActivity.this, LabView.class);
        startActivity(myIntent);
    }

    public void send(View v){
        if(error != null && !error.equals("Select One")){
            TextView dets = (TextView)findViewById(R.id.editText);
            String details = dets.getText().toString();
            rec = "aparvis@ucsc.edu";
            subject = "GLCS: Error Report";
            textMessage = "Computer: " + comp + "Room: " + room + "Error: " + error + "Details: " + details;
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
        }else{
            Toast.makeText(getApplicationContext(), "Please select an error type", Toast.LENGTH_LONG).show();
        }

    }
    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try{
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("glcs.ucsc@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rec));
                message.setSubject(subject);
                message.setContent(textMessage, "text/html; charset=utf-8");
                Transport.send(message);
            } catch(MessagingException e) {
                e.printStackTrace();
            } catch(Exception e) {
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
}
