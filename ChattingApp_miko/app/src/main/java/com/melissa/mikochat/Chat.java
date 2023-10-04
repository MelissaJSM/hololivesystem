package com.melissa.mikochat;

public class Chat {
    //Data Transfer Object

    private String name;
    private String msg;
    private String question;
    private String answer;
    private String member;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }


    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }


    public String getAwnser() {
        return answer;
    }
    public void setAwnser(String answer) { // 이 메소드 이름 따라가네 아놔
        this.answer = answer;
    }

    public String getMember() {
        return member;
    }
    public void setMember(String member) { // 이 메소드 이름 따라가네 아놔
        this.member = member;
    }


}
