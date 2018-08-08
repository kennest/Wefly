package com.wefly.wealert.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;
import com.wefly.wealert.fragments.OptionsDialogFragment;
import com.wefly.wealert.R;

import java.util.ArrayList;
import java.util.List;

public class onboardActivity extends AhoyOnboarderActivity {
    boolean onboardpassed;
    boolean agree;
    boolean setPeriodPassed;
    OptionsDialogFragment options=new OptionsDialogFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //We present the terms and privacy Dialog
        checkTermsAgreed();

        //We check if the has already passed the on board screen
        checkOnboardPassed();

        checkSetPeriodPassed();

        //Create Cards for Onboard
        AhoyOnboarderCard welcomeCard = new AhoyOnboarderCard(getString(R.string.onboard1_title), getString(R.string.onboard1_description), R.drawable.ic_logo);
        welcomeCard.setBackgroundColor(R.color.black_transparent);
        welcomeCard.setTitleColor(R.color.white);
        welcomeCard.setDescriptionColor(R.color.grey_200);
        welcomeCard.setTitleTextSize(dpToPixels(10, this));
        welcomeCard.setDescriptionTextSize(dpToPixels(12, this));
        welcomeCard.setIconLayoutParams(250, 250, 50, 50, 50, 50);

        AhoyOnboarderCard emailCard = new AhoyOnboarderCard(getString(R.string.onboard2_title), getString(R.string.onboard2_description), R.drawable.photo_camera);
        emailCard.setBackgroundColor(R.color.black_transparent);
        emailCard.setTitleColor(R.color.white);
        emailCard.setDescriptionColor(R.color.grey_200);
        emailCard.setTitleTextSize(dpToPixels(10, this));
        emailCard.setDescriptionTextSize(dpToPixels(12, this));
        emailCard.setIconLayoutParams(250, 250, 50, 50, 50, 50);

        AhoyOnboarderCard lastCard = new AhoyOnboarderCard(getString(R.string.onboard3_title), getString(R.string.onboard3_description), R.drawable.success);
        lastCard.setBackgroundColor(R.color.black_transparent);
        lastCard.setTitleColor(R.color.white);
        lastCard.setDescriptionColor(R.color.grey_200);
        lastCard.setTitleTextSize(dpToPixels(10, this));
        lastCard.setDescriptionTextSize(dpToPixels(12, this));
        lastCard.setIconLayoutParams(250, 250, 50, 50, 50, 50);

        //add cards to List
        List<AhoyOnboarderCard> pages = new ArrayList<>();
        pages.add(welcomeCard);
        //pages.add(databaseCard);
        pages.add(emailCard);
        pages.add(lastCard);
        setOnboardPages(pages);
        setImageBackground(R.drawable.img_background);
        setFinishButtonTitle(R.string.start);
        setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.rounded_button));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOnboardPassed();
    }

    @Override
    public void onFinishButtonPressed() {
        SharedPreferences sp = getApplicationContext().getSharedPreferences("boot_options", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("onboardpassed", true);
        editor.commit();
        Intent splash = new Intent(this, SplashScreensActivity.class);
        startActivity(splash);
    }

    protected void checkOnboardPassed() {
        SharedPreferences sp = getApplicationContext().getSharedPreferences("boot_options", 0);
        onboardpassed = sp.getBoolean("onboardpassed", false);
        if (onboardpassed) {
            Intent splash = new Intent(this, SplashScreensActivity.class);
            startActivity(splash);
        }
    }

    protected void checkTermsAgreed(){
        SharedPreferences sp = getApplicationContext().getSharedPreferences("boot_options", 0);
        agree=sp.getBoolean("agree",false);
        Bundle bundle=new Bundle();
        if(agree==false) {
            bundle.putString("tag", "terms");
            options.setArguments(bundle);
            options.setCancelable(false);
            options.show(getSupportFragmentManager(), "Terms");
        }else{
            return;
        }
    }
    protected void checkSetPeriodPassed(){
        SharedPreferences sp = getApplicationContext().getSharedPreferences("boot_options", 0);

        setPeriodPassed=sp.getBoolean("setPeriodPassed",false);

        if(setPeriodPassed==false) {
            startActivity(new Intent(this, WorkRangeActivity.class));
        }
    }
}
