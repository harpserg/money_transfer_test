package com.harpserg.tasks.component;

import com.harpserg.tasks.component.helper.TestHelper;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ServerTest extends BaseTest {

    @Test
    public void serverIsRunning() throws IOException {
        Request request = new Request.Builder().url(TestHelper.REST_API_BASE + "/status").build();

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        assertEquals(200, response.code());
    }

}
