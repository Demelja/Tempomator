package mda.sport.tempomator;

// MainActivity.java

import android.app.AlertDialog;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private CircleMatrixView circleMatrixView;
    private TextView bpmText;
    private SeekBar bpmSeekBar;
    private Button startStopButton;
    private View[] beatOverViews = new View[4];


    private Handler handler = new Handler();
    private int bpm = 120;
    private int beatIndex = 0;
    private int beatOverIndex = 0;
    private boolean isRunning = false;

    private SoundPool soundPool;
    private int sound1, sound2, sound3;

    private final Runnable beatRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            runOnUiThread(() -> {
                bpmText.setBackgroundColor(Color.RED);
                bpmText.postDelayed(() ->
                        bpmText.setBackgroundColor(Color.TRANSPARENT), 100);
            });

            updateBeatOverCircles(beatOverIndex);

            circleMatrixView.nextBeat(beatIndex);
            if (beatIndex == 3 && beatOverIndex == 3) {
                playSound(sound3);
            } else if (beatIndex == 3) {
                playSound(sound2);
            } else {
                playSound(sound1);
            }
            /*
            🔊 Відтворення звуку — в окремому потоці
🧠 UI залишається чутливим, метроном не зависає
📦 Кожен MediaPlayer звільняє ресурси після завершення

            new Thread(() -> {
        try {
            MediaPlayer player = null;
            if (a == 3 && b == 3) {
                player = MediaPlayer.create(this, R.raw.click3);
            } else if (b == 3) {
                player = MediaPlayer.create(this, R.raw.click2);
            } else {
                player = MediaPlayer.create(this, R.raw.click1);
            }

            if (player != null) {
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
            */
            //beatIndex = (beatIndex + 1) % 4;
            beatIndex++;
            if (beatIndex > 3) {
                beatIndex = 0;
                beatOverIndex++;
                if (beatOverIndex > 3) beatOverIndex = 0;
            }

            long interval = 60000 / bpm;
            handler.postDelayed(this, interval);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleMatrixView = findViewById(R.id.circleMatrixView);
        bpmText = findViewById(R.id.bpmRate);
        bpmSeekBar = findViewById(R.id.bpmSeekBar);
        startStopButton = findViewById(R.id.startStopButton);

        beatOverViews[0] = findViewById(R.id.beatOver1);
        beatOverViews[1] = findViewById(R.id.beatOver2);
        beatOverViews[2] = findViewById(R.id.beatOver3);
        beatOverViews[3] = findViewById(R.id.beatOver4);

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();

        sound1 = soundPool.load(this, R.raw.click_routine, 1);
        sound2 = soundPool.load(this, R.raw.click_forth, 1);
        sound3 = soundPool.load(this, R.raw.click_last_cycle, 1);

        /*
        click1 = MediaPlayer.create(this, R.raw.click1);
        click2 = MediaPlayer.create(this, R.raw.click2);
        click3 = MediaPlayer.create(this, R.raw.click3);
        */
        bpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpm = Math.max(30, progress);
                bpmText.setText("BPM: " + bpm);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        startStopButton.setOnClickListener(v -> toggleMetronome());
    }

    private void toggleMetronome() {
        isRunning = !isRunning;
        beatIndex = 0;
        if (isRunning) {
            handler.post(beatRunnable);
        } else {
            handler.removeCallbacks(beatRunnable);
        }
    }

    private void updateBeatOverCircles(int index) {
        for (int i = 0; i < 4; i++) {
            beatOverViews[i].setBackgroundResource(i == index ? R.drawable.circle_green : R.drawable.circle_gray);
        }
    }

    private void playSound(int soundId) {
        soundPool.play(soundId, 1, 1, 1, 0, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_about) {
            new AlertDialog.Builder(this)
                    .setTitle("About Program")
                    .setMessage("Metronome App\nCreated with ❤️ using Java and Android Studio.")
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("bpm", bpm);
        outState.putInt("beatIndex", beatIndex);
        outState.putInt("beatOverIndex", beatOverIndex);
        outState.putBoolean("isRunning", isRunning);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        bpm = savedInstanceState.getInt("bpm", 120);
        beatIndex = savedInstanceState.getInt("beatIndex", 0);
        beatOverIndex = savedInstanceState.getInt("beatOverIndex", 0);
        isRunning = savedInstanceState.getBoolean("isRunning", false);

        bpmSeekBar.setProgress(bpm);
        bpmText.setText("BPM: " + bpm);

        circleMatrixView.setRunning(isRunning);
        circleMatrixView.nextBeat(beatIndex);
        updateBeatOverCircles(beatOverIndex);

        if (isRunning) {
            handler.postDelayed(beatRunnable, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(beatRunnable);
        soundPool.release();
    }
}


/*
public class MainActivity extends AppCompatActivity {

    private CircleMatrixView circleMatrixView;
    private View[] beatViews = new View[4];
    private View[] beatOverViews = new View[4];
    private TextView bpmText;
    private SeekBar bpmSeekBar;
    private boolean isRunning = false;
    private int bpm = 60;
    private int beatIndex = 0;
    private int beatOverIndex = 0;
    private Handler handler = new Handler();

    private Runnable beatRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            updateBeatOverCircles(beatOverIndex);
            //updateBeatCircles(beatIndex);
            circleMatrixView.setActiveCircle(beatIndex);
            playSound(beatOverIndex, beatIndex);

            beatIndex++;
            if (beatIndex > 3) {
                beatIndex = 0;
                beatOverIndex++;
                if (beatOverIndex > 3) beatOverIndex = 0;
            }

            long delay = 60000 / bpm;
            handler.postDelayed(this, delay);
        }
    };

    private void updateBeatOverCircles(int index) {
        for (int i = 0; i < 4; i++) {
            beatOverViews[i].setBackgroundResource(i == index ? R.drawable.circle_green : R.drawable.circle_gray);
        }
    }

    private void updateBeatCircles(int index) {
        for (int i = 0; i < 4; i++) {
            beatViews[i].setBackgroundResource(i == index ? R.drawable.circle_green : R.drawable.circle_gray);
        }
    }

    private void playSound(int measure, int beat) {
        new Thread(() -> {
            MediaPlayer player;
            if (measure == 3 && beat == 3) {
                player = MediaPlayer.create(this, R.raw.click3);
            } else if (beat == 3) {
                player = MediaPlayer.create(this, R.raw.click2);
            } else {
                player = MediaPlayer.create(this, R.raw.click1);
            }
            if (player != null) {
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleMatrixView = findViewById(R.id.circleMatrixView);

        bpmText = findViewById(R.id.bpmText);
        bpmSeekBar = findViewById(R.id.bpmSeekBar);
        //beatOverViews[0] = findViewById(R.id.beatOver1);
        //beatOverViews[1] = findViewById(R.id.beatOver2);
        //beatOverViews[2] = findViewById(R.id.beatOver3);
        //beatOverViews[3] = findViewById(R.id.beatOver4);
        beatViews[0] = findViewById(R.id.beat1);
        beatViews[1] = findViewById(R.id.beat2);
        beatViews[2] = findViewById(R.id.beat3);
        beatViews[3] = findViewById(R.id.beat4);

        bpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpm = Math.max(progress, 30);
                bpmText.setText("BPM: " + bpm);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.touchArea).setOnClickListener(v -> {
            isRunning = !isRunning;
            if (isRunning) {
                beatIndex = 0;
                beatOverIndex = 0;
                handler.post(beatRunnable);
            } else {
                handler.removeCallbacks(beatRunnable);
                updateBeatCircles(-1);
            }
        });
    }
}
*/