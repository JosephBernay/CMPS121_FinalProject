package com.example.sidneysmall.finalproject121.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ComputerResponse {
    @SerializedName("messageInfo")
    @Expose
    public List<MessageInfo> messageInfo = new ArrayList<MessageInfo>();
    @SerializedName("response")
    @Expose
    public String response;
}
