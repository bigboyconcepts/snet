package org.skynetsoftware.snet.example.app;

import android.app.Activity;

import org.skynetsoftware.snet.AndroidSmartRequestHandler;
import org.skynetsoftware.snet.SmartRequestHandler;

/**
 * Created by pedja on 2/17/17 10:30 AM.
 * This class is part of the snet
 * Copyright © 2017 ${OWNER}
 */

public class MySmartRequestHandler extends AndroidSmartRequestHandler
{
    public static final int REQUEST_CODE_PLACES = 1001;

    static
    {
        SmartRequestHandler.init(JSONParser.class, null);
        registerParserMethod(REQUEST_CODE_PLACES, "parsePlacesResponse");
    }

    public MySmartRequestHandler(Activity activity)
    {
        super(activity);
    }
}
