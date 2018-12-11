package org.biologer.biologer.model.network;
import android.support.annotation.NonNull;

import org.biologer.biologer.App;
import org.biologer.biologer.SettingsManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthorizationInterceptor implements Interceptor {
    private static final String TAG = AuthorizationInterceptor.class.getName();

    private static final AtomicBoolean LOGIN_LOCK = new AtomicBoolean(false);//true = login in progress

    private static final List<Request> waitingRequests = new ArrayList<>();

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + SettingsManager.getToken()).build();

        Response response = chain.proceed(request);

        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED)//unauthorized
        {
//            if (!(App.get().getCurrentActivity() instanceof LoginActivity)) {
//                User.logout(App.get().getCurrentActivity());
//                SplashActivity.clearStart(App.get().getCurrentActivity());
//            }
        }

        return response;
    }

}