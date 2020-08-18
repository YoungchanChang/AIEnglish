package com.techtown.ainglish.JSON;

public class ChatMessageJSON {
    private String user_info;

    private String teacher_info;

    private String position;

    private String user_profile; //글라이드로 쓸 예정

    //is_picture==TRUE인지 FALSE인지에 따라서
    //user_message가 사진으로 해석되거나 메시지로 해석된다.
    private String is_picture;

    private String user_message;

    private String user_nickname;

    private String user_chat_time;


    public String getUser_nickname() {
        return user_nickname;
    }

    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }

    public String getIs_picture() {
        return is_picture;
    }

    public void setIs_picture(String is_picture) {
        this.is_picture = is_picture;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTeacher_info() {
        return teacher_info;
    }

    public void setTeacher_info(String teacher_info) {
        this.teacher_info = teacher_info;
    }

    public String getUser_profile() {
        return user_profile;
    }

    public void setUser_profile(String user_profile) {
        this.user_profile = user_profile;
    }

    public String getUser_chat_time() {
        return user_chat_time;
    }

    public void setUser_chat_time(String user_chat_time) {
        this.user_chat_time = user_chat_time;
    }


    public String getUser_message() {
        return user_message;
    }

    public void setUser_message(String user_message) {
        this.user_message = user_message;
    }

    public String getUser_info() {
        return user_info;
    }

    public void setUser_info(String user_info) {
        this.user_info = user_info;
    }
}
