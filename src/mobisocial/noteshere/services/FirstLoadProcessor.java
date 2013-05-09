package mobisocial.noteshere.services;

import mobisocial.noteshere.App;
import mobisocial.noteshere.social.SocialClient;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;

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
        if (!Musubi.isMusubiInstalled(mContext)) {
            mContext.getContentResolver().notifyChange(App.URI_APP_SETUP_COMPLETE, null);
            return;
        }
        Musubi musubi = Musubi.getInstance(mContext);
        
        DbIdentity me = musubi.userForLocalDevice(null);
        if (me == null) {
            mContext.getContentResolver().notifyChange(App.URI_APP_SETUP_COMPLETE, null);
            return;
        }
        
        Cursor c = musubi.queryAppData(
                new String[] {
                        DbObj.COL_APP_ID, DbObj.COL_TYPE, DbObj.COL_STRING_KEY, DbObj.COL_JSON,
                        DbObj.COL_RAW, DbObj.COL_IDENTITY_ID, DbObj.COL_UNIVERSAL_HASH,
                        DbObj.COL_FEED_ID, DbObj.COL_INT_KEY, DbObj.COL_TIMESTAMP, DbObj.COL_PARENT_ID
                }, DbObj.COL_TYPE + "=?", new String[] { SocialClient.HELLO }, null);
        SocialClient sc = new SocialClient(mContext, musubi);
        try {
            while (c != null && c.moveToNext()) {
                DbObj obj = musubi.objForCursor(c);
                if (obj == null) continue;
                sc.handleIncomingObj(obj);
            }
        } finally {
            if (c != null) c.close();
        }
        
        mContext.getSharedPreferences(App.PREFS_NAME, 0)
            .edit().putBoolean(App.PREF_APP_SETUP_COMPLETE, true);
        mContext.getContentResolver().notifyChange(App.URI_APP_SETUP_COMPLETE, null);
    }

}
