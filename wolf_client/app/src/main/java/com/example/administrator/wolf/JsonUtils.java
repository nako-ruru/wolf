package com.example.administrator.wolf;


import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by yund on 2016/3/7.
 */
public class JsonUtils {

    private JsonUtils() {
    }

    public static <T> T toBean(String message, Class<? extends T> clazz) {
        ObjectMapper mapper = newObjectMapper();
        try {
            return mapper.readValue(message, clazz);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String toString(Object o) {
        ObjectMapper mapper = newObjectMapper();
        try {
            return mapper.writeValueAsString(o);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        return mapper;
    }

}
