package com.zvidia.game.pomelo.protobuf;

import com.zvidia.game.pomelo.exception.PomeloException;
import com.zvidia.game.pomelo.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-7
 * Time: 下午10:27
 * To change this template use File | Settings | File Templates.
 */
public class Decoder {
    private JSONObject protos;

    private int offset;

    private ByteBuffer buffer;

    public Decoder(JSONObject protos) {
        this.protos = protos;
    }

    public String decode(String proto, byte[] bytes) throws PomeloException {
        if (StringUtils.isEmpty(proto) || bytes == null) {
            throw new PomeloException("Route or msg can not be null, proto : " + proto);
        }
        this.buffer = ByteBuffer.wrap(bytes);
        this.offset = 0;
        if (protos != null) {
            JSONObject jsonObject = decodeMsg(new JSONObject(), protos, bytes.length);
            return jsonObject.toString();
        }
        return null;
    }

    private JSONObject decodeMsg(JSONObject msg, JSONObject proto, int length) {
        while (offset < length) {
            JSONObject head = getHead();
            JSONObject tags = protos.getJSONObject(ProtoBufParser.TAGS_KEY);
            int tag = head.getInt(ProtoBufParser.TAG_KEY);
            int type = head.getInt(ProtoBufParser.TYPE_KEY);
            String name = tags.getString(tag + "");
            JSONObject _proto = proto.getJSONObject(name);
            String option = _proto.getString(ProtoBufParser.OPTION_KEY);
            String _type = _proto.getString(ProtoBufParser.TYPE_KEY);
            MessageOption _option = MessageOption.valueOf(option);
            switch (_option) {
                case optional:
                case required: {
                    Object obj = decodeProp(_type, _proto);
                    msg.put(name, obj);
                    break;
                }
                case repeated: {
                    if (msg.isNull(name)) {
                        msg.put(name, new JSONArray());
                    }
                    JSONArray array = msg.getJSONArray(name);
                    decodeArray(array, _type, _proto);
                    break;
                }
            }
        }
        return msg;
    }

    private boolean isFinsh() {
        JSONObject tags = protos.getJSONObject(ProtoBufParser.TAGS_KEY);
        JSONObject head = peekHead();
        int tag = head.getInt(ProtoBufParser.TAG_KEY);
        return tags.isNull(tag + "");
    }

    private JSONObject getHead() {
        int tag = (int) Codec.decodeUInt32(getBytes(false));
        JSONObject obj = new JSONObject();
        obj.put(ProtoBufParser.TYPE_KEY, tag & 0x7);
        obj.put(ProtoBufParser.TAG_KEY, tag >> 3);
        return obj;
    }

    private JSONObject peekHead() {
        int tag = (int) Codec.decodeUInt32(peekBytes());
        JSONObject obj = new JSONObject();
        obj.put(ProtoBufParser.TYPE_KEY, tag & 0x7);
        obj.put(ProtoBufParser.TAG_KEY, tag >> 3);
        return obj;
    }

    private Object decodeProp(String type, JSONObject proto) {
        WireType _type = WireType.valueOf("_" + type);
        JSONObject messages = proto.getJSONObject(ProtoBufParser.MESSAGES_KEY);
        switch (_type) {
            case _uInt32: {
                return Codec.decodeUInt32(getBytes(false));
            }
            case _int32:
            case _sInt32: {
                return Codec.decodeSInt32(getBytes(false));
            }
            case _float: {
                float aFloat = buffer.getFloat(offset);
                offset += 4;
                return aFloat;
            }
            case _double: {
                double aDouble = buffer.getDouble(offset);
                offset += 8;
                return aDouble;
            }
            case _string: {
                int length = (int) Codec.decodeUInt32(getBytes(false));
                byte[] _bytes = new byte[length];
                buffer.get(_bytes, offset, length);
                offset += length;
                return new String(_bytes, ProtoBufParser.DEFAULT_CHARSET);
            }
            default: {
                if (!messages.isNull(type)) {
                    JSONObject _proto = messages.getJSONObject(type);
                    int length = (int) Codec.decodeUInt32(getBytes(false));
                    JSONObject msg = new JSONObject();
                    decodeMsg(msg, _proto, offset + length);
                    return msg;
                }
                break;
            }
        }
        return null;
    }

    private void decodeArray(JSONArray array, String type, JSONObject proto) {
        int tag = proto.getInt(ProtoBufParser.TAG_KEY);
        WireType _type = WireType.valueOf(type);

        if (_type != WireType._string && _type != WireType._message) {
            //simple type
            int length = (int) Codec.decodeUInt32(getBytes(false));

            for (int i = 0; i < length; i++) {
                Object obj = decodeProp(type, proto);
                array.put(obj);
            }
        } else {
            Object obj = decodeProp(type, proto);
            array.put(obj);
        }
    }

    private byte[] getBytes(boolean flag) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        int pos = offset;
        flag = flag || false;
        byte b = 0;
        do {
            b = buffer.get(pos);
            buf.put(b);
            pos++;
        } while (b >= 128);

        if (!flag) {
            offset = pos;
        }
        return buf.array();
    }

    private byte[] peekBytes() {
        return getBytes(true);
    }
}
