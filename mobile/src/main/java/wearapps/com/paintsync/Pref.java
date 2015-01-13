package wearapps.com.paintsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Base64;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


public class Pref
{
	// ===========================================================
	// Constants
	// ===========================================================


	// ===========================================================
	// Keys
	// ===========================================================

	private static final String PREF_NAME = "Paint";
	private static final char[] SOD = new char[] { 'a', 'f', 'm', 't', 'h', 'b' };
	// ===========================================================
	// Fields
	// ===========================================================
	private static SharedPreferences sPref;
	private static Editor edit;

	// ===========================================================
	// Initialization
	// ===========================================================

	public static void init(Context context)
	{
		sPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		edit = sPref.edit();
	}

	public static Editor getEditor(Context cxt)
	{
		if (edit == null)
			init(cxt);

		return edit;
	}

	public static SharedPreferences getPrefs(Context cxt)
	{
		if (edit == null)
			init(cxt);

		return sPref;
	}

	// Public

	public static void setPrefString(Context cxt, String key, String value)
	{
		if (edit == null)
			init(cxt);


		edit.putString(key, value);
		edit.commit();
	}

	public static void setPrefLong(Context cxt, String key, long value)
	{
		if (edit == null)
			init(cxt);

		edit.putLong(key, value);
		edit.commit();
	}

	public static void setPrefBoolean(Context cxt, String key, boolean value)
	{
		if (edit == null)
			init(cxt);


		edit.putBoolean(key, value);
		edit.commit();
	}

	public static void setPrefInt(Context cxt, String key, int value)
	{
		if (edit == null)
			init(cxt);


		edit.putInt(key, value);
		edit.commit();
	}

	// GETTERS

	public static String getPrefString(Context cxt, String key)
	{
		if (sPref == null)
			init(cxt);
		String retval = sPref.getString(key, null);

		return retval;
	}

	public static String getPrefString(Context cxt, String key, String def)
	{
		if (sPref == null)
			init(cxt);

		return sPref.getString(key, def);
	}

	public static boolean getPrefBoolean(Context cxt, String key)
	{
		if (sPref == null)
			init(cxt);

		return sPref.getBoolean(key, false);
	}

	public static boolean getPrefBoolean(Context cxt, String key, boolean defValue)
	{
		if (sPref == null)
			init(cxt);

		return sPref.getBoolean(key, defValue);
	}

	public static long getPrefLong(Context cxt, String key)
	{
		if (sPref == null)
			init(cxt);

		return sPref.getLong(key, 0);
	}

	public static int getPrefInt(Context cxt, String key)
	{
		if (sPref == null)
			init(cxt);

		return sPref.getInt(key, 0);
	}
    public static int getPrefGalleryInt(Context cxt, String key)
    {
        if (sPref == null)
            init(cxt);

        return sPref.getInt(key, 3);
    }
	public static String getPrefStringEnc(Context context, String name)
	{
		String key = encrypt(context, name);
		String value = getPrefString(context, key);
		if (null != value)
		{
			value = decrypt(context, value);
		}

		return value;
	}

	public static void setPrefString(String name, String value)
	{

		Editor editor = sPref.edit();
		editor.putString(name, value);
		editor.commit();
	}

	public static void setPrefStringEnc(Context context, String name, String value)
	{
		setPrefString(encrypt(context, name), encrypt(context, value));
	}

	private static String encrypt(Context context, String value)
	{
		try
		{
			final byte[] bytes = value != null ? value.getBytes("utf-8") : new byte[0];
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SOD));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(Secure.ANDROID_ID.getBytes("utf-8"), 20));
			return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), "utf-8");
		} catch (Exception e)
		{

		}
		return null;
	}

	private static String decrypt(Context context, String value)
	{
		try
		{
			final byte[] bytes = value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SOD));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(
					Cipher.DECRYPT_MODE,
					key,
					new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(),
							Settings.Secure.ANDROID_ID).getBytes("utf-8"), 20));
			return new String(pbeCipher.doFinal(bytes), "utf-8");
		} catch (Exception e)
		{

		}
		return null;
	}



}
