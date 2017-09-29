package com.example.medicalofficial.model.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/9/28.
 */

public class OkHttpUtils<T> {

    public final int CONNECT_TIMEOUT = 60;
    public final int READ_TIMEOUT = 100;
    public final int WRITE_TIMEOUT = 60;
    private BaseCallBack callBack;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    callBack.success(msg.obj);
                    break;
                case 1:
                    callBack.err(500, (String) msg.obj);
                    break;
            }
        }
    };

    //实现https的类
    private static SSLSocketFactory getSSLSocketFactory(Context context,String name) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        //CertificateFactory用来证书生成
        CertificateFactory certificateFactory;
        InputStream inputStream = null;
        Certificate certificate;
        try {
            inputStream = context.getResources().getAssets().open(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
            certificate = certificateFactory.generateCertificate(inputStream);

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry(name,certificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
        }
        return null;
    }

    public <T> void getLoadNet(String url, BaseCallBack callBack, final Class<T> tClass) {
        this.callBack = callBack;
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = e.getMessage().toString();
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Gson gson = new Gson();
                T t = gson.fromJson(string, tClass);
                Message msg = Message.obtain();
                msg.what = 0;
                msg.obj = t;
                handler.sendMessage(msg);
            }
        });
    }

    public <T> void postLoadNet(String url, Map<String, Object> map, BaseCallBack callBack, final Class<T> tClass) {
        this.callBack = callBack;
        RequestBody requestBody;
        if (map == null) {
            map = new HashMap<>();
        }
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
                .build();
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, Object> map1 : map.entrySet()) {
            String key = map1.getKey();
            String value;
            if (map1.getValue() == null) {
                value = "";
            } else {
                value = (String) map1.getValue();
            }
            builder.add(key, value);
        }
        requestBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        //new call
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = e.getMessage().toString();
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Gson gson = new Gson();
                T t = gson.fromJson(string, tClass);
                Message msg = Message.obtain();
                msg.what = 0;
                msg.obj = t;
                handler.sendMessage(msg);
            }
        });
    }
}
