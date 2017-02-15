package org.skynetsoftware.snet;

import android.os.AsyncTask;

/**
 * Created by pedja on 1/22/14 10.11.
 * This class is part of the snet
 * Copyright © 2014 ${OWNER}
 * @author Predrag Čokulov
 */
public interface ATListener
{
    /**
     * @param requestCode for this task
     * @see AsyncTask#doInBackground(Object[])
     * @return Result
     * */
    ResponseParser doInBackground(int requestCode);

    /**
     * @param requestCode for this task
     * @param result Result
     * @see AsyncTask#onPostExecute(Object)
     * */
    void onPostExecute(int requestCode, ResponseParser result);

    /**
     * @param requestCode for this task
     * @see AsyncTask#onPreExecute()
     * */
    void onPreExecute(int requestCode);

    /**
     * @param requestCode for this task
     * @param result Result
     * @see AsyncTask#onCancelled(Object)
     * */
    void onCancelled(int requestCode, ResponseParser result);
}
