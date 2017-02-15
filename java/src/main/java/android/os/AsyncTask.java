package android.os;

import java.util.concurrent.Executor;

/**
 * Created by pedja on 2/15/17.
 * Default implementation juts executes everything on main thread
 */

public abstract class AsyncTask<Params, Progress, Result>
{
    public static final Executor THREAD_POOL_EXECUTOR = null;

    protected abstract Result doInBackground(Params... params);

    public void cancel(boolean mayInterruptIfRunning)
    {
    }

    protected void onPreExecute()
    {
    }

    protected void onPostExecute(Result result)
    {
    }

    protected void onCancelled(Result result)
    {
    }

    public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params)
    {
        onPreExecute();
        Result result = doInBackground(params);
        onPostExecute(result);
        return this;
    }
}
