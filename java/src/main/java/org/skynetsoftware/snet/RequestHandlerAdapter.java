package org.skynetsoftware.snet;

import javax.annotation.Nonnull;

/**
 * Created by pedja on 28.3.16. 11.20.
 * This class is part of the snet
 * Copyright © 2016 ${OWNER}
 */
public abstract class RequestHandlerAdapter implements RequestHandler
{
    @Override
    public void handlePreRequest(int requestCode, boolean sync)
    {

    }

    @Override
    public void handlePostRequest(int requestCode, @Nonnull Request builder, ResponseParser parser, boolean sync)
    {

    }

    @Override
    public void handleRequestCancelled(int requestCode, @Nonnull ResponseParser parser, boolean sync)
    {

    }
}
