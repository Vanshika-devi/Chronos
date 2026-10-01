package com.chronos.model;

import jakarta.persistence.*;

@Entity
@Table(name="jobs")
public class Job{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int priority;

    @Enumerated(EnumType.STRING)
    private JobStatus status;
    public Job(){

    }
    public Job(Long id,String name,int priority,JobStatus status){
        this.id=id;
        this.name=name;
        this.priority=priority;
        this.status=status;
    }
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id=id;
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