package org.osdg.fata;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by plter on 9/24/15.
 */
public class FlashAnimationView extends SurfaceView {

    private JSONArray configFramesArray = null;
    private List<FlashAnimationFrame> frames = new ArrayList<>();
    private double fps = 20;
    private boolean released = false;
    private Bitmap bitmap = null;
    private Timer timer = new Timer();
    private TimerTask task = null;
    private boolean playing = false;
    private int delay = 50;
    private int currentFrameIndex = 0;
    private boolean autoPlay = false;
    private boolean repeat = true;

    private SurfaceHolder.Callback surfaceViewHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (isAutoPlay()){
                start();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stop();
        }
    };

    public FlashAnimationView(Context context, int drawableId, int configId) {
        this(context, drawableId, configId, 20, true, true);
    }

    public FlashAnimationView(Context context, int drawableId, int configId, float fps, boolean autoPlay, boolean repeat) {
        super(context);

        InputStream in = context.getResources().openRawResource(configId);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            StringBuilder content = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }

            initWithDrawableAndConfigJSON(BitmapFactory.decodeResource(context.getResources(), drawableId), new JSONObject(content.toString()), fps, autoPlay, repeat);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("UnsupportedEncodingException");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IOException");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("IOException");
        }
    }

    public FlashAnimationView(Context context, Bitmap drawable, JSONObject jsonObject, float fps) {
        super(context);
        initWithDrawableAndConfigJSON(drawable, jsonObject, fps, true, true);
    }

    public FlashAnimationView(Context context, Bitmap drawable, JSONObject jsonObject) {
        super(context);
        initWithDrawableAndConfigJSON(drawable, jsonObject, 20, true, true);
    }

    private void initWithDrawableAndConfigJSON(Bitmap bitmap, JSONObject jsonObject, double fps, boolean autoPlay, boolean repeat) {
        try {
            setFps(fps);
            setAutoPlay(autoPlay);
            setRepeat(repeat);

            configFramesArray = jsonObject.getJSONArray("frames");

            for (int i = 0; i < configFramesArray.length(); i++) {
                frames.add(new FlashAnimationFrame(configFramesArray.getJSONObject(i).getJSONObject("frame")));
            }

            this.bitmap = bitmap;

            System.out.println(bitmap.getWidth() + "," + bitmap.getHeight());

            getHolder().addCallback(surfaceViewHolderCallback);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Wrong config file!");
        }
    }

    public void setFps(double fps) {
        this.fps = fps;

        delay = (int) (1000 / fps);
    }

    public double getFps() {
        return fps;
    }

    public void play() {
        if (!playing) {
            task = new TimerTask() {
                @Override
                public void run() {
                    drawFrame();
                }
            };
            timer.schedule(task, delay, delay);
            playing = true;
        }
    }

    public void start() {
        currentFrameIndex = 0;
        play();
    }

    public void stop() {
        if (playing) {
            pause();
            currentFrameIndex = 0;
        }
    }

    private void pause() {
        if (playing) {
            task.cancel();
            playing = false;
        }
    }

    private void drawFrame() {

        if (currentFrameIndex >= frames.size()) {
            currentFrameIndex = 0;

            if (!isRepeat()){
                stop();
            }
        }

        Canvas canvas = getHolder().lockCanvas();

        if (canvas!=null) {

            canvas.save();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            frames.get(currentFrameIndex).drawOnCanvas(bitmap, canvas);

            currentFrameIndex++;
            canvas.restore();

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    /**
     * Release the bitmap data
     */
    public void release() {
        bitmap.recycle();
        released = true;
    }

    public boolean isReleased() {
        return released;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isRepeat() {
        return repeat;
    }
}
