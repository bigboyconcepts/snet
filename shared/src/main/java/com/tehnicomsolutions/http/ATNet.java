package com.tehnicomsolutions.http;


import android.os.AsyncTask;

import java.util.HashMap;


/**
 * Created by pedja on 1/22/14 10.11.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 * @author Predrag Čokulov
 */
public class ATNet extends AsyncTask<Void, Void, ResponseParser>
{
    private ATListener atListener;
    private final int requestCode;
    public static final HashMap<Integer, AsyncTask> activeTasks = new HashMap<>();

    private NoInternetConnectionHandler noInternetConnectionHandler;

    public ATNet(ATListener atListener, int requestCode)
    {
        this.atListener = atListener;
        this.requestCode = requestCode;
        if(atListener == null)
        {
            throw new IllegalArgumentException("ATListener must not be null");
        }
    }

    /**
     * Calls {@link #atListener#doInBackground(int, Object[])}
     * */
    @Override
    protected ResponseParser doInBackground(Void... params)
    {
        return atListener.doInBackground(requestCode);
    }

    /**
     * Calls {@link #atListener#onPostExecute(Object)}
     * */
    @Override
    protected void onPostExecute(ResponseParser result)
    {
        if(atListener != null) atListener.onPostExecute(requestCode, result);
        activeTasks.remove(requestCode);
    }

    /**
     * Calls {@link #atListener#onPreExecute()}
     * */
    @Override
    protected void onPreExecute()
    {
        if(!Http.getInstance().getNetwork().isNetworkAvailable())//if we don't have internet connection prevent task from starting and notify user
        {
            if(noInternetConnectionHandler != null)noInternetConnectionHandler.handleNoInternetConnection();
            cancel(true);
            return;
        }
        if(atListener != null) atListener.onPreExecute(requestCode);
    }

    @Override
    protected void onCancelled(ResponseParser result)
    {
        super.onCancelled(result);
        activeTasks.remove(requestCode);
        atListener.onCancelled(requestCode, result);
    }

    public ATNet execute()
    {
        AsyncTask task = activeTasks.get(requestCode);
        if(task != null)
        {
            task.cancel(true);
        }
        activeTasks.put(requestCode, this);
        super.executeOnExecutor(THREAD_POOL_EXECUTOR);
        return this;
    }

    public int getRequestCode()
    {
        return requestCode;
    }

    public void setNoInternetConnectionHandler(NoInternetConnectionHandler noInternetConnectionHandler)
    {
        this.noInternetConnectionHandler = noInternetConnectionHandler;
    }
}
