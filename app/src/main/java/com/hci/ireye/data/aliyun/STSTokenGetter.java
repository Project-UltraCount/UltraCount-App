package com.hci.ireye.data.aliyun;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class STSTokenGetter {

    private static String roleArn = "acs:ram::1394721840664016:role/ramosstest";
    // 自定义角色会话名称，用来区分不同的令牌，例如可填写为SessionTest。
    private static String roleSessionName = "SessionTest";

    public static Map<String, String> getSTSToken() {

        //-----------------------------------------
        try (Response response = new OSSRequestExecutor("AssumeRole")
                .addParam("RoleArn", roleArn)
                .addParam("RoleSessionName", roleSessionName)
                .addParam("DurationSeconds", String.valueOf(3600)) // todo increase max duration on panel
                .execute()) {
            // success
            JsonParser jsonParser = new JsonParser();
            JsonObject data = (JsonObject)jsonParser.parse(response.body().string());
            JsonElement credentials = data.get("Credentials");
            return new Gson().fromJson(credentials.toString(), HashMap.class);
        } catch (Exception e) {
            Log.d("cao", "getSTSToken: " + e.toString());
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(getSTSToken());
    }
}