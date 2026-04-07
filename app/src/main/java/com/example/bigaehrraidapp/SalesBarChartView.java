package com.example.bigaehrraidapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SalesBarChartView extends View {

    // Hourly sales data — 24 values (hour 0 → hour 23)
    private float[] values = {
            2,  1,  1,  0,  0,  1,   // 12 AM – 5 AM
            4,  8, 12, 18, 22, 28,   // 6 AM  – 11 AM
           35, 40, 38, 30, 42, 55,   // 12 PM – 5 PM
           48, 60, 58, 45, 30, 15    // 6 PM  – 11 PM
    };

    // X-axis tick labels and their approximate hour positions
    private final String[] xLabels    = {"12 AM", "6 AM", "12 PM", "6 PM"};
    private final int[]    xLabelHour = {0, 6, 12, 18};

    private final Paint barPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SalesBarChartView(Context context) {
        super(context);
        init();
    }

    public SalesBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SalesBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(Color.parseColor("#4A90D9"));

        labelPaint.setColor(Color.parseColor("#888888"));
        labelPaint.setTextSize(28f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint.setColor(Color.parseColor("#E0E0E0"));
        axisPaint.setStrokeWidth(2f);
    }

    /** Allow fragment to push live data */
    public void setValues(float[] values) {
        this.values = values;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        float labelAreaHeight = 40f;
        float chartHeight     = h - labelAreaHeight - 8f;

        // Find max value
        float max = 1f;
        for (float v : values) if (v > max) max = v;

        int   count   = values.length;
        float barGap  = 4f;
        float barW    = (w - barGap * (count + 1)) / count;

        for (int i = 0; i < count; i++) {
            float barH  = (values[i] / max) * chartHeight;
            float left  = barGap + i * (barW + barGap);
            float top   = chartHeight - barH;
            float right = left + barW;
            float bot   = chartHeight;

            // Darker shade for taller bars (light → dark blue)
            float ratio = values[i] / max;
            int   r     = (int) (74  + (1 - ratio) * 100);
            int   g     = (int) (144 + (1 - ratio) * 60);
            int   b     = (int) (217);
            barPaint.setColor(Color.rgb(r, g, b));

            canvas.drawRoundRect(new RectF(left, top, right, bot), 4f, 4f, barPaint);
        }

        // Draw axis line
        canvas.drawLine(0, chartHeight + 4, w, chartHeight + 4, axisPaint);

        // Draw X-axis labels at 12 AM, 6 AM, 12 PM, 6 PM
        for (int i = 0; i < xLabels.length; i++) {
            int   hour  = xLabelHour[i];
            float cx    = barGap + hour * (barW + barGap) + barW / 2f;
            canvas.drawText(xLabels[i], cx, h - 4f, labelPaint);
        }
    }
}
