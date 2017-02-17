package org.skynetsoftware.snet.example.app;

import org.json.JSONObject;
import org.skynetsoftware.snet.Internet;
import org.skynetsoftware.snet.ResponseParser;


/**
 * Created by pedja on 3.7.15. 10.38.
 * This class is part of the SBBet
 * Copyright © 2015 ${OWNER}
 */
public class JSONParser extends ResponseParser
{
    private JSONObject jsonObject;

    public JSONParser(String stringObject)
    {
        super(stringObject);
        init();
    }

    public JSONParser(Internet.Response serverResponse)
    {
        super(serverResponse);
        init();
    }

    private void init()
    {
        try
        {
            jsonObject = new JSONObject(this.serverResponse.responseDataString);
            if (!serverResponse.isResponseOk())
            {
                jsonObject = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void parsePlacesResponse()
    {
        if(jsonObject == null)
            return;
        System.out.println("parsePlacesResponse");
    }
}

