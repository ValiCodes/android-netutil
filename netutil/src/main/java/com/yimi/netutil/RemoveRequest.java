package com.yimi.netutil;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * Created by liuniu on 2018/4/5.
 */

public class RemoveRequest {
    Call call;
    OkHttpClient client;

    public RemoveRequest(Call call, OkHttpClient client) {
        this.call = call;
        this.client = client;
    }

    public void remove() {
        if (call != null && client != null && !call.isCanceled()) {
            client.dispatcher().executorService().execute(new Runnable() {
                @Override
                public void run() {
                    call.cancel();
                }
            });
        }
    }
}
