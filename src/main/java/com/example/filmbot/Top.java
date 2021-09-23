package com.example.filmbot;

import com.example.filmbot.entity.Chat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Top {
    private Document document;

    public String[] getMostPopular(Chat chat) {
        String link = "";
        String sortBy = chat.getSortingWay();
        if (sortBy.equals("Ranking"))
            link = "https://www.imdb.com/chart/moviemeter/?sort=rk,asc&mode=simple&page=1";
        if (sortBy.equals("IMDb Rating"))
            link = "https://www.imdb.com/chart/moviemeter/?sort=ir,desc&mode=simple&page=1";
        if (sortBy.equals("Release Date"))
            link = "https://www.imdb.com/chart/moviemeter/?sort=us,desc&mode=simple&page=1";
        if (sortBy.equals("Number of Ratings"))
            link = "https://www.imdb.com/chart/moviemeter/?sort=nv,desc&mode=simple&page=1";

        try {
            document = Jsoup.connect(link).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] links = new String[10];
        Elements hrefs = document.select("td.titleColumn > a");
        for (int i = chat.getFilmNum(); i < chat.getFilmNum() + 10; i++) {
            Matcher matcher = Pattern.compile(".+/").matcher(hrefs.get(i).attr("href"));
            String linkToFilm = "https://www.imdb.com";
            if (matcher.find())
                linkToFilm += matcher.group();
            links[i % 10] = linkToFilm;
        }
        return links;
    }

    public String[] getComingSoon(Chat chat) {
        try {
            document = Jsoup.connect("https://www.imdb.com/movies-coming-soon/" + chat.getChosenPeriod() + "/").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] links = new String[10];
        Elements hrefs = document.select("td.overview-top > h4 > a");
        int beginFrom = chat.getFilmNum();
        int endWith = beginFrom + 10;
        if (hrefs.size() < endWith) {
            endWith = hrefs.size();
            links = new String[endWith];
        }
        for (int i = beginFrom; i < endWith; i++) {
            Matcher matcher = Pattern.compile(".+/").matcher(hrefs.get(i).attr("href"));
            String linkToFilm = "https://www.imdb.com";
            if (matcher.find())
                linkToFilm += matcher.group();
            links[i % 10] = linkToFilm;
        }
        return links;
    }
}
