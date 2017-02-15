package org.skynetsoftware.snet.example.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.skynetsoftware.snet.AndroidRequestHandler;
import org.skynetsoftware.snet.Request;
import org.skynetsoftware.snet.RequestManager;
import org.skynetsoftware.snet.ResponseHandler;
import org.skynetsoftware.snet.ResponseParser;

public class MainActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView tvResponse = (TextView)findViewById(R.id.tvResponse);

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
