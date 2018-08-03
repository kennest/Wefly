package com.wefly.wealert.presenters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.tasks.RecipientsGetTask;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.Constants;
import com.wefly.wealert.utils.NetworkWatcher;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by admin on 15/06/2018.
 */

public class RecipientPresenter implements RecipientsGetTask.OnRecipientsDownloadCompleteListener, NetworkWatcher.OnInternetListener {
    final NumberProgressBar pCounter;
    RecipientsGetTask task;
    private FormActivity act;
    private Activity activity;
    private String stRecipients = "";
    private String nextPage = "", prevPage = "";
    private boolean hasNext;
    private boolean hasPrevious;
    private NetworkWatcher watcher;
    private int selected;
    private boolean withNext;
    private AppController appController;
    private CopyOnWriteArrayList<Recipient> list = new CopyOnWriteArrayList<>();
    private OnRecipientDownloadCallBack listener;
    private String TAG = getClass().getSimpleName();
    private int mMax;

    public RecipientPresenter(@NonNull final FormActivity act) {
        this.act = act;
        this.appController = AppController.getInstance();
        this.pCounter = act.getCounter();
    }

    public RecipientPresenter(@NonNull final Activity act) {
        this.activity = act;
        this.appController = AppController.getInstance();
        this.pCounter = new NumberProgressBar(appController.getApplicationContext());
    }

    public void downloadAllRecipients(boolean goToNext) {
        if (appController != null) {
            if (appController.isTokenValide()) {
                withNext = goToNext;
                launchOnNetworkAvai(2);
            }
        }

    }

    public CopyOnWriteArrayList<Recipient> getRecipients() {
        CopyOnWriteArrayList<Recipient> list = new CopyOnWriteArrayList<>();
        if (act != null) {
            list.addAll(act.getRecipients(act));
            if (list.size() == 0) {
                //454545
                onDownload();
            }

        }
        appController.setRecipientsList(list);
        return list;
    }

    private void onDownload() {
        if (withNext)
            downloadAllRecipients(true);
        else
            downloadAllRecipients(false);
    }

    public void setOnRecipientDownloadCallBack(@NonNull OnRecipientDownloadCallBack listener) {
        this.listener = listener;
    }

    private void notifyOnRecipientDownloadCallBack() {
        if (listener != null) {
            Log.v(Constants.APP_NAME, TAG + " notifyOnRecipientDownloadCallBack RUN");
            listener.onRecipientDownloadSucces();
        }
    }

    @Override
    public void onDownloadError(@NonNull String errorMsg) {
        // Retry
        launchOnNetworkAvai(1);
    }

    @Override
    public void onDownloadSucces(@NonNull CopyOnWriteArrayList<Recipient> recipentsArray, boolean hPrev, boolean hNext, @NonNull String prev, @NonNull String next, int max) {
        try {
            this.hasNext = hNext;
            this.hasPrevious = hPrev;
            this.nextPage = next;
            this.prevPage = prev;
            this.mMax = max;
            this.list.addAll(recipentsArray);

            // download not finish
            if (hasNext) {
                Log.v(Constants.APP_NAME, TAG + " download NOT finish RUN hasNext " + hasNext + " next = " + next + " hasprev " + hasPrevious + " prev = " + prev);
                downloadAllRecipients(true);
            } else {
                // download finish
                //Clearn old
                DataBasePresenter.getInstance().clearRecipients(act);
                //Save New
                if (appController != null) {
                    DataBasePresenter.getInstance().saveRecipientList(act, appController.recipientListToJSONArr((list)));
                }
                notifyOnRecipientDownloadCallBack();
                Log.v(Constants.APP_NAME, TAG + " download COMPLETE RUN" + hasNext + " next = " + next + " hasprev " + hasPrevious + " prev = " + prev);
            }

            // Nofity
            if (act != null) {
                act.setCounterMax(mMax);
                act.notifCounter(list.size());
            }

            Log.v(Constants.APP_NAME, TAG + " onDownloadSucces END");

        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + " onDownloadSucces SAVE FAILD");
        }
    }

    private void launchOnNetworkAvai(int sel) {
        if (act != null)
            if (act.watcher != null) {
                selected = sel;
                act.watcher.setOnInternetListener(this);
                act.watcher.isNetworkAvailableWithSilent();
            }
    }

    @Override
    public void onConnected() {
        switch (selected) {
            case 1:
                // Retry
                onDownload();
                break;
            case 2:
                // new Download
                if (withNext)
                    task = new RecipientsGetTask(act, nextPage);
                else
                    task = new RecipientsGetTask(act, Constants.RECIPIENTS_URL);
                task.setOnRecipientsDownloadCompleteListener(this);
                task.execute();
                break;
            default:
                break;
        }
        selected = 0;

    }

    @Override
    public void onNotConnected() {

    }

    @Override
    public void onRetry() {

    }

    public interface OnRecipientDownloadCallBack {
        void onRecipientDownloadSucces();
    }
}
