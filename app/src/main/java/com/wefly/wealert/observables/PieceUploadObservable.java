package com.wefly.wealert.observables;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wefly.wealert.events.JsonExceptionEvent;
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

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PieceUploadObservable {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private AppController appController=AppController.getInstance();
    private String prefresponse;

    public Observable<Boolean> upload(Set<Piece> pieces,Alert alert){
        return Observable.fromCallable(()-> process(pieces,alert));
    }

    private Boolean process(Set<Piece>pieces,Alert alert){
        try {
            SharedPreferences sp = appController.getSharedPreferences("sent_data", 0);
            prefresponse = sp.getString("sent_response", "NO DATA IN PREFS");
            if (pieces.size() > 0)
                for (Piece p : pieces) {
                    uploadPiece(p, "PJ_" + System.nanoTime(), Constants.SEND_FILE_URL,alert);
                }
        } catch (IOException e) {
            EventBus.getDefault().post(new JsonExceptionEvent("Error:"+e.getMessage()));
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    private String uploadPiece(@Nullable Piece piece, @Nullable String pieceName, @Nullable String url, @NonNull Alert alert) throws IOException {
        Response response;
        Integer id = 0;
        Log.v("PREFS SENT DATA", prefresponse);
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
            Log.v("Piece path", piece.getExtension(piece.getUrl()));

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
            e.printStackTrace();
        }
        Log.v("Piece Upload Result", result);
        return result;
    }
}
