package org.skynetsoftware.snet;

import android.content.Context;

/**
 * Created by pedja on 3/12/16.
 */
public class AndroidTextManager implements TextManager
{
    private Context context;

    public AndroidTextManager(Context context)
    {
        this.context = context;
    }

    @Override
    public String getText(String key)
    {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(key, "string", packageName);
        return context.getString(resId);
    }
}
