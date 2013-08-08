package com.zvidia.game.pomelo.protobuf;

import org.junit.Test;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-8
 * Time: 上午10:21
 * To change this template use File | Settings | File Templates.
 */
public class CodecTest {
    int count = 1000;

    @Test
    public void testEncodeUInt64() {
        long limit = Long.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            long number = (long) Math.ceil(Math.random() * limit);
            System.out.println("number:" + number);
            byte[] result = Codec.encodeUInt64(number);
            System.out.println("bytes:" + Arrays.toString(result));
            long _number = Codec.decodeUInt64(result);
            System.out.println("decode number:" + _number);
            System.out.println("result:" + (number == _number));
            org.junit.Assert.assertEquals("assert result", number, _number);
        }
    }

    @Test
    public void testEncodeUInt32() {
        int limit = Integer.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            int number = (int) Math.ceil(Math.random() * limit);
            System.out.println("number:" + number);
            byte[] result = Codec.encodeUInt32(number);
            System.out.println("bytes:" + Arrays.toString(result));
            long _number = Codec.decodeUInt32(result);
            System.out.println("decode number:" + _number);
            System.out.println("result:" + (number == _number));
            org.junit.Assert.assertEquals("assert result", number, _number);
        }
    }

    @Test
    public void testEncodeSInt64() {
        long limit = Long.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            long number = (long) Math.ceil(Math.random() * limit);
            System.out.println("number:" + number);
            byte[] result = Codec.encodeSInt64(number);
            System.out.println("bytes:" + Arrays.toString(result));
            long _number = Codec.decodeSInt64(result);
            System.out.println("decode number:" + _number);
            System.out.println("result:" + (number == _number));
            org.junit.Assert.assertEquals("assert result", number, _number);
        }
    }

    @Test
    public void testEncodeSInt32() {
        int limit = Integer.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            int number = (int) Math.ceil(Math.random() * limit);
            System.out.println("number:" + number);
            byte[] result = Codec.encodeSInt32(number);
            System.out.println("bytes:" + Arrays.toString(result));
            long _number = Codec.decodeSInt32(result);
            System.out.println("decode number:" + _number);
            System.out.println("result:" + (number == _number));
            org.junit.Assert.assertEquals("assert result", number, _number);
        }
    }
}
