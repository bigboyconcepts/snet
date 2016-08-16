package com.tehnicomsolutions.http;

/**
 * Created by pedja on 26.6.15. 15.08.
 * This class is part of the ts-http
 * Copyright © 2015 ${OWNER}
 */
public class Http
{
    /**
     * Debugging log tag
     * */
    public static final String LOG_TAG = "ts-http";
    public static boolean LOGGING = false;

    private static Http instance;

    public final Network network;
    public final Internet internet;
    public final UI ui;
    public final TextManager textManager;

    private Http(Network network, Internet internet, UI ui, TextManager textManager)
    {
        this.network = network;
        this.internet = internet;
        this.ui = ui;
        this.textManager = textManager;
    }

    /**
     * Call this only once to initialize ts-http<br>
     * */
    public static void initialize(Network network, Internet internet, UI ui, TextManager textManager)
    {
        assertNetwork(network);
        assertInternet(internet);
        assertUi(ui);
        assertTm(textManager);
        instance = new Http(network, internet, ui, textManager);
    }

    private static void assertInternet(Internet internet)
    {
        if(internet == null)
            throw new IllegalArgumentException("Internet cannot be null");
    }

    private static void assertNetwork(Network network)
    {
        if(network == null)
            throw new IllegalArgumentException("Network cannot be null");
    }

    private static void assertUi(UI ui)
    {
        if(ui == null)
            throw new IllegalArgumentException("UI cannot be null");
    }

    private static void assertTm(TextManager tm)
    {
        if(tm == null)
            throw new IllegalArgumentException("TextManager cannot be null");
    }

    public static Http getInstance()
    {
        if(instance == null)
        {
            throw new IllegalStateException("You must initialize TSHttp first. Call initialize()");
        }
        return instance;
    }
}
