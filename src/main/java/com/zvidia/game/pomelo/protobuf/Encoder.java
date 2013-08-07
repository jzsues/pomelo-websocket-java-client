package com.zvidia.game.pomelo.protobuf;

import com.zvidia.game.pomelo.exception.PomeloException;
import com.zvidia.game.pomelo.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-7
 * Time: 下午10:27
 * To change this template use File | Settings | File Templates.
 */
public class Encoder {


    private JSONObject protos;

    public Encoder(JSONObject protos) {
        this.protos = protos;
    }

    public byte[] encode(String proto, String msg) throws PomeloException {
        if (StringUtils.isEmpty(proto) || StringUtils.isEmpty(msg)) {
            throw new PomeloException("Route or msg can not be null : " + msg + ", proto : " + proto);
        }
        if (this.protos.isNull(proto)) {
            throw new PomeloException("check proto failed! msg : " + msg + ", proto : " + proto);
        }
        //Get protos from protos map use the route as key
        JSONObject _proto = this.protos.getJSONObject(proto);
        JSONObject _msg = new JSONObject(msg);

        //check msg
        if (!checkMsg(_msg, _proto)) {
            throw new PomeloException("check msg failed! msg : " + msg + ", proto : " + proto);
        }

        int length = msg.getBytes().length * 2;

        ByteBuffer buffer = ByteBuffer.allocate(length);
        int offset = 0;
        if (_proto != null) {
            offset = encodeMsg(buffer, offset, _proto, _msg);
            return buffer.array();
        }
        return null;
    }

    private boolean checkMsg(JSONObject msg, JSONObject proto) throws PomeloException {
        if (msg == null || proto == null) {
            throw new PomeloException("check msg failed! msg : " + msg + ", proto : " + proto);
        }
        Set<String> names = proto.keySet();
        for (String name : names) {
            JSONObject value = proto.getJSONObject(name);
            if (!value.isNull(ProtoBufParser.OPTION_KEY)) {
                String option = value.getString(ProtoBufParser.OPTION_KEY);
                String type = value.getString(ProtoBufParser.TYPE_KEY);
                JSONObject messages = value.getJSONObject(ProtoBufParser.MESSAGES_KEY);
                JSONObject tags = value.getJSONObject(ProtoBufParser.TAGS_KEY);
                MessageOption messageOption = MessageOption.valueOf(option);
                switch (messageOption) {
                    case required: {
                        if (msg.isNull(name)) {
                            //no property exist for required!
                            return false;
                        }
                    }
                    case optional: {
                        if (!msg.isNull(name)) {
                            if (!msg.isNull(name) && !messages.isNull(type) && !checkMsg(msg.getJSONObject(name), messages.getJSONObject(type))) {
                                //inner proto error!
                                return false;
                            }
                        }
                        break;
                    }
                    case repeated: {
                        if (!msg.isNull(name) && !messages.isNull(type)) {
                            JSONArray array = msg.getJSONArray(name);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                if (!checkMsg(obj, messages.getJSONObject(type))) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private int encodeMsg(ByteBuffer buffer, int offset, JSONObject proto, JSONObject msg) {
        Set<String> names = msg.keySet();
        for (String name : names) {
            Object value = msg.get(name);
            JSONObject _proto = proto.getJSONObject(name);
            String option = _proto.getString(ProtoBufParser.OPTION_KEY);
            String type = _proto.getString(ProtoBufParser.TYPE_KEY);
            int tag = _proto.getInt(ProtoBufParser.TAG_KEY);
            MessageOption _option = MessageOption.valueOf(option);
            switch (_option) {
                case required:
                case optional: {
                    offset = writeBytes(buffer, offset, encodeTag(type, tag));
                    offset = encodeProp(value, type, offset, buffer, _proto);
                    break;
                }
                case repeated: {
                    if (value != null) {
                        offset = encodeArray((JSONArray) value, _proto, offset, buffer, proto);
                    }
                    break;
                }
            }
        }
        return offset;
    }

    private int encodeArray(JSONArray array, JSONObject _proto, int offset, ByteBuffer buffer, JSONObject proto) {
        String type = _proto.getString(ProtoBufParser.TYPE_KEY);
        int tag = _proto.getInt(ProtoBufParser.TAG_KEY);
        WireType _type = WireType.valueOf(type);

        if (_type != WireType._string && _type != WireType._message) {
            //simple type
            offset = writeBytes(buffer, offset, encodeTag(type, tag));
            offset = writeBytes(buffer, offset, Codec.encodeUInt32(array.length()));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                offset = encodeProp(obj, type, offset, buffer, proto);
            }
        }
        return offset;
    }

    private int encodeProp(Object value, String type, int offset, ByteBuffer buffer, JSONObject proto) {
        WireType _type = WireType.valueOf("_" + type);

        switch (_type) {
            case _uInt32: {
                long _value = Long.parseLong(value.toString());
                offset = writeBytes(buffer, offset, Codec.encodeUInt32(_value));
                break;
            }
            case _int32:
            case _sInt32: {
                int _value = Integer.parseInt(value.toString());
                offset = writeBytes(buffer, offset, Codec.encodeSInt32(_value));
                break;
            }
            case _float: {
                float _value = Float.parseFloat(value.toString());
                buffer.putFloat(_value);
                offset += 4;
                break;
            }
            case _double: {
                double _value = Double.parseDouble(value.toString());
                buffer.putDouble(_value);
                offset += 8;
                break;
            }
            case _string: {
                String _value = value.toString();
                byte[] bytes = _value.getBytes(ProtoBufParser.DEFAULT_CHARSET);
                int length = bytes.length;
                offset = writeBytes(buffer, offset, Codec.encodeUInt32(length));
                offset = writeBytes(buffer, offset, bytes);
                break;
            }
            default: {
                JSONObject messages = proto.getJSONObject(ProtoBufParser.MESSAGES_KEY);
                if (!messages.isNull(type)) {
                    ByteBuffer tmpBuffer = ByteBuffer.allocate(value.toString().getBytes(ProtoBufParser.DEFAULT_CHARSET).length * 2);
                    int length = encodeMsg(tmpBuffer, 0, messages.getJSONObject(type), (JSONObject) value);
                    offset = writeBytes(buffer, offset, Codec.encodeUInt32(length));
                    //contact the object
                    buffer.put(tmpBuffer.array(), offset, length);
                    offset += length;
                }
                break;
            }
        }
        return offset;
    }

    private int writeBytes(ByteBuffer buffer, int offset, byte[] bytes) {
        buffer.put(bytes, offset, bytes.length);
        return offset + bytes.length;
    }

    private byte[] encodeTag(String type, int tag) {
        WireType _type = WireType.valueOf("_" + type);
        return Codec.encodeUInt32(tag);
    }
}
