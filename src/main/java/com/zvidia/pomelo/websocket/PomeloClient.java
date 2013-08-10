package com.zvidia.pomelo.websocket;

import com.zvidia.pomelo.protocol.PomeloPackage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-8
 * Time: 下午10:04
 * To change this template use File | Settings | File Templates.
 */
public class PomeloClient extends WebSocketClient {
    private long heartbeatInterval = 0;
    private long heartbeatTimeout = 0;
    private long nextHeartbeatTimeout = 0;


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

        // new package arrived, update the heartbeat timeout
        if (heartbeatTimeout > 0) {
            nextHeartbeatTimeout = new Date().getTime() + heartbeatTimeout;
        }
    }

    private void handshake(PomeloPackage.Package decode) {
        String resStr = PomeloPackage.strdecode(decode.getBody());
        System.out.println("handshake resStr: " + resStr);
        JSONObject data = new JSONObject(resStr);
        if (data.isNull(HandshakeProvider.RES_CODE_KEY)) {
            System.out.println("handshake res data error!");
            return;
        }
        int code = data.getInt(HandshakeProvider.RES_CODE_KEY);
        if (HandshakeProvider.RES_OLD_CLIENT == code) {
            System.out.println("old handshake version!");
            return;
        }
        if (HandshakeProvider.RES_FAIL == code) {
            System.out.println("handshake fail!");
            return;
        }
        handshakeInit(data);
        //send ack msg
        byte[] ackBytes = PomeloPackage.encode(PomeloPackage.TYPE_HANDSHAKE_ACK, null);
        send(ackBytes);
    }

    private void handshakeInit(JSONObject data) {
        if (!data.isNull(HandshakeProvider.HANDSHAKE_SYS_KEY)) {
            JSONObject sys = data.getJSONObject(HandshakeProvider.HANDSHAKE_SYS_KEY);
            if (!sys.isNull(HandshakeProvider.HANDSHAKE_SYS_HEARTBEAT_KEY)) {
                long heartbeat = sys.getLong(HandshakeProvider.HANDSHAKE_SYS_HEARTBEAT_KEY);
                heartbeatInterval = heartbeat * 1000;   // heartbeat interval
                heartbeatTimeout = heartbeatInterval * 2;        // max heartbeat timeout
            } else {
                heartbeatInterval = 0;
                heartbeatTimeout = 0;
            }
        } else {
            heartbeatInterval = 0;
            heartbeatTimeout = 0;
        }
        initData(data);
    }

    private void initData(JSONObject data) {
        if (data == null || data.isNull(HandshakeProvider.HANDSHAKE_SYS_KEY)) {
            System.out.println("data format error!");
            return;
        }
        JSONObject sys = data.getJSONObject(HandshakeProvider.HANDSHAKE_SYS_KEY);
        if (!sys.isNull(HandshakeProvider.HANDSHAKE_SYS_DICT_KEY)) {
            JSONObject dict = sys.getJSONObject(HandshakeProvider.HANDSHAKE_SYS_DICT_KEY);
            System.out.println("sys.dict:" + dict.toString());
        }
        if (!sys.isNull(HandshakeProvider.HANDSHAKE_SYS_PROTOS_KEY)) {
            JSONObject protos = sys.getJSONObject(HandshakeProvider.HANDSHAKE_SYS_PROTOS_KEY);
            int version = protos.getIntNullable(HandshakeProvider.HANDSHAKE_SYS_PROTOS_VERSION_KEY);
            JSONObject serverProtos = protos.getJSONObjectNullable(HandshakeProvider.HANDSHAKE_SYS_PROTOS_SERVER_KEY);
            JSONObject clientProtos = protos.getJSONObjectNullable(HandshakeProvider.HANDSHAKE_SYS_PROTOS_CLIENT_KEY);
            System.out.println("sys.protos.version:" + version);
            System.out.println("sys.protos.server:" + serverProtos.toString());
            System.out.println("sys.protos.client:" + clientProtos.toString());
        }


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
