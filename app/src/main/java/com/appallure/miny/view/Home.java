package com.appallure.miny.view;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.gesture.Gesture;
import android.os.BatteryManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appallure.miny.MainActivity;
import com.appallure.miny.R;
import com.appallure.miny.model.App;
import com.appallure.miny.util.AppListUtil;
import com.appallure.miny.util.SimpleGestureListener;

import java.util.List;

public class Home extends Fragment {

    TextView shortcut1, shortcut2, shortcut3, shortcut4, setDefaultLauncher;
    String shortcut1PackageName, shortcut1AppName;
    String shortcut2PackageName, shortcut2AppName;
    String shortcut3PackageName, shortcut3AppName;
    String shortcut4PackageName, shortcut4AppName;

    ImageView swipe;

    private static Home instance;


    private TextView batteryPercentage;
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryPercentage.setText(String.valueOf(level) + "%");
        }
    };

    @Override
    public void onResume() {
        determineSetDefaultLauncher();
        super.onResume();
    }

    public static Home getInstance() {
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        this.instance = this;

        shortcut1 = view.findViewById(R.id.shortcut1);
        shortcut2 = view.findViewById(R.id.shortcut2);
        shortcut3 = view.findViewById(R.id.shortcut3);
        shortcut4 = view.findViewById(R.id.shortcut4);
        setDefaultLauncher = view.findViewById(R.id.tv_set_default_launcher);
        swipe = view.findViewById(R.id.iv_swipe);

        shortcut1.setOnClickListener(clickListener);
        shortcut2.setOnClickListener(clickListener);
        shortcut3.setOnClickListener(clickListener);
        shortcut4.setOnClickListener(clickListener);

        shortcut1.setOnLongClickListener(longClickListener);
        shortcut2.setOnLongClickListener(longClickListener);
        shortcut3.setOnLongClickListener(longClickListener);
        shortcut4.setOnLongClickListener(longClickListener);


        batteryPercentage = (TextView) view.findViewById(R.id.batteryPercentage);
        getActivity().registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


        setUpShortcuts();



       /* refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpShortcuts();
                determineSetDefaultLauncher();
                AppListUtil.refreshAppList(getContext());
            }
        });
*/
        swipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Swipe left to see all apps", Toast.LENGTH_SHORT).show();
            }
        });

        final GestureDetector gestureDetector = new GestureDetector(getContext(), new SimpleGestureListener(getContext()));

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        return view;
    }

    public View.OnClickListener clickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            TextView textView = (TextView) v;
            String text = textView.getText().toString();

            switch (v.getId()){
                case R.id.shortcut1:
                    handleShortcutClick(text, "shortcut1");
                    break;
                case R.id.shortcut2:
                    handleShortcutClick(text, "shortcut2");
                    break;
                case R.id.shortcut3:
                    handleShortcutClick(text, "shortcut3");
                    break;
                case R.id.shortcut4:
                    handleShortcutClick(text, "shortcut4");
                    break;
            }
        }
    };

    public View.OnLongClickListener longClickListener = new View.OnLongClickListener(){

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()){
                case R.id.shortcut1:
                    launchAppPicker("shortcut1");
                    return true;
                case R.id.shortcut2:
                    launchAppPicker("shortcut2");
                    return true;
                case R.id.shortcut3:
                    launchAppPicker("shortcut3");
                    return true;
                case R.id.shortcut4:
                    launchAppPicker("shortcut4");
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2) {
            // app has been picked in app picker
            setUpShortcuts();
        }
    }

    protected void launchApp(String packageName) {
        Intent mIntent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            try {
                getContext().startActivity(mIntent);
            } catch (ActivityNotFoundException err) {
                Toast t = Toast.makeText(getContext().getApplicationContext(), "App was not found!", Toast.LENGTH_SHORT);
                t.show();
            }
        }
    }

    private void handleShortcutClick(String text, String shortcutNumber){
        if(text.equals("select")){
            launchAppPicker(shortcutNumber);
        }else {
            if (shortcutNumber.equals("shortcut1")){
                launchApp(shortcut1PackageName);
            }else if (shortcutNumber.equals("shortcut2")){
                launchApp(shortcut2PackageName);
            }else if (shortcutNumber.equals("shortcut3")){
                launchApp(shortcut3PackageName);
            }else if (shortcutNumber.equals("shortcut4")){
                launchApp(shortcut4PackageName);
            }
        }
    }

    private void launchAppPicker(String shortcutNumber){
        Log.i("shortcutNum", "from launcher" + shortcutNumber);
        Bundle bundle = new Bundle();
        bundle.putString("shortcutNumber", shortcutNumber);
        Intent appPickerIntent = new Intent(getActivity(), AppPicker.class);
        appPickerIntent.putExtras(bundle);
        startActivityForResult(appPickerIntent, 2);
    }

    private boolean containsApp(List<App> appList, String packageName){
        for (App app: appList){
            if (app.getPackageName().equals(packageName)){
                return true;
            }
        }
        return false;
    }

    public void setUpShortcuts(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(getContext().getPackageName(), getContext().MODE_PRIVATE);

        shortcut1PackageName = sharedPreferences.getString("shortcut1PackageName", null);
        shortcut2PackageName = sharedPreferences.getString("shortcut2PackageName", null);
        shortcut3PackageName = sharedPreferences.getString("shortcut3PackageName", null);
        shortcut4PackageName = sharedPreferences.getString("shortcut4PackageName", null);

        shortcut1AppName = sharedPreferences.getString("shortcut1AppName", null);
        shortcut2AppName = sharedPreferences.getString("shortcut2AppName", null);
        shortcut3AppName = sharedPreferences.getString("shortcut3AppName", null);
        shortcut4AppName = sharedPreferences.getString("shortcut4AppName", null);

        List<App> appList = AppListUtil.getAppsList(getContext());
        if(!containsApp(appList, shortcut1PackageName)){
            sharedPreferences.edit().remove("shortcut1PackageName").apply();
            sharedPreferences.edit().remove("shortcut1AppName").apply();
            shortcut1AppName = null;
            shortcut1PackageName = null;
        }

        if(!containsApp(appList, shortcut2PackageName)){
            sharedPreferences.edit().remove("shortcut2PackageName").apply();
            sharedPreferences.edit().remove("shortcut2AppName").apply();
            shortcut2AppName = null;
            shortcut2PackageName = null;
        }

        if(!containsApp(appList, shortcut3PackageName)){
            sharedPreferences.edit().remove("shortcut3PackageName").apply();
            sharedPreferences.edit().remove("shortcut3AppName").apply();
            shortcut3AppName = null;
            shortcut3PackageName = null;
        }

        if(!containsApp(appList, shortcut4PackageName)){
            sharedPreferences.edit().remove("shortcut4PackageName").apply();
            sharedPreferences.edit().remove("shortcut4AppName").apply();
            shortcut4AppName = null;
            shortcut4PackageName = null;
        }

        if(shortcut1AppName != null){
            shortcut1.setText(shortcut1AppName);
        }
        if(shortcut2AppName != null){
            shortcut2.setText(shortcut2AppName);
        }
        if(shortcut3AppName != null){
            shortcut3.setText(shortcut3AppName);
        }
        if(shortcut4AppName != null){
            shortcut4.setText(shortcut4AppName);
        }
    }

    public void determineSetDefaultLauncher(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getContext().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentLauncherName= resolveInfo.activityInfo.packageName;
        if(!currentLauncherName.equals("com.appallure.miny")){
            setDefaultLauncher.setVisibility(View.VISIBLE);
            setDefaultLauncher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        final Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                        startActivity(intent);
                    }
                    else {
                        final Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    }
                }
            });
        }else {
            setDefaultLauncher.setVisibility(View.GONE);
        }
    }
}