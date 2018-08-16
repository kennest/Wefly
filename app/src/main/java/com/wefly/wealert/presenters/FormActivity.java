package com.wefly.wealert.presenters;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.wefly.wealert.models.Alert;
import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.utils.Constants;
import com.wefly.wealert.utils.NetworkWatcher;
import com.wefly.wealert.utils.Utils;
import com.wefly.wealert.R;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by admin on 06/06/2018.
 */

public class FormActivity extends DBActivity implements RecipientPresenter.OnRecipientDownloadCallBack, OnProgressBarListener, DialogPresenter.OnDialogListener {
    protected ArrayList<AppCompatImageButton> butList = new ArrayList<>();
    protected AppCompatImageButton bClose, bSend, bCancel;
    protected LinearLayout liMain, liLoading, liProgress;
    protected ScrollView srvMain;
    protected Alert sAlert = new Alert();
    protected int selected = 0;
    protected CopyOnWriteArrayList<Recipient> recipientsList;
    protected CopyOnWriteArrayList<Recipient> recipientsSelected = new CopyOnWriteArrayList<>();
    protected NumberProgressBar bnp;
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    protected void iniViewColors() {
        liProgress = findViewById(R.id.liLoading);
       // srvMain = findViewById(R.id.srvMain);
        butList.clear();
        butList.add(bCancel);
        butList.add(bClose);
        butList.add(bSend);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // below lollipop
            ColorStateList csl = ColorStateList.valueOf(Color.TRANSPARENT);
            for (AppCompatImageButton btn : butList) {
                btn.setSupportBackgroundTintList(csl);
            }
        }
        bnp.setOnProgressBarListener(this);
    }

    @Override
    public void onProgressChange(int current, int max) {

    }

    @Override
    public void onSaveRequest() {

    }

    @Override
    public void onDeleteRequest() {

    }

    protected void animMe(@NonNull final View v,@Nullable final Alert alert, @NonNull final DBActivity act, @NonNull final NetworkWatcher w) {
        ViewCompat.animate(v)
                .setDuration(200)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setInterpolator(new CycleInterpolator())
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(final View view) {

                    }

                    @Override
                    public void onAnimationEnd(final View view) {
                        switch (view.getId()) {

                            case R.id.btnSend:
                                 if (alert != null) {
                                    if (alert.getRecipients().size() == 0) {
                                        // No recipients
                                        showMessage(R.string.empty_recipient);
                                    } else if (alert.getContent().equals("")) {
                                        // empty content
                                        showMessage(R.string.empty_content);
                                    } else {
                                        onSelected(1, w);
                                    }
                                }
                                break;
                            default:
                                break;
                        }

                    }

                    @Override
                    public void onAnimationCancel(final View view) {

                    }
                })
                .withLayer()
                .start();
    }

    private void onSelected(int i, @NonNull NetworkWatcher wat) {
        Log.v(Constants.APP_NAME, TAG + " onSelected RUN" + i);
        selected = i;
        wat.setOnInternetListener(this);
        checkTokenAndNetwork(this, wat);
    }

    @Override
    public void onConnected() {
        super.onConnected();
        Log.v(Constants.APP_NAME, TAG + " onConnected selected" + selected + " RUN");
        switch (selected) {
            case 1:
                // ALERT
                if (sAlert != null) {
                    lockSendBtn();
                    showMessage(R.string.sending);
                    super.sendAlert(sAlert);
                } else
                    Log.v(Constants.APP_NAME, TAG + " ALERT IS NULL");
                break;
            default:
                break;
        }
        Log.v(Constants.APP_NAME, TAG + " onConnected selected" + selected + " RUN");
    }

    protected void lockSendBtn() {
        if (bSend != null) {
            bSend.setClickable(false);
        }
    }

    protected void unlockSendBtn() {
        if (bSend != null) {
            bSend.setClickable(true);
        }
    }

    protected void showMessage(@StringRes int msg) {
        if (liMain != null)
            Utils.showToast(this, msg, liMain);
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


    @Override
    public void onRecipientDownloadSucces() {

    }

    protected void counterResetAndHide() {
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                counterHide();
                                bnp.setProgress(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 800);

    }

    protected void counterHide() {
        try {
            liLoading.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void notifCounter(int p) {
        if (liLoading != null && liLoading.getVisibility() != View.VISIBLE)
            liLoading.setVisibility(View.VISIBLE);
        if (bnp != null) {
            bnp.setProgress(p);

            if (p == 0)
                counterHide();

            if (p == bnp.getMax())
                counterResetAndHide();
        }

    }

    protected void setCounterMax(int max) {
        if (bnp != null) {
            bnp.setMax(max);
        }

    }

    protected @Nullable
    NumberProgressBar getCounter() {
        return bnp;
    }

    protected void showDialog( @Nullable final Alert alert) {
        final DialogPresenter pre = new DialogPresenter();
    }

    protected class CycleInterpolator implements android.view.animation.Interpolator {

        private final float mCycles = 0.5f;

        @Override
        public float getInterpolation(final float input) {
            return (float) Math.sin(2.0f * mCycles * Math.PI * input);
        }
    }

}
