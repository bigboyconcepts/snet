ts-http
====================

Async http library


Usage
=====


Including In Your Project
-------------------------

Add maven url in repositories:
```groovy
maven { url 'http://maven.android-forever.com/' }
```
Add the library to your dependencies:
```groovy
compile 'org.skynetsoftware:snet-android:3.1.0'
```

Example
-------------------------

You use `RequestManager` for all requests  

#### First initialize everything. 
You should probably put this in your `Application` class  
You only need to do it once.

This is and example for android.

``` java
Network network = new AndroidNetwork(this);//this is used to check if network connection is available
Internet internet = new AndroidInternet(this);//used to make actual requests, you can create your own and extend Internet
UI ui = new AndroidUI(this);//used for diaplaying messages to user
TextManager textManager = new AndroidTextManager(this);//used for getting text strings

SNet.initialize(network, internet, ui, textManager);//main initialization method

Request.setDefaultRequestUrl(API.API_REQUEST_URL);//you can use this to set global request url or you can set it on each Request object sepparately
RequestManager.initialize(new AndroidRequestManager());//initialize request manager, this is the main class for making requests
SNet.LOGGING = true;//enable or disable logging
```

#### Get RequestManager instance
`RequestManager` is singleton, you can get it like this

``` java
RequestManager manager = RequestManager.getInstance();
```  

#### Add a response handler callback

```java
manager.addResponseHandler(new ResponseHandler()
{
    @Override
    public void onResponse(int requestCode, int responseStatus, ResponseParser responseParser)
    {
        //you can handle response here, for example show it
        tvResponse.setText(responseParser.getServerResponse().responseData);
        //you could also create your own implementation of ResponseParser that will parse the response and return the result
    }
});
```

Make sure that you remove response handler when you are done with it. For example in your activities `onDestroy` method

#### Create a request

To create a request you will use `Request` class

```java
Request request = new Request(Request.Method.POST);//create new Request with HTTP POST method
request.setPostMethod(Request.PostMethod.X_WWW_FORM_URL_ENCODED);//set post method, X_WWW_FORM_URL_ENCODED is default so you don't need to set it
request.setRequestUrl("http://example.com/api/v1/");//set a request url. You can also call static method `setDefaultRequestUrl` once, instead of setting is every time
request.addUrlPart("members");//this will add param as part of the url. eg. http://tulfie.conveo.net/api/v1/member/login
request.addUrlPart("login");
request.addParam("username", "predragcokulov@gmail.com");//add POST param, if HTTP method was get, then this would put param in url. eg. http://tulfie.conveo.net/api/v1/member/login?username=predragcokulov@gmail.com
request.addParam("password", 123456);
```

#### Execute the request

```java
manager.executeAsync(request, REQUEST_CODE_LOGIN);//executed on a worker thread, result is delivered in `ResponseHandler`
```
or
```java
manager.executeSync(request, REQUEST_CODE_LOGIN);//executed on a caller thread, result is returned
```

By default request will be executed using default implementation `SimpleRequestHandler`  
You can create your own and set it using  

```java
request.setRequestHandler(RequestHandler);
```
or globaly
```java
Request.getInstance().setGlobalRequestHandler(RequestHandler);
```

Developed By
============

* Predrag Čokulov - <predragcokulov@gmail.com>



License
=======

    Copyright 2014 Predrag Čokulov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



 [1]: https://github.com/pedja1/FontWidgets/releases