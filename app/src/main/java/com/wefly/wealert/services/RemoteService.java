package com.wefly.wealert.services;

import io.reactivex.Observable;import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface RemoteService {
    @GET("communications/api/categorie-alerte/")
    Observable<String> CategoriesList(@Header("Authorization") String jwt);
}
