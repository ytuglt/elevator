package com.shaoxia.elevator.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by gonglt1 on 2018/3/21.
 */

public class ViewUtils {
    public static void setImageViewtint(final ImageView imageView, final int color) {
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        imageView.setColorFilter(color);
                        break;
                    case MotionEvent.ACTION_UP:
                        imageView.setColorFilter(null);
                        break;
                }
                //这里一定要return false，不然该方法会拦截事件，造成不能响应点击等操作
                return false;
            }
        });
    }

    public static void setImageViewTint(final ImageView imageView) {
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        imageView.setColorFilter(Color.parseColor("#89DBFA"));
                        break;
                    case MotionEvent.ACTION_UP:
                        imageView.setColorFilter(null);
                        break;
                }
                //这里一定要return false，不然该方法会拦截事件，造成不能响应点击等操作
                return false;
            }
        });
    }

    public static void setViewtint(final View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        view.setAlpha(0.6f);
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setAlpha(1.0f);
                        break;
                }
                //这里一定要return false，不然该方法会拦截事件，造成不能响应点击等操作
                return false;
            }
        });
    }
}
