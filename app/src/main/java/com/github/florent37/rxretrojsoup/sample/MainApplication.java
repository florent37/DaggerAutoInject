package com.github.florent37.rxretrojsoup.sample;

import android.app.Activity;
import android.app.Application;

import com.github.florent37.daggerautoinject.DaggerAutoInject;
import com.github.florent37.rxretrojsoup.sample.dagger.AppComponent;

import com.github.florent37.daggerautoinject.InjectApplication;
import com.github.florent37.rxretrojsoup.sample.dagger.DaggerAppComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

@InjectApplication(component = AppComponent.class)
public class MainApplication extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();

        final AppComponent appComponent = DaggerAppComponent.builder()
                .application(this)
                .build();

        DaggerAutoInject.init(this, appComponent);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityAndroidInjector;
    }
}
