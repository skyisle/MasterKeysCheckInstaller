package com.alanjeon.blueboxcheck;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

    private boolean patchedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get intent information

        CheckBlueBoxBug checker = new CheckBlueBoxBug();
        patchedDevice = checker.isPatchedDevice();

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setChecked(patchedDevice);

        int greenColor = getResources().getColor(R.color.green);
        TextView patchStatus = (TextView) findViewById(R.id.textView);
        patchStatus.setTextColor(patchedDevice ? greenColor : Color.RED);
        patchStatus.setText(patchedDevice ? "PATCHED" : "NOT PATCHED");

        TextView status = (TextView) findViewById(R.id.status);
        status.setTextColor(patchedDevice ?  greenColor : Color.RED);
        status.setText(patchedDevice ? "You don't need this application. You can uninstall." : "You need this application. Choose this as a installer.");

        View uninstallButton = findViewById(R.id.buttonUninstall);
        uninstallButton.setVisibility(patchedDevice ? View.VISIBLE : View.GONE);

        uninstallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri packageUri = Uri.parse("package:" + getPackageName());
                Intent uninstallIntent =
                        new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                startActivity(uninstallIntent);
            }
        });
    }
}
