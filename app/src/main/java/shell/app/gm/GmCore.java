package shell.app.gm;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.mindrex.collection.Array;
import com.mindrex.debugger.Log;
import com.mindrex.standardassets.Color;
import com.mindrex.util.Sfx;
import com.mindrex.standardassets.StandardActivity;
import com.mindrex.util.vector.Pos2;

import java.security.SecureRandom;
import java.util.Random;

@Deprecated
public final class GmCore {
    private static final Holder holder;
    private static final Pos2 screenSize;

    static {
        holder = new Holder();
        screenSize = new Pos2(1080, 1920);
    }

    public static void prepare(Context ctx) {
        holder.attach(ctx);
    }

    public static void create(Activity act) {
        holder.gmCore.createView(act);
    }

    public static void pause() {
        holder.gameView.pause();
    }

    public static void resume() {
        holder.gameView.resume();
    }

    public static void destroy() {
        holder.gameView.destroy();
    }

    private final Context context;

    private GmCore(Context ctx) {
        this.context = ctx;
    }

    private void createView(Activity act) {
        //act.requestWindowFeature(Window.FEATURE_NO_TITLE);
        act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        act.getWindow().getDecorView().setSystemUiVisibility(StandardActivity.FULL_SCREEN_SETTING);
        act.getActionBar().hide();
        final Point a = new Point();
        act.getWindowManager().getDefaultDisplay().getSize(a);
        screenSize.x = (float) a.x;
        screenSize.y = (float) a.y;
        holder.attachView();
        act.setContentView(holder.gameView);
    }

    private static final class GmView extends com.mindrex.standardassets.GameView {
        /* Order: [y][x] x: gameBoard[0].length; y: gameBoard.length; */
        private static final byte[][] gameBoard;
        private static final Color[] pieceColors;

        private final Random random;
        private final int widthDp;
        private final int heightDp;
        private Piece currentPiece;

        static {
            gameBoard = new byte[30][15];
            Array.fill(gameBoard, (byte) -1);
            pieceColors = new Color[]{
                    Color.GREEN,
                    Color.MAGENTA,
                    Color.BLUE,
                    Color.CYAN,
                    Color.parse("#FFBF00"),
                    Color.GRAY,
                    Color.RED
            };
            Log.i("static init");
        }

        public GmView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.random = new SecureRandom();
            this.widthDp = densityDp / gameBoard[0].length;
            this.heightDp = densityDp / gameBoard.length;
            //setBackgroundColor(Color.WHITE.toInt());
            Log.i("init");
        }

        @Override
        protected final void onClick(Pos2 pos) {
            super.onClick(pos);
            if (isGameOver) restart();
            else if (!isPlaying) start();
            else {
                try {
                    if (pos.x < screenSize.x / 3) currentPiece.isMovingLeft = true;
                    else if (pos.x > (screenSize.y / 3) * 2) currentPiece.isMovingRight = true;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            Log.i("on click");
        }

        @Override
        protected final void Update() {
            super.Update();
            Log.i("update");
            Log.i("value", String.valueOf(gameBoard[10][10]));
            if (currentPiece != null) {
                fitOnGameBoard(currentPiece);
                if (!currentPiece.isMoving) currentPiece = null;
                else currentPiece.update();
            } else {
                currentPiece = new Piece(random);
                currentPiece.update();
                if (!currentPiece.isMoving) isGameOver = true;
            }
        }

        @Override
        protected final void Draw() {
            if (getHolder().getSurface().isValid()) {
                final Canvas canvas = getHolder().lockCanvas();
                if (isGameOver) {
                    paint.setTextSize(24);
                    paint.setColor(Color.WHITE.toInt());
                    canvas.drawText("Game Over", 20, 20, paint);
                } else for (byte y = 0; y < gameBoard.length; y++) {
                    for (byte x = 0; x < gameBoard[y].length; x++) {
                        final Rect r = new Rect(x * widthDp, y * heightDp, (x + 1) * widthDp, (y + 1) * heightDp);
                        final byte b = gameBoard[y][x];
                        if (b == -1) {
                            paint.setStyle(Paint.Style.FILL);
                            paint.setColor(Color.YELLOW.toInt());
                            canvas.drawRect(r, paint);
                        } else {
                            paint.setStyle(Paint.Style.FILL);
                            paint.setColor(pieceColors[b].toInt());
                            canvas.drawRect(r, paint);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setColor(Color.BLACK.toInt());
                            canvas.drawRect(r, paint);
                        }
                    }
                }
                getHolder().unlockCanvasAndPost(canvas);
            }
            Log.i("draw");
        }

        @Override
        protected final void Sleep() {
            try {
                Thread.sleep(800);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("sleep");
        }

        @Override
        protected final void GameOver() {
            super.GameOver();
            Log.i("game over");
        }

        private void fitOnGameBoard(Piece p) {
            final byte[][] a = p.toArrayMap();
            Log.i("value", String.valueOf(gameBoard[10][10]));
            for (byte y = 0; y < gameBoard.length; y++) {
                for (byte x = 0; x < gameBoard[y].length; x++) {
                    if (a[y][x] != 0) {
                        gameBoard[y][x] = a[y][x];
                    } else gameBoard[y][x] = -1;
                }
            }
            Log.i("fit on game board");
        }

        public final static class Piece {
            private static final boolean[][] PATTERN_SQUARE = new boolean[][]{
                    new boolean[]{true, true},
                    new boolean[]{true, true}
            };
            private static final boolean[][] PATTERN_Z = new boolean[][]{
                    new boolean[]{true, true, false},
                    new boolean[]{false, true, true}
            };
            private static final boolean[][] PATTERN_I = new boolean[][]{
                    new boolean[]{true},
                    new boolean[]{true},
                    new boolean[]{true},
                    new boolean[]{true}
            };
            private static final boolean[][] PATTERN_T = new boolean[][]{
                    new boolean[]{true, true, true},
                    new boolean[]{false, true, false},
                    new boolean[]{false, true, false}
            };
            private static final boolean[][] PATTERN_L = new boolean[][]{
                    new boolean[]{true, false},
                    new boolean[]{true, false},
                    new boolean[]{true, true}
            };

            private final boolean[][] pattern;
            private final int color;
            private final Pos2 pos;
            private boolean isMoving;
            private boolean isMovingLeft;
            private boolean isMovingRight;

            public Piece(Random random) {
                final boolean[][][] a = new boolean[][][]{
                        PATTERN_SQUARE,
                        PATTERN_Z,
                        PATTERN_I,
                        PATTERN_T,
                        PATTERN_L
                };
                this.pattern = Array.pickRandom(a, random);
                this.color = random.nextInt(pieceColors.length);
                this.pos = new Pos2(gameBoard[0].length / 2f, 0);
                this.isMoving = true;
                this.isMovingLeft = false;
                this.isMovingRight = false;
                Log.i("piece init");
            }

            public final byte[][] toArrayMap() {
                final byte[][] a = gameBoard;
                for (byte y = 0; y < pattern.length; y++) {
                    for (byte x = 0; x < pattern[y].length; x++) {
                        final int c = pos.y.intValue() + y;
                        int d = pos.x.intValue() + x;
                        if (d >= a[0].length) d = a[0].length;
                        if (a[c - 1][d] == (byte) color) a[c - 1][d] = -1;
                        if (c >= a.length - 1) isMoving = false;
                            //else if (gameBoard[c][d] > 0) isMoving = false;
                        else if (pattern[y][x]) {
                            a[c][d] = (byte) color;
                        } else a[c][d] = -1;
                    }
                }
                Log.i("to array map");
                return a;
            }

            public final void update() {
                if (isMoving) {
                    pos.y++;
                    if (isMovingLeft) {
                        isMovingLeft = false;
                        if (pos.x > 0) pos.x--;
                    } else if (isMovingRight) {
                        isMovingRight = false;
                        if (pos.x < pos.x + pattern[0].length) pos.x++;
                    }
                }
                Log.i("update");
            }
        }
    }

    private static final class Holder {
        private GmCore gmCore;
        private GmView gameView;

        private Holder() {
        }

        private void attach(Context ctx) {
            if (gmCore == null)
                gmCore = new GmCore(ctx);
        }

        private void attachView() {
            if (gmCore != null && gameView == null)
                gameView = new GmView(gmCore.context, null);
        }

        @NonNull
        @Override
        public final String toString() {
            return "Holder{" +
                    "gmCore=" + gmCore +
                    '}';
        }
    }
}