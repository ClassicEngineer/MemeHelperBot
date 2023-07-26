package ru.daniladeveloper.meme;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.daniladeveloper.meme.infrustructure.Constants;
import ru.daniladeveloper.meme.infrustructure.MemeFile;

import java.util.Collection;

import static ru.daniladeveloper.meme.infrustructure.Constants.*;


@Log
@Component
@RequiredArgsConstructor
public class MemesBot extends TelegramLongPollingBot {

    private final MemeStorage memeStorage;

    @Value("${botToken}")
    private String botToken;

    @Value("${botUserName}")
    private String botUserName;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String incomeText = update.getMessage().getText();
            SendMessage message = new SendMessage();
            String chatId = update.getMessage().getChatId().toString();
            message.setChatId(chatId);
            String messageText = "Are you SURE about it ?!";

            try {
                if (incomeText.contains(FIND)) {
                    MemeFile meme = memeStorage.findByInput(incomeText);
                    sendTo(chatId, meme);
                } else if (incomeText.contains(CATEGORY)) {
                    Collection<MemeFile> memes = memeStorage.findByCategory(incomeText);
                    for (MemeFile mem : memes) {
                        sendTo(chatId, mem);
                    }
                }
                else if (incomeText.contains(Constants.ADD)) {
                    messageText = "Saved.";
                }

                else {
                    message.setText(messageText);
                    execute(message);
                }

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }



    @PostConstruct
    public void post(){
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    public void sendTo(String chatId, MemeFile meme) throws TelegramApiException {
        switch (meme.type) {
            case GIF -> {
                SendAnimation animation = new SendAnimation(chatId, meme.getInputFile());
                execute(animation);
            } case PICTURE -> {
                SendPhoto photo = new SendPhoto(chatId, meme.getInputFile());
                execute(photo);
            }
        }
    }

}
