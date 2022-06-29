package com.myapp.supermanhero;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.materialdrawer.Drawer;
import com.myapp.mylibrary.AppInfo;
import com.myapp.mylibrary.AppPreferences;
import com.myapp.mylibrary.UtilsApp;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.yalantis.phoenix.PullToRefreshView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class MainActivity extends AppCompatActivity{

    private AppPreferences appPreferences;
    protected RecyclerView recyclerView;
    private Drawer drawer;
    private Toolbar toolbar;
    private AppAdapter appAdapter,appSystemAdapter,appFavoriteAdapter,appHiddenAdapter;
    private Boolean doubleBackToExitPressedOnce = false;
    private PullToRefreshView pullToRefreshView;
    private SearchView searchView;
    private static LinearLayout noResult;
    //private MenuItem searchItem;
    private ProgressWheel progressWheel;
    private Context context;
    private Activity activity;
    private List<AppInfo> appList;
    private List<AppInfo> appSystemList;
    private List<AppInfo> appHiddenList;
    private AdsInterstitial adsInterstitial;

    public static void setResultsMessage(boolean result) {
        if(result){
            noResult.setVisibility(View.VISIBLE);
        }else{
            noResult.setVisibility(View.GONE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
        this.activity = this;
        this.appPreferences = MLManagerApplication.getAppPreferences();

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(R.string.app_name);
        }

        adsInterstitial = new AdsInterstitial(
                "07CC7E40850ABA2DF210A2D2564CAD76",
                "ca-app-pub-8404443559572571/7181154350",
                this);

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

        checkAndAddPermissions(this);
        setAppDir();

        recyclerView = findViewById(R.id.appList);
        pullToRefreshView = findViewById(R.id.pull_to_refresh);
        progressWheel = findViewById(R.id.progress);
        noResult = findViewById(R.id.noResults);

        pullToRefreshView.setEnabled(false);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        drawer = UtilsUI.setNavigationDrawer(this,this,toolbar,
                appAdapter,appSystemAdapter,appFavoriteAdapter,appHiddenAdapter,recyclerView);

        progressWheel.setBarColor(appPreferences.getPrimaryColorPref(getResources().getColor(R.color.primary)));
        progressWheel.setVisibility(View.VISIBLE);
        new getInstalledApps().execute();

    }



    class getInstalledApps extends AsyncTask<Void,String,Void> {

        private Integer actualApps;
        private Integer totalApps;

        public getInstalledApps(){
            actualApps = 0;

            appList = new ArrayList<>();
            appSystemList = new ArrayList<>();
            appHiddenList = new ArrayList<>();

        }
        @Override
        protected Void doInBackground(Void... voids) {

            int flags = PackageManager.GET_META_DATA |
                    PackageManager.GET_SHARED_LIBRARY_FILES |
                    PackageManager.GET_UNINSTALLED_PACKAGES;
            final PackageManager packageManager = getPackageManager();
            List<PackageInfo> packages = packageManager.getInstalledPackages(flags);

            List<PackageInfo> packages2 = packageManager.getInstalledPackages(0);


            List<ApplicationInfo> infos = getPackageManager().getInstalledApplications(flags);
            // create a list with size of total number of apps
            String[] apps = new String[infos.size()];

            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities( mainIntent, 0);

            Set<String> hiddenApps = appPreferences.getHiddenApps();
            totalApps = packages.size() + hiddenApps.size();

            switch (appPreferences.getSortMode()){
                default:
                    Collections.sort(packages, new Comparator<PackageInfo>() {
                        @Override
                        public int compare(PackageInfo p1, PackageInfo p2) {
                            return packageManager.getApplicationLabel(p1.applicationInfo).toString().toLowerCase()
                                    .compareTo(packageManager.getApplicationLabel(p2.applicationInfo).toString().toLowerCase());
                        }
                    });
                    break;
                case "2":
                    Collections.sort(packages, new Comparator<PackageInfo>() {
                        @Override
                        public int compare(PackageInfo p1, PackageInfo p2) {
                            Long size1 = new File(p1.applicationInfo.sourceDir).length();
                            Long size2 = new File(p2.applicationInfo.sourceDir).length();
                            return size2.compareTo(size1);
                        }
                    });
                    break;
                case "3":
                    Collections.sort(packages, new Comparator<PackageInfo>() {
                        @Override
                        public int compare(PackageInfo p1, PackageInfo p2) {
                            return Long.toString(p1.firstInstallTime).compareTo(Long.toString(p1.firstInstallTime));
                        }
                    });
                    break;
                case "4":
                    Collections.sort(packages, new Comparator<PackageInfo>() {
                        @Override
                        public int compare(PackageInfo p1, PackageInfo p2) {
                            return Long.toString(p2.lastUpdateTime).compareTo(Long.toString(p1.lastUpdateTime));
                        }
                    });
                    break;
            }

            for(PackageInfo packageInfo:packages){
                //if(!(packageManager.getApplicationLabel(packageInfo.applicationInfo).equals("")||packageInfo.packageName.equals(""))){
                if (!(packageManager.getApplicationLabel(packageInfo.applicationInfo).equals("") || packageInfo.packageName.equals(""))) {
                    if((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                        try{
                            String t = packageInfo.applicationInfo.dataDir;
                            AppInfo tempApp = new AppInfo(
                                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
                                    packageInfo.packageName,
                                    packageInfo.versionName,
                                    packageInfo.applicationInfo.sourceDir,
                                    packageInfo.applicationInfo.dataDir,
                                    packageInfo.packageName,false);
                                    //packageManager.getApplicationIcon(packageInfo.applicationInfo),false);
                            Drawable icon = packageManager.getApplicationIcon(packageInfo.applicationInfo);
                            appList.add(tempApp);
                        }catch (OutOfMemoryError e){
                            AppInfo tempApp = new AppInfo(
                                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
                                    packageInfo.packageName,
                                    packageInfo.versionName,
                                    packageInfo.applicationInfo.sourceDir,
                                    packageInfo.applicationInfo.dataDir,
                                    packageInfo.packageName,false);
                                    //ContextCompat.getDrawable(context,R.drawable.ic_launcher_background),false);
                            String icon = packageManager.getApplicationIcon(packageInfo.applicationInfo).toString();
                            appList.add(tempApp);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        try{
                            AppInfo tempApp = new AppInfo(
                                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
                                    packageInfo.packageName,
                                    packageInfo.versionName,
                                    packageInfo.applicationInfo.sourceDir,
                                    packageInfo.applicationInfo.dataDir,
                                    packageInfo.packageName,true);
                            String icon = packageManager.getApplicationIcon(packageInfo.applicationInfo).toString();
                                    //packageManager.getApplicationIcon(packageInfo.applicationInfo),true);
                            appSystemList.add(tempApp);
                        }catch (OutOfMemoryError e){
                            AppInfo tempApp = new AppInfo(
                                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString(),
                                    packageInfo.packageName,
                                    packageInfo.versionName,
                                    packageInfo.applicationInfo.sourceDir,packageInfo.applicationInfo.dataDir,
                                    packageInfo.packageName,false);
                                    //ContextCompat.getDrawable(context,R.drawable.ic_launcher_background),false);
                            String icon = packageManager.getApplicationIcon(packageInfo.applicationInfo).toString();
                            appSystemList.add(tempApp);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                actualApps++;
                publishProgress(Double.toString((actualApps*100)/totalApps));
            }

            for(String app: hiddenApps){
                AppInfo tempApp = new AppInfo(app);

                Drawable tempAppIcon = UtilsApp.getIconFromCache(context,tempApp,
                        ContextCompat.getDrawable(context,R.drawable.ic_launcher_background));

                //tempApp.setIcon(tempAppIcon);
                tempApp.setIcon("icon");

                appHiddenList.add(tempApp);

                actualApps++;
                publishProgress(Double.toString((actualApps*100)/totalApps));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressWheel.setProgress(Float.parseFloat(values[0]));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            appAdapter = new AppAdapter(appList,context,activity,adsInterstitial);
            appSystemAdapter = new AppAdapter(appSystemList,context,activity,adsInterstitial);
            appHiddenAdapter = new AppAdapter(appHiddenList,context,activity,adsInterstitial);
            appFavoriteAdapter = new AppAdapter(getFavoriteList(appList,appSystemList),context,activity,adsInterstitial);

            recyclerView.setAdapter(appAdapter);
            pullToRefreshView.setEnabled(true);
            progressWheel.setVisibility(View.GONE);

            setPulltoRefreshView(pullToRefreshView);
            drawer.closeDrawer();
            drawer = UtilsUI.setNavigationDrawer(activity,context,toolbar,
                    appAdapter,appSystemAdapter,appFavoriteAdapter,appHiddenAdapter,recyclerView);
        }

        private void setPulltoRefreshView(PullToRefreshView pullToRefreshView) {
            pullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    appAdapter.clear();
                    appSystemAdapter.clear();
                    appFavoriteAdapter.clear();

                    recyclerView.setAdapter(null);
                    new getInstalledApps().execute();

                    pullToRefreshView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pullToRefreshView.setRefreshing(false);
                        }
                    },2000);
                }
            });
        }
    }

    private void checkAndAddPermissions(Activity activity){
        UtilsApp.checkPermissions(activity);
    }
    private void setAppDir(){
        File appDir = UtilsApp.getAppFolder(appPreferences);
        if(!appDir.exists()){
            appDir.mkdir();
        }
    }

    private List<AppInfo> getFavoriteList(List<AppInfo> appList, List<AppInfo> appSystemList){
        List<AppInfo> res = new ArrayList<>();
        for(AppInfo app: appList){
            if(UtilsApp.isAppFavorite(app.getAPK(),appPreferences.getFavoriteApps())){
                res.add(app);
            }
        }
        for(AppInfo app: appSystemList){
            if(UtilsApp.isAppFavorite(app.getAPK(),appPreferences.getFavoriteApps())){
                res.add(app);
            }
        }
        return res;
    }


   /* @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.isEmpty()){
            ((AppAdapter) Objects.requireNonNull(recyclerView.getAdapter())).getFilter().filter("");
        }else{
            ((AppAdapter) Objects.requireNonNull(recyclerView.getAdapter())).getFilter().filter(newText.toLowerCase());
        }
        return false;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =(SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()){
                    ((AppAdapter) Objects.requireNonNull(recyclerView.getAdapter())).getFilter().filter("");
                }else{
                    ((AppAdapter) Objects.requireNonNull(recyclerView.getAdapter())).getFilter().filter(newText.toLowerCase());
                }

                return false;
            }
        });

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_search:
                //Toast.makeText(this,"new game",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
                context.startActivity(new Intent(context, SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                //Toast.makeText(this,"help",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if( !searchView.isIconified()){
            searchView.onActionViewCollapsed();
        }else{
            if(doubleBackToExitPressedOnce){
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this,R.string.tap_exit,Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            },2000);
        }
    }
}