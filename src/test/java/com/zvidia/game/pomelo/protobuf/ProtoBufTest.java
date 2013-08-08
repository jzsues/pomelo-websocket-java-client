package com.zvidia.game.pomelo.protobuf;

import com.zvidia.game.pomelo.exception.PomeloException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-8
 * Time: 上午10:52
 * To change this template use File | Settings | File Templates.
 */
public class ProtoBufTest {

    @Test
    public void testEncode() throws IOException {
        byte[] msgBytes = Files.readAllBytes(Paths.get("D:\\work\\workspace_game\\pomelo_java\\src\\test\\java\\com\\zvidia\\game\\pomelo\\protobuf\\msg.json"));
        String msgJsonStr = Charset.forName("UTF-8").decode(ByteBuffer.wrap(msgBytes)).toString();
        JSONObject msgs = new JSONObject(msgJsonStr);
        byte[] protoBytes = Files.readAllBytes(Paths.get("D:\\work\\workspace_game\\pomelo_java\\src\\test\\java\\com\\zvidia\\game\\pomelo\\protobuf\\example.json"));
        String protoJsonStr = Charset.forName("UTF-8").decode(ByteBuffer.wrap(protoBytes)).toString();
        JSONObject _proto = new JSONObject(protoJsonStr);
        JSONObject proto = ProtoBufParser.parse(_proto);
        Encoder encoder = new Encoder(proto);
        Decoder decoder = new Decoder(proto);
        ProtoBuf protoBuf = new ProtoBuf(encoder, decoder);
        Set<String> routes = msgs.keySet();
        for (String route : routes) {
            JSONObject msg = msgs.getJSONObject(route);
            try {
                String msgStr = msg.toString();
                System.out.println("encode route " + route + ":" + msgStr);
                byte[] encode = protoBuf.encode(route, msgStr);
                System.out.println(Arrays.toString(encode));
                String decode = protoBuf.decode(route, encode);
                //org.junit.Assert.assertEquals("assert result", msgStr, decode);
                System.out.println("decode:" + decode);
            } catch (PomeloException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }
}
