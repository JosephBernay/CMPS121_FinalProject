package com.example.sidneysmall.finalproject121.response;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Joseph on 3/11/2016.
 */
public class ScheduleResponse {
    @SerializedName("scheduleInfo")
    @Expose
    public List<ScheduleInfo> scheduleInfo = new ArrayList<ScheduleInfo>();
    @SerializedName("response")
    @Expose
    public String response;
}
