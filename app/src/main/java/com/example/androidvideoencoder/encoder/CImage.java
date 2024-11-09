package com.example.androidvideoencoder.encoder;

import java.nio.ByteBuffer;

public class CImage {

    int width;
    int height;
    int channels;
    ByteBuffer data;

    public CImage(int width, int height, int channels, ByteBuffer data) {
        this.width = width;
        this.height = height;
        this.channels = channels;
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }
}
