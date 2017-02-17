package org.skynetsoftware.snet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pedja on 2/17/17 9:19 AM.
 * This class is part of the snet
 * Copyright © 2017 ${OWNER}
 */

@SuppressWarnings({"ClassInitializerMayBeStatic", "FinalStaticMethod"})
public abstract class SmartRequestHandler implements RequestHandler
{
    private static Class<? extends ResponseParser> responseParserClass;
    private static Class<? extends Internet> internetClass;
    private static boolean initialized = false;
    private static final Map<Integer, String> methods = new HashMap<>();

    public static final void init(Class<? extends ResponseParser> responseParserClass, Class<? extends Internet> internetClass)
    {
        if (initialized)
        {
            throw new IllegalStateException("SmartRequestHandler is already initialized. call init(...) only once");
        }

        SmartRequestHandler.responseParserClass = responseParserClass;
        SmartRequestHandler.internetClass = internetClass;

        if (responseParserClass == null)
            throw new IllegalArgumentException("responseParserClass cannot be null");

        initialized = true;
    }

    public static final void registerParserMethod(int requestCode, String parserMethod)
    {
        methods.put(requestCode, parserMethod);
    }

    private Internet internet;

    {
        if (!initialized)
        {
            throw new IllegalStateException("SmartRequestHandler is not initialized. call init(...) first");
        }
        if (internetClass == null && getInternetInstance() == null)
        {
            throw new IllegalStateException("You must either initialize SmartRequestHandler with internetClass or return non-null Internet instance in getInternetInstance");
        }
        if (getInternetInstance() != null)
        {
            internet = getInternetInstance();
        }
        else
        {
            try
            {
                internet = internetClass.newInstance();
            }
            catch (InstantiationException e)
            {
                if(SNet.LOGGING)e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                if(SNet.LOGGING)e.printStackTrace();
            }
        }
        if(internet == null)
        {
            throw new IllegalStateException("Failed to create internet instance. You must either initialize SmartRequestHandler with internetClass or return non-null Internet instance in getInternetInstance");
        }
    }

    @Override
    public final ResponseParser handleRequest(int requestCode, @NonNull Request builder, boolean sync)
    {
        for(Integer rc : methods.keySet())
        {
            if(rc == requestCode)
            {
                String method = methods.get(rc);
                ResponseParser parser = createResponseParser(builder);
                try
                {
                    Method m = responseParserClass.getMethod(method, null);
                    m.invoke(parser, null);
                    return parser;
                }
                catch (NoSuchMethodException e)
                {
                    throw new IllegalStateException(String.format("Method '%s' doesn't exist in '%s", method, responseParserClass.getName()));
                }
                catch (InvocationTargetException e)
                {
                    throw new IllegalStateException(e);
                }
                catch (IllegalAccessException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
        throw new IllegalStateException(String.format("method not registered for requestCode: '%s'", requestCode));
    }

    private ResponseParser createResponseParser(Request request)
    {
        try
        {
            Constructor<? extends ResponseParser> constructor = responseParserClass.getConstructor(Internet.Response.class);
            return constructor.newInstance(makeRequest(request));
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalStateException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException(e);
        }
        catch (InstantiationException e)
        {
            throw new IllegalStateException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException(e);
        }
    }

    protected Internet.Response makeRequest(@NonNull Request request)
    {
        return internet.executeHttpRequest(request);
    }

    protected abstract Internet getInternetInstance();
}
