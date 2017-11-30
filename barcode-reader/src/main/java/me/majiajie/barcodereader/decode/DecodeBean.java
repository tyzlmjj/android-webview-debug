package me.majiajie.barcodereader.decode;

import android.graphics.Rect;

/**
 * 解码需要的数据模型
 */
public class DecodeBean
{

    private byte[] data;//图片数据

    private int width;//图片宽度

    private int height;//图片高度

    private Rect rect;//需要解码识别的区域

    public DecodeBean(byte[] data, int width, int height, Rect rect)
    {
        this.data = data;
        this.width = width;
        this.height = height;
        this.rect = rect;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Rect getRect() {
        return rect;
    }
}
