package com.zvidia.pomelo.protobuf;

import com.zvidia.pomelo.utils.ByteUtils;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-7
 * Time: 下午6:08
 * To change this template use File | Settings | File Templates.
 */
public class Codec {

    public static byte[] encodeUInt32(int num) {
        return ByteUtils.intToBytes(num);
    }

    public static int decodeUInt32(byte[] bytes) {
		int result = 0;
		for(int i=0; i<bytes.length; i++)
		{
			byte tmp = bytes[i];
			result += (tmp & 0x7f) << 7 * i;
			if(tmp > 0)
			{
				return result;
			}
		}

		return result;
    }

    public static byte[] encodeSInt32(int num) {
        return ByteUtils.intToBytes(num);
    }

    public static int decodeSInt32(byte[] bytes) {
		int uResult = Codec.decodeUInt32(bytes);
		int flag = ((uResult % 2) == 1 ) ? -1 : 1;

		return ((uResult % 2 + uResult)/2) * flag;
    }

    public static byte[] encodeUInt64(long num) {
        return ByteUtils.longToBytes(num);
    }

    public static long decodeUInt64(byte[] bytes) {
        return ByteUtils.bytesToLong(bytes);
    }

    public static byte[] encodeSInt64(long num) {
        return ByteUtils.longToBytes(num);
    }

    public static long decodeSInt64(byte[] bytes) {
        return ByteUtils.bytesToLong(bytes);
    }


}
