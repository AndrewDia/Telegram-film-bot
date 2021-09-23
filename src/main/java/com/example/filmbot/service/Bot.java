package com.example.filmbot.service;

import com.example.filmbot.DBManager;
import com.example.filmbot.entity.Film;
import com.example.filmbot.Top;
import com.example.filmbot.entity.Chat;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Bot extends TelegramLongPollingBot {
    private static String chatId;
    private ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    private static Chat chat;
    private final DBManager dbManager = DBManager.getInstance();

    @Override
    public String getBotUsername() {
        return "imdb_film_bot";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        chatId = message.getChatId().toString();
        String text = message.getText();
        System.out.println("Message received " + text);

        chat = dbManager.getChat(message.getChatId());
        if (chat == null) {
            chat = new Chat(message.getChatId());
            dbManager.insertChat(chat);
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setText(getMessage(text));
        sendMessage.setChatId(chatId);
        System.out.println(chat);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String message) {
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        if (message.equals("/start") || message.equals("Back to Menu \uD83D\uDD19")) {
            KeyboardRow firstRow = new KeyboardRow();
            firstRow.add("Films Top");
            firstRow.add("Coming Soon");
            keyboardRows.add(firstRow);
            replyKeyboardMarkup.setKeyboard(keyboardRows);
            if (message.equals("/start"))
                return "Welcome to Film bot! Please choose a category: ";
            else
                return "Please choose a category: ";
        }

        if (message.equals("Films Top") || message.equals("Back \uD83D\uDD19")) {
            setTopFilmsKeyboard();
            return "Please choose what to sort films by: ";
        }

        if (message.equals("Ranking") || message.equals("IMDb Rating") || message.equals("Release Date")
                || message.equals("Number of Ratings") || message.equals("Load more")) {
            if (message.equals("Load more")) {
                int beginFrom = chat.getFilmNum() + 10;
                if (beginFrom >= 100) {
                    setTopFilmsKeyboard();
                    return "That is Top 100 Most Popular Films. Maybe you should choose another way of sorting: ";
                }
                chat.setFilmNum(beginFrom);
                dbManager.updateChat(chat);
                System.out.println(chat.getFilmNum());
            } else {
                chat.setSortingWay(message);
                chat.setFilmNum(0);
                dbManager.updateChat(chat);
            }
            KeyboardRow firstRow = new KeyboardRow();
            firstRow.add("Load more");
            firstRow.add("Back \uD83D\uDD19");
            keyboardRows.add(firstRow);
            replyKeyboardMarkup.setKeyboard(keyboardRows);
            try {
                if(chat.getSortingWay().equals("Coming Soon"))
                    getInfoFilm(new Top().getComingSoon(chat));
                getInfoFilm(new Top().getMostPopular(chat));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                setTopFilmsKeyboard();
                return "Please choose what to sort films by: ";
            }
            return "Do you want to see more films?";
        }

        if (message.equals("Coming Soon")) {
            KeyboardRow firstRow = new KeyboardRow();
            firstRow.add("Load more");
            firstRow.add("Next Month");
            KeyboardRow secondRow = new KeyboardRow();
            secondRow.add("Back to Menu \uD83D\uDD19");
            keyboardRows.add(firstRow);
            keyboardRows.add(secondRow);
            replyKeyboardMarkup.setKeyboard(keyboardRows);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
            chat.setFilmNum(0);
            chat.setSortingWay("Coming Soon");
            chat.setChosenPeriod(dateFormat.format(new Date()));
            dbManager.updateChat(chat);
            getInfoFilm(new Top().getComingSoon(chat));
            return "Enter a month with year to see coming films of chosen period or just tap one of the buttons";
        }

        if (message.matches("(?i)\\d{4} \\p{Alpha}+|\\p{Alpha}+( \\d{4})?")) {
            if(!chat.getSortingWay().equals("Coming Soon"))
                return "Sorry! I don't understand you";
            Calendar today = new GregorianCalendar();
            int month = today.get(Calendar.MONTH);
            int year = today.get(Calendar.YEAR);
            boolean found = false;

            String[] months = {"January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};
            for (int i = 0; i < months.length; i++) {
                Matcher matcher = Pattern.compile("(?i)(20\\d{2})? ?" + months[i] + " ?(20\\d{2})?").matcher(message);
                if (matcher.matches()) {
                    month = i;
                    if (matcher.group(1) != null || matcher.group(2) != null)
                        year = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : Integer.parseInt(matcher.group(2));
                    else if (month < today.get(Calendar.MONTH))
                        year++;
                    found = true;
                    break;
                }
            }
            if (!found)
                return "Please check the spelling or entered year.\nTry one more time!";

            Calendar wantedPeriod = new GregorianCalendar(year, month, 1);
            wantedPeriod.set(Calendar.DAY_OF_MONTH, wantedPeriod.getActualMaximum(Calendar.DATE));
            if (wantedPeriod.before(today))
                return "Sorry, you cannot select a date that has already passed.\nTry one more time!";

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
            String date = dateFormat.format(wantedPeriod.getTime());
            chat.setFilmNum(0);
            chat.setChosenPeriod(date);
            dbManager.updateChat(chat);
            getInfoFilm(new Top().getComingSoon(chat));
        }

        return "Sorry! I don't understand you";
    }

    private void getInfoFilm(String[] links) {
        for (String link : links) {
            Film film = new Film(link);
            SendPhoto sendPhoto = new SendPhoto();
            String path = "";
            try (InputStream in = new URL(film.getImg()).openStream()) {
                Files.copy(in, Paths.get(path)); //Download image from the Net
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(new File(path)));
                execute(sendPhoto); //send image
                Files.delete(Paths.get(path)); //delete image
            } catch (IOException e) {
                System.out.println("File not found");
                e.printStackTrace();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.enableHtml(true);
            sendMessage.setText(film.toString());
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void setTopFilmsKeyboard() {
        chat.setSortingWay("");
        chat.setFilmNum(0);
        dbManager.updateChat(chat);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add("Ranking");
        firstRow.add("IMDb Rating");
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add("Release Date");
        secondRow.add("Number of Ratings");
        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add("Back to Menu \uD83D\uDD19");
        keyboardRows.add(firstRow);
        keyboardRows.add(secondRow);
        keyboardRows.add(thirdRow);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
    }
}
