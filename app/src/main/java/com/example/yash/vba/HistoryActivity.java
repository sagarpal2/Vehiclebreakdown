package com.example.yash.vba;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.yash.vba.HistoryRecyclerView.HistoryAdapter;
import com.example.yash.vba.HistoryRecyclerView.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class HistoryActivity extends AppCompatActivity {
    private String TravellerorMechanic, userId;

    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mHistoryRecyclerView=(RecyclerView)findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(),HistoryActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        TravellerorMechanic = getIntent().getExtras().getString("TravellerOrMechanic");
        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistory();

    }

    private void getUserHistory() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(TravellerorMechanic).child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history : dataSnapshot.getChildren()){
                        FetchWorkInfo(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    String tname,mname,mgarage;
    String rating;
    private void FetchWorkInfo(String workkey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(workkey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String workid = dataSnapshot.getKey();
                    Long timestamp = 0L;
                    for(DataSnapshot child: dataSnapshot.getChildren()){
                        if(child.getKey().equals("timestamp")){
                            timestamp=Long.valueOf(child.getValue().toString());
                        }
                        else if(child.getKey().equals("Tname")){
                            tname=String.valueOf(child.getValue().toString());
                        }
                        else if(child.getKey().equals("Mname")){
                            mname=String.valueOf(child.getValue().toString());
                        }
                        else if(child.getKey().equals("Mgarage")){
                            mgarage=String.valueOf(child.getValue().toString());
                        }
                        else if(child.getKey().equals("rating")){
                            rating=String.valueOf(child.getValue().toString());
                        }
                    }
                    HistoryObject obj = new HistoryObject(workid,tname,mname,getdate(timestamp),rating,mgarage);
                    resultsHistory.add(obj);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String getdate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();
        return date;
    }

    private ArrayList resultsHistory=new ArrayList<HistoryObject>();
    private ArrayList<HistoryObject> getDataSetHistory() {
        return resultsHistory;
    }
}
