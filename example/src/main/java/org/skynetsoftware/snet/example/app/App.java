package org.skynetsoftware.snet.example.app;

import android.app.Application;

import org.skynetsoftware.snet.AndroidBootstrap;

/**
 * Created by pedja on 2/15/17.
 */

public class App extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        AndroidBootstrap.initialize(this);
    }
}
