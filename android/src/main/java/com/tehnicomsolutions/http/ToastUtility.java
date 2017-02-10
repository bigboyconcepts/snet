package com.tehnicomsolutions.http;

import android.content.Context;
import android.text.Html;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperToast;

/**
 * Created by pedja on 13.4.16. 10.06.
 * This class is part of the ts-http-v1
 * Copyright © 2016 ${OWNER}
 */
public class ToastUtility
{
    public static void showToast(final Context context, final String message, final int length)
    {
        if(message == null)
            return;
        Style style = new Style();
        style.type = Style.TYPE_STANDARD;
        style.frame = Style.FRAME_STANDARD;
        final SuperToast toast = SuperToast.create(context, Html.fromHtml(message), length);
        toast.show();
    }

    public static void showToast(Context context, String message)
    {
        showToast(context, message, Style.DURATION_SHORT);
    }
}
