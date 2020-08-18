package com.techtown.ainglish.JSON;

public class ToServerJSON {
    String server_id;
    String server_phone;
    String server_email;
    String server_password;
    String server_image;
    String server_nickname;
    String server_year;
    String server_month;
    String server_day;

    //카카오 로그인때 쓰이는 식별자이다.
    String kakao_profile;

    //서버로부터 정보를 받아올 때 쓰인다.
    String server_birthday;
    String server_coin;
    String server_study_day;

    public String getServer_coin() {
        return server_coin;
    }

    public String getServer_study_day() {
        return server_study_day;
    }

    public String getServer_birthday() {
        return server_birthday;
    }

    public void setKakao_profile(String kakao_profile) {
        this.kakao_profile = kakao_profile;
    }

    public void setServer_image(String server_image) {
        this.server_image = server_image;
    }

    public ToServerJSON(String server_phone, String server_email, String server_password,
                        String server_nickname, String server_year,
                        String server_month, String server_day) {
        this.server_phone = server_phone;
        this.server_email = server_email;
        this.server_password = server_password;
        this.server_nickname = server_nickname;
        this.server_year = server_year;
        this.server_month = server_month;
        this.server_day = server_day;
    }

    public String getServer_phone() {
        return server_phone;
    }

    public void setServer_phone(String server_phone) {
        this.server_phone = server_phone;
    }

    public String getServer_email() {
        return server_email;
    }

    public void setServer_email(String server_email) {
        this.server_email = server_email;
    }

    public String getServer_password() {
        return server_password;
    }

    public void setServer_password(String server_password) {
        this.server_password = server_password;
    }

    public String getServer_image() {
        return server_image;
    }

    public String getServer_nickname() {
        return server_nickname;
    }

    public void setServer_nickname(String server_nickname) {
        this.server_nickname = server_nickname;
    }

    public String getServer_year() {
        return server_year;
    }

    public void setServer_year(String server_year) {
        this.server_year = server_year;
    }

    public String getServer_month() {
        return server_month;
    }

    public void setServer_month(String server_month) {
        this.server_month = server_month;
    }

    public String getServer_day() {
        return server_day;
    }

    public void setServer_day(String server_day) {
        this.server_day = server_day;
    }

    public String getServer_id() {
        return server_id;
    }
}
