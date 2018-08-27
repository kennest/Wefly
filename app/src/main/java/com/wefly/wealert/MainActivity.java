package com.wefly.wealert;

import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.wefly.wealert.models.Alert;
import com.wefly.wealert.presenters.DBActivity;
import com.wefly.wealert.tasks.AlertReceiveGetTask;
import com.wefly.wealert.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends DBActivity {
    private int selected;
    private Button recordbtn;
    private RelativeLayout rLayout;
    View vEmail, vSms, vAlert;
    View vsSent, vsReceive, vsDraft;
    List<Alert> alertList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rLayout = findViewById(R.id.Rlayout);

        // Email fragment


        // Setup custom tab
        ViewGroup tab = findViewById(R.id.tab_layout);
        //tab.addView(LayoutInflater.from(this).inflate(R.layout.fragment_main, tab, false));

        ViewPager viewPager = findViewById(R.id.viewpager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == 10) {
                Log.v("Record OK", "OK");
            } else {
                // Oops! User has canceled the recording
                Log.v("Record OK", "No");
            }
        }
    }

//    private void changeBoomButton(int index) {
//        // From version 2.0.9, BMB supports a new feature to change contents in boom-button
//        // by changing contents in the corresponding builder.
//        // Please notice that not every method supports this feature. Only the method whose comment
//        // contains the "Synchronicity" tag supports.
//        // For more details, check:
//        // https://github.com/Nightonke/BoomMenu/wiki/Change-Boom-Buttons-Dynamically
//        HamButton.Builder builder = (HamButton.Builder) bmb.getBuilder(index);
//        if (index == 0) {
//            builder.normalText("Changed!");
//            builder.highlightedText("Highlighted, changed!");
//            builder.subNormalText("Sub-text, changed!");
//            builder.normalTextColor(Color.YELLOW);
//            builder.highlightedTextColorRes(R.color.colorPrimary);
//            builder.subNormalTextColor(Color.BLACK);
//        } else if (index == 1) {
//            builder.normalImageRes(R.drawable.ic_login);
//            builder.highlightedImageRes(R.drawable.bear);
//        } else if (index == 2) {
//            builder.normalColorRes(R.color.colorAccent);
//        } else if (index == 3) {
//            builder.pieceColor(Color.WHITE);
//        } else if (index == 4) {
//            builder.unable(true);
//        }
//    }

    protected void loadAlertList(View v){
        //On charge la page alert avec les alerts
        try {
            alertList = new AlertReceiveGetTask(appController).execute().get();
            Log.v(getPackageName() + "emailList Size", String.valueOf(alertList.size()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
//
//        if (alertList.size() > 0) {
//            emailListView.setAdapter(new AlertAdapter(getApplicationContext(), alertList));
//        }
    }

    protected void loadEmailList(View v) {

        //On charge la page email avec les emails
//        try {
//            //emailList = new EmailReceiveGetTask(appController).execute().get();
//            Log.v(getPackageName() + "emailList Size", String.valueOf(emailList.size()));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        ListView emailListView = v.findViewById(R.id.emailListView);
//
//        if (emailList.size() > 0) {
//            emailListView.setAdapter(new EmailAdapter(getApplicationContext(), emailList));
//        }
    }

    @Override
    public void onConnected() {
        super.onConnected();

        switch (selected) {

            case 1: // 1 is undefine

                break;
            default:
                break;
        }
        selected = 0;


    }

    @Override
    public void onNotConnected() {
        super.onNotConnected();
    }

    @Override
    public void onRetry() {
        super.onRetry();
        if (watcher != null)
            watcher.isNetworkAvailable();
    }
}
