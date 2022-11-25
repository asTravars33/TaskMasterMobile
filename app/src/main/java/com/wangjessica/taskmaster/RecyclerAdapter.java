package com.wangjessica.taskmaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{

    private List<ToDo> todoList;
    private ClickListener<ToDo> clickListener;

    public RecyclerAdapter(List<ToDo> todoList){
        this.todoList = todoList;
    }

    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todos_cardview,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.MyViewHolder holder, final int position) {
        final ToDo cur = todoList.get(position);
        holder.cardView.setBackgroundColor(cur.getColor());
        holder.title.setText(cur.getTitle());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(cur);
            }
        });
    }
    @Override
    public int getItemCount() {
        return todoList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public void setOnItemClickListener(ClickListener<ToDo> listener){
        this.clickListener = listener;
    }
}