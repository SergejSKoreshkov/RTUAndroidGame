package james.asteroid.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import james.asteroid.R;
import james.asteroid.activities.MainActivity;
import james.asteroid.data.AsteroidData;
import james.asteroid.data.drawer.AsteroidDrawer;
import james.asteroid.utils.ImageUtils;

import static android.app.PendingIntent.getActivity;

public class GameView extends SurfaceView implements Runnable, View.OnTouchListener {

    private Paint paint;
    private Paint accentPaint;
    private SurfaceHolder surfaceHolder;
    private boolean isRunning;
    private Thread thread;

    private Bitmap shipBitmap;
    private float shipPositionX = 0.5f;
    private float shipPositionY = -1f;
    private float shipPositionStartX;
    private float shipRotation;

    private AsteroidDrawer asteroids;

    private ValueAnimator animator;
    private boolean isPlaying;
    public int score;
    private float speed = 1;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        surfaceHolder = getHolder();

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        accentPaint = new Paint();
        accentPaint.setColor(Color.RED);
        accentPaint.setStyle(Paint.Style.FILL);
        accentPaint.setAntiAlias(true);

        asteroids = new AsteroidDrawer(getContext(), Color.RED, Color.RED, paint, accentPaint);

        shipBitmap = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(getContext(), R.drawable.ic_ship), Color.BLUE, Color.BLUE);
    }

    @Override
    public void run() {
        while (isRunning) {
            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas == null)
                    return;

                canvas.drawColor(Color.BLACK);

                asteroids.draw(canvas, speed);

                float left = canvas.getWidth() * shipPositionX;
                float top = canvas.getHeight() - (shipBitmap.getHeight() * shipPositionY);

                Matrix matrix = new Matrix();
                matrix.postTranslate(-shipBitmap.getWidth() / 2, -shipBitmap.getHeight() / 2);
                matrix.postRotate(shipRotation);
                matrix.postTranslate(left, top);
                canvas.drawBitmap(shipBitmap, matrix, paint);

                Rect position = new Rect(
                        (int) left - (shipBitmap.getWidth() / 2),
                        (int) top - (shipBitmap.getWidth() / 2),
                        (int) left + (shipBitmap.getWidth() / 2),
                        (int) top + (shipBitmap.getWidth() / 2)
                );

                if (isPlaying) {
                    AsteroidData asteroid = asteroids.asteroidAt(position);
                    if (asteroid != null) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                stop();
                            });
                    }
                }

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void play() {
        isPlaying = true;
        score = 0;
        speed = 2;
        shipPositionX = 0.5f;
        shipRotation = 0;
        asteroids.setMakeAsteroids(true);

        if (animator != null && animator.isStarted())
            animator.cancel();

        ValueAnimator animator = ValueAnimator.ofFloat(shipPositionY, 1);
        animator.setDuration(150);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> shipPositionY = (float) valueAnimator.getAnimatedValue());
        animator.start();

        setOnTouchListener(this);
    }

    public void stop() {
        isPlaying = false;
        setOnTouchListener(null);
        asteroids.setMakeAsteroids(false);

        animator.cancel();

        ValueAnimator animator = ValueAnimator.ofFloat(shipPositionY, -1f);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            shipPositionY = (float) valueAnimator.getAnimatedValue();
        });
        animator.start();

    }

    public boolean isPlaying() {
        return isPlaying;
    }


    public void onPause() {
        if (thread == null)
            return;

        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
        thread = null;
    }

    public void onResume() {
        if (thread != null)
            onPause();

        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (animator != null && animator.isStarted())
                    animator.cancel();

                if (event.getX() > getWidth() / 2) {
                    if (shipPositionX < 1)
                        animator = ValueAnimator.ofFloat(shipPositionX, shipPositionX + 1);
                    else return false;
                } else if (shipPositionX > 0)
                    animator = ValueAnimator.ofFloat(shipPositionX, shipPositionX - 1);

                animator.setDuration((long) (1000 / speed));
                animator.setStartDelay(50);
                animator.setInterpolator(new AccelerateInterpolator());
                animator.addUpdateListener(valueAnimator -> {
                    float newX = (float) valueAnimator.getAnimatedValue();
                    if (newX <= 0)
                        shipPositionX = 0;
                    else if (newX >= 1)
                        shipPositionX = 1;
                    else shipPositionX = newX;
                });

                animator.start();
                shipPositionStartX = shipPositionX;
                break;
            case MotionEvent.ACTION_UP:
                if (animator != null && animator.isStarted())
                    animator.cancel();

                float newX = shipPositionX + ((shipPositionX - shipPositionStartX) / 1.5f);
                if (newX <= 0)
                    newX = 0;
                else if (newX >= 1)
                    newX = 1;

                animator = ValueAnimator.ofFloat(shipPositionX, newX);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.setDuration((long) (500 / speed));
                animator.addUpdateListener(valueAnimator -> {
                    float newX1 = (float) valueAnimator.getAnimatedValue();
                    if (newX1 <= 0)
                        shipPositionX = 0;
                    else if (newX1 >= 1)
                        shipPositionX = 1;
                    else shipPositionX = newX1;
                });

                animator.start();
                break;
        }
        return true;
    }
}
