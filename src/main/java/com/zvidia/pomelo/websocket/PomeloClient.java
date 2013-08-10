package com.zvidia.pomelo.websocket;

import com.zvidia.pomelo.exception.PomeloException;
import com.zvidia.pomelo.protobuf.Encoder;
import com.zvidia.pomelo.protobuf.ProtoBuf;
import com.zvidia.pomelo.protocol.PomeloMessage;
import com.zvidia.pomelo.protocol.PomeloPackage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;
import java.util.Set;

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

    private JSONObject protos;

    private JSONObject dict;

    private JSONObject abbrs;

    private JSONObject routeMap;

    private JSONObject clientProtos;

    private JSONObject serverProtos;

    private int protosVersion;

    private ProtoBuf protoBuf;

    private int reqIdIndex = 0;


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
    public void connect() {
        super.connect();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("opened connection");
        if (protoBuf == null) {
            protoBuf = new ProtoBuf();
        }
        JSONObject jsonObject = HandshakeProvider.handshakeObject();
        byte[] strencode = PomeloPackage.strencode(jsonObject.toString());
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
        int type = decode.getType();
        switch (type) {
            case PomeloPackage.TYPE_HANDSHAKE: {
                handshake(decode);
                break;
            }
            case PomeloPackage.TYPE_HEARTBEAT: {
                heartbeat(decode);
                break;
            }
            case PomeloPackage.TYPE_DATA: {
                try {
                    onData(decode);
                } catch (PomeloException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                break;
            }
            case PomeloPackage.TYPE_KICK: {
                onKick(decode);
                break;
            }
        }

        // new package arrived, update the heartbeat timeout
        if (heartbeatTimeout > 0) {
            nextHeartbeatTimeout = new Date().getTime() + heartbeatTimeout;
        }
    }

    public void request(String route, String msg) throws PomeloException {
        reqIdIndex++;
        sendMessage(reqIdIndex, route, msg);
        routeMap.put(reqIdIndex + "", route);
    }

    public void sendMessage(int reqId, String route, String msg) throws PomeloException {
        byte[] bytes = defaultEncode(reqId, route, msg);
        byte[] encode = PomeloPackage.encode(PomeloPackage.TYPE_DATA, bytes);
        send(encode);
    }

    private void heartbeat(PomeloPackage.Package decode) {
        if (heartbeatInterval == 0) {
            //no heartbeat
            return;
        }
        byte[] encode = PomeloPackage.encode(PomeloPackage.TYPE_HEARTBEAT, null);
        send(encode);
        nextHeartbeatTimeout = new Date().getTime() + heartbeatTimeout;
    }

    private void onData(PomeloPackage.Package decode) throws PomeloException {
        PomeloMessage.Message message = defaultDecode(decode.getBody());
        System.out.println("onData msg :" + message.toString());
    }

    private PomeloMessage.Message defaultDecode(byte[] buffer) throws PomeloException {
        PomeloMessage.Message msg = PomeloMessage.decode(buffer);
        if (msg.getId() > 0) {
            int id = msg.getId();
            if (routeMap != null && routeMap.has(id + "")) {
                msg.setRoute(routeMap.getString(id + ""));
                if (msg.getRoute() == null) {
                    throw new PomeloException("msg route can not be null");
                }
            }
        }
        JSONObject jsonObject = deCompose(msg);
        msg.setBodyJson(jsonObject);
        return msg;
    }

    private byte[] defaultEncode(int reqId, String route, String msg) throws PomeloException {
        int type = reqId > 0 ? PomeloMessage.TYPE_REQUEST : PomeloMessage.TYPE_NOTIFY;
        byte[] encode = null;
        //compress message by protobuf
        if (clientProtos != null && clientProtos.has(route)) {
            encode = protoBuf.encode(route, msg);
        } else {
            encode = PomeloPackage.strencode(msg);
        }
        int compressRoute = 0;
        if (dict != null && dict.has(route)) {
            route = dict.getString(route);
            compressRoute = 1;
        }
        return PomeloMessage.encode(reqId, type, compressRoute, route, encode);
    }

    private JSONObject deCompose(PomeloMessage.Message msg) throws PomeloException {
        String route = msg.getRoute();
        int compressRoute = msg.getCompressRoute();
        if (compressRoute > 0) {
            if (abbrs.isNull(route)) {
                return new JSONObject();
            }
        }
        boolean hasRoute = abbrs.has(route);
        route = hasRoute ? abbrs.getString(route) : null;
        msg.setRoute(route);
        if (serverProtos != null && serverProtos.has(route)) {
            String decode = protoBuf.decode(route, msg.getBody());
            return new JSONObject(decode);
        } else {
            String strdecode = PomeloPackage.strdecode(msg.getBody());
            return new JSONObject(strdecode);
        }
    }

    private void onKick(PomeloPackage.Package decode) {
        System.out.println("on kick");
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
            dict = sys.getJSONObject(HandshakeProvider.HANDSHAKE_SYS_DICT_KEY);
            System.out.println("sys.dict:" + dict.toString());
            abbrs = new JSONObject();
            Set<String> routes = dict.keySet();
            for (String route : routes) {
                String key = dict.getString(route);
                abbrs.put(key, route);
            }
        }
        if (!sys.isNull(HandshakeProvider.HANDSHAKE_SYS_PROTOS_KEY)) {
            protos = sys.getJSONObject(HandshakeProvider.HANDSHAKE_SYS_PROTOS_KEY);
            protosVersion = protos.getIntNullable(HandshakeProvider.HANDSHAKE_SYS_PROTOS_VERSION_KEY);
            serverProtos = protos.getJSONObjectNullable(HandshakeProvider.HANDSHAKE_SYS_PROTOS_SERVER_KEY);
            clientProtos = protos.getJSONObjectNullable(HandshakeProvider.HANDSHAKE_SYS_PROTOS_CLIENT_KEY);
            System.out.println("sys.protos.version:" + protosVersion);
            System.out.println("sys.protos.server:" + serverProtos.toString());
            System.out.println("sys.protos.client:" + clientProtos.toString());
            if (protoBuf != null) {
                protoBuf.initProtos(clientProtos, serverProtos);
            }
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
