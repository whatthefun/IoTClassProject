package com.example.user.iotclassproject;

import java.util.HashMap;

/**
 * Created by YUAN on 2017/04/27.
 */

public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    public static String lookup(String uuid) {
        String name = attributes.get(uuid);
        return name == null ? "unknown service" : name;
    }
}
