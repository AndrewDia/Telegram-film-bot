package com.example.filmbot.entity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Film {
    private Document document;

    public Film(String href) {
        try {
            document = Jsoup.connect(href).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        Elements elements = document.getElementsByClass("llYePj");
        if (elements.text().isEmpty())
            return document.title();
        return elements.text().replace("Original title: ", "");
    }

    public String getRating() {
        Elements elements = document.getElementsByClass("iTLWoV");
        if (elements.first() == null)
            return "-";
        return elements.first().text() + "/10";
    }

    public String getYear() {
        Elements elements = document.getElementsByClass("rgaOW");
        if (elements.first() == null)
            return "-";
        return elements.first().text();
    }

    public String getGenres() {
        Elements elements = document.getElementsByClass("fzmeux");
        String[] genres = elements.text().split(" ");
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < genres.length; i++) {
            str.append(genres[i]);
            if (i < genres.length - 1)
                str.append(", ");
        }
        return str.toString();
    }

    public String getDescription() {
        Elements elements = document.getElementsByClass("gCtawA");
        return elements.text();
    }

    public String getDirector() {
        Elements elements = document.getElementsByClass("ipc-metadata-list-item__list-content-item");
        return elements.first().text();
    }

    public String getActors() {
        Elements elements = document.getElementsByClass("eyqFnv");
        return elements.get(0) + ", " + elements.get(1) + ", " + elements.get(2);
    }

    public String getImg() {
        Elements elements = document.getElementsByClass("ipc-image");
        Matcher matcher = Pattern.compile(".+/.+?(?=\\.)").matcher(elements.attr("src"));
        StringBuilder url = new StringBuilder();
        if (matcher.find())
            url.append(matcher.group());
//        return url.toString();
        return elements.attr("src");
    }

    @Override
    public String toString() {
        return "<b>" + getTitle() + "</b>" +
                "\n<b>IMDb Rating</b>: " + getRating() +
                "\n<b>Year</b>: " + getYear() +
                "\n<b>Genres</b>: " + getGenres() +
                "\n<b>Director</b>: " + getDirector() +
                "\n<b>Actors</b>: " + getActors() +
                "\n\n<b>Description</b>:\n" + getDescription();
    }
}
