package wearapps.com.paintsync;

import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;

import com.google.android.gms.wearable.PutDataMapRequest;

import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;

import java.util.Date;


public class MainActivity extends FragmentActivity implements View.OnTouchListener, SettingsDialogListener, DataApi.DataListener {

    private static final String TAG = "paint";

    // Key for the string that's delivered in the action's intent
    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    public boolean buttonClicked = false;
    private int currentColor = Color.WHITE ;
    Node mNode;

    private GoogleApiClient mGoogleAppiClient;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private Paint mPaint;
    private Paint mBitmapPaint,BitmapPaint2;
    DrawingView dv ;

    // Config Values
    public static final int PATH_COLOR = 0;
    public static final int BACKGROUND_COLOR= 1;
    public static final int SHARE = 2;
    public static final int CLEAR = 3;
    public static final int SAVE = 4;
    public static final int EXIT = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        if(savedInstanceState!=null)
            mResolvingError = savedInstanceState != null
                    && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
               // root = (RelativeLayout) stub.findViewById(R.id.root);
               // root.setOnTouchListener(MainActivity.this);
                dv = new DrawingView(MainActivity.this);

                setContentView(dv);
                dv.setBackgroundColor(currentColor );
                        mPaint = new Paint();
                mPaint.setAntiAlias(true);
                mPaint.setDither(true);
                mPaint.setColor(Color.GREEN);

                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeJoin(Paint.Join.ROUND);
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mPaint.setStrokeWidth(12);


            }
        });


         mGoogleAppiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {

                        Wearable.DataApi.addListener(mGoogleAppiClient, MainActivity.this);

                        // Now you can use the data layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {

                        if (mResolvingError) {
                            // Already attempting to resolve an error.
                            return;
                        } else if (result.hasResolution()) {
                            try {
                                mResolvingError = true;
                                result.startResolutionForResult(MainActivity.this, REQUEST_RESOLVE_ERROR);
                            } catch (IntentSender.SendIntentException e) {
                                // There was an error with the resolution intent. Try again.
                                mGoogleAppiClient.connect();
                            }
                        } else {
                            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                            showErrorDialog(result.getErrorCode());
                            mResolvingError = true;
                        }
                    }
                })
                .addApi(Wearable.API)
                .build();

    }


    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onColorPicked(int color) {
        if(color == 0)
            return;

        sendConfigEvent(PATH_COLOR,color);
        mPaint.setColor(color);
        buttonClicked = false;
    }

    @Override
    public void onBackgroundPicked(int color) {
        sendConfigEvent(BACKGROUND_COLOR,color);
        dv.setBackgroundColor(color);
        currentColor = color;
        buttonClicked = false;
    }

    @Override
    public void onSaveClicked()
    {
        //sendConfigEvent(SAVE,0);
        Toast.makeText(MainActivity.this,"Image had been saved on your phone",Toast.LENGTH_LONG).show();
        dv.shareClicked(SAVE);
        dv.saveDrawing();
        buttonClicked = false;
    }

    @Override
    public void onClearClicked() {
        sendConfigEvent(CLEAR,0);
        dv.clearDrawing();
        currentColor = Color.WHITE;
        dv.setBackgroundColor(currentColor);
        buttonClicked = false;
    }

    @Override
    public void onShareClicked() {
        Toast.makeText(MainActivity.this,"Continue action on your phone",Toast.LENGTH_LONG).show();
        buttonClicked = false;
        dv.shareClicked(SHARE);
    }

    @Override
    public void onExitClicked() {

        buttonClicked = false;
        if(mGoogleAppiClient!=null)
            mGoogleAppiClient.disconnect();
        mGoogleAppiClient = null;
        finish();


    }

    @Override
    public void onEraseClicked() {

        sendConfigEvent(PATH_COLOR,currentColor);
        mPaint.setColor(currentColor);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }
    private void sendConfigEvent(int type,int extraColor) {

        if (mGoogleAppiClient.isConnected()) {

            PutDataMapRequest request = PutDataMapRequest.create("/config");
            DataMap map = request.getDataMap();

            map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
            map.putInt("type", type);

            if(extraColor!=0)
                map.putInt("color",extraColor);

            Wearable.DataApi.putDataItem(mGoogleAppiClient, request.asPutDataRequest());
        }
        else {
            Log.e(TAG, "No connection to wearable available!");
        }
    }

    public class DrawingView extends View {

        public int width;
        public  int height;
        private Bitmap  mBitmap,mSettingsBitmap;
        private Canvas  mCanvas;
        private Path    mPath;
        Context context;

        private Paint circlePaint;
        private Path circlePath;
        Rect buttonOptions;
        Rect buttonOptionsSource;

        public DrawingView(Context c) {
            super(c);
            context=c;

            mSettingsBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.settings);

            int rectWidth=50;
            int rectHeight=50;
            int rectX=130;
            int rectY= 0;
            buttonOptionsSource = new Rect(0, 0, rectWidth, rectHeight);
            buttonOptions = new Rect(rectX, rectY, rectX + rectWidth, rectY+ rectHeight);

            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

            BitmapPaint2 = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(2f);

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
            canvas.drawBitmap(mSettingsBitmap, buttonOptionsSource, buttonOptions, BitmapPaint2);

            canvas.drawPath( mPath,  mPaint);

            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        private void sendEvent(float x,float y,int action) {

            if (mGoogleAppiClient.isConnected()) {

                PutDataMapRequest request = PutDataMapRequest.create("/touch");
                DataMap map = request.getDataMap();

                map.putLong("time", new Date().getTime()); // MOST IMPORTANT LINE FOR TIMESTAMP
                map.putFloat("x", x);
                map.putFloat("y",y);
                map.putInt("action", action);

                Wearable.DataApi.putDataItem(mGoogleAppiClient, request.asPutDataRequest());
            }
            else {
                Log.e(TAG, "No connection to wearable available!");
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {


            if (buttonOptions.contains((int)event.getX(), (int)event.getY()) && !buttonClicked) {
                buttonClicked = true;

                SettingsDialog settingsDialog =new SettingsDialog(MainActivity.this,MainActivity.this,currentColor,mPaint.getColor());
                settingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        buttonClicked = false;
                    }
                });
                settingsDialog.show();

            }

            float x = event.getX();
            float y = event.getY();

            sendEvent(x,y,event.getAction());

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

        public void shareClicked(int action) {

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
            map.putInt("action",action);
            Wearable.DataApi.putDataItem(mGoogleAppiClient, request.asPutDataRequest());

        }
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity)getActivity()).onDialogDismissed();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleAppiClient.isConnecting() &&
                        !mGoogleAppiClient.isConnected()) {
                    mGoogleAppiClient.connect();
                }
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {  // more about this later
            mGoogleAppiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if(mGoogleAppiClient!=null)
        mGoogleAppiClient.disconnect();
        super.onStop();
    }

}
