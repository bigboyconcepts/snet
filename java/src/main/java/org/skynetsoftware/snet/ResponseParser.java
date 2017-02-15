package org.skynetsoftware.snet;

/**
 * Created by pedja on 1/22/14.
 *
 * Default response parser, with basic functionality, you will probably want to extend this to do your stuff
 * @author Predrag Čokulov
 */
public class ResponseParser
{
    /**
     * success - server returned http code &lt; 400 and response is also ok */
    public static final int RESPONSE_STATUS_SUCCESS = 0;

    /**
     * server error - server returned http code &gt;= 400*/
    public static final int RESPONSE_STATUS_SERVER_ERROR = 1;

    /**
     * response error - response wasn't valid in any way*/
    public static final int RESPONSE_STATUS_RESPONSE_ERROR = 2;

    /**
     * client error - client wasnt able to reach server (network error, server unavailable...)*/
    public static final int RESPONSE_STATUS_CLIENT_ERROR = 3;

    /**
     * response cancelled - response was cancelled*/
    public static final int RESPONSE_STATUS_CANCELLED = 4;

    protected final org.skynetsoftware.snet.Internet.Response serverResponse;

    protected Object parseObject;

    public ResponseParser(String stringObject)
    {
        this.serverResponse = new org.skynetsoftware.snet.Internet.Response();
        serverResponse.code = 200;
        serverResponse.request = null;
        serverResponse.responseDataString = stringObject;
        serverResponse.responseMessage = null;
    }

    public ResponseParser(org.skynetsoftware.snet.Internet.Response serverResponse)
    {
        this.serverResponse = serverResponse;
    }

    @SuppressWarnings("unchecked")
    /**
     * return object parsed from this server response. You need to manually set this*/
    public <T> T getParseObject()
    {
        return (T) parseObject;
    }

    @SuppressWarnings("unchecked")
    /**
     * return object parsed from this server response. You need to manually set this*/
    public <T> T getParseObject(Class<T> type)
    {
        return (T) parseObject;
    }

    public org.skynetsoftware.snet.Internet.Response getServerResponse()
    {
        return serverResponse;
    }

    /**
     * @return response status, default implementation doesn't check response data just http code*/
    public int getResponseStatus()
    {
        if(serverResponse.code < 0)
            return RESPONSE_STATUS_CLIENT_ERROR;
        else if(serverResponse.code < 400)
            return RESPONSE_STATUS_SUCCESS;
        else
            return RESPONSE_STATUS_SERVER_ERROR;
    }

    public String getResponseMessage()
    {
        return serverResponse.responseMessage;
    }
}
