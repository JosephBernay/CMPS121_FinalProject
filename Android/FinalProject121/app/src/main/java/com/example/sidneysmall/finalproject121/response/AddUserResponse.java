package com.example.sidneysmall.finalproject121.response;

/**
 * Created by ampar_000 on 3/12/2016.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddUserResponse {
    @SerializedName("response")
    @Expose
    private String response;

    /**
     *
     * @return
     * The response
     */
    public String getResponse() {
        return response;
    }

    /**
     *
     * @param response
     * The response
     */
    public void setResponse(String response) {
        this.response = response;
    }
}
