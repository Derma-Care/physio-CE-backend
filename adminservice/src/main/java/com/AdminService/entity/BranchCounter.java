package com.AdminService.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "BranchCounter")
public class BranchCounter {

    @Id
    private String id; 

    private Integer seq;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }
}
