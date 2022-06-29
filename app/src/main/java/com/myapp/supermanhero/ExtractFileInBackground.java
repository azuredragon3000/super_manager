package com.myapp.supermanhero;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;
import com.myapp.mylibrary.AppInfo;
import com.myapp.mylibrary.AppPreferences;
import com.myapp.mylibrary.UtilsApp;


public class ExtractFileInBackground extends AsyncTask<Void, String, Boolean> {
    private Context context;
    private Activity activity;
    private MaterialDialog dialog;
    private AppInfo appInfo;
    private AppPreferences appPreferences;

    public ExtractFileInBackground(Context context, MaterialDialog dialog, AppInfo appInfo, AppPreferences appPreferences) {
        this.activity = (Activity) context;
        this.context = context;
        this.dialog = dialog;
        this.appInfo = appInfo;
        this.appPreferences = appPreferences;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Boolean status = false;

        if (UtilsApp.checkPermissions(activity)) {
            if (!appInfo.getAPK().equals(MLManagerApplication.getProPackage())) {
                status = UtilsApp.copyFile(appInfo,appPreferences);
            } else {
                status = UtilsApp.extractMLManagerPro(context, appInfo,appPreferences,R.drawable.ic_launcher_background);
            }
        }

        return status;
    }

    @Override
    protected void onPostExecute(Boolean status) {
        super.onPostExecute(status);
        dialog.dismiss();
        if (status) {
            UtilsDialog.showSnackbar(activity, String.format(context.getResources().getString(R.string.dialog_saved_description),
                    appInfo.getName(), UtilsApp.getAPKFilename(appInfo,appPreferences)), context.getResources().getString(R.string.button_undo), UtilsApp.getOutputFilename(appInfo,appPreferences), 1).show();
        } else {
            UtilsDialog.showTitleContent(context, context.getResources().getString(R.string.dialog_extract_fail),
                    context.getResources().getString(R.string.dialog_extract_fail_description));
        }
    }
}