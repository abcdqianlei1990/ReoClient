package com.ufclient.cn.sample;

import java.util.Observable;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by channey on 2018/5/8.
 */

public interface ApiManager {
    @GET("weather/json.shtml")
    rx.Observable<Bean> getWeather(@Query("city") String city);
}
