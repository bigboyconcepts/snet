package com.tehnicomsolutions.http;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by pedja on 2/21/14 10.17.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 *
 * This is the core of ts-http. You will use this class for all http requests
 *
 * @author Predrag Čokulov
 */
public class AndroidRequestManager extends RequestManager
{
    private Handler uiHandler;

    public AndroidRequestManager()
    {
        super();
        uiHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void runOnUIThread(Runnable runnable)
    {
        uiHandler.post(runnable);
    }
}
