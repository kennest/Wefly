package com.wefly.wealert.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wefly.wealert.models.Alert;
import com.wefly.wealert.models.Recipient;
import com.wefly.wealert.tasks.AlertPostItemTask;
import com.wefly.wealert.tasks.PieceUploadTask;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by admin on 27/03/2018.
 */

public class DBActivity extends BaseActivity implements  AlertPostItemTask.OnAlertSendListener {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBasePresenter.init(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            DataBasePresenter.getInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    protected void onDiplayParcelleDetails(@NonNull Activity act, @NonNull final Parcelle p){
//
//        Intent formIntent = new Intent(act, ParcellesActivity.class);
//        formIntent.putExtra("showDetail", true);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("parcelObj", p);
//        formIntent.putExtras(bundle);
//        startActivity(formIntent);
//        AppController.clearDestroyList();
//    }

    //Recupere la liste des destinataires dans la DB Sqlite
    protected @NonNull
    CopyOnWriteArrayList<Recipient> getRecipients(final Context ctx) {
        CopyOnWriteArrayList<Recipient> list = new CopyOnWriteArrayList<>();
        try {
            list.addAll(DataBasePresenter.getInstance().getRecipients(ctx));
        } catch (Exception e) {
            e.printStackTrace();
        }
        appController.setRecipientsList(list);
        return list;
    }


    //Envoi une alert
    protected void sendAlert(@NonNull Alert alert) {
        Log.v(TAG + "send alert", "RUN");
        AlertPostItemTask task = new AlertPostItemTask(alert);
        task.setOnAlertSendListener(this);
        task.execute();
    }


    @Override
    public void onSendError(@NonNull Alert e) {

    }

    @Override
    public void onSendSucces(@NonNull Alert e) {

    }
}
