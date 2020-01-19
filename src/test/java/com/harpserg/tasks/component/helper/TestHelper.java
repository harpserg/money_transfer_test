package com.harpserg.tasks.component.helper;

import com.harpserg.tasks.dto.Money;
import com.harpserg.tasks.dto.MoneyTransferDTO;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Random;
import java.util.UUID;

import static com.harpserg.tasks.converter.GsonConverter.gson;

public class TestHelper {

    public static final String REST_API_BASE = "http://localhost:4567";

    public static Request createMoneyTransferRequest(UUID from, UUID to, long amount) {
        String transferRequest = gson.toJson(MoneyTransferDTO.builder()
                .from(from)
                .to(to)
                .amount(new Money(amount))
                .build());

        RequestBody body = RequestBody.create(transferRequest, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(REST_API_BASE + "/money/transfer")
                .post(body)
                .build();

        return request;
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

}
