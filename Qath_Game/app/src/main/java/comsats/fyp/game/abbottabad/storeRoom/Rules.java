package comsats.fyp.game.abbottabad.storeRoom;

import android.util.Log;


/*
 * The game board positions
 *
 * 01           02           03
 *     04       05       06
 *         07   08   09
 * 10  11  12        13  14  15
 *         16   17   18
 *     19       20       21
 * 22           23           24
 *
 */

public class Rules {
    private final String TAG = "Rules";

    public int[] playingfield;
    private int turn;
    //Markers not on the playing field
    private int blackMarkers;
    private int whiteMarkers;

    public final int EMPTY_FIELD = 0;

    public Rules() {
        playingfield = new int[25];
        blackMarkers = 9;
        whiteMarkers = 9;
        turn = constants.WHITE;
    }

    /**
     * Try to move the checker.
     *
     * @param from The position to move from.
     * @param to   The position to move to.
     * @return True if the move was successful, else false is returned.
     */
    public boolean validMove(int from, int to) {
        Log.i(TAG, "Trying to move : " + from + " - " + to);
        Log.i(TAG, "White markers in sideboard: " + whiteMarkers);
        Log.i(TAG, "Black markers in sideboard: " + blackMarkers);

        // Put a marker from "hand" to the board
        if (blackMarkers > 0 && turn == constants.BLACK && playingfield[to] == EMPTY_FIELD) {
            playingfield[to] = constants.BLACK;
            blackMarkers--;
            turn = constants.WHITE;
            return true;
        }
        if (whiteMarkers > 0 && turn == constants.WHITE && playingfield[to] == EMPTY_FIELD) {
            playingfield[to] = constants.WHITE;
            whiteMarkers--;
            turn = constants.BLACK;
            return true;
        }

        //Not the right players turn
        if (playingfield[from] != turn) {
            return false;
        }

        //Not a valid move
        if (!isValidMove(from, to)) {
            return false;
        }

        // Move the marker to it's new position
        playingfield[to] = playingfield[from];
        playingfield[from] = EMPTY_FIELD;

        // Change turn
        if (turn == constants.WHITE) {
            turn = constants.BLACK;
        } else {
            turn = constants.WHITE;
        }

        return true;
    }

    public void changeTurn(int player) {
        turn = player;
    }

    public void setBlackMarkers(int blackMarkers) {
        this.blackMarkers = blackMarkers;
    }

    public void setWhiteMarkers(int whiteMarkers) {
        this.whiteMarkers = whiteMarkers;
    }

    public int getBlackMarkers() {
        return blackMarkers;
    }

    public int getWhiteMarkers() {
        return whiteMarkers;
    }

    public boolean isThisBallSuspect(int color, int position) {
        Log.i("12312313", position + " : " + playingfield[position] + " : color = " + color);
        return playingfield[position] == color;
    }

    /**
     * Is it a valid move?
     *
     * @param from The area the checker is at.
     * @param to   The area the checker wants to go.
     * @return True if it's a valid move, else false is returned.
     */
    public boolean isValidMove(int from, int to) {
        //The "to"-field needs to be empty
        if (playingfield[to] != EMPTY_FIELD) {
            return false;
        }

        //If it is from the side board, all moves are valid.
        if (from == 0) {
            return true;
        }

        //If it is flying phase, all moves are valid.
        if (isItFlyingPhase(playingfield[from])) {
            Log.i("123123", "Flying phase is true boss");
            return true;
        }

        //Can only move to it's neighbors.
        switch (to) {
            case 1:
                return (from == 10 || from == 2);
            case 2:
                return (from == 1 || from == 3 || from == 5);
            case 3:
                return (from == 2 || from == 15);
            case 4:
                return (from == 5 || from == 11);
            case 5:
                return (from == 2 || from == 4 || from == 8 || from == 6);
            case 6:
                return (from == 5 || from == 14);
            case 7:
                return (from == 8 || from == 12);
            case 8:
                return (from == 5 || from == 7 || from == 9);
            case 9:
                return (from == 8 || from == 13);
            case 10:
                return (from == 1 || from == 11 || from == 22);
            case 11:
                return (from == 4 || from == 10 || from == 12 || from == 19);
            case 12:
                return (from == 7 || from == 11 || from == 16);
            case 13:
                return (from == 9 || from == 14 || from == 18);
            case 14:
                return (from == 6 || from == 13 || from == 15 || from == 21);
            case 15:
                return (from == 3 || from == 14 || from == 24);
            case 16:
                return (from == 12 || from == 17);
            case 17:
                return (from == 16 || from == 18 || from == 20);
            case 18:
                return (from == 13 || from == 17);
            case 19:
                return (from == 11 || from == 20);
            case 20:
                return (from == 17 || from == 19 || from == 21 || from == 23);
            case 21:
                return (from == 14 || from == 20);
            case 22:
                return (from == 10 || from == 23);
            case 23:
                return (from == 20 || from == 22 || from == 24);
            case 24:
                return (from == 15 || from == 23);
        }
        return false;
    }


    public boolean canBotBallMoveToPosition(int position, int ballOnGrid) {
        int from = position;
        Log.i("asdasdad1", "BallOnGrid = " + ballOnGrid);
        switch (ballOnGrid) {
            case 1:
                return (from == 10 || from == 2);
            case 2:
                return (from == 1 || from == 3 || from == 5);
            case 3:
                return (from == 2 || from == 15);
            case 4:
                return (from == 5 || from == 11);
            case 5:
                return (from == 2 || from == 4 || from == 8 || from == 6);
            case 6:
                return (from == 5 || from == 14);
            case 7:
                return (from == 8 || from == 12);
            case 8:
                return (from == 5 || from == 7 || from == 9);
            case 9:
                return (from == 8 || from == 13);
            case 10:
                return (from == 1 || from == 11 || from == 22);
            case 11:
                return (from == 4 || from == 10 || from == 12 || from == 19);
            case 12:
                return (from == 7 || from == 11 || from == 16);
            case 13:
                return (from == 9 || from == 14 || from == 18);
            case 14:
                return (from == 6 || from == 13 || from == 15 || from == 21);
            case 15:
                return (from == 3 || from == 14 || from == 24);
            case 16:
                return (from == 12 || from == 17);
            case 17:
                return (from == 16 || from == 18 || from == 20);
            case 18:
                return (from == 13 || from == 17);
            case 19:
                return (from == 11 || from == 20);
            case 20:
                return (from == 17 || from == 19 || from == 21 || from == 23);
            case 21:
                return (from == 14 || from == 20);
            case 22:
                return (from == 10 || from == 23);
            case 23:
                return (from == 20 || from == 22 || from == 24);
            case 24:
                return (from == 15 || from == 23);
        }
        return false;
    }

    public Integer isValidMove_BOT(int from) {
        Log.i("13adad", "Ball passed = " + from);

        /*
         * The game board positions
         *
         * 01           02           03
         *     04       05       06
         *         07   08   09
         * 10  11  12        13  14  15
         *         16   17   18
         *     19       20       21
         * 22           23           24
         *
         */


        switch (from) {
            case 1:
                if (playingfield[10] == EMPTY_FIELD) {
                    return 9;
                } else if (playingfield[2] == EMPTY_FIELD) {
                    return 1;
                }
                break;
            case 2:
                if (playingfield[1] == EMPTY_FIELD) {
                    return 0;
                } else if (playingfield[3] == EMPTY_FIELD) {
                    return 2;
                } else if (playingfield[5] == EMPTY_FIELD) {
                    return 4;
                }
                break;
            case 3:
                if (playingfield[2] == EMPTY_FIELD) {
                    return 1;
                } else if (playingfield[15] == EMPTY_FIELD) {
                    return 14;
                }
                break;
            case 4:
                if (playingfield[5] == EMPTY_FIELD) {
                    return 4;
                } else if (playingfield[11] == EMPTY_FIELD) {
                    return 10;
                }
                break;
            case 5:
                if (playingfield[2] == EMPTY_FIELD) {
                    return 1;
                } else if (playingfield[4] == EMPTY_FIELD) {
                    return 3;
                } else if (playingfield[8] == EMPTY_FIELD) {
                    return 7;
                } else if (playingfield[6] == EMPTY_FIELD) {
                    return 5;
                }
                break;
            case 6:
                if (playingfield[5] == EMPTY_FIELD) {
                    return 4;
                } else if (playingfield[14] == EMPTY_FIELD) {
                    return 13;
                }
                break;
            case 7:
                if (playingfield[8] == EMPTY_FIELD) {
                    return 7;
                } else if (playingfield[12] == EMPTY_FIELD) {
                    return 11;
                }
                break;
            case 8:
                if (playingfield[5] == EMPTY_FIELD) {
                    return 4;
                } else if (playingfield[7] == EMPTY_FIELD) {
                    return 6;
                } else if (playingfield[9] == EMPTY_FIELD) {
                    return 8;
                }
                break;
            case 9:
                if (playingfield[8] == EMPTY_FIELD) {
                    return 7;
                } else if (playingfield[13] == EMPTY_FIELD) {
                    return 12;
                }
                break;
            case 10:
                if (playingfield[1] == EMPTY_FIELD) {
                    return 0;
                } else if (playingfield[11] == EMPTY_FIELD) {
                    return 10;
                } else if (playingfield[22] == EMPTY_FIELD) {
                    return 21;
                }
                break;
            case 11:
                if (playingfield[4] == EMPTY_FIELD) {
                    return 3;
                } else if (playingfield[10] == EMPTY_FIELD) {
                    return 9;
                } else if (playingfield[12] == EMPTY_FIELD) {
                    return 11;
                } else if (playingfield[19] == EMPTY_FIELD) {
                    return 18;
                }
                break;
            case 12:
                if (playingfield[7] == EMPTY_FIELD) {
                    return 6;
                } else if (playingfield[11] == EMPTY_FIELD) {
                    return 10;
                } else if (playingfield[16] == EMPTY_FIELD) {
                    return 15;
                }
                break;
            case 13:
                if (playingfield[9] == EMPTY_FIELD) {
                    return 8;
                } else if (playingfield[14] == EMPTY_FIELD) {
                    return 13;
                } else if (playingfield[18] == EMPTY_FIELD) {
                    return 17;
                }
                break;
            case 14:
                if (playingfield[6] == EMPTY_FIELD) {
                    return 5;
                } else if (playingfield[13] == EMPTY_FIELD) {
                    return 12;
                } else if (playingfield[15] == EMPTY_FIELD) {
                    return 14;
                } else if (playingfield[21] == EMPTY_FIELD) {
                    return 20;
                }
                break;
            case 15:

                if (playingfield[3] == EMPTY_FIELD) {
                    return 2;
                } else if (playingfield[14] == EMPTY_FIELD) {
                    return 13;
                } else if (playingfield[24] == EMPTY_FIELD) {
                    return 23;
                }
                break;
            case 16:
                if (playingfield[12] == EMPTY_FIELD) {
                    return 11;
                } else if (playingfield[17] == EMPTY_FIELD) {
                    return 16;
                }
                break;
            case 17:

                if (playingfield[16] == EMPTY_FIELD) {
                    return 15;
                } else if (playingfield[18] == EMPTY_FIELD) {
                    return 17;
                } else if (playingfield[20] == EMPTY_FIELD) {
                    return 19;
                }
                break;

            case 18:

                if (playingfield[13] == EMPTY_FIELD) {
                    return 12;
                } else if (playingfield[17] == EMPTY_FIELD) {
                    return 16;
                }
                break;

            case 19:

                if (playingfield[11] == EMPTY_FIELD) {
                    return 10;
                } else if (playingfield[20] == EMPTY_FIELD) {
                    return 19;
                }

                break;
            case 20:
                if (playingfield[17] == EMPTY_FIELD) {
                    return 16;
                } else if (playingfield[19] == EMPTY_FIELD) {
                    return 18;
                } else if (playingfield[21] == EMPTY_FIELD) {
                    return 20;
                } else if (playingfield[23] == EMPTY_FIELD) {
                    return 22;
                }
                break;
            case 21:
                if (playingfield[14] == EMPTY_FIELD) {
                    return 13;
                } else if (playingfield[20] == EMPTY_FIELD) {
                    return 19;
                }
                break;
            case 22:
                if (playingfield[10] == EMPTY_FIELD) {
                    return 9;
                } else if (playingfield[23] == EMPTY_FIELD) {
                    return 22;
                }
                break;
            case 23:

                if (playingfield[20] == EMPTY_FIELD) {
                    return 19;
                } else if (playingfield[22] == EMPTY_FIELD) {
                    return 21;
                } else if (playingfield[24] == EMPTY_FIELD) {
                    return 23;
                }
                break;

            case 24:
                if (playingfield[15] == EMPTY_FIELD) {
                    return 14;
                } else if (playingfield[23] == EMPTY_FIELD) {
                    return 22;
                }
                break;
        }
        return null;
    }

    public Boolean isOpponentHasBalls(int color) {
        for (int i : playingfield) {
            if (i == color) {
                return true;
            }
        }
        return false;
    }

    public int countPlayerBalls(int color) {
        int count = 0;
        for (int i : playingfield) {
            if (i == color) {
                count++;
            }
        }
        return count;
    }

    public Boolean isPlayerHasQath(int opponentColor) {
        Boolean someBool = false;
        if (!(playingfield[1] == playingfield[2] && playingfield[2] == playingfield[3] && opponentColor == playingfield[3] && playingfield[3] != 0)) {
            someBool = true;
        }
        if (!(playingfield[4] == playingfield[5] && playingfield[5] == playingfield[6] && opponentColor == playingfield[6] && playingfield[6] != 0)) {
            someBool = true;
        }
        if (!(playingfield[7] == playingfield[8] && playingfield[8] == playingfield[9] && opponentColor == playingfield[9] && playingfield[9] != 0)) {
            someBool = true;
        }
        if (!(playingfield[10] == playingfield[11] && playingfield[11] == playingfield[12] && opponentColor == playingfield[12] && playingfield[12] != 0)) {
            someBool = true;
        }
        if (!(playingfield[13] == playingfield[14] && playingfield[14] == playingfield[15] && opponentColor == playingfield[15] && playingfield[15] != 0)) {
            someBool = true;
        }
        if (!(playingfield[16] == playingfield[17] && playingfield[17] == playingfield[18] && opponentColor == playingfield[18] && playingfield[18] != 0)) {
            someBool = true;
        }
        if (!(playingfield[19] == playingfield[20] && playingfield[20] == playingfield[21] && opponentColor == playingfield[21] && playingfield[21] != 0)) {
            someBool = true;
        }
        if (!(playingfield[22] == playingfield[23] && playingfield[23] == playingfield[24] && opponentColor == playingfield[24] && playingfield[24] != 0)) {
            someBool = true;
        }
        if (!(playingfield[1] == playingfield[10] && playingfield[10] == playingfield[22] && opponentColor == playingfield[22] && playingfield[22] != 0)) {
            someBool = true;
        }
        if (!(playingfield[4] == playingfield[11] && playingfield[11] == playingfield[19] && opponentColor == playingfield[19] && playingfield[19] != 0)) {
            someBool = true;
        }
        if (!(playingfield[7] == playingfield[12] && playingfield[12] == playingfield[16] && opponentColor == playingfield[16] && playingfield[16] != 0)) {
            someBool = true;
        }
        if (!(playingfield[2] == playingfield[5] && playingfield[5] == playingfield[8] && opponentColor == playingfield[8]) && playingfield[18] != 0) {
            someBool = true;
        }
        if (!(playingfield[17] == playingfield[20] && playingfield[20] == playingfield[23] && opponentColor == playingfield[23] && playingfield[23] != 0)) {
            someBool = true;
        }
        if (!(playingfield[9] == playingfield[13] && playingfield[13] == playingfield[18] && opponentColor == playingfield[18] && playingfield[18] != 0)) {
            someBool = true;
        }
        if (!(playingfield[6] == playingfield[14] && playingfield[14] == playingfield[21] && opponentColor == playingfield[21] && playingfield[21] != 0)) {
            someBool = true;
        }
        if (!(playingfield[3] == playingfield[15] && playingfield[15] == playingfield[24] && opponentColor == playingfield[24] && playingfield[24] != 0)) {
            someBool = true;
        }
        return someBool;
    }

    /**
     * Check if the player is allowed to remove a checker from the other player.
     *
     * @param partOfLine The position of the checker.
     * @return True if the checker is part of a line, else return false.
     */

    public boolean canRemove(int partOfLine, int opponentColor) {

        /*
         * The game board positions
         *
         * 01           02           03
         *     04       05       06
         *         07   08   09
         * 10  11  12        13  14  15
         *         16   17   18
         *     19       20       21
         * 22           23           24
         *
         */

        Log.i("someLog", "partOfLine " + partOfLine + " and opponentColor " + opponentColor);
        if (!isOpponentHasBalls(opponentColor)) {
            return false;
        }

        // Check if the argument is part of a line on the board
        if (playingfield[partOfLine] == EMPTY_FIELD) {
            return false;
        }

        //All possible lines.
        if ((partOfLine == 1 || partOfLine == 2 || partOfLine == 3) && (playingfield[1] == playingfield[2] && playingfield[2] == playingfield[3])) {
            return true; // return isPlayerHasQath(opponentColor);
        }
        if ((partOfLine == 4 || partOfLine == 5 || partOfLine == 6) && (playingfield[4] == playingfield[5] && playingfield[5] == playingfield[6])) {
            return true;
        }
        if ((partOfLine == 7 || partOfLine == 8 || partOfLine == 9) && (playingfield[7] == playingfield[8] && playingfield[8] == playingfield[9])) {
            return true;
        }
        if ((partOfLine == 10 || partOfLine == 11 || partOfLine == 12) && (playingfield[10] == playingfield[11] && playingfield[11] == playingfield[12])) {
            return true;
        }
        if ((partOfLine == 13 || partOfLine == 14 || partOfLine == 15) && (playingfield[13] == playingfield[14] && playingfield[14] == playingfield[15])) {
            return true;
        }
        if ((partOfLine == 16 || partOfLine == 17 || partOfLine == 18) && (playingfield[16] == playingfield[17] && playingfield[17] == playingfield[18])) {
            return true;
        }
        if ((partOfLine == 19 || partOfLine == 20 || partOfLine == 21) && (playingfield[19] == playingfield[20] && playingfield[20] == playingfield[21])) {
            return true;
        }
        if ((partOfLine == 22 || partOfLine == 23 || partOfLine == 24) && (playingfield[22] == playingfield[23] && playingfield[23] == playingfield[24])) {
            return true;
        }
        if ((partOfLine == 1 || partOfLine == 10 || partOfLine == 22) && (playingfield[1] == playingfield[10] && playingfield[10] == playingfield[22])) {
            return true;
        }
        if ((partOfLine == 4 || partOfLine == 11 || partOfLine == 19) && (playingfield[4] == playingfield[11] && playingfield[11] == playingfield[19])) {
            return true;
        }
        if ((partOfLine == 7 || partOfLine == 12 || partOfLine == 16) && (playingfield[7] == playingfield[12] && playingfield[12] == playingfield[16])) {
            return true;
        }
        if ((partOfLine == 2 || partOfLine == 5 || partOfLine == 8) && (playingfield[2] == playingfield[5] && playingfield[5] == playingfield[8])) {
            return true;
        }
        if ((partOfLine == 17 || partOfLine == 20 || partOfLine == 23) && (playingfield[17] == playingfield[20] && playingfield[20] == playingfield[23])) {
            return true;
        }
        if ((partOfLine == 9 || partOfLine == 13 || partOfLine == 18) && (playingfield[9] == playingfield[13] && playingfield[13] == playingfield[18])) {
            return true;
        }
        if ((partOfLine == 6 || partOfLine == 14 || partOfLine == 21) && (playingfield[6] == playingfield[14] && playingfield[14] == playingfield[21])) {
            return true;
        }
        if ((partOfLine == 3 || partOfLine == 15 || partOfLine == 24) && (playingfield[3] == playingfield[15] && playingfield[15] == playingfield[24])) {
            return true;
        }
        return false;
    }

    public void printGrid() {
        for (int i = 0; i < playingfield.length; i++) {
            Log.i("123213", "i = " + i + " and value = " + playingfield[i] + "");
        }
        Log.i("123213", "----------------------------------------------------------------------------");
    }

    public boolean canRemove(int partOfLine) {
        // Check if the argument is part of a line on the board
        if (playingfield[partOfLine] == EMPTY_FIELD) {
            return false;
        }


        /*
         * The game board positions
         *
         * 01           02           03
         *     04       05       06
         *         07   08   09
         * 10  11  12        13  14  15
         *         16   17   18
         *     19       20       21
         * 22           23           24
         *
         */


        //All possible lines.
        if ((partOfLine == 1 || partOfLine == 2 || partOfLine == 3) && (playingfield[1] == playingfield[2] && playingfield[2] == playingfield[3])) {
            return true;
        }
        if ((partOfLine == 4 || partOfLine == 5 || partOfLine == 6) && (playingfield[4] == playingfield[5] && playingfield[5] == playingfield[6])) {
            return true;
        }
        if ((partOfLine == 7 || partOfLine == 8 || partOfLine == 9) && (playingfield[7] == playingfield[8] && playingfield[8] == playingfield[9])) {
            return true;
        }
        if ((partOfLine == 10 || partOfLine == 11 || partOfLine == 12) && (playingfield[10] == playingfield[11] && playingfield[11] == playingfield[12])) {
            return true;
        }
        if ((partOfLine == 13 || partOfLine == 14 || partOfLine == 15) && (playingfield[13] == playingfield[14] && playingfield[14] == playingfield[15])) {
            return true;
        }
        if ((partOfLine == 16 || partOfLine == 17 || partOfLine == 18) && (playingfield[16] == playingfield[17] && playingfield[17] == playingfield[18])) {
            return true;
        }
        if ((partOfLine == 19 || partOfLine == 20 || partOfLine == 21) && (playingfield[19] == playingfield[20] && playingfield[20] == playingfield[21])) {
            return true;
        }
        if ((partOfLine == 22 || partOfLine == 23 || partOfLine == 24) && (playingfield[22] == playingfield[23] && playingfield[23] == playingfield[24])) {
            return true;
        }
        if ((partOfLine == 1 || partOfLine == 10 || partOfLine == 22) && (playingfield[1] == playingfield[10] && playingfield[10] == playingfield[22])) {
            return true;
        }
        if ((partOfLine == 4 || partOfLine == 11 || partOfLine == 19) && (playingfield[4] == playingfield[11] && playingfield[11] == playingfield[19])) {
            return true;
        }
        if ((partOfLine == 7 || partOfLine == 12 || partOfLine == 16) && (playingfield[7] == playingfield[12] && playingfield[12] == playingfield[16])) {
            return true;
        }
        if ((partOfLine == 2 || partOfLine == 5 || partOfLine == 8) && (playingfield[2] == playingfield[5] && playingfield[5] == playingfield[8])) {
            return true;
        }
        if ((partOfLine == 17 || partOfLine == 20 || partOfLine == 23) && (playingfield[17] == playingfield[20] && playingfield[20] == playingfield[23])) {
            return true;
        }
        if ((partOfLine == 9 || partOfLine == 13 || partOfLine == 18) && (playingfield[9] == playingfield[13] && playingfield[13] == playingfield[18])) {
            return true;
        }
        if ((partOfLine == 6 || partOfLine == 14 || partOfLine == 21) && (playingfield[6] == playingfield[14] && playingfield[14] == playingfield[21])) {
            return true;
        }
        if ((partOfLine == 3 || partOfLine == 15 || partOfLine == 24) && (playingfield[3] == playingfield[15] && playingfield[15] == playingfield[24])) {
            return true;
        }
        return false;
    }

    /**
     * Remove a marker from the position if it matches the color
     *
     * @param from  The checker to be removed.
     * @param color The color the checker should be if the remove is valid.
     * @return True if the removal was successful, else false is returned.
     */
    public boolean remove(int from, int color) {
        Log.i("someLoase", "from " + from + " color " + color);

        /*
         * The game board positions
         *
         * 01           02           03
         *     04       05       06
         *         07   08   09
         * 10  11  12        13  14  15
         *         16   17   18
         *     19       20       21
         * 22           23           24
         *
         *
         */


        for (int i = 1; i < playingfield.length; i++) {
            Log.i("akdoad", i + " : " + playingfield[i]);
        }

        if (checkIfBallInQath(from)) {
            Log.i("23dasad", "checkIfBallInQath ::::: true");
            if (playerEveryBallIsInQath(color)) {
                Log.i("23dasad", "playerEveryBallIsInQath ::::: true");
                if (playingfield[from] == color) {
                    playingfield[from] = EMPTY_FIELD;
                    return true;
                } else {
                    return false;
                }
            } else {
                Log.i("23dasad", "playerEveryBallIsInQath ::::: false");
                return false;
            }
        } else {
            Log.i("23dasad", "checkIfBallInQath ::::: false");
            if (playingfield[from] == color) {
                playingfield[from] = EMPTY_FIELD;
                return true;
            } else {
                return false;
            }
        }
    }

    public Boolean playerEveryBallIsInQath(int color) {
        for (int i = 1; i < playingfield.length; i++) {
            if (playingfield[i] == color) {
                Log.i("23dasad", i + " -- " + checkIfBallInQath(i));
                if (!checkIfBallInQath(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Boolean checkIfBallInQath(int from) {
        Boolean whatToReturn = false;
        switch (from) {
            case 1:
                if (playingfield[1] == playingfield[10] && playingfield[10] == playingfield[22] && playingfield[from] == playingfield[22] && playingfield[22] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[1] == playingfield[2] && playingfield[2] == playingfield[3] && playingfield[from] == playingfield[3] && playingfield[3] != 0) {
                    whatToReturn = true;
                }
                break;
            case 2:
                if (playingfield[2] == playingfield[5] && playingfield[5] == playingfield[8] && playingfield[from] == playingfield[8] && playingfield[8] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[1] == playingfield[2] && playingfield[2] == playingfield[3] && playingfield[from] == playingfield[3] && playingfield[3] != 0) {
                    whatToReturn = true;
                }
                break;
            case 3:
                if (playingfield[3] == playingfield[15] && playingfield[15] == playingfield[24] && playingfield[from] == playingfield[24] && playingfield[24] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[1] == playingfield[2] && playingfield[2] == playingfield[3] && playingfield[from] == playingfield[3] && playingfield[3] != 0) {
                    whatToReturn = true;
                }
                break;
            case 4:
                if (playingfield[4] == playingfield[11] && playingfield[11] == playingfield[19] && playingfield[from] == playingfield[19] && playingfield[19] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[4] == playingfield[5] && playingfield[5] == playingfield[6] && playingfield[from] == playingfield[6] && playingfield[6] != 0) {
                    whatToReturn = true;
                }
                break;
            case 5:
                if (playingfield[2] == playingfield[5] && playingfield[5] == playingfield[8] && playingfield[from] == playingfield[8] && playingfield[8] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[4] == playingfield[5] && playingfield[5] == playingfield[6] && playingfield[from] == playingfield[6] && playingfield[6] != 0) {
                    whatToReturn = true;
                }
                break;
            case 6:
                if (playingfield[6] == playingfield[14] && playingfield[14] == playingfield[21] && playingfield[from] == playingfield[21] && playingfield[21] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[4] == playingfield[5] && playingfield[5] == playingfield[6] && playingfield[from] == playingfield[6] && playingfield[6] != 0) {
                    whatToReturn = true;
                }
                break;
            case 7:
                if (playingfield[7] == playingfield[12] && playingfield[12] == playingfield[16] && playingfield[from] == playingfield[16] && playingfield[16] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[7] == playingfield[8] && playingfield[8] == playingfield[9] && playingfield[from] == playingfield[9] && playingfield[9] != 0) {
                    whatToReturn = true;
                }
                break;
            case 8:
                if (playingfield[2] == playingfield[5] && playingfield[5] == playingfield[8] && playingfield[from] == playingfield[8] && playingfield[8] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[7] == playingfield[8] && playingfield[8] == playingfield[9] && playingfield[from] == playingfield[9] && playingfield[9] != 0) {
                    whatToReturn = true;
                }
                break;
            case 9:
                if (playingfield[9] == playingfield[13] && playingfield[13] == playingfield[18] && playingfield[from] == playingfield[18] && playingfield[18] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[7] == playingfield[8] && playingfield[8] == playingfield[9] && playingfield[from] == playingfield[9] && playingfield[9] != 0) {
                    whatToReturn = true;
                }
                break;

            case 10:
                if (playingfield[1] == playingfield[10] && playingfield[10] == playingfield[22] && playingfield[from] == playingfield[22] && playingfield[22] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[10] == playingfield[11] && playingfield[11] == playingfield[12] && playingfield[from] == playingfield[12] && playingfield[12] != 0) {
                    whatToReturn = true;
                }
                break;
            case 11:
                if (playingfield[4] == playingfield[11] && playingfield[11] == playingfield[19] && playingfield[from] == playingfield[19] && playingfield[19] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[10] == playingfield[11] && playingfield[11] == playingfield[12] && playingfield[from] == playingfield[12] && playingfield[12] != 0) {
                    whatToReturn = true;
                }
                break;
            case 12:
                if (playingfield[7] == playingfield[12] && playingfield[12] == playingfield[16] && playingfield[from] == playingfield[16] && playingfield[16] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[10] == playingfield[11] && playingfield[11] == playingfield[12] && playingfield[from] == playingfield[12] && playingfield[12] != 0) {
                    whatToReturn = true;
                }
                break;

            case 13:
                if (playingfield[9] == playingfield[13] && playingfield[13] == playingfield[18] && playingfield[from] == playingfield[18] && playingfield[18] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[13] == playingfield[14] && playingfield[14] == playingfield[15] && playingfield[from] == playingfield[15] && playingfield[15] != 0) {
                    whatToReturn = true;
                }
                break;
            case 14:
                if (playingfield[6] == playingfield[14] && playingfield[14] == playingfield[21] && playingfield[from] == playingfield[21] && playingfield[21] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[13] == playingfield[14] && playingfield[14] == playingfield[15] && playingfield[from] == playingfield[15] && playingfield[15] != 0) {
                    whatToReturn = true;
                }
                break;
            case 15:
                if (playingfield[3] == playingfield[15] && playingfield[15] == playingfield[24] && playingfield[from] == playingfield[24] && playingfield[24] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[13] == playingfield[14] && playingfield[14] == playingfield[15] && playingfield[from] == playingfield[15] && playingfield[15] != 0) {
                    whatToReturn = true;
                }
                break;

            case 16:
                if (playingfield[7] == playingfield[12] && playingfield[12] == playingfield[16] && playingfield[from] == playingfield[16] && playingfield[16] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[16] == playingfield[17] && playingfield[17] == playingfield[18] && playingfield[from] == playingfield[18] && playingfield[18] != 0) {
                    whatToReturn = true;
                }
                break;
            case 17:
                if (playingfield[17] == playingfield[20] && playingfield[20] == playingfield[23] && playingfield[from] == playingfield[23] && playingfield[23] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[16] == playingfield[17] && playingfield[17] == playingfield[18] && playingfield[from] == playingfield[18] && playingfield[18] != 0) {
                    whatToReturn = true;
                }
                break;
            case 18:
                if (playingfield[9] == playingfield[13] && playingfield[13] == playingfield[18] && playingfield[from] == playingfield[18] && playingfield[18] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[16] == playingfield[17] && playingfield[17] == playingfield[18] && playingfield[from] == playingfield[18] && playingfield[18] != 0) {
                    whatToReturn = true;
                }
                break;

            case 19:
                if (playingfield[4] == playingfield[11] && playingfield[11] == playingfield[19] && playingfield[from] == playingfield[19] && playingfield[19] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[19] == playingfield[20] && playingfield[20] == playingfield[21] && playingfield[from] == playingfield[21] && playingfield[21] != 0) {
                    whatToReturn = true;
                }
                break;
            case 20:
                if (playingfield[17] == playingfield[20] && playingfield[20] == playingfield[23] && playingfield[from] == playingfield[23] && playingfield[23] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[19] == playingfield[20] && playingfield[20] == playingfield[21] && playingfield[from] == playingfield[21] && playingfield[21] != 0) {
                    whatToReturn = true;
                }
                break;
            case 21:
                if (playingfield[6] == playingfield[14] && playingfield[14] == playingfield[21] && playingfield[from] == playingfield[21] && playingfield[21] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[19] == playingfield[20] && playingfield[20] == playingfield[21] && playingfield[from] == playingfield[21] && playingfield[21] != 0) {
                    whatToReturn = true;
                }
                break;

            case 22:
                if (playingfield[1] == playingfield[10] && playingfield[10] == playingfield[22] && playingfield[from] == playingfield[22] && playingfield[22] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[22] == playingfield[23] && playingfield[23] == playingfield[24] && playingfield[from] == playingfield[24] && playingfield[24] != 0) {
                    whatToReturn = true;
                }
                break;
            case 23:
                if (playingfield[17] == playingfield[20] && playingfield[20] == playingfield[23] && playingfield[from] == playingfield[23] && playingfield[23] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[22] == playingfield[23] && playingfield[23] == playingfield[24] && playingfield[from] == playingfield[24] && playingfield[24] != 0) {
                    whatToReturn = true;
                }
                break;
            case 24:
                if (playingfield[3] == playingfield[15] && playingfield[15] == playingfield[24] && playingfield[from] == playingfield[24] && playingfield[24] != 0) {
                    whatToReturn = true;
                }
                if (playingfield[22] == playingfield[23] && playingfield[23] == playingfield[24] && playingfield[from] == playingfield[24] && playingfield[24] != 0) {
                    whatToReturn = true;
                }
                break;
        }
        return whatToReturn;
    }

    public void printData() {
        Log.i("online_random", "White balls = " + whiteMarkers + " AND Black balls = " + blackMarkers + " EMPTY = " + EMPTY_FIELD);
        for (int i = 0; i < playingfield.length; i++) {
            Log.i("online_random", "POSITION = " + i + " AND Player = " + playingfield[i]);
        }
    }

    /**
     * Check if color 'color' has lost
     *
     * @param color The color which may have lost.
     * @return True if color has lost, else false is returned.
     */
    public boolean isItAWin(int color) {
        //A player can't win if it is checker left on the sideboard.
        // method for checking no ball on grid and win

        if (color == constants.WHITE) {
            if (whiteMarkers + countPlayerBalls(color) < 3) {
                return true;
            }
        } else {
            if (blackMarkers + countPlayerBalls(color) < 3) {
                return true;
            }
        }

        if (whiteMarkers > 0 || blackMarkers > 0) {
            Log.i("online_random", "POINT E " + whiteMarkers + " :: " + blackMarkers);
            return false;
        }

        //color lost if there is no valid moves
        if (!hasValidMoves(color)) {
            Log.i("online_random", "POINT F " + whiteMarkers + " :: " + blackMarkers);
            return true;
        }

        //Does the color have less then 3 checkers left?
        int count = 0;
        for (int i : playingfield) {
            if (i == color) {
                count++;
            }
        }
        Log.i("online_random", "POINT G " + whiteMarkers + " :: " + blackMarkers);
        return (count < 3);
    }

    private boolean hasValidMoves(int color) {
        for (int i = 0; i < 24; i++) {
            if (playingfield[i + 1] == color) {
                Log.i("online_random", "found color: " + color);
                for (int j = 0; j < 24; j++) {
                    if (isValidMove(i + 1, j + 1)) {
                        Log.i("online_random", "Has valid moves");
                        return true;
                    }
                }
            }
        }
        Log.i("online_random", "Doesn't have valid moves");
        return false;
    }

    /**
     * @param color The color which may be in the flying phase.
     * @return True if it has exactly 3 checkers left, else return false.
     */
    private boolean isItFlyingPhase(int color) {

        int count = 0;
        for (int i : playingfield) {
            if (i == color) {
                count++;
            }
        }
        return (count == 3);
    }

    /**
     * @param field The field to be checked.
     * @return The color the checker on a field is.
     */
    public int fieldColor(int field) {
        return playingfield[field];
    }

    /**
     * @return The player whos turn it is.
     */
    public int getTurn() {
        return turn;
    }

}