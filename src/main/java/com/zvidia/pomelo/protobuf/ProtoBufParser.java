package com.zvidia.pomelo.protobuf;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-7
 * Time: 下午9:06
 * To change this template use File | Settings | File Templates.
 */
public class ProtoBufParser {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String MESSAGES_KEY = "__messages";
    public static final String TAGS_KEY = "__tags";
    public static final String OPTION_KEY = "option";
    public static final String TAG_KEY = "tag";
    public static final String TYPE_KEY = "type";


    public static JSONObject parse(JSONObject proto) {
        JSONObject protos = new JSONObject();
        Set<String> keys = proto.keySet();
        for (String key : keys) {
            JSONObject jsonObject = proto.getJSONObject(key);
            JSONObject object = parseObject(jsonObject);
            protos.put(key, object);
        }
        return protos;
    }

    public static JSONObject parseObject(JSONObject proto) {
        JSONObject protos = new JSONObject();
        JSONObject nestProtos = new JSONObject();
        JSONObject tags = new JSONObject();
        Set<String> keys = proto.keySet();
        for (String key : keys) {
            Object tag = proto.get(key);
            String[] params = key.split(" ");
            String option = params[0];
            MessageOption messageOption = MessageOption.valueOf(option);
            switch (messageOption) {
                case message: {
                    if (params.length != 2) {
                        continue;
                    }
                    String name = params[1];
                    nestProtos.put(name, parseObject((JSONObject) tag));
                    continue;
                }
                case required: {
                    //ignore
                }
                case optional: {
                    //ignore
                }
                case repeated: {
                    //params length should be 3 and tag can't be duplicated
                    String tagStr = tag.toString();
                    int _tag = Integer.parseInt(tagStr);
                    if (params.length != 3 || !tags.isNull(tagStr)) {
                        continue;
                    }
                    String name = params[2];
                    String type = params[1];
                    Proto buf = new Proto(option, type, _tag);
                    protos.put(name, new JSONObject(buf));
                    tags.put(tagStr, name);
                }
            }
        }
        protos.put(MESSAGES_KEY, nestProtos);
        protos.put(TAGS_KEY, tags);
        return protos;
    }

}
