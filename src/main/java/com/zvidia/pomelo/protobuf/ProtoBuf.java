package com.zvidia.pomelo.protobuf;

import com.zvidia.pomelo.exception.PomeloException;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-8
 * Time: 上午10:05
 * To change this template use File | Settings | File Templates.
 */
public class ProtoBuf {
    private Encoder encoder;

    private Decoder decoder;

    public ProtoBuf(Encoder encoder, Decoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public byte[] encode(String proto, String msg) throws PomeloException {
        return this.encoder.encode(proto, msg);
    }

    public String encodeBase64(String proto, String msg) throws PomeloException {
        byte[] bytes = this.encoder.encode(proto, msg);
        return new String(bytes);
    }

    public String decode(String proto, byte[] bytes) throws PomeloException {
        return this.decoder.decode(proto, bytes);
    }

    public String decodeBase64(String proto, String str) throws PomeloException {
        return this.decoder.decode(proto, str.getBytes());
    }
}
