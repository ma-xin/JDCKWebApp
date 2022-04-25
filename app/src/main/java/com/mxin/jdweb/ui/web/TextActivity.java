package com.mxin.jdweb.ui.web;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.mxin.jdweb.R;
import com.mxin.jdweb.utils.ConvertUtils;
import com.mxin.jdweb.utils.RecycleViewDivider;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
        String content = getIntent().getStringExtra("content");
        List<String> list = Arrays.asList(content.split(";"));
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.VERTICAL, ConvertUtils.dp2px(1), ContextCompat.getColor(this, R.color.divide_color)));
        recyclerView.setAdapter(new TextAdapter(list));
    }

    static class TextAdapter extends BaseQuickAdapter<String, BaseViewHolder>{


        public TextAdapter(List<String> data) {
            super(R.layout.item_text, data);
        }

        @Override
        protected void convert(@NotNull BaseViewHolder baseViewHolder, String item) {
            String[] value = item.split("=");
            baseViewHolder.setText(R.id.tv_name, value[0]+": " );
            baseViewHolder.setText(R.id.tv_value,value[0]+": " + (value.length>1?value[1]:"") );
        }
    }

}



