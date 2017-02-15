package org.skynetsoftware.snet;


import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by pedja on 2/21/14 10.17.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 * <p>
 * This is the core of ts-http. You will use this class for all http requests
 *
 * @author Predrag Čokulov
 */
public abstract class RequestManager
{
    private static RequestManager instance;

    public static RequestManager getInstance()
    {
        if (instance == null)
            throw new IllegalStateException("You must call initialize() first");
        return instance;
    }

    public static void initialize(RequestManager requestManager)
    {
        if (requestManager == null)
            throw new IllegalStateException("RequestManager cannot be null");
        instance = requestManager;
    }

    private List<ResponseHandler> responseHandlers;

    //this array has all tasks currently running
    private final Map<Integer, ATNet> runningTasks;

    //this list holds all tasks that are waiting to be executed
    private final List<ATNet> taskQueue;

    private org.skynetsoftware.snet.RequestHandler mGlobalRequestHandler;

    private NoInternetConnectionHandler noInternetConnectionHandler;

    /**
     * Create a new instance of TSRequestManager
     */
    protected RequestManager()
    {
        taskQueue = new ArrayList<>();
        runningTasks = new HashMap<>();
        responseHandlers = new ArrayList<>();
    }

    @ObjectiveCName("executeAsyncRequest:withRequestCode:")
    public ResponseParser executeAsync(Request request, int requestCode)
    {
        return execute(requestCode, request, false);
    }

    @ObjectiveCName("executeSyncRequest:withRequestCode:")
    public ResponseParser executeSync(Request request, int requestCode)
    {
        return execute(requestCode, request, true);
    }

    /**
     * Execute this request
     *
     * @param requestCode request code for this task, this code should be unique for each request
     * @param request     request builder
     * @return if request is async null else ResponseParser object
     */
    private ResponseParser execute(int requestCode, Request request, boolean sync)
    {
        assertRequestHandler(request.getRequestHandler());
        if (sync)
        {
            handlePreRequest(requestCode, request, true);
            ResponseParser responseParser = handleRequest(requestCode, request, true);
            handlePostRequest(requestCode, request, responseParser, true);
            return responseParser;
        }
        else
        {
            synchronized (runningTasks)
            {
                ATNet task = new ATNet(new ATNetListener(request), requestCode);
                task.setNoInternetConnectionHandler(noInternetConnectionHandler);
                if (runningTasks.get(task.getRequestCode()) != null)//task exists
                {
                    taskQueue.add(task); // add task to queue, don't execute it
                }
                else
                {
                    runningTasks.put(task.getRequestCode(), task);//add this task to list of running tasks
                    task.execute();//execute task
                }
            }
            return null;
        }
    }

    @ObjectiveCName("runOnUIThread:")
    protected abstract void runOnUIThread(Runnable runnable);

    private void runNextTaskFromQueue(int requestCode, boolean sync)
    {
        if (!sync)
        {
            synchronized (runningTasks)
            {
                runningTasks.remove(requestCode); // remove this task from running tasks list

                //We only added tasks to queue if it already exists in runningTasks
                //We check here if there is a task in taskQueue with same id as this one, remove it from queue and execute it
                ATNet task = getTaskByCode(requestCode, taskQueue);
                if (task != null)
                {
                    taskQueue.remove(task);
                    if (runningTasks.get(task.getRequestCode()) != null)//task exists
                    {
                        taskQueue.add(task); // add task to queue, don't execute it
                    }
                    else
                    {
                        runningTasks.put(task.getRequestCode(), task);//add this task to list of running tasks
                        task.execute();//execute task
                    }
                }
            }
        }
    }

    private void notifyResponseHandler(int requestCode, int responseStatus, ResponseParser responseParser)
    {
        for (int i = responseHandlers.size() - 1; i >= 0; i--)
        {
            responseHandlers.get(i).onResponse(requestCode, responseStatus, responseParser);
        }
    }

    private ResponseParser handleRequest(int requestCode, Request request, boolean sync)
    {
        assertRequestHandler(request.getRequestHandler());
        return request.getRequestHandler() != null ? request.getRequestHandler().handleRequest(requestCode, request, sync) : mGlobalRequestHandler.handleRequest(requestCode, request, sync);
    }

    private void assertRequestHandler(org.skynetsoftware.snet.RequestHandler requestHandler)
    {
        if (mGlobalRequestHandler == null && requestHandler == null)
            throw new IllegalStateException("both global request handler and request handler are null");
    }

    private void handlePreRequest(int requestCode, Request request, boolean sync)
    {
        assertRequestHandler(request.getRequestHandler());
        if (request.getRequestHandler() != null)
            request.getRequestHandler().handlePreRequest(requestCode, sync);
        else
            mGlobalRequestHandler.handlePreRequest(requestCode, sync);
    }

    private void handlePostRequest(final int requestCode, final Request request, final ResponseParser responseParser, boolean sync)
    {
        assertRequestHandler(request.getRequestHandler());
        runOnUIThread(new Runnable()
        {
            @Override
            public void run()
            {
                if(responseParser == null)return;
                if (responseParser.getResponseStatus() == ResponseParser.RESPONSE_STATUS_SUCCESS)
                {
                    if(responseParser.getResponseMessage() != null && request.getResponseMessagePolicy().showSuccessMessages)
                        SNet.getInstance().getUi().showToast(responseParser.getResponseMessage());
                }
                else
                {
                    if(request.getResponseMessagePolicy().showErrorMessages)
                    {
                        SNet.getInstance().getUi().showToast(responseParser.getResponseMessage() != null ? responseParser.getResponseMessage() : SNet.getInstance().getTextManager().getText("unknown_error"));
                    }
                }
                notifyResponseHandler(requestCode, responseParser.getResponseStatus(), responseParser);
            }
        });
        if (request.getRequestHandler() != null)
            request.getRequestHandler().handlePostRequest(requestCode, request, responseParser, sync);
        else
            mGlobalRequestHandler.handlePostRequest(requestCode, request, responseParser, sync);
        runNextTaskFromQueue(requestCode, sync);
    }

    private void handleRequestCancelled(final int requestCode, final Request request, final ResponseParser responseParser, boolean sync)
    {
        assertRequestHandler(request.getRequestHandler());
        runOnUIThread(new Runnable()
        {
            @Override
            public void run()
            {
                notifyResponseHandler(requestCode, ResponseParser.RESPONSE_STATUS_CANCELLED, responseParser);
            }
        });
        if (request.getRequestHandler() != null)
            request.getRequestHandler().handleRequestCancelled(requestCode, responseParser, sync);
        else
            mGlobalRequestHandler.handleRequestCancelled(requestCode, responseParser, sync);
        runNextTaskFromQueue(requestCode, sync);
    }

    /**
     * Cancel task with the given id(requestCode)
     *
     * @param cancelOtherQueuedTasksWithSameId whether to also remove all task with same requestOCde from queue
     * @param requestCode                      requestCode of the task
     */
    @ObjectiveCName("cancelTaskWithRequestCode:andCancelOtherQueuedTasksWithSameId:")
    public void cancel(int requestCode, boolean cancelOtherQueuedTasksWithSameId)
    {
        synchronized (runningTasks)
        {
            if (runningTasks.get(requestCode) != null)
            {
                runningTasks.get(requestCode).cancel(true);
                runningTasks.remove(requestCode);
                if (cancelOtherQueuedTasksWithSameId)//this will also cancel all other not yet executed tasks with same id
                {
                    removeTasksFromList(taskQueue, requestCode);
                }
            }
        }
    }

    /**
     * Cancel all tasks, and clear all pending queue regardless of the requestCode
     */
    public void cancelAll()
    {
        synchronized (runningTasks)
        {
            for (int i = 0; i < runningTasks.size(); i++)
            {
                runningTasks.remove(i).cancel(true);
            }
        }
    }

    /**
     * Get task from list by its requestCode
     *
     * @param requestCode requestCode of the task
     * @param tasks       list of tasks to iterate
     * @return Task with from list with given requestCode or null if no such task exists in list
     */
    private static ATNet getTaskByCode(int requestCode, List<ATNet> tasks)
    {
        for (ATNet t : tasks)
        {
            if (t.getRequestCode() == requestCode)
            {
                return t;
            }
        }
        return null;
    }

    /**
     * Remove all tasks from list that have specified requestCode
     *
     * @param tasks       list of tasks to iterate
     * @param requestCode requestCode of the tasks to remove
     * @return number of tasks removed from list
     */
    private static int removeTasksFromList(List<ATNet> tasks, int requestCode)
    {
        int removedCount = 0;
        List<ATNet> tasksToRemove = new ArrayList<>();
        for (ATNet task : tasks)
        {
            //Add all tasks to different list first to avoid ConcurrentModificationException
            if (task.getRequestCode() == requestCode)
            {
                tasksToRemove.add(task);
            }
        }
        for (ATNet task : tasksToRemove)
        {
            //Remove all tasks from original list
            if (tasks.remove(task)) removedCount++;
        }
        return removedCount;
    }

    private class ATNetListener implements ATListener
    {
        Request mRequest;

        ATNetListener(Request builder)
        {
            this.mRequest = builder;
        }

        @Override
        public ResponseParser doInBackground(int requestCode)
        {
            return handleRequest(requestCode, mRequest, false);
        }

        @Override
        public void onPostExecute(int requestCode, ResponseParser responseParser)
        {
            handlePostRequest(requestCode, mRequest, responseParser, false);
        }

        @Override
        public void onPreExecute(int requestCode)
        {
            handlePreRequest(requestCode, mRequest, false);
        }

        @Override
        public void onCancelled(int requestCode, ResponseParser responseParser)
        {
            handleRequestCancelled(requestCode, mRequest, responseParser, false);
        }
    }

    /**
     * Add a response handler
     *
     * @param responseHandler ResponseHandler to add
     */
    @ObjectiveCName("addResponseHandler:")
    public void addResponseHandler(ResponseHandler responseHandler)
    {
        if (responseHandler == null) return;
        this.responseHandlers.add(responseHandler);
    }

    /**
     * remove response handler
     *
     * @param responseHandler ResponseHandler to remove
     */
    @ObjectiveCName("removeResponseHandler:")
    public void removeResponseHandler(ResponseHandler responseHandler)
    {
        this.responseHandlers.remove(responseHandler);
    }

    /**
     * Set RequestHandler
     *
     * @param requestHandler request handler to set or null ot unset it
     */
    @ObjectiveCName("setGlobalRequestHandler:")
    public void setGlobalRequestHandler(org.skynetsoftware.snet.RequestHandler requestHandler)
    {
        this.mGlobalRequestHandler = requestHandler;
    }

    /**
     * Set NoInternetConnectionHandler
     *
     * @param noInternetConnectionHandler NoInternetConnection handler to set or null to remove it
     */
    @ObjectiveCName("setNoInternetConnectionHandler:")
    public void setNoInternetConnectionHandler(NoInternetConnectionHandler noInternetConnectionHandler)
    {
        this.noInternetConnectionHandler = noInternetConnectionHandler;
    }
}
