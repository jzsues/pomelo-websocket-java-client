package com.zvidia.pomelo.protocol;

import com.zvidia.pomelo.exception.PomeloException;
import com.zvidia.pomelo.protocol.PomeloMessage;
import com.zvidia.pomelo.protocol.PomeloPackage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-6
 * Time: 下午1:54
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JUnit4.class)
public class PomeloMessageTest {
    @Test
    public void testEncodeNoCompressUseRoute() {
        int id = 128;
        int compress = 0;
        String route = "connector.entryHandler.entry";
        String msg = "hello wor231ld~";
        try {
            System.out.println("----begin-----");
            System.out.println("encode msg str:" + msg);
            byte[] encode = PomeloMessage.encode(id, PomeloMessage.TYPE_REQUEST, compress, route, PomeloPackage.strencode(msg));
            System.out.println("encode:" + Arrays.toString(encode));
            PomeloMessage.Message decode = PomeloMessage.decode(encode);
            System.out.println("decode:" + decode);
            System.out.println("decode body str:" + PomeloPackage.strdecode(decode.getBody()));
            System.out.println("----end-----");
        } catch (PomeloException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEncodeNoCompressNoRoute() {
        int id = 256;
        int compress = 0;
        String route = "";
        String msg = "hello wor11ld~!!";
        try {
            System.out.println("----begin-----");
            System.out.println("encode msg str:" + msg);
            byte[] encode = PomeloMessage.encode(id, PomeloMessage.TYPE_REQUEST, compress, route, PomeloPackage.strencode(msg));
            System.out.println("encode:" + Arrays.toString(encode));
            PomeloMessage.Message decode = PomeloMessage.decode(encode);
            System.out.println("decode:" + decode);
            System.out.println("decode body str:" + PomeloPackage.strdecode(decode.getBody()));
            System.out.println("----end-----");
        } catch (PomeloException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEncodeNoCompressNullRoute() {
        int n = (int) Math.floor(10000 * Math.random());
        int id = 256 + n;
        int compress = 0;
        String route = null;
        String msg = "hello wor33ld~!!";
        try {
            System.out.println("----begin-----");
            System.out.println("encode msg str:" + msg);
            byte[] encode = PomeloMessage.encode(id, PomeloMessage.TYPE_REQUEST, compress, route, PomeloPackage.strencode(msg));
            System.out.println("encode:" + Arrays.toString(encode));
            PomeloMessage.Message decode = PomeloMessage.decode(encode);
            System.out.println("decode:" + decode);
            System.out.println("decode body str:" + PomeloPackage.strdecode(decode.getBody()));
            System.out.println("----end-----");
        } catch (PomeloException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEncodeCompressNumRoute() {
        int n = (int) Math.floor(10000 * Math.random());
        int id = 256 + n;
        int compress = 1;
        String route = "3";
        String msg = "hello world~!123!";
        try {
            System.out.println("----begin-----");
            System.out.println("encode msg str:" + msg);
            byte[] encode = PomeloMessage.encode(id, PomeloMessage.TYPE_REQUEST, compress, route, PomeloPackage.strencode(msg));
            System.out.println("encode:" + Arrays.toString(encode));
            PomeloMessage.Message decode = PomeloMessage.decode(encode);
            System.out.println("decode:" + decode);
            System.out.println("decode body str:" + PomeloPackage.strdecode(decode.getBody()));
            System.out.println("----end-----");
        } catch (PomeloException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEncodeCompressNumRoute1() {
        int n = (int) Math.pow(3, 10);
        int id = 256 + n;
        int compress = 1;
        String route = "3";
        String msg = "hello world~!!111111111111111111111111111";
        try {
            System.out.println("----begin-----");
            System.out.println("encode msg str:" + msg);
            byte[] encode = PomeloMessage.encode(id, PomeloMessage.TYPE_REQUEST, compress, route, PomeloPackage.strencode(msg));
            System.out.println("encode:" + Arrays.toString(encode));
            PomeloMessage.Message decode = PomeloMessage.decode(encode);
            System.out.println("decode:" + decode);
            System.out.println("decode body str:" + PomeloPackage.strdecode(decode.getBody()));
            System.out.println("----end-----");
        } catch (PomeloException e) {
            e.printStackTrace();
        }
    }
}
