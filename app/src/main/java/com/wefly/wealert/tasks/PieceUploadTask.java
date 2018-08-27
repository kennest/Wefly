package com.wefly.wealert.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mikhaellopez.circularfillableloaders.CircularFillableLoaders;
import com.wefly.wealert.events.BeforeUploadEvent;
import com.wefly.wealert.events.JsonExceptionEvent;
import com.wefly.wealert.events.UploadDoneEvent;
import com.wefly.wealert.models.Alert;
import com.wefly.wealert.models.Piece;
import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.Constants;
import com.wefly.wealert.utils.EncodeBase64;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PieceUploadTask extends AsyncTask<String, Integer, String> {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    final Alert alert;
    private Set<Piece> pieces;
    private AppController appController;
    private String prefresponse;
    private OnPieceSendListener listener;

    CircularProgressIndicator bar;

    public void setProgressBar(CircularProgressIndicator bar) {
        this.bar = bar;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (this.bar != null) {
            Log.e(getClass().getSimpleName(), "Progress" + String.valueOf(values[0]));
            bar.setCurrentProgress(values[0]);
        }
    }

    public PieceUploadTask(@NonNull Set<Piece> pieces, @Nullable Alert alert) {
        this.alert = alert;
        this.pieces = pieces;
        appController = AppController.getInstance();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        EventBus.getDefault().post(new BeforeUploadEvent("Uploading..."));
        SharedPreferences sp = appController.getSharedPreferences("sent_data", 0);
        prefresponse = sp.getString("sent_response", "NO DATA IN PREFS");
        Log.v("PREFS SENT DATA", prefresponse);
    }

    @Override
    protected String doInBackground(String... strings) {
        PieceUploadNetworkUtilities util = new PieceUploadNetworkUtilities();
        String result = "";
        try {
            if (pieces.size() > 0)
                for (Piece p : pieces) {
                    int progess = 1;
                    Thread.sleep(1000);
                    publishProgress(progess+1);
                    result = util.uploadPiece(p, "PJ_" + System.nanoTime(), Constants.SEND_FILE_URL);
                    Log.v(getClass().getSimpleName(),"AlertDataPiece progress"+progess);
                }
        } catch (IOException | InterruptedException e) {
            EventBus.getDefault().post(new JsonExceptionEvent("Error:"+e.getMessage()));
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        EventBus.getDefault().post(new UploadDoneEvent("Finished!"));
        bar.setCurrentProgress(0);
        setProgressBar(null);
        notifyOnPieceSendListener(true);
    }

    private final class PieceUploadNetworkUtilities {
        private String uploadPiece(@Nullable Piece piece, @Nullable String pieceName, @Nullable String url) throws IOException {
            Response response;
            Integer id = 0;
            try {
                JSONObject sent_response = new JSONObject(prefresponse);
                id = sent_response.getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String result = "";

            String encodedPiece = "";
            try {
                encodedPiece = new EncodeBase64().encode(piece.getUrl());
                OkHttpClient client = new OkHttpClient();
                JSONObject dataJson = new JSONObject();
                //Init Json data
                dataJson.put("piece_name", pieceName + piece.getExtension(piece.getUrl()));
                dataJson.put("piece_b64", encodedPiece.trim());
                if (alert != null) {
                    dataJson.put("alerte", id);
                    dataJson.put("email", null);
                }

                Log.v("PieceUploadTask params", dataJson.toString());
                Log.v("AlertDataPiece path", piece.getExtension(piece.getUrl()));

                //Build request body
                RequestBody body = RequestBody.create(JSON, dataJson.toString());

                Request request = new Request.Builder().url(url)
                        .addHeader("Authorization", Constants.TOKEN_HEADER_NAME + appController.getToken())
                        .post(body)
                        .build();
                //Execute
                response = client.newCall(request).execute();
                result = response.body().string();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            } catch (JSONException e) {
                EventBus.getDefault().post(new JsonExceptionEvent("Error:"+e.getMessage()));
                notifyOnPieceSendListener(false);
                e.printStackTrace();
            }
            Log.v("AlertDataPiece Upload Result", result);
            return result;
        }

    }


    //DECLARE LISTENER
    public interface OnPieceSendListener {
        void onUploadError();

        void onUploadSucces();
    }

    public void setOnPieceSendListener(@NonNull OnPieceSendListener listener) {
        this.listener = listener;
    }

    private void notifyOnPieceSendListener(boolean isDone) {
        if (listener != null) {
            try {
                if (isDone)
                    listener.onUploadSucces();
                else
                    listener.onUploadError();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
