package org.osdg.fata;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by plter on 9/25/15.
 */
class FlashAnimationFrame {


    private int x, y, width, height;
    private Rect sourceRect = new Rect(0, 0, 0, 0), distRect = new Rect(0, 0, 0, 0);
    private Paint paint = new Paint();

    FlashAnimationFrame(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        sourceRect.set(x, y, x + width, y + height);
        distRect.set(0, 0, width, width);
    }

    FlashAnimationFrame(JSONObject frameJsonObject) throws JSONException {
        this(frameJsonObject.getInt("x"), frameJsonObject.getInt("y"), frameJsonObject.getInt("w"), frameJsonObject.getInt("h"));
    }


    public void drawOnCanvas(Bitmap source, Canvas canvas) {
        canvas.save();
        canvas.drawBitmap(source,sourceRect,distRect,paint);
        canvas.restore();
    }

}
