package wearapps.com.paintsync;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * Created by elad on 11/4/2014.
 */
public class SettingsDialog extends Dialog implements  ColorPickerDialog.OnColorChangedListener,View.OnClickListener {

    private final Context mContext;
    private SettingsDialogListener mListener;
    private int mBackgroundColor;
    private int mPaintColor;
    private boolean backgroundOrColor;


    public SettingsDialog(Context context,SettingsDialogListener listener,int backgroundColor,int paintColor) {

        super(context);
        mContext = context;
        mListener = listener;
        mBackgroundColor = backgroundColor;
        mPaintColor = paintColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        findViewById(R.id.imageViewBackground).setOnClickListener(this);
        findViewById(R.id.imageViewClear).setOnClickListener(this);
        findViewById(R.id.imageViewColor).setOnClickListener(this);
        findViewById(R.id.imageViewSave).setOnClickListener(this);
        findViewById(R.id.imageViewExit).setOnClickListener(this);
        findViewById(R.id.imageViewShare).setOnClickListener(this);
        findViewById(R.id.imageViewClose).setOnClickListener(this);
        findViewById( R.id.imageViewEraser).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.imageViewBackground:
                backgroundOrColor = true;
                new ColorPickerDialog(mContext, SettingsDialog.this, mBackgroundColor).show();
                break;
            case R.id.imageViewColor:
                backgroundOrColor = false;
                new ColorPickerDialog(mContext, SettingsDialog.this, mPaintColor).show();
                break;
            case R.id.imageViewClear:
                if(mListener!=null)
                    mListener.onClearClicked();
                dismiss();
                break;
            case R.id.imageViewSave:
                if(mListener!=null)
                    mListener.onSaveClicked();
                dismiss();
                break;
            case R.id.imageViewExit:
                if(mListener!=null)
                    mListener.onExitClicked();
                dismiss();
                break;
            case R.id.imageViewShare:
                if(mListener!=null)
                    mListener.onShareClicked();
                dismiss();
                break;
            case R.id.imageViewClose:
                //only exit dialog
                if(mListener!=null)
                    mListener.onColorPicked(0);
                dismiss();
                break;
            case R.id.imageViewEraser:
                //only exit dialog
                if(mListener!=null)
                    mListener.onEraseClicked();
                dismiss();
                break;


            default:
                dismiss();
                break;
        }
    }
    @Override
    public void colorChanged(int color) {

         if(mListener!=null) {

             if(!backgroundOrColor)
                 mListener.onColorPicked(color);
             else
                 mListener.onBackgroundPicked(color);
         }

        dismiss();
    }


}
