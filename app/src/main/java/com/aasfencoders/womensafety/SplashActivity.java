package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    ImageView image;

    SharedPreferences sharedPreferences;

    public void function(){
        image.animate().alphaBy(1f).setDuration(1500);
        new CountDownTimer(1500, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                String firstOnboarding = sharedPreferences.getString(getString(R.string.OnBoarding),"");
                Intent intent;
                if(firstOnboarding.equals("")){
                    intent = new Intent(SplashActivity.this, OnBoardingScreen.class);
                    sharedPreferences.edit().putString(getString(R.string.OnBoarding) , getString(R.string.done)).apply();
                }else{
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        image = (ImageView) findViewById(R.id.splashImage);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
        function();
    }
}
