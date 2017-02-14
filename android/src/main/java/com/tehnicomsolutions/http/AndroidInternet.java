package com.tehnicomsolutions.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Predrag Čokulov
 */

public class AndroidInternet extends Internet
{
    protected final Context context;

    public AndroidInternet(Context context)
    {
        if(context == null)
            throw new IllegalArgumentException("Context cannot be null");
        this.context = context.getApplicationContext();
    }

    @Override
    public Response executeHttpRequest(@NonNull Request request)
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
    @Override
    public Response executeHttpRequest(@NonNull Request request, boolean streamToString)
    {
        MyTimer timer = new MyTimer();
        Response response = new Response();
        InputStream is = null;
        try
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(request.getRequestUrl()).openConnection();
            //conn.setReadTimeout(10000 /* milliseconds */);
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
                    switch (request.getPostMethod())
                    {
                        case BODY:
                            if (request.getRequestBody() == null)
                                throw new IllegalArgumentException("body cannot be null if post method is BODY");
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
                            for (String key : request.getPOSTParams().keySet())
                            {
                                writer.append("--").append(BOUNDARY).append(LINE_FEED);
                                writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_FEED);
                                writer.append("Content-Type: text/plain; charset=" + ENCODING).append(LINE_FEED);
                                writer.append(LINE_FEED);
                                writer.append(request.getPOSTParams().get(key)).append(LINE_FEED);
                                writer.flush();
                            }

                            if (request.getFiles() != null)
                            {
                                for (int i = 0; i < request.getFiles().length; i++)
                                {
                                    Request.UploadFile file = request.getFiles()[i];

                                    writer.append("--").append(BOUNDARY).append(LINE_FEED);
                                    writer.append("Content-Disposition: form-data; name=\"").append(request.getFileParamName()).append(Integer.toString(i)).append("\"; filename=\"").append(file.fileName).append("\"").append(LINE_FEED);
                                    writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(file.fileName)).append(LINE_FEED);
                                    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                                    writer.append(LINE_FEED);
                                    writer.flush();

                                    InputStream inputStream = rescaleImageIfNecessary(file);
                                    if(inputStream == null)continue;

                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1)
                                    {
                                        os.write(buffer, 0, bytesRead);
                                    }
                                    os.flush();
                                    inputStream.close();

                                    writer.append(LINE_FEED);
                                    writer.flush();
                                }
                            }
                            writer.append(LINE_FEED).flush();
                            writer.append("--").append(BOUNDARY).append("--").append(LINE_FEED);
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
            if(streamToString)
            {
                response.responseDataString = readStreamToString(is = response.code < 400 ? conn.getInputStream() : conn.getErrorStream());
            }
            response.responseData = is = response.code < 400 ? conn.getInputStream() : conn.getErrorStream();
            response.responseMessage = response.code < 400 ? null : conn.getResponseMessage();
        }
        catch (IOException e)
        {
            response.responseMessage = context.getString(R.string.network_error);
            response.responseDetailedMessage = e.getMessage();
        }
        finally
        {
            response.request = request.getRequestUrl();
            if (Http.LOGGING)
                Log.d(Http.LOG_TAG, "executeHttpRequest[" + request.getRequestUrl() + "]: Took:'" + timer.get() + "', " + response);
            if (is != null)
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

    protected InputStream rescaleImageIfNecessary(Request.UploadFile file) throws FileNotFoundException
    {
        InputStream inputStream = createInputStreamFromUploadFile(file);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);

        int width = options.outWidth;
        int height = options.outHeight;
        int maxSize = /*Constants.IMAGE_MAX_SIZE*/1024;
        //if any dimension is larger than maxSize, do resize, to avod loading to large bitmap in memory
        if (width > maxSize || height > maxSize)
        {
            //first scale it down using inSampleSize power of 2
            int inSampleSize = 1;

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= maxSize && (halfWidth / inSampleSize) >= maxSize)
            {
                inSampleSize *= 2;
            }

            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            inputStream = createInputStreamFromUploadFile(file);//get the new inputstream

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

            if (bitmap != null)
            {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                //if any dimension is larger than maxSize, do resize
                if (width > maxSize || height > maxSize)
                {
                    //now scale it to target dimension
                    //check which dimension is larger, if they are equal any will do
                    boolean scaleByWidth = width > height;
                    float aspectRatio = scaleByWidth ? (float)width/(float)height : (float)height/(float)width;

                    //scale bitmap keeping aspect ratio
                    int diff = (scaleByWidth ? width : height) - maxSize;
                    width = scaleByWidth ? maxSize : (int) (width - diff / aspectRatio);
                    height = scaleByWidth ? (int) (height - diff / aspectRatio) : maxSize;
                    Bitmap tmp = Bitmap.createScaledBitmap(bitmap, width, height, false);
                    bitmap.recycle();
                    bitmap = tmp;
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                bitmap.recycle();
                byte[] bitmapData = bos.toByteArray();
                return new ByteArrayInputStream(bitmapData);
            }
        }
        //since we used input stream above, create new one here
        inputStream = createInputStreamFromUploadFile(file);
        return inputStream;
    }

    protected InputStream createInputStreamFromUploadFile(Request.UploadFile file) throws FileNotFoundException
    {
        if (file.uri.startsWith("content://"))
        {
            return context.getContentResolver().openInputStream(Uri.parse(file.uri));
        }
        else if (file.uri.startsWith("/"))
        {
            return new FileInputStream(new File(file.uri));
        }
        else if (file.uri.startsWith("file://"))
        {
            return new FileInputStream(new File(file.uri.replace("file://", "")));
        }
        return null;
    }

    protected static void setXWwwFormUrlEncodedParams(HttpURLConnection conn, Request requestBuilder) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        for (String key : requestBuilder.getPOSTParams().keySet())
        {
            builder.append("&").append(key).append("=").append(HttpUtility.encodeString(requestBuilder.getPOSTParams().get(key)));
        }
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(builder.toString().getBytes().length));
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(builder.toString());
        wr.flush();
        wr.close();
    }
}