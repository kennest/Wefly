package com.wefly.wealert.services;

import com.wefly.wealert.services.models.AlertResponse;
import com.wefly.wealert.services.models.EmployeId;

import org.json.JSONObject;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface APIService {

    @GET("communications/api/alertes/")
    Observable<AlertResponse> AlertSentList(@Header("Authorization") String jwt);

    @GET("communications/api/alerte-receive-status/")
    Observable<AlertResponse> AlertReceiveList(@Header("Authorization") String jwt);

    @GET("communications/api/liste-employes/{id}/getcurrent/")
    Observable<EmployeId> CurrentEmployeId(@Header("Authorization") String jwt, @Path("id") String id);

}
