package com.luckyspinner.app.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import com.facebook.FacebookSdk;
import com.facebook.applinks.AppLinkData;
import com.luckyspinner.app.R;
import com.luckyspinner.app.fragemnts.SlotFragment;
import com.luckyspinner.app.fragemnts.WebViewFragment;
import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {
    private SlotFragment slotFragment;
    private WebViewFragment webViewFragment;
    private AppEventsLogger logger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.setAutoInitEnabled(true);
        FacebookSdk.fullyInitialize();
        logger = AppEventsLogger.newLogger(this);
        slotFragment = new SlotFragment();
        webViewFragment = new WebViewFragment(() -> {
            changeFragment(slotFragment);
        }, AppLinkData.createFromAlApplinkData(getIntent()));
        changeFragment(webViewFragment);
    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(getApplication());
    }
}