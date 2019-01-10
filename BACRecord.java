package com.deepwares.checkpointdwi.entities;

import java.io.Serializable;

/**
 * Created by deesunda on 10/4/2014.
 */

public class BACRecord implements Serializable {

    String indexID, date, time, image, videoFile,status, deviceName;
    double bac, latitudeValue, longitudeValue;
    boolean faceMatchFailBAC;

    public BACRecord(String date,String time,double bac){
        this.date=date;
        this.time=time;
        this.bac = bac;
    }

    public BACRecord(String date,String time,double bac,String status){
        this.date=date;
        this.time=time;
        this.bac = bac;
        this.status = status;
    }

    public BACRecord(String date,String time,double bac, double latitudeValue, double longitudeValue,
                     String videoFile, String status, boolean faceMatchFailBAC, String deviceName){
        this.date=date;
        this.time=time;
        this.bac = bac;
        this.latitudeValue = latitudeValue;
        this.longitudeValue = longitudeValue;
        this.videoFile = videoFile;
        this.status = status;
        this.faceMatchFailBAC = faceMatchFailBAC;
        this.deviceName = deviceName;
    }

    public BACRecord(String indexID, String date,String time,double bac, double latitudeValue, double longitudeValue,
                     String videoFile, String status, boolean faceMatchFailBAC, String deviceName){
        this.indexID = indexID;
        this.date=date;
        this.time=time;
        this.bac = bac;
        this.latitudeValue = latitudeValue;
        this.longitudeValue = longitudeValue;
        this.videoFile = videoFile;
        this.status = status;
        this.faceMatchFailBAC = faceMatchFailBAC;
        this.deviceName = deviceName;
    }

    public String getIndexID() {
        return indexID;
    }

    public void setIndexID(String indexID) {
        this.indexID = indexID;
    }

    public double getLatitudeValue() {
        return latitudeValue;
    }

    public void setLatitudeValue(double latitudeValue) {
        this.latitudeValue = latitudeValue;
    }

    public double getLongitudeValue() {
        return longitudeValue;
    }

    public void setLongitudeValue(double longitudeValue) {
        this.longitudeValue = longitudeValue;
    }

    public String getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(String videoFile) {
        this.videoFile = videoFile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BACRecord() {

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getBac() {
        return bac;
    }

    public void setBac(double bac) {
        this.bac = bac;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isFaceMatchFailBAC() {
        return faceMatchFailBAC;
    }

    public void setFaceMatchFailBAC(boolean faceMatchFailBAC) {
        this.faceMatchFailBAC = faceMatchFailBAC;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
