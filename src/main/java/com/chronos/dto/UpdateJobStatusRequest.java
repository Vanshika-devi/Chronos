package com.chronos.dto;

import com.chronos.model.JobStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateJobStatusRequest{
    @NotNull
    private JobStatus status;
    public UpdateJobStatusRequest(){

    }
    public JobStatus getStatus(){
        return status;
    }
    public void setStatus(JobStatus status){
        this.status=status;
    }
}