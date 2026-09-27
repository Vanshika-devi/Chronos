package com.chronos.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="executions")
public class Execution{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long jobId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public Execution(){

    }
    public Execution(
            Long id,
            Long jobId,
            String status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ){
        this.id=id;
        this.jobId=jobId;
        this.status=status;
        this.startedAt=startedAt;
        this.finishedAt=finishedAt;
    }
    public Long getId(){
        return id;
    }
    public Long getJobId(){
        return jobId;
    }
    public String getStatus(){
        return status;
    }
    public LocalDateTime getStartedAt(){
        return startedAt;
    }
    public LocalDateTime getFinishedAt(){
        return finishedAt;
    }
    public void setId(Long id){
        this.id=id;
    }
    public void setJobId(Long jobId){
        this.jobId=jobId;
    }
    public void setStatus(String status){
        this.status=status;
    }
    public void setStartedAt(LocalDateTime startedAt){
        this.startedAt=startedAt;
    }
    public void setFinishedAt(LocalDateTime finishedAt){
        this.finishedAt=finishedAt;
    }
}