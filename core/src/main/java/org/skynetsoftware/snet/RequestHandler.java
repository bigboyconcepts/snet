package org.skynetsoftware.snet;

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
    ResponseParser handleRequest(int requestCode, @NonNull Request builder, boolean sync);

    /**
     * Called before request is executed
     * @param requestCode unique request code for this request
     * @param sync if this request is done on caller thread or async
     * */
    void handlePreRequest(int requestCode, boolean sync);

    /**
     * Called when request is done
     * @param requestCode unique request code for this request
     * @param builder RequestBuilder instance
     * @param sync if this request is done on caller thread or async
     * @param parser ResponseParser instance returned from {@link #handleRequest(int, Request, boolean)}*/
    void handlePostRequest(int requestCode, @NonNull Request builder, ResponseParser parser, boolean sync);

    /**
     * Called when request is done
     * @param requestCode unique request code for this request
     * @param sync if this request is done on caller thread or async
     * @param parser ResponseParser instance returned from {@link #handleRequest(int, Request, boolean)}*/
    void handleRequestCancelled(int requestCode, @NonNull ResponseParser parser, boolean sync);
}
