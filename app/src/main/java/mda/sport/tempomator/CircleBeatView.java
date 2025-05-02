package mda.sport.tempomator;
// ChatGPT generated code 27.04.2025


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleBeatView extends View {
    private int activeIndex = -1;
    private final Paint paintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintShape = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleBeatView(Context context) {
        super(context);
        init();
    }

    public CircleBeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintCircle.setColor(Color.LTGRAY);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(5);

        paintFill.setColor(Color.parseColor("#CCFFCC")); // ясно-зелений
        paintFill.setStyle(Paint.Style.FILL);

        paintShape.setColor(Color.WHITE);
        paintShape.setStyle(Paint.Style.FILL);
    }

    public void setActiveIndex(int index) {
        activeIndex = index;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = 4;

        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        float w = getWidth();
        float h = getHeight();
        float availableSize = isPortrait ? w : h;

        float spacing = availableSize / 20;
        float diameter = (availableSize - spacing * (count + 1)) / count;
        float radius = diameter / 2;

        for (int i = 0; i < count; i++) {
            float cx = isPortrait ? spacing + radius + i * (diameter + spacing) : w / 2;
            float cy = isPortrait ? h / 2 : h - (spacing + radius + i * (diameter + spacing));

            // Заповнення фоном
            if (i == activeIndex) {
                canvas.drawCircle(cx, cy, radius, paintFill);
                drawShape(canvas, i, cx, cy, radius);
            } else if (i < activeIndex) {
                Paint grayFill = new Paint(Paint.ANTI_ALIAS_FLAG);
                grayFill.setColor(Color.GRAY);
                grayFill.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, radius, grayFill);
            }

            // Контур
            canvas.drawCircle(cx, cy, radius, paintCircle);
        }
    }

    private void drawShape(Canvas canvas, int index, float cx, float cy, float radius) {
        float r = radius * 0.6f;

        switch (index) {
            case 0: // один білий вузький прямокутник
                canvas.drawRect(cx - r * 0.2f, cy - r, cx + r * 0.2f, cy + r, paintShape);
                break;
            case 1: // два білі прямокутники
                canvas.drawRect(cx - r * 0.5f, cy - r, cx - r * 0.1f, cy + r, paintShape);
                canvas.drawRect(cx + r * 0.1f, cy - r, cx + r * 0.5f, cy + r, paintShape);
                break;
            case 2: // три білі круги трикутником
                float offset = r * 0.5f;
                canvas.drawCircle(cx, cy - offset, r * 0.2f, paintShape);
                canvas.drawCircle(cx - offset, cy + offset * 0.6f, r * 0.2f, paintShape);
                canvas.drawCircle(cx + offset, cy + offset * 0.6f, r * 0.2f, paintShape);
                break;
            case 3: // чотири білі круги квадратом
                float dx = r * 0.5f;
                float dy = r * 0.5f;
                canvas.drawCircle(cx - dx, cy - dy, r * 0.2f, paintShape);
                canvas.drawCircle(cx + dx, cy - dy, r * 0.2f, paintShape);
                canvas.drawCircle(cx - dx, cy + dy, r * 0.2f, paintShape);
                canvas.drawCircle(cx + dx, cy + dy, r * 0.2f, paintShape);
                break;
        }
    }
}
