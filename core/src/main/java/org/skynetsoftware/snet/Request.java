package org.skynetsoftware.snet;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


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
    private static String DEFAULT_REQUEST_URL = null;

    /**
     * HTTP method used for this request*/
    public enum Method
    {
        POST, GET, PUT, DELETE,
    }

    /**
     * Method for making HTTP POST request*/
    public enum BodyType
    {
        /**
         * Data will be sent as raw bytes(or text), you should specify Content-Type if you use this method<br>
         * You must set data with {@link #setRequestBody(String)}*/
        RAW,

        /**
         * This is used for normal HTTP posts, when you just need to send text to server<br>
         * This is default method*/
        X_WWW_FORM_URL_ENCODED,

        /**
         * This mus be used if you are sending file to the server*/
        FORM_DATA
    }

    private StringBuilder mUrlBuilder;
    private List<Param> mParams = new ArrayList<>();

    @NonNull
    private final Method mMethod;

    /**
     * */
    private String mRequestUrl = DEFAULT_REQUEST_URL;

    /**
     * Method for POST request<br>
     * For now either {@link BodyType#RAW}, {@link BodyType#X_WWW_FORM_URL_ENCODED} or {@link BodyType#FORM_DATA}<br>
     * Cannot be null<br>
     * Default is {@link BodyType#X_WWW_FORM_URL_ENCODED}*/
    @NonNull
    private BodyType mBodyType = BodyType.X_WWW_FORM_URL_ENCODED;

    /**
     * Content type for request<br>
     * Only used if BodyType is {@link BodyType#RAW}'*/
    private String mContentType = "text/plain";

    /**
     * Raw body if post method is {@link BodyType#RAW}*/
    @Nullable
    private String mRequestBody;

    /**
     * Array of mFiles to upload to server<br>
     * Used only with {@link BodyType#FORM_DATA}<br>*/
    @Nullable
    private UploadFile[] mFiles;

    private transient int mMaxRetries = 3;
    public int retriesLeft = mMaxRetries;

    private boolean mAllowEmptyValues;

    private ResponseHandler.ResponseMessagePolicy mResponseMessagePolicy = ResponseHandler.DEFAULT_RESPONSE_MESSAGE_POLICY;

    private HashMap<String, String> mHeaders = new HashMap<>();

    private RequestHandler mRequestHandler;

    /**
     * Create new request mUrlBuilder<br>
     * If no other option is specified default options are:
     * <pre>
     * * mRequestUrl - {@link #DEFAULT_REQUEST_URL}
     * * postMethod - {@link BodyType#X_WWW_FORM_URL_ENCODED}
     * * mContentType - "text/plain"
     * </pre>
     * @param method HTTP method for this request*/
    public Request(@NonNull Method method)
    {
        mMethod = method;
        mUrlBuilder = new StringBuilder();
    }

    /**
     * Set request body.<br>
     * This is only used with {@link BodyType#RAW}
     * @param body body for this request
     * @return this object for method chaining*/
    public Request setRequestBody(@Nullable String body)
    {
        this.mRequestBody = body;
        if(!isEmpty(mRequestBody))
            setBodyType(BodyType.RAW);
        return this;
    }

    /**
     * Set request content-type.<br>
     * This is only used with {@link BodyType#RAW}
     * @param contentType content type for for request (application/json)
     * @return this object for method chaining*/
    public Request setContentType(String contentType)
    {
        this.mContentType = contentType;
        return this;
    }

    /**
     * Set HTTP POST method to use
     * @param bodyType HTTP POST method
     * @return this object for method chaining*/
    public Request setBodyType(@NonNull BodyType bodyType)
    {
        this.mBodyType = bodyType;
        if(bodyType != BodyType.RAW && !isEmpty(mRequestBody))
        {
            if(SNet.LOGGING) Log.w(SNet.LOG_TAG, "Warning. mRequestBody is not empty, it will be ignored since new BodyType is not BodyType.RAW");
        }
        if(bodyType != BodyType.FORM_DATA && (mFiles != null && mFiles.length > 0))
        {
            if(SNet.LOGGING)Log.w(SNet.LOG_TAG, "Warning. Files will be ignored since new BodyType is not BodyType.FORM_DATA");
        }
        return this;
    }

    /**
     * Set list of mFiles for uploading
     * @param files list of mFiles to upload
     * @see UploadFile
     * @return same object for method chaining*/
    public Request setFiles(@Nullable UploadFile... files)
    {
        this.mFiles = files;
        if(files != null && files.length > 0)
            setBodyType(BodyType.FORM_DATA);
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
        mRequestUrl = url;
        return this;
    }

    /**
     * Set maximum number of retries if request fails due to network or other errors
     * @param maxRetries max retries
     * @return this object for method chaining*/
    public Request setMaxRetires(int maxRetries)
    {
        this.mMaxRetries = maxRetries;
        this.retriesLeft = maxRetries;
        return this;
    }

    /**
     * @return Max possible retries
     * */
    public int getMaxRetries()
    {
        return mMaxRetries;
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
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, String value)
    {
        Param param = getParam(key);
        if(param != null)
            param.setValue(value);
        else
            addParam(key, value);
        return this;
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, String value, boolean forceAddToUrl)
    {
        Param param = getParam(key);
        if(param != null)
            param.setValue(value);
        else
            addParam(key, value, forceAddToUrl);
        return this;
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, int value)
    {
        return setParam(key, Integer.toString(value));
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, int value, boolean forceAddToUrl)
    {
        return setParam(key, Integer.toString(value), forceAddToUrl);
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, long value)
    {
        return setParam(key, Long.toString(value));
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, long value, boolean forceAddToUrl)
    {
        return setParam(key, Long.toString(value), forceAddToUrl);
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, float value)
    {
        return setParam(key, Float.toString(value));
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, float value, boolean forceAddToUrl)
    {
        return setParam(key, Float.toString(value), forceAddToUrl);
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, double value)
    {
        return setParam(key, Double.toString(value));
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, double value, boolean forceAddToUrl)
    {
        return setParam(key, Double.toString(value), forceAddToUrl);
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, boolean value)
    {
        return setParam(key, Boolean.toString(value));
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, boolean value, boolean forceAddToUrl)
    {
        return setParam(key, Boolean.toString(value), forceAddToUrl);
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, short value)
    {
        return setParam(key, Short.toString(value));
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, short value, boolean forceAddToUrl)
    {
        return setParam(key, Short.toString(value), forceAddToUrl);
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, byte value)
    {
        return setParam(key, Byte.toString(value));
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, byte value, boolean forceAddToUrl)
    {
        return setParam(key, Byte.toString(value), forceAddToUrl);
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, Object value)
    {
        return setParam(key, value == null ? null : value.toString());
    }

    /**
     * <pre>
     * Set value of param
     * If param doesn't exist it will be added
     * Encoding of parameter is taken care of
     * </pre>
     * @param key parameter for this request. Passing null will not set action
     * @param value value of this parameter. If param doesn't exist and value is empty or null, param wont be added
     * @return same object for method chaining
     * */
    public Request setParam(String key, Object value, boolean forceAddToUrl)
    {
        return setParam(key, value == null ? null : value.toString(), forceAddToUrl);
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
            if(SNet.LOGGING)Log.e(SNet.LOG_TAG, "RequestBuilder >> _addUrlPart : param not set");
            return this;
        }
        if(forceAddToUrl || mMethod == Method.GET || mMethod == Method.DELETE)
        {
            mParams.add(new Param(key, SNetUtils.encodeString(value), true));
        }
        else
        {
            mParams.add(new Param(key, value, false));
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
        return _addUrlPart(part);
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(int part)
    {
        return _addUrlPart(Integer.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(long part)
    {
        return _addUrlPart(Long.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(short part)
    {
        return _addUrlPart(Short.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(float part)
    {
        return _addUrlPart(Float.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(double part)
    {
        return _addUrlPart(Double.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(boolean part)
    {
        return _addUrlPart(Boolean.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(byte part)
    {
        return _addUrlPart(Byte.toString(part));
    }

    /**
     * Add url part
     * @param part url encoded param eg.: www.example.com/controller/action/{parame1}/{param2}
     * @return same object for method chaining
     * */
    public Request addUrlPart(Object part)
    {
        return _addUrlPart(part == null ? null : part.toString());
    }

    /**
     * Add url part
     * */
    private Request _addUrlPart(String value)
    {
        if(value == null || value.isEmpty())
        {
            if(SNet.LOGGING)Log.e(SNet.LOG_TAG, "RequestBuilder >> _addUrlPart : param not set");
            return this;
        }
        if(mUrlBuilder.length() == 0)
        {
            if(mRequestUrl != null && !mRequestUrl.endsWith("/"))
                mUrlBuilder.append("/");
        }
        else if(!mUrlBuilder.toString().endsWith("/"))
        {
            mUrlBuilder.append("/");
        }
        mUrlBuilder.append(value.startsWith("/") ? value.substring(1, value.length()) : value);
        return this;
    }

    /**
     * Remove parameter.*/
    public void removeParam(String key)
    {
        if(key == null || key.isEmpty())
            return;
        for(int i = mParams.size() - 1; i < 0; i--)
        {
            Param param = mParams.get(i);
            if(key.equals(param.getKey()))
                mParams.remove(i);
        }
    }

    /**
     * Add HTTP request header
     * @param header key for this heade (Content-Type)
     * @param value value for this header (application/json)
     * @return this for method chaining*/
    public Request addHeader(String header, String value)
    {
        mHeaders.put(header, value);
        return this;
    }

    /**
     * Get all user specified request mHeaders. This will not return any header added by underlying http client implementation
     * @return mHeaders for this RequestBuilder*/
    public HashMap<String, String> getHeaders()
    {
        return mHeaders;
    }

    /**
     * @return generate url parameters string. eg. username=pedja&amp;password=123456
     * */
    public String buildUrl()
    {
        String urlParts = mUrlBuilder.toString();
        StringBuilder tmpBuilder = new StringBuilder();
        for(Param param : mParams)
        {
            if(!param.isQueryParam)
                continue;
            if(!tmpBuilder.toString().contains("?"))
                tmpBuilder.append("?");
            else
                tmpBuilder.append("&");
            tmpBuilder.append(param.getKey()).append("=").append(param.getValue());
        }
        return urlParts + tmpBuilder.toString();
    }

    public List<Param> getParams()
    {
        return mParams;
    }

    public List<Param> getBodyParams()
    {
        List<Param> params = new ArrayList<>();
        for(Param param : mParams)
        {
            if(!param.isQueryParam)
                params.add(param);
        }
        return params;
    }

    public List<Param> getQueryParams()
    {
        List<Param> params = new ArrayList<>();
        for(Param param : mParams)
        {
            if(param.isQueryParam)
                params.add(param);
        }
        return params;
    }

    /**
     * Get request url including url parameters
     * @return request url for this request mUrlBuilder*/
    public String getRequestUrl()
    {
        return mRequestUrl + buildUrl();
    }

    /**
     * Get the response message policy for this request
     * @return response message policy. Null is returned only if policy is explicitly set to null*/
    public ResponseHandler.ResponseMessagePolicy getResponseMessagePolicy()
    {
        return mResponseMessagePolicy;
    }

    /**
     * Set the ResponseMessagePolicy
     * @see org.skynetsoftware.snet.ResponseHandler.ResponseMessagePolicy
     * @param responseMessagePolicy policy
     * @return this for call chain*/
    public Request setResponseMessagePolicy(org.skynetsoftware.snet.ResponseHandler.ResponseMessagePolicy responseMessagePolicy)
    {
        this.mResponseMessagePolicy = responseMessagePolicy;
        return this;
    }

    public Method getMethod()
    {
        return mMethod;
    }

    @NonNull
    public BodyType getBodyType()
    {
        return mBodyType;
    }

    public String getContentType()
    {
        return mContentType;
    }

    @Nullable
    public String getRequestBody()
    {
        return mRequestBody;
    }

    @Nullable
    public UploadFile[] getFiles()
    {
        return mFiles;
    }

    /**
     * Sets the default request url. It is used only if request url is not specified
     * @see #setRequestUrl(String)
     * @param url default request url*/
    public static void setDefaultRequestUrl(String url)
    {
        DEFAULT_REQUEST_URL = url;
    }

    public Param getParam(String key)
    {
        if(key == null || key.isEmpty())
            return null;
        for(Param param : mParams)
        {
            if(key.equals(param.key))
                return param;
        }
        return null;
    }

    public void setAllowEmptyValues(boolean allowEmptyValues)
    {
        this.mAllowEmptyValues = allowEmptyValues;
    }

    public RequestHandler getRequestHandler()
    {
        return mRequestHandler;
    }

    public void setRequestHandler(RequestHandler requestHandler)
    {
        this.mRequestHandler = requestHandler;
    }

    private static final Random RANDOM = new Random();

    public void execute(final @NonNull ResponseListener listener)
    {
        final int requestCode = RANDOM.nextInt();
        RequestManager.getInstance().addResponseHandler(new ResponseHandler()
        {
            @Override
            public void onResponse(int _requestCode, int responseStatus, ResponseParser responseParser)
            {
                if(requestCode == _requestCode)
                {
                    listener.onResponse(responseParser);
                    RequestManager.getInstance().removeResponseHandler(this);
                }
            }
        });
        RequestManager.getInstance().executeAsync(this, requestCode);
    }

    public ResponseParser execute()
    {
        return RequestManager.getInstance().executeSync(this, 0);
    }

    /**
     * Wrapper for file that sould be uploaded<br>
     * Used only with {@link BodyType#RAW}*/
    public static class UploadFile
    {
        /**
         * Uri of the image for uploading file to server*/

        @NonNull
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

        /**
         * Post parameter name for file upload<br>
         * Used only with {@link BodyType#FORM_DATA}<br>*/
        private String fileParamName = "file";

        @NonNull
        public String getUri()
        {
            return uri;
        }

        public void setUri(@NonNull String uri)
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

        /**Set parameter name for file that is uploading to server.
         * @param fileParamName name of the file param*/
        public void setFileParamName(String fileParamName)
        {
            this.fileParamName = fileParamName;
        }

        public String getFileParamName()
        {
            return fileParamName;
        }
    }

    private static boolean isEmpty(@Nullable CharSequence str)
    {
        return str == null || str.length() == 0;
    }

    public interface ResponseListener
    {
        void onResponse(ResponseParser responseParser);
    }

    public static class Param
    {
        private final String key;
        private String value;
        private boolean isQueryParam;

        public Param(String key, String value, boolean isQueryParam)
        {
            this.key = key;
            this.value = value;
            this.isQueryParam = isQueryParam;
        }

        public Param(String key, String value)
        {
            this.key = key;
            this.value = value;
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }

        public boolean isQueryParam()
        {
            return isQueryParam;
        }

        public void setQueryParam(boolean queryParam)
        {
            isQueryParam = queryParam;
        }

        public void setValue(String value)
        {
            this.value = value;
        }
    }
}
