package com.wefly.wealert.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.RelativeLayout;

import com.wefly.wealert.events.InitDataEmptyEvent;
import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.presenters.FormActivity;
import com.wefly.wealert.presenters.RecipientPresenter;
import com.wefly.wealert.tasks.CategoryGetTask;
import com.wefly.wealert.tasks.RecipientGetTask;
import com.wefly.wealert.utils.Constants;
import com.wefly.wealert.utils.NetworkWatcher;
import com.wefly.wealert.utils.PermissionUtil;
import com.wefly.wealert.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SplashScreensActivity extends FormActivity {
    private static boolean isAllPermissionGranted = false;
    private static boolean showPermissionDialog;
    private static boolean isFirstTime = true;
    private PermissionUtil pUtil;
    private RelativeLayout rLayout;
    List<Recipient> recipients = new ArrayList<>();
    static Map<String, Integer> response_category = new HashMap<>();

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Log.e(getLocalClassName(),"Started");

        getInitData();

        pUtil = new PermissionUtil(this);
        rLayout = findViewById(R.id.Rlayout);
        watcher = new NetworkWatcher(this, rLayout);

        //check permission
        isAllPermissionGranted = pUtil.isAllPermissionsGranded();
        if (!isAllPermissionGranted) {
            if (isFirstTime) {
                showPermissionDialog = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            onRequestAllPermissions();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, TimeUnit.SECONDS.toMillis(1));
                isFirstTime = false;
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //check if User is already connected
                    //Read token
                    try {
                        //start session
                        startSession();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 2000);
        }

    }


    public void onRequestAllPermissions() {
        //request Permission
        pUtil.requestAllPermissions();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (showPermissionDialog)
            pUtil.requestAllPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_APP_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    showPermissionDialog = false;
                    isAllPermissionGranted = true;

                    // Check user Token
                    try {
                        //start session
                        startSession();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    showPermissionDialog = true;
                    // Display on Next launch
                    isFirstTime = true;
                    try {
                        pUtil.onAllPermissionError(pUtil.getPermissionsDenied());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //not granted
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void startSession() {
        if (appController != null) {
            if (appController.isTokenValide()) {
                if (watcher != null && rLayout != null) {
                    watcher.setOnOffLineListener(new NetworkWatcher.OnOffLineListener() {
                        @Override
                        public void onOffLine() {
                            startActivity(new Intent(SplashScreensActivity.this, BootActivity.class));
                            finish();
                        }

                        @Override
                        public void onOnLine() {
                            // RECIPIENTS
                            RecipientPresenter presenter = new RecipientPresenter(SplashScreensActivity.this);
                            presenter.setOnRecipientDownloadCallBack(() -> {

                            });
                            presenter.downloadAllRecipients(false);
                            startActivity(new Intent(SplashScreensActivity.this, BootActivity.class));
                            finish();
                        }
                    });
                    watcher.checkOffLine();
                }

            } else {
                startActivity(new Intent(SplashScreensActivity.this, LoginActivity.class));
                finish();
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInitDataEmptyEvent(InitDataEmptyEvent event) {
        Snackbar.make(rLayout, event.message, Snackbar.LENGTH_LONG).show();
        AlertDialog.Builder builder=new AlertDialog.Builder(getApplicationContext());
        builder.setTitle(getString(R.string.init_value_error));
        builder.setMessage(event.message);
        builder.setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    protected void getInitData(){
        //We launch category task and pass the categorie Map to the dialog
        try {
            CategoryGetTask task = new CategoryGetTask(appController);
            response_category = task.execute().get();
            appController.setAlert_categories(response_category);
        } catch (InterruptedException | ExecutionException e) {
            EventBus.getDefault().post(new InitDataEmptyEvent(getString(R.string.init_value_missing)));
            e.printStackTrace();
        }

        //We launch the recipient task and pass the recipients list to the dialog in bundle
        try {
            recipients = new RecipientGetTask().execute().get();
        } catch (ExecutionException | InterruptedException e) {
            EventBus.getDefault().post(new InitDataEmptyEvent(getString(R.string.init_value_missing)));
            e.printStackTrace();
        }

        if(recipients.size()<=0 || response_category.size()<=0){
            EventBus.getDefault().post(new InitDataEmptyEvent(getString(R.string.init_value_missing)));
        }
    }
}
