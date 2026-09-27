package com.chronos.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateJobStatusRequest{
    @NotBlank
    private String status;
    public UpdateJobStatusRequest(){

    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status=status;
    }
}