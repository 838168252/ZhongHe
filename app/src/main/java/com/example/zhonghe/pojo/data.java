package com.example.zhonghe.pojo;

public class data {
    private int id;
    private String TID;
    private String QR;
    private String batch;
    private String type;
    private String comment;
    private String time;
    private String condition;

    public data() {
    }

    public data(int id, String TID, String QR, String batch, String type, String comment, String time, String condition) {
        this.id = id;
        this.TID = TID;
        this.QR = QR;
        this.batch = batch;
        this.type = type;
        this.comment = comment;
        this.time = time;
        this.condition = condition;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTID() {
        return TID;
    }

    public void setTID(String TID) {
        this.TID = TID;
    }

    public String getQR() {
        return QR;
    }

    public void setQR(String QR) {
        this.QR = QR;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "data{" +
                "id=" + id +
                ", TID='" + TID + '\'' +
                ", QR='" + QR + '\'' +
                ", batch='" + batch + '\'' +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                ", time='" + time + '\'' +
                ", condition='" + condition + '\'' +
                '}';
    }
}
