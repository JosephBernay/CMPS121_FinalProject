package com.example.sidneysmall.finalproject121;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sidneysmall.finalproject121.response.AddUserResponse;
import com.example.sidneysmall.finalproject121.response.GetUserResponse;

import java.util.ArrayList;
import java.util.List;

import javax.mail.event.MailEvent;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static android.Manifest.permission.READ_CONTACTS;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    String key;
    String email;
    String password;
    AppInfo appInfo;

    String LOG = "Print: ";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        key = getString(R.string.KEY);
        TextView passwordText = (TextView)findViewById(R.id.password);
        TextView emailText = (TextView)findViewById(R.id.email);
        appInfo = AppInfo.getInstance(this);
    }

    public interface AddUserService {
        @GET("add_user")
        Call<AddUserResponse> addUser(@Query("email") String email,
                                      @Query("password") String password,
                                      @Query("key") String key
        );
    }

    public interface GetUserService {
        @GET("get_user")
        Call<GetUserResponse> getUser(@Query("email") String email,
                                      @Query("password") String password,
                                      @Query("key") String key
        );
    }

    public void login(View v){
        TextView passwordText = (TextView) findViewById(R.id.password);
        TextView emailText = (TextView) findViewById(R.id.email);
        if((passwordText == null || passwordText.getText().equals("")) && (emailText == null || emailText.getText().equals(""))){
            Toast.makeText(getApplicationContext(), "Please fill in both fields", Toast.LENGTH_LONG).show();
        }else {
            password = passwordText.getText().toString();
            email = emailText.getText().toString();
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

            GetUserService service = retrofit.create(GetUserService.class);

            Call<GetUserResponse> queryResponseCall =
                    service.getUser(email, password, key);

            queryResponseCall.enqueue(new Callback<GetUserResponse>() {
                @Override
                public void onResponse(Response<GetUserResponse> response) {
                    Log.d("DEBUG", "password: " + password);
                    if (response.body().getResponse().equals("ok") && response.body().getUserInfo().getLogin().equals("success")) {
                        if (password.equals("temp")) {
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

                            AddUserService service = retrofit.create(AddUserService.class);

                            Call<AddUserResponse> queryResponseCall =
                                    service.addUser(email, password, key);

                            //Call retrofit asynchronously
                            queryResponseCall.enqueue(new Callback<AddUserResponse>() {
                                @Override
                                public void onResponse(Response<AddUserResponse> response) {
                                    if (response.body().getResponse().equals("ok")) {
                                        Intent myIntent = new Intent(LoginActivity.this, VerifyActivity.class);
                                        myIntent.putExtra("key", email); //Optional parameters
                                        startActivity(myIntent);
                                    } else if (response.body().getResponse().equals("user not authorized")) {
                                        Log.i(LOG, "add user not authorized");
                                        Toast.makeText(getApplicationContext(), "add user not authorized", Toast.LENGTH_LONG).show();

                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    // Log error here since request failed
                                }
                            });
                        }
                        else {
                            Intent myIntent = new Intent(LoginActivity.this, LabView.class);
                            myIntent.putExtra("email", email);
                            startActivity(myIntent);
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Incorrect username or password", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    // Log error here since request failed
                }
            });
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
}
