package com.tashicell.sop.Utility;

import com.squareup.okhttp.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*public class sms {
    //public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    final OkHttpClient client = new OkHttpClient();

    public Response post(HashMap<String, String> hashMap) throws IOException {
       // RequestBody body = RequestBody.create(json, JSON);
        FormEncodingBuilder builder = new FormEncodingBuilder();

        Resource resource = new ClassPathResource("/smscDetails.properties");
        Properties props = PropertiesLoaderUtils.loadProperties(resource);

        for(Map.Entry<String, String> entry: hashMap.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(props.getProperty("sms.url.firstPart"))
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        else
        {
        	//System.out.println(response.toString());
            return response;
        }
       
    }
}*/

public class sms {
    //public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    final OkHttpClient client = new OkHttpClient();

    public Response post(HashMap<String, String> hashMap) throws IOException {
        // RequestBody body = RequestBody.create(json, JSON);
        // FormEncodingBuilder builder = new FormEncodingBuilder();

        Resource resource = new ClassPathResource("/smscDetails.properties");
        Properties props = PropertiesLoaderUtils.loadProperties(resource);

//        for(Map.Entry<String, String> entry: hashMap.entrySet()) {
//            builder.add(entry.getKey(), entry.getValue());
//        }
//
//        RequestBody requestBody = builder.build();
//
//        Request request = new Request.Builder()
//                .url(props.getProperty("sms.url.firstPart"))
//                //.post(requestBody)
//                .get()
//                .build();

        // Create a URL with the parameters
        HttpUrl.Builder urlBuilder = HttpUrl.parse(props.getProperty("sms.url.firstPart")).newBuilder();

        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        HttpUrl url = urlBuilder.build();

        // Create a GET request with the final URL
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        else
        {
            //System.out.println(response.toString());
            return response;
        }

    }
}
