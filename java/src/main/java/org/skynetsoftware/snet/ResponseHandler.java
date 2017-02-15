package org.skynetsoftware.snet;

import java.io.Serializable;

/**
 * Created by pedja on 2/19/14 10.17.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 * @author Predrag Čokulov
 */
public interface ResponseHandler
{
    ResponseMessagePolicy DEFAULT_RESPONSE_MESSAGE_POLICY = new ResponseMessagePolicy();

    /**
     * Called if response from server is successful and doesn't contains errors
     * @param requestCode unique request code for this request
     * @param responseStatus status of the response
     * @param responseParser ResponseParser instance returned from {@link RequestHandler#handleRequest(int, Request, boolean)}
     * */
    void onResponse(int requestCode, int responseStatus, ResponseParser responseParser);

    class ResponseMessagePolicy implements Serializable
    {
        protected boolean showSuccessMessages = true;
        protected boolean showErrorMessages = true;
        /**
         * Not supported yet*/
        public ResponseMessageFormat responseMessageFormat = ResponseMessageFormat.show_as_toast;

        public ResponseMessagePolicy showSuccessMessages(boolean showSuccessMessages)
        {
            this.showSuccessMessages = showSuccessMessages;
            return this;
        }

        public ResponseMessagePolicy showErrorMessages(boolean showErrorMessages)
        {
            this.showErrorMessages = showErrorMessages;
            return this;
        }

        public ResponseMessagePolicy responseMessageFormat(ResponseMessageFormat responseMessageFormat)
        {
            this.responseMessageFormat = responseMessageFormat;
            return this;
        }

        public boolean isShowSuccessMessages()
        {
            return showSuccessMessages;
        }

        public boolean isShowErrorMessages()
        {
            return showErrorMessages;
        }

        public enum ResponseMessageFormat
        {
            show_as_toast, show_as_dialog, show_as_notification/*this one is stupid*/
        }
    }

}
