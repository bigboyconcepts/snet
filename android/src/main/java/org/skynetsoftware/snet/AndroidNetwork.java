package org.skynetsoftware.snet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by pedja on 3/12/16.
 */
public class AndroidNetwork extends Network
{
    private Context context;

    public AndroidNetwork(Context context)
    {
        if(context == null)
            throw new IllegalArgumentException("Context cannot be null");
        this.context = context.getApplicationContext();
    }

    @Override
    public  boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean isWiFiConnected()
    {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi != null && mWifi.isConnected();
    }
}
