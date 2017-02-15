package org.skynetsoftware.snet;

import android.content.Context;

/**
 * Created by pedja on 23.5.16. 15.45.
 * This class is part of the ts-http
 * Copyright © 2016 ${OWNER}
 */
public class AndroidBootstrap
{
    private static boolean initialized;

    public static synchronized void initialize(Context context)
    {
        if(initialized)
        {
            throw new IllegalStateException("You can only initialize AndroidBootstrap once");
        }
        Network network = new AndroidNetwork(context.getApplicationContext());
        Internet internet = new AndroidInternet(context.getApplicationContext());
        UI ui = new AndroidUI(context.getApplicationContext());
        TextManager textManager = new AndroidTextManager(context.getApplicationContext());

        SNet.initialize(network, internet, ui, textManager);

        RequestManager.initialize(new AndroidRequestManager());

        initialized = true;
    }

}
