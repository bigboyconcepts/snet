package org.skynetsoftware.snet;

import com.google.j2objc.annotations.ObjectiveCName;

import javax.annotation.Nonnull;

/**
 * Created by pedja on 27.6.15..
 */
public interface RequestHandler
{
    /**
     * Here you should make network request and return ResponseParser instance
     * @param requestCode unique request code for this request
     * @param builder RequestBuilder instance
     * @param sync if this request is done on caller thread or async
     * @return ResponseParser to be passed to other methods*/
    @ObjectiveCName("handleRequestWithRequestCode:andRequestBuilder:sync:")
    ResponseParser handleRequest(int requestCode, @Nonnull Request builder, boolean sync);

    /**
     * Called before request is executed
     * @param requestCode unique request code for this request
     * @param sync if this request is done on caller thread or async
     * */
    @ObjectiveCName("handlePreRequestWithRequestCode:sync:")
    void handlePreRequest(int requestCode, boolean sync);

    /**
     * Called when request is done
     * @param requestCode unique request code for this request
     * @param builder RequestBuilder instance
     * @param sync if this request is done on caller thread or async
     * @param parser ResponseParser instance returned from {@link #handleRequest(int, Request, boolean)}*/
    @ObjectiveCName("handlePostRequestWithRequestCode:andRequestBuilder:andResponseParser:sync:")
    void handlePostRequest(int requestCode, @Nonnull Request builder, ResponseParser parser, boolean sync);

    /**
     * Called when request is done
     * @param requestCode unique request code for this request
     * @param sync if this request is done on caller thread or async
     * @param parser ResponseParser instance returned from {@link #handleRequest(int, Request, boolean)}*/
    @ObjectiveCName("handleRequestCanceledWithRequestCode:andResponseParser:sync:")
    void handleRequestCancelled(int requestCode, @Nonnull ResponseParser parser, boolean sync);
}
