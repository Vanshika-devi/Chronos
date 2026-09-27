package com.chronos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateJobRequest{
    @NotBlank
    private String name;

    @Min(1)
    private int priority;

    public CreateJobRequest(){

    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public int getPriority(){
        return priority;
    }
    public void setPriority(int priority){
        this.priority=priority;
    }
}