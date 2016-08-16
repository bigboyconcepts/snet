package com.tehnicomsolutions.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.Nonnull;

/**
 * @author Predrag Čokulov
 */

public abstract class Internet
{
    /**
     * HTTP connection timeout
     * */
    public static int CONN_TIMEOUT = 2 * 60 * 1000;

    /**
     * URL encoding
     * */
    public static final String ENCODING = "UTF-8";

    private static final boolean printResponse = Http.LOGGING && true;

    protected static final String LINE_FEED = "\r\n";

    public Internet()
    {
    }

    public Response executeHttpRequest(@Nonnull Request request)
    {
        return executeHttpRequest(request, true);
    }

    /**
     * Executes HTTP POST request and returns response as string<br>
     * This method will not check if response code from server is OK ( &lt; 400)<br>
     *
     * @param request request builder object, used to build request. cannot be null
     * @param streamToString true if you want to convert input stream to string, if true you shouldn't try to use InputStream after
     * @return server response as string
     */
    public abstract Response executeHttpRequest(@Nonnull Request request, boolean streamToString);

    public static String readStreamToString(InputStream stream) throws IOException
    {
        MyTimer timer = new MyTimer();
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        StringBuilder string = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null)
        {
            string.append(line);
        }
        timer.log("Internet::readStreamToString");
        return string.toString();
    }


    public static class Response
    {
        public int code = -1;
        public String responseMessage;
        public String responseDetailedMessage;
        /**
         * On android this is input stream, on ios this is NSData*/
        public String responseDataString;
        public Object responseData;
        public String request;

        public boolean isResponseOk()
        {
            return code > 0 && code < 400;
        }

        @Override
        public String toString()
        {
            return "Response{" +
                    "code=" + code +
                    ", responseMessage='" + responseMessage + '\'' +
                    ", responseDetailedMessage='" + responseDetailedMessage + '\'' +
                    (printResponse ? ", responseDataString='" + responseDataString : "") + '\'' +
                    '}';
        }


    }
}