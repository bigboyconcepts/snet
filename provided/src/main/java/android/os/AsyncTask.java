package android.os;

import java.util.concurrent.Executor;

/**
 * Created by pedja on 2/15/17.
 */

public abstract class AsyncTask<Params, Progress, Result>
{
    public static final Executor THREAD_POOL_EXECUTOR = null;
    protected abstract Result doInBackground(Params... params);
    public final boolean cancel(boolean mayInterruptIfRunning)
    {
        throw new RuntimeException("Stub!");
    }
    protected void onPreExecute(){}
    protected void onPostExecute(Result result){}
    protected void onCancelled(Result result){}
    public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec, Params... params){ return this; }
}
