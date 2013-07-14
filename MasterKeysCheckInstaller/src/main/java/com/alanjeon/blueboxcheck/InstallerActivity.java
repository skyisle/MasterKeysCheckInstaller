package com.alanjeon.blueboxcheck;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InstallerActivity extends FragmentActivity implements BlueBoxCheckTask.OnCheckResult, View.OnClickListener {

    private Uri mPackageURI;
    private BlueBoxCheckTask mBlueBoxCheckTask;
    private Button mOk;
    private Button mCancel;
    private ProgressBar progressBar;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checking);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        status = (TextView) findViewById(R.id.status);
        mOk = (Button) findViewById(R.id.ok_button);
        mCancel = (Button) findViewById(R.id.cancel_button);

        mOk.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        // get intent information
        final Intent intent = getIntent();
        mPackageURI = intent.getData();
        boolean installerMode = true;

        if (mPackageURI != null) {
            final String scheme = mPackageURI.getScheme();
            if (scheme != null && !"file".equals(scheme) && !"package".equals(scheme)) {
                installerMode = false;
            }
        } else {
            installerMode = false;
        }

        if (!installerMode) {
            finish();
            return;
        }

        String apkPath = mPackageURI.getPath();
        ApplicationInfo appInfo = getApplicationInfoFromApk(apkPath);

        if (appInfo == null) {
            progressBar.setVisibility(View.GONE);
            //mCancel.setVisibility(View.GONE);
            mOk.setVisibility(View.GONE);

            status.setText("Failed to read apk info");
            return;
        }

        String label = (String) appInfo.loadLabel(getPackageManager());
        Drawable img = appInfo.loadIcon(getPackageManager());

        View snippetView = findViewById(R.id.uninstall_activity_snippet);
        initSnippet(snippetView, label, img);


        status.setText("Checking ANDROID MASTERKEYS exploit....");

        mOk.setVisibility(View.GONE);

        mBlueBoxCheckTask = new BlueBoxCheckTask(this);
        mBlueBoxCheckTask.execute(mPackageURI);
    }

    private ApplicationInfo getApplicationInfoFromApk(String apkPath) {
        PackageInfo info = getPackageManager().getPackageArchiveInfo(apkPath, 0);
        if (info == null) {
            return null;
        }
        info.applicationInfo.sourceDir = apkPath;
        info.applicationInfo.publicSourceDir = apkPath;

        return info.applicationInfo;
    }

    private View initSnippet(View snippetView, CharSequence label, Drawable icon) {
        ((ImageView) snippetView.findViewById(R.id.app_icon)).setImageDrawable(icon);
        ((TextView) snippetView.findViewById(R.id.app_name)).setText(label);
        return snippetView;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBlueBoxCheckTask != null) {
            mBlueBoxCheckTask.onStop();
        }

        mBlueBoxCheckTask = null;
        finish();
    }

    @Override
    public void OnCheckResult(boolean hasDuplicated) {
        if (!hasDuplicated) {
            progressBar.setVisibility(View.GONE);
            mCancel.setVisibility(View.GONE);
            mOk.setVisibility(View.GONE);

            status.setText("This apk is Okay");

            status.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = getIntent();
                    intent.setPackage("com.android.packageinstaller");

                    startActivity(Intent.createChooser(intent, "Choose installer"));

                }
            }, 500);
        } else {
            progressBar.setVisibility(View.GONE);
            mCancel.setVisibility(View.GONE);
            mOk.setVisibility(View.VISIBLE);

            status.setTextColor(Color.RED);
            status.setText("This apk has ANDROID MASTERKEYS exploit. DO NOT INSTALL THIS APK.");
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mOk) {
            finish();
        } else if (v == mCancel) {
            finish();
        }

    }
}
