package com.channey.cn.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.reoclient.cn.client.ReoClient;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private Button btn;
    private ApiManager manager1;
    private ApiManager manager2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        btn = (Button) findViewById(R.id.btn);

        initClient();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeather();
            }
        });
    }

    public void initClient(){
        String url1 = "https://www.sojson.com/";
        String url2 = "http://v.juhe.cn/";
        List<String> urlList = new ArrayList<>();
        urlList.add(url1);
        urlList.add(url2);

        ReoClient.Builder builder = new ReoClient.Builder(this);
        ReoClient client = builder.setBaseUrl(urlList)
                .setCacheable(true)
                .openLog(true) //默认debug模式下开启日志
                .create();
        manager1 = client.createApiManager(ApiManager.class,url1);
        manager2 = client.createApiManager(ApiManager.class,url2);
    }


    public void getWeather(){
        Observable<Bean> observable = manager1.getWeather("北京");
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Bean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(Bean bean) {
                tv.setText(bean.toString());
            }
        });
    }
}
