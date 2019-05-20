package james.asteroid.activities;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import james.asteroid.R;
import james.asteroid.views.GameView;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private TextView hintView;
    private GameView gameView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        hintView = findViewById(R.id.hint);
        gameView = findViewById(R.id.game);

        hintView.setText(String.format("%s", "Press to play"));
        gameView.setOnClickListener(this);
    }


    @Override
    public void onPause() {
        if (gameView != null)
            gameView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.onResume();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (!gameView.isPlaying()) {
            gameView.play();
            hintView.setVisibility(View.GONE);
        }
    }
}
