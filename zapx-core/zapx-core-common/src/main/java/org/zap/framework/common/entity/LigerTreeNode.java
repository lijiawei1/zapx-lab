package org.zap.framework.common.entity;

import java.io.Serializable;

/**
 * 树形节点
 *
 * idFieldName
 * textFieldName
 * parentIDFieldName
 *
 * Created by Shin on 2016/4/27.
 */
public class LigerTreeNode implements Serializable{

    String id;

    String pid;

    String text;

    String value;

    String ischecked;

    public LigerTreeNode() {}

    public LigerTreeNode(String id, String pid, String text, String value, String ischecked) {
        this.id = id;
        this.pid = pid;
        this.text = text;
        this.value = value;
        this.ischecked = ischecked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIschecked() {
        return ischecked;
    }

    public void setIschecked(String ischecked) {
        this.ischecked = ischecked;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
