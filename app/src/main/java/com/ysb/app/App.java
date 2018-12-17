package com.ysb.app;

import android.app.Application;

import com.google.gson.Gson;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.squareup.leakcanary.LeakCanary;
import com.yanzhenjie.nohttp.InitializationConfig;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.OkHttpNetworkExecutor;
import com.yanzhenjie.nohttp.cache.DBCacheStore;
import com.yanzhenjie.nohttp.cookie.DBCookieStore;
import com.yuanshenbin.network.IFromJson;
import com.yuanshenbin.network.NetworkConfig;
import com.yuanshenbin.network.manager.NetworkManager;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by Jacky on 2016/10/31.
 */

public class App extends Application {
    private static Application mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        LeakCanary.install(this);
        InitializationConfig config = InitializationConfig.newBuilder(mContext)
                // 全局连接服务器超时时间，单位毫秒，默认10s。
                .connectionTimeout(30 * 1000)
                // 全局等待服务器响应超时时间，单位毫秒，默认10s。
                .readTimeout(30 * 1000)
                // 配置缓存，默认保存数据库DBCacheStore，保存到SD卡使用DiskCacheStore。
                .cacheStore(
                        // 如果不使用缓存，setEnable(false)禁用。
                        new DBCacheStore(mContext).setEnable(true)
                )
                // 配置Cookie，默认保存数据库DBCookieStore，开发者可以自己实现CookieStore接口。
                .cookieStore(
                        // 如果不维护cookie，setEnable(false)禁用。
                        new DBCookieStore(mContext).setEnable(true)
                )
                // 配置网络层，默认URLConnectionNetworkExecutor，如果想用OkHttp：OkHttpNetworkExecutor。
                .networkExecutor(new OkHttpNetworkExecutor())
                // 全局通用Header，add是添加，多次调用add不会覆盖上次add。
                // 全局通用Param，add是添加，多次调用add不会覆盖上次add。
                .retry(0) // 全局重试次数，配置后每个请求失败都会重试x次。
                .build();

        Logger.setDebug(false);

        NetworkManager.getInstance().
                InitializationConfig(new NetworkConfig.Builder()
                        .fromJson(new IFromJson() {
                            @Override
                            public <T> T onFromJson(String json, Type type) {
                                return new Gson().fromJson(json,type);
                            }

                            @Override
                            public String onToJson(Object object) {
                                return new Gson().toJson(object);
                            }

                            @Override
                            public Map<String, Object> onJsonToMap(Object param) {
                                return null;
                            }
                        })
                        .noHttpConfig(config)

                        .build());

        Logger.setDebug(true);

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag("@@")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();

        com.orhanobut.logger.Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));

    }

    public static Application getInstance() {
        return mContext;
    }
}
