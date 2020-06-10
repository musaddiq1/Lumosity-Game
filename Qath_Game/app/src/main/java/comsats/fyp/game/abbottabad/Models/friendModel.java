package comsats.fyp.game.abbottabad.Models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class friendModel extends RealmObject {

    @PrimaryKey
    int ID;
    String status;
    int sender;
    int receiver;
    String text;
    String receiverName;
    String receiverProfile;
    String senderName;
    String senderProfile;

    public friendModel() {
    }

    public friendModel(int ID, String status, int sender, int receiver, String text, String receiverName, String receiverProfile) {
        this.ID = ID;
        this.status = status;
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.receiverName = receiverName;
        this.receiverProfile = receiverProfile;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setReceiverProfile(String receiverProfile) {
        this.receiverProfile = receiverProfile;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderProfile() {
        return senderProfile;
    }

    public void setSenderProfile(String senderProfile) {
        this.senderProfile = senderProfile;
    }

    public int getID() {
        return ID;
    }

    public String getStatus() {
        return status;
    }

    public int getSender() {
        return sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiverProfile() {
        return receiverProfile;
    }
}
