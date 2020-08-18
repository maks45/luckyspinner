package com.luckyspinner.app;

import android.app.Application;

import com.facebook.appevents.AppEventsLogger;
import com.onesignal.OneSignal;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        AppEventsLogger.activateApp(this);
    }
}
