package comsats.fyp.game.abbottabad.GamePlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.Rules;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;

public class offline_player_vs_bot extends AppCompatActivity implements MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener {
    private final String TAG = "player_vs_bot";

    private final String NEW_GAME = "NEW_GAME";

    Rules rules;

    private ArrayList<ImageView> whiteCheckers;
    private ArrayList<ImageView> blackCheckers;
    private ArrayList<FrameLayout> higBoxAreas;
    private ImageView selectedChecker;
    private FrameLayout areaToMoveTo;
    private HashMap<ImageView, Integer> checkerPositions;

    private boolean hasSelectedChecker = false;
    private boolean removeNextChecker = false;
    private boolean isWin = false;

    private ArrayList<String> whiteIndexes = new ArrayList<>();
    private ArrayList<String> blackIndexes = new ArrayList<>();

    LinearLayout white_box_layout, black_box_layout;
    TextView game_play_status;

    TextView white_timer, black_timer;
    ImageView white_clock, black_clock;
    CountDownTimer timerForWhite = null, timerForBlack;
    Integer timeForBlackTemp = 3000;
    Integer totalTimerTimeBlack = timeForBlackTemp;
    Integer totalTimerTimeWhite;

    int BOT, PLAYER;
    int previousBallPlayed = -99;
    int previousReturn = -99;
    int previousBallMoved = -99;

    private MediaPlayer mediaPlayer;
    private int resumePosition = 0;
    private AudioManager audioManager;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    Boolean backIsNotPressed = true;
    int whiteMoveCounter = 0, blackMoveCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Creating activity");
        setContentView(R.layout.game_play_offline_bot);

        if (requestAudioFocus()) {
            //foucs gain
            if (preferenceManager.getInstance(getApplicationContext()).isGameSoundEnabled()) {
                initMediaPlayer();
            }
        }

        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();


        if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.SILVER_BALL)) {
            totalTimerTimeWhite = constants.SILVER_GAME_TIME;
        } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.GOLD_BALL)) {
            totalTimerTimeWhite = constants.GOLD_GAME_TIME;
        } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
            totalTimerTimeWhite = constants.DIAMOND_GAME_TIME;
        }

        getSupportActionBar().hide();

        rules = new Rules();

        selectedChecker = null;
        areaToMoveTo = null;
        checkerPositions = new HashMap<>();

        game_play_status = findViewById(R.id.game_play_status);

        black_timer = findViewById(R.id.black_timer);
        black_clock = findViewById(R.id.black_clock);

        white_timer = findViewById(R.id.white_timer);
        white_clock = findViewById(R.id.white_clock);

        white_box_layout = findViewById(R.id.white_box_layout);
        black_box_layout = findViewById(R.id.black_box_layout);

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

        //Add a onClickListener to the white checkers
        for (ImageView v : whiteCheckers) {
            checkerPositions.put(v, 0);

            if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                v.setImageResource(R.drawable.white_checker);
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                v.setImageResource(R.drawable.white_gold);
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                v.setImageResource(R.drawable.white_diamond);
            }

            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {


                    if (PLAYER == constants.WHITE) {
                        if (rules.getTurn() == constants.WHITE && !isWin && !removeNextChecker) {
                            selectChecker(v);
                        } else {
                            Toast.makeText(offline_player_vs_bot.this, "Black's turn", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(offline_player_vs_bot.this, "opponent's balls", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        //Add a onClickListener to the black checkers
        for (ImageView v : blackCheckers) {
            checkerPositions.put(v, 0);

            if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.SILVER_BALL)) {
                v.setImageResource(R.drawable.black_checker);
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.GOLD_BALL)) {
                v.setImageResource(R.drawable.black_gold);
            } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
                v.setImageResource(R.drawable.black_diamond);
            }

            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {


                    Log.i(TAG, "removeChecker is  " + removeNextChecker + " :: " + rules.getTurn());

                    if (removeNextChecker) {
                        if (rules.getTurn() == constants.WHITE && !isWin) {
                            selectChecker(v);
                        } else {
                            Log.i(TAG, "not white turn");
                        }
                    } else {
                        Log.i(TAG, "removeChecker is false ");
                    }

                }
            });
        }

        //Add a clickListener to all the hit box areas
        for (FrameLayout v : higBoxAreas) {
            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    gridHelperMethod(v);
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

                String type = "";

                int indexFromList = whiteBallIndexFromSideView();
                type = "SideView";
                if (indexFromList == -99) {
                    indexFromList = whiteBallIndexFromGridView();
                    type = "GridView";
                }

                if (indexFromList != -99) {
                    removeBallFromView("white", indexFromList, type);
                } else {
                    Log.i(TAG, "OOpps its -99 position WHITE");
                }

                white_clock.setVisibility(View.INVISIBLE);
                white_timer.setVisibility(View.INVISIBLE);
                game_play_status.setText("");
            }
        };


        startWhiteTimer();
        BOT = constants.BLACK;
        PLAYER = constants.WHITE;

    }//onCreate ends here

    public void gridHelperMethod(View v) {
        //If we have a selected checker, try to move it
        if (hasSelectedChecker) {
            Log.i(TAG, "Area clicked");
            int currentTurn = rules.getTurn();
            areaToMoveTo = (FrameLayout) v;

            //What areas are we moving from and to?
            int to = Integer.parseInt((String) areaToMoveTo.getContentDescription());
            int from = checkerPositions.get(selectedChecker);

            Log.i(TAG, "From = " + from + " -- to = " + to);

            //Try to move the checker
            if (rules.validMove(from, to)) { // This line will change turn
                Log.i(TAG, "VAlid move");
                //Update the UI
                unMarkAllFields();
                moveChecker(currentTurn);

                checkerPositions.put((ImageView) selectedChecker, Integer.parseInt((String) areaToMoveTo.getContentDescription()));

                Log.i("123132", checkerPositions.get(selectedChecker) + "");

                //Did the row create a row of 3?
                //  removeNextChecker = rules.canRemove(to);

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
                        //    playerTurn.setText("Remove White");
                        game_play_status.setText("Remove White");

                        rules.changeTurn(constants.BLACK);

                        resetBlackTimer();
                        resetWhiteTimer();
                        startBlackTimer();

                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                        removeNextChecker = true;

                    } else {

                        rules.changeTurn(constants.WHITE);

                        resetBlackTimer();
                        resetWhiteTimer();
                        startWhiteTimer();


                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));

                        game_play_status.setText("Remove Black");
                        //  playerTurn.setText("Remove Black");
                        removeNextChecker = true;
                    }
                } else {
                    if (currentTurn == constants.BLACK) {

                        resetBlackTimer();
                        startWhiteTimer();


                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                        //  playerTurn.setText("White turn");
                    } else {
                        // playerTurn.setText("Black turn");

                        resetWhiteTimer();
                        startBlackTimer();

                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
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
                        // playerTurn.setText("Black wins!");
                    }

                }
            } else {
                Log.i(TAG, "invalid move");
            }
        } else {
            Log.i(TAG, "checker not selected");
        }
    }

    public int findPlayer2Balls(int opponent, int previousReturn) {
        int[] playingfield = rules.playingfield;
        int movePosition = -99;

        if (playingfield[1] == playingfield[2] && playingfield[2] == opponent && playingfield[3] == rules.EMPTY_FIELD) {
            movePosition = 2;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[4] == playingfield[5] && playingfield[5] == opponent && rules.EMPTY_FIELD == playingfield[6])) {
            movePosition = 5;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[7] == playingfield[8] && playingfield[8] == opponent && rules.EMPTY_FIELD == playingfield[9])) {
            movePosition = 8;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[10] == playingfield[11] && playingfield[11] == opponent && rules.EMPTY_FIELD == playingfield[12]) {
            movePosition = 11;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[13] == playingfield[14] && playingfield[14] == opponent && rules.EMPTY_FIELD == playingfield[15]) {
            movePosition = 14;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[16] == playingfield[17] && playingfield[17] == opponent && rules.EMPTY_FIELD == playingfield[18])) {
            movePosition = 17;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[19] == playingfield[20] && playingfield[20] == opponent && rules.EMPTY_FIELD == playingfield[21])) {
            movePosition = 20;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[22] == playingfield[23] && playingfield[23] == opponent && rules.EMPTY_FIELD == playingfield[24]) {
            movePosition = 23;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[1] == playingfield[10] && playingfield[10] == opponent && rules.EMPTY_FIELD == playingfield[22]) {
            movePosition = 21;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[4] == playingfield[11] && playingfield[11] == opponent && rules.EMPTY_FIELD == playingfield[19])) {
            movePosition = 18;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[7] == playingfield[12] && playingfield[12] == opponent && rules.EMPTY_FIELD == playingfield[16])) {
            movePosition = 15;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[2] == playingfield[5] && playingfield[5] == opponent && rules.EMPTY_FIELD == playingfield[8])) {
            movePosition = 7;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[17] == playingfield[20] && playingfield[20] == opponent && rules.EMPTY_FIELD == playingfield[23])) {
            movePosition = 22;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[9] == playingfield[13] && playingfield[13] == opponent && rules.EMPTY_FIELD == playingfield[18])) {
            movePosition = 17;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[6] == playingfield[14] && playingfield[14] == opponent && rules.EMPTY_FIELD == playingfield[21])) {
            movePosition = 20;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[3] == playingfield[15] && playingfield[15] == opponent && rules.EMPTY_FIELD == playingfield[24])) {
            movePosition = 23;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }

        /// --- ////

        if (playingfield[2] == playingfield[3] && playingfield[2] == opponent && playingfield[1] == rules.EMPTY_FIELD) {
            movePosition = 0;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[5] == playingfield[6] && playingfield[5] == opponent && rules.EMPTY_FIELD == playingfield[4])) {
            movePosition = 3;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[8] == playingfield[9] && playingfield[8] == opponent && rules.EMPTY_FIELD == playingfield[7])) {
            movePosition = 6;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[11] == playingfield[12] && playingfield[11] == opponent && rules.EMPTY_FIELD == playingfield[10]) {
            movePosition = 9;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[15] == playingfield[14] && playingfield[14] == opponent && rules.EMPTY_FIELD == playingfield[13]) {
            movePosition = 12;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[18] == playingfield[17] && playingfield[17] == opponent && rules.EMPTY_FIELD == playingfield[16])) {
            movePosition = 15;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[21] == playingfield[20] && playingfield[20] == opponent && rules.EMPTY_FIELD == playingfield[19])) {
            movePosition = 18;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[24] == playingfield[23] && playingfield[23] == opponent && rules.EMPTY_FIELD == playingfield[22]) {
            movePosition = 21;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[22] == playingfield[10] && playingfield[10] == opponent && rules.EMPTY_FIELD == playingfield[1]) {
            movePosition = 0;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[19] == playingfield[11] && playingfield[11] == opponent && rules.EMPTY_FIELD == playingfield[4])) {
            movePosition = 3;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[16] == playingfield[12] && playingfield[12] == opponent && rules.EMPTY_FIELD == playingfield[7])) {
            movePosition = 6;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[8] == playingfield[5] && playingfield[5] == opponent && rules.EMPTY_FIELD == playingfield[2])) {
            movePosition = 1;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[23] == playingfield[20] && playingfield[20] == opponent && rules.EMPTY_FIELD == playingfield[17])) {
            movePosition = 16;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[18] == playingfield[13] && playingfield[13] == opponent && rules.EMPTY_FIELD == playingfield[9])) {
            movePosition = 8;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[21] == playingfield[14] && playingfield[14] == opponent && rules.EMPTY_FIELD == playingfield[6])) {
            movePosition = 5;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[24] == playingfield[15] && playingfield[15] == opponent && rules.EMPTY_FIELD == playingfield[3])) {
            movePosition = 2;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }

        /// --- ////


        if (playingfield[1] == playingfield[3] && playingfield[3] == opponent && playingfield[2] == rules.EMPTY_FIELD) {
            movePosition = 1;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[4] == playingfield[6] && playingfield[6] == opponent && rules.EMPTY_FIELD == playingfield[5])) {
            movePosition = 4;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[7] == playingfield[9] && playingfield[9] == opponent && rules.EMPTY_FIELD == playingfield[8])) {
            movePosition = 7;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[10] == playingfield[12] && playingfield[12] == opponent && rules.EMPTY_FIELD == playingfield[11]) {
            movePosition = 10;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[15] == playingfield[13] && playingfield[13] == opponent && rules.EMPTY_FIELD == playingfield[14]) {
            movePosition = 13;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[16] == playingfield[18] && playingfield[16] == opponent && rules.EMPTY_FIELD == playingfield[17])) {
            movePosition = 16;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[19] == playingfield[21] && playingfield[21] == opponent && rules.EMPTY_FIELD == playingfield[20])) {
            movePosition = 19;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[22] == playingfield[24] && playingfield[22] == opponent && rules.EMPTY_FIELD == playingfield[23]) {
            movePosition = 22;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if (playingfield[22] == playingfield[1] && playingfield[1] == opponent && rules.EMPTY_FIELD == playingfield[10]) {
            movePosition = 9;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[19] == playingfield[4] && playingfield[4] == opponent && rules.EMPTY_FIELD == playingfield[11])) {
            movePosition = 10;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[16] == playingfield[7] && playingfield[7] == opponent && rules.EMPTY_FIELD == playingfield[12])) {
            movePosition = 11;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[8] == playingfield[2] && playingfield[2] == opponent && rules.EMPTY_FIELD == playingfield[5])) {
            movePosition = 4;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[23] == playingfield[17] && playingfield[17] == opponent && rules.EMPTY_FIELD == playingfield[23])) {
            movePosition = 22;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[18] == playingfield[9] && playingfield[9] == opponent && rules.EMPTY_FIELD == playingfield[13])) {
            movePosition = 12;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[21] == playingfield[6] && playingfield[6] == opponent && rules.EMPTY_FIELD == playingfield[14])) {
            movePosition = 13;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }
        if ((playingfield[24] == playingfield[3] && playingfield[3] == opponent && rules.EMPTY_FIELD == playingfield[15])) {
            movePosition = 14;
            if (movePosition != previousReturn) {
                return movePosition;
            }
        }

        return movePosition;
    }

    public int findPlayer2Balls(int opponent) {
        int[] playingfield = rules.playingfield;
        int movePosition = -99;

        if (playingfield[1] == playingfield[2] && playingfield[2] == opponent && playingfield[3] == rules.EMPTY_FIELD) {
            movePosition = 2;
            return movePosition;
        }
        if ((playingfield[4] == playingfield[5] && playingfield[5] == opponent && rules.EMPTY_FIELD == playingfield[6])) {
            movePosition = 5;
            return movePosition;
        }
        if ((playingfield[7] == playingfield[8] && playingfield[8] == opponent && rules.EMPTY_FIELD == playingfield[9])) {
            movePosition = 8;
            return movePosition;
        }
        if (playingfield[10] == playingfield[11] && playingfield[11] == opponent && rules.EMPTY_FIELD == playingfield[12]) {
            movePosition = 11;
            return movePosition;
        }
        if (playingfield[13] == playingfield[14] && playingfield[14] == opponent && rules.EMPTY_FIELD == playingfield[15]) {
            movePosition = 14;
            return movePosition;
        }
        if ((playingfield[16] == playingfield[17] && playingfield[17] == opponent && rules.EMPTY_FIELD == playingfield[18])) {
            movePosition = 17;
            return movePosition;
        }
        if ((playingfield[19] == playingfield[20] && playingfield[20] == opponent && rules.EMPTY_FIELD == playingfield[21])) {
            movePosition = 20;
            return movePosition;
        }
        if (playingfield[22] == playingfield[23] && playingfield[23] == opponent && rules.EMPTY_FIELD == playingfield[24]) {
            movePosition = 23;
            return movePosition;
        }
        if (playingfield[1] == playingfield[10] && playingfield[10] == opponent && rules.EMPTY_FIELD == playingfield[22]) {
            movePosition = 21;
            return movePosition;
        }
        if ((playingfield[4] == playingfield[11] && playingfield[11] == opponent && rules.EMPTY_FIELD == playingfield[19])) {
            movePosition = 18;
            return movePosition;
        }
        if ((playingfield[7] == playingfield[12] && playingfield[12] == opponent && rules.EMPTY_FIELD == playingfield[16])) {
            movePosition = 15;
            return movePosition;
        }
        if ((playingfield[2] == playingfield[5] && playingfield[5] == opponent && rules.EMPTY_FIELD == playingfield[8])) {
            movePosition = 7;
            return movePosition;
        }
        if ((playingfield[17] == playingfield[20] && playingfield[20] == opponent && rules.EMPTY_FIELD == playingfield[23])) {
            movePosition = 22;
            return movePosition;
        }
        if ((playingfield[9] == playingfield[13] && playingfield[13] == opponent && rules.EMPTY_FIELD == playingfield[18])) {
            movePosition = 17;
            return movePosition;
        }
        if ((playingfield[6] == playingfield[14] && playingfield[14] == opponent && rules.EMPTY_FIELD == playingfield[21])) {
            movePosition = 20;
            return movePosition;
        }
        if ((playingfield[3] == playingfield[15] && playingfield[15] == opponent && rules.EMPTY_FIELD == playingfield[24])) {
            movePosition = 23;
            return movePosition;
        }

        /// --- ////

        if (playingfield[2] == playingfield[3] && playingfield[2] == opponent && playingfield[1] == rules.EMPTY_FIELD) {
            movePosition = 0;
            return movePosition;
        }
        if ((playingfield[5] == playingfield[6] && playingfield[5] == opponent && rules.EMPTY_FIELD == playingfield[4])) {
            movePosition = 3;
            return movePosition;
        }
        if ((playingfield[8] == playingfield[9] && playingfield[8] == opponent && rules.EMPTY_FIELD == playingfield[7])) {
            movePosition = 6;
            return movePosition;
        }
        if (playingfield[11] == playingfield[12] && playingfield[11] == opponent && rules.EMPTY_FIELD == playingfield[10]) {
            movePosition = 9;
            return movePosition;
        }
        if (playingfield[15] == playingfield[14] && playingfield[14] == opponent && rules.EMPTY_FIELD == playingfield[13]) {
            movePosition = 12;
            return movePosition;
        }
        if ((playingfield[18] == playingfield[17] && playingfield[17] == opponent && rules.EMPTY_FIELD == playingfield[16])) {
            movePosition = 15;
            return movePosition;
        }
        if ((playingfield[21] == playingfield[20] && playingfield[20] == opponent && rules.EMPTY_FIELD == playingfield[19])) {
            movePosition = 18;
            return movePosition;
        }
        if (playingfield[24] == playingfield[23] && playingfield[23] == opponent && rules.EMPTY_FIELD == playingfield[22]) {
            movePosition = 21;
            return movePosition;
        }
        if (playingfield[22] == playingfield[10] && playingfield[10] == opponent && rules.EMPTY_FIELD == playingfield[1]) {
            movePosition = 0;
            return movePosition;
        }
        if ((playingfield[19] == playingfield[11] && playingfield[11] == opponent && rules.EMPTY_FIELD == playingfield[4])) {
            movePosition = 3;
            return movePosition;
        }
        if ((playingfield[16] == playingfield[12] && playingfield[12] == opponent && rules.EMPTY_FIELD == playingfield[7])) {
            movePosition = 6;
            return movePosition;
        }
        if ((playingfield[8] == playingfield[5] && playingfield[5] == opponent && rules.EMPTY_FIELD == playingfield[2])) {
            movePosition = 1;
            return movePosition;
        }
        if ((playingfield[23] == playingfield[20] && playingfield[20] == opponent && rules.EMPTY_FIELD == playingfield[17])) {
            movePosition = 16;
            return movePosition;
        }
        if ((playingfield[18] == playingfield[13] && playingfield[13] == opponent && rules.EMPTY_FIELD == playingfield[9])) {
            movePosition = 8;
            return movePosition;
        }
        if ((playingfield[21] == playingfield[14] && playingfield[14] == opponent && rules.EMPTY_FIELD == playingfield[6])) {
            movePosition = 5;
            return movePosition;
        }
        if ((playingfield[24] == playingfield[15] && playingfield[15] == opponent && rules.EMPTY_FIELD == playingfield[3])) {
            movePosition = 2;
            return movePosition;
        }

        /// --- ////


        if (playingfield[1] == playingfield[3] && playingfield[3] == opponent && playingfield[2] == rules.EMPTY_FIELD) {
            movePosition = 1;
            return movePosition;
        }
        if ((playingfield[4] == playingfield[6] && playingfield[6] == opponent && rules.EMPTY_FIELD == playingfield[5])) {
            movePosition = 4;
            return movePosition;
        }
        if ((playingfield[7] == playingfield[9] && playingfield[9] == opponent && rules.EMPTY_FIELD == playingfield[8])) {
            movePosition = 7;
            return movePosition;
        }
        if (playingfield[10] == playingfield[12] && playingfield[12] == opponent && rules.EMPTY_FIELD == playingfield[11]) {
            movePosition = 10;
            return movePosition;
        }
        if (playingfield[15] == playingfield[13] && playingfield[13] == opponent && rules.EMPTY_FIELD == playingfield[14]) {
            movePosition = 13;
            return movePosition;
        }
        if ((playingfield[16] == playingfield[18] && playingfield[16] == opponent && rules.EMPTY_FIELD == playingfield[17])) {
            movePosition = 16;
            return movePosition;
        }
        if ((playingfield[19] == playingfield[21] && playingfield[21] == opponent && rules.EMPTY_FIELD == playingfield[20])) {
            movePosition = 19;
            return movePosition;
        }
        if (playingfield[22] == playingfield[24] && playingfield[22] == opponent && rules.EMPTY_FIELD == playingfield[23]) {
            movePosition = 22;
            return movePosition;
        }
        if (playingfield[22] == playingfield[1] && playingfield[1] == opponent && rules.EMPTY_FIELD == playingfield[10]) {
            movePosition = 9;
            return movePosition;
        }
        if ((playingfield[19] == playingfield[4] && playingfield[4] == opponent && rules.EMPTY_FIELD == playingfield[11])) {
            movePosition = 10;
            return movePosition;
        }
        if ((playingfield[16] == playingfield[7] && playingfield[7] == opponent && rules.EMPTY_FIELD == playingfield[12])) {
            movePosition = 11;
            return movePosition;
        }
        if ((playingfield[8] == playingfield[2] && playingfield[2] == opponent && rules.EMPTY_FIELD == playingfield[5])) {
            movePosition = 4;
            return movePosition;
        }
        if ((playingfield[23] == playingfield[17] && playingfield[17] == opponent && rules.EMPTY_FIELD == playingfield[23])) {
            movePosition = 22;
            return movePosition;
        }
        if ((playingfield[18] == playingfield[9] && playingfield[9] == opponent && rules.EMPTY_FIELD == playingfield[13])) {
            movePosition = 12;
            return movePosition;
        }
        if ((playingfield[21] == playingfield[6] && playingfield[6] == opponent && rules.EMPTY_FIELD == playingfield[14])) {
            movePosition = 13;
            return movePosition;
        }
        if ((playingfield[24] == playingfield[3] && playingfield[3] == opponent && rules.EMPTY_FIELD == playingfield[15])) {
            movePosition = 14;
            return movePosition;
        }

        return movePosition;
    }

    public int findBot_1_Ball(int opponent) {
        int[] playingfield = rules.playingfield;

        if (playingfield[1] == opponent && playingfield[2] == rules.EMPTY_FIELD) {
            return 1;
        }

        if (playingfield[1] == opponent && playingfield[10] == rules.EMPTY_FIELD) {
            return 9;
        }

        if (playingfield[2] == opponent && playingfield[1] == rules.EMPTY_FIELD) {
            return 0;
        }

        if (playingfield[2] == opponent && playingfield[3] == rules.EMPTY_FIELD) {
            return 2;
        }

        if (playingfield[2] == opponent && playingfield[5] == rules.EMPTY_FIELD) {
            return 4;
        }

        if (playingfield[3] == opponent && playingfield[2] == rules.EMPTY_FIELD) {
            return 1;
        }

        if (playingfield[3] == opponent && playingfield[15] == rules.EMPTY_FIELD) {
            return 14;
        }

        if (playingfield[4] == opponent && playingfield[5] == rules.EMPTY_FIELD) {
            return 4;
        }

        if (playingfield[4] == opponent && playingfield[11] == rules.EMPTY_FIELD) {
            return 10;
        }

        if (playingfield[5] == opponent && playingfield[6] == rules.EMPTY_FIELD) {
            return 5;
        }

        if (playingfield[5] == opponent && playingfield[4] == rules.EMPTY_FIELD) {
            return 3;
        }

        if (playingfield[5] == opponent && playingfield[8] == rules.EMPTY_FIELD) {
            return 7;
        }

        if (playingfield[6] == opponent && playingfield[5] == rules.EMPTY_FIELD) {
            return 4;
        }

        if (playingfield[6] == opponent && playingfield[14] == rules.EMPTY_FIELD) {
            return 13;
        }

        if (playingfield[7] == opponent && playingfield[8] == rules.EMPTY_FIELD) {
            return 7;
        }

        if (playingfield[7] == opponent && playingfield[12] == rules.EMPTY_FIELD) {
            return 11;
        }

        if (playingfield[8] == opponent && playingfield[7] == rules.EMPTY_FIELD) {
            return 6;
        }

        if (playingfield[8] == opponent && playingfield[5] == rules.EMPTY_FIELD) {
            return 4;
        }

        if (playingfield[8] == opponent && playingfield[9] == rules.EMPTY_FIELD) {
            return 8;
        }

        if (playingfield[9] == opponent && playingfield[8] == rules.EMPTY_FIELD) {
            return 7;
        }

        if (playingfield[9] == opponent && playingfield[13] == rules.EMPTY_FIELD) {
            return 12;
        }

        if (playingfield[10] == opponent && playingfield[1] == rules.EMPTY_FIELD) {
            return 0;
        }

        if (playingfield[10] == opponent && playingfield[11] == rules.EMPTY_FIELD) {
            return 10;
        }

        if (playingfield[10] == opponent && playingfield[22] == rules.EMPTY_FIELD) {
            return 21;
        }

        if (playingfield[11] == opponent && playingfield[4] == rules.EMPTY_FIELD) {
            return 3;
        }
        if (playingfield[11] == opponent && playingfield[10] == rules.EMPTY_FIELD) {
            return 9;
        }
        if (playingfield[11] == opponent && playingfield[12] == rules.EMPTY_FIELD) {
            return 11;
        }
        if (playingfield[11] == opponent && playingfield[19] == rules.EMPTY_FIELD) {
            return 18;
        }

        if (playingfield[12] == opponent && playingfield[11] == rules.EMPTY_FIELD) {
            return 10;
        }
        if (playingfield[12] == opponent && playingfield[7] == rules.EMPTY_FIELD) {
            return 6;
        }
        if (playingfield[12] == opponent && playingfield[16] == rules.EMPTY_FIELD) {
            return 15;
        }

        if (playingfield[13] == opponent && playingfield[9] == rules.EMPTY_FIELD) {
            return 8;
        }
        if (playingfield[13] == opponent && playingfield[14] == rules.EMPTY_FIELD) {
            return 13;
        }
        if (playingfield[13] == opponent && playingfield[18] == rules.EMPTY_FIELD) {
            return 17;
        }

        if (playingfield[14] == opponent && playingfield[6] == rules.EMPTY_FIELD) {
            return 5;
        }
        if (playingfield[14] == opponent && playingfield[13] == rules.EMPTY_FIELD) {
            return 12;
        }
        if (playingfield[14] == opponent && playingfield[15] == rules.EMPTY_FIELD) {
            return 14;
        }
        if (playingfield[14] == opponent && playingfield[21] == rules.EMPTY_FIELD) {
            return 20;
        }

        if (playingfield[15] == opponent && playingfield[3] == rules.EMPTY_FIELD) {
            return 2;
        }
        if (playingfield[15] == opponent && playingfield[14] == rules.EMPTY_FIELD) {
            return 13;
        }
        if (playingfield[15] == opponent && playingfield[24] == rules.EMPTY_FIELD) {
            return 23;
        }

        if (playingfield[16] == opponent && playingfield[12] == rules.EMPTY_FIELD) {
            return 11;
        }
        if (playingfield[16] == opponent && playingfield[17] == rules.EMPTY_FIELD) {
            return 16;
        }

        if (playingfield[17] == opponent && playingfield[16] == rules.EMPTY_FIELD) {
            return 15;
        }
        if (playingfield[17] == opponent && playingfield[20] == rules.EMPTY_FIELD) {
            return 19;
        }
        if (playingfield[17] == opponent && playingfield[18] == rules.EMPTY_FIELD) {
            return 17;
        }

        if (playingfield[18] == opponent && playingfield[13] == rules.EMPTY_FIELD) {
            return 12;
        }
        if (playingfield[18] == opponent && playingfield[17] == rules.EMPTY_FIELD) {
            return 16;
        }

        if (playingfield[19] == opponent && playingfield[11] == rules.EMPTY_FIELD) {
            return 10;
        }
        if (playingfield[19] == opponent && playingfield[20] == rules.EMPTY_FIELD) {
            return 19;
        }

        if (playingfield[20] == opponent && playingfield[19] == rules.EMPTY_FIELD) {
            return 18;
        }
        if (playingfield[20] == opponent && playingfield[17] == rules.EMPTY_FIELD) {
            return 16;
        }
        if (playingfield[20] == opponent && playingfield[21] == rules.EMPTY_FIELD) {
            return 20;
        }
        if (playingfield[20] == opponent && playingfield[23] == rules.EMPTY_FIELD) {
            return 22;
        }

        if (playingfield[21] == opponent && playingfield[14] == rules.EMPTY_FIELD) {
            return 13;
        }
        if (playingfield[21] == opponent && playingfield[20] == rules.EMPTY_FIELD) {
            return 19;
        }

        if (playingfield[22] == opponent && playingfield[10] == rules.EMPTY_FIELD) {
            return 9;
        }
        if (playingfield[22] == opponent && playingfield[23] == rules.EMPTY_FIELD) {
            return 22;
        }

        if (playingfield[23] == opponent && playingfield[22] == rules.EMPTY_FIELD) {
            return 21;
        }
        if (playingfield[23] == opponent && playingfield[20] == rules.EMPTY_FIELD) {
            return 19;
        }
        if (playingfield[23] == opponent && playingfield[24] == rules.EMPTY_FIELD) {
            return 23;
        }


        if (playingfield[24] == opponent && playingfield[23] == rules.EMPTY_FIELD) {
            return 22;
        }
        if (playingfield[24] == opponent && playingfield[15] == rules.EMPTY_FIELD) {
            return 14;
        }

        return -99;
    }


    public void removeBallFromView(String type, Integer indexFromList, String Removetype) {

        Log.i(TAG, "Index from list = " + indexFromList);
        removeNextChecker = false;
        if (indexFromList != -99) {
            View v = null;

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

            if (mediaPlayer != null) mediaPlayer.setVolume(0.1f, 0.1f);
            MediaPlayer tickPlayer = MediaPlayer.create(this, R.raw.kill_ball);
            tickPlayer.setVolume(1f, 1f);
            tickPlayer.start();
            tickPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) mediaPlayer.setVolume(1f, 1f);
                }
            });


            ViewGroup parent = ((ViewGroup) v.getParent());
            final int index = parent.indexOfChild(v);
            parent.removeView(v);
            FrameLayout placeholder = (FrameLayout) getLayoutInflater().inflate(R.layout.layout_placeholder, parent, false);
            parent.addView(placeholder, index);
            //  checkerPositions.put((ImageView) v, -99);
            checkerPositions.remove((ImageView) v);


            if (Removetype.trim().equalsIgnoreCase("SideView")) {
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
                    Log.i("asdad12312", "Current turn = black & changed to white ");
                    rules.changeTurn(constants.WHITE);
                    resetBlackTimer();
                    startWhiteTimer();
                    white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                    black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));

                    //  playerTurn.setText("White turn");
                } else {
                    Log.i("asdad12312", "Current turn = white & changed to black ");
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
        new CountDownTimer(totalTimerTimeBlack, 1000) {

            public void onTick(long millisUntilFinished) {
                // black_clock.setVisibility(View.VISIBLE);
                //   black_timer.setVisibility(View.VISIBLE);
                //black_timer.setText(millisUntilFinished / 1000 + "");

            }

            public void onFinish() {

                if (removeNextChecker) {
                    Log.i("asdad12312", "removeNextChecker");
                    removeNextChecker = false;
                    String type = "";
                    int indexFromList = whiteBallIndexFromGridView();
                    type = "GridView";
                    if (indexFromList != -99) {
                        Log.i("asdad12312", "Index for removal = " + indexFromList);
                        removeBallFromView("white", indexFromList, type);
                    } else {
                        //  change turn white has no ball on grid
                        Log.i("asdad12312", "change turn white has no ball on grid");
                        rules.changeTurn(constants.WHITE);
                        resetBlackTimer();
                        startWhiteTimer();
                        white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));
                        black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                    }
                } else {
                    Log.i("asdad12312", "Play normal");
                    if (backIsNotPressed) {
                        playBotBall();
                    }
                }

                black_clock.setVisibility(View.INVISIBLE);
                black_timer.setVisibility(View.INVISIBLE);
                game_play_status.setText("");
            }
        }.start();
    }

    public void playBotBall() {
        String thisTag = "asd1234";
        Boolean isGalleryBall = false;

        int indexFromList = blackBallIndexFromSideView();
        if (indexFromList == -99) {
            indexFromList = blackBallIndexFromGridView();
            isGalleryBall = true;
        }


        if (isGalleryBall) {

            ArrayList<Integer> randArr = new ArrayList<>();

            for (Map.Entry<ImageView, Integer> entry : checkerPositions.entrySet()) {
                ImageView imageView = entry.getKey();
                if (entry.getValue() != 0 && entry.getValue() != -99) {
                    if (blackCheckers.contains(imageView)) {
                        int index = blackCheckers.indexOf(imageView);
                        randArr.add(index);
                    }
                }
            }

            Collections.shuffle(randArr);

            int moveToOwnBalls = findPlayer2Balls(BOT, previousReturn);

            previousReturn = moveToOwnBalls;

            boolean hasMoved = false;


            if (moveToOwnBalls != -99) {
                moveToOwnBalls = moveToOwnBalls + 1;
                for (int ball : randArr) {
                    if (ball == previousBallMoved) {
                        Log.i("123avmpwe", "Ball  FOubd ------------- " + ball);
                        continue;
                    }
                    View v = blackCheckers.get(ball);
                    Log.i("123avmpwe", "Player Balls : Previous = " + previousReturn + " moveToOWnBalls = " + moveToOwnBalls + " ball = " + ball);
                    if (rules.canBotBallMoveToPosition(moveToOwnBalls, checkerPositions.get(v))) {
                        selectChecker(v);
                        moveToOwnBalls--;
                        FrameLayout frameLayout = higBoxAreas.get(moveToOwnBalls);
                        gridHelperMethod(frameLayout);
                        previousBallMoved = ball;
                        hasMoved = true;
                        break;
                    }
                }
            }
            Collections.shuffle(randArr);
            if (hasMoved) {
                return;
            }

            int moveToOwnSingleBall = findBot_1_Ball(BOT);

            if (moveToOwnSingleBall != -99) {
                moveToOwnSingleBall = moveToOwnSingleBall + 1;
                for (int ball : randArr) {
                    if (ball == previousBallMoved) {
                        continue;
                    }
                    View v = blackCheckers.get(ball);
                    Log.i("123avmpwe", "Player Balls Single : Previous = " + previousReturn + " moveToOWnBalls = " + moveToOwnBalls + " ball = " + ball);
                    if (rules.canBotBallMoveToPosition(moveToOwnBalls, checkerPositions.get(v))) {
                        selectChecker(v);
                        moveToOwnSingleBall--;
                        FrameLayout frameLayout = higBoxAreas.get(moveToOwnSingleBall);
                        gridHelperMethod(frameLayout);
                        previousBallMoved = ball;
                        hasMoved = true;
                        break;
                    }
                }
            }
            Collections.shuffle(randArr);
            if (hasMoved) {
                return;
            }

            int moveToOpponent = findPlayer2Balls(constants.WHITE, previousReturn);

            previousReturn = moveToOpponent;

            if (moveToOpponent != -99) {
                moveToOpponent = moveToOpponent + 1;
                for (int ball : randArr) {
                    if (ball == previousBallMoved) {
                        continue;
                    }
                    View v = blackCheckers.get(ball);

                    Log.i("123avmpwe", "Opponent Balls : Previous = " + previousReturn + " moveToOpponent = " + moveToOwnBalls + " ball = " + ball);
                    if (rules.canBotBallMoveToPosition(moveToOwnBalls, checkerPositions.get(v))) {
                        selectChecker(v);
                        moveToOpponent--;
                        FrameLayout frameLayout = higBoxAreas.get(moveToOpponent);
                        gridHelperMethod(frameLayout);
                        previousBallMoved = ball;
                        hasMoved = true;
                        break;
                    }
                }
            }
            Collections.shuffle(randArr);
            if (hasMoved) {
                return;
            }


            int randIndex = getRandomNumber(randArr.size());


            while (randIndex == previousBallPlayed) {
                randIndex = getRandomNumber(randArr.size());
                Log.i("asasdad12", randIndex + " -- " + previousBallPlayed);
            }
            indexFromList = randArr.get(randIndex);
            View v = blackCheckers.get(indexFromList);
            selectChecker(v);

            int ballPostOnGrid = checkerPositions.get(v);

            Integer moveArea = rules.isValidMove_BOT(ballPostOnGrid);
            Log.i("13adad", "Move = " + moveArea);
            Log.i("123asdads", randIndex + "");

            while (moveArea == null) {
                randIndex = getRandomNumber(randArr.size());
                Log.i("asasdad12", randIndex + " -- " + previousBallPlayed);
                while (randIndex == previousBallPlayed) {
                    randIndex = getRandomNumber(randArr.size());
                    Log.i("asasdad12", randIndex + " -- " + previousBallPlayed);
                }

                Log.i("123asdads", randIndex + "");
                indexFromList = randArr.get(randIndex);
                View vv = blackCheckers.get(indexFromList);
                selectChecker(vv);
                ballPostOnGrid = checkerPositions.get(vv);
                moveArea = rules.isValidMove_BOT(ballPostOnGrid);
            }

            Log.i("123avmpwe", "Any : Previous = " + previousBallPlayed + " moveArea = " + moveArea + " randIndex = " + randIndex);

            previousBallPlayed = randIndex;
            Log.i("asasdad12", randIndex + " -- " + previousBallPlayed);
            FrameLayout frameLayout = higBoxAreas.get(moveArea);
            gridHelperMethod(frameLayout);
            Collections.shuffle(randArr);
            return;
        }
        //Player two balls
        int moveTo = findPlayer2Balls(constants.WHITE);
        Log.i(thisTag, "Move To : Player 2 balls " + moveTo + " " + " -- " + indexFromList);
        if (moveTo != -99) {
            View v = blackCheckers.get(indexFromList);
            selectChecker(v);
            FrameLayout frameLayout = higBoxAreas.get(moveTo);
            gridHelperMethod(frameLayout);
        } else {
            // Bot two balls
            int moveToOwnBalls = findPlayer2Balls(BOT);
            Log.i(thisTag, "Move To : BOT 2 balls " + moveTo);
            if (moveToOwnBalls != -99) {
                View v = blackCheckers.get(indexFromList);
                selectChecker(v);
                FrameLayout frameLayout = higBoxAreas.get(moveToOwnBalls);
                gridHelperMethod(frameLayout);
            } else {
                //  Bot 1 Ball
                int moveToOwnSingleBall = findBot_1_Ball(BOT);
                Log.i(thisTag, "Move To : BOT 1 Ball " + moveTo);
                if (moveToOwnSingleBall != -99) {
                    View v = blackCheckers.get(indexFromList);
                    selectChecker(v);
                    FrameLayout frameLayout = higBoxAreas.get(moveToOwnSingleBall);
                    gridHelperMethod(frameLayout);
                } else {
                    // move to any position

                    View v = blackCheckers.get(indexFromList);
                    selectChecker(v);

                    int rand = getRandomNumber();

                    while (rules.playingfield[rand] != rules.EMPTY_FIELD) {
                        rand = getRandomNumber();
                    }


                    FrameLayout frameLayout = higBoxAreas.get(rand);
                    gridHelperMethod(frameLayout);

                }
            }

        }
    }

    public int getRandomNumber() {
        Random rand = new Random();
        return rand.nextInt(24);
    }

    public int getRandomNumber(int max) {
        Random rand = new Random();
        return rand.nextInt(max);
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
            timerForBlack = null;
        } else {
            Log.i(TAG, "timerForBlack is null");
        }


        totalTimerTimeBlack = timeForBlackTemp;
        black_clock.setVisibility(View.INVISIBLE);
        black_timer.setVisibility(View.INVISIBLE);
    }


    /**
     * Move the checker from the current position to a new position
     *
     * @param turn Constant.WHITE or Constant.BLACK according to whos turn it is
     */
    private void moveChecker(int turn) {

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
            whiteIndexes.add(index + "");

            whiteMoveCounter++;

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

        if (mediaPlayer != null) mediaPlayer.setVolume(0.1f, 0.1f);
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
    private void selectChecker(View v) {
        //Is it a remove click=
        if (removeNextChecker) {
            //Is it a valid remove click?
            if (rules.getTurn() == constants.WHITE && rules.remove(checkerPositions.get(v), constants.BLACK)) {

                if (mediaPlayer != null) mediaPlayer.setVolume(0.1f, 0.1f);
                MediaPlayer tickPlayer = MediaPlayer.create(this, R.raw.kill_ball);
                tickPlayer.setVolume(1f, 1f);
                tickPlayer.start();
                tickPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mediaPlayer != null) mediaPlayer.setVolume(1f, 1f);
                    }
                });

                //Unamrk all options and remove the selected checker
                game_play_status.setText("");
                unMarkAllFields();
                blackCheckers.remove(v);
                removeNextChecker = false;
                ViewGroup parent = ((ViewGroup) v.getParent());
                parent.removeView(v);
                // playerTurn.setText("Black turn");

                resetWhiteTimer();
                startBlackTimer();

                white_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColorOther));
                black_box_layout.setBackgroundColor(getResources().getColor(R.color.turnColor));

                //Did someone win?
                isWin = rules.isItAWin(constants.BLACK);
                if (isWin) {
                    gameWon(constants.WHITE);
                    //  playerTurn.setText("White wins!");
                }

                rules.changeTurn(constants.BLACK);

            } else if (rules.getTurn() == constants.WHITE && rules.remove(checkerPositions.get(v), constants.WHITE)) {
                //Unmark all options and remove the selected checker

                if (mediaPlayer != null) mediaPlayer.setVolume(0.1f, 0.1f);
                MediaPlayer tickPlayer = MediaPlayer.create(this, R.raw.kill_ball);
                tickPlayer.setVolume(1f, 1f);
                tickPlayer.start();
                tickPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mediaPlayer != null) mediaPlayer.setVolume(1f, 1f);
                    }
                });

                game_play_status.setText("");
                unMarkAllFields();
                whiteCheckers.remove(v);
                removeNextChecker = false;
                ViewGroup parent = ((ViewGroup) v.getParent());
                parent.removeView(v);
                //  playerTurn.setText("White turn");

                startWhiteTimer();
                resetBlackTimer();

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
    private void markAvailableMoveFields(int from) {
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
    private void unMarkAllFields() {
        for (FrameLayout f : higBoxAreas) {
            f.setBackgroundResource(0);
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

    }

    @Override
    public void onPause() {
        super.onPause();
        pauseMedia();

    }

    public void gameWon(int color) {

        resetBlackTimer();
        resetWhiteTimer();

        String message = "";

        if (color == constants.WHITE) {
            message = "White Has Won The Game.\nBlack Moves = " + blackMoveCounter + " \nWhite Moves = " + whiteMoveCounter;
        } else {
            message = "Black Has Won The Game.\nBlack Moves = " + blackMoveCounter + " \nWhite Moves = " + whiteMoveCounter;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(offline_player_vs_bot.this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        if (offline_player_vs_bot.this != null) {
            dialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(offline_player_vs_bot.this);
        builder.setMessage("You want to quit this game?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                backIsNotPressed = false;
                dialog.dismiss();
                resetBlackTimer();
                resetWhiteTimer();
                finish();
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
