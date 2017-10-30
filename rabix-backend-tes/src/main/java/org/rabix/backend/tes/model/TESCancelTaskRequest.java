package org.rabix.backend.tes.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TESCancelTaskRequest {
    @JsonProperty("id")
    private String id;


    @JsonCreator
    public TESCancelTaskRequest(@JsonProperty("id") String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TESCancelTaskRequest [id=" + id + "]";
    }
}
