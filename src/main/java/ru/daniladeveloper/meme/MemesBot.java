package ru.daniladeveloper.meme;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
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
import ru.daniladeveloper.meme.infrustructure.MemeFile;
import ru.daniladeveloper.meme.infrustructure.Stage;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.ADMIN;
import static ru.daniladeveloper.meme.infrustructure.Constants.*;


@Log
@Component
public class MemesBot extends AbilityBot {

    @Autowired
    private MemeStorage memeStorage;

    private Long creatorId;

    private final Map<String, ChatAddStage> chatToStage;

    public MemesBot(@Value("${botToken}") String token,
                    @Value("${botUserName}")String username,
                    @Value("${creatorId}") Long creatorId) {
        super(token, username);
        this.creatorId = creatorId;
        this.chatToStage = new ConcurrentHashMap<>();
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    public Ability showMenu() {
        return Ability
            .builder()
            .name("menu")
            .info("Shows menu")
            .input(0)
            .locality(USER)
            .privacy(ADMIN)
            .action(ctx -> silent.execute(getMenuMessage(ctx.chatId().toString())))
            .build();
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


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String incomeText = update.getMessage().getText();
            SendMessage message = new SendMessage();
            String chatId = update.getMessage().getChatId().toString();
            message.setChatId(chatId);
            ChatAddStage stage = this.chatToStage.get(chatId);
            if (stage == null ) {
                stage = new ChatAddStage(Stage.START);
                this.chatToStage.put(chatId, stage);

            }
            try {
                if (incomeText.equalsIgnoreCase(FIND)) {
                    stage.setStage(Stage.SEARCH_START_KEYWORDS);
                    message.setText("Enter key-words");
                    execute(message);
                } else if (incomeText.equalsIgnoreCase(ADD)) {
                    stage.setStage(Stage.ADD_START_NAME);
                    message.setText("Enter name of mem");
                    execute(message);
                }

                else {
                    switch (stage.getStage()) {
                            case START -> {
                                message.setText("You wanna play , let's play!");
                                execute(message);
                            }
                            case SEARCH_START_KEYWORDS -> {
                                var mem = memeStorage.findByInput(incomeText);
                                sendTo(chatId, mem);
                                stage.setStage(Stage.START);
                            }
                            case ADD_START_NAME -> {
                                stage.setName(incomeText);
                                stage.setStage(Stage.ADD_FILE);
                                execute(new SendMessage(chatId, "Good name for mem: " + incomeText + "\nNow give me mem-file"));
                            }
                            case ADD_DESCRIPTION -> {
                                stage.setDescription(incomeText);
                                stage.setStage(Stage.ADD_CATEGORY);
                                execute(new SendMessage(chatId, "Now add category for mem"));
                            }
                            case ADD_CATEGORY -> {
                                stage.setCategory(incomeText);
                                memeStorage.addMemByStage(stage);
                                stage.setStage(Stage.START);
                                execute(new SendMessage(chatId, "Good job! Mem was saved."));
                            }
                        }
                    }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            try {
                String chatId = update.getMessage().getChatId().toString();
                ChatAddStage stage = chatToStage.get(chatId);
                if (stage != null) {
                    if (Objects.requireNonNull(stage.getStage()) == Stage.ADD_FILE) {
                        saveFile(update, stage);
                        stage.setStage(Stage.ADD_DESCRIPTION);
                        execute(new SendMessage(chatId, "Mem was loaded. Print description!"));
                    } else {
                        execute(new SendMessage(chatId, "Nice meme!"));
                    }
                } else {
                    execute(new SendMessage(chatId, "Nice meme!"));
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void saveFile(Update update, ChatAddStage stage) throws IOException {
        PhotoSize size = getPhoto(update);
        String path = getFilePath(size);
        java.io.File file = downloadPhotoByFilePath(path);
        String filename = stage.getName();
        String fullFileName = MEMES_DIR + filename;
        Files.move(file.toPath(), Path.of(fullFileName));
        Long pictureId = memeStorage.saveFilePath(fullFileName);
        stage.setPictureId(pictureId);
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

    @PostConstruct
    public void post(){
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
