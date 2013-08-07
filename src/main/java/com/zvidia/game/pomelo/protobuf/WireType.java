package com.zvidia.game.pomelo.protobuf;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-7
 * Time: 下午8:41
 * To change this template use File | Settings | File Templates.
 */
public enum WireType {
    _uInt32(0),
    _sInt32(0),
    _int32(0),
    _double(1), //double
    _string(2),
    _message(2),
    _float(5); //float

    int value;

    private WireType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
