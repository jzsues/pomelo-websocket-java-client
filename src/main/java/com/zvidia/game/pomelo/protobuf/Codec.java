package com.zvidia.game.pomelo.protobuf;

import com.zvidia.game.pomelo.utils.ByteUtils;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-7
 * Time: 下午6:08
 * To change this template use File | Settings | File Templates.
 */
public class Codec {

    public static byte[] encodeUInt32(long num) {
        return ByteUtils.longToBytes(num);
    }

    public static long decodeUInt32(byte[] bytes) {
        return ByteUtils.bytesToLong(bytes);
    }

    public static byte[] encodeSInt32(int num) {
        return ByteUtils.intToBytes(num);
    }

    public static int decodeSInt32(byte[] bytes) {
        return ByteUtils.bytesToInt(bytes);
    }
}
