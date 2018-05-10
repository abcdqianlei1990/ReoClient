package com.channey.cn.sample;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by channey on 2018/5/8.
 */

public interface ApiManager {
    @GET("open/api/weather/json.shtml")
    rx.Observable<Bean> getWeather(@Query("city") String city);
}
