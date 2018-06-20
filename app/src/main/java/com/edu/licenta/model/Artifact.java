package com.edu.licenta.model;

import java.io.Serializable;

/**
 * Created by naritc
 * on 19-Apr-18.
 */

public class Artifact implements Serializable {

    private Long id;
    private String name;
    private String textBasic;
    private String textAdvanced;
    private String tagId;

    public Artifact(Long id, String name, String textBasic, String textAdvanced, String tagId) {
        this.id = id;
        this.name = name;
        this.textBasic = textBasic;
        this.textAdvanced = textAdvanced;
        this.tagId = tagId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTextBasic() {
        return textBasic;
    }

    public void setTextBasic(String textBasic) {
        this.textBasic = textBasic;
    }

    public String getTextAdvanced() {
        return textAdvanced;
    }

    public void setTextAdvanced(String textAdvanced) {
        this.textAdvanced = textAdvanced;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }
}
