package com.yimi.netutil.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.yimi.netutil.NetCallback;
import com.yimi.netutil.NetUtils;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "YIMI-TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private NetCallback newNetCallback() {
        return new NetCallback<String>() {
            @Override
            public void onFailure(int resultCode, String resultMsg) {
                Log.d(TAG, "onFailure: " + resultCode + ", " + resultMsg);
            }

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);
            }
        };
    }

    public void onBtnClick(View view) {
        // test url
        String url = "http://tools.yimigit.com/showdoc/index.php?s=/34&page_id=1213";

        // test get
        //NetUtils.getCmsWithParams(url, null, newNetCallback());

        // test post
        //NetUtils.post(url, null, newNetCallback());

        // get url params - 可以为空
        String baseUrl = "http://tools.yimigit.com/showdoc/index.php";
        Map<String, String> params = new HashMap<>();
        params.put("s", "/34");
        params.put("page_id", "1213");

        // test get - TestResponseBean为业务的数据Bean
        NetUtils.getCmsWithParams(baseUrl, params, new NetCallback<TestResponseBean>() {
            @Override
            public void onFailure(int resultCode, String resultMsg) {
                Log.e(TAG, "onFailure: " + resultCode + ", " + resultMsg);
            }

            @Override
            public void onResponse(TestResponseBean response) {
                // 直接使用解析后的 业务的数据Bean
                Log.d(TAG, "onResponse: " + response.code + ", " + response.msg);

                // 也可以使用解析前的 json string -> NetCallback#mResponse
                Log.d(TAG, "onResponse: " + mResponse);
            }
        });
    }

}
