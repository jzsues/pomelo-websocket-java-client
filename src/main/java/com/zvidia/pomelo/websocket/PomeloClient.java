package com.zvidia.pomelo.websocket;

import com.zvidia.pomelo.protocol.PomeloPackage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-8
 * Time: 下午10:04
 * To change this template use File | Settings | File Templates.
 */
public class PomeloClient extends WebSocketClient {

    public PomeloClient(URI serverURI) {
        super(serverURI);
    }

    public PomeloClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public PomeloClient(URI serverUri, Draft draft, Map<String, String> headers, int connecttimeout) {
        super(serverUri, draft, headers, connecttimeout);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("opened connection");
        JSONObject jsonObject = HandshakeProvider.handshakeObject();
        int[] strencode = PomeloPackage.strencode(jsonObject.toString());
        byte[] encode = PomeloPackage.encode(PomeloPackage.TYPE_HANDSHAKE, strencode);
        send(encode);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
    }

    @Override
    public void onMessage(ByteBuffer buffer) {
        System.out.println("received buffer: " + buffer);
        byte[] array = buffer.array();
        PomeloPackage.Package decode = PomeloPackage.decode(array);
        System.out.println("received decode package: " + decode);
        if (decode.getType() == PomeloPackage.TYPE_HANDSHAKE) {
            handshake(decode);
        }
    }

    public void handshake(PomeloPackage.Package decode) {
        String ack = PomeloPackage.strdecode(decode.getBody());
        System.out.println("handshake ack: " + ack);
    }

    @Override
    public void onClose(int code, String msg, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us"));
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
}
