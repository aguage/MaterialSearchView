package com.science.materialsearchview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.science.materialsearch.MaterialSearchView;
import com.science.materialsearch.adapter.SearchAdapter;

public class MainActivity extends AppCompatActivity {

    private MaterialSearchView materialSearchView;
    private String strQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        materialSearchView = (MaterialSearchView) findViewById(R.id.searchView);
        // 设置软键盘搜索键监听
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            // 点击软件盘搜索键
            @Override
            public boolean onQueryTextSubmit(String query) {
                strQuery = query;
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("query", query);
                startActivityForResult(intent, 1);
                return true;
            }
        });
        // 设置搜索历史列表点击监听
        materialSearchView.setAdapter(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String queryHistory) {
                strQuery = queryHistory;
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("query", queryHistory);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 在搜索结果界面清除历史记录后，需要调用onTextChanged，以更新历史界面
        materialSearchView.onTextChanged(strQuery);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            // 开始打开搜索框
            materialSearchView.open();
        }
        return super.onOptionsItemSelected(item);
    }
}
