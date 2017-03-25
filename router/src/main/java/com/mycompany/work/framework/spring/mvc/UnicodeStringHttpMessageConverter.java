package com.mycompany.work.framework.spring.mvc;

import org.springframework.http.converter.StringHttpMessageConverter;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yinwenbo on 16/1/18.
 */
public class UnicodeStringHttpMessageConverter extends StringHttpMessageConverter {

    public UnicodeStringHttpMessageConverter() {
        super(Charset.forName("UTF-8"));
    }

    @Override
    protected List<Charset> getAcceptedCharsets() {
        return Arrays.asList(Charset.forName("UTF-8"));
    }
}
