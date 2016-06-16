package com.karan.stockhawk.service;

/**
 * Created by Karan on 16-06-2016.
 */

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.karan.stockhawk.R;
import com.karan.stockhawk.data.QuoteColumns;
import com.karan.stockhawk.data.QuoteProvider;
import com.karan.stockhawk.rest.Utils;

/**
 * Created by Karan on 16-06-2016.
 */

public class StockWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(StockWidgetService.this);
    }
}

class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;

    public StockRemoteViewsFactory(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        final long token = Binder.clearCallingIdentity();
        try {
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void onDestroy() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteView = new RemoteViews(mContext.getPackageName(),
                R.layout.list_item_quote);

        mCursor.moveToPosition(position);
        String stockName = mCursor.getString(1);
        remoteView.setTextViewText(R.id.stock_symbol, stockName);
        remoteView.setTextViewText(R.id.bid_price, mCursor.getString(2));
        remoteView.setTextViewText(R.id.change, mCursor.getString(3));

        int sdk = Build.VERSION.SDK_INT;
        if (mCursor.getInt(mCursor.getColumnIndex("is_up")) == 1){
            remoteView.setInt(R.id.change, "setBackgroundColor",
                    mContext.getResources().getColor(R.color.material_green_700));
        } else{
            remoteView.setInt(R.id.change, "setBackgroundColor",
                    mContext.getResources().getColor(R.color.material_red_700));
        }

        Intent fillInIntent = new Intent();
        Bundle mBundle = new Bundle();
        mBundle.putString(Utils.KEY_STOCK_SYMBOL, stockName);
        fillInIntent.putExtras(mBundle);
        remoteView.setOnClickFillInIntent(R.id.widget_list_item_parent, fillInIntent);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


}
