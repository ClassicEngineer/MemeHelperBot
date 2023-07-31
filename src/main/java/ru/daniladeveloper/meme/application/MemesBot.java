package ru.daniladeveloper.meme.application;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.daniladeveloper.meme.api.ChatStage;
import ru.daniladeveloper.meme.domain.Meme;
import ru.daniladeveloper.meme.infrustructure.FindMemeResult;
import ru.daniladeveloper.meme.infrustructure.MemeFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, ChatStage> chatToStage = new ConcurrentHashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String incomeText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            ChatStage stage = this.chatToStage.get(chatId);
            if (stage == null ) {
                stage = new ChatStage(Stage.START);
                this.chatToStage.put(chatId, stage);
            }
            try {
                if (incomeText.equalsIgnoreCase(FIND)) {
                    stage.setStage(Stage.SEARCH_START_KEYWORDS);
                    sendText(chatId, "Enter key-words to find your mem");
                } else if (incomeText.equalsIgnoreCase(ADD)) {
                    stage.setStage(Stage.ADD_START_NAME);
                    sendText(chatId, "Enter name of mem");
                } else if (incomeText.equalsIgnoreCase(MENU) || incomeText.equalsIgnoreCase(CANCEL)) {
                    stage.setStage(Stage.START);
                    execute(getMenuMessage(chatId));
                }

                else {
                    switch (stage.getStage()) {
                            case START -> sendText(chatId, "You wanna play? Let's play!");
                            case SEARCH_START_KEYWORDS -> {
                                var result = memeStorage.findByInput(incomeText);
                                if (!result.isMultiple()) {
                                    sendTo(chatId, result.getResult());
                                    stage.setStage(Stage.START);
                                }
                                else {
                                    sendText(chatId, "We've found several results. Please choose:");
                                    sendText(chatId, result.toMultipleChoice());
                                    stage.setStage(Stage.SEARCH_CHOICE);
                                    stage.getSearchParameters().setFindResult(result);
                                }
                            }
                            case SEARCH_CHOICE -> {
                                FindMemeResult findResult = stage.getSearchParameters().getFindResult();
                                Optional<Meme> memeByNumber = findResult.getMemeByNumber(incomeText);
                                if (memeByNumber.isPresent()) {
                                    var meme = memeStorage.findByName(memeByNumber.get());
                                    sendTo(chatId, meme);
                                    stage.setStage(Stage.START);
                                }
                                else {
                                    sendText(chatId, "Unable to find meme\nPlease choose another one option.");
                                }
                            }
                            case ADD_START_NAME -> {
                                if (!isMemeNameUnique(incomeText)) {
                                    sendText(chatId, "Name for mem: '" + incomeText + "' is not unique!\nPlease choose another one.");
                                } else {
                                    stage.getAddParameters().setName(incomeText);
                                    stage.setStage(Stage.ADD_FILE);
                                    sendText(chatId, "Good name for mem: " + incomeText + "\nNow give me mem-file");

                                }
                            }
                            case ADD_DESCRIPTION -> {
                                stage.getAddParameters().setDescription(incomeText);
                                stage.setStage(Stage.ADD_CATEGORY);
                                sendText(chatId, "Now let's add category for mem");
                            }
                            case ADD_CATEGORY -> {
                                stage.getAddParameters().setCategory(incomeText);
                                memeStorage.addMemByStage(stage.getAddParameters());
                                stage.setStage(Stage.START);
                                sendText(chatId, "Good job! Mem was saved.");
                            }
                            default -> sendText(chatId, "We are sorry! so sorry!");
                        }
                    }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            try {
                String chatId = update.getMessage().getChatId().toString();
                ChatStage stage = chatToStage.get(chatId);
                if (stage != null) {
                    if (Objects.requireNonNull(stage.getStage()) == Stage.ADD_FILE) {
                        saveFile(update, stage);
                        stage.setStage(Stage.ADD_DESCRIPTION);
                        sendText(chatId, "Mem was loaded. Give me description!");
                    } else {
                        sendText(chatId, "Nice meme!");
                    }
                } else {
                    sendText(chatId, "Nice meme!");
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isMemeNameUnique(String name) {
        return memeStorage.isNameUnique(name);
    }

    private void saveFile(Update update, ChatStage stage) throws IOException {
        PhotoSize size = getPhoto(update);
        String path = getFilePath(size);
        java.io.File file = downloadPhotoByFilePath(path);
        String filename = stage.getAddParameters().getName();
        String fullFileName = MEMES_DIR + filename;
        Files.move(file.toPath(), Path.of(fullFileName));
        Long pictureId = memeStorage.saveFilePath(fullFileName);
        stage.getAddParameters().setPictureId(pictureId);
    }


    private void sendTo(String chatId, MemeFile meme) throws TelegramApiException {
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
    private void sendText(String chatId, String text) throws TelegramApiException {
        execute(new SendMessage(chatId, text));
    }

    @PostConstruct
    public void registration(){
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static BotApiMethod<Message> getMenuMessage(String chatId) {
        return SendMessage
            .builder()
            .text("Here is menu!")
            .chatId(chatId)
            .replyMarkup(getMenuKeyboardMarkup())
            .build();
    }

    private static ReplyKeyboard getMenuKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (var cmd : COMMANDS) {
            KeyboardButton button = new KeyboardButton();
            button.setText(cmd);
            row.add(button);
        }
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public PhotoSize getPhoto(Update update) {
        // Check that the update contains a message and the message has a photo
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            // When receiving a photo, you usually get different sizes of it
            List<PhotoSize> photos = update.getMessage().getPhoto();

            // We fetch the bigger photo
            return photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
        }

        // Return null if not found
        return null;
    }

    public String getFilePath(PhotoSize photo) {
        Objects.requireNonNull(photo);

        if (photo.getFilePath() != null) { // If the file_path is already present, we are done!
            return photo.getFilePath();
        } else { // If not, let find it
            // We create a GetFile method and set the file_id from the photo
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(photo.getFileId());
            try {
                // We execute the method using AbsSender::execute method.
                File file = execute(getFileMethod);
                // We now have the file_path
                return file.getFilePath();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        return null; // Just in case
    }

    public java.io.File downloadPhotoByFilePath(String filePath) {
        try {
            // Download the file calling AbsSender::downloadFile method
            return downloadFile(filePath);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return null;
    }

}
