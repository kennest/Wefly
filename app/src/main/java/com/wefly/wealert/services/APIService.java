package com.wefly.wealert.services;

import com.wefly.wealert.services.models.AlertResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface APIService {
    @GET("communications/api/alertes/")
    Observable<AlertResponse> AlertSentList(@Header("Authorization") String jwt);

    @GET("communications/api/alerte-receive-status/")
    Observable<AlertResponse> AlertReceiveList(@Header("Authorization") String jwt);
}
