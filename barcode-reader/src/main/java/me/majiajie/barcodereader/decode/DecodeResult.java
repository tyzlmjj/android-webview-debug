package me.majiajie.barcodereader.decode;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.zxing.Result;

/**
 * 解码结果
 */
public class DecodeResult implements Parcelable{

    private String text;

    DecodeResult(Result rawResult) {
        setText(rawResult.getText());
    }

    protected DecodeResult(Parcel in) {
        text = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DecodeResult> CREATOR = new Creator<DecodeResult>() {
        @Override
        public DecodeResult createFromParcel(Parcel in) {
            return new DecodeResult(in);
        }

        @Override
        public DecodeResult[] newArray(int size) {
            return new DecodeResult[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
