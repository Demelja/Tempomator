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
    private CircleBeatView circleBeatView;
    private TextView bpmText;
    private SeekBar bpmSeekBar;
    private Button startStopButton;


    private Handler handler = new Handler();
    private int bpm = 120;
    private int beatIndex = 0;
    private int beatOverIndex = 0;
    private boolean isRunning = false;
    private boolean isOrderSwitched = true;

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

            if (isOrderSwitched) {
                circleMatrixView.nextBeat(beatIndex);
                circleBeatView.setActiveIndex(beatOverIndex);
            } else {
                circleMatrixView.nextBeat(beatOverIndex);
                circleBeatView.setActiveIndex(beatIndex);
            }

            new Thread(() -> {
                try {
                    if (beatIndex == 3 && beatOverIndex == 3) {
                        playSound(sound3);
                    } else if (beatIndex == 3) {
                        playSound(sound2);
                    } else {
                        playSound(sound1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

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
        circleBeatView = findViewById(R.id.circleBeatView);
        bpmText = findViewById(R.id.bpmRate);
        bpmSeekBar = findViewById(R.id.bpmSeekBar);
        startStopButton = findViewById(R.id.startStopButton);

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

        bpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpm = Math.max(30, progress);
                String bpmTextValue = getString(R.string.bpm_label) + " " + String.valueOf(bpm);
                bpmText.setText(bpmTextValue);
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
        if (item.getItemId() == R.id.menu_switch_order) {
            isOrderSwitched = !isOrderSwitched;
            return true;
        } else if (item.getItemId() == R.id.menu_help) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.help_text)
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        } else if (item.getItemId() == R.id.menu_about) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.about_program)
                    .setMessage(R.string.about_text)
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
        outState.putBoolean("isOrderSwitched", isOrderSwitched);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        bpm = savedInstanceState.getInt("bpm", 120);
        beatIndex = savedInstanceState.getInt("beatIndex", 0);
        beatOverIndex = savedInstanceState.getInt("beatOverIndex", 0);
        isRunning = savedInstanceState.getBoolean("isRunning", false);
        isOrderSwitched = savedInstanceState.getBoolean("isOrderSwitched", false);

        bpmSeekBar.setProgress(bpm);
        String bpmTextValue = getString(R.string.bpm_label) + " " + String.valueOf(bpm);
        bpmText.setText(bpmTextValue);

        circleMatrixView.setRunning(isRunning);
        circleMatrixView.nextBeat(beatIndex);
        circleBeatView.setActiveIndex(beatOverIndex);

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
