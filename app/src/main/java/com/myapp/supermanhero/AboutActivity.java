package com.myapp.supermanhero;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;


import com.myapp.mylibrary.AppPreferences;

public class AboutActivity extends AppCompatActivity {

    AppPreferences appPreferences;
    private Context context;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        this.appPreferences = MLManagerApplication.getAppPreferences();
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(R.string.app_name);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(
                    UtilsUI.darker(appPreferences.getPrimaryColorPref(getResources().getColor(R.color.primary)), 0.8));
            toolbar.setBackgroundColor(appPreferences.getPrimaryColorPref(getResources().getColor(R.color.primary)));
            if (!appPreferences.getNavigationBlackPref()) {
                getWindow().setNavigationBarColor(
                        appPreferences.getPrimaryColorPref(getResources().getColor(R.color.primary)));
            }
        }
    }
}