package com.wefly.wealert.tracking;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.wefly.wealert.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by root on 12/2/17.
 */

public class SendRepportsTask extends AsyncTask<Void, Integer, Void> {
     String mrepport;
     Context mcontext;
    String response = null;
    public SendRepportsTask(String repport, Context context) {
      this.mrepport = repport;
      this.mcontext = context;
    }

    // {@inheritDoc}
    //
    @Override
    protected void onPreExecute() {

    }


    @Override
    protected Void doInBackground(Void... voids) {

        try {
            SendRepportUtilities report = new SendRepportUtilities();
            String result = report.getResponseFromHttpUrl(mrepport,"http://217.182.133.143:8000/geolocation/get-user-position/");
            Log.e("report", result);
            try {
                JSONObject jsonresult = new JSONObject(result);
                response = jsonresult.getString("reponse");
                Log.e("reponse", response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if("donnée recu avec succès !".equals(response)){
                File rf = new File(
                        "/data/data/com.wefly.wealert/shared_prefs/repportFile.xml");
                rf.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostExecute(final Void result) {
        sendNotification();
    }

    public void sendNotification() {

        //Get an instance of NotificationManager//

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mcontext)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle("Wefly locate")
                        .setContentText(response);


        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager = (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//

//        NotificationManager.notify().
                mNotificationManager.notify(001, mBuilder.build());
    }
}

