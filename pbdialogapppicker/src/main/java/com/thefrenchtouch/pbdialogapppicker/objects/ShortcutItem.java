package com.thefrenchtouch.pbdialogapppicker.objects;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;


import com.thefrenchtouch.pbdialogapppicker.DialogAppPicker;
import com.thefrenchtouch.pbdialogapppicker.ShortcutCreatedListener;
import com.thefrenchtouch.pbdialogapppicker.ShortcutHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ShortcutItem extends AppItem implements ShortcutHandler {
    private Intent mCreateShortcutIntent;
    private ShortcutCreatedListener mShortcutCreatedListener;
    private Context mContext;
    private Resources mResources;
    private boolean mAllowUnlockAction;
    private ResolveInfo ri;

    public ShortcutItem(Context mContext, String appName, ResolveInfo ri) {
        super(mContext, appName, ri);
        this.mContext = mContext;
        this.ri = ri;
        mResources = ((Activity)mContext).getResources();
        mAppName = appName;
        mResolveInfo = ri;
        if (mResolveInfo != null) {
            mCreateShortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
            ComponentName cn = new ComponentName(mResolveInfo.activityInfo.packageName,
                    mResolveInfo.activityInfo.name);
            mCreateShortcutIntent.setComponent(cn);
            // mark intent so we can later identify it comes from GB
            mCreateShortcutIntent.putExtra("gravitybox", true);

            if (mAllowUnlockAction) {
                //TODO
               // mCreateShortcutIntent.putExtra(ShortcutActivity.EXTRA_ALLOW_UNLOCK_ACTION, true);
            }
        }
    }

    public void setShortcutCreatedListener(ShortcutCreatedListener listener) {
        mShortcutCreatedListener = listener;
    }

    public String getPackageName() {
        return ri.activityInfo.packageName;
    }
    @Override
    protected String getKey() {
        return mCreateShortcutIntent.toUri(0);
    }

    @Override
    public Intent getCreateShortcutIntent() {
        return mCreateShortcutIntent;
    }


    public void onHandleShortcut(Intent intent, String name, Bitmap icon) {
        if (intent == null) {
            Toast.makeText(mContext, "Null intent", Toast.LENGTH_LONG).show();
            return;
        }

        mIntent = intent;
        mIntent.putExtra("mode", DialogAppPicker.MODE_SHORTCUT);

        // generate label
        if (name != null) {
            mAppName += ": " + name;
            mIntent.putExtra("label", name);
            mIntent.putExtra("prefLabel", mAppName);
        } else {
            mIntent.putExtra("label", mAppName);
            mIntent.putExtra("prefLabel", mAppName);
        }

        if (icon != null) {
            mAppIcon = new BitmapDrawable(mResources, icon);
        }

        // process icon
        if (mAppIcon != null) {
            try {
                final Context context = mContext;
                final String dir = context.getFilesDir() + "/app_picker";
                final String fileName = dir + "/" + UUID.randomUUID().toString();
                File d = new File(dir);
                d.mkdirs();
                d.setReadable(true, false);
                d.setExecutable(true, false);
                File f = new File(fileName);
                FileOutputStream fos = new FileOutputStream(f);
                final boolean iconSaved = icon == null ?
                        mAppIcon.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos) :
                        icon.compress(Bitmap.CompressFormat.PNG, 100, fos);
                if (iconSaved) {
                    mIntent.putExtra("icon", f.getAbsolutePath());
                    f.setReadable(true, false);
                }
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // callback to shortcut created listener if set
        if (mShortcutCreatedListener != null) {
            mShortcutCreatedListener.onShortcutCreated(this);
        }
    }

    @Override
    public void onShortcutCancelled() {
        Toast.makeText(mContext,"Cancel", Toast.LENGTH_SHORT).show();
    }


}
