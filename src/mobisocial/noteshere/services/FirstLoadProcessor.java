package mobisocial.noteshere.services;

import java.util.HashSet;
import java.util.Set;

import mobisocial.noteshere.App;
import mobisocial.noteshere.social.SocialClient;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class FirstLoadProcessor extends ContentObserver {
    public static final String TAG = "FirstLoadProcessor";
    
    private final Context mContext;
    
    public static FirstLoadProcessor newInstance(Context context) {
        HandlerThread thread = new HandlerThread("FirstLoadProcessorThread");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return new FirstLoadProcessor(context, thread);
    }
    
    private FirstLoadProcessor(Context context, HandlerThread thread) {
        super(new Handler(thread.getLooper()));
        mContext = context;
    }
    
    @Override
    public void onChange(boolean selfChange) {
        Log.d(TAG, "first load check");
        if (!Musubi.isMusubiInstalled(mContext)) {
            mContext.getContentResolver().notifyChange(App.URI_APP_SETUP_COMPLETE, null);
            return;
        }
        Musubi musubi = Musubi.getInstance(mContext);
        
        SharedPreferences p = mContext.getSharedPreferences(App.PREFS_NAME, 0);
        String feedString = p.getString(App.PREF_FEED_URI, null);
        if (feedString == null) {
            mContext.getContentResolver().notifyChange(App.URI_APP_SETUP_COMPLETE, null);
            return;
        }
        
        Uri feedUri = Uri.parse(feedString);
        DbIdentity me = musubi.userForLocalDevice(feedUri);
        if (me == null) {
            mContext.getContentResolver().notifyChange(App.URI_APP_SETUP_COMPLETE, null);
            return;
        }
        
        Log.d(TAG, "getting hellos");
        
        Cursor c = musubi.queryAppData(
                new String[] {
                        DbObj.COL_APP_ID, DbObj.COL_TYPE, DbObj.COL_STRING_KEY, DbObj.COL_JSON,
                        DbObj.COL_RAW, DbObj.COL_IDENTITY_ID, DbObj.COL_UNIVERSAL_HASH,
                        DbObj.COL_FEED_ID, DbObj.COL_INT_KEY, DbObj.COL_TIMESTAMP, DbObj.COL_PARENT_ID
                }, DbObj.COL_TYPE + "=?", new String[] { SocialClient.HELLO }, null);
        SocialClient sc = new SocialClient(mContext, musubi);
        Set<Long> senders = new HashSet<Long>();
        try {
            while (c != null && c.moveToNext()) {
                DbObj obj = musubi.objForCursor(c);
                if (obj == null) continue;
                // process the hello once per sender
                Long sender = obj.getSenderId();
                if (!senders.contains(sender)) {
                    senders.add(sender);
                    sc.handleIncomingObj(obj);
                }
            }
        } finally {
            if (c != null) c.close();
        }
        
        p.edit().putBoolean(App.PREF_APP_SETUP_COMPLETE, true).commit();
        mContext.getContentResolver().notifyChange(App.URI_APP_SETUP_COMPLETE, null);
    }

}
