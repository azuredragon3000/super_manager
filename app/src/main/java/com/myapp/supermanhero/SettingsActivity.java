package com.myapp.supermanhero;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.myapp.mylibrary.AppPreferences;
import com.myapp.mylibrary.DirectoryChooserConfig;
import com.myapp.mylibrary.DirectoryChooserFragment;
import com.myapp.mylibrary.DirectoryChooserActivity;
import com.myapp.mylibrary.UtilsApp;
import com.myapp.mylibrary.widget.CustomPreference;


public class SettingsActivity extends AppCompatActivity implements
        DirectoryChooserFragment.OnFragmentInteractionListener{

    protected Context context;
    protected Toolbar toolbar;
    protected AppPreferences appPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        this.appPreferences = MLManagerApplication.getAppPreferences();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        this.context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //TODO Toolbar should load the default style in XML (white title and back arrow), but doesn't happen
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setInitialConfiguration();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //overridePendingTransition(R.anim.fade_forward,R.anim.slide_out_right);
        startActivity(new Intent(this,MainActivity.class));
    }

    private void setInitialConfiguration() {
        toolbar.setTitle(getResources().getString(R.string.action_setting));

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

    @Override
    public void onSelectDirectory(@NonNull String path) {
        //final Intent intent = new Intent();
        //intent.putExtra(RESULT_SELECTED_DIR, path);
        //setResult(RESULT_CODE_DIR_SELECTED, intent);
        //finish();
    }

    @Override
    public void onCancelChooser() {
        //finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat  implements SharedPreferences.OnSharedPreferenceChangeListener,
            DirectoryChooserFragment.OnFragmentInteractionListener{

       // protected Toolbar toolbar;
        protected AppPreferences appPreferences;
        protected Preference prefVersion,prefLicense, prefDeleteAll, prefDefaultValues, prefNavigationBlack,prefCustomPath;
        protected CustomPreference prefPrimaryColor,prefFABColor;
        protected ListPreference prefCustomFilename, prefSortMode;
        protected DirectoryChooserFragment chooseDialog;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            this.appPreferences = MLManagerApplication.getAppPreferences();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.registerOnSharedPreferenceChangeListener(this);

            prefVersion = findPreference("prefVersion");
            prefLicense = findPreference("prefLicense");
            prefPrimaryColor = (CustomPreference) findPreference("prefPrimaryColor");
            prefFABColor =  (CustomPreference) findPreference("prefFABColor");
            prefDeleteAll = findPreference("prefDeleteAll");
            prefDefaultValues = findPreference("prefDefaultValues");
            prefNavigationBlack = findPreference("prefNavigationBlack");
            prefCustomFilename = (ListPreference) findPreference("prefCustomFilename");
            prefSortMode = (ListPreference)  findPreference("prefSortMode");
            prefCustomPath = findPreference("prefCustomPath");

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                prefPrimaryColor.setEnabled(false);;
                prefNavigationBlack.setEnabled(false);
                prefNavigationBlack.setDefaultValue(true);
            }

            String versionName = UtilsApp.getAppVersionName(getContext());
            int versionCode = UtilsApp.getAppVersionCode(getContext());

            prefVersion.setTitle(getResources().getString(R.string.app_name)+"v"+versionName+"("+versionCode+")");


            prefVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getContext(),AboutActivity.class));
                    getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.fade_back);
                    return false;
                }
            });

            setCustomFilenameSummary();

            setSortModeSummary();

            setCustomPathSummary();

            prefDeleteAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    prefDeleteAll.setSummary(R.string.deleting);
                    prefDeleteAll.setEnabled(false);
                    Boolean deleteAll = UtilsApp.deleteAppFiles(appPreferences);
                    if(deleteAll){
                        prefDeleteAll.setSummary(R.string.deleting_done);
                    }else{
                        prefDeleteAll.setSummary(R.string.deleting_error);
                    }
                    prefDeleteAll.setEnabled(true);
                    return false;
                }
            });

            prefCustomPath.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final DirectoryChooserConfig chooserConfig = DirectoryChooserConfig.builder()
                            .newDirectoryName("MLManagerAPKs")
                            .allowReadOnlyDirectory(false)
                            .allowNewDirectoryNameModification(true)
                            .initialDirectory(appPreferences.getCustomPath(UtilsApp.getDefaultAppFolder().getPath()))
                            .build();
                    chooseDialog = DirectoryChooserFragment.newInstance(chooserConfig);
                    chooseDialog.show(getActivity().getFragmentManager(),null);
                    return false;
                }
            });
        }



        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);
            if(pref == prefCustomFilename){
                setCustomFilenameSummary();
            }else if(pref == prefSortMode){
                setSortModeSummary();
            }else if(pref == prefCustomPath){
                setCustomPathSummary();
            }
        }

        private void setSortModeSummary() {
            int sortValue = Integer.valueOf(appPreferences.getSortMode())-1;
            prefSortMode.setSummary(getResources().getStringArray(R.array.sortEntries)[sortValue]);
        }

        private void setCustomFilenameSummary() {
            int filenameValue =Integer.valueOf(appPreferences.getCustomFilename())-1;
            prefCustomFilename.setSummary(getResources().getStringArray(R.array.filenameEntries)[filenameValue]);
        }

        @Override
        public void onSelectDirectory(@NonNull String path) {
            appPreferences.setCustomPath(path);
            setCustomPathSummary();
            chooseDialog.dismiss();
        }

        private void setCustomPathSummary() {
            String path = appPreferences.getCustomPath(UtilsApp.getDefaultAppFolder().getPath());
            if(path.equals(UtilsApp.getDefaultAppFolder().getPath())){
                prefCustomPath.setSummary(getResources().getString(R.string.button_default)+": "+UtilsApp.getDefaultAppFolder().getPath());
            }else{
                prefCustomPath.setSummary(path);
            }
        }

        @Override
        public void onCancelChooser() {
            chooseDialog.dismiss();;
        }

    }

}