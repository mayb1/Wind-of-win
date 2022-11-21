package wind.win.com;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnalyticSingleton.getInstance(this).logActivity(this);
    }

}