package com.tehnicomsolutions.tshttp.example.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tehnicomsolutions.http.AndroidInternet;
import com.tehnicomsolutions.http.AndroidNetwork;
import com.tehnicomsolutions.http.AndroidRequestHandler;
import com.tehnicomsolutions.http.AndroidRequestManager;
import com.tehnicomsolutions.http.AndroidTextManager;
import com.tehnicomsolutions.http.AndroidUI;
import com.tehnicomsolutions.http.Http;
import com.tehnicomsolutions.http.Internet;
import com.tehnicomsolutions.http.Network;
import com.tehnicomsolutions.http.Request;
import com.tehnicomsolutions.http.RequestManager;
import com.tehnicomsolutions.http.ResponseHandler;
import com.tehnicomsolutions.http.ResponseParser;
import com.tehnicomsolutions.http.TextManager;
import com.tehnicomsolutions.http.ToastUtility;
import com.tehnicomsolutions.http.UI;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView tvResponse = (TextView)findViewById(R.id.tvResponse);

        ToastUtility.showToast(this, "Bla bla bla");
        ToastUtility.showToast(this, "Bla bla bla2");

        Network network = new AndroidNetwork(this.getApplicationContext());
        Internet internet = new AndroidInternet(this.getApplicationContext());
        UI ui = new AndroidUI(this.getApplicationContext());
        TextManager textManager = new AndroidTextManager(this.getApplicationContext());
        RequestManager.initialize(new AndroidRequestManager());

        Http.initialize(network, internet, ui, textManager);

        RequestManager.getInstance().setGlobalRequestHandler(new AndroidRequestHandler(this));

        RequestManager.getInstance().addResponseHandler(new ResponseHandler()
        {
            @Override
            public void onResponse(int requestCode, int responseStatus, ResponseParser responseParser)
            {
                tvResponse.setText(responseParser.getServerResponse().responseDataString);
            }
        });
    }

    public void makeRequest(View view)
    {
        Request builder = new Request(Request.Method.POST);
        builder.setPostMethod(Request.PostMethod.X_WWW_FORM_URL_ENCODED);
        builder.setRequestUrl("http://tulfie.conveo.net/api/v1/members/login");
        builder.addParam("asdsad", "asfdsfds  safddsfsd fsdsf");
        RequestManager.getInstance().executeAsync(builder, 0);
    }
}
