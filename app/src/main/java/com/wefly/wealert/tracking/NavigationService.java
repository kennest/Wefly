package com.wefly.wealert.tracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.wefly.wealert.utils.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by deepshikha on 24/11/16.
 */

public class NavigationService extends Service implements LocationListener {

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    String nblatitude, nblongitudetude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    //    long notify_interval = 5000;
    long notify_interval = 1800000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent intent;
    private String USERID_PREF_NAME = "userCredential";
    private String CURRENT_REPPORT_PREF_NAME = "currentRepports";
    private String REPORT_FILE_PREF_NAME = "repportFile";
    int time = 0;

    public NavigationService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 1, notify_interval);
        intent = new Intent(str_receiver);
//        fn_getlocation();

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void fn_getlocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable) {

        } else {

            if (isNetworkEnable) {
                location = null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {

                            Log.e("latitude", location.getLatitude() + "");
                            Log.e("longitude", location.getLongitude() + "");
                            nblatitude = Double.toString(Math.abs(location.getLatitude()));
                            int integerPlaces = nblatitude.indexOf('.');
                            int decimalPlaces = nblatitude.length() - integerPlaces - 1;
                            Log.e("ici", decimalPlaces + "");
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            fn_update(location);
                        }
                    }
                    return;
                }
            }


            if (isGPSEnable) {
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        Log.e("latitude", location.getLatitude() + "");
                        Log.e("longitude", location.getLongitude() + "");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                    }
                }
            }


        }

    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(() -> fn_getlocation());

        }
    }

    @SuppressLint("CheckResult")
    private void fn_update(Location location) {
        Date date = new Date();
        long currentTime = date.getTime();
        int currentHour = date.getHours();
        JSONArray reportFile = new JSONArray();
        JSONArray locations = new JSONArray();
//         getting lovale gmt
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();
        DateFormat gmtdate = new SimpleDateFormat("Z");
        String localTime = gmtdate.format(currentLocalTime);
//         end getting local gmt

//      on vérife que le fichier existe s

        Log.e("heure", currentHour + "");
        AppController appController=AppController.getInstance();
        String userId = appController.getUserId();
        Log.e("userid", userId + "");

        if (6 <= currentHour && currentHour <= 18) {
//            time += 1;time < 10
            JSONObject locate = new JSONObject();
            try {
                locate.put("Time", currentTime);
                locate.put("Latitude", latitude);
                locate.put("Longitude", longitude);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            File f = new File(
                    "/data/data/com.wefly.wealert/shared_prefs/currentRepports.xml");
            //On recupère l'identifiant de l'utilisateur
            if (f.exists()) {
                Log.e("sharepref", "exist");
                //On recupère le contenu du fichier de report
                SharedPreferences scurrentRepport = getSharedPreferences(CURRENT_REPPORT_PREF_NAME, MODE_PRIVATE);
                String currentReport = scurrentRepport.getString("report", "");
                try {
                    //On crée un objet à partir du contenu
                    JSONObject curenReportObj = new JSONObject(currentReport);
                    //ON crée un tableau contenant chaque locations
                    JSONArray locationArray = curenReportObj.getJSONArray("Positions");
                    // On rcupère l'identifiant dans le fichier charepreferences
                    //On ajoute la nouvelle position à l'ancien tableau
                    String gmt = curenReportObj.getString("gmt");
                    locationArray.put(locate);
                    // On crée un nouveau repport à partir des nouvelles données
                    JSONObject report = new JSONObject();
                    try {
                        report.put("Userid", userId);
                        report.put("Date", currentTime);
                        report.put("gmt", gmt);
                        report.put("Positions", locationArray);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //On met à jour le fichier current locations
                    SharedPreferences NewSettings = getSharedPreferences(CURRENT_REPPORT_PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor leditor = NewSettings.edit();
                    leditor.putString("report", report + "");
                    leditor.apply();

                    String newRepport = NewSettings.getString("report", "");
                    Log.e("repport", newRepport + "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                locations.put(locate);
                // On crée un nouveau repport à partir des nouvelles données
                JSONObject report = new JSONObject();
                try {
                    report.put("Userid", userId);
                    report.put("Date", currentTime);
                    report.put("gmt", localTime);
                    report.put("Positions", locations);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                //On met à jour le fichier current locations
                SharedPreferences NewSettings = getSharedPreferences(CURRENT_REPPORT_PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor leditor = NewSettings.edit();
                leditor.putString("report", report + "");
                leditor.apply();

                String newRepport = NewSettings.getString("report", "");
                Log.e("repport", newRepport + "");

            }
        } else {
            //On vérifie que le fichier report existe si oui
            File cf = new File(
                    "/data/data/com.wefly.wealert/shared_prefs/currentRepports.xml");

            if (cf.exists()) {
                //On vérifie que le fichier report file existe

                File rf = new File(
                        "/data/data/com.wefly.wealert/shared_prefs/repportFile.xml");
                if (rf.exists()) {
                    //On recupère le contenu du fichier de file report
                    SharedPreferences fileRepport = getSharedPreferences(REPORT_FILE_PREF_NAME, MODE_PRIVATE);
                    String fileRepportString = fileRepport.getString("reportfile", "");
                    try {
                        JSONArray reportArray = new JSONArray(fileRepportString);
                        //On recupère le contenu du fichier report
                        SharedPreferences scurrentRepport = getSharedPreferences(CURRENT_REPPORT_PREF_NAME, MODE_PRIVATE);
                        String currentReport = scurrentRepport.getString("report", "");
                        JSONObject curenReportObj = new JSONObject(currentReport);
                        //On supprime le fichier current reppor après avoir pris son contenu
                        cf.delete();
                        //-----------------------------------------------------------------
                        // On ajoute le contenu du rapport courant au fichier de rapport
                        reportArray.put(curenReportObj);
                        //puis on met à jours le fichier de rapport
                        SharedPreferences NewRepportSettings = getSharedPreferences(REPORT_FILE_PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor leditor = NewRepportSettings.edit();
                        leditor.putString("reportfile", reportArray + "");
                        leditor.apply();

                        String newRepportfile = NewRepportSettings.getString("reportfile", "");
                        Log.e("repport", newRepportfile + "");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    JSONArray reportArray = new JSONArray();
                    try {
                        //On recupère le contenu du fichier report
                        SharedPreferences scurrentRepport = getSharedPreferences(CURRENT_REPPORT_PREF_NAME, MODE_PRIVATE);
                        String currentReport = scurrentRepport.getString("report", "");
                        JSONObject curenReportObj = new JSONObject(currentReport);
                        //On supprime le fichier current reppor après avoir pris son contenu
                        cf.delete();
                        //-----------------------------------------------------------------
                        // On ajoute le contenu du rapport courant au fichier de rapport
                        reportArray.put(curenReportObj);
                        //puis on met à jours le fichier de rapport
                        SharedPreferences NewRepportSettings = getSharedPreferences(REPORT_FILE_PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor leditor = NewRepportSettings.edit();
                        leditor.putString("reportfile", reportArray + "");
                        leditor.apply();

                        String newRepportfile = NewRepportSettings.getString("reportfile", "");
                        Log.e("repport", newRepportfile + "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
            //On ajoute son contenu au fichier report file puis on le supprime
        }

//        On vérifie que le fichier report file existe si oui

        File rf = new File(
                "/data/data/com.wefly.wealert/shared_prefs/repportFile.xml");
        if (rf.exists()) {

            ReactiveNetwork.observeInternetConnectivity()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override public void accept(Boolean isConnectedToInternet) {
                            if(isConnectedToInternet){
                                SharedPreferences fileRepport = getSharedPreferences(REPORT_FILE_PREF_NAME, MODE_PRIVATE);
                                String fileRepportString = fileRepport.getString("reportfile", "");
                                //Debut requète vers la base de donnée
                                SendRepportsTask sendrepport = new SendRepportsTask(fileRepportString, getApplicationContext());
                                sendrepport.execute();
                            }
                        }
                    });
        }

        intent.putExtra("latutide", location.getLatitude() + "");
        intent.putExtra("longitude", location.getLongitude() + "");
        sendBroadcast(intent);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


}
