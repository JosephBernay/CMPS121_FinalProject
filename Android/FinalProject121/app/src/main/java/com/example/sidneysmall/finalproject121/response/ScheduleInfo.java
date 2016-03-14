package com.example.sidneysmall.finalproject121.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joseph on 3/11/2016.
 */
public class ScheduleInfo {
    @SerializedName("computerName")
    @Expose
    public String computerName;
    @SerializedName("endTime")
    @Expose
    public String endTime;
    @SerializedName("dateReserved")
    @Expose
    public String dateReserved;
    @SerializedName("beginTime")
    @Expose
    public String beginTime;
    @SerializedName("email")
    @Expose
    public String email;
}
