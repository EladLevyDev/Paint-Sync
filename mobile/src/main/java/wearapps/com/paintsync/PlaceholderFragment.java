package wearapps.com.paintsync;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.util.Date;


public class PlaceholderFragment extends Fragment {

    private int currentColor= Color.WHITE;
    private boolean actionDownCalled;
    public GoogleApiClient mGoogleAppiClient;

    public static final PlaceholderFragment newInstance(GoogleApiClient googleApp) {
        PlaceholderFragment f = new PlaceholderFragment();
        f.mGoogleAppiClient = googleApp;

        return f;
    }
    public PlaceholderFragment ()
    {

    }

    private Paint mPaint;
    DrawingView dv ;
    private boolean needUpdate;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_wear, container, false);
//             imageRoot = (ImageView) rootView.findViewById(R.id.imageRoot);
//             text = (TextView) rootView.findViewById(R.id.textViewStart);

        dv = new DrawingView(getActivity());

        dv.setBackgroundColor(currentColor );
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        return dv;
    }

    public void updateRoot(float y, float x, int action) {

        if(!actionDownCalled && action!= MotionEvent.ACTION_DOWN)
            action = MotionEvent.ACTION_DOWN;

//
//            if(needUpdate )
//
//            {
//
//                Log.d("paint","blocking");
//                if(timer == null) {
//                    timer = new Timer();
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            timer = null;
//
//                            needUpdate = false;
//                        }
//                    }, 500);
//                }
//                return;
//
//            }

        Log.d("paint", "values: " + +y + " " + x + " " + action);
        if(y>20 && x>10) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    actionDownCalled = true;
                    dv.touch_start(x, y);
                    dv.invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    dv.touch_move(x, y);
                    dv.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    dv.touch_up();
                    dv.invalidate();
                    // needUpdate = true;
                    actionDownCalled = false;
                    break;
            }
        }
    }

    public void updateConfig(int color, int action) {
        switch (action)
        {
            case MainActivity.BACKGROUND_COLOR:
                currentColor =color ;
                dv.setBackgroundColor(color);
                break;

            case MainActivity.PATH_COLOR:
                if(color!=0)
                mPaint.setColor(color);
                break;

            case MainActivity.SAVE:

                break;

            case MainActivity.CLEAR:
                dv.clearDrawing();
                currentColor = Color.WHITE ;
                dv.setBackgroundColor(currentColor);
                break;
        }
    }

    public class DrawingView extends View {

        public int width;
        public  int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Paint mBitmapPaint;
        private Path mPath;
        Context context;

        // private Paint circlePaint;
        // private Path circlePath;


        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

            // circlePaint = new Paint();
            // circlePath = new Path();
            // circlePaint.setAntiAlias(true);
            // circlePaint.setColor(Color.BLUE);
            // circlePaint.setStyle(Paint.Style.STROKE);
            // circlePaint.setStrokeJoin(Paint.Join.MITER);
            //circlePaint.setStrokeWidth(4f);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            width = w;      // don't forget these
            height = h;

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);

        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);


            canvas.drawPath( mPath,  mPaint);

            //canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        public void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        public void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                //  circlePath.reset();
                // circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }
        public void touch_up() {

            mPath.lineTo(mX, mY);
            // circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {


            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    needUpdate = true;
                    invalidate();
                    break;
            }
            return true;
        }

        public void clearDrawing()
        {

            // don't forget that one and the match below,
            // or you just keep getting a duplicate when you save.

            onSizeChanged(width, height, width, height);
            invalidate();

        }
        public void saveDrawing()
        {
            //TODO: FIX THIS ALSO Bitmap whatTheUserDrewBitmap = getDrawingCache();
            // don't forget to clear it (see above) or you just get duplicates

            // almost always you will want to reduce res from the very high screen res
            // whatTheUserDrewBitmap =
            //     ThumbnailUtils.extractThumbnail(whatTheUserDrewBitmap, 256, 256);
            // NOTE that's an incredibly useful trick for cropping/resizing squares
            // while handling all memory problems etc
            // http://stackoverflow.com/a/17733530/294884

            // you can now save the bitmap to a file, or display it in an ImageView:

            //TODO: GET IMAGE ImageView testArea = ...
            //TODO: SHOW HERE  testArea.setImageBitmap( whatTheUserDrewBitmap );

            // these days you often need a "byte array". for example,
            // to save to parse.com or other cloud services
            //   ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //    whatTheUserDrewBitmap.compress(Bitmap.CompressFormat.JPEG, 0, baos);
            //  byte[] yourByteArray;
            //  yourByteArray = baos.toByteArray();
        }

        public void shareClicked() {

            Bitmap whatTheUserDrewBitmap= Bitmap.createBitmap( dv.getWidth(), dv.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(whatTheUserDrewBitmap);
            dv.layout(dv.getLeft(), dv.getTop(), dv.getRight(),dv.getBottom());
            dv.draw(c);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream()  ;

            try
            {

                whatTheUserDrewBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteStream);


                dv.invalidate();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if(byteStream == null)
                return;
            Asset asset = Asset.createFromBytes(byteStream.toByteArray());

            PutDataMapRequest request = PutDataMapRequest.create("/image");
            DataMap map = request.getDataMap();
            map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
            map.putAsset("profileImage", asset);
            Wearable.DataApi.putDataItem(mGoogleAppiClient, request.asPutDataRequest());

        }
    }
}