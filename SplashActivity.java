package com.deepwares.checkpointdwi.splash;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.deepwares.checkpointdwi.R;
import com.deepwares.checkpointdwi.activities.LoginUserActivity;
import com.deepwares.checkpointdwi.activities.MainActivity;
import com.deepwares.checkpointdwi.activities.PictureActivity;
import com.deepwares.checkpointdwi.session.Cache;
import com.deepwares.checkpointdwi.session.CatchValue;
import com.deepwares.checkpointdwi.session.SessionManager;
import com.parse.ParseUser;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

/**
 * Created by innData on 18/01/18.
 */

public class SplashActivity extends AppCompatActivity {

    private Locale locale;
    String appLang;
    ParseUser user;
    final static long DURATION = 3 * 1000;
    SessionManager session;
    Boolean userPicStatus = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Fabric.with(this, new Crashlytics());
        session = new SessionManager(this);
        Log.e("session_login", "" + session.isLoggedIn());
        //get the language from session
        appLang = (String) Cache.getData(CatchValue.APP_LANGUAGE, SplashActivity.this);
        // handler for navigate to login screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!session.isLoggedIn()) {
                    //pass to login class
                    Intent intent = new Intent(SplashActivity.this, LoginUserActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    locale = new Locale(appLang);
                    Resources res = getResources();
                    DisplayMetrics dm = res.getDisplayMetrics();
                    Configuration conf = res.getConfiguration();
                    conf.locale = locale;
                    res.updateConfiguration(conf, dm);
                    loginProcess();
                }
            }
        }, DURATION);
    }
    //pass to login class
    private void loginProcess() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
