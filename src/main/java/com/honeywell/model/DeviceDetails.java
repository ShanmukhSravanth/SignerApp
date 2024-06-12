package com.honeywell.model;
public class DeviceDetails {
    private String name;
    private String serialNo;
    private int noOfPorts;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public int getNoOfPorts() {
        return noOfPorts;
    }

    public void setNoOfPorts(int noOfPorts) {
        this.noOfPorts = noOfPorts;
    }
}