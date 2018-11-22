package com.yimi.netutil.progressdialog;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.yimi.netutil.R;

import java.lang.ref.WeakReference;

/**
 * Created by liuniu on 2018/3/6.
 */

public class QProgressDialog extends ProgressDialog {
    public static final String TAG = "QProgressDialog";
    private Context context;
    public final static long delayMillis = 10;
    private ObjectAnimator objectAnimator;
    private Handler handler = new DelayHandler(this);
    private boolean hasSendMessage = false;
    private TextView loading_text;
    private CharSequence loadingText;

    public QProgressDialog(Context context) {
        super(context);
        this.context = context;
    }

    public QProgressDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadContent();
    }

    private void loadContent() {
        setContentView(R.layout.qprogress_layout);
        loading_text = (TextView) findViewById(R.id.loading_text);
        if (loadingText != null && !TextUtils.isEmpty(loadingText.toString())) {
            loading_text.setText(loadingText);
        }
        objectAnimator = ObjectAnimator.ofFloat(findViewById(R.id.loading_circle), "rotation", 0f, 360f);
        objectAnimator.setDuration(1000).setRepeatCount(-1);
        objectAnimator.setInterpolator(null);
        if (getWindow() != null) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes(layoutParams);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (objectAnimator != null) {
            objectAnimator.start();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
    }

    @Override
    public void setMessage(CharSequence message) {
        this.loadingText = message;
        if (message != null && !TextUtils.isEmpty(message.toString()) && loading_text != null) {
            loading_text.setText(message);
        }
    }

    public static void show(QProgressDialog dialog) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public static void dismiss(QProgressDialog dialog) {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "Dialog disimss exception" + e.getMessage());//dialog在Activity关闭之后disimss会出现IllegalArgumentException
        }
    }

    @Override
    public void show() {
        hasSendMessage = true;
        handler.sendEmptyMessageDelayed(0, delayMillis);
    }

    private void realShow() {
        try {
            if (context != null && context instanceof Activity && !((Activity) context).isFinishing()) {
                super.show();
            }
        } catch (Exception e) {
        }

    }

    @Override
    public boolean isShowing() {
        return hasSendMessage || super.isShowing();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        hasSendMessage = false;
        handler.removeCallbacksAndMessages(null);
    }

    private static class DelayHandler extends Handler {
        WeakReference<QProgressDialog> dialog;

        DelayHandler(QProgressDialog dialog) {
            this.dialog = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message message) {
            if (dialog.get() != null) {
                dialog.get().realShow();
            }
        }
    }

}
