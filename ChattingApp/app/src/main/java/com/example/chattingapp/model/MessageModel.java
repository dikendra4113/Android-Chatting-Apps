package com.example.chattingapp.model;

public class MessageModel {
    private String message;
    private String sender;
    private String time;
    private String uid;
    public int viewCode;



    public MessageModel(){

    }
    public int getViewCode() {
        return viewCode;
    }

    public void setViewCode(int viewCode) {
        this.viewCode = viewCode;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
