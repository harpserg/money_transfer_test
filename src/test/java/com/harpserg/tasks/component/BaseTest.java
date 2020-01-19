package com.harpserg.tasks.component;


import com.harpserg.tasks.App;
import com.harpserg.tasks.component.helper.TestHelper;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.Spark;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BaseTest {

    protected final OkHttpClient httpClient = new OkHttpClient();

    @Before
    public void init() {
        String[] args = {};
        App.main(args);
    }

    @After
    public void tearDown() throws Exception {
        Thread.sleep(1000);
        Spark.stop();
    }

}
