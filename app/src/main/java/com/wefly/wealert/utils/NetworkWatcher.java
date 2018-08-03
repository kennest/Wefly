package com.wefly.wealert.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wefly.wealert.R;


/**
 * Created by admin on 26/04/2018.
 */

public class NetworkWatcher {
    private Context ctx;
    private View v;
    private final AppController appController;
    private OnInternetListener listener;
    private OnOffLineListener offListener;
    private String TAG = getClass().getSimpleName();
    public NetworkWatcher(final Context ctx, @NonNull View view){
        this.ctx = ctx;
        appController = AppController.getInstance();
        this.v = view;
    }


    public void isNetworkAvailable() {
        if (appController.isNetworkAvailable()){
            Log.v(Constants.APP_NAME, TAG + " isNetworkAvailable TRUE RUN");
            notifyOnInternetListener(true, false);
        }else {
            try {
                Snackbar snackbar = Snackbar
                        .make(v, ctx.getResources().getString(R.string.error_no_internet), Snackbar.LENGTH_INDEFINITE)
                        .setAction(ctx.getResources().getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                notifyOnInternetListener(false, true);
                            }
                        });
                snackbar.setActionTextColor(Color.RED);
                View sbView = snackbar.getView();
                TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
                snackbar.show();
                notifyOnInternetListener(false, false);
            } catch (Exception e){
                e.printStackTrace();
            }
            Log.v(Constants.APP_NAME, TAG + " isNetworkAvailable FALSE RUN");
        }
    }



    public void isNetworkAvailableWithSilent() {
        if (appController.isNetworkAvailable()){
            notifyOnInternetListener(true, false);
        }else
            notifyOnInternetListener(false, false);
    }

    public  void checkOffLine() {
        if (!appController.isNetworkAvailable()){
            try {
                Snackbar snackbar = Snackbar
                        .make(v, ctx.getResources().getString(R.string.error_no_connection), Snackbar.LENGTH_INDEFINITE)
                        .setAction(ctx.getResources().getString(R.string.off_line), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                notifyOnOffLineListener(true);
                            }
                        });
                snackbar.setActionTextColor(Color.RED);
                View sbView = snackbar.getView();
                TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
                snackbar.show();
            } catch (Exception e){
                e.printStackTrace();
            }
        }else
            notifyOnOffLineListener(false);
    }


    public void setOnInternetListener(@NonNull OnInternetListener listener) {
        this.listener = listener;

    }

    public void setOnOffLineListener(@NonNull OnOffLineListener listener) {
        this.offListener = listener;

    }

    public interface OnInternetListener {
        void onConnected();
        void onNotConnected();
        void onRetry();
    }

    public interface OnOffLineListener {
        void onOffLine();
        void onOnLine();
    }

    private void notifyOnOffLineListener(boolean isOK) {
        if (offListener != null){
            try {
                if (isOK){
                    offListener.onOffLine();
                }else{
                    offListener.onOnLine();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void notifyOnInternetListener(boolean isOK, boolean shouldRetry) {
        if (listener != null){
            try {
                if (shouldRetry){
                    listener.onRetry();
                    Log.v(Constants.APP_NAME, TAG + " notifyOnInternetListener ON RETRY FALSE RUN");
                }
                else {
                    if (isOK){
                        Log.v(Constants.APP_NAME, TAG + " notifyOnInternetListener ON CONNECT  RUN");
                        listener.onConnected();
                    }else{
                        listener.onNotConnected();
                        Log.v(Constants.APP_NAME, TAG + " notifyOnInternetListener ON NOT CONNECT  RUN");
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }else
            Log.v(Constants.APP_NAME, TAG + " notifyOnInternetListener LISTENER IS NULL RUN");
    }
}
