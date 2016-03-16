package com.example.sidneysmall.finalproject121;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class VerifyActivity extends AppCompatActivity {

    String LOG = "Print: ";

    String email;
    Session session = null;
    String rec, subject, textMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        Intent intent = getIntent();
        email = intent.getStringExtra("key");

    }

    public void changepassword(View v){
        TextView newpassText = (TextView)findViewById(R.id.newpassword);
        TextView confirmText = (TextView)findViewById(R.id.confirmpassword);
        if((confirmText == null || confirmText.getText().toString().equals("")) && (newpassText == null || newpassText.getText().toString().equals(""))){
            Toast.makeText(getApplicationContext(), "Please Fill In Both Fields", Toast.LENGTH_LONG).show();
        }else if(newpassText.getText().toString().equals(confirmText.getText().toString())){
            String newpass = newpassText.getText().toString();
            rec = email;
            subject = "Password Change Verification";
            textMessage = "Someone has attempted to log in with this email and change the password. If this was you, then click the link below. Otherwise, contact your instructor. <br/><br/>http://glcs-1251.appspot.com/welcome/default/add_user?email=" + email + "&password=" + newpass + "&key="+ getString(R.string.KEY);

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
            Toast.makeText(getApplicationContext(), "The two passwords do not match", Toast.LENGTH_LONG).show();
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
}
