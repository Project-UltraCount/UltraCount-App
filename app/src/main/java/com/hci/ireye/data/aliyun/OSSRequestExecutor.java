package com.hci.ireye.data.aliyun;

import org.apache.commons.net.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OSSRequestExecutor {
    private final Map<String, String> mParameters;

    private static final String  API = "https://sts.aliyuncs.com";

    private static final String ACCESS_KEY_ID = "LTAI5tHgN6uT3pkpyUvEswof";  // RAM AccessKeyId
    private static final String ACCESS_KEY_SECRET = "CeVAftQ9XdLiznCe0xzI2hyl9LRT5I";

    private static String percentEncode(String value) {
        try {
            return value != null ? URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~") : null;
        } catch (UnsupportedEncodingException e) {
            return "IEATSHITIFITHAPPENS";
        }
    }

    private static String getSignature(Map<String, String> parameters) {
        final String HTTP_METHOD = "GET";
        String[] sortedKeys = parameters.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys);
        final String SEPARATOR = "&";

        // 构造 stringToSign 字符串
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(HTTP_METHOD).append(SEPARATOR);
        stringToSign.append(percentEncode("/")).append(SEPARATOR);
        StringBuilder canonicalizedQueryString = new StringBuilder();
        for (String key : sortedKeys) { // 这里注意编码 key 和 value
            canonicalizedQueryString.append("&")
                    .append(percentEncode(key)).append("=")
                    .append(percentEncode(parameters.get(key)));
        }

        // 这里注意编码 canonicalizedQueryString
        stringToSign.append(percentEncode(
                canonicalizedQueryString.substring(1)));

        // 以下是一段计算签名的示例代码
        final String ALGORITHM = "HmacSHA1";
        final String ENCODING = "UTF-8";
        final String key = ACCESS_KEY_SECRET + "&";
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(ENCODING), ALGORITHM));
            byte[] signData = mac.doFinal(stringToSign.toString().getBytes(ENCODING));
            return new String(Base64.encodeBase64(signData));
        } catch (Exception e) {
            return "ERRORRRRRR!!!!!"; // we should not end up here
        }
    }

    public OSSRequestExecutor(String action) {
        this.mParameters = new HashMap<>();
        mParameters.put("Action", action);

    }

    public OSSRequestExecutor addParam(String key, String value) {
        mParameters.put(key, value);
        return this;
    }

    /*
     send http request
     */
    public Response execute() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = build();
        return client.newCall(request).execute();
    }

    private Request build() {
        mParameters.put("Version", "2015-04-01");
        mParameters.put("Format", "JSON");
        mParameters.put("AccessKeyId", ACCESS_KEY_ID);

        mParameters.put("SignatureMethod", "HMAC-SHA1");
        mParameters.put("SignatureNonce", UUID.randomUUID().toString());
        mParameters.put("SignatureVersion", "1.0");
        mParameters.put("Timestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));

        mParameters.put("Signature", getSignature(mParameters));

        HttpUrl.Builder urlBuilder = HttpUrl.parse(API).newBuilder();



        for (Map.Entry<String, String> entry : mParameters.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        HttpUrl constructedUrl = urlBuilder.build();
        return new Request.Builder().url(constructedUrl).build();
    }

}
