package com.harpserg.tasks.component;


import com.harpserg.tasks.component.helper.TestHelper;
import com.harpserg.tasks.dto.AccountFullDTO;
import com.harpserg.tasks.dto.AccountShortDTO;
import com.harpserg.tasks.dto.Money;
import okhttp3.*;
import org.junit.Test;

import java.io.IOException;

import static com.harpserg.tasks.converter.GsonConverter.gson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountTest extends BaseTest {

    @Test
    public void whenCreateAccount_thenCorrect() throws IOException {

        AccountShortDTO accountShortDTO = new AccountShortDTO();
        accountShortDTO.setBalance(new Money(10000));
        accountShortDTO.setOwnerName("John");

        RequestBody body = RequestBody.create(gson.toJson(accountShortDTO), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(TestHelper.REST_API_BASE + "/account")
                .post(body)
                .build();

        Call call = httpClient.newCall(request);
        Response response = call.execute();

        assertEquals(201, response.code());
        AccountFullDTO accountFullDTO = gson.fromJson(response.body().string(), AccountFullDTO.class);
        assertNotNull(accountFullDTO.getId());
        assertEquals(10000, accountFullDTO.getBalance().getCents());
        assertEquals("John", accountFullDTO.getOwnerName());
    }

}
