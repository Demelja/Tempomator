package mda.sport.tempomator;
// ChatGPT generated code 27.04.2025

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircleBeatView extends View {
    private int activeIndex = -1;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleBeatView(Context context) {
        super(context);
    }

    public CircleBeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setActiveIndex(int index) {
        this.activeIndex = index;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int count = 4;
        int radius = Math.min(width, height) / 3;
        int spacing = (width - count * radius * 2) / (count + 1);

        for (int i = 0; i < count; i++) {
            int cx = spacing + radius + i * (radius * 2 + spacing);
            int cy = height / 2;
            if (i == activeIndex) {
                paint.setColor(Color.RED);
            } else {
                paint.setColor(Color.LTGRAY);
            }
            canvas.drawCircle(cx, cy, radius, paint);
        }
    }
}
