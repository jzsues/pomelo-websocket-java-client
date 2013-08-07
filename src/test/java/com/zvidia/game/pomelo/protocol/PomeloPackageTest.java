package com.zvidia.game.pomelo.protocol;

import com.zvidia.game.pomelo.protocol.PomeloPackage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-6
 * Time: 上午10:47
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JUnit4.class)
public class PomeloPackageTest {

    @Test
    public void testStrendecode() {
        String str = "你好, abc~~~艾丝凡";
        int[] buffer = PomeloPackage.strencode(str);
        System.out.println(Arrays.toString(buffer));
        String strdecode = PomeloPackage.strdecode(buffer);
        System.out.println(strdecode);
    }

    @Test
    public void testEndecode() {
        String str = "你好, abc~~~艾丝凡";
        int[] buffer = PomeloPackage.strencode(str);
        System.out.println("buffer:" + Arrays.toString(buffer));
        int type = PomeloPackage.TYPE_HANDSHAKE;
        int[] encode = PomeloPackage.encode(type, buffer);
        System.out.println("encode:" + Arrays.toString(encode));
        PomeloPackage.Package decode = PomeloPackage.decode(encode);
        System.out.println("decode body:" + Arrays.toString(decode.getBody()));
    }

}
