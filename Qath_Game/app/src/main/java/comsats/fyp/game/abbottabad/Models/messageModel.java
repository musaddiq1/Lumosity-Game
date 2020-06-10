package comsats.fyp.game.abbottabad.Models;

import java.util.Comparator;

public class messageModel {

    String messageText;
    Long messageTime;
    int sender;
    int receiver;

    public String getMessageText() {
        return messageText;
    }

    public Long getMessageTime() {
        return messageTime;
    }

    public int getSender() {
        return sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public messageModel(String messageText, Long messageTime, int sender, int receiver) {
        this.messageText = messageText;
        this.messageTime = messageTime;
        this.sender = sender;
        this.receiver = receiver;
    }

    public static Comparator<messageModel> getCompareByTime() {
        return COMPARE_BY_TIME;
    }

    public static Comparator<messageModel> COMPARE_BY_TIME = new Comparator<messageModel>() {

        @Override
        public int compare(messageModel one, messageModel other) {
            return one.getMessageTime().compareTo(other.getMessageTime());
        }
    };
}
