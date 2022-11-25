package com.wangjessica.taskmaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.MyViewHolder>{

    private List<Group> groupList;
    private ClickListener<Group> clickListener;

    public GroupsRecyclerAdapter(List<Group> GroupList){
        this.groupList = GroupList;
    }

    @Override
    public GroupsRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_cardview, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupsRecyclerAdapter.MyViewHolder holder, final int position) {
        final Group cur = groupList.get(position);
        holder.title.setText(cur.getTitle());
        // Get the capacity info
        holder.capacityCnt.setText("");
        holder.userView.setText(cur.getUserName());
        holder.cardView.setBackgroundColor(cur.getColor());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(cur);
            }
        });
    }
    @Override
    public int getItemCount() {
        return groupList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView capacityCnt;
        private TextView userView;
        private CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            capacityCnt = itemView.findViewById(R.id.capacity_cnt);
            userView = itemView.findViewById(R.id.user);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public void setOnItemClickListener(ClickListener<Group> listener){
        this.clickListener = listener;
    }
}