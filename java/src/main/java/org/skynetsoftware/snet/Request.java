package org.skynetsoftware.snet;

import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Created by pedja on 2/21/14 10.17.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 *
 * RequestBuilder is used for building http requests
 *
 * @author Predrag Čokulov
 */

public class Request implements Serializable
{
    private static String DEFAULT_REQUEST_ROOT = null;

    /**
     * HTTP method used for this request*/
    public enum Method
    {
        POST, GET, PUT, DELETE,
    }

    /**
     * Method for making HTTP POST request*/
    public enum PostMethod
    {
        /**
         * Data will be sent as raw bytes(or text), you should specify Content-Type if you use this method<br>
         * You must set data with {@link #setRequestBody(String)}*/
        BODY,

        /**
         * This is used for normal HTTP posts, when you just need to send text to server<br>
         * This is default method*/
        X_WWW_FORM_URL_ENCODED,

        /**
         * This mus be used if you are sending file to the server*/
        FORM_DATA
    }

    private StringBuilder builder;
    private HashMap<String, String> postParams = new HashMap<>();
    private HashMap<String, String> urlParams = new HashMap<>();

    @Nonnull
    private final Method mMethod;

    /**
     * */
    private String requestUrl = DEFAULT_REQUEST_ROOT;

    /**
     * Method for POST request<br>
     * For now either {@link PostMethod#BODY}, {@link PostMethod#X_WWW_FORM_URL_ENCODED} or {@link PostMethod#FORM_DATA}<br>
     * Cannot be null<br>
     * Default is {@link PostMethod#X_WWW_FORM_URL_ENCODED}*/
    @Nonnull
    private PostMethod postMethod = PostMethod.X_WWW_FORM_URL_ENCODED;

    /**
     * Content type for request<br>
     * Only used if PostMethod is {@link PostMethod#BODY}'*/
    private String contentType = "text/plain";

    /**
     * Raw body if post method is {@link PostMethod#BODY}*/
    @Nullable
    private String requestBody;

    /**
     * Post parameter name for file upload<br>
     * Used only with {@link PostMethod#FORM_DATA}<br>*/
    private String fileParamName = "file";

    /**
     * Array of files to upload to server<br>
     * Used only with {@link PostMethod#FORM_DATA}<br>*/
    @Nullable
    private UploadFile[] files;

    private transient int maxRetries = 3;
    public int retriesLeft = maxRetries;

    private boolean mAllowEmptyValues;

    private org.skynetsoftware.snet.ResponseHandler.ResponseMessagePolicy responseMessagePolicy = ResponseHandler.DEFAULT_RESPONSE_MESSAGE_POLICY;

    private HashMap<String, String> headers = new HashMap<>();

    private org.skynetsoftware.snet.RequestHandler mRequestHandler;

    /**
     * Create new request builder<br>
     * If no other option is specified default options are:
     * <pre>
     * * requestUrl - {@link #DEFAULT_REQUEST_ROOT}
     * * postMethod - {@link PostMethod#X_WWW_FORM_URL_ENCODED}
     * * contentType - "text/plain"
     * </pre>
     * @param method HTTP method for this request*/
    public Request(@Nonnull Method method)
    {
        mMethod = method;
        builder = new StringBuilder();
    }

    /**Set parameter name for file that is uploading to server.
     * @param fileParamName name of the file param
     * @return this obejct for method chaining*/
    public Request setFileParamName(String fileParamName)
    {
        this.fileParamName = fileParamName;
        return this;
    }

    /**
     * Set request body.<br>
     * This is only used with {@link PostMethod#BODY}
     * @param body body for this request
     * @return this object for method chaining*/
    public Request setRequestBody(@Nullable String body)
    {
        this.requestBody = body;
        if(!isEmpty(requestBody))
            setPostMethod(PostMethod.BODY);
        return this;
    }

    /**
     * Set request content-type.<br>
     * This is only used with {@link PostMethod#BODY}
     * @param contentType content type for for request (application/json)
     * @return this object for method chaining*/
    public Request setContentType(String contentType)
    {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set HTTP POST method to use
     * @param postMethod HTTP POST method
     * @return this object for method chaining*/
    public Request setPostMethod(@Nonnull PostMethod postMethod)
    {
        this.postMethod = postMethod;
        if(postMethod != PostMethod.BODY && !isEmpty(requestBody))
        {
            if(SNet.LOGGING) Log.w(SNet.LOG_TAG, "Warning. requestBody is not empty, it will be ignored since new PostMethod is not PostMethod.BODY");
        }
        if(postMethod != PostMethod.FORM_DATA && (files != null && files.length > 0))
        {
            if(SNet.LOGGING)Log.w(SNet.LOG_TAG, "Warning. Files will be ignored since new PostMethod is not PostMethod.FORM_DATA");
        }
        return this;
    }

    /**
     * Set list of files for uploading
     * @param files list of files to upload
     * @see UploadFile
     * @return same object for method chaining*/
    public Request setFiles(@Nullable UploadFile... files)
    {
        this.files = files;
        if(files != null && files.length > 0)
            setPostMethod(PostMethod.FORM_DATA);
        return this;
    }

    /**
     * Set url root for this request. url on which all params will be added. for example<br>
     * <b>http://www.example.com/api.json/</b>users/register
     * Bolded is url that you should specify here
     * @param url url as described above
     * @return this object for method chaining*/
    public Request setRequestUrl(String url)
    {
        requestUrl = url;
        return this;
    }

    /**
     * Set maximum number of retries if request fails due to network or other errors
     * @param maxRetries max retries
     * @return this object for method chaining*/
    public Request setMaxRetires(int maxRetries)
    {
        this.maxRetries = maxRetries;
        this.retriesLeft = maxRetries;
        return this;
    }

    /**
     * @return Max possible retries
     * */
    public int getMaxRetries()
    {
        return maxRetries;
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. Passing null or "" will not set parameter if mAllowEmptyValues is false
     * @return same object for method chaining
     * */
    public Request addParam(String key, String value)
    {
        return addParam(key, value, false);
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @return same object for method chaining
     * */
    public Request addParam(String key, int value)
    {
        return addParam(key, Integer.toString(value));
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @return same object for method chaining
     * */
    public Request addParam(String key, long value)
    {
        return addParam(key, Long.toString(value));
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @return same object for method chaining
     * */
    public Request addParam(String key, float value)
    {
        return addParam(key, Float.toString(value));
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @return same object for method chaining
     * */
    public Request addParam(String key, double value)
    {
        return addParam(key, Double.toString(value));
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @return same object for method chaining
     * */
    public Request addParam(String key, boolean value)
    {
        return addParam(key, Boolean.toString(value));
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @return same object for method chaining
     * */
    public Request addParam(String key, short value)
    {
        return addParam(key, Short.toString(value));
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @return same object for method chaining
     * */
    public Request addParam(String key, byte value)
    {
        return addParam(key, Byte.toString(value));
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @return same object for method chaining
     * */
    public Request addParam(String key, Object value)
    {
        return addParam(key, value == null ? null : value.toString());
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @param forceAddToUrl if true, params will be added to url regardless of the HTTP method
     * @return same object for method chaining
     * */
    public Request addParam(String key, byte value, boolean forceAddToUrl)
    {
        return addParam(key, Byte.toString(value), forceAddToUrl);
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @param forceAddToUrl if true, params will be added to url regardless of the HTTP method
     * @return same object for method chaining
     * */
    public Request addParam(String key, int value, boolean forceAddToUrl)
    {
        return addParam(key, Integer.toString(value), forceAddToUrl);
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @param forceAddToUrl if true, params will be added to url regardless of the HTTP method
     * @return same object for method chaining
     * */
    public Request addParam(String key, long value, boolean forceAddToUrl)
    {
        return addParam(key, Long.toString(value), forceAddToUrl);
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @param forceAddToUrl if true, params will be added to url regardless of the HTTP method
     * @return same object for method chaining
     * */
    public Request addParam(String key, float value, boolean forceAddToUrl)
    {
        return addParam(key, Float.toString(value), forceAddToUrl);
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @param forceAddToUrl if true, params will be added to url regardless of the HTTP method
     * @return same object for method chaining
     * */
    public Request addParam(String key, double value, boolean forceAddToUrl)
    {
        return addParam(key, Double.toString(value), forceAddToUrl);
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @param forceAddToUrl if true, params will be added to url regardless of the HTTP method
     * @return same object for method chaining
     * */
    public Request addParam(String key, short value, boolean forceAddToUrl)
    {
        return addParam(key, Short.toString(value), forceAddToUrl);
    }

    /**
     * Add parameters for request
     * Encoding of parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter
     * @param forceAddToUrl if true, params will be added to url regardless of the HTTP method
     * @return same object for method chaining
     * */
    public Request addParam(String key, Object value, boolean forceAddToUrl)
    {
        return addParam(key, value == null ? null : value.toString(), forceAddToUrl);
    }

    /**
     * Add parameters for request
     * Encoding parameter is taken care of
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. Passing null or "" will not set parameter
     * @param forceAddToUrl if true, params will be added to url regardless of the HTTP method
     * @return same object for method chaining
     * */
    public Request addParam(String key, String value, boolean forceAddToUrl)
    {
        if(isEmpty(key) || (!mAllowEmptyValues && isEmpty(value)))
        {
            if(SNet.LOGGING)Log.e(SNet.LOG_TAG, "RequestBuilder >> addParam : param not set");
            return this;
        }
        if(forceAddToUrl || mMethod == Method.GET || mMethod == Method.DELETE)
        {
            urlParams.put(key, SNetUtils.encodeString(value));
        }
        else if(mMethod == Method.POST || mMethod == Method.PUT)
        {
            postParams.put(key, value);
        }
        return this;
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(String part)
    {
        return addParam(part);
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(int part)
    {
        return addParam(Integer.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(long part)
    {
        return addParam(Long.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(short part)
    {
        return addParam(Short.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(float part)
    {
        return addParam(Float.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(double part)
    {
        return addParam(Double.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(boolean part)
    {
        return addParam(Boolean.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(byte part)
    {
        return addParam(Byte.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(Object part)
    {
        return addParam(part == null ? null : part.toString());
    }

    /**
     * This method is deprecated, use {@link #addUrlPart(String)} instead
     * Add parameters for request
     * @param value url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    @Deprecated
    public Request addParam(String value)
    {
        if(value == null || value.isEmpty())
        {
            if(SNet.LOGGING)Log.e(SNet.LOG_TAG, "RequestBuilder >> addParam : param not set");
            return this;
        }
        if(builder.length() == 0)
        {
            if(requestUrl != null && !requestUrl.endsWith("/"))
                builder.append("/");
        }
        else if(!builder.toString().endsWith("/"))
        {
            builder.append("/");
        }
        builder.append(value.startsWith("/") ? value.substring(1, value.length()) : value);
        return this;
    }

    /**
     * Add HTTP request header
     * @param header key for this heade (Content-Type)
     * @param value value for this header (application/json)
     * @return this for method chaining*/
    public Request addHeader(String header, String value)
    {
        headers.put(header, value);
        return this;
    }

    /**
     * Get all user specified request headers. This will not return any header added by underlying http client implementation
     * @return headers for this RequestBuilder*/
    public HashMap<String, String> getHeaders()
    {
        return headers;
    }

    /**
     * @return generate url parameters string. eg. username=pedja&amp;password=123456
     * */
    public String getUrlParams()
    {
        String urlParts = builder.toString();
        StringBuilder tmpBuilder = new StringBuilder();
        for(String key : this.urlParams.keySet())
        {
            if(!tmpBuilder.toString().contains("?"))
                tmpBuilder.append("?");
            else
                tmpBuilder.append("&");
            tmpBuilder.append(key).append("=").append(urlParams.get(key));
        }
        return urlParts == null ? "" : urlParts + tmpBuilder.toString();
    }

    public HashMap<String, String> getPOSTParams()
    {
        return postParams;
    }

    /**
     * Get request url including url parameters
     * @return request url for this request builder*/
    public String getRequestUrl()
    {
        return requestUrl + getUrlParams();
    }

    /**
     * Get the response message policy for this request
     * @return response message policy. Null is returned only if policy is explicitly set to null*/
    public org.skynetsoftware.snet.ResponseHandler.ResponseMessagePolicy getResponseMessagePolicy()
    {
        return responseMessagePolicy;
    }

    /**
     * Set the ResponseMessagePolicy
     * @see org.skynetsoftware.snet.ResponseHandler.ResponseMessagePolicy
     * @param responseMessagePolicy policy
     * @return this for call chain*/
    public Request setResponseMessagePolicy(org.skynetsoftware.snet.ResponseHandler.ResponseMessagePolicy responseMessagePolicy)
    {
        this.responseMessagePolicy = responseMessagePolicy;
        return this;
    }

    public Method getMethod()
    {
        return mMethod;
    }

    @Nonnull
    public PostMethod getPostMethod()
    {
        return postMethod;
    }

    public String getContentType()
    {
        return contentType;
    }

    @Nullable
    public String getRequestBody()
    {
        return requestBody;
    }

    @Nullable
    public UploadFile[] getFiles()
    {
        return files;
    }

    public String getFileParamName()
    {
        return fileParamName;
    }

    /**
     * Sets the default request url. It is used only if request url is not specified
     * @see #setRequestUrl(String)
     * @param url default request url*/
    public static void setDefaultRequestUrl(String url)
    {
        DEFAULT_REQUEST_ROOT = url;
    }

    public String getParam(String key)
    {
        String urlParam = urlParams.get(key);
        if(urlParam != null)
            return urlParam;
        String postParam = postParams.get(key);
        if(postParam != null)
            return postParam;
        return null;
    }

    public void setAllowEmptyValues(boolean allowEmptyValues)
    {
        this.mAllowEmptyValues = allowEmptyValues;
    }

    public org.skynetsoftware.snet.RequestHandler getRequestHandler()
    {
        return mRequestHandler;
    }

    public void setRequestHandler(org.skynetsoftware.snet.RequestHandler requestHandler)
    {
        this.mRequestHandler = requestHandler;
    }

    /**
     * Wrapper for file that sould be uploaded<br>
     * Used only with {@link PostMethod#BODY}*/
    public static class UploadFile
    {
        /**
         * Uri of the image for uploading file to server*/

        @Nonnull
        private String uri;

        /**
         * Name of the file*/
        private String fileName;

        /**
         * Mime type of file for uploading file to server<br>*/
        private String mimeType;

        /**
         * <pre>
         * If > 0 image will be rescaled to be equal or less than specified value
         *
         * </pre>*/
        private int maxImageSize;

        @Nonnull
        public String getUri()
        {
            return uri;
        }

        public void setUri(@Nonnull String uri)
        {
            this.uri = uri;
        }

        public String getFileName()
        {
            return fileName;
        }

        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }

        public String getMimeType()
        {
            return mimeType;
        }

        public void setMimeType(String mimeType)
        {
            this.mimeType = mimeType;
        }

        public int getMaxImageSize()
        {
            return maxImageSize;
        }

        public void setMaxImageSize(int maxImageSize)
        {
            this.maxImageSize = maxImageSize;
        }
    }

    private static boolean isEmpty(@Nullable CharSequence str)
    {
        return str == null || str.length() == 0;
    }

}
