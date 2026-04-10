package com.example.bigaehrraidapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SalesBarChartView extends View {

    private float[] values = {
            2,  1,  1,  0,  0,  1,
            4,  8, 12, 18, 22, 28,
           35, 40, 38, 30, 42, 55,
           48, 60, 58, 45, 30, 15
    };

    private final String[] xLabels    = {"12 AM", "6 AM", "12 PM", "6 PM"};
    private final int[]    xLabelHour = {0, 6, 12, 18};

    private final Paint barPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SalesBarChartView(Context context) {
        super(context); init();
    }
    public SalesBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs); init();
    }
    public SalesBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
        labelPaint.setColor(Color.parseColor("#888888"));
        labelPaint.setTextSize(28f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        axisPaint.setColor(Color.parseColor("#E0E0E0"));
        axisPaint.setStrokeWidth(2f);
    }

    public void setValues(float[] values) {
        this.values = values;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int   w              = getWidth();
        int   h              = getHeight();
        float labelAreaHeight = 40f;
        float chartHeight    = h - labelAreaHeight - 8f;

        float max = 1f;
        for (float v : values) if (v > max) max = v;

        int   count  = values.length;
        float barGap = 4f;
        float barW   = (w - barGap * (count + 1)) / count;

        for (int i = 0; i < count; i++) {
            float barH  = (values[i] / max) * chartHeight;
            float left  = barGap + i * (barW + barGap);
            float top   = chartHeight - barH;
            float right = left + barW;

            float ratio = values[i] / max;
            int   r = (int) (74  + (1 - ratio) * 100);
            int   g = (int) (144 + (1 - ratio) * 60);
            barPaint.setColor(Color.rgb(r, g, 217));

            canvas.drawRoundRect(new RectF(left, top, right, chartHeight), 4f, 4f, barPaint);
        }

        canvas.drawLine(0, chartHeight + 4, w, chartHeight + 4, axisPaint);

        for (int i = 0; i < xLabels.length; i++) {
            float cx = barGap + xLabelHour[i] * (barW + barGap) + barW / 2f;
            canvas.drawText(xLabels[i], cx, h - 4f, labelPaint);
        }
    }
}
