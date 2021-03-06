package org.skynetsoftware.snet;

import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Predrag Čokulov
 */

public class Internet
{
    /**
     * HTTP connection timeout
     */
    public static int CONN_TIMEOUT = 2 * 60 * 1000;

    /**
     * HTTP connection timeout
     */
    public static int READ_TIMEOUT = 2 * 60 * 1000;

    /**
     * URL encoding
     */
    public static final String ENCODING = "UTF-8";

    private static final boolean PRINT_RESPONSE = SNet.LOGGING && SNet.PRINT_RESPONSE;

    protected static final String MULTI_PART_LINE_FEED = "\r\n";

    public Response executeHttpRequest(@NonNull Request request)
    {
        return executeHttpRequest(request, true);
    }

    /**
     * Executes HTTP POST request and returns response as string<br>
     * This method will not check if response code from server is OK ( &lt; 400)<br>
     *
     * @param request        request builder object, used to build request. cannot be null
     * @param streamToString true if you want to convert input stream to string, if true you shouldn't try to use InputStream after
     * @return server response as string
     */
    public Response executeHttpRequest(@NonNull Request request, boolean streamToString)
    {
        MyTimer timer = new MyTimer();
        Response response = new Response();
        InputStream is = null;
        try
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(request.getRequestUrl()).openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setRequestMethod(request.getMethod().toString());
            conn.setDoInput(true);

            for (String key : request.getHeaders().keySet())
            {
                conn.setRequestProperty(key, request.getHeaders().get(key));
            }

            switch (request.getMethod())
            {
                case PUT:
                    break;
                case POST:
                    conn.setDoOutput(true);
                    switch (request.getBodyType())
                    {
                        case RAW:
                            if (request.getRequestBody() == null)
                                throw new IllegalArgumentException("body cannot be null if post method is RAW");
                            conn.setRequestProperty("Content-Type", request.getContentType());
                            conn.setRequestProperty("Content-Length", Integer.toString(request.getRequestBody().getBytes().length));
                            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                            wr.writeBytes(request.getRequestBody());
                            wr.flush();
                            wr.close();
                            break;
                        case X_WWW_FORM_URL_ENCODED:
                            setXWwwFormUrlEncodedParams(conn, request);
                            break;
                        case FORM_DATA:
                            final String BOUNDARY = "===" + System.currentTimeMillis() + "===";
                            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                            OutputStream os = conn.getOutputStream();
                            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, ENCODING), true);
                            for (Request.Param param : request.getBodyParams())
                            {
                                writer.append("--").append(BOUNDARY).append(MULTI_PART_LINE_FEED);
                                writer.append("Content-Disposition: form-data; name=\"").append(param.getKey()).append("\"").append(MULTI_PART_LINE_FEED);
                                writer.append("Content-Type: text/plain; charset=" + ENCODING).append(MULTI_PART_LINE_FEED);
                                writer.append(MULTI_PART_LINE_FEED);
                                writer.append(param.getValue()).append(MULTI_PART_LINE_FEED);
                                writer.flush();
                            }

                            if (request.getFiles() != null)
                            {
                                for (int i = 0; i < request.getFiles().length; i++)
                                {
                                    Request.UploadFile file = request.getFiles()[i];

                                    writer.append("--").append(BOUNDARY).append(MULTI_PART_LINE_FEED);
                                    writer.append("Content-Disposition: form-data; name=\"").append(file.getFileParamName()).append(Integer.toString(i)).append("\"; filename=\"").append(file.getFileName()).append("\"").append(MULTI_PART_LINE_FEED);
                                    writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(file.getFileName())).append(MULTI_PART_LINE_FEED);
                                    writer.append("Content-Transfer-Encoding: binary").append(MULTI_PART_LINE_FEED);
                                    writer.append(MULTI_PART_LINE_FEED);
                                    writer.flush();

                                    InputStream inputStream;
                                    if(file.getMaxImageSize() > 0)
                                    {
                                        inputStream = rescaleImageIfNecessary(file);

                                        //if rescaleImageIfNecessary returns false, it is possible that file is not image, try to just upload it
                                        //TODO read mime type and decide if rescale should even be called (if file is image or not)
                                        if (inputStream == null)
                                            inputStream = createInputStreamFromUploadFile(file);
                                    }
                                    else
                                    {
                                        inputStream = createInputStreamFromUploadFile(file);
                                    }
                                    if (inputStream == null) continue;

                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1)
                                    {
                                        os.write(buffer, 0, bytesRead);
                                    }
                                    os.flush();
                                    inputStream.close();
                                    //TODO stream could end up not closed

                                    writer.append(MULTI_PART_LINE_FEED);
                                    writer.flush();
                                }
                            }
                            writer.append(MULTI_PART_LINE_FEED).flush();
                            writer.append("--").append(BOUNDARY).append("--").append(MULTI_PART_LINE_FEED);
                            writer.close();

                            os.close();
                            break;
                    }
                    break;
                case GET:
                    break;
                case DELETE:
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    break;
            }

            conn.connect();

            response.code = conn.getResponseCode();
            if (streamToString)
            {
                response.responseDataString = readStreamToString(is = response.code < 400 ? conn.getInputStream() : conn.getErrorStream());
            }
            response.responseData = is = response.code < 400 ? conn.getInputStream() : conn.getErrorStream();
            response.responseMessage = response.code < 400 ? null : conn.getResponseMessage();
        }
        catch (IOException e)
        {
            response.exception = e;
            response.responseMessage = SNet.getInstance().getTextManager().getText("network_error");
            response.responseDetailedMessage = e.getMessage();
        }
        finally
        {
            response.request = request.getRequestUrl();
            if (SNet.LOGGING)
                Log.d(SNet.LOG_TAG, "executeHttpRequest[" + request.getRequestUrl() + "]: Took:'" + timer.get() + "', " + response);
            if (is != null && streamToString)
            {
                try
                {
                    is.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }

        return response;
    }

    /**
     * Rescale image if {@link Request.UploadFile#maxImageSize} > 0*/
    protected InputStream rescaleImageIfNecessary(Request.UploadFile file) throws IOException
    {
        throw new IllegalStateException("rescaleImageIfNecessary has no default implementation yet, will be done soon");
    }

    /**
     * Set parameters for {@link Request.BodyType#X_WWW_FORM_URL_ENCODED}*/
    protected static void setXWwwFormUrlEncodedParams(HttpURLConnection conn, Request requestBuilder) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        for (Request.Param param : requestBuilder.getBodyParams())
        {
            builder.append("&").append(param.getKey()).append("=").append(SNetUtils.encodeString(param.getValue()));
        }
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(builder.toString().getBytes().length));
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(builder.toString());
        wr.flush();
        wr.close();
    }

    /**
     * Read {@link InputStream} to {@link String}*/
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

    /**
     * Create {@link InputStream} from {@link Request.UploadFile}*/
    protected InputStream createInputStreamFromUploadFile(Request.UploadFile file) throws FileNotFoundException
    {
        if (file.getUri().startsWith("/"))
        {
            return new FileInputStream(new File(file.getUri()));
        }
        else if (file.getUri().startsWith("file://"))
        {
            return new FileInputStream(new File(file.getUri().replace("file://", "")));
        }
        return null;
    }

    /**
     * Convenient method for closing multiple {@link Closeable} Objects*/
    protected static void close(Closeable... closeables)
    {
        for (Closeable c : closeables)
        {
            if(c == null)
                continue;
            try
            {
                c.close();
            }
            catch (Throwable ignore)
            {
            }
        }
    }

    public static class Response
    {
        /**
         * HTTP response code*/
        public int code = -1;

        /**
         * HTTP response code, eg 'OK' for code 200*/
        public String responseMessage;

        /**
         * Mostly Exception message, Exception.getMessage()*/
        public String responseDetailedMessage;

        /**
         * If exception occurred, this is the exception*/
        public Throwable exception;

        /**
         * String content from responseData*/
        public String responseDataString;

        /**
         * On android/java this is InputStream, on iOS this is NSData
         */
        public Object responseData;

        /**
         * Request url*/
        public String request;

        /**
         * Checks if response is OK, if code is > 0 and code < 400 then response is OK*/
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
                    (", responseDetailedMessage='" + responseDetailedMessage) + '\'' +
                    (exception != null ? ", exception='" + exception.toString() : "") + '\'' +
                    (PRINT_RESPONSE ? ", responseDataString='" + responseDataString : "") + '\'' +
                    '}';
        }


    }
}