package com.shaoxia.elevator;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private RecyclerView mRecycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRecycleView();

    }

    private void initRecycleView() {
        mRecycleView = findViewById(R.id.out_call_list);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));

        List<String> data = new ArrayList<>();
        for(int i=0;i <10; i++) {
            data.add("text " + i);
        }
        MyRecycleAdapter adapter = new MyRecycleAdapter(data);
        mRecycleView.setAdapter(adapter);
    }
}
