package org.skynetsoftware.snet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;

import com.tehnicomsolutions.http.R;

import java.lang.ref.WeakReference;


/**
 * Created by pedja on 6.7.15. 11.29.
 * This class is part of the ts-http
 * Copyright © 2015 ${OWNER}
 */
public class AndroidSmartRequestHandler extends SmartRequestHandler
{
    private ProgressDialog dialog;
    private WeakReference<Activity> ref;

    public AndroidSmartRequestHandler(Activity activity)
    {
        this.ref = new WeakReference<>(activity);
    }

    @Override
    protected Internet getInternetInstance()
    {
        return SNet.getInstance().getInternet();
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
    public void handlePostRequest(final int requestCode, @NonNull final Request builder, final ResponseParser responseParser, boolean sync)
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
    public void handleRequestCancelled(int requestCode, @NonNull ResponseParser parser, boolean sync)
    {

    }
}
