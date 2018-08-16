package com.wefly.wealert.presenters;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.wefly.wealert.activities.LoginActivity;
import com.wefly.wealert.interfaces.MainAction;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.Constants;
import com.wefly.wealert.utils.NetworkWatcher;
import com.wefly.wealert.utils.PermissionUtil;

import java.util.concurrent.TimeUnit;


/**
 * Created by admin on 21/03/2018.
 */

public class BaseActivity extends AppCompatActivity implements NetworkWatcher.OnInternetListener, MainAction {
    public static Double uLatitude = Constants.DOUBLE_NULL;
    public static Double uLongitude = Constants.DOUBLE_NULL;
    protected static boolean isAllPermissionGranted = false;
    private static boolean showPermissionDialog;
    private static boolean isFirstTime = true;

    // Support vector
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    protected PermissionUtil pUtil;
    protected NetworkWatcher watcher;
    protected AppController appController;
    protected boolean isReqDone;
    private String TAG = getClass().getSimpleName();

    public static boolean isAllPermissionGranted() {
        return isAllPermissionGranted;
    }

    public static void setIsAllPermissionGranted(boolean isAllPermissionGranted) {
        BaseActivity.isAllPermissionGranted = isAllPermissionGranted;
    }

    public static Double getCurrentLatitude() {
        return uLatitude;
    }

    public static Double getCurrentLongitude() {
        return uLongitude;
    }

    public static void setuLatitude(@NonNull Double uLatitude) {
        BaseActivity.uLatitude = uLatitude;
    }

    public static void setuLongitude(@NonNull Double uLongitude) {
        BaseActivity.uLongitude = uLongitude;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);

        pUtil = new PermissionUtil(this);
        appController = AppController.getInstance();
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
                            isReqDone = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, TimeUnit.SECONDS.toMillis(1));
                isFirstTime = false;
            }
        } else
            isReqDone = true;

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
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_APP_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    showPermissionDialog = false;
                    isAllPermissionGranted = true;
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

    protected void checkTokenAndNetwork(@NonNull final BaseActivity activity, @NonNull final NetworkWatcher w) {
        if (appController != null) {
            if (appController.isTokenValide()) {
                Log.v(Constants.APP_NAME, TAG + " TOKEN IS VALID RUN");
                if (w != null)
                    w.isNetworkAvailable();
            } else {
                startActivity(new Intent(activity, LoginActivity.class));
                activity.finish();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onNotConnected() {

    }

    @Override
    public void onRetry() {

    }

    @Override
    public void onDisplayMainActivity(Activity activity) {
        new MainActivityClass().onDisplayMainActivity(activity);
    }

    protected void removeAsynTasks() {
        AppController.clearAsynTask();
    }
}
