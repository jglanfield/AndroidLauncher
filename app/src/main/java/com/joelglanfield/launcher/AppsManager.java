package com.joelglanfield.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joel on 2017-02-10.
 * This class taken from: http://www.android--tutorials.com/2016/03/android-get-installed-apps-icon-and.html
 */

public class AppsManager {
    private Context mContext;

    public AppsManager(Context context){
        mContext = context;
    }

    // Get a list of installed app
    public List<AppPackage> getInstalledPackages(){
        // Initialize a new Intent which action is main
        Intent intent = new Intent(Intent.ACTION_MAIN,null);

        // Set the newly created intent category to launcher
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // Set the intent flags
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        // Generate a list of ResolveInfo object based on intent filter
        List<ResolveInfo> resolveInfoList = mContext.getPackageManager().queryIntentActivities(intent,0);

        // Initialize a new ArrayList for holding non system package names
        List<AppPackage> packages = new ArrayList<>();

        // Loop through the ResolveInfo list
        for(ResolveInfo resolveInfo : resolveInfoList){
            // Get the ActivityInfo from current ResolveInfo
            ActivityInfo activityInfo = resolveInfo.activityInfo;

            AppPackage appPackage = new AppPackage(activityInfo.applicationInfo.packageName,
                    getApplicationLabelByPackageName(activityInfo.applicationInfo.packageName),
                    isSystemPackage(resolveInfo),
                    getAppIconByPackageName(activityInfo.applicationInfo.packageName));
            packages.add(appPackage);
        }

        return packages;
    }

    // Custom method to determine an app is system app
    private boolean isSystemPackage(ResolveInfo resolveInfo){
        return ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    // Custom method to get application icon by package name
    private Drawable getAppIconByPackageName(String packageName) {
        Drawable icon = null;
        try{
            icon = mContext.getPackageManager().getApplicationIcon(packageName);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            // Get a default icon
//            icon = ContextCompat.getDrawable(mContext,R.drawable.ic_face_black_36dp);
        }
        return icon;
    }

    // Custom method to get application label by package name
    private String getApplicationLabelByPackageName(String packageName){
        PackageManager packageManager = mContext.getPackageManager();
        ApplicationInfo applicationInfo;
        String label = "Unknown";
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if(applicationInfo!=null){
                label = (String)packageManager.getApplicationLabel(applicationInfo);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }
}