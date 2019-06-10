package com.dinghmcn.android.wificonnectclient;

import java.io.Serializable;

public class XNode implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String value;
    private String permission;
    private String operation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
    public String getOperation() {
        return operation;
    }

    public void setOperation(String value) {
        this.operation = operation;
    }

}
