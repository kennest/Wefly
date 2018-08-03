package com.wefly.wealert.tasks;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.wefly.wealert.presenters.BaseActivity;
import com.wefly.wealert.presenters.TaskPresenter;
import com.wefly.wealert.utils.Constants;
import com.wefly.wealert.utils.Save;
import com.wefly.wealert.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import pl.tajchert.waitingdots.DotsTextView;

/**
 * Created by admin on 02/04/2018.
 */

public class LoginTask extends TaskPresenter {
    private String response = "";
    private OnLoginListener listener;
    private View v;
    private JSONObject obj;
    private String uName, uPword;
    private BaseActivity act;
    private DotsTextView dotsTView;
    private String token = "";
    private boolean isNetworkError;

    private String TAG = getClass().getSimpleName();

    public LoginTask(@NonNull String uName, @NonNull String pWord, @NonNull final BaseActivity act) {
        this.uName = uName;
        this.uPword = pWord;
        this.act = act;
        obj = new JSONObject();
        isNetworkError = false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        try {
            dotsTView = act.findViewById(R.id.dots);
            dotsTView.showAndPlay();
//            obj.put("username", "koffi@weflyagri.com");
//            obj.put("password", "emp1pass");

            obj.put("username", uName);
            obj.put("password", uPword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Fonction d'envoi des donnees de connexion
    @Override
    protected Boolean doInBackground(Void... voids) {
        LoginNetworkUtilities util = new LoginNetworkUtilities();
        try {
            response = util.getResponseFromHttpUrl(obj, Constants.LOGIN_URL);
            Log.v(Constants.APP_NAME, TAG + " response " + response);
            if (!response.trim().equals("") && !response.trim().equals(Constants.SERVER_ERROR) && !response.trim().contains(Constants.RESPONSE_ERROR_HTML)) {
                // post update
                return !response.trim().contains(Constants.RESPONSE_EMPTY_INPUT);
            } else
                isNetworkError = true;

        } catch (Exception e) {
            e.printStackTrace();
            Log.v(Constants.APP_NAME, TAG + "doInBackground Error ");
        }
        return false;

    }


    //Fonction de traitement apres envoi des donnees de connxions
    @Override
    protected void onPostExecute(Boolean isOk) {
        super.onPostExecute(isOk);
        try {
            dotsTView.hideAndStop();
            if (isOk) {
                token = new JSONObject(response)
                        .getString("token");
                Save.defaultSaveString(Constants.PREF_TOKEN, token, act);
                Save.defaultSaveString(Constants.PREF_USER_NAME, uName, act);
                Save.defaultSaveString(Constants.PREF_USER_PASSWORD, uPword, act);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyOnLoginListener(isOk);
    }

    public void setOnLoginListener(@NonNull OnLoginListener listener, @NonNull View view) {
        this.listener = listener;
        this.v = view;
    }

    private void notifyOnLoginListener(boolean isDone) {
        if (listener != null && v != null) {
            try {
                if (isNetworkError)
                    listener.onLoginNetworkError();
                else {
                    if (isDone)
                        listener.onLoginSucces();
                    else
                        listener.onLoginError(v);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public interface OnLoginListener {
        void onLoginError(@NonNull View view);

        void onLoginNetworkError();

        void onLoginSucces();
    }

    public final class LoginNetworkUtilities {
        public String getResponseFromHttpUrl(@NonNull JSONObject jsonParam, @Nullable String url) {

            HttpClient httpclient;
            HttpPost httppost = new HttpPost(url);
            HttpResponse response;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.VOLLEY_TIME_OUT);
            HttpConnectionParams.setSoTimeout(httpParameters, Constants.VOLLEY_TIME_OUT);
            httpclient = new DefaultHttpClient(httpParameters);
            Log.v(Constants.APP_NAME, TAG + "  doInBackGround url " + url + " Obj " + jsonParam.toString());

            try {
                httppost.setEntity(new StringEntity(jsonParam.toString(), "UTF-8"));
                httppost.setHeader("Content-type", "application/json");
                response = httpclient.execute(httppost);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }
                Log.v(Constants.APP_NAME, TAG + "  doInBackGround parcelJson.toString response ");
                return builder.toString();
            } catch (UnsupportedEncodingException e) {
                Log.v(Constants.APP_NAME, TAG + "   doInBackGround UnsupportedEncodingException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.v(Constants.APP_NAME, TAG + "   doInBackGround IOException");
                e.printStackTrace();
            }
            return Constants.SERVER_ERROR;
        }
    }

}
