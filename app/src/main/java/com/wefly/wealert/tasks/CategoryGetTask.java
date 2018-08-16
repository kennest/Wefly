package com.wefly.wealert.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.wefly.wealert.utils.AppController;
import com.wefly.wealert.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CategoryGetTask extends AsyncTask<Map<String, Integer>, Integer, Map<String, Integer>> {
    private Map<String, Integer> categories = new HashMap<>();
    private String response="";
    private String next="";
    Integer count=0;
    private AppController appController;


    public CategoryGetTask(AppController appController) {
        this.appController = appController;
    }

    @Override
    protected Map<String, Integer> doInBackground(Map<String, Integer>... maps) {
        JSONObject resultJSON;
        Map<String, Integer> map1 = new HashMap<>();
        Map<String, Integer> map2 = new HashMap<>();
        try {
            response = getCategoryFromUrl(Constants.ALERT_CATEGORY_URL);
            resultJSON = new JSONObject(response);
            count = Integer.parseInt(resultJSON.getString("count"));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (count > 0)
            map1.putAll(extractCategory(response));
            categories.putAll(map1);

        //Si on a encore des pages suivantes on lance de nouveau la requete
        try {
            int i = 1;
            resultJSON = new JSONObject(response);
            count = Integer.parseInt(resultJSON.getString("count"));
            next = resultJSON.getString("next");
            if (count > 0 && !next.equals("null"))
                do {
                    map2.putAll(extractCategory(getCategoryFromUrl(next)));
                } while (++i == (count - 1));
            categories.putAll(map2);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return categories;
    }

    private String getCategoryFromUrl(@NonNull String url) throws IOException {

        OkHttpClient client = new OkHttpClient();
        String result;

        Request request = new Request.Builder().url(url)
                .addHeader("Authorization", Constants.TOKEN_HEADER_NAME + appController.getToken())
                .build();
        Response response = client.newCall(request).execute();

        result = response.body().string();
        return result;
    }


    //Fonction d'extraction des categories du flux JSON
    private Map<String, Integer> extractCategory(@NonNull String json) {
        Map<String, Integer> extractedCategories = new HashMap<>();
        try {
            JSONObject response_data = new JSONObject(json);
            JSONArray response_category = new JSONArray(response_data.getString("results"));

            for (int i = 0; i < response_category.length(); i++) {
                JSONObject obj = new JSONObject(response_category.getString(i));
                extractedCategories.put(obj.getString("nom"), obj.getInt("id"));
            }

            Log.v("CATEGORY LIST:", response_category.toString());

            for (Map.Entry entry : extractedCategories.entrySet()) {
                Log.v("Entry " + entry.toString(), " key" + entry.getKey() + " Value" + entry.getValue());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return extractedCategories;
    }
}
