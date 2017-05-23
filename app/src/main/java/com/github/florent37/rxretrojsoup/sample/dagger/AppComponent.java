package com.github.florent37.rxretrojsoup.sample.dagger;

import android.app.Application;

import com.github.florent37.daggerautoinject.ActivityModule;
import com.github.florent37.daggerautoinject.FragmentModule;
import com.github.florent37.rxretrojsoup.sample.MainApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = {
        AppModule.class,

        AndroidInjectionModule.class,
        AndroidSupportInjectionModule.class,

        ActivityModule.class,
        FragmentModule.class,
})
public interface AppComponent {
    void inject(MainApplication application);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        AppComponent build();
    }
}
