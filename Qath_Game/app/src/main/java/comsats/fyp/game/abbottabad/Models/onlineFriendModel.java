package comsats.fyp.game.abbottabad.Models;

public class onlineFriendModel {

    int friendID;
    String last_seen;
    String state;

    public onlineFriendModel(int friendID, String last_seen, String state) {
        this.friendID = friendID;
        this.last_seen = last_seen;
        this.state = state;
    }

    public int getFriendID() {
        return friendID;
    }

    public String getLast_seen() {
        return last_seen;
    }

    public String getState() {
        return state;
    }
}
