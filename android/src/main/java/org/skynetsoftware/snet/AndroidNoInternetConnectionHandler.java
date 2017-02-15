package org.skynetsoftware.snet;

import android.app.AlertDialog;
import android.content.Context;

import com.tehnicomsolutions.http.R;

/**
 * Created by pedja on 3/12/16.
 */
public class AndroidNoInternetConnectionHandler implements NoInternetConnectionHandler
{
    private Context context;
    private AlertDialog dialog;

    public AndroidNoInternetConnectionHandler(Context context)
    {
        this.context = context;
    }

    @Override
    public void handleNoInternetConnection()
    {
        if(context == null || dialog != null && dialog.isShowing())
            return;
        if(dialog == null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.no_connection_dialog_title);
            builder.setMessage(R.string.no_connection_dialog_message);
            builder.setNeutralButton(android.R.string.ok, null);
            dialog = builder.create();
        }
        dialog.show();
    }
}
