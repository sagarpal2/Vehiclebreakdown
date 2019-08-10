package com.example.yash.vba.HistoryRecyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.yash.vba.R;

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView workId,time,mname,tname,garage,rating;
    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        workId=(TextView) itemView.findViewById(R.id.workId);
        time=(TextView) itemView.findViewById(R.id.time);
        mname=(TextView) itemView.findViewById(R.id.MnameId);
        tname=(TextView) itemView.findViewById(R.id.TnameId);
        garage=(TextView) itemView.findViewById(R.id.garageId);
        rating=(TextView) itemView.findViewById(R.id.ratingId);
    }

    @Override
    public void onClick(View v) {

    }
}
