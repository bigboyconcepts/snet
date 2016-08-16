package com.tehnicomsolutions.http;

import android.app.Activity;
import android.app.ProgressDialog;

import java.lang.ref.WeakReference;


/**
 * Created by pedja on 6.7.15. 11.29.
 * This class is part of the ts-http
 * Copyright © 2015 ${OWNER}
 */
public class AndroidRequestHandler implements RequestHandler
{
    private ProgressDialog dialog;
    private WeakReference<Activity> ref;

    public AndroidRequestHandler(Activity activity)
    {
        this.ref = new WeakReference<>(activity);
    }

    @Override
    public ResponseParser handleRequest(int requestCode, Request request, boolean sync)
    {
        Internet.Response response = Http.getInstance().internet.executeHttpRequest(request);
        return new ResponseParser(response);//TODO this does nothing
    }

    @Override
    public void handlePreRequest(int requestCode, boolean sync)
    {
        if (!sync)
        {
            if (ref.get() != null)
            {
                dialog = new ProgressDialog(ref.get());
                dialog.setCancelable(false);
                dialog.setMessage(ref.get().getString(R.string.please_wait));
                dialog.show();
            }
        }
    }

    @Override
    public void handlePostRequest(final int requestCode, final Request builder, final ResponseParser responseParser, boolean sync)
    {
        if (!sync)
        {
            if (ref.get() != null)
            {
                if (dialog != null)
                    dialog.dismiss();
            }
        }
    }

    @Override
    public void handleRequestCancelled(int requestCode, ResponseParser parser, boolean sync)
    {

    }
}
