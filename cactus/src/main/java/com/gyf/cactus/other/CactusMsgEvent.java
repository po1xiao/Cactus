package com.gyf.cactus.other;

public class CactusMsgEvent<T> {
    private int code;
    private T data;
    public static final int SERVICE_STOP = 0X01;


    public CactusMsgEvent(int code) {
        this.code = code;
    }

    public CactusMsgEvent(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

