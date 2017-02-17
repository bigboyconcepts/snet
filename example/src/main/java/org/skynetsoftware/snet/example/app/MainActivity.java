package org.skynetsoftware.snet.example.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
        Request request = new Request(Request.Method.GET);
        request.setRequestHandler(new MySmartRequestHandler(this));
        request.setRequestUrl("http://n551jk.com/demo/places_v2.json");
        RequestManager.getInstance().executeAsync(request, MySmartRequestHandler.REQUEST_CODE_PLACES);
    }
}
