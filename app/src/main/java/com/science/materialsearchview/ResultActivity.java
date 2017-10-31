package com.science.materialsearchview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.science.materialsearch.MaterialSearchView;
import com.science.materialsearch.adapter.SearchAdapter;

/**
 * @author 幸运Science
 * @description
 * @email chentushen.science@gmail.com,274240671@qq.com
 * @data 2016/9/23
 */

public class ResultActivity extends AppCompatActivity {

    private MaterialSearchView materialSearchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        materialSearchView = (MaterialSearchView) findViewById(R.id.searchView);
        // 设置搜索样式（默认不显示）：浮于Toolbar上
        materialSearchView.setVersion(MaterialSearchView.VERSION_TOOLBAR);
        // 设置搜索输入框文字
        materialSearchView.setTextInput(getIntent().getStringExtra("query"));
        // 设置软键盘搜索键监听
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(ResultActivity.this, "搜索关键字:" + query, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        // 设置搜索历史列表点击监听
        materialSearchView.setAdapter(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String queryHistory) {
                Toast.makeText(ResultActivity.this, "搜索关键字:" + queryHistory, Toast.LENGTH_SHORT).show();
            }
        });
        // 设置搜索框左边返回箭头监听
        materialSearchView.setOnMenuClickListener(new MaterialSearchView.OnMenuClickListener() {
            @Override
            public void onMenuClick() {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }
}
