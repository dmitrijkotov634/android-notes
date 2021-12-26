package com.dm.notes;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class NotesApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
