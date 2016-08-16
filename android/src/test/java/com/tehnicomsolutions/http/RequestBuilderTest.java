package com.tehnicomsolutions.http;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by pedja on 20.1.16. 13.54.
 * This class is part of the Tulfie
 * Copyright © 2016 ${OWNER}
 */
public class RequestBuilderTest
{
    @Test
    public void requestBuilder_addUrlPart()
    {
        //assertThat(Utility.isEmailValid("name@email.com"), is(true));
        Request builder = new Request(Request.Method.POST);
        builder.setRequestUrl("http://example.com/");
        builder.addUrlPart("user");
        builder.addUrlPart("login");
        assertThat(builder.getRequestUrl(), is("http://example.com/user/login"));

        builder = new Request(Request.Method.POST);
        builder.setRequestUrl("http://example.com/");
        builder.addUrlPart("/user");
        builder.addUrlPart("login/");
        assertThat(builder.getRequestUrl(), is("http://example.com/user/login/"));

        builder = new Request(Request.Method.POST);
        builder.setRequestUrl("http://example.com/");
        builder.addUrlPart("/user/");
        builder.addUrlPart("/login");
        assertThat(builder.getRequestUrl(), is("http://example.com/user/login"));

        builder = new Request(Request.Method.POST);
        builder.setRequestUrl("http://example.com/");
        builder.addUrlPart("/user");
        builder.addUrlPart("/login");
        assertThat(builder.getRequestUrl(), is("http://example.com/user/login"));
    }
}
