package com.tehnicomsolutions.http;

import android.content.Context;
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

    public static void showToast(Context context, String message, int length)
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
            Style style = new Style();
            style.type = Style.TYPE_STANDARD;
            style.frame = Style.FRAME_STANDARD;
            final SuperToast toast = SuperToast.create(context, Html.fromHtml(message), length);
            toast.setOnDismissListener(new SuperToast.OnDismissListener()
            {
                @Override
                public void onDismiss(View view, Parcelable token)
                {
                    currentToast = toastQueue.isEmpty() ? null : toastQueue.removeFirst();
                    if(currentToast != null)
                    {
                        toast.setText(currentToast.message);
                        toast.setDuration(currentToast.length);
                        toast.show();
                    }
                }
            });
            toast.show();
        }
        else
        {
            toastQueue.add(newMessage);
        }
    }

    public static void showToast(Context context, String message)
    {
        showToast(context, message, Style.DURATION_SHORT);
    }

    private static final class ToastMessage
    {
        private final String message;
        private final int length;

        public ToastMessage(String message, int length)
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

            if (length != that.length) return false;
            return message != null ? message.equals(that.message) : that.message == null;

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
