package com.thefrenchtouch.pbdialogapppicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;


import com.thefrenchtouch.pbdialogapppicker.adapters.mothers.IconListAdapter;
import com.thefrenchtouch.pbdialogapppicker.objects.AppItem;
import com.thefrenchtouch.pbdialogapppicker.objects.ShortcutItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Paul on 12/07/15.
 */
public class DialogAppPicker implements OnItemSelectedListener {

    Dialog mIconPickerDialog;
    View mPrincipalView;
    ListView mListView;
    EditText mSearch;
    ProgressBar mProgressBar;
    private int mMode = 0;
    public static final int MODE_APP = 0;
    public static final int MODE_SHORTCUT = 1;
    private Context c;
    private boolean mNullItemEnabled = true;
    private Resources mResources;
    private static IconListAdapter sIconPickerAdapter;
    private OnItemChooseListener mItemChooseListener;
    Spinner mSpinner;

    public DialogAppPicker(Context c){
        this.c = c;
    }

    private void init(Context c) {
        this.c = c;
        mResources = ((Activity) c).getResources();
        mPrincipalView = ((Activity) c).getLayoutInflater().inflate(R.layout.app_picker_preference, null);
        mListView = (ListView) mPrincipalView.findViewById(R.id.icon_list);
        mSearch = (EditText) mPrincipalView.findViewById(R.id.input_search);
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) { }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) { }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3)
            {
                if(mListView.getAdapter() == null)
                    return;

                ((IconListAdapter)mListView.getAdapter()).getFilter().filter(arg0);
            }
        });
        mProgressBar = (ProgressBar) mPrincipalView.findViewById(R.id.progress_bar);
        mSpinner = (Spinner) mPrincipalView.findViewById(R.id.mode_spinner);

        ArrayAdapter<String> mModeSpinnerAdapter = new ArrayAdapter<String>(
                c, android.R.layout.simple_spinner_item,
                new ArrayList<String>(Arrays.asList(
                        "Applications",
                        "Raccourcis")));
        mModeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mModeSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(this);
        mMode = mSpinner.getSelectedItemPosition();
    }



    public void show() {
        init(c);
        AlertDialog.Builder builder = new AlertDialog.Builder(c)
                .setTitle("Choose an App or a Shortcut");
//                          builder.setAdapter(sIconPickerAdapter, new DialogInterface.OnClickListener() {
//                              @Override
//                              public void onClick(DialogInterface dialog, int which) {
//                                  dialog.dismiss();
//                                  try {
//                                      BasicIconListItem item = (BasicIconListItem) sIconPickerAdapter.getItem(which);
//                                      Log.i("ITEM", item.getText());
//                                  } catch (Exception e) {
//                                      e.printStackTrace();
//                                  }
//
//                              }
//                          });
        builder.setView(mPrincipalView);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mIconPickerDialog = builder.create();
        setData();

        mIconPickerDialog.show();
    }

    public void dismiss(){
       mIconPickerDialog.dismiss();
    }

    private void setData() {
        AsyncTask mAsyncTask = new AsyncTask<Void, Void, ArrayList<AppItem>>() {
            PackageManager mPackageManager;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mListView.setVisibility(View.INVISIBLE);
                mSearch.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                mPackageManager = ((Activity) c).getPackageManager();
            }

            @Override
            protected ArrayList<AppItem> doInBackground(Void... arg0) {
                ArrayList<AppItem> itemList = new ArrayList<AppItem>();
                List<ResolveInfo> appList = new ArrayList<ResolveInfo>();

                List<PackageInfo> packages = mPackageManager.getInstalledPackages(0);
                Intent mainIntent = new Intent();
                if (mMode == MODE_APP) {
                    mainIntent.setAction(Intent.ACTION_MAIN);
                    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                } else if (mMode == MODE_SHORTCUT) {
                    mainIntent.setAction(Intent.ACTION_CREATE_SHORTCUT);
                }
                for (PackageInfo pi : packages) {
                    if (this.isCancelled()) break;
                    mainIntent.setPackage(pi.packageName);
                    List<ResolveInfo> activityList = mPackageManager.queryIntentActivities(mainIntent, 0);
                    for (ResolveInfo ri : activityList) {
                        appList.add(ri);
                    }
                }

                Collections.sort(appList, new ResolveInfo.DisplayNameComparator(mPackageManager));
                if (mNullItemEnabled) {
                    itemList.add(mMode == MODE_SHORTCUT ?
                            new ShortcutItem(c, "Shortcuts", null) :
                            new AppItem(c, "Applications", null));
                }
                for (ResolveInfo ri : appList) {
                    if (this.isCancelled()) break;
                    String appName = ri.loadLabel(mPackageManager).toString();
                    AppItem ai = mMode == MODE_SHORTCUT ?
                            new ShortcutItem(c, appName, ri) : new AppItem(c, appName, ri);
                    itemList.add(ai);
                }

                return itemList;
            }

            @Override
            protected void onPostExecute(final ArrayList<AppItem> result) {
                mProgressBar.setVisibility(View.GONE);
                mSearch.setVisibility(View.VISIBLE);
                mListView.setAdapter(new IconListAdapter(c, result));
                ((IconListAdapter) mListView.getAdapter()).notifyDataSetChanged();
                mListView.setVisibility(View.VISIBLE);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        try {
                            //On test si c'est un raccourci
                            ((ShortcutItem) result.get(position)).setShortcutCreatedListener(new ShortcutCreatedListener() {
                                @Override
                                public void onShortcutCreated(ShortcutItem sir) {
                                    //setValue(sir.getValue());
                                    mItemChooseListener.onShortcutSelected(sir);
                                    // we have to call this explicitly for some yet unknown reason...
                                    // sPrefsFragment.onSharedPreferenceChanged(getSharedPreferences(), getKey());
                                    //getDialog().dismiss();
                                }
                            });
                            obtainShortcut((ShortcutItem) result.get(position));
                        } catch (ClassCastException e) {
                            //Sinon c'est une app
                            mItemChooseListener.onAppSelected(result.get(position));
                        }

                    }
                });
            }
        }.execute();
    }
    public void setOnItemChooseListener(OnItemChooseListener l){
        this.mItemChooseListener = l;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterViewCompat, View view, int i, long l) {
        mMode = i;
        setData();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterViewCompat) {

    }

    public interface OnItemChooseListener{
        public void onAppSelected(AppItem item);
        public void onShortcutSelected(ShortcutItem item);
    }
    private ShortcutHandler mShortcutHandler;

    public void obtainShortcut(ShortcutHandler handler) {
        if (handler == null) return;

        try {
            mShortcutHandler = handler;
            ((Activity) c).startActivityForResult(handler.getCreateShortcutIntent(), 1028);
        }catch (NullPointerException e){}
    }

    private int REQ_OBTAIN_SHORTCUT = 1028;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_OBTAIN_SHORTCUT && mShortcutHandler != null) {
            if (resultCode == Activity.RESULT_OK) {
                Bitmap b = null;
                Intent.ShortcutIconResource siRes = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                if (siRes != null) {
                    try {
                        final Context extContext = ((Activity) c).createPackageContext(
                                siRes.packageName, Context.CONTEXT_IGNORE_SECURITY);
                        final Resources extRes = extContext.getResources();
                        final int drawableResId = extRes.getIdentifier(siRes.resourceName, "drawable", siRes.packageName);
                        b = BitmapFactory.decodeResource(extRes, drawableResId);
                    } catch (PackageManager.NameNotFoundException e) {
                        //
                    }
                }
                if (b == null) {
                    b = (Bitmap) data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
                }

                ((ShortcutItem) mShortcutHandler).onHandleShortcut(
                        (Intent) data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME), b);
            } else {
                mShortcutHandler.onShortcutCancelled();
            }
        }
    }
}
