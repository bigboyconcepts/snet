package com.tehnicomsolutions.http;

import android.content.Context;

import com.github.johnpersano.supertoasts.library.Style;

/**
 * Created by pedja on 3/12/16.
 */
public class AndroidUI implements UI
{
    private Context context;
    private int toastLength = Style.DURATION_LONG;

    public AndroidUI(Context context)
    {
        this.context = context;
    }

    public void setToastLength(int toastLength)
    {
        this.toastLength = toastLength;
    }

    @Override
    public void showToast(String message)
    {
        ToastUtility.showToast(context, message, toastLength);
    }

    @Override
    public void showToast(String message, int length)
    {
        ToastUtility.showToast(context, message, length);
    }
}
