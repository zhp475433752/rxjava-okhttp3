package com.hpzhang.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * RxJava和okhttp3结合使用加载网络图片
 * 观察者模式：异步 + 链式调用
 *
 * @author hpzhang
 */
public class MainActivity extends AppCompatActivity {
    Disposable disposable;
    ImageView imageView;
    Button button;
    String url = "http://b-ssl.duitang.com/uploads/item/201805/02/20180502072301_xxpfu.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImage();
            }
        });
    }

    private void loadImage() {
        // 被观察者 异步+链式编程
        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(final ObservableEmitter<Bitmap> emitter) throws Exception {
                // okhttp
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();

                // 异步调用
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d("---------", "-onFailure-" + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            byte[] bytes = response.body().bytes();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            emitter.onNext(bitmap);
                            emitter.onComplete();
                        }
                    }
                });
            }
        })
                .observeOn(AndroidSchedulers.mainThread())    // 回调在主线程
                .subscribeOn(Schedulers.io())           // 执行在io线程
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        // 设置请求网络回来的图片
                        imageView.setImageBitmap(bitmap);
                    }
                });
    }
}
