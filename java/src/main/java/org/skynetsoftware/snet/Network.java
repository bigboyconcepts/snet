package org.skynetsoftware.snet;

/**
 * @author Predrag Čokulov*/

public abstract class Network
{
    /**Check if network is enabled. This only checks if device is connected, not if it actually has internet access
     * @return true if network is available, false if not*/
    public abstract boolean isNetworkAvailable();

    /**
     * Check if wifi is connected
     * @return true if wifi is connected, false if not*/
    public abstract boolean isWiFiConnected();

    /**
     * Get current network state. One of: {@link NETWORK_STATE#OFFLINE}, {@link NETWORK_STATE#ONLINE_3G} or {@link NETWORK_STATE#ONLINE_WIFI}
     * @return {@link NETWORK_STATE}*/
    public NETWORK_STATE getNetworkState()
    {
        if(isWiFiConnected())
        {
            return NETWORK_STATE.ONLINE_WIFI;
        }
        else if(isNetworkAvailable())
        {
            return NETWORK_STATE.ONLINE_3G;
        }
        else
        {
            return NETWORK_STATE.OFFLINE;
        }
    }

    enum NETWORK_STATE
    {
        ONLINE_3G, ONLINE_WIFI, OFFLINE
    }
}
