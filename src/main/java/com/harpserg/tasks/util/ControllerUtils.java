package com.harpserg.tasks.util;

import com.google.gson.Gson;
import spark.Request;

import java.util.UUID;

public class ControllerUtils {

    private static final Gson gson = new Gson();

    public static String generateUri(Request request, UUID id) {
        String uri = request.url();
        if (uri.matches(".*task/\\d.*")) {
            uri = uri.substring(0, uri.lastIndexOf("/"));
        }
        return uri + "/" + id;
    }

}
