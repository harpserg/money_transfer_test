package com.harpserg.tasks.converter;

import com.google.gson.*;
import com.harpserg.tasks.dto.Money;

import java.util.Locale;

public class GsonConverter {

    public static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<Money> serializer = (money, type, jsonSerializationContext) -> new JsonPrimitive(String.format(Locale.US, "%.2f", ((float) money.getCents()) / 100));
        JsonDeserializer<Money> deserializer = (jsonElement, type, jsonDeserializationContext) -> new Money((long) (jsonElement.getAsFloat() * 100));

        gsonBuilder.registerTypeAdapter(Money.class, deserializer);
        gsonBuilder.registerTypeAdapter(Money.class, serializer);

        gson = gsonBuilder.create();
    }

}
