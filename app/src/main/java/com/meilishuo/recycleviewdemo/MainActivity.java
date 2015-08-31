package com.meilishuo.recycleviewdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.meilishuo.recycleviewdemo.view.MyRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private MyRecyclerView recyclerView;
    private List<String> mDatas = new ArrayList<String>();
    private RadioGroup radioGroup;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private Random random;
    private int currentState = 0;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(this);
        recyclerView = (MyRecyclerView) findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL); //设置滚动方向
        recyclerView.setLayoutManager(linearLayoutManager); //设置LayoutManger
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setCustomeClickListener(new CustomeClickListener() {
            @Override
            public void onItemClick(View item, int position) {
                Toast.makeText(MainActivity.this, "Click position:" + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View item, int position) {
                Toast.makeText(MainActivity.this, "Long Click position:" + position, Toast.LENGTH_SHORT).show();
            }
        });

//        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST));//添加下划线
        gridLayoutManager = new GridLayoutManager(this, 4);
        random = new Random();
        View headerView = LayoutInflater.from(this).inflate(R.layout.header_view, null);

        recyclerView.addHeader(headerView);
//        TextView footer = new TextView(this);
//        footer.setText("footer");
//        header.setTextSize(50);
//        recyclerView.addFooter(footer);

        recyclerView.setLoadDataListener(new MyRecyclerView.LoadDataListener() {
            @Override
            public void onLoadMore() {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addMoreData();
                        if (currentState != 0) {
                            recyclerView.requestLayout();
                        }
                        recyclerView.loadMoreComplate();//完成加载更多
                    }
                }, 2000);
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        changeLayoutManager(checkedId);
    }

    private void changeLayoutManager(int checkedId) {
        switch (checkedId) {
            case R.id.rb1:
                currentState = 0;
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(linearLayoutManager);
                break;
            case R.id.rb2:
                currentState = 1;
//                gridLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
//              暂时不支持gridLayoutManager为水平方向是的header高度为全屏，因此需要是用StaggeredGridLayoutManager达到gridLayoutManager的效果
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.HORIZONTAL));
                break;
            case R.id.rb3:
                currentState = 2;
                StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);//
                recyclerView.setLayoutManager(staggeredGridLayoutManager);
                break;
        }
    }

    interface CustomeClickListener {
        void onItemClick(View item, int position);

        void onItemLongClick(View item, int position);
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private CustomeClickListener listener;

        public void setCustomeClickListener(CustomeClickListener listener) {
            this.listener = listener;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //创建ViewHolder
//            View view = View.inflate(MainActivity.this, R.layout.item_recycler, null);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_recycler, parent, false);//false表示自己来根据自己的属性创建布局
            MyViewHolder holder = new MyViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            //给viewHolder中控件设置数据
            holder.tv.setText(mDatas.get(position));
            if (currentState == 2) {
                holder.tv.setBackgroundColor(Color.BLUE);
                holder.tv.setHeight(random.nextInt(400) + 100);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(v, position);
                    }
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onItemLongClick(v, position);
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        //创建自己的ViewHolder继承RecyclerView.ViewHolder
        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tv;

            public MyViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.item_tv);
            }
        }
    }

    private void initData() {
        if (!mDatas.isEmpty()) {
            mDatas.clear();
        }
        for (int i = 'A'; i < 'z'; i++) {
            mDatas.add("" + (char) i);

        }
    }

    private void addMoreData() {
        if (mDatas != null) {
            for (int i = 0; i < 20; i++) {
                mDatas.add(i + "");
            }
        }
    }

}
