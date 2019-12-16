package com.harpserg.tasks.component;


import com.harpserg.tasks.App;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BaseTest {

    protected final OkHttpClient httpClient = new OkHttpClient();
    protected final String REST_API_BASE = "http://localhost:4567";

    @Before
    public void init() {//TODO refactor, create runner
        String[] args = {};
        App.main(args);
    }

    @Test
    public void serverIsRunning() throws IOException {
        Request request = new Request.Builder().url(REST_API_BASE + "/status").build();

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        assertEquals(200, response.code());
    }

}
