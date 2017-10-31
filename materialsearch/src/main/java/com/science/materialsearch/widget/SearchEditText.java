package com.science.materialsearch.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.science.materialsearch.MaterialSearchView;

/**
 * @author 幸运Science
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2016/9/21
 */

public class SearchEditText extends AppCompatEditText {

    private MaterialSearchView mSearchView;

    public SearchEditText(Context context) {
        super(context);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSearchView(MaterialSearchView searchView) {
        mSearchView = searchView;
    }

    // 监听软键盘的退出
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mSearchView != null && mSearchView.isSearchOpen()) {
                mSearchView.close();
                return true;
            }
        }
        // return super.onKeyPreIme(keyCode, event);
        return super.dispatchKeyEvent(event);
    }

}
