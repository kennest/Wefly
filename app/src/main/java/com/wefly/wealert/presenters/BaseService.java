package com.wefly.wealert.presenters;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wefly.wealert.utils.Constants;
import com.wefly.wealert.utils.PermissionUtil;


/**
 * Created by admin on 04/04/2018.
 */

public class BaseService extends Service {
    public static Double uLatitude = Constants.DOUBLE_NULL;
    public static Double uLongitude = Constants.DOUBLE_NULL;
    protected static boolean isAllPermissionGranted = false;
    protected PermissionUtil pUtil;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pUtil = new PermissionUtil(this);
        //check permission
        isAllPermissionGranted = pUtil.isAllPermissionsGranded();
    }
}
