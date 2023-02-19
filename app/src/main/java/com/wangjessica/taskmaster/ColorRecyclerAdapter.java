package com.wangjessica.taskmaster;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ColorRecyclerAdapter extends RecyclerView.Adapter<ColorRecyclerAdapter.MyViewHolder>{

    private List<ColorSquare> colorList;
    private ClickListener<ColorSquare> clickListener;

    public ColorRecyclerAdapter(List<ColorSquare> colorList){
        this.colorList = colorList;
    }

    @Override
    public ColorRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.colors_cardview,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ColorRecyclerAdapter.MyViewHolder holder, final int position) {
        final ColorSquare cur = colorList.get(position);
        System.out.println(cur.getColor());
        holder.cardView.setBackgroundColor(Color.parseColor(cur.getColor()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(cur);
            }
        });
    }
    @Override
    public int getItemCount() {
        return colorList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public void setOnItemClickListener(ClickListener<ColorSquare> listener){
        this.clickListener = listener;
    }
}