package com.aasfencoders.womensafety;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.ramotion.paperonboarding.PaperOnboardingFragment;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnRightOutListener;

import java.util.ArrayList;

public class OnBoardingScreen extends AppCompatActivity {

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding_screen);

        PaperOnboardingPage scr1 = new PaperOnboardingPage("Make Your Connections",
                "Make connection with your friend and well-wisher to ",
                Color.parseColor("#678FB4"), R.drawable.team_small, R.drawable.team_small);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("Two Way Connection",
                "Ones they accept your request you both will be connected to each other",
                Color.parseColor("#65B0B4"), R.drawable.hand_small, R.drawable.hand_small);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("Track your Loved Ones",
                "Send realtime location to friends when you want with just one tap",
                Color.parseColor("#9B90BC"), R.drawable.mapssmall, R.drawable.mapssmall);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);

        PaperOnboardingFragment onBoardingFragment = PaperOnboardingFragment.newInstance(elements);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, onBoardingFragment);
        fragmentTransaction.commit();

        onBoardingFragment.setOnRightOutListener(new PaperOnboardingOnRightOutListener() {
            @Override
            public void onRightOut() {
                Intent intent = new Intent(OnBoardingScreen.this , MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
