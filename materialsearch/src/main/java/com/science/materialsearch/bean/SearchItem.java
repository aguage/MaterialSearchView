package com.science.materialsearch.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @author 幸运Science
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2016/9/23
 */

public class SearchItem implements Parcelable {

    private CharSequence text;

    public CharSequence getText() {
        return text;
    }

    public void setText(CharSequence text) {
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.text, dest, flags);
    }

    public SearchItem() {
    }


    public SearchItem(CharSequence text) {
        this.text = text;
    }

    protected SearchItem(Parcel in) {
        this.text = in.readParcelable(CharSequence.class.getClassLoader());
    }

    public static final Parcelable.Creator<SearchItem> CREATOR = new Parcelable.Creator<SearchItem>() {
        @Override
        public SearchItem createFromParcel(Parcel source) {
            return new SearchItem(source);
        }

        @Override
        public SearchItem[] newArray(int size) {
            return new SearchItem[size];
        }
    };
}
