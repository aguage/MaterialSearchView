package com.science.materialsearch;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.science.materialsearch.adapter.SearchAdapter;
import com.science.materialsearch.bean.SearchItem;
import com.science.materialsearch.db.SearchHistoryTable;
import com.science.materialsearch.utils.SearchAnimator;
import com.science.materialsearch.widget.SearchEditText;

/**
 * @author 幸运Science
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2016/9/21
 */

public class MaterialSearchView extends FrameLayout implements View.OnClickListener {

    public static final int LAYOUT_TRANSITION_DURATION = 200;
    public static final int ANIMATION_DURATION = 350;

    public static final int VERSION_TOOLBAR = 1000;
    public static final int VERSION_MENU_ITEM = 1001;

    private Context mContext;
    protected OnQueryTextListener mOnQueryChangeListener = null;
    protected OnOpenCloseListener mOnOpenCloseListener = null;
    protected OnMenuClickListener mOnMenuClickListener = null;
    protected RecyclerView.Adapter mAdapter = null;
    protected RecyclerView mRecyclerView;
    private SearchHistoryTable mHistoryDatabase;
    protected View mShadowView;
    protected View mDividerView;
    protected CardView mCardView;
    protected SearchEditText mEditText;
    protected ImageView mBackImageView;
    protected ImageView mEmptyImageView;
    private CharSequence mUserQuery = "";
    protected CharSequence mOldQueryText;
    private View mMenuItemView = null;
    private int mMenuItemCx = -1;
    protected int mVersion = VERSION_MENU_ITEM;
    private boolean mIsSearchOpen = false;

    public MaterialSearchView(Context context) {
        this(context, null);
    }

    public MaterialSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public MaterialSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.search_view, this, true);

        mHistoryDatabase = new SearchHistoryTable(mContext);
        mCardView = (CardView) findViewById(R.id.cardView);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_result);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutTransition(getRecyclerViewLayoutTransition());
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) { //正在被外部拖拽,一般为用户正在用手指滚动
                    mRecyclerView.setLayoutTransition(null);
                    hideKeyboard();
                } else {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) { //静止,没有滚动
                        mRecyclerView.setLayoutTransition(getRecyclerViewLayoutTransition());
                    }
                }
            }
        });

        // 搜索界面半透明背景
        mShadowView = findViewById(R.id.view_shadow);
        mShadowView.setOnClickListener(this);
        mShadowView.setVisibility(View.GONE);
        // 搜索框和搜索历史分割线
        mDividerView = findViewById(R.id.view_divider);
        mDividerView.setVisibility(View.GONE);
        // 搜索框返回箭头按钮
        mBackImageView = (ImageView) findViewById(R.id.imageView_arrow_back);
        mBackImageView.setOnClickListener(this);
        // 搜索框清空文字按钮
        mEmptyImageView = (ImageView) findViewById(R.id.imageView_clear);
        mEmptyImageView.setOnClickListener(this);
        mEmptyImageView.setVisibility(View.GONE);
        // 搜索框输入文字
        mEditText = (SearchEditText) findViewById(R.id.searchEditText_input);
        mEditText.setSearchView(this);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onSubmitQuery();
                return true;
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MaterialSearchView.this.onTextChanged(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addFocus();
                } else {
                    removeFocus();
                }
            }
        });

        setVersion(VERSION_MENU_ITEM);
    }

    public void setVersion(int version) {
        mVersion = version;

        if (mVersion == VERSION_TOOLBAR) {
            setVisibility(View.VISIBLE);
            mEditText.clearFocus();
        }

        if (mVersion == VERSION_MENU_ITEM) {
            setVisibility(View.GONE);
        }
    }

    public LayoutTransition getRecyclerViewLayoutTransition() {
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(LAYOUT_TRANSITION_DURATION);
        return layoutTransition;
    }

    public void showKeyboard() {
        if (!isInEditMode()) {
            InputMethodManager imm = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mEditText, 0);
            imm.showSoftInput(this, 0);
        }
    }

    public void hideKeyboard() {
        if (!isInEditMode()) {
            InputMethodManager imm = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }

    /**
     * 点击软件盘搜索键
     */
    private void onSubmitQuery() {
        CharSequence query = mEditText.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (mOnQueryChangeListener != null) {
                mOnQueryChangeListener.onQueryTextSubmit(query.toString());
                mHistoryDatabase.addItem(new SearchItem(query.toString()));
            }
        }
    }

    /**
     * 在搜索结果界面清除历史记录时，需要在退出结果界面后的Activity调用此方法，以更新此时界面的历史记录
     *
     * @param newText
     */
    public void onTextChanged(CharSequence newText) {
        CharSequence text = mEditText.getText();
        mUserQuery = text;
        if (mAdapter != null && mAdapter instanceof Filterable) {
            ((Filterable) mAdapter).getFilter().filter(text);
        }
        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText)) {
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        mOldQueryText = newText.toString();

        if (!TextUtils.isEmpty(newText)) {
            showClearTextIcon();
        } else {
            hideClearTextIcon();
        }
    }

    /**
     * 隐藏情况按钮
     */
    private void hideClearTextIcon() {
        if (mUserQuery.length() == 0) {
            mEmptyImageView.setVisibility(View.GONE);
        }
    }

    /**
     * 显示清空按钮
     */
    private void showClearTextIcon() {
        if (mUserQuery.length() > 0) {
            mEmptyImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 输入框获取焦点
     */
    public void addFocus() {
        mIsSearchOpen = true;
        // 显示半透明背景
        SearchAnimator.fadeIn(mShadowView, ANIMATION_DURATION);

        showSuggestions();
        showKeyboard();
        showClearTextIcon();
        if (mVersion != VERSION_MENU_ITEM) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mOnOpenCloseListener != null) {
                        mOnOpenCloseListener.onOpen();
                    }
                }
            }, ANIMATION_DURATION);
        }
    }

    /**
     * 输入框失去焦点
     */
    public void removeFocus() {
        mIsSearchOpen = false;
        // 隐藏半透明背景
        SearchAnimator.fadeOut(mShadowView, ANIMATION_DURATION);

        hideSuggestions();
        hideKeyboard();
        hideClearTextIcon();
        if (mVersion != VERSION_MENU_ITEM) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mOnOpenCloseListener != null) {
                        mOnOpenCloseListener.onClose();
                    }
                }
            }, ANIMATION_DURATION);
        }
    }

    public boolean isSearchOpen() {
        return mIsSearchOpen;
    }

    /**
     * 显示搜索建议
     */
    public void showSuggestions() {
        if (mRecyclerView.getVisibility() == View.GONE) {
            if (mAdapter != null) {
                if (mAdapter.getItemCount() > 0) {
                    mDividerView.setVisibility(View.VISIBLE);
                }
                mRecyclerView.setVisibility(View.VISIBLE);
                SearchAnimator.fadeIn(mRecyclerView, ANIMATION_DURATION);
            }
        }
    }

    /**
     * 隐藏搜索建议
     */
    public void hideSuggestions() {
        if (mRecyclerView.getVisibility() == View.VISIBLE) {
            mDividerView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            SearchAnimator.fadeOut(mRecyclerView, ANIMATION_DURATION);
        }
    }

    public void setTextInput(CharSequence text) {
        mEditText.setText(text);
    }

    public void setTextInput(@StringRes int text) {
        mEditText.setText(text);
    }

    public RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    public void setAdapter(SearchAdapter.OnItemClickListener onItemClickListener) {
        SearchAdapter searchAdapter = new SearchAdapter(mContext);
        mAdapter = searchAdapter;
        mRecyclerView.setAdapter(mAdapter);
        searchAdapter.setOnItemClickListener(onItemClickListener);
    }

    public void open() {
        open(null);
    }

    public void open(MenuItem menuItem) {
        if (mVersion == VERSION_MENU_ITEM) {
            setVisibility(View.VISIBLE);

            if (menuItem != null)
                getMenuItemPosition(menuItem.getItemId());
            reveal();
        }
        if (mVersion == VERSION_TOOLBAR) {
            if (mEditText.length() > 0) {
                mEditText.getText().clear();
            }
            mEditText.requestFocus();
        }
    }
    //修改兼容21以下版本
    public void close() {
        if (mVersion == VERSION_MENU_ITEM) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SearchAnimator.revealClose(mCardView, mMenuItemCx, ANIMATION_DURATION,
                        mContext, mEditText, this, mOnOpenCloseListener);

            } else {
                //   SearchAnimator.revealOpen(mCardView, ANIMATION_DURATION, mEditText,mOnOpenCloseListener);
                SearchAnimator.fadeClose(mCardView,ANIMATION_DURATION,mEditText,false,this,mOnOpenCloseListener);
            }



        }
        if (mVersion == VERSION_TOOLBAR) {
            mEditText.clearFocus();
        }
    }

    private int getCenterX(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0] + view.getWidth() / 2;
    }

    private void getMenuItemPosition(int menuItemId) {
        if (mMenuItemView != null) {
            mMenuItemCx = getCenterX(mMenuItemView);
        }
        ViewParent viewParent = getParent();
        while (viewParent != null && viewParent instanceof View) {
            View parent = (View) viewParent;
            View view = parent.findViewById(menuItemId);
            if (view != null) {
                mMenuItemView = view;
                mMenuItemCx = getCenterX(mMenuItemView);
                break;
            }
            viewParent = viewParent.getParent();
        }
    }

    private void reveal() {
        mCardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    SearchAnimator.revealOpen(mCardView, mMenuItemCx, ANIMATION_DURATION,
                            mContext, mEditText, mOnOpenCloseListener);

                } else {
                 //   SearchAnimator.revealOpen(mCardView, ANIMATION_DURATION, mEditText,mOnOpenCloseListener);
                    SearchAnimator.fadeOpen(mCardView,ANIMATION_DURATION,mEditText,false,mOnOpenCloseListener);
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == mBackImageView) {
            if (mOnMenuClickListener != null) {
                mOnMenuClickListener.onMenuClick();
            } else {
                close();
            }
        } else if (v == mEmptyImageView) {
            if (mEditText.length() > 0) {
                mEditText.getText().clear();
                mEditText.requestFocus();
            }
        } else if (v == mShadowView) {
            close();
        }
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    public void setOnOpenCloseListener(OnOpenCloseListener listener) {
        mOnOpenCloseListener = listener;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        mOnMenuClickListener = listener;
    }

    public interface OnQueryTextListener {
        boolean onQueryTextChange(String newText);

        boolean onQueryTextSubmit(String query);
    }

    public interface OnOpenCloseListener {
        void onClose();

        void onOpen();
    }

    public interface OnMenuClickListener {
        void onMenuClick();
    }
}
