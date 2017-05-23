package com.github.florent37.rxretrojsoup.sample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.florent37.daggerautoinject.InjectActivity;

import javax.inject.Inject;

@InjectActivity
public class MainActivity extends BaseActivity {

    @Inject
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, MainFragment.newInstance())
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println(sharedPreferences.getAll());
    }

}
