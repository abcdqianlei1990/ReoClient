package com.reoclient.cn.client;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.reoclient.cn.convert.NoBodyConvertFactory;
import com.reoclient.cn.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by channey on 2018/5/4.
 */

public class ReoClient {
    private static final String TAG = "ReoClient";
    private static final String CACHE_FILE_NAME = "reoClientCache";
    private static final int CACHE_MAX_AGE = 1 * 24 * 3600 * 1000;  //1天
    private Context mContext;
    private List<String> mBaseUrl;
    private Map<String,String> mHeaders;
    private long DEFAULT_TIMEOUT = 10 * 60;
    private long mTimeout = DEFAULT_TIMEOUT;
    private List<Interceptor> mInterceptors;
    private List<Interceptor> mNetworkInterceptors;
    private boolean mCacheable = false;
    private boolean mOpenLog = false;   //debug模式下为true
    //https域名
    private static List<String> mTrustHostList = new ArrayList<String>();
    private Map<String,Retrofit> mRetrofits = new HashMap<>();
    private OkHttpClient mClient;

    private final Interceptor REWRITE_RESPONSE_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response originalResponse = chain.proceed(request);
            Response response = null;
            String cacheControl = originalResponse.header("Cache-Control");
            if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                    cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) {
                Response.Builder newBuilder = originalResponse.newBuilder();
                if(mCacheable){
                    newBuilder.header("Cache-Control", "public, max-age=" + CACHE_MAX_AGE)
                            .removeHeader("Pragma");
                }
                Response res = newBuilder.build();
                response = res;
            } else {
                response =  originalResponse;
            }
            return response;
        }
    };

    private final Interceptor CACHE_MODE_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (Util.isNetworkAvailable(mContext)) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .removeHeader("Pragma")
                        .build();
            }else {
                request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached")
                        .removeHeader("Pragma")
                        .build();
            }
            Response response = chain.proceed(request);
            return response;
        }
    };

    private Interceptor headerInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Request.Builder builder = request.newBuilder();
            Response response;
            addHeaders(builder);

            request = builder.build();
            response = chain.proceed(request);
//            HttpLoggingInterceptor.Logger.DEFAULT.log(sb.toString());
            return response;
        }
    };

    /**
     * 添加公共请求头
     * @param builder
     */
    private void addHeaders(Request.Builder builder){
        if (mHeaders != null){
            for (Map.Entry<String,String> entry:mHeaders.entrySet()){
                String key = entry.getKey();
                String value = entry.getValue();
                builder.addHeader(key, value);
                if(mOpenLog) Log.d(TAG,"-------- header:"+key+"#"+value+" --------");
            }
        }
    }

    private void initReoClient(Context context){
        initOkHttpClient(context);
        for (String url:mBaseUrl){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(NoBodyConvertFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(mClient)
                    .build();
            mRetrofits.put(url,retrofit);
        }
    }

    private void initOkHttpClient(Context context){
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        if(mOpenLog){
            // log interceptor
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(httpLoggingInterceptor);
        }

        if (mCacheable){
            File cacheFile = new File(context.getCacheDir(),CACHE_FILE_NAME);
            Cache cache = new Cache(cacheFile, 1024 * 1024 * 10); //10Mb
            builder.cache(cache);
            builder.addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR);
            builder.addInterceptor(CACHE_MODE_INTERCEPTOR);
        }
        builder.retryOnConnectionFailure(true)
                .connectTimeout(mTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(mTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(mTimeout, TimeUnit.MILLISECONDS)
                .addInterceptor(headerInterceptor);
//                .addInterceptor(REWRITE_REQUEST_INTERCEPTOR);
        if (mInterceptors != null){
            for (Interceptor i:mInterceptors){
                builder.addInterceptor(i);
            }
        }
        if (mNetworkInterceptors != null){
            for (Interceptor i:mNetworkInterceptors){
                builder.addNetworkInterceptor(i);
            }
        }
        initTrustHosts(builder);
        mClient = builder.build();
    }

    private void initTrustHosts(OkHttpClient.Builder builder){
        //默认信任base url域名
        if (mBaseUrl == null){
            throw new IllegalArgumentException("base url should not be null.");
        }
        if (mTrustHostList.size() == 0){
            try {
                for (String s:mBaseUrl){
                    URL url = new URL(s);
                    String host = url.getHost();
                    mTrustHostList.add(host);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        addTrustHosts(builder,mTrustHostList);
    }

    private OkHttpClient.Builder addTrustHosts(OkHttpClient.Builder builder,@NonNull final List<String> list){
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
//                for (String s:list){
//                    Log.d(TAG,"trust host:"+s);
//                }
//                Log.d(TAG,"current hostname:"+hostname);
                return list.contains(hostname);
            }
        });
        return builder;
    }

    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override public MediaType contentType() {
                return body.contentType();
            }

            @Override public long contentLength() {
                return -1; // 无法知道压缩后的数据大小
            }

            @Override public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }

    public void setContext(Context context){
        this.mContext = context;
    }

    public void setBaseUrl(List<String> url){
        this.mBaseUrl = url;
    }

    public void setHeaders(Map<String,String> headers){
        this.mHeaders = headers;
    }

    public void setTimeout(long timeout){
        this.mTimeout = timeout;
    }

    public void setInterceptors(List<Interceptor> interceptors){
        this.mInterceptors = interceptors;
    }

    public void setNetworkInterceptors(List<Interceptor> interceptors){
        this.mNetworkInterceptors = interceptors;
    }

    /**
     * create customer api manager with baseUrl
     * @param cls class of your customer api manager
     * @param baseUrl
     * @param <T> api manager instance
     * @return
     */
    public <T>T createApiManager(Class<T> cls,String baseUrl){
        Retrofit retrofit = mRetrofits.get(baseUrl);
        if (retrofit == null) throw new NullPointerException("can't find client with this url.create api manager can only be used in building modle.");
        return retrofit.create(cls);
    }

    public void openLog(boolean open){
        this.mOpenLog = open;
    }

    public void setTrustHost(List<String> list){
        if (list != null){
            mTrustHostList.clear();
            mTrustHostList.addAll(list);
        }
    }

    public void setCacheable(boolean cacheable){
        this.mCacheable = cacheable;
    }

    public static class Builder{
        private final ReoClient.Params P;
        private final Context mContext;

        public Builder(Context context) {
            this.mContext = context;
            P = new ReoClient.Params(context);
            P.mOpenLog = Util.isDebug(mContext);
        }

        public Builder setBaseUrl(List<String> url){
            P.mBaseUrl = url;
            return this;
        }

        public Builder setHeaders(Map<String,String> headers){
            P.mHeaders = headers;
            return this;
        }

        public Builder setTimeout(long timeout){
            P.mTimeout = timeout;
            return this;
        }

        public Builder setInterceptors(List<Interceptor> interceptors){
            P.mInterceptors = interceptors;
            return this;
        }

        public Builder setNetworkInterceptors(List<Interceptor> interceptors){
            P.mNetworkInterceptors = interceptors;
            return this;
        }

        public Builder setTrustHost(List<String> list){
            P.mTrustHostList = list;
            return this;
        }

        public Builder setCacheable(boolean cacheable){
            P.mCacheable = cacheable;
            return this;
        }

        public Builder openLog(boolean open){
            P.mOpenLog = open;
            return this;
        }

        public ReoClient create(){
            ReoClient client = new ReoClient();
            client.setContext(P.mContext);
            client.setBaseUrl(P.mBaseUrl);
            client.setHeaders(P.mHeaders);
            client.setTimeout(P.mTimeout);
            client.setInterceptors(P.mInterceptors);
            client.setNetworkInterceptors(P.mNetworkInterceptors);
            client.setTrustHost(P.mTrustHostList);
            client.setCacheable(P.mCacheable);
            client.openLog(P.mOpenLog);
            client.initReoClient(P.mContext);
            return client;
        }
    }

    private static class Params {
        private Context mContext;
        private List<String> mBaseUrl;
        private Map<String,String> mHeaders;
        private long mTimeout;
        private List<Interceptor> mInterceptors;
        private List<Interceptor> mNetworkInterceptors;
        private List<String> mTrustHostList;
        private boolean mCacheable;
        private boolean mOpenLog;

        public Params(Context mContext) {
            this.mContext = mContext;
        }
    }
}
