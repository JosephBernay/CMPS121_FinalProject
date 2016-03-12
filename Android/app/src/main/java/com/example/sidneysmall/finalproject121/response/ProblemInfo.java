package com.example.sidneysmall.finalproject121.response;

/**
 * Created by SidneySmall on 3/7/2016.
 */
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProblemInfo {

    @SerializedName("currentStatus")
    @Expose
    public String currentStatus;
    @SerializedName("computerName")
    @Expose
    public String computerName;

}