package com.joelglanfield.launcher;

import android.graphics.drawable.Drawable;

/**
 * Created by joel on 2017-02-10.
 */

public class AppPackage {

    private String packageName;
    private String appName;
    private boolean isSystemApp;
    private Drawable appIcon;

    public AppPackage (String packageName, String appName, boolean isSystemApp, Drawable appIcon) {
        this.packageName = packageName;
        this.appName = appName;
        this.isSystemApp = isSystemApp;
        this.appIcon = appIcon;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getAppName() {
        return appName;
    }

    public boolean getIsSystemApp() {
        return isSystemApp;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

}
