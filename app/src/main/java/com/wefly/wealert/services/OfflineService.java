package com.wefly.wealert.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.wefly.wealert.R;
import com.wefly.wealert.activities.BootActivity;
import com.wefly.wealert.dbstore.Alert;
import com.wefly.wealert.events.RecipientEmptyEvent;
import com.wefly.wealert.models.Piece;
import com.wefly.wealert.observables.AlertPostObservable;
import com.wefly.wealert.observables.PieceUploadObservable;
import com.wefly.wealert.tracking.NavigationService;
import com.wefly.wealert.tracking.SendRepportsTask;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
    Set<Piece> pieces = new HashSet<>();
    Piece piece = new Piece();
    com.wefly.wealert.models.Alert alert = new com.wefly.wealert.models.Alert();
    Timer mTimer = null;
    long notify_interval = 30000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskSendAlertLocation(), 1, notify_interval);
    }

    @SuppressLint("CheckResult")
    protected void doSend() {
        Box<Alert> alertBox = boxStore.boxFor(Alert.class);
        if(alertBox.count()>0){
            Toast.makeText(getBaseContext(),"Offline task Init",Toast.LENGTH_LONG).show();
            Alert a = boxStore.boxFor(Alert.class).get(alertBox.count());
            alert.setContent(a.getContent());
            alert.setCategory(a.getRecipientsID());
            alert.setObject(a.getTitle());
            for (com.wefly.wealert.dbstore.Piece p : a.pieces) {
                piece.setUrl(p.getUrl());
                pieces.add(piece);
            }
            SendAlertRx(alert);
        }
    }

    public void SendAlertRx(com.wefly.wealert.models.Alert a) {
        if (hasRecipientsID()) {
            Observer mObserver = new Observer<Boolean>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Boolean r) {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    uploadRx(pieces, a);
                }
            };
            Observable<Boolean> observable = new AlertPostObservable().send(a);

            observable.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(mObserver);
        } else {
            EventBus.getDefault().post(new RecipientEmptyEvent(getString(R.string.empty_recipient)));
        }
    }

    private void uploadRx(Set<Piece> plist, com.wefly.wealert.models.Alert a) {
        Observer mObserver = new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean r) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                box.remove(box.count());
            }
        };
        Observable<Boolean> observable = new PieceUploadObservable().upload(plist, a);

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
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
        @SuppressLint("CheckResult")
        @Override
        public void run() {
            ReactiveNetwork.observeNetworkConnectivity(getApplicationContext())
                    .subscribeOn(Schedulers.io())
                    .filter(ConnectivityPredicate.hasState(NetworkInfo.State.CONNECTED))
                    .filter(ConnectivityPredicate.hasType(ConnectivityManager.TYPE_MOBILE))
                    .filter(ConnectivityPredicate.hasType(ConnectivityManager.TYPE_WIFI))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Connectivity>() {
                        @Override public void accept(final Connectivity connectivity) {
                            if(connectivity.available()){
                                doSend();
                            }
                        }
                    });
        }
    }
}
