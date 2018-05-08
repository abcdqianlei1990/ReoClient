package com.ufclient.cn.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.reoclient.cn.client.UfClient;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private Button btn;
    private ApiManager manager;
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
        UfClient.Builder builder = new UfClient.Builder(this);
        builder.setBaseUrl("https://www.sojson.com/open/api/")
                .setCacheable(true)
                .openLog(true); //默认debug模式下开启日志
        UfClient client = builder.create();
        manager = client.createApiManager(ApiManager.class);
    }


    public void getWeather(){
        Observable<Bean> observable = manager.getWeather("北京");
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
