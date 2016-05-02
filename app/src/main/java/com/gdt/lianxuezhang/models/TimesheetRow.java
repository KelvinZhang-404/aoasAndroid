package com.gdt.lianxuezhang.models;

/**
 * Created by LianxueZhang on 1/12/2015.
 */
public class TimesheetRow {

    private String comment;
    private String overtime;
    private String timeStamp;
    private String timeStampType;

    public TimesheetRow(String comment, String overtime, String timeStamp, String timeStampType) {
        this.comment = comment;
        this.overtime = overtime;
        this.timeStamp = timeStamp;
        this.timeStampType = timeStampType;
    }

    public String getComment(){
        return comment;
    }

    public String getOvertime(){
        return overtime;
    }

    public String getTimeStamp(){
        return timeStamp;
    }

    public String getTimeStampType(){
        return timeStampType;
    }
}
