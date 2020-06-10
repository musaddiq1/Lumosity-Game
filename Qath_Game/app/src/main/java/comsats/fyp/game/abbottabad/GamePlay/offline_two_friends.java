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
import android.support.v4.content.LocalBroadcastManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import comsats.fyp.game.abbottabad.R;
import comsats.fyp.game.abbottabad.storeRoom.Rules;
import comsats.fyp.game.abbottabad.storeRoom.constants;
import comsats.fyp.game.abbottabad.storeRoom.preferenceManager;
import comsats.fyp.game.abbottabad.storeRoom.socketConnectionHandler;

public class offline_two_friends extends AppCompatActivity implements MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener {
    private final String TAG = "offline_two_friends";

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
    Integer totalTimerTimeBlack;
    Integer totalTimerTimeWhite;

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
        Log.i(TAG, "Creating activity");
        setContentView(R.layout.game_play_offline_friends);


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
            totalTimerTimeBlack = constants.SILVER_GAME_TIME;
        } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.GOLD_BALL)) {
            totalTimerTimeWhite = constants.GOLD_GAME_TIME;
            totalTimerTimeBlack = constants.GOLD_GAME_TIME;
        } else if (preferenceManager.getInstance(getApplicationContext()).getUserCurrentBall().trim().equalsIgnoreCase(constants.DIAMOND_BALL)) {
            totalTimerTimeWhite = constants.DIAMOND_GAME_TIME;
            totalTimerTimeBlack = constants.DIAMOND_GAME_TIME;
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
                    if (rules.getTurn() == constants.WHITE && !isWin) {
                        selectChecker(v);
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
                    if (rules.getTurn() == constants.BLACK && !isWin) {
                        selectChecker(v);
                    }
                }
            });
        }

        //Add a clickListener to all the hit box areas
        for (FrameLayout v : higBoxAreas) {
            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    //If we have a selected checker, try to move it
                    if (hasSelectedChecker) {
                        Log.i(TAG, "Area clicked");
                        int currentTurn = rules.getTurn();
                        areaToMoveTo = (FrameLayout) v;

                        //What areas are we moving from and to?
                        int to = Integer.parseInt((String) areaToMoveTo.getContentDescription());
                        int from = checkerPositions.get(selectedChecker);
                        //Try to move the checker
                        if (rules.validMove(from, to)) { // This line will change turn
                            Log.i(TAG, "VAlid move");
                            //Update the UI
                            unMarkAllFields();
                            moveChecker(currentTurn);

                            checkerPositions.put((ImageView) selectedChecker, Integer.parseInt((String) areaToMoveTo.getContentDescription()));

                            Log.i("123132", checkerPositions.get(selectedChecker) + "");

                            //Did the row create a row of 3?
                            // removeNextChecker = rules.canRemove(to);


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
                                } else {
                                    game_play_status.setText("Remove Black");
                                    //  playerTurn.setText("Remove Black");
                                }
                            } else {
                                if (currentTurn == constants.BLACK) {

                                    startWhiteTimer();
                                    resetBlackTimer();

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
                        }
                    }
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

                int indexFromList = whiteBallIndexFromSideView();
                if (indexFromList == -99) {
                    indexFromList = whiteBallIndexFromGridView();
                }


                if (indexFromList != -99) {
                    removeBallFromView("white", indexFromList);
                } else {
                    Log.i(TAG, "OOpps its -99 position WHITE");
                }

                white_clock.setVisibility(View.INVISIBLE);
                white_timer.setVisibility(View.INVISIBLE);
                removeNextChecker = false;
                game_play_status.setText("");
            }
        };

        timerForBlack = new CountDownTimer(totalTimerTimeBlack, 1000) {

            public void onTick(long millisUntilFinished) {
                black_clock.setVisibility(View.VISIBLE);
                black_timer.setVisibility(View.VISIBLE);
                black_timer.setText(millisUntilFinished / 1000 + "");
            }

            public void onFinish() {

                int indexFromList = blackBallIndexFromSideView();
                if (indexFromList == -99) {
                    indexFromList = blackBallIndexFromGridView();
                }

                if (indexFromList != -99) {
                    removeBallFromView("black", indexFromList);
                } else {
                    Log.i(TAG, "OOpps its -99 position BLACK");
                }

                black_clock.setVisibility(View.INVISIBLE);
                black_timer.setVisibility(View.INVISIBLE);
                removeNextChecker = false;
                game_play_status.setText("");
            }
        };


        startWhiteTimer();

    }//onCreate ends here

    public void removeBallFromView(String type, Integer indexFromList) {

        Log.i(TAG, "Index from list = " + indexFromList);
        if (indexFromList != -99) {
            View v = null;

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
    private void selectChecker(View v) {
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


    public void gameWon(int color) {

        resetBlackTimer();
        resetWhiteTimer();

        String message = "";

        if (color == constants.WHITE) {
            message = "White Has Won The Game.\nBlack Moves = " + blackMoveCounter + " \nWhite Moves = " + whiteMoveCounter;
        } else {
            message = "Black Has Won The Game.\nBlack Moves = " + blackMoveCounter + " \nWhite Moves = " + whiteMoveCounter;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(offline_two_friends.this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(offline_two_friends.this);
        builder.setMessage("You want to quit this game?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                dialog.dismiss();
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

}
