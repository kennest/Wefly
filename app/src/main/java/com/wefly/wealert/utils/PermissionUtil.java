package com.wefly.wealert.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;


import com.wefly.wealert.R;

import java.util.ArrayList;

/**
 * Created by Obrina.KIMI on 11/23/2017.
 */

public class PermissionUtil {
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static boolean isPermissionNeed = false;


    public PermissionUtil(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.permission_preference), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isPermissionNeed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


    private  void requestGroupPermission(ArrayList<String> permissions, Activity activity)
    {
        try{
            if(isPermissionNeed){
                if(permissions.size()>0){
                    String[] permissionList = new String[permissions.size()];
                    permissions.toArray(permissionList);
                    ActivityCompat.requestPermissions(activity, permissionList, Constants.REQUEST_GROUP_PERMISSION);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void requestAllPermissions(){
        try{
            if(isPermissionNeed){
                ArrayList<String> permissionNeeded = new ArrayList<>();
                ArrayList<String> permissionAvailable = new ArrayList<>();
                permissionAvailable.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissionAvailable.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                permissionAvailable.add(Manifest.permission.ACCESS_FINE_LOCATION);
                permissionAvailable.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                permissionAvailable.add(Manifest.permission.CAMERA);
                permissionAvailable.add(Manifest.permission.VIBRATE);
                permissionAvailable.add(Manifest.permission.RECORD_AUDIO);
                for(String permission: permissionAvailable){
                    if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                        permissionNeeded.add(permission);
                }
                if(permissionNeeded.size() > 0)
                    requestGroupPermission(permissionNeeded, (Activity) context);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onAllPermissionError( ArrayList<String> nameArray){
        try{
            if(isPermissionNeed){
                if(nameArray.size() > 0){
                    String content = context.getString(R.string.error_permission_all_left)+ " ";
                    for(String name: nameArray){


                        if(name.equals(Manifest.permission.READ_EXTERNAL_STORAGE)){
                            if(!content.contains("Stokage"))
                                content = content + "Stokage" + " ";
                        }
                        if(name.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                            if(!content.contains("Stokage"))
                                content = content + "Stokage" + " ";

                        }

                        if(name.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            if(!content.contains("Position"))
                                content = content + "Position" + " ";
                        }

                        if(name.equals(Manifest.permission.ACCESS_COARSE_LOCATION)){
                            if(!content.contains("Position"))
                                content = content + "Position" + " ";
                        }

                        if(name.equals(Manifest.permission.CAMERA)){
                            if(!content.contains("Appareil photo"))
                                content = content + "Appareil photo" + " ";
                        }

                        if(name.equals(Manifest.permission.VIBRATE)){
                            if(!content.contains("Appareil photo"))
                                content = content + "Appareil photo" + " ";
                        }

                        if(name.equals(Manifest.permission.RECORD_AUDIO)){
                            if(!content.contains("Microphone"))
                                content = content + "Microphone" + " ";
                        }
                    }
                    content = content + " " + context.getString(R.string.error_permission_all_right);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getString(R.string.error_permission_all_title));
                    builder.setMessage(content);


                    builder.setPositiveButton("Autoriser", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",(context.getPackageName()), null);
                            intent.setData(uri);
                            context.startActivity(intent);
                        }
                    });

                    builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Utils utils =  new Utils();
                            ((Activity) context).finish();
                            utils.animActivityClose((Activity) context);
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isPermissionsGranted(){
        ArrayList<String> permissionAvailable = new ArrayList<>();
        permissionAvailable.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionAvailable.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionAvailable.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionAvailable.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionAvailable.add(Manifest.permission.CAMERA);
        permissionAvailable.add(Manifest.permission.VIBRATE);
        permissionAvailable.add(Manifest.permission.RECORD_AUDIO);
        for(String permission: permissionAvailable){
            if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public @Nullable  ArrayList<String> getPermissionsDenied(){
        try{
            ArrayList<String> permissionAvailable = new ArrayList<>();
            ArrayList<String> permissionDenied = new ArrayList<>();

            permissionAvailable.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionAvailable.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissionAvailable.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionAvailable.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissionAvailable.add(Manifest.permission.CAMERA);
            permissionAvailable.add(Manifest.permission.VIBRATE);
            permissionAvailable.add(Manifest.permission.RECORD_AUDIO);
            for(String permission: permissionAvailable){
                if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                    permissionDenied.add(permission);
            }
            return permissionDenied;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



    public boolean isAllPermissionsGranded(){
        try{
            if(isPermissionNeed){
                ArrayList<String> permissionNeeded = new ArrayList<>();
                ArrayList<String> permissionAvailable = new ArrayList<>();
                permissionAvailable.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissionAvailable.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                permissionAvailable.add(Manifest.permission.ACCESS_FINE_LOCATION);
                permissionAvailable.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                permissionAvailable.add(Manifest.permission.CAMERA);
                permissionAvailable.add(Manifest.permission.VIBRATE);
                permissionAvailable.add(Manifest.permission.RECORD_AUDIO);
                for(String permission: permissionAvailable){
                    if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                        permissionNeeded.add(permission);
                }
                if(permissionNeeded.size() > 0){
                    return false;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }



}
