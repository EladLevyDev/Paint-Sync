package wearapps.com.paintsync;

/**
 * Created by elad on 11/4/2014.
 */
public interface SettingsDialogListener{

    public void onColorPicked(int color);
    public void onBackgroundPicked(int color);
    public void onSaveClicked();
    public void onClearClicked();
    public void onShareClicked();
    public void onExitClicked();
    public void onEraseClicked();

}
