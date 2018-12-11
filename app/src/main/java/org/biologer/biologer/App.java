package org.biologer.biologer;

import android.app.Application;
import android.content.SharedPreferences;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.biologer.biologer.model.DaoMaster;
import org.biologer.biologer.model.DaoSession;
import org.biologer.biologer.model.network.AuthorizationInterceptor;
import org.greenrobot.greendao.database.Database;


import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by brjovanovic on 12/24/2017.
 */

public class App extends Application {

    private static App app;
    private RetrofitService service;
    private Retrofit retrofit;
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = this.getSharedPreferences("My_Shared_Preference_Name",MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        app = this;
        /*odavde*/
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(new Interceptor()
//                {
//                    @Override
//                    public Response intercept(@NonNull Chain chain) throws IOException
//                    {
//                        Request original = chain.request();
//
//                        Request request = original.newBuilder()
//                                .header("lang", "en")
//                                .method(original.method(), original.body())
//                                .build();
//
//                        Response response = chain.proceed(request);
//                        // Do anything with response here
//                        //TODO for auth and stuff
//                        return response;
//                    }
//                }).build();

        //super.onCreate();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        OkHttpClient client = new OkHttpClient.Builder().
                addInterceptor(new AuthorizationInterceptor()).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(SettingsManager.getDatabaseName())
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();

        service = retrofit.create(RetrofitService.class);

        //za GreenDAO bazu, obavezno
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();

    }

    // Take a look if preferences change and restart this part of the app if so...
    // This is a workaround for .baseUrl not being reloaded after change
    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("DATABASE_NAME")){

            }
        }
    };

    public static App get(){
        return app;
    }

    public RetrofitService getService() {
        return service;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public DaoSession getDaoSession() {return daoSession;}
}
