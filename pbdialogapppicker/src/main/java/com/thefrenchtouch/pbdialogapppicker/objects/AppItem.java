package com.thefrenchtouch.pbdialogapppicker.objects;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.util.TypedValue;

import com.thefrenchtouch.pbdialogapppicker.DialogAppPicker;
import com.thefrenchtouch.pbdialogapppicker.IIconListAdapterItem;
import com.thefrenchtouch.pbdialogapppicker.utils.Utils;

public class AppItem implements IIconListAdapterItem {
    protected String mAppName;
    protected BitmapDrawable mAppIcon;
    protected ResolveInfo mResolveInfo;
    protected Intent mIntent;

    private static LruCache<String, BitmapDrawable> sAppIconCache;

    static {
        final int cacheSize = Math.min((int) Runtime.getRuntime().maxMemory() / 6, 4194304);
        sAppIconCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable d) {
                return d.getBitmap().getByteCount();
            }
        };
    }

    private AppItem() {
    }
private Context c;
    private PackageManager mPackageManager;
    private Resources mResources;
    private int mAppIconSizePx;
    private int mAppIconPreviewSizePx;
    private int mIconPickSizePx;
    private ResolveInfo ri;
    public AppItem(Context c, String appName, ResolveInfo ri) {
        this.c = c;
        this.ri = ri;
        mPackageManager = ((Activity)c).getPackageManager();
        mResources = ((Activity)c).getResources();
        mAppName = appName;
        mResolveInfo = ri;
        if (mResolveInfo != null) {
            mIntent = new Intent(Intent.ACTION_MAIN);
            mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(mResolveInfo.activityInfo.packageName,
                    mResolveInfo.activityInfo.name);
            mIntent.setComponent(cn);
            mIntent.putExtra("mode", DialogAppPicker.MODE_APP);
        }

        mAppIconSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40,
                mResources.getDisplayMetrics());
        mAppIconPreviewSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                mResources.getDisplayMetrics());
        mIconPickSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50,
                mResources.getDisplayMetrics());


    }

    public String getAppName() {
        return mAppName;
    }
    public String getPackageName() {
        return ri.activityInfo.packageName;
    }

    public String getValue() {
        return (mIntent == null ? null : mIntent.toUri(0));
    }

    public Intent getIntent() {
        return mIntent;
    }

    @Override
    public String getText() {
        return mAppName;
    }

    @Override
    public String getSubText() {
        return null;
    }

    protected String getKey() {
        return getValue();
    }

    @Override
    public Drawable getIconLeft() {
        if (mResolveInfo == null) return null;

        if (mAppIcon == null) {
            final String key = getKey();
            mAppIcon = sAppIconCache.get(key);
            if (mAppIcon == null) {
                Bitmap bitmap = Utils.drawableToBitmap(mResolveInfo.loadIcon(mPackageManager));
                bitmap = Bitmap.createScaledBitmap(bitmap, mAppIconSizePx, mAppIconSizePx, false);
                mAppIcon = new BitmapDrawable(mResources, bitmap);
                sAppIconCache.put(key, mAppIcon);
            }
        }
        return mAppIcon;
    }

    @Override
    public Drawable getIconRight() {
        return null;
    }
}
