package com.wefly.wealert.services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.wefly.wealert.R;
import com.wefly.wealert.dbstore.Alert;
import com.wefly.wealert.dbstore.OtherRecipient;
import com.wefly.wealert.models.Piece;
import com.wefly.wealert.observables.AlertPostObservable;
import com.wefly.wealert.observables.PieceUploadObservable;
import com.wefly.wealert.utils.AppController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.objectbox.Box;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.wefly.wealert.utils.AppController.boxStore;

public class OfflineService extends Service {
    Box<Alert> box = boxStore.boxFor(Alert.class);
    List<Piece> pieces = new ArrayList<>();
    Piece piece = new Piece();
    Timer mTimer = null;
    long period = 20000;
    long delay=30000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mTimer = new Timer();
        TimerTask timerTask=new TimerTaskSendAlertLocation(box.getAll().size(),mTimer);
        mTimer.schedule(timerTask, delay, period);
    }

    @SuppressLint("CheckResult")
    protected void doSend() {
        Toast.makeText(getBaseContext(), "Offline task Init:" + box.getAll().size(), Toast.LENGTH_SHORT).show();
        SharedPreferences sp = getSharedPreferences("recipients", 0);

        for (Alert a : box.getAll()) {
            com.wefly.wealert.models.Alert alert = new com.wefly.wealert.models.Alert();

            alert.setContent(a.getContent());
            alert.setCategory(a.getCategory());
            alert.setObject(a.getTitle());

            Set<String> recipient_ids = new HashSet<>();

            for (OtherRecipient r : a.otherRecipients) {
                recipient_ids.add(String.valueOf(r.getRaw_id()));
            }

            sp.edit().putStringSet("recipients_id", recipient_ids).apply();

            for (com.wefly.wealert.dbstore.Piece p : a.pieces) {
                if (p.getUrl() != null) {
                    piece.setUrl(p.getUrl());
                    pieces.add(piece);
                }
            }
            SendAlertRx(alert, a);
        }
    }

    public void SendAlertRx(com.wefly.wealert.models.Alert a, Alert an) {
        if (hasRecipientsID() && box.getAll().size()>0) {
            Observer mObserver = new Observer<Boolean>() {

                @Override
                public void onSubscribe(Disposable d) {
                    Log.e("AlertBox count 0",String.valueOf(box.count()));
                    String msg = getString(R.string.offline_start_txt);
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    showNotification(msg);
                }

                @Override
                public void onNext(Boolean r) {
                    if(r) {
                        box.remove(an);
                        Log.e("AlertBox count A", String.valueOf(box.count()));
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(getApplicationContext(), R.string.app_name + "offline task error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onComplete() {
                    SharedPreferences sp = getSharedPreferences("recipients", 0);
                    sp.edit().remove("recipients_id").apply();
                    Log.e("AlertBox count Z",String.valueOf(box.count()));
                    uploadRx(pieces);
                }
            };
            Observable<Boolean> observable = new AlertPostObservable().send(a);
            observable
                    .take(box.count())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(mObserver);
        }
    }

    private void uploadRx(List<Piece> plist) {
        Observer mObserver = new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean r) {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(getApplicationContext(), R.string.app_name + "offline task error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                box.removeAll();
                String msg = getString(R.string.offline_end_txt);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                showNotification(msg);
            }
        };

        Observable<Boolean> observable = new PieceUploadObservable().upload(plist);
        observable
                .take(pieces.size())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(mObserver);
    }

    private boolean hasRecipientsID() {
        SharedPreferences sp = getSharedPreferences("recipients", 0);
        Set recipient_ids = sp.getStringSet("recipients_id", new HashSet<String>());
        Log.e(getPackageResourcePath(), "recipient id size" + recipient_ids.size());
        if (recipient_ids.size() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    private class TimerTaskSendAlertLocation extends TimerTask {
        private int maxCalledTimes = 0;
        private int calledTimes=0;
        private Timer timer;

        TimerTaskSendAlertLocation(int maxCalledTimes, Timer timer) {
            this.maxCalledTimes = maxCalledTimes;
            this.timer = timer;
            Log.e("AlertBox count A",String.valueOf(box.count()));
        }

        @SuppressLint("CheckResult")
        @Override
        public void run() {
            if (calledTimes == maxCalledTimes) {
                timer.cancel();
                Log.e("Offline service:","CANCELED");
            } else {
                Log.e("Offline service:","STARTED");
                ReactiveNetwork.observeInternetConnectivity()
                        .subscribeOn(Schedulers.trampoline())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean isConnectedToInternet) {
                                if (isConnectedToInternet) {
                                    calledTimes++;
                                    if (box.getAll().size() > 0) {
                                        doSend();
                                    }
                                }
                            }
                        });
            }
            Log.e("Offline service:","FINISHED");
        }
    }

    public void showNotification(String msg) {
        //Get an instance of NotificationManager//
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle("Wefly")
                        .setContentText(msg);

        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

//        NotificationManager.notify().
        mNotificationManager.notify(001, mBuilder.build());
    }
}
