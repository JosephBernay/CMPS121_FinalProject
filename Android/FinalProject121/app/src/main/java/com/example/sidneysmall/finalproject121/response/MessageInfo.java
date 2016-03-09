package com.example.sidneysmall.finalproject121.response;

/**
 * Created by SidneySmall on 3/7/2016.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessageInfo {

    @SerializedName("timeCreated")
    @Expose
    public String timeCreated;
    @SerializedName("problem")
    @Expose
    public String problem;
    @SerializedName("computerName")
    @Expose
    public String computerName;
    @SerializedName("computerNumber")
    @Expose
    public String computerNumber;
    @SerializedName("messageData")
    @Expose
    public String messageData;

}