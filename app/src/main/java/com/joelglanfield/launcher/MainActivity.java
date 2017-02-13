package com.joelglanfield.launcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String MRU_APPS_KEY = "com.joelglanfield.mru_apps";

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        RecyclerView.Adapter adapter = new InstalledAppsAdapter(
                new AppsManager(getApplicationContext()).getInstalledPackages()
        );

        recyclerView.setAdapter(adapter);
    }

    private void addAppToMRU(String appName) {
        Set<String> mruApps = new HashSet<>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Set<String> sharedMruApps = preferences.getStringSet(MRU_APPS_KEY, null);
        if (sharedMruApps != null) {
            for (String s : sharedMruApps) {
                mruApps.add(s);
            }
        }

        mruApps.add(appName);
        preferences.edit()
                .putStringSet(MRU_APPS_KEY, mruApps)
                .apply();
    }

    private class InstalledAppsAdapter extends RecyclerView.Adapter<InstalledAppsAdapter.AppsBaseViewholder> {

        private final int HEADER_ITEM = 0;
        private final int APP_ITEM = 1;

        private List<AppPackage> mruPackages;
        private List<AppPackage> userAppPackages;
        private List<AppPackage> systemAppPackages;

        InstalledAppsAdapter(List<AppPackage> packages) {
            Collections.sort(packages, new Comparator<AppPackage>() {
                @Override
                public int compare(AppPackage ap1, AppPackage ap2) {
                    String appName1 = ap1.getAppName();
                    String appName2 = ap2.getAppName();

                    boolean isSystemApp1 = ap1.getIsSystemApp();
                    boolean isSystemApp2 = ap2.getIsSystemApp();

                    if (isSystemApp1 == isSystemApp2) {
                        return appName1.compareToIgnoreCase(appName2);
                    } else if (isSystemApp1) {
                        return 1;
                    }

                    return -1;
                }
            });

            Set<String> mruAppNames = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getStringSet(MRU_APPS_KEY, null);


            mruPackages = new ArrayList<>();
            userAppPackages = new ArrayList<>();
            systemAppPackages = new ArrayList<>();

            for (int i = 0; i < packages.size(); i++) {
                AppPackage ap = packages.get(i);
                if (!ap.getIsSystemApp()) {
                    userAppPackages.add(ap);
                } else {
                    systemAppPackages.add(ap);
                }

                if (mruAppNames != null && mruAppNames.contains(ap.getAppName())) {
                    mruPackages.add(ap);
                }
            }
        }

        class AppsBaseViewholder extends RecyclerView.ViewHolder {
            AppsBaseViewholder(View v) {
                super(v);
            }
        }

        class AppsHeaderViewHolder extends AppsBaseViewholder {
            TextView headerTextView;

            AppsHeaderViewHolder(View v) {
                super(v);

                headerTextView = (TextView) v.findViewById(R.id.apps_header_label);
            }
        }

        class AppItemViewHolder extends AppsBaseViewholder {
            TextView mTextViewLabel;
            ImageView mImageViewIcon;

            AppItemViewHolder(View v) {
                super(v);

                mTextViewLabel = (TextView) v.findViewById(R.id.app_label);
                mImageViewIcon = (ImageView) v.findViewById(R.id.iv_icon);
            }
        }

        @Override
        public AppsBaseViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == HEADER_ITEM) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.apps_header, parent, false);
                return new AppsHeaderViewHolder(view);
            }

            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.app_item, parent, false);
            return new AppItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AppsBaseViewholder holder, int position) {
            if (isHeaderPosition(position)) {
                AppsHeaderViewHolder appsHeaderViewHolder = (AppsHeaderViewHolder) holder;
                appsHeaderViewHolder.headerTextView.setText(getTitleForHeader(position));
                return;
            }

            final AppPackage appPackage = getAppPackageAtPosition(position);

            AppItemViewHolder appItemViewHolder = (AppItemViewHolder) holder;
            appItemViewHolder.mTextViewLabel.setText(appPackage.getAppName());
            appItemViewHolder.mImageViewIcon.setImageDrawable(appPackage.getAppIcon());
            appItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = MainActivity.this.getPackageManager().getLaunchIntentForPackage(appPackage.getPackageName());
                    if (intent != null) {
                        MainActivity.this.startActivity(intent);
                        MainActivity.this.addAppToMRU(appPackage.getAppName());
                    } else {
                        Toast.makeText(MainActivity.this, appPackage.getPackageName() + " Launch Error.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            appItemViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mruPackages.isEmpty()) {
                return userAppPackages.size() + systemAppPackages.size() + 2;
            }

            return mruPackages.size() + userAppPackages.size() + systemAppPackages.size() + 3;
        }

        @Override
        public int getItemViewType(int position) {
            if (isHeaderPosition(position)) {
                return HEADER_ITEM;
            }

            return APP_ITEM;
        }

        private boolean isHeaderPosition(int position) {
            if (position == 0) {
                return true;
            }

            if (mruPackages.isEmpty()) {
                return position == userAppPackages.size() + 1;
            } else {
                return position == mruPackages.size() + 1 ||
                        position == mruPackages.size() + 1 + userAppPackages.size() + 1;
            }
        }

        private String getTitleForHeader(int position) {
            if (!mruPackages.isEmpty()) {
                if (position == 0) {
                    return "RECENT";
                } else if (position == mruPackages.size() + 1) {
                    return "MY APPS";
                } else {
                    return "SYSTEM APPS";
                }
            } else if (position == 0) {
                return "MY APPS";
            }

            return "SYSTEM APPS";
        }

        private AppPackage getAppPackageAtPosition(int position) {
            if (!mruPackages.isEmpty()) {
                if (position <= mruPackages.size()) {
                    return mruPackages.get(position - 1);
                } else if (position <= mruPackages.size() + userAppPackages.size() + 2) {
                    return userAppPackages.get(position - mruPackages.size() - 2);
                } else {
                    return systemAppPackages.get(position - mruPackages.size() - userAppPackages.size() - 3);
                }
            } else if (position <= userAppPackages.size()) {
                return userAppPackages.get(position-1);
            } else {
                Log.d("Launcher", "Position: " + position + " System apps: " + systemAppPackages.size() + " User apps: " + userAppPackages.size());
                return systemAppPackages.get(position - userAppPackages.size() - 2);
            }
        }
    }
}