package com.tehnicomsolutions.http;

import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Html;
import android.view.View;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperToast;

import java.util.LinkedList;

/**
 * Created by pedja on 13.4.16. 10.06.
 * This class is part of the ts-http-v1
 * Copyright © 2016 ${OWNER}
 */
public class ToastUtility
{
    private static LinkedList<ToastMessage> toastQueue = new LinkedList<>();
    private static ToastMessage currentToast;
    private static Handler toastHandler = new Handler();

    public static void showToast(final Context context, final String message, final int length)
    {
        if(message == null)
            return;
        ToastMessage newMessage = new ToastMessage(message, length);
        if(newMessage.equals(currentToast) || toastQueue.contains(newMessage))
        {
            return;
        }
        if(currentToast == null)
        {
            currentToast = newMessage;
            _showToast(context.getApplicationContext());
        }
        else
        {
            toastQueue.add(newMessage);
        }
    }

    private static void _showToast(final Context appContext)
    {
        Style style = new Style();
        style.type = Style.TYPE_STANDARD;
        style.frame = Style.FRAME_STANDARD;
        final SuperToast toast = SuperToast.create(appContext, Html.fromHtml(currentToast.message), currentToast.length);
        toast.setOnDismissListener(new SuperToast.OnDismissListener()
        {
            @Override
            public void onDismiss(View view, Parcelable token)
            {
                //dismiss is called before previous toast has been removed from internal queue (supertoast queue)
                //we delay showing next toast to avoid infinite toast showing loop
                //TODO handle internally in supertoast
                toastHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        currentToast = toastQueue.isEmpty() ? null : toastQueue.removeFirst();
                        if(currentToast != null)
                        {
                            _showToast(appContext);
                        }
                    }
                });
            }
        });
        toast.show();
    }

    public static void showToast(Context context, String message)
    {
        showToast(context, message, Style.DURATION_SHORT);
    }

    private static final class ToastMessage
    {
        private final String message;
        private final int length;

        ToastMessage(String message, int length)
        {
            this.message = message;
            this.length = length;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ToastMessage that = (ToastMessage) o;

            return length == that.length && (message != null ? message.equals(that.message) : that.message == null);

        }

        @Override
        public int hashCode()
        {
            int result = message != null ? message.hashCode() : 0;
            result = 31 * result + length;
            return result;
        }
    }
}
