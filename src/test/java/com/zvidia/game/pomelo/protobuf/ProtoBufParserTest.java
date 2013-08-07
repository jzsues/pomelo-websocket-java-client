package com.zvidia.game.pomelo.protobuf;

import org.json.JSONObject;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-7
 * Time: 下午9:40
 * To change this template use File | Settings | File Templates.
 */
public class ProtoBufParserTest {

    @Test
    public void testParse() {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get("D:\\work\\workspace_game\\pomelo_java\\src\\test\\java\\com\\zvidia\\game\\pomelo\\protobuf\\example.json"));
            String jsonStr = Charset.forName("UTF-8").decode(ByteBuffer.wrap(encoded)).toString();
            JSONObject json = new JSONObject(jsonStr);
            System.out.println(json.toString());
            JSONObject parse = ProtoBufParser.parse(json);
            System.out.println(parse.toString());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


}
