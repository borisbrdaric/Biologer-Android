package org.biologer.biologer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.biologer.biologer.RetrofitService;
import org.biologer.biologer.model.network.AuthorizationInterceptor;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static OkHttpClient client = null;

    private static Retrofit getClient(String base_url) {
        if (retrofit == null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthorizationInterceptor())
                    .cache(null)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(base_url)
                    .client(client)
                    .addConverterFactory(JacksonConverterFactory.create(mapper))
                    .build();
        }
        return retrofit;
    }

    public static RetrofitService getService(String base_url) {
        retrofit = getClient(base_url);
        RetrofitService service;
        service = retrofit.create(RetrofitService .class);
        return service;
    }
}