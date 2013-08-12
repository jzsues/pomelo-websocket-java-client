package com.zvidia.pomelo.protobuf;

import com.zvidia.pomelo.exception.PomeloException;
import com.zvidia.pomelo.protobuf.Decoder;
import com.zvidia.pomelo.protobuf.Encoder;
import com.zvidia.pomelo.protobuf.ProtoBuf;
import com.zvidia.pomelo.protobuf.ProtoBufParser;
import com.zvidia.pomelo.utils.GzipUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-8
 * Time: 上午10:52
 * To change this template use File | Settings | File Templates.
 */
public class ProtoBufTest {
    int rTimes = 100;

    @Test
    public void testGzip() throws IOException {
        byte[] msgBytes = Files.readAllBytes(Paths.get("D:\\work\\workspace_game\\pomelo_java\\src\\test\\java\\com\\zvidia\\pomelo\\protobuf\\maze_data_1k.json"));
        String msgJsonStr = Charset.forName("UTF-8").decode(ByteBuffer.wrap(msgBytes)).toString();
        long encode_cost = 0L;
        long decode_cost = 0L;
        long all_cost = 0L;
        for (int i = 0; i < rTimes; i++) {
            long start = new Date().getTime();
            String compress = GzipUtils.compress(msgJsonStr);
            long encode_end = new Date().getTime();
            encode_cost += (encode_end - start);
            String decompress = GzipUtils.decompress(compress);
            long decode_end = new Date().getTime();
            decode_cost += (decode_end - encode_end);
            all_cost += (decode_end - start);
        }
        System.out.println("gzip time cost sum:" + all_cost + ",encode cost sum:" + encode_cost + ",decode cost sum:" + decode_cost);
    }

    @Test
    public void testEncode() throws IOException, JSONException {
        byte[] msgBytes = Files.readAllBytes(Paths.get("D:\\work\\workspace_game\\pomelo_java\\src\\test\\java\\com\\zvidia\\pomelo\\protobuf\\maze_data_1k.json"));
        String msgJsonStr = Charset.forName("UTF-8").decode(ByteBuffer.wrap(msgBytes)).toString();
        JSONObject msgs = new JSONObject(msgJsonStr);
        byte[] protoBytes = Files.readAllBytes(Paths.get("D:\\work\\workspace_game\\pomelo_java\\src\\test\\java\\com\\zvidia\\pomelo\\protobuf\\maze_proto.json"));
        String protoJsonStr = Charset.forName("UTF-8").decode(ByteBuffer.wrap(protoBytes)).toString();
        JSONObject _proto = new JSONObject(protoJsonStr);
        JSONObject proto = ProtoBufParser.parse(_proto);
        Encoder encoder = new Encoder(proto);
        Decoder decoder = new Decoder(proto);
        ProtoBuf protoBuf = new ProtoBuf(encoder, decoder);
        Iterator<String> routes = msgs.keys();
        long encode_cost = 0L;
        long decode_cost = 0L;
        long all_cost = 0L;
        for (int i = 0; i < rTimes; i++) {
            while (routes.hasNext()) {
                String route = routes.next();
                long start = new Date().getTime();
                JSONObject msg = msgs.getJSONObject(route);
                try {
                    String msgStr = msg.toString();
                    System.out.println("encode route " + route + ":" + msgStr);
                    byte[] encode = protoBuf.encode(route, msgStr);
                    long encode_end = new Date().getTime();
                    encode_cost += (encode_end - start);
                    System.out.println(Arrays.toString(encode));
                    String decode = protoBuf.decode(route, encode);
                    long decode_end = new Date().getTime();
                    decode_cost += (decode_end - encode_end);
                    all_cost += (decode_end - start);
                    org.junit.Assert.assertEquals("assert result", msgStr, decode);
                    System.out.println("decode:" + decode);
                } catch (PomeloException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        System.out.println("protobuf time cost sum:" + all_cost + ",encode cost sum:" + encode_cost + ",decode cost sum:" + decode_cost);
    }
}
