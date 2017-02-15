package android.util;

/**
 * Created by pedja on 2/15/17.
 */

public class Log
{
    public static void d(String tag, String msg)
    {
        System.out.println(tag + ":" + msg);
    }

    public static void w(String tag, String msg)
    {
        System.out.println(tag + ":" + msg);
    }

    public static void i(String tag, String msg)
    {
        System.out.println(tag + ":" + msg);
    }

    public static void e(String tag, String msg)
    {
        System.out.println(tag + ":" + msg);
    }
}
