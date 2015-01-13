package wearapps.com.paintsync;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;

import com.google.android.gms.wearable.DataMapItem;


import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity implements
        DataApi.DataListener {

    Bitmap bitmap =null;
    private static final String TAG = "paint";
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final long TIMEOUT_MS = 10000;
    private GoogleApiClient mGoogleAppiClient;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    public PlaceholderFragment fragment;


    public static final int PATH_COLOR = 0;
    public static final int BACKGROUND_COLOR= 1;
    public static final int SHARE = 2;
    public static final int CLEAR = 3;
    public static final int SAVE = 4;


    private TextView textViewStart;
    private boolean firstPaint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewStart = (TextView) findViewById(R.id.textViewStart);

        if(savedInstanceState!=null)
            mResolvingError = savedInstanceState != null
                    && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);


        mGoogleAppiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        Wearable.DataApi.addListener(mGoogleAppiClient, MainActivity.this);

                        // Now you can use the data layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
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

        if (savedInstanceState == null) {
            fragment = PlaceholderFragment.newInstance(mGoogleAppiClient);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.wear_frame,fragment )
                    .commit();
        }



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
        dialogFragment.show(getFragmentManager(), "errordialog");
    }
    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {



        for (DataEvent event : dataEvents) {
            Log.d("paint","elad"+ event.getDataItem().getUri().getPath());
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/image")) {
                try {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
                    int action = dataMapItem.getDataMap().getInt("action");
                     bitmap = loadBitmapFromAsset(profileAsset);

                      if(action == SHARE)
                        shareImage(bitmap);
                      else
                        saveImage(bitmap);
                } catch (Exception e) {


                    e.printStackTrace();
                }
            }  else if(event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().equals("/touch")) {
                final DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!firstPaint)
                        {
                            textViewStart.setText("Displaying wear paint..");
                            firstPaint = true;
                        }
                        float y = dataMapItem.getDataMap().getFloat("y");
                        float x = dataMapItem.getDataMap().getFloat("x");
                        int action = dataMapItem.getDataMap().getInt("action");

                        fragment.updateRoot(y, x, action);
                    }
                });
            }
            else if(event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().equals("/config"))
            {
                final DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        int action = dataMapItem.getDataMap().getInt("type");
                        int color = dataMapItem.getDataMap().getInt("color");

                        fragment.updateConfig(color, action);

                        if(action == BACKGROUND_COLOR)
                        {
                            if(color !=0)
                                findViewById(R.id.frameContainer).setBackgroundColor(color);
                        }
                        else if(action == CLEAR)
                        {
                            findViewById(R.id.frameContainer).setBackgroundColor(Color.WHITE);
                        }

                    }
                });
            }

        }
    }

    public  String saveImage( Bitmap bmp)
    {

        String fileName = "Paint"+(int)((System.currentTimeMillis()/1000))+".png";
        File gallery_folder;

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {

                gallery_folder = new File(Environment.getExternalStorageDirectory(),
                        "Paint Images" );

        }
        else {


                gallery_folder = new File(getCacheDir(),
                        "Paint Images" );

        }
        gallery_folder.mkdirs();
        File galleryFileName = new File(gallery_folder, fileName);
        FileOutputStream out = null;

        if (!galleryFileName.exists())
        {

            try
            {
                // photoFile.mkdirs();
                galleryFileName.createNewFile();

                // photoFile.createte
            } catch (IOException e)
            {
                e.printStackTrace();

            }
        }

        try
        {
            out = new FileOutputStream(galleryFileName);
            bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();

            out = null;
        } catch (Exception e)
        {
            e.printStackTrace();


            return null;
        }

        return galleryFileName.getAbsolutePath();
    }

    private void shareImage(Bitmap bitmap) {
        Bitmap icon = bitmap;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleAppiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleAppiClient, asset).await().getInputStream();
        mGoogleAppiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
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

//    public  class PlaceholderFragment extends Fragment {
//
//        private int currentColor= Color.parseColor("#f1edd9");
//
//        public PlaceholderFragment() {
//        }
//
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
////            View rootView = inflater.inflate(R.layout.fragment_wear, container, false);
////             imageRoot = (ImageView) rootView.findViewById(R.id.imageRoot);
////             text = (TextView) rootView.findViewById(R.id.textViewStart);
//
//            dv = new DrawingView(getActivity());
//
//            dv.setBackgroundColor(currentColor );
//            mPaint = new Paint();
//            mPaint.setAntiAlias(true);
//            mPaint.setDither(true);
//            mPaint.setColor(Color.GREEN);
//
//            mPaint.setStyle(Paint.Style.STROKE);
//            mPaint.setStrokeJoin(Paint.Join.ROUND);
//            mPaint.setStrokeCap(Paint.Cap.ROUND);
//            mPaint.setStrokeWidth(12);
//
//            return dv;
//        }

//        public void updateRoot(float y, float x, int action) {
//
//            if(!actionDownCalled && action!= MotionEvent.ACTION_DOWN)
//                action = MotionEvent.ACTION_DOWN;

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

//            Log.d("paint","values: "+ +y + " "+x + " "+action);
//            if(y>20 && x>10) {
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN:
//                        actionDownCalled = true;
//                        dv.touch_start(x, y);
//                        dv.invalidate();
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        dv.touch_move(x, y);
//                        dv.invalidate();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        dv.touch_up();
//                        dv.invalidate();
//                       // needUpdate = true;
//                        actionDownCalled = false;
//                        break;
//                }
//            }
//        }
//
//        public void updateConfig(int color, int action) {
//            switch (action)
//            {
//                case BACKGROUND_COLOR:
//                    currentColor =color ;
//                    dv.setBackgroundColor(color);
//                    break;
//
//                case PATH_COLOR:
//                    mPaint.setColor(color);
//                    break;
//
//                case SAVE:
//
//                    break;
//
//                case CLEAR:
//                    dv.clearDrawing();
//                    currentColor = Color.WHITE ;
//                    dv.setBackgroundColor(currentColor);
//                    break;
//            }
//        }
//    }
//
//    public class DrawingView extends View {
//
//        public int width;
//        public  int height;
//        private Bitmap  mBitmap;
//        private Canvas  mCanvas;
//        private Paint mBitmapPaint;
//        private Path    mPath;
//        Context context;
//
//       // private Paint circlePaint;
//       // private Path circlePath;
//
//
//        public DrawingView(Context c) {
//            super(c);
//            context=c;
//            mPath = new Path();
//            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
//
//           // circlePaint = new Paint();
//           // circlePath = new Path();
//           // circlePaint.setAntiAlias(true);
//           // circlePaint.setColor(Color.BLUE);
//           // circlePaint.setStyle(Paint.Style.STROKE);
//           // circlePaint.setStrokeJoin(Paint.Join.MITER);
//            //circlePaint.setStrokeWidth(4f);
//
//        }
//
//        @Override
//        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//            super.onSizeChanged(w, h, oldw, oldh);
//
//            width = w;      // don't forget these
//            height = h;
//
//            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//            mCanvas = new Canvas(mBitmap);
//
//        }
//        @Override
//        protected void onDraw(Canvas canvas) {
//            super.onDraw(canvas);
//
//            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
//
//
//            canvas.drawPath( mPath,  mPaint);
//
//            //canvas.drawPath( circlePath,  circlePaint);
//        }
//
//        private float mX, mY;
//        private static final float TOUCH_TOLERANCE = 4;
//
//        public void touch_start(float x, float y) {
//            mPath.reset();
//            mPath.moveTo(x, y);
//            mX = x;
//            mY = y;
//        }
//        public void touch_move(float x, float y) {
//            float dx = Math.abs(x - mX);
//            float dy = Math.abs(y - mY);
//            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
//                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
//                mX = x;
//                mY = y;
//
//              //  circlePath.reset();
//              // circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
//            }
//        }
//        public void touch_up() {
//
//            mPath.lineTo(mX, mY);
//           // circlePath.reset();
//            // commit the path to our offscreen
//            mCanvas.drawPath(mPath,  mPaint);
//            // kill this so we don't double draw
//            mPath.reset();
//        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent event) {
//
//
//            float x = event.getX();
//            float y = event.getY();
//
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    touch_start(x, y);
//                    invalidate();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    touch_move(x, y);
//                    invalidate();
//                    break;
//                case MotionEvent.ACTION_UP:
//                    touch_up();
//                    needUpdate = true;
//                    invalidate();
//                    break;
//            }
//            return true;
//        }
//
//        public void clearDrawing()
//        {
//
//            // don't forget that one and the match below,
//            // or you just keep getting a duplicate when you save.
//
//            onSizeChanged(width, height, width, height);
//            invalidate();
//
//        }
//
//    }
}

//<com.google.android.gms.ads.AdView android:id="@+id/adView"
//        android:layout_width="match_parent"
//        android:layout_height="wrap_content"
//        android:layout_alignParentBottom="true"
//        ads:adSize="BANNER"
//        ads:adUnitId="ca-app-pub-8754175996077923/2472866090"/>
// Look up the AdView as a resource and load a request.
//AdView adView = (AdView)this.findViewById(R.id.adView);
//AdRequest adRequest = new AdRequest.Builder().build();
//adView.loadAd(adRequest);

//
//<!--Include the AdActivity configChanges and theme. -->
//<activity android:name="com.google.android.gms.ads.AdActivity"
//        android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|uiMode|screenSize|smallestScreenSize"
//        android:theme="@android:style/Theme.Translucent" />