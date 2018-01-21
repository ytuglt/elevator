package com.shaoxia.elevator.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.shaoxia.elevator.R;

/**
 * Created by gonglt1 on 2018/1/21.
 */

public class DrawableTextView  extends TextView{
    public DrawableTextView(Context context) {
        super(context);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);
        int width = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_drawable_width, -1);
        int height = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_drawable_height, -1);

        SizeWrap sizeWrap = new SizeWrap();
        Drawable leftDrawable = ta.getDrawable(R.styleable.DrawableTextView_left_drawable);
        if (leftDrawable != null) {
            int lwidth = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_leftdrawable_width, -1);
            int lheight = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_lefttdrawable_height, -1);
            if (sizeWrap.checkWidthAndHeight(width, height, lwidth, lheight)) {
                leftDrawable.setBounds(0, 0, sizeWrap.width, sizeWrap.height);
            } else {
                throw new IllegalArgumentException("error left drawable size setting");
            }
        }

        Drawable rightDrawable = ta.getDrawable(R.styleable.DrawableTextView_right_drawable);
        if (rightDrawable != null) {
            int rwidth = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_rightdrawable_width, -1);
            int rheight = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_rightdrawable_height, -1);
            if (sizeWrap.checkWidthAndHeight(width,height, rwidth, rheight)) {
                rightDrawable.setBounds(0, 0, sizeWrap.width, sizeWrap.height);
            } else {
                throw new IllegalArgumentException("error right drawable size setting");
            }
        }

        Drawable topDrawable = ta.getDrawable(R.styleable.DrawableTextView_top_drawable);
        if (topDrawable != null) {
            int twidth = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_topdrawable_width, -1);
            int theight = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_topdrawable_height, -1);
            if (sizeWrap.checkWidthAndHeight(width,height, twidth, theight)) {
                topDrawable.setBounds(0, 0, sizeWrap.width, sizeWrap.height);
            } else {
                throw new IllegalArgumentException("error top drawable size setting");
            }
        }

        Drawable bottomDrawable = ta.getDrawable(R.styleable.DrawableTextView_bottom_drawable);
        if (bottomDrawable != null) {
            int bwidth = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_bottomdrawable_width, -1);
            int bheight = ta.getDimensionPixelOffset(R.styleable.DrawableTextView_bottomdrawable_height, -1);
            if (sizeWrap.checkWidthAndHeight(width, height, bwidth, bheight)) {
                bottomDrawable.setBounds(0, 0, sizeWrap.width, sizeWrap.height);
            } else {
                throw new IllegalArgumentException("error bottom drawable size setting");
            }
        }

        this.setCompoundDrawables(leftDrawable, topDrawable, rightDrawable, bottomDrawable);
        ta.recycle();
        ta = null;
    }

    /**
     *
     */
    public static class SizeWrap {
        int width;
        int height;

        public boolean checkWidthAndHeight(int globalWidth, int globalHeight, int localWidth, int localHeight) {
            width = 0;
            height = 0;

            //局部的大小设置均正常的情况
            if (localWidth > 0 && localHeight > 0) {
                width = localWidth;
                height = localHeight;
                return true;
            }

            //局部大小没设置时，看全局的大小是否正确设置
            if (localWidth == -1 && localHeight == -1) {
                if (globalWidth > 0 && globalHeight > 0) {
                    width = globalWidth;
                    height = globalHeight;
                    return true;
                }
            }
            return false;
        }
    }
}
