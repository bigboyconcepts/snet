package org.skynetsoftware.snet;

/**
 * Created by pedja on 2/19/14.
 *
 * This class is wrapper for {@link ATListener} allowing you to implement only needed methods
 * @author Predrag Čokulov
 */
public class SimpleATListener implements ATListener
{
    @Override
    public ResponseParser doInBackground(int requestCode)
    {
        return null;
    }

    @Override
    public void onPostExecute(int requestCode, ResponseParser result)
    {

    }

    @Override
    public void onPreExecute(int requestCode)
    {

    }

    @Override
    public void onCancelled(int requestCode, ResponseParser result)
    {

    }
}
