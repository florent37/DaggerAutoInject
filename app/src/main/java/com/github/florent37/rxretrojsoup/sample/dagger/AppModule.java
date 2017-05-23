package com.github.florent37.rxretrojsoup.sample.dagger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
@Singleton
public class AppModule {

    @Provides
    @Singleton
    public SharedPreferences providesSharedPreferences(Application application){
        return application.getSharedPreferences("user", Context.MODE_PRIVATE);
    }

}
