package mda.sport.tempomator;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;


public class CircleMatrixView extends View {

    private static final int CIRCLE_COUNT = 4;

    private final Paint[] fillPaints = new Paint[CIRCLE_COUNT];
    private final int[] baseColors = {
            ContextCompat.getColor(getContext(), R.color.basecolor1),
            ContextCompat.getColor(getContext(), R.color.basecolor2),
            ContextCompat.getColor(getContext(), R.color.basecolor3),
            ContextCompat.getColor(getContext(), R.color.basecolor4)
    };
    private final int[] flashColors = {
            ContextCompat.getColor(getContext(), R.color.flashcolor1),
            ContextCompat.getColor(getContext(), R.color.flashcolor2),
            ContextCompat.getColor(getContext(), R.color.flashcolor3),
            ContextCompat.getColor(getContext(), R.color.flashcolor4)
    };
    private final boolean[] shouldFill = new boolean[CIRCLE_COUNT];
    private final boolean[] isFlashing = new boolean[CIRCLE_COUNT];

    private float[] baseRadii = new float[CIRCLE_COUNT];
    private int centerX, centerY;

    private boolean isRunning = false;

    private final Handler handler = new Handler();
    private MediaPlayer sound1, sound2;

    public CircleMatrixView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        for (int i = 0; i < CIRCLE_COUNT; i++) {
            fillPaints[i] = new Paint();
            fillPaints[i].setStyle(Paint.Style.FILL);
            fillPaints[i].setColor(Color.TRANSPARENT);
            fillPaints[i].setAntiAlias(true);
        }

        sound1 = MediaPlayer.create(context, R.raw.click_routine);
        sound2 = MediaPlayer.create(context, R.raw.click_last_cycle);
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w / 2;
        centerY = h / 2;
        float maxRadius = Math.min(w, h) / 2f * 0.9f;

        for (int i = 0; i < CIRCLE_COUNT; i++) {
            baseRadii[i] = maxRadius * (i + 1) / CIRCLE_COUNT;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // paint from outer to inner
        for (int i = CIRCLE_COUNT - 1; i >= 0; i--) {
            if (shouldFill[i]) {
                float radius = baseRadii[i];
                if (isFlashing[i]) {
                    fillPaints[i].setColor(flashColors[i]);
                    radius *= 1.1f;
                } else {
                    fillPaints[i].setColor(baseColors[i]);
                }
                canvas.drawCircle(centerX, centerY, radius, fillPaints[i]);
            }
        }

        // countours
        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(5);
        strokePaint.setColor(Color.parseColor("#D3D3D3"));
        strokePaint.setAntiAlias(true);

        for (int i = 0; i < CIRCLE_COUNT; i++) {
            canvas.drawCircle(centerX, centerY, baseRadii[i], strokePaint);
        }
    }

    public void nextBeat(int beatIndex) {
        if (beatIndex < 0 || beatIndex >= CIRCLE_COUNT) return;

        // Очищаємо всі заповнення на новому циклі
        if (beatIndex == 0) {
            for (int i = 0; i < CIRCLE_COUNT; i++) {
                shouldFill[i] = false;
                isFlashing[i] = false;
            }
        }

        // Заповнюємо кола від 0 до поточного beatIndex включно
        for (int i = 0; i <= beatIndex; i++) {
            shouldFill[i] = true;
        }

        isFlashing[beatIndex] = true;
        playSound(beatIndex);
        invalidate();

        handler.postDelayed(() -> {
            isFlashing[beatIndex] = false;
            invalidate();
        }, 100);
    }

    private void playSound(int index) {
        if (index < CIRCLE_COUNT - 1) {
            if (sound1 != null) sound1.start();
        } else {
            if (sound2 != null) sound2.start();
        }
    }

    public void reset() {
        for (int i = 0; i < CIRCLE_COUNT; i++) {
            shouldFill[i] = false;
            isFlashing[i] = false;
            fillPaints[i].setColor(Color.TRANSPARENT);
        }
        invalidate();
    }
}
