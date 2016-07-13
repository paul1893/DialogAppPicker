package com.thefrenchtouch.dialogapppicker.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.thefrenchtouch.pbdialogapppicker.DialogAppPicker;
import com.thefrenchtouch.pbdialogapppicker.objects.AppItem;
import com.thefrenchtouch.pbdialogapppicker.objects.ShortcutItem;
import com.thefrenchtouch.pbdialogapppicker.R;

public class MainActivity extends ActionBarActivity {

    private DialogAppPicker mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDialog = new DialogAppPicker(this);
        mDialog.setOnItemChooseListener(new DialogAppPicker.OnItemChooseListener() {
            @Override
            public void onAppSelected(AppItem item) {

            }

            @Override
            public void onShortcutSelected(ShortcutItem item) {

            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mDialog.onActivityResult(requestCode, resultCode, data);
        }
}
