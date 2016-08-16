package com.tehnicomsolutions.http;


import javax.annotation.Nonnull;

/**
 * Created by pedja on 2/19/14 10.17.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 *
 * <br>
 * Implementation of {@link ResponseHandler} for general purpose response handling
 *
 * @author Predrag Čokulov
 */
public class ResponseHandlerImpl implements ResponseHandler
{
    public static final ResponseMessagePolicy DEFAULT_RESPONSE_MESSAGE_POLICY = new ResponseMessagePolicy();

    public ResponseHandlerImpl(int requestCode, ResponseParser responseParser)
    {
        this(requestCode, responseParser, DEFAULT_RESPONSE_MESSAGE_POLICY);
    }

    public ResponseHandlerImpl(int requestCode, ResponseParser responseParser, @Nonnull ResponseMessagePolicy responseMessagePolicy)
    {
        if(responseParser == null)return;
        if (responseParser.getResponseStatus() == ResponseParser.RESPONSE_STATUS_SUCCESS)
        {
            if(responseParser.getResponseMessage() != null && responseMessagePolicy.showSuccessMessages)
                Http.getInstance().ui.showToast(responseParser.getResponseMessage());
        }
        else
        {
            if(responseMessagePolicy.showErrorMessages)
            {
                Http.getInstance().ui.showToast(responseParser.getResponseMessage() != null ? responseParser.getResponseMessage() : Http.getInstance().textManager.getText("unknown_error"));
            }
        }
        onResponse(requestCode, responseParser.getResponseStatus(), responseParser);
    }

    public ResponseHandlerImpl()
    {

    }

    @Override
    public void onResponse(int requestCode, int responseStatus, ResponseParser responseParser)
    {

    }

}
