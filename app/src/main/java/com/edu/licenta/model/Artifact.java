package com.edu.licenta.model;

/**
 * Created by naritc
 * on 19-Apr-18.
 */

public class Artifact {

    private String name;
    private String tagId;

    public Artifact(String name, String tagId) {
        this.name = name;
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }
}
