package  com.chronos.dto;

import com.chronos.model.JobStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateJobRequest{

    @NotBlank
    private String name;

    @Min(1)
    private int priority;

    @NotNull
    private JobStatus status;
    public UpdateJobRequest(){

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
    public JobStatus getStatus(){
        return status;
    }
    public void setStatus(JobStatus status){
        this.status=status;
    }
}