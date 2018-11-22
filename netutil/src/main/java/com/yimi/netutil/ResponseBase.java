package com.yimi.netutil;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by liuniu on 2017/12/15.
 */

public class ResponseBase implements Parcelable, Serializable {
    private static final long serialVersionUID = 1;

    public String msg;
    public String code;

    /**
     * client add
     * Indicates whether data is from cache
     */
    protected boolean isCache;
    /**
     * client add
     * Indicates whether data is success
     */
    protected boolean isSuccess;
    /**
     * client add
     * 失败时用于保存失败的code,成功时忽略此字段
     */
    public String failCode;
    /**
     * client add
     * 成功时,如果是网络数据,可以使用此字段标记是否与缓存一致
     */
    protected boolean isRemoteEqualsToCache;

    public static final Creator<ResponseBase> CREATOR = new Creator<ResponseBase>() {
        @Override
        public ResponseBase createFromParcel(Parcel in) {
            return new ResponseBase(in);
        }

        @Override
        public ResponseBase[] newArray(int size) {
            return new ResponseBase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.msg);
        dest.writeString(this.code);
    }

    public ResponseBase() {
    }

    protected ResponseBase(Parcel in) {
        this.msg = in.readString();
        this.code = in.readString();
    }

    @Override
    public String toString() {
        return "ResponseBase{" +
                "msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        /*
        if (this == o) return true;
        if (!(o instanceof ResponseBase)) return false;

        ResponseBase that = (ResponseBase) o;

        if (msg != null ? !msg.equals(that.msg) : that.msg != null) return false;
        return code != null ? code.equals(that.code) : that.code == null;
        */
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        /*
        int result = msg != null ? msg.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
        */
        return super.hashCode();
    }


    public boolean isCache() {
        return isCache;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setCache(boolean cache) {
        isCache = cache;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public boolean isRemoteEqualsToCache() {
        return isRemoteEqualsToCache;
    }

    public void setRemoteEqualsToCache(boolean isRemoteEqualsToCache) {
        this.isRemoteEqualsToCache = isRemoteEqualsToCache;
    }
}
