package james.asteroid.data.drawer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import james.asteroid.R;
import james.asteroid.data.AsteroidData;
import james.asteroid.data.DrawerData;
import james.asteroid.utils.ImageUtils;

public class AsteroidDrawer extends DrawerData {

    private List<AsteroidData> asteroids;
    private Bitmap asteroidBitmap;
    private Bitmap asteroidBitmap2;

    private long asteroidTime;
    private float asteroidLength;

    private boolean shouldMakeAsteroids;

    public AsteroidDrawer(Context context, int colorAccent, int colorPrimary, Paint asteroidPaint, Paint particlePaint) {
        super(asteroidPaint, particlePaint);
        asteroids = new ArrayList<>();
        asteroidBitmap = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(context, R.drawable.ic_asteroid), colorAccent, colorPrimary);
        asteroidBitmap2 = ImageUtils.gradientBitmap(ImageUtils.getVectorBitmap(context, R.drawable.ic_asteroid_two), colorAccent, colorPrimary);
    }

    public void setMakeAsteroids(boolean shouldMakeAsteroids) {
        this.shouldMakeAsteroids = shouldMakeAsteroids;
        asteroidLength = 3000;
        if (shouldMakeAsteroids)
            asteroids.clear();
    }

    public void makeNew() {
        asteroidTime = System.currentTimeMillis();
        asteroids.add(new AsteroidData(Math.round(Math.random()) == 0 ? asteroidBitmap : asteroidBitmap2));
    }

    public int size() {
        return asteroids.size();
    }

    public AsteroidData asteroidAt(Rect position) {
        for (AsteroidData asteroid : asteroids) {
            if (asteroid.position != null && position.intersect(asteroid.position))
                return asteroid;
        }

        return null;
    }

    public void destroy(AsteroidData asteroid) {
        asteroids.remove(asteroid);
    }

    @Override
    public boolean draw(Canvas canvas, float speed) {
        boolean isPassed = false;

        for (AsteroidData asteroid : new ArrayList<>(asteroids)) {
            Matrix matrix = asteroid.next(speed, canvas.getWidth(), canvas.getHeight());
            if (matrix != null) {
                canvas.drawBitmap(asteroid.asteroidBitmap, matrix, paint(0));
            } else {
                isPassed = true;
                asteroids.remove(asteroid);
                if (asteroidLength > 750)
                    asteroidLength -= (asteroidLength * 0.1);
            }
        }

        if (shouldMakeAsteroids && System.currentTimeMillis() - asteroidTime > asteroidLength)
            makeNew();

        return isPassed;
    }
}
