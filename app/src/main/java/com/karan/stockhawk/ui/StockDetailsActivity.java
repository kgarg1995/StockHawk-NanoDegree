package com.karan.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.karan.stockhawk.R;
import com.karan.stockhawk.data.QuoteColumns;
import com.karan.stockhawk.data.QuoteProvider;
import com.karan.stockhawk.rest.Utils;

/**
 * Created by Karan on 16-06-2016.
 */

public class StockDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CURSOR_LOADER_ID = 1;
    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(CURSOR_LOADER_ID, null, StockDetailsActivity.this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP,
                        QuoteColumns.CREATED},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{getIntent().getStringExtra(Utils.KEY_STOCK_SYMBOL)},
                QuoteColumns._ID + " DESC LIMIT 10");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        final LineChartView lineChartView = (LineChartView) findViewById(R.id.linechart);
        LineSet dataSet = new LineSet();
        float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
        /*String previousLabel = null;*/
        while (data.moveToNext()) {
            try {
                float value = Float.parseFloat(data.getString(2));
                /*String xLabel = data.getString(6);
                if (xLabel.equals(previousLabel)) {
                    xLabel = "";
                }
                previousLabel = data.getString(6);
                dataSet.addPoint(xLabel, value);*/
                Point point = new Point("", value);
                point.setColor(Color.parseColor("#ffffff"));
                point.setStrokeColor(Color.parseColor("#0290c3"));
                dataSet.addPoint(point);

                min = Math.min(min, value);
                max = Math.max(max, value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < dataSet.size(); ++i) {
            Point point = (Point) dataSet.getEntry(i);
        }

        dataSet.setColor(Color.parseColor("#53c1bd"))
                .setFill(Color.parseColor("#3d6c73"))
                .setGradientFill(new int[]{Color.parseColor("#364d5a"), Color.parseColor("#3f7178")}, null);

        /*dataSet.setColor(Color.parseColor("#004f7f"))
                .setThickness(Tools.fromDpToPx(3))
                .setSmooth(true)
                *//*.beginAt()
                .endAt()*//*;*/
        lineChartView.getData().clear();
        lineChartView.addData(dataSet);

        /*Paint thresPaint = new Paint();
        thresPaint.setColor(Color.parseColor("#0079ae"));
        thresPaint.setStyle(Paint.Style.STROKE);
        thresPaint.setAntiAlias(true);
        thresPaint.setStrokeWidth(Tools.fromDpToPx(.75f));
        thresPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));*/

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#ffffff"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));

        lineChartView.setBorderSpacing(Tools.fromDpToPx(0))
                .setXLabels(AxisController.LabelPosition.NONE)
                //.setLabelsColor(Color.parseColor("#304a00"))
                .setLabelsColor(Color.parseColor("#FFFFFF"))
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXAxis(false)
                .setYAxis(false)
                .setGrid(ChartView.GridType.HORIZONTAL, (Math.round(max + 1) - Math.round(min - 1)),
                        1, gridPaint)
                //.setValueThreshold(80f, 80f, thresPaint)
                .setAxisBorderValues(Math.round(min - 1), Math.round(max + 1))
                /*.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        ((int) Tools.fromDpToPx(Math.round(max + 1) - Math.round(min - 1)) - 1) * 100))*/;

        Animation anim = new Animation().setStartPoint(-1, 0).setEasing(new BounceEase());
        lineChartView.show(anim);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        LineChartView lineChartView = (LineChartView) findViewById(R.id.linechart);
        lineChartView.getData().clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((LineChartView) findViewById(R.id.linechart)).getChartAnimation().cancel();
    }
}
