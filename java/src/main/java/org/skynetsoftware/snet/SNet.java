package org.skynetsoftware.snet;

/**
 * Created by pedja on 26.6.15. 15.08.
 * This class is part of the snet
 * Copyright © 2015 ${OWNER}
 */
public class SNet
{
    /**
     * Debugging log tag
     * */
    public static final String LOG_TAG = "ts-http";
    public static boolean LOGGING = false;
    public static boolean PRINT_RESPONSE = false;

    private static SNet instance;

    private final Network network;
    private final Internet internet;
    private final UI ui;
    private final TextManager textManager;

    private SNet(Network network, Internet internet, UI ui, TextManager textManager)
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
        instance = new SNet(network, internet, ui, textManager);
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

    public static SNet getInstance()
    {
        if(instance == null)
        {
            throw new IllegalStateException("You must initialize TSHttp first. Call initialize()");
        }
        return instance;
    }

    public Network getNetwork()
    {
        return network;
    }

    public Internet getInternet()
    {
        return internet;
    }

    public UI getUi()
    {
        return ui;
    }

    public TextManager getTextManager()
    {
        return textManager;
    }
}
