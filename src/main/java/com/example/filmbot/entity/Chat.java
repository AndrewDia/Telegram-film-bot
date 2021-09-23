package com.example.filmbot.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Chat {
    private long chatId;
    private int filmNum;
    private String sortingWay;
    private String chosenPeriod;

    public Chat(long chatID) {
        this.chatId = chatID;
        filmNum = 0;
        sortingWay = "";
        chosenPeriod = new SimpleDateFormat("yyyy-MM").format(new Date());
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public int getFilmNum() {
        return filmNum;
    }

    public void setFilmNum(int pageNum) {
        this.filmNum = pageNum;
    }

    public String getSortingWay() {
        return sortingWay;
    }

    public void setSortingWay(String sortingWay) {
        this.sortingWay = sortingWay;
    }

    public String getChosenPeriod() {
        return chosenPeriod;
    }

    public void setChosenPeriod(String chosenPeriod) {
        this.chosenPeriod = chosenPeriod;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "chatId=" + chatId +
                ", filmNum=" + filmNum +
                ", sortingWay='" + sortingWay + '\'' +
                ", chosenPeriod='" + chosenPeriod + '\'' +
                '}';
    }
}
