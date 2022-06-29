package com.myapp.supermanhero;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.button.MaterialButton;
import com.myapp.mylibrary.AppInfo;
import com.myapp.mylibrary.AppPreferences;
import com.myapp.mylibrary.UtilsApp;
import com.myapp.mylibrary.views.ButtonFlat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> implements Filterable {

    private List<AppInfo> appList;
    private List<AppInfo>  appListSearch;
    private AppPreferences appPreferences;
    private Context context;
    private Activity activity;
    private AdsInterstitial adsInterstitial;

    public AppAdapter(List<AppInfo> applist, Context context,Activity activity,AdsInterstitial adsInterstitial){
        this.appList = applist;
        this.context = context;
        this.activity = activity;
        this.appPreferences = MLManagerApplication.getAppPreferences();
        this.adsInterstitial = adsInterstitial;
    }
    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View appAdapterView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new AppViewHolder(appAdapterView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo appInfo = appList.get(position);
        holder.vName.setText(appInfo.getName());
        holder.vApk.setText(appInfo.getAPK());

        /*int id = context.getResources().getIdentifier(appInfo.getIcon(), "drawable", context.getPackageName());
        //imageView.setImageResource(id);
        holder.vIcon.setImageResource(id);*/

        String pkg = appInfo.getIcon(); //"com.app.my";//your package name
        Drawable icon = null;
        try {
            icon = context.getPackageManager().getApplicationIcon(pkg);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        holder.vIcon.setImageDrawable(icon);

        setButtonEvents(holder,appInfo);
    }

    private void setButtonEvents(AppViewHolder holder, AppInfo appInfo) {
        ButtonFlat appExtract = holder.vExtract;
        //ButtonFlat appShare = holder.vShare;
        ButtonFlat appOpen = holder.vOpen;
        ButtonFlat appMore = holder.vMore;

        final ImageView appIcon = holder.vIcon;
        final CardView cardView = holder.vCard;

        appExtract.setBackgroundColor(appPreferences.getPrimaryColorPref(context.getResources().getColor(R.color.primary)));
        //appShare.setBackgroundColor(appPreferences.getPrimaryColorPref(context.getResources().getColor(R.color.primary)));

        appOpen.setOnClickListener(v->{
            try {
                Intent intent = activity.getPackageManager().getLaunchIntentForPackage(appInfo.getAPK());
                activity.startActivity(intent);
            } catch (NullPointerException e) {
                e.printStackTrace();
                UtilsDialog.showSnackbar(activity, String.format(activity.getResources().getString(R.string.dialog_cannot_open), appInfo.getName()), null, null, 2).show();
            }
        });
        appMore.setOnClickListener(v->{
            Activity activity = (Activity) context;
            Intent intent = new Intent(context, AppActivity.class);

            intent.putExtra("KEY_NAME", appInfo);

            intent.putExtra("app_name", appInfo.getName());
            intent.putExtra("app_apk", appInfo.getAPK());
            intent.putExtra("app_version", appInfo.getVersion());
            intent.putExtra("app_source", appInfo.getSource());
            intent.putExtra("app_data", appInfo.getData());
            intent.putExtra("app_icon",appInfo.getIcon());
            // this is can be throw error if we are using bitmap transfer
            // adaptive icon cant cast to bitmap
            //Bitmap bitmap = ((BitmapDrawable) appInfo.getIcon()).getBitmap();
            //intent.putExtra("app_icon", bitmap);
            intent.putExtra("app_isSystem", appInfo.isSystem());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String transitionName = context.getResources().getString(R.string.transition_app_icon);
                ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(activity, appIcon, transitionName);
                context.startActivity(intent, transitionActivityOptions.toBundle());
                adsInterstitial.showAds(activity);
            } else {
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back);
                adsInterstitial.showAds(activity);
            }
        });
        appExtract.setOnClickListener(v->{
            MaterialDialog dialog = UtilsDialog.showTitleContentWithProgress(context
                    , String.format(activity.getResources().getString(R.string.dialog_saving), appInfo.getName())
                    , activity.getResources().getString(R.string.dialog_saving_description));
            new ExtractFileInBackground(context, dialog, appInfo,appPreferences).execute();
        });

        /*appShare.setOnClickListener(v->{
            UtilsApp.copyFile(appInfo,appPreferences);
            Intent shareIntent = UtilsApp.getShareIntent(UtilsApp.getOutputFilename(appInfo,appPreferences));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent,
                    String.format(context.getResources().getString(R.string.send_to), appInfo.getName())));

        });*/

        cardView.setOnClickListener(v->{
            Activity activity = (Activity) context;
            Intent intent = new Intent(context, AppActivity.class);

            intent.putExtra("KEY_NAME", appInfo);

            intent.putExtra("app_name", appInfo.getName());
            intent.putExtra("app_apk", appInfo.getAPK());
            intent.putExtra("app_version", appInfo.getVersion());
            intent.putExtra("app_source", appInfo.getSource());
            intent.putExtra("app_data", appInfo.getData());
            intent.putExtra("app_icon",appInfo.getIcon());
            // this is can be throw error if we are using bitmap transfer
            // adaptive icon cant cast to bitmap
            //Bitmap bitmap = ((BitmapDrawable) appInfo.getIcon()).getBitmap();
            //intent.putExtra("app_icon", bitmap);
            intent.putExtra("app_isSystem", appInfo.isSystem());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String transitionName = context.getResources().getString(R.string.transition_app_icon);
                ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(activity, appIcon, transitionName);
                context.startActivity(intent, transitionActivityOptions.toBundle());
                adsInterstitial.showAds(activity);
            } else {
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back);
                adsInterstitial.showAds(activity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                final FilterResults oReturn = new FilterResults();
                final List<AppInfo> results = new ArrayList<>();
                if(appListSearch == null){
                    appListSearch = appList;
                }
                if(charSequence != null){
                    if(appListSearch != null && appListSearch.size() > 0){
                        for (final AppInfo appInfo : appListSearch){
                            if(appInfo.getName().toLowerCase().contains(charSequence.toString())){
                                results.add(appInfo);
                            }
                        }
                    }
                    oReturn.values = results;
                    oReturn.count = results.size();
                }
                return oReturn;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if(filterResults.count > 0){
                    MainActivity.setResultsMessage(false);
                }else{
                    MainActivity.setResultsMessage(true);
                }
                appList = (ArrayList<AppInfo>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        appList.clear();
        notifyDataSetChanged();
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder{

        protected TextView vName;
        protected TextView vApk;
        protected ImageView vIcon;
        protected ButtonFlat vExtract;
        //protected ButtonFlat vShare;
        protected CardView vCard;
        protected ButtonFlat vOpen;
        protected ButtonFlat vMore;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            vName = itemView.findViewById(R.id.txtName);
            vApk = itemView.findViewById(R.id.txtApk);
            vIcon = itemView.findViewById(R.id.imgIcon);
            vExtract = itemView.findViewById(R.id.btnExtract);
            //vShare = itemView.findViewById(R.id.btnShare);
            vCard = itemView.findViewById(R.id.app_card);
            vOpen = itemView.findViewById(R.id.btnOpen);
            vMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
