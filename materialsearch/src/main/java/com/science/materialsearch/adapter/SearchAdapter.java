package com.science.materialsearch.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.science.materialsearch.R;
import com.science.materialsearch.bean.SearchItem;
import com.science.materialsearch.db.SearchHistoryTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author 幸运Science
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2016/9/22
 */

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int TYPE_NORMAL_ITEM = 0;  //普通Item
    private static final int TYPE_FOOTER_ITEM = 1;  //底部FooterView

    protected final SearchHistoryTable mHistoryDatabase;
    protected String key = "";
    protected List<SearchItem> mResultList = new ArrayList<>();
    protected OnItemClickListener mItemClickListeners;
    private Context mContext;

    public SearchAdapter(Context context) {
        mContext = context;
        mHistoryDatabase = new SearchHistoryTable(context);
        getFilter().filter("");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_FOOTER_ITEM) {
            View view = inflater.inflate(R.layout.search_footer_view, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.search_item, parent, false);
            return new ResultViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ResultViewHolder) {
            SearchItem item = mResultList.get(position);

            String itemText = item.getText().toString();
            String itemTextLower = itemText.toLowerCase(Locale.getDefault());

            if (itemTextLower.contains(key) && !key.isEmpty()) {
                SpannableString s = new SpannableString(itemText);
                s.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.search_light_text_highlight)),
                        itemTextLower.indexOf(key), itemTextLower.indexOf(key) + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ((ResultViewHolder) viewHolder).text.setText(s, TextView.BufferType.SPANNABLE);
            } else {
                ((ResultViewHolder) viewHolder).text.setText(item.getText());
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // 如果position+1等于整个布局所有数总和就是底部布局
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER_ITEM;
        } else {
            return TYPE_NORMAL_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        if (mResultList.size() == 0) {
            return 0;
        }
        return mResultList.size() + 1;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (!TextUtils.isEmpty(constraint)) {
                    key = constraint.toString().toLowerCase(Locale.getDefault());

                    List<SearchItem> results = new ArrayList<>();
                    List<SearchItem> history = new ArrayList<>();
                    List<SearchItem> databaseAllItems = mHistoryDatabase.getAllItems();
                    if (!databaseAllItems.isEmpty()) {
                        history.addAll(databaseAllItems);
                    }

                    for (SearchItem str : history) {
                        String string = str.getText().toString().toLowerCase(Locale.getDefault());
                        if (string.contains(key)) {
                            results.add(str);
                        }
                    }

                    if (results.size() > 0) {
                        filterResults.values = results;
                        filterResults.count = results.size();
                    }
                } else {
                    key = "";
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                List<SearchItem> dataSet = new ArrayList<>();

                if (results.values != null) {
                    List<?> result = (ArrayList<?>) results.values;
                    for (Object object : result) {
                        if (object instanceof SearchItem) {
                            dataSet.add((SearchItem) object);
                        }
                    }
                } else {
                    if (key.isEmpty()) {
                        List<SearchItem> allItems = mHistoryDatabase.getAllItems();
                        if (!allItems.isEmpty()) {
                            dataSet = allItems;
                        }
                    }
                }
                setData(dataSet);
            }
        };
    }

    public void setData(List<SearchItem> data) {
        if (mResultList == null) {
            mResultList = data;
            notifyDataSetChanged();
        } else {
            int previousSize = mResultList.size();
            int nextSize = data.size();
            mResultList = data;
            if (previousSize == nextSize && nextSize != 0)
                notifyItemRangeChanged(0, previousSize);
            else if (previousSize > nextSize) {
                if (nextSize == 0)
                    notifyItemRangeRemoved(0, previousSize);
                else {
                    notifyItemRangeChanged(0, nextSize);
                    notifyItemRangeRemoved(nextSize - 1, previousSize);
                }
            } else {
                notifyItemRangeChanged(0, previousSize);
                notifyItemRangeInserted(previousSize, nextSize - previousSize);
            }
        }
    }

    public class ResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView text;

        public ResultViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text_history);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListeners != null) {
                mItemClickListeners.onItemClick(v, getLayoutPosition(), text.getText().toString());
            }
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public FooterViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mHistoryDatabase.clearDatabase(); // 清除数据库历史记录
            getFilter().filter(""); // 清除界面历史记录
        }
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListeners = itemClickListener;
    }

    @SuppressWarnings("UnusedParameters")
    public interface OnItemClickListener {
        void onItemClick(View view, int position, String queryHistory);
    }

}
