package comsats.fyp.game.abbottabad.GamePlay;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.Socket;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import comsats.fyp.game.abbottabad.Adapters.inboxChatAdapter;
import comsats.fyp.game.abbottabad.Models.messageModel;
import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.Rules;
import comsats.fyp.game.abbottabad.storeRoom.URLs;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;
import comsats.fyp.game.abbottabad.storeRoom.socketConnectionHandler;

public class online_with_friends extends AppCompatActivity implements MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener {

    public final String TAG = "online_random";

    Rules rules;

    public ArrayList<ImageView> whiteCheckers;
    public ArrayList<ImageView> blackCheckers;
    public ArrayList<FrameLayout> higBoxAreas;
    public ImageView selectedChecker;
    public FrameLayout areaToMoveTo;
    public HashMap<ImageView, Integer> checkerPositions;

    public boolean hasSelectedChecker = false;
    public boolean removeNextChecker = false;
    public boolean isWin = false;

    public ArrayList<String> whiteIndexes = new ArrayList<>();
    public ArrayList<String> blackIndexes = new ArrayList<>();

    TextView game_play_status;

    ProgressDialog progressDialog;
    Socket socket;

    LinearLayout blackCheckerArea, whiteCheckerArea;
    RelativeLayout board;

    Boolean isGameStarted = false;
    Boolean isGameRequestMade = false;

    Boolean isPlayerWhite = false;
    Boolean isPlayerBlack = false;
    String otherPlayerSocketID = "";
    int otherPlayerID;
    String mySocketID = "";

    LinearLayout white_box_layout, black_box_layout;
    ImageView white_profile, black_profile;
    TextView black_name, white_name;

    Boolean canIRemove = false;
    String gameID;
    JSONObject gameStartedOBject;

    TextView white_timer, black_timer;
    ImageView white_clock, black_clock;
    CountDownTimer timerForWhite = null, timerForBlack;
    Integer totalTimerTimeBlack;
    Integer totalTimerTimeWhite;
    Timer timer;

    Boolean trigerGameConnectionTimeOut = true;
    BroadcastReceiver connectionLostWithServer_Receiver;

    int friendRequestID;
    String friendRequestStatus;
    Boolean shouldRemoveRequest = false;
    int requestSender;
    String whiteCurrentBalls, blackCurrentBalls;

    ImageView chat_black, chat_white;
    ArrayList<messageModel> messageList;
    inboxChatAdapter inboxChatAdapterObject;

    private MediaPlayer mediaPlayer;
    private int resumePosition = 0;
    private AudioManager audioManager;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    int whiteMoveCounter = 0, blackMoveCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_play_online_with_friends);

        getSupportActionBar().hide();

        if (requestAudioFocus()) {
            //foucs gain
            if (preferenceManager.getInstance(getApplicationContext()).isGameSoundEnabled()) {
                initMediaPlayer();
            }
        }

        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();

        rules = new Rules();

        messageList = new ArrayList<>();
        inboxChatAdapterObject = new inboxChatAdapter(messageList, preferenceManager.getInstance(getApplicationContext()).getUserID());

        chat_black = findViewById(R.id.chat_black);
        chat_white = findViewById(R.id.chat_white);

        black_timer = findViewById(R.id.black_timer);
        black_clock = findViewById(R.id.black_clock);

        white_timer = findViewById(R.id.white_timer);
        white_clock = findViewById(R.id.white_clock);

        white_profile = findViewById(R.id.white_profile);
        black_profile = findViewById(R.id.black_profile);
        black_name = findViewById(R.id.black_name);
        white_name = findViewById(R.id.white_name);

        white_box_layout = findViewById(R.id.white_box_layout);
        black_box_layout = findViewById(R.id.black_box_layout);

        board = findViewById(R.id.board);
        blackCheckerArea = findViewById(R.id.blackCheckerArea);
        whiteCheckerArea = findViewById(R.id.whiteCheckerArea);

        selectedChecker = null;
        areaToMoveTo = null;
        checkerPositions = new HashMap<>();

        game_play_status = (TextView) findViewById(R.id.game_play_status);

        socket = socketConnectionHandler.getInstance(getApplicationContext()).getConnection();

        white_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewUserProfile("whiteInfo");
            }
        });

        black_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewUserProfile("blackInfo");
            }
        });

        // dis-connecting previous listeners
        socket.off("requestDefinedGame");
        socket.off("gameStarted");
        socket.off("gameFinished");
        socket.off("gameWonByPlayer");
        socket.off("playerLostTurn");
        socket.off("receivedPlayerMove");


        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Connecting server");
        progressDialog.setCancelable(false);
        progressDialog.show();

        gameConnectionTimeOut();
        try {

            if (getIntent().getExtras().getString("way").trim().equalsIgnoreCase("adapter")) {
                JSONObject _reqJson = new JSONObject();
                _reqJson.put("friendID", getIntent().getExtras().getInt("friendID"));
                _reqJson.put("friendName", getIntent().getExtras().getString("friendName"));
                _reqJson.put("requestedBy", preferenceManager.getInstance(getApplicationContext()).getUserName());

                socket.emit("requestDefinedGame", _reqJson, new Ack() {
                    @Override
                    public void call(final Object... args) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                try {
                                    JSONObject jsonObject = (JSONObject) args[0];
                                    Log.i(TAG, "requestGame : " + jsonObject);
                                    Boolean error = jsonObject.getBoolean("error");

                                    trigerGameConnectionTimeOut = false;
                                    if (timer != null) {
                                        timer.cancel();
                                    }

                                    if (!error) {
                                        Boolean playerFound = jsonObject.getBoolean("playerFound");

                                        isGameRequestMade = true;
                                        if (!playerFound) {
                                            // un hide views

                                            game_play_status.setVisibility(View.VISIBLE);
                                            game_play_status.setText(jsonObject.getString("message"));
                                        }
                                    } else {
                                        showMessage(jsonObject.getString("message"));
                                    }
                                } catch (Exception e) {
                                    Log.i(TAG, "JSON exception : requestGame " + e);
                                }
                            }
                        });
                    }
                });
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("friendID", getIntent().getExtras().getInt("friendID"));
                socket.emit("startTheDefinedGame", jsonObject, new Ack() {
                    @Override
                    public void call(final Object... objects) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                                trigerGameConnectionTimeOut = false;
                                if (timer != null) {
                                    timer.cancel();
                                }

                                JSONObject ob = (JSONObject) objects[0];
                                try {
                                    if (!ob.getBoolean("error")) {
                                        game_play_status.setText(ob.getString("message"));
                                    } else {
                                        showMessage(ob.getString("message"));
                                    }
                                } catch (Exception e) {
                                    Log.i(TAG, e + "");
                                }
                            }
                        });
                    }
                });
            }

            socket.on("friendWentDown", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject o = (JSONObject) args[0];
                                if (!isGameStarted && !o.getBoolean("error")) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(online_with_friends.this);
                                    builder.setMessage(getIntent().getExtras().getString("friendName") + " went offline.\nGame can not be proceed ahead.");
                                    builder.setCancelable(false);
                                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            } catch (Exception e) {
                                Log.i(TAG, e + "");
                            }
                        }
                    });
                }
            });


            socket.on("gameRequestRejected", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject o = (JSONObject) args[0];
                            try {
                                if (!o.getBoolean("error")) {
                                    String text = o.getString("message");
                                    AlertDialog.Builder builder = new AlertDialog.Builder(online_with_friends.this);
                                    builder.setMessage(text);
                                    builder.setCancelable(false);
                                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            } catch (Exception e) {
                                Log.i(TAG, "JSON exception : gameRequestRejected " + e);
                            }

                        }
                    });
                }
            });


            socket.on("gameStarted", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                gameStartedOBject = (JSONObject) args[0];
                                Log.i(TAG, "gameStarted : " + gameStartedOBject);
                                Boolean error = gameStartedOBject.getBoolean("error");
                                if (!error) {

                                    isGameStarted = true;

                                    game_play_status.setVisibility(View.GONE);
                                    blackCheckerArea.setVisibility(View.VISIBLE);
                                    whiteCheckerArea.setVisibility(View.VISIBLE);
                                    black_box_layout.setVisibility(View.VISIBLE);
                                    white_box_layout.setVisibility(View.VISIBLE);

                                    Toast.makeText(online_with_friends.this, gameStartedOBject.getString("message"), Toast.LENGTH_SHORT).show();

                                    board.setVisibility(View.VISIBLE);

                                    JSONObject dataObject = gameStartedOBject.getJSONObject("data");

                                    int whiteID = dataObject.getInt("white");
                                    int blackID = dataObject.getInt("black");

                                    if (preferenceManager.getInstance(getApplicationContext()).getUserID() == whiteID) {
                                        isPlayerWhite = true;
                                        chat_black.setVisibility(View.VISIBLE);
                                        mySocketID = dataObject.getString("white_socketID");
                                        otherPlayerSocketID = dataObject.getString("black_socketID");
                                        otherPlayerID = dataObject.getInt("black");
                                    } else {
                                        isPlayerBlack = true;
                                        chat_white.setVisibility(View.VISIBLE);
                                        mySocketID = dataObject.getString("black_socketID");
                                        otherPlayerSocketID = dataObject.getString("white_socketID");
                                        otherPlayerID = dataObject.getInt("white");
                                    }

                                    gameID = dataObject.getString("gameID");

                                    JSONObject friendsOb = dataObject.getJSONObject("friendsOb");
                                    friendRequestStatus = friendsOb.getString("friendStatus").trim();
                                    if (friendRequestStatus.equalsIgnoreCase("not_friends")) {
                                        friendRequestID = -99;
                                        requestSender = -99;
                                    } else {
                                        requestSender = friendsOb.getInt("requestSender");
                                        friendRequestID = friendsOb.getInt("friendID");
                                    }

                                    JSONObject playerInfoBlack = dataObject.getJSONObject("blackInfo");
                                    JSONObject playerInfoWhite = dataObject.getJSONObject("whiteInfo");
                                    JSONObject whiteProfileOb = playerInfoWhite.getJSONObject("profile");
                                    JSONObject blackProfileOb = playerInfoBlack.getJSONObject("profile");


                                    whiteCurrentBalls = playerInfoWhite.getString("currentBalls");
                                    blackCurrentBalls = playerInfoBlack.getString("currentBalls");

                                    white_name.setText(playerInfoWhite.getString("name"));


                                    if (whiteProfileOb.getString("provider").trim().equals("Qath")) {
                                        // use qath api to get image
                                        if (!whiteProfileOb.getString("image").trim().equals("none")) {
                                            Picasso.get().
                                                    load(URLs.UPLOADED_FILES(whiteProfileOb.getString("image").trim()))
                                                    .placeholder(R.drawable.user)
                                                    .error(R.drawable.user)
                                                    .into(white_profile);
                                        }
                                    } else {
                                        if (!whiteProfileOb.getString("image").trim().equals("none")) {
                                            Picasso.get().
                                                    load(whiteProfileOb.getString("image").trim())
                                                    .placeholder(R.drawable.user)
                                                    .error(R.drawable.user)
                                                    .into(white_profile);
                                        }
                                    }

                                    if (blackProfileOb.getString("provider").trim().equals("Qath")) {
                                        // use qath api to get image
                                        if (!blackProfileOb.getString("image").trim().equals("none")) {
                                            Picasso.get().
                                                    load(URLs.UPLOADED_FILES(blackProfileOb.getString("image").trim()))
                                                    .placeholder(R.drawable.user)
                                                    .error(R.drawable.user)
                                                    .into(black_profile);
                                        }
                                    } else {
                                        if (!blackProfileOb.getString("image").trim().equals("none")) {
                                            Picasso.get().
                                                    load(blackProfileOb.getString("image").trim())
                                                    .placeholder(R.drawable.user)
                                                    .error(R.drawable.user)
                                                    .into(black_profile);
                                        }
                                    }

                                    black_name.setText(playerInfoBlack.getString("name"));

                                    game_play_status.setVisibility(View.GONE);
                                    blackCheckerArea.setVisibility(View.VISIBLE);
                                    whiteCheckerArea.setVisibility(View.VISIBLE);
                                    board.setVisibility(View.VISIBLE);

                                    if (whiteCurrentBalls.trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                                        totalTimerTimeWhite = constants.SILVER_GAME_TIME;
                                    } else if (whiteCurrentBalls.trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                                        totalTimerTimeWhite = constants.GOLD_GAME_TIME;
                                    } else if (whiteCurrentBalls.trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                                        totalTimerTimeWhite = constants.DIAMOND_GAME_TIME;
                                    }

                                    if (blackCurrentBalls.trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                                        totalTimerTimeBlack = constants.SILVER_GAME_TIME;
                                    } else if (blackCurrentBalls.trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                                        totalTimerTimeBlack = constants.GOLD_GAME_TIME;
                                    } else if (blackCurrentBalls.trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                                        totalTimerTimeBlack = constants.DIAMOND_GAME_TIME;
                                    }

                                    renderView();

                                    if (rules.getTurn() == constants.BLACK) {
                                        // start black timer

                                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));

                                        startBlackTimer();

                                    } else {
                                        // start white timer

                                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));

                                        startWhiteTimer();
                                    }


                                }
                            } catch (Exception e) {
                                Log.i(TAG, "JSON exception : gameStarted " + e);
                            }

                        }
                    });
                }
            });

            socket.on("gameFinished", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final JSONObject jsonObject = (JSONObject) args[0];
                                Log.i(TAG, jsonObject + "");
                                if (!jsonObject.getBoolean("error")) {
                                    if (timer != null) {
                                        timer.cancel();
                                    }

                                    resetWhiteTimer();
                                    resetBlackTimer();

                                    int wonPlayerID = jsonObject.getInt("wonPlayerID");
                                    if (preferenceManager.getInstance(getApplicationContext()).getUserID() == wonPlayerID) {
                                        preferenceManager.getInstance(getApplicationContext()).setGameWonCount(preferenceManager.getInstance(getApplicationContext()).getGameWonCount() + 1);
                                        preferenceManager.getInstance(getApplicationContext()).setUserCoins(preferenceManager.getInstance(getApplicationContext()).getUserCoins() + constants.GAME_CHARGES);
                                    } else {
                                        preferenceManager.getInstance(getApplicationContext()).setGameLostCount(preferenceManager.getInstance(getApplicationContext()).getGameLostCount() + 1);
                                        preferenceManager.getInstance(getApplicationContext()).setUserCoins(preferenceManager.getInstance(getApplicationContext()).getUserCoins() - constants.GAME_CHARGES);
                                    }

                                    AlertDialog.Builder builder = new AlertDialog.Builder(online_with_friends.this);
                                    builder.setMessage(jsonObject.getString("message"));
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            } catch (Exception e) {
                                Log.i(TAG, "JSON exception : gameFinished " + e);
                            }
                        }
                    });
                }
            });

            socket.on("gameWonByPlayer", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                resetBlackTimer();
                                resetWhiteTimer();
                                final JSONObject jsonObject = (JSONObject) args[0];
                                if (!jsonObject.getBoolean("error")) {
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    Log.i(TAG, "gameWonByPlayer : " + jsonObject);
                                    if (data.getString("gameID").trim().equals(gameID)) {
                                        if (data.getInt("wonPlayerID") == preferenceManager.getInstance(getApplicationContext()).getUserID()) {
                                            showGameWinMessage("Hurrah!!!\nYou have won the game.\nBlack Moves = " + blackMoveCounter + " \nWhite Moves = " + whiteMoveCounter);
                                        } else {
                                            showGameWinMessage("Alas!!!\n" + data.getString("wonPlayerName") + " has won the game.\nBlack Moves = " + blackMoveCounter + " \nWhite Moves = " + whiteMoveCounter);
                                        }
                                    } else {
                                        Log.i(TAG, "Not this game");
                                    }
                                }
                            } catch (Exception e) {
                                Log.i(TAG, "JSON exception : gameWonByPlayer " + e);
                            }
                        }
                    });
                }
            });


            socket.on("playerLostTurn", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final JSONObject jsonObject = (JSONObject) args[0];
                                Log.i(TAG, jsonObject + "" + rules);
                                if (!jsonObject.getBoolean("error")) {
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    String type = data.getString("type");
                                    int indexFromList = data.getInt("index");

                                    Log.i(TAG, "Index from list = " + indexFromList);
                                    if (indexFromList != -99) {
                                        View v = null;

                                        if (mediaPlayer != null && mediaPlayer.isPlaying())
                                            mediaPlayer.setVolume(0.1f, 0.1f);

                                        MediaPlayer tickPlayer = MediaPlayer.create(getApplicationContext(), R.raw.kill_ball);
                                        tickPlayer.setVolume(1f, 1f);
                                        tickPlayer.start();
                                        tickPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {
                                                if (mediaPlayer != null && mediaPlayer.isPlaying())
                                                    mediaPlayer.setVolume(1f, 1f);
                                            }
                                        });

                                        if (selectedChecker != null) {
                                            selectedChecker.setAlpha(1.0f);
                                        }

                                        if (type.trim().equalsIgnoreCase("black")) {
                                            v = blackCheckers.get(indexFromList);
                                        } else {
                                            v = whiteCheckers.get(indexFromList);
                                        }


                                        if (type.trim().equalsIgnoreCase("black")) {
                                            rules.remove(checkerPositions.get(v), constants.BLACK);
                                        } else {
                                            rules.remove(checkerPositions.get(v), constants.WHITE);
                                        }


                                        ViewGroup parent = ((ViewGroup) v.getParent());
                                        final int index = parent.indexOfChild(v);
                                        parent.removeView(v);
                                        FrameLayout placeholder = (FrameLayout) getLayoutInflater().inflate(R.layout.layout_placeholder, parent, false);
                                        parent.addView(placeholder, index);
                                        //  checkerPositions.put((ImageView) v, -99);
                                        checkerPositions.remove((ImageView) v);


                                        if (type.trim().equalsIgnoreCase("black")) {
                                            int prev = rules.getBlackMarkers();
                                            if (prev != 0) {
                                                rules.setBlackMarkers(rules.getBlackMarkers() - 1);
                                            }
                                        } else {
                                            int prev = rules.getWhiteMarkers();
                                            if (prev != 0) {
                                                rules.setWhiteMarkers(rules.getWhiteMarkers() - 1);
                                            }
                                        }

                                        unMarkAllFields();

                                        isWin = rules.isItAWin(rules.getTurn());

                                        Log.i(TAG, "isWin " + isWin);

                                        if (isWin) {
                                            if (rules.getTurn() == constants.BLACK) {
                                                gameWon(constants.WHITE);
                                                // playerTurn.setText("White wins!");
                                            } else {
                                                gameWon(constants.BLACK);
                                                //  playerTurn.setText("Black wins!");
                                            }

                                        }

                                        if (!isWin) {
                                            if (rules.getTurn() == constants.BLACK) {
                                                rules.changeTurn(constants.WHITE);
                                                resetBlackTimer();
                                                startWhiteTimer();
                                                white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                                                black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));

                                                //  playerTurn.setText("White turn");
                                            } else {
                                                rules.changeTurn(constants.BLACK);
                                                resetWhiteTimer();
                                                startBlackTimer();

                                                white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                                                black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                                                //playerTurn.setText("Black turn");
                                            }
                                        }
                                    } else {
                                        Log.i(TAG, "indexFromList = -99");
                                    }

                                }
                            } catch (Exception e) {
                                Log.i(TAG, "JSON exception : playerLostTurn " + e);
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Log.i(TAG, "JSON exception : overAll " + e);
        }

        //Save all white checkers in a list
        whiteCheckers = new ArrayList<ImageView>();
        whiteCheckers.add((ImageView) findViewById(R.id.whiteChecker1));
        whiteCheckers.add((ImageView) findViewById(R.id.whiteChecker2));
        whiteCheckers.add((ImageView) findViewById(R.id.whiteChecker3));
        whiteCheckers.add((ImageView) findViewById(R.id.whiteChecker4));
        whiteCheckers.add((ImageView) findViewById(R.id.whiteChecker5));
        whiteCheckers.add((ImageView) findViewById(R.id.whiteChecker6));
        whiteCheckers.add((ImageView) findViewById(R.id.whiteChecker7));
        whiteCheckers.add((ImageView) findViewById(R.id.whiteChecker8));
        whiteCheckers.add((ImageView) findViewById(R.id.whiteChecker9));

        //Save all black checkers in a list
        blackCheckers = new ArrayList<ImageView>();
        blackCheckers.add((ImageView) findViewById(R.id.blackChecker1));
        blackCheckers.add((ImageView) findViewById(R.id.blackChecker2));
        blackCheckers.add((ImageView) findViewById(R.id.blackChecker3));
        blackCheckers.add((ImageView) findViewById(R.id.blackChecker4));
        blackCheckers.add((ImageView) findViewById(R.id.blackChecker5));
        blackCheckers.add((ImageView) findViewById(R.id.blackChecker6));
        blackCheckers.add((ImageView) findViewById(R.id.blackChecker7));
        blackCheckers.add((ImageView) findViewById(R.id.blackChecker8));
        blackCheckers.add((ImageView) findViewById(R.id.blackChecker9));

        //Save all areas the checkers can move to in a list
        higBoxAreas = new ArrayList<FrameLayout>();
        higBoxAreas.add((FrameLayout) findViewById(R.id.area1));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area2));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area3));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area4));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area5));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area6));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area7));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area8));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area9));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area10));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area11));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area12));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area13));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area14));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area15));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area16));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area17));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area18));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area19));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area20));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area21));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area22));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area23));
        higBoxAreas.add((FrameLayout) findViewById(R.id.area24));


        socket.on("receivedPlayerMove", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject dataOb = (JSONObject) args[0];
                            Log.i(TAG, "receivedPlayerMove : " + dataOb);
                            if (!dataOb.getBoolean("error")) {
                                if (dataOb.getString("type").trim().equalsIgnoreCase("white")) {

                                    Log.i("12312asasd", whiteCheckers.size() + "");

                                    ImageView v = whiteCheckers.get(dataOb.getInt("index"));
                                    selectChecker(v);
                                } else if (dataOb.getString("type").trim().equalsIgnoreCase("black")) {
                                    ImageView v = blackCheckers.get(dataOb.getInt("index"));
                                    selectChecker(v);
                                } else if (dataOb.getString("type").trim().equalsIgnoreCase("grid")) {
                                    FrameLayout v = higBoxAreas.get(dataOb.getInt("index"));
                                    gridHelperMethod(v);

                                }

                            }
                        } catch (Exception e) {
                            Log.i(TAG, "JSON exception : receivedPlayerMove" + e);
                        }

                    }
                });
            }
        });

        connectionLostWithServer_Receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // show connection lost message
                if (isGameStarted || isGameRequestMade) {
                    showConnectionLostWithServerMessage();
                }
            }
        };

        chat_black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageDialog();
            }
        });

        chat_white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageDialog();
            }
        });

        socket.on("chatMessageReceived", new Emitter.Listener() {
            @Override
            public void call(final Object... objects) {
                try {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                JSONObject jsonObject = (JSONObject) objects[0];
                                Log.i(TAG, "" + jsonObject);

                                if (!jsonObject.getBoolean("error")) {
                                    jsonObject = jsonObject.getJSONObject("data");
                                    messageList.add(new messageModel(
                                            jsonObject.getString("messageText"),
                                            jsonObject.getLong("time"),
                                            jsonObject.getInt("sender"),
                                            jsonObject.getInt("receiver")
                                    ));
                                    if (inboxChatAdapterObject != null) {
                                        inboxChatAdapterObject.notifyDataSetChanged();
                                    }

                                    if (mediaPlayer != null && mediaPlayer.isPlaying())
                                        mediaPlayer.setVolume(0.1f, 0.1f);

                                    MediaPlayer tickPlayer = MediaPlayer.create(online_with_friends.this, R.raw.sms);
                                    tickPlayer.setVolume(1f, 1f);
                                    tickPlayer.start();
                                    tickPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            if (mediaPlayer != null && mediaPlayer.isPlaying())
                                                mediaPlayer.setVolume(1f, 1f);
                                        }
                                    });
                                }

                            } catch (Exception e) {
                                Log.i(TAG, "Exception " + e);
                            }

                        }
                    });


                } catch (Exception e) {
                    Log.i(TAG, "Exception " + e);
                }
            }
        });


    }

    public void messageDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(online_with_friends.this);
        final View customView = layoutInflater.inflate(R.layout.chat_layout, null);
        customView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        AlertDialog.Builder myBox = new AlertDialog.Builder(online_with_friends.this);
        myBox.setView(customView);
        final AlertDialog dialog = myBox.create();

        RecyclerView recyclerView = customView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(inboxChatAdapterObject);
        final EditText messageText = customView.findViewById(R.id.et_message);
        Button bt_send = customView.findViewById(R.id.bt_send);

        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String msg = messageText.getText().toString().trim();
                if (!TextUtils.isEmpty(msg)) {

                    try {

                        final JSONObject jsonObject = new JSONObject();
                        jsonObject.put("messageText", msg);
                        jsonObject.put("receiver", otherPlayerID);
                        jsonObject.put("otherPlayerSocketID", otherPlayerSocketID);
                        jsonObject.put("sender", preferenceManager.getInstance(getApplicationContext()).getUserID());
                        socket.emit("sendChatMessage", jsonObject, new Ack() {
                            @Override
                            public void call(final Object... objects) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject mJson = (JSONObject) objects[0];
                                            if (mJson.getBoolean("error")) {
                                                Toast.makeText(online_with_friends.this, mJson.getString("message"), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(online_with_friends.this, "sent", Toast.LENGTH_SHORT).show();
                                                messageList.add(new messageModel(
                                                        msg, mJson.getLong("time"), preferenceManager.getInstance(getApplicationContext()).getUserID(), otherPlayerID
                                                ));
                                                if (inboxChatAdapterObject != null) {
                                                    inboxChatAdapterObject.notifyDataSetChanged();
                                                }
                                                messageText.setText("");
                                                messageText.setHint("Enter your message");
                                            }
                                        } catch (Exception e) {
                                            Log.i(TAG, "Exception " + e);
                                        }
                                    }
                                });
                            }
                        });
                    } catch (Exception e) {
                        Log.i(TAG, "Exception " + e);
                    }

                }
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    public void renderView() {
        //Add a onClickListener to the white checkers
        for (int i = 0; i < whiteCheckers.size(); i++) {
            ImageView v = whiteCheckers.get(i);
            checkerPositions.put(v, 0);
            final int finalI = i;

            if (whiteCurrentBalls.trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                v.setImageResource(R.drawable.white_checker);
            } else if (whiteCurrentBalls.trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                v.setImageResource(R.drawable.white_gold);
            } else if (whiteCurrentBalls.trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                v.setImageResource(R.drawable.white_diamond);
            }

            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Log.i(TAG, "TURN ------------ " + rules.getTurn());
                    if (isPlayerWhite || canIRemove) {
                        if (rules.getTurn() == constants.WHITE && !isWin) {
                            sendMoveToServer(finalI, "white");
                            //selectChecker(v);
                        }
                    } else {
                        Toast.makeText(online_with_friends.this, white_name.getText() + "'s balls", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        //Add a onClickListener to the black checkers
        for (int i = 0; i < blackCheckers.size(); i++) {
            ImageView v = blackCheckers.get(i);
            checkerPositions.put(v, 0);
            final int finalI = i;

            if (blackCurrentBalls.trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                v.setImageResource(R.drawable.black_checker);
            } else if (blackCurrentBalls.trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                v.setImageResource(R.drawable.black_gold);
            } else if (blackCurrentBalls.trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                v.setImageResource(R.drawable.black_diamond);
            }

            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i(TAG, "TURN ------------ " + rules.getTurn());
                    if (isPlayerBlack || canIRemove) {
                        if (rules.getTurn() == constants.BLACK && !isWin) {
                            sendMoveToServer(finalI, "black");
                            //selectChecker(v);
                        }
                    } else {
                        Toast.makeText(online_with_friends.this, black_name.getText() + "'s balls", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        //Add a clickListener to all the hit box areas

        for (int i = 0; i < higBoxAreas.size(); i++) {
            FrameLayout v = higBoxAreas.get(i);
            final int finalI = i;
            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    sendMoveToServer(finalI, "grid");
                    //gridHelperMethod(v);
                }
            });
        }

        timerForWhite = new CountDownTimer(totalTimerTimeWhite, 1000) {

            public void onTick(long millisUntilFinished) {
                white_timer.setVisibility(View.VISIBLE);
                white_clock.setVisibility(View.VISIBLE);
                white_timer.setText(millisUntilFinished / 1000 + "");
            }

            public void onFinish() {

                if (canIRemove) {
                    removeNextChecker = false;
                    canIRemove = false;
                    game_play_status.setText("");
                    game_play_status.setVisibility(View.INVISIBLE);

                    isWin = rules.isItAWin(rules.getTurn());

                    Log.i(TAG, "isWin " + isWin);

                    if (isWin) {
                        if (rules.getTurn() == constants.BLACK) {
                            gameWon(constants.WHITE);
                            // playerTurn.setText("White wins!");
                        } else {
                            gameWon(constants.BLACK);
                            //  playerTurn.setText("Black wins!");
                        }

                    }

                    if (!isWin) {
                        rules.changeTurn(constants.BLACK);
                        resetWhiteTimer();
                        startBlackTimer();

                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                    }
                } else {

                    if (isPlayerWhite) {
                        int indexFromList = whiteBallIndexFromSideView();
                        if (indexFromList == -99) {
                            indexFromList = whiteBallIndexFromGridView();
                        }

                        if (indexFromList != -99) {
                            try {
                                JSONObject gameOb = gameStartedOBject.getJSONObject("data");
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("white_socketID", gameOb.getString("white_socketID"));
                                jsonObject.put("black_socketID", gameOb.getString("black_socketID"));
                                jsonObject.put("index", indexFromList);
                                jsonObject.put("type", "white");
                                socket.emit("removeBallFromView", jsonObject);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.i(TAG, "JSON exception : removeBallFromView " + e);
                            }
                        } else {
                            Log.i(TAG, "OOpps its -99 position WHITE");
                        }

                    }
                }

                white_clock.setVisibility(View.INVISIBLE);
                white_timer.setVisibility(View.INVISIBLE);
            }
        };

        timerForBlack = new CountDownTimer(totalTimerTimeBlack, 1000) {

            public void onTick(long millisUntilFinished) {
                black_clock.setVisibility(View.VISIBLE);
                black_timer.setVisibility(View.VISIBLE);
                black_timer.setText(millisUntilFinished / 1000 + "");
            }

            public void onFinish() {

                if (canIRemove) {
                    removeNextChecker = false;
                    canIRemove = false;
                    game_play_status.setText("");
                    game_play_status.setVisibility(View.INVISIBLE);

                    isWin = rules.isItAWin(rules.getTurn());

                    Log.i(TAG, "isWin " + isWin);

                    if (isWin) {
                        if (rules.getTurn() == constants.BLACK) {
                            gameWon(constants.WHITE);
                            // playerTurn.setText("White wins!");
                        } else {
                            gameWon(constants.BLACK);
                            //  playerTurn.setText("Black wins!");
                        }

                    }

                    if (!isWin) {
                        rules.changeTurn(constants.WHITE);
                        resetBlackTimer();
                        startWhiteTimer();
                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                    }
                } else {
                    if (isPlayerBlack) {
                        int indexFromList = blackBallIndexFromSideView();
                        if (indexFromList == -99) {
                            indexFromList = blackBallIndexFromGridView();
                        }

                        if (indexFromList != -99) {
                            try {
                                JSONObject gameOb = gameStartedOBject.getJSONObject("data");
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("white_socketID", gameOb.getString("white_socketID"));
                                jsonObject.put("black_socketID", gameOb.getString("black_socketID"));
                                jsonObject.put("index", indexFromList);
                                jsonObject.put("type", "black");
                                socket.emit("removeBallFromView", jsonObject);
                            } catch (Exception e) {
                                Log.i(TAG, "JSON exception : removeBallFromView " + e);
                            }
                        } else {
                            Log.i(TAG, "OOpps its -99 position BLACK");
                        }
                    }
                }

                black_clock.setVisibility(View.INVISIBLE);
                black_timer.setVisibility(View.INVISIBLE);
            }
        };
    }

    public void gameConnectionTimeOut() {
        int milisInAMinute = 15 * 1000;

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // check if game is not started, this means we are not connected with server
                        if (trigerGameConnectionTimeOut) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            socket.off("requestDefinedGame");
                            socket.off("gameStarted");

                            AlertDialog.Builder builder = new AlertDialog.Builder(online_with_friends.this);
                            builder.setMessage("Oho!\n\nIt seems that we can not connect to Qath's Server.\nPlease try again later.");
                            builder.setCancelable(false);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
            }
        }, milisInAMinute);

    }

    public int blackBallIndexFromSideView() {
        for (Map.Entry<ImageView, Integer> entry : checkerPositions.entrySet()) {
            ImageView imageView = entry.getKey();
            if (entry.getValue() == 0) {
                if (blackCheckers.contains(imageView)) {
                    int indexFromList = blackCheckers.indexOf(imageView);
                    return indexFromList;
                }
            }
        }
        return -99;
    }


    public int whiteBallIndexFromSideView() {
        for (Map.Entry<ImageView, Integer> entry : checkerPositions.entrySet()) {
            ImageView imageView = entry.getKey();
            Log.i(TAG, "ENTRY = " + entry.getValue());
            if (entry.getValue() == 0) {
                if (whiteCheckers.contains(imageView)) {
                    int indexFromList = whiteCheckers.indexOf(imageView);
                    return indexFromList;
                }
            }
        }
        return -99;
    }


    public int whiteBallIndexFromGridView() {
        for (Map.Entry<ImageView, Integer> entry : checkerPositions.entrySet()) {
            ImageView imageView = entry.getKey();
            if (entry.getValue() != 0 && entry.getValue() != -99) {
                if (whiteCheckers.contains(imageView)) {
                    int from = whiteCheckers.indexOf(imageView);
                    Log.i("aosome", from + " and " + checkerPositions.get(imageView));
                    if (rules.checkIfBallInQath(checkerPositions.get(imageView))) {
                        if (rules.playerEveryBallIsInQath(constants.WHITE)) {
                            return from;
                        }
                    } else {
                        return from;
                    }
                }
            }
        }
        return -99;
    }

    public int blackBallIndexFromGridView() {
        for (Map.Entry<ImageView, Integer> entry : checkerPositions.entrySet()) {
            ImageView imageView = entry.getKey();
            if (entry.getValue() != 0 && entry.getValue() != -99) {
                if (blackCheckers.contains(imageView)) {
                    int from = blackCheckers.indexOf(imageView);
                    Log.i("aosome", from + " and " + checkerPositions.get(imageView));
                    if (rules.checkIfBallInQath(checkerPositions.get(imageView))) {
                        if (rules.playerEveryBallIsInQath(constants.BLACK)) {
                            return from;
                        }
                    } else {
                        return from;
                    }
                }
            }
        }
        return -99;
    }


    public void startWhiteTimer() {
        timerForWhite.start();
    }

    public void startBlackTimer() {
        timerForBlack.start();
    }

    public void resetWhiteTimer() {
        if (timerForWhite != null) {
            timerForWhite.cancel();
        } else {
            Log.i(TAG, "timerForWhite is null");
        }
        totalTimerTimeWhite = constants.GAME_TIME;
        white_clock.setVisibility(View.INVISIBLE);
        white_timer.setVisibility(View.INVISIBLE);
    }

    public void resetBlackTimer() {
        if (timerForBlack != null) {
            timerForBlack.cancel();
        } else {
            Log.i(TAG, "timerForBlack is null");
        }
        totalTimerTimeBlack = constants.GAME_TIME;
        black_clock.setVisibility(View.INVISIBLE);
        black_timer.setVisibility(View.INVISIBLE);
    }

    public void gridHelperMethod(View v) {
        //If we have a selected checker, try to move it
        if (hasSelectedChecker) {
            Log.i(TAG, "Area clicked");
            int currentTurn = rules.getTurn();
            areaToMoveTo = (FrameLayout) v;

            //What areas are we moving from and to?
            int to = Integer.parseInt((String) areaToMoveTo.getContentDescription());
            int from = checkerPositions.get(selectedChecker);
            Log.i(TAG, "FROM = " + from);
            //Try to move the checker
            if (rules.validMove(from, to)) { // This line will change turn
                Log.i(TAG, "VAlid move");
                //Update the UI
                unMarkAllFields();
                moveChecker(currentTurn);

                checkerPositions.put((ImageView) selectedChecker, Integer.parseInt((String) areaToMoveTo.getContentDescription()));

                Log.i("123132", checkerPositions.get(selectedChecker) + "");

                //Did the row create a row of 3?

                if (currentTurn == constants.BLACK) {
                    removeNextChecker = rules.canRemove(to, constants.WHITE);
                } else {
                    removeNextChecker = rules.canRemove(to, constants.BLACK);
                }

                selectedChecker.setAlpha(1.0f);

                //The selected checker is not selected anymore
                hasSelectedChecker = false;
                selectedChecker = null;
                //  checkerPositions.put((ImageView) selectedChecker, Integer.parseInt((String) areaToMoveTo.getContentDescription()));
                //  Log.i("123132", checkerPositions.get(selectedChecker) + "");
                //Did the move create a row of 3?
                //   removeNextChecker = rules.canRemove(to);

                //Update the turn text
                if (removeNextChecker) {
                    if (currentTurn == constants.BLACK) {
                        // showMessage("You can remove one white ball from grid now.");
                        resetBlackTimer();
                        game_play_status.setText(black_name.getText() + ", you can remove one white ball from grid.");
                        canIRemove = true;
                        game_play_status.setVisibility(View.VISIBLE);
                        // playerTurn.setText("Remove White");
                    } else {
                        //showMessage("You can remove one black ball from grid now.");
                        canIRemove = true;
                        resetWhiteTimer();
                        game_play_status.setText(white_name.getText() + " you can remove one black ball from grid.");
                        game_play_status.setVisibility(View.VISIBLE);

                        // playerTurn.setText("Remove Black");
                    }
                } else {
                    if (currentTurn == constants.BLACK) {

                        resetBlackTimer();
                        startWhiteTimer();
                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));

                        //  playerTurn.setText("White turn");
                    } else {

                        resetWhiteTimer();
                        startBlackTimer();

                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                        //playerTurn.setText("Black turn");
                    }
                }
                //Did someone win?
                isWin = rules.isItAWin(rules.getTurn());
                if (isWin) {
                    if (rules.getTurn() == constants.BLACK) {
                        gameWon(constants.WHITE);
                        // playerTurn.setText("White wins!");
                    } else {
                        gameWon(constants.BLACK);
                        //  playerTurn.setText("Black wins!");
                    }

                }
            }
        }
    }

    public void sendMoveToServer(int ballIndex, String ballType) {
        // sending player move to server for sending to other user view
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("index", ballIndex);
            jsonObject.put("type", ballType);
            jsonObject.put("emitTo", otherPlayerSocketID);
            jsonObject.put("myID", mySocketID);

            socket.emit("sendMoveToPlayer", jsonObject, new Ack() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject dataOb = (JSONObject) args[0];
                                Log.i(TAG, "sendMoveToPlayer : " + dataOb);
                            } catch (Exception e) {
                                Log.i(TAG, "JSON exception : sendMoveToPlayer " + e);
                            }
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.i(TAG, "JSON exception : sendMoveToPlayer " + e);
        }
    }

    /**
     * Move the checker from the current position to a new position
     *
     * @param turn Constant.WHITE or Constant.BLACK according to whos turn it is
     */
    public void moveChecker(int turn) {

        ImageView animChecker = null;
        //Get the position of the checker that will move and the area it will move to
        final int[] locationChecker = {0, 0};
        final int[] locationArea = {0, 0};
        selectedChecker.getLocationOnScreen(locationChecker);
        areaToMoveTo.getLocationOnScreen(locationArea);
        Log.i(TAG, "move from x: " + locationChecker[0] + " y: " + locationChecker[1]);
        Log.i(TAG, "move to x: " + locationArea[0] + " y: " + locationArea[1]);

        ViewGroup parent = ((ViewGroup) selectedChecker.getParent());
        final int index = parent.indexOfChild(selectedChecker);

        //Create a ghost checker which will be animated while the real one just moves.
        if (turn == constants.WHITE) {

            whiteMoveCounter++;

            whiteIndexes.add(index + "");
            animChecker = (ImageView) getLayoutInflater().inflate(R.layout.anim_white_checker, parent, false);

            if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                animChecker.setImageResource(R.drawable.white_checker);
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                animChecker.setImageResource(R.drawable.white_gold);
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                animChecker.setImageResource(R.drawable.white_diamond);
            }

        } else {

            blackMoveCounter++;

            blackIndexes.add(index + "");
            animChecker = (ImageView) getLayoutInflater().inflate(R.layout.anim_black_checker, parent, false);

            if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                animChecker.setImageResource(R.drawable.black_checker);
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                animChecker.setImageResource(R.drawable.black_gold);
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                animChecker.setImageResource(R.drawable.black_diamond);
            }
        }


        //If the checker is in the side board, we need to update the side board as well
        if (parent != findViewById(R.id.board)) {
            //Remove the real checker and add the ghost where the real one was
            parent.removeView(selectedChecker);
            parent.addView(animChecker, index);
            //Move the real one to the side board

            ((ViewGroup) findViewById(R.id.board)).addView(selectedChecker);

        } else {

            //Add the ghost checker at the real ones position
            parent.addView(animChecker);
            animChecker.setLayoutParams(selectedChecker.getLayoutParams());
        }

        //Make the real checker invisible and move it
        selectedChecker.setLayoutParams(areaToMoveTo.getLayoutParams());
        selectedChecker.setVisibility(View.INVISIBLE);

        //final copies to be used in the animation thread

        final ImageView tmpAnimChecker = animChecker;
        final ImageView tmpSelectedChecker = selectedChecker;

        //Prepare animation with x and y movement
        TranslateAnimation tAnimation = new TranslateAnimation(0, locationArea[0] - locationChecker[0], 0, locationArea[1] - locationChecker[1]);
        tAnimation.setFillEnabled(true);
        tAnimation.setFillAfter(true);
        tAnimation.setDuration(800);

        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
        MediaPlayer tickPlayer = MediaPlayer.create(this, R.raw.tick);
        tickPlayer.setVolume(1f, 1f);
        tickPlayer.start();
        tickPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.setVolume(1f, 1f);
            }
        });

        tAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup parent = ((ViewGroup) tmpAnimChecker.getParent());
                //Fix the side board so its children stays in position
                if (tmpAnimChecker.getParent() != findViewById(R.id.board)) {
                    //Add a placeholder frame layout to stop the other checkers from jmping towards the middle.
                    FrameLayout placeholder = (FrameLayout) getLayoutInflater().inflate(R.layout.layout_placeholder, parent, false);
                    parent.addView(placeholder, index);
                }

                //Remove the ghost and make the real checker visible again.
                parent.removeView(tmpAnimChecker);
                tmpSelectedChecker.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });

        tmpAnimChecker.startAnimation(tAnimation);
    }

    /**
     * Lets the player select a checker to remove or move
     *
     * @param v The checker which was clicked on.
     */
    public void selectChecker(View v) {
        //Is it a remove click=
        if (removeNextChecker) {
            //Is it a valid remove click?
            if (rules.getTurn() == constants.BLACK && rules.remove(checkerPositions.get(v), constants.BLACK)) {
                //Unamrk all options and remove the selected checker

                if (mediaPlayer != null && mediaPlayer.isPlaying())
                    mediaPlayer.setVolume(0.1f, 0.1f);

                MediaPlayer tickPlayer = MediaPlayer.create(this, R.raw.kill_ball);
                tickPlayer.setVolume(1f, 1f);
                tickPlayer.start();
                tickPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mediaPlayer != null && mediaPlayer.isPlaying())
                            mediaPlayer.setVolume(1f, 1f);
                    }
                });

                unMarkAllFields();
                // blackCheckers.remove(v);
                removeNextChecker = false;
                ViewGroup parent = ((ViewGroup) v.getParent());
                parent.removeView(v);
                //  playerTurn.setText("Black turn");

                game_play_status.setText("");
                game_play_status.setVisibility(View.INVISIBLE);
                canIRemove = false;

                resetWhiteTimer();
                startBlackTimer();

                white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));

                //Did someone win?
                isWin = rules.isItAWin(constants.BLACK);
                if (isWin) {
                    gameWon(constants.WHITE);
                    // playerTurn.setText("White wins!");
                }
            } else if (rules.getTurn() == constants.WHITE && rules.remove(checkerPositions.get(v), constants.WHITE)) {
                //Unmark all options and remove the selected checker

                if (mediaPlayer != null && mediaPlayer.isPlaying())
                    mediaPlayer.setVolume(0.1f, 0.1f);

                MediaPlayer tickPlayer = MediaPlayer.create(this, R.raw.kill_ball);
                tickPlayer.setVolume(1f, 1f);
                tickPlayer.start();
                tickPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mediaPlayer != null && mediaPlayer.isPlaying())
                            mediaPlayer.setVolume(1f, 1f);
                    }
                });


                unMarkAllFields();
                // whiteCheckers.remove(v);
                removeNextChecker = false;
                ViewGroup parent = ((ViewGroup) v.getParent());
                parent.removeView(v);
                //   playerTurn.setText("White turn");

                game_play_status.setText("");
                game_play_status.setVisibility(View.INVISIBLE);
                canIRemove = false;

                resetBlackTimer();
                startWhiteTimer();

                white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));

                //Did someone win?
                isWin = rules.isItAWin(constants.WHITE);
                if (isWin) {
                    gameWon(constants.BLACK);
                    // playerTurn.setText("Black wins!");
                }
            }

        }
        //Try to select the checker for a move
        else if (!(checkerPositions.get(v) != 0 && checkerPositions.containsValue(0)) || (checkerPositions.get(v) == 0)) {
            //If a checker is already selected, unselect it
            if (selectedChecker != null) {
                selectedChecker.setAlpha(1.0f);
            }
            //If its the selected checker which is clicked, no checker is selected.

            if (selectedChecker == v) {
                hasSelectedChecker = false;
                selectedChecker = null;
                unMarkAllFields();
                return;
            }
            //Select a checker and mark available moves.
            markAvailableMoveFields(checkerPositions.get(v));
            hasSelectedChecker = true;
            selectedChecker = (ImageView) v;
            selectedChecker.setAlpha(0.5f);
        }
    }

    /**
     * Mark all available moves that can be done.
     *
     * @param from The position of the checker which wants to move
     */
    public void markAvailableMoveFields(int from) {
        unMarkAllFields();
        for (int i = 0; i < 24; i++) {
            if (rules.isValidMove(from, i + 1)) {
                higBoxAreas.get(i).setBackgroundResource(R.drawable.valid_move);
            }
        }
    }

    /**
     * Unmark all fields.
     */
    public void unMarkAllFields() {
        for (FrameLayout f : higBoxAreas) {
            f.setBackgroundResource(0);
        }
    }

    public void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(online_with_friends.this);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void gameWon(int color) {
        Log.i(TAG, "GAME WON BY " + color);

        resetBlackTimer();
        resetWhiteTimer();

        try {
            JSONObject gameOb = gameStartedOBject.getJSONObject("data");

            Integer wonPlayerID = null;
            Integer losePlayerID = null;
            String wonPlayerName = null;
            if (color == constants.BLACK) {
                losePlayerID = gameOb.getInt("white");
                wonPlayerID = gameOb.getInt("black");
                wonPlayerName = gameOb.getJSONObject("blackInfo").getString("name");
            } else {
                wonPlayerID = gameOb.getInt("white");
                losePlayerID = gameOb.getInt("black");
                wonPlayerName = gameOb.getJSONObject("whiteInfo").getString("name");
            }

            if (preferenceManager.getInstance(getApplicationContext()).getUserID() == wonPlayerID) {

                if (blackMoveCounter != 0 && whiteMoveCounter != 0) {
                    if (preferenceManager.getInstance(getApplicationContext()).getUserID() == constants.BLACK) {
                        if (blackMoveCounter > preferenceManager.getInstance(getApplicationContext()).getBestGameWon()) {
                            preferenceManager.getInstance(getApplicationContext()).setBestGameWon(blackMoveCounter);
                        }
                    } else {
                        if (whiteMoveCounter > preferenceManager.getInstance(getApplicationContext()).getBestGameWon()) {
                            preferenceManager.getInstance(getApplicationContext()).setBestGameWon(whiteMoveCounter);
                        }
                    }
                }

                preferenceManager.getInstance(getApplicationContext()).setGameWonCount(preferenceManager.getInstance(getApplicationContext()).getGameWonCount() + 1);
                preferenceManager.getInstance(getApplicationContext()).setUserCoins(preferenceManager.getInstance(getApplicationContext()).getUserCoins() + constants.GAME_CHARGES);
            } else {
                preferenceManager.getInstance(getApplicationContext()).setGameLostCount(preferenceManager.getInstance(getApplicationContext()).getGameLostCount() + 1);
                preferenceManager.getInstance(getApplicationContext()).setUserCoins(preferenceManager.getInstance(getApplicationContext()).getUserCoins() - constants.GAME_CHARGES);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("losePlayerID", losePlayerID);
            jsonObject.put("wonPlayerID", wonPlayerID);
            jsonObject.put("wonPlayerName", wonPlayerName);
            jsonObject.put("gameID", gameOb.getString("gameID"));
            jsonObject.put("white_socketID", gameOb.getString("white_socketID"));
            jsonObject.put("black_socketID", gameOb.getString("black_socketID"));

            jsonObject.put("blackMoveCounter", blackMoveCounter);
            jsonObject.put("whiteMoveCounter", whiteMoveCounter);


            socket.emit("gameWon", jsonObject);

        } catch (Exception e) {
            Log.i(TAG, "Exception : " + e);
        }
    }

    public void showGameWinMessage(String message) {

        resetBlackTimer();
        resetWhiteTimer();


        AlertDialog.Builder builder = new AlertDialog.Builder(online_with_friends.this);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (timer != null) {
                    timer.cancel();
                }
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (isGameRequestMade || isGameStarted) {
            String buttonText = "OK";
            AlertDialog.Builder builder = new AlertDialog.Builder(online_with_friends.this);
            if (isGameStarted) {
                builder.setMessage("Oh.\nYou want to quit this game?");
                buttonText = "YES QUIT GAME";
            } else if (isGameRequestMade) {
                builder.setMessage("Oh.\nYour request for game is made.\nCancel Game Request?");
                buttonText = "YES CANCEL REQUEST";
            }

            builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {

                    if (isGameStarted) {
                        socket.emit("quitGame", new Ack() {
                            @Override
                            public void call(final Object... args) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jsonObject = (JSONObject) args[0];
                                            if (!jsonObject.getBoolean("error")) {
                                                if (timer != null) {
                                                    timer.cancel();
                                                }
                                                dialog.dismiss();

                                                preferenceManager.getInstance(getApplicationContext()).setGameLostCount(preferenceManager.getInstance(getApplicationContext()).getGameLostCount() + 1);
                                                preferenceManager.getInstance(getApplicationContext()).setUserCoins(preferenceManager.getInstance(getApplicationContext()).getUserCoins() - constants.GAME_CHARGES);

                                                finish();
                                            }
                                        } catch (Exception e) {
                                            Log.i(TAG, "Exception : " + e);
                                        }
                                    }
                                });
                            }
                        });
                    } else if (isGameRequestMade)

                    {
                        socket.emit("cancelDefinedGameRequest", new Ack() {
                            @Override
                            public void call(final Object... args) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jsonObject = (JSONObject) args[0];
                                            if (!jsonObject.getBoolean("error")) {
                                                if (timer != null) {
                                                    timer.cancel();
                                                }
                                                dialog.dismiss();
                                                finish();
                                            }
                                        } catch (Exception e) {
                                            Log.i(TAG, "Exception : " + e);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });
            builder.setCancelable(false);
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else

        {
            super.onBackPressed();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (requestAudioFocus()) {
            //foucs gain
            if (preferenceManager.getInstance(getApplicationContext()).isGameSoundEnabled()) {
                playMedia();
            }
        }
        if (socketConnectionHandler.isConnectionLost && (isGameStarted || isGameRequestMade)) {
            // show connection lost message
            showConnectionLostWithServerMessage();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((connectionLostWithServer_Receiver), new IntentFilter(constants.BROADCAST_CONNECTION_LOST_WITH_SERVER));
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseMedia();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(connectionLostWithServer_Receiver);
    }

    public void showConnectionLostWithServerMessage() {
        socket.off("requestGame");
        socket.off("gameStarted");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        resetBlackTimer();
        resetWhiteTimer();

        preferenceManager.getInstance(getApplicationContext()).setUserCoins(preferenceManager.getInstance(getApplicationContext()).getUserCoins() - 500);

        AlertDialog.Builder builder = new AlertDialog.Builder(online_with_friends.this);
        builder.setMessage("Oho!!\nWe have lost connection with server.\nUnfortunately game would not be carry ahead.");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (timer != null) {
                    timer.cancel();
                }
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void viewUserProfile(String user) {

        LayoutInflater layoutInflater = LayoutInflater.from(online_with_friends.this);
        final View customView = layoutInflater.inflate(R.layout.add_people_custom, null);
        AlertDialog.Builder myBox = new AlertDialog.Builder(online_with_friends.this);
        myBox.setView(customView);
        final AlertDialog dialog = myBox.create();

        final LinearLayout requestbutton = (LinearLayout) customView.findViewById(R.id.requestbutton);
        final ImageView cancel = (ImageView) customView.findViewById(R.id.txtclose);
        final TextView personName = (TextView) customView.findViewById(R.id.suspectname);

        final ImageView userProfile = (ImageView) customView.findViewById(R.id.userProfile);
        final TextView userName = (TextView) customView.findViewById(R.id.userName);
        final TextView userCoins = (TextView) customView.findViewById(R.id.userCoins);
        final TextView gameWon = (TextView) customView.findViewById(R.id.gameWon);
        final TextView lostGames = (TextView) customView.findViewById(R.id.lostGames);

        final TextView rank = (TextView) customView.findViewById(R.id.rank);
        final TextView best_game_won = (TextView) customView.findViewById(R.id.best_game_won);


        try {
            JSONObject dataObject = gameStartedOBject.getJSONObject("data").getJSONObject(user);
            String name = dataObject.getString("name");
            String firstName = "";
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) == 32) {
                    break;
                } else {
                    firstName += name.charAt(i);
                }
            }

            rank.setText("Rank : " + dataObject.getString("rank"));

            if (dataObject.getInt("best_game_won") != -99) {
                String text = dataObject.getInt("best_game_won") == 1 ? "Best Game won in " + dataObject.getInt("best_game_won") + " move." : "Best Game won in " + dataObject.getInt("best_game_won") + " moves";
                best_game_won.setText(text);
            }

            if (user.trim().equalsIgnoreCase("whiteInfo") && isPlayerWhite) {
                requestbutton.setVisibility(View.GONE);
            } else if (user.trim().equalsIgnoreCase("blackInfo") && isPlayerBlack) {
                requestbutton.setVisibility(View.GONE);
            } else {

                if (friendRequestStatus.equalsIgnoreCase("not_friends")) {
                    personName.setText("Send " + firstName + " Friend Request");
                } else if (friendRequestStatus.equalsIgnoreCase("pending")) {
                    if (requestSender == preferenceManager.getInstance(getApplicationContext()).getUserID()) {
                        personName.setText("Cancel Friend Request");
                    } else {
                        personName.setText("Accept Friend Request");
                    }
                } else if (friendRequestStatus.equalsIgnoreCase("friends")) {
                    personName.setText("Remove from friend list");
                }


                final String finalFirstName = firstName;
                requestbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {

                            if (friendRequestStatus.equalsIgnoreCase("not_friends")) {
                                JSONObject payload = new JSONObject();
                                payload.put("sender", preferenceManager.getInstance(getApplicationContext()).getUserID());
                                payload.put("receiver", otherPlayerID);
                                payload.put("text", "Friend Request is received");

                                socket.emit("sendFriendRequest", payload, new Ack() {
                                    @Override
                                    public void call(final Object... args) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                try {
                                                    JSONObject jsonObject = (JSONObject) args[0];
                                                    if (!jsonObject.getBoolean("error")) {
                                                        friendRequestID = jsonObject.getInt("friendRequestID");
                                                        shouldRemoveRequest = true;
                                                        Toast.makeText(online_with_friends.this, "Friend Request sent successfully", Toast.LENGTH_SHORT).show();
                                                        personName.setText("Cancel Friend Request");
                                                    } else {
                                                        Toast.makeText(online_with_friends.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                                    }
                                                } catch (Exception e) {
                                                    Log.i(TAG, "Exception : " + e);
                                                }
                                            }
                                        });

                                    }
                                });
                            } else if (friendRequestStatus.equalsIgnoreCase("pending")) {
                                if (requestSender == preferenceManager.getInstance(getApplicationContext()).getUserID()) {
                                    JSONObject payload = new JSONObject();
                                    payload.put("sender", preferenceManager.getInstance(getApplicationContext()).getUserID());
                                    payload.put("receiver", otherPlayerID);
                                    payload.put("ID", friendRequestID);


                                    socket.emit("cancelFriendRequest", payload, new Ack() {
                                        @Override
                                        public void call(final Object... args) {

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    try {
                                                        JSONObject jsonObject = (JSONObject) args[0];
                                                        if (!jsonObject.getBoolean("error")) {


                                                            Toast.makeText(online_with_friends.this, "Friend Request cancelled successfully", Toast.LENGTH_SHORT).show();

                                                            personName.setText("Send " + finalFirstName + " Friend Request");
                                                            friendRequestID = -99;
                                                            shouldRemoveRequest = false;
                                                            friendRequestStatus = "not_friends";

                                                        } else {
                                                            Toast.makeText(online_with_friends.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (Exception e) {
                                                        Log.i(TAG, "Exception : " + e);
                                                    }
                                                }
                                            });

                                        }
                                    });
                                } else {
                                    // Accept Friend Request
                                    JSONObject payload = new JSONObject();
                                    payload.put("sender", preferenceManager.getInstance(getApplicationContext()).getUserID());
                                    payload.put("receiver", otherPlayerID);
                                    payload.put("ID", friendRequestID);


                                    socket.emit("acceptFriendRequest", payload, new Ack() {
                                        @Override
                                        public void call(final Object... args) {

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    try {
                                                        JSONObject jsonObject = (JSONObject) args[0];
                                                        if (!jsonObject.getBoolean("error")) {


                                                            Toast.makeText(online_with_friends.this, "Friend Request accepted successfully", Toast.LENGTH_SHORT).show();

                                                            personName.setText("Remove from friend list");
                                                            friendRequestStatus = "friends";

                                                        } else {
                                                            Toast.makeText(online_with_friends.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (Exception e) {
                                                        Log.i(TAG, "Exception : " + e);
                                                    }
                                                }
                                            });

                                        }
                                    });
                                }
                            } else if (friendRequestStatus.equalsIgnoreCase("friends")) {
                                JSONObject payload = new JSONObject();
                                payload.put("sender", preferenceManager.getInstance(getApplicationContext()).getUserID());
                                payload.put("receiver", otherPlayerID);
                                payload.put("ID", friendRequestID);


                                socket.emit("cancelFriendRequest", payload, new Ack() {
                                    @Override
                                    public void call(final Object... args) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                try {
                                                    JSONObject jsonObject = (JSONObject) args[0];
                                                    if (!jsonObject.getBoolean("error")) {


                                                        Toast.makeText(online_with_friends.this, "Removed from friend list successfully", Toast.LENGTH_SHORT).show();

                                                        personName.setText("Send " + finalFirstName + " Friend Request");
                                                        friendRequestID = -99;
                                                        shouldRemoveRequest = false;
                                                        friendRequestStatus = "not_friends";

                                                    } else {
                                                        Toast.makeText(online_with_friends.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                                    }
                                                } catch (Exception e) {
                                                    Log.i(TAG, "Exception : " + e);
                                                }
                                            }
                                        });

                                    }
                                });
                            }

                        } catch (Exception e) {
                            Log.i(TAG, "Exception : " + e);
                        }

                    }
                });

            }

            JSONObject profileOb = dataObject.getJSONObject("profile");

            userName.setText(name);
            userCoins.setText(dataObject.getInt("coins") + "");
            gameWon.setText(dataObject.getInt("gameWon") + "");
            lostGames.setText(dataObject.getInt("gameLost") + "");

            if (profileOb.getString("provider").trim().equals("Qath")) {
                // use qath api to get image
                if (!profileOb.getString("image").trim().equals("none")) {
                    Picasso.get().
                            load(URLs.UPLOADED_FILES(profileOb.getString("image").trim()))
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .into(userProfile);
                }
            } else {
                if (!profileOb.getString("image").trim().equals("none")) {
                    Picasso.get().
                            load(profileOb.getString("image").trim())
                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .into(userProfile);
                }
            }


        } catch (Exception e) {
            Log.i(TAG, "Exception : " + e);
        }


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.sound);
            //Set up MediaPlayer event listeners
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setLooping(true); // Set looping
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Exception : " + e);
        }
    }

    private void playMedia() {
        if (mediaPlayer == null) {
            initMediaPlayer();
            return;
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                                // controlMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            // buildNotification(PlaybackStatus.PAUSED);
        }
    };

    @Override
    public void onAudioFocusChange(int focusChange) {
        //Invoked when the audio focus of the system is updated.
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) resumeMedia();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer != null && mediaPlayer.isPlaying()) pauseMedia();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer != null && mediaPlayer.isPlaying())
                    mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        resetWhiteTimer();
        resetBlackTimer();

        socket.off("requestGame");
        socket.off("gameStarted");
        socket.off("gameFinished");
        socket.off("gameWonByPlayer");
        socket.off("playerLostTurn");
        socket.off("receivedPlayerMove");

        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
    }


}
