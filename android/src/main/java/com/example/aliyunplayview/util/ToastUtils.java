package com.example.aliyunplayview.util;

import android.content.Context;
import android.widget.Toast;

/**
 * @author 何晏波
 * @QQ 1054539528
 * @date 2019/5/17
 * @function: Toast工具
*/
public class ToastUtils {
    public static void s(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void l(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
