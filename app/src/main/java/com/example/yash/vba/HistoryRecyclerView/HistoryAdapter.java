package com.example.yash.vba.HistoryRecyclerView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yash.vba.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> {
    private List<HistoryObject> itemList;
    private Context context;


    public HistoryAdapter(List<HistoryObject> itemList,Context context){
        this.itemList=itemList;
        this.context=context;
    }


    @NonNull
    @Override
    public HistoryViewHolders onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,null,false);
        RecyclerView.LayoutParams lp=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        HistoryViewHolders rov=new HistoryViewHolders(layoutView);
        return rov;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolders holder, int position) {
        holder.workId.setText(itemList.get(position).getworkid());
        holder.time.setText(itemList.get(position).gettime());
        holder.tname.setText(itemList.get(position).gettname());
        holder.mname.setText(itemList.get(position).getmname());
        holder.garage.setText(itemList.get(position).getgarage());
        holder.rating.setText(itemList.get(position).getrating());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
