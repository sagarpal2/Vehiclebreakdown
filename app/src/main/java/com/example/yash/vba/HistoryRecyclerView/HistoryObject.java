package com.example.yash.vba.HistoryRecyclerView;

public class HistoryObject {
    private String workId;
    private String time;
    private String tname;
    private String mname;
    private String garage;
    private String rating;

    public HistoryObject(String workId,String tname,String mname,String time,String rating,String garage){
        this.workId=workId;
        this.time=time;
        this.tname=tname;
        this.mname=mname;
        this.rating=rating;
        this.garage=garage;
    }

    public String getworkid(){return workId;}
    public String gettime(){return time;}
    public String gettname(){return tname;}
    public String getmname(){return mname;}
    public String getgarage(){return garage;}
    public String getrating(){return rating;}
}
