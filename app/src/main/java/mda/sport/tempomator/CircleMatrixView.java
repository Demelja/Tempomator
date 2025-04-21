package mda.sport.tempomator;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircleMatrixView extends View {

    private Paint paint;
    private final int[] colors = {GRAY, GRAY, GRAY, GRAY};
    private final float[] scaleFactors = {1f, 1f, 1f, 1f};

    private final int RED = 0xFFFF4444;
    private static final int GRAY = 0xFFCCCCCC;

    private float[][] centers = new float[4][2];
    private float baseRadius = 0;

    public CircleMatrixView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setActiveCircle(int index) {
        for (int i = 0; i < 4; i++) {
            final int circleIndex = i;

            // Анімація кольору
            int startColor = colors[i];
            int endColor = (i == index) ? RED : GRAY;

            ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
            colorAnim.setDuration(150);
            colorAnim.addUpdateListener(animation -> {
                colors[circleIndex] = (int) animation.getAnimatedValue();
                invalidate();
            });
            colorAnim.start();

            // Анімація розміру
            if (i == index) {
                ValueAnimator scaleUp = ValueAnimator.ofFloat(1f, 1.2f);
                scaleUp.setDuration(75);
                scaleUp.addUpdateListener(anim -> {
                    scaleFactors[circleIndex] = (float) anim.getAnimatedValue();
                    invalidate();
                });

                ValueAnimator scaleDown = ValueAnimator.ofFloat(1.2f, 1f);
                scaleDown.setDuration(75);
                scaleDown.addUpdateListener(anim -> {
                    scaleFactors[circleIndex] = (float) anim.getAnimatedValue();
                    invalidate();
                });

                scaleUp.setStartDelay(0);
                scaleUp.start();
                scaleDown.setStartDelay(75);
                scaleDown.start();
            } else {
                scaleFactors[i] = 1f;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float spacing = 0.05f * w;
        float diameter = (w - spacing * 2) / 2;
        baseRadius = diameter / 2;
        float cx = w / 2f;
        float cy = h / 2f;

        centers[0][0] = cx;                        centers[0][1] = cy - diameter - spacing; // top
        centers[1][0] = cx - diameter / 2 - spacing; centers[1][1] = cy;                    // left
        centers[2][0] = cx + diameter / 2 + spacing; centers[2][1] = cy;                    // right
        centers[3][0] = cx;                        centers[3][1] = cy + diameter + spacing; // bottom
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < 4; i++) {
            paint.setColor(colors[i]);
            float scale = scaleFactors[i];
            float radius = baseRadius * scale;
            canvas.drawCircle(centers[i][0], centers[i][1], radius, paint);
        }
    }
}

