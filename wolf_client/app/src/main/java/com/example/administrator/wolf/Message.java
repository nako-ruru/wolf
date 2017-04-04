package com.example.administrator.wolf;


import java.util.Map;

/**
 *
 * @author Administrator
 */
public class Message {

    private String code;
    private Map<String, String> properties;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return JsonUtils.toString(this);
    }

}
