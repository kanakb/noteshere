package mobisocial.noteshere;

import mobisocial.noteshere.db.DatabaseHelper;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class App extends Application {
    public static final String TAG = "noteshere";

    private static final String URI_SCHEME = "content://";
    private static final String URI_AUTHORITY = "mobisocial.noteshere.db";
    public static final Uri URI_NOTE_AVAILABLE = Uri.parse(
            URI_SCHEME + URI_AUTHORITY + "/note_available");
    public static final Uri URI_APP_SETUP = Uri.parse(
            URI_SCHEME + URI_AUTHORITY + "/app_setup");
    public static final Uri URI_APP_SETUP_COMPLETE = Uri.parse(
            URI_SCHEME + URI_AUTHORITY + "/app_setup_complete");
    
    public static final String PREFS_NAME = "noteshere_prefs";
    public static final String PREF_FEED_URI = "feed_uri";
    public static final String PREF_FOLLOWING = "following";
    public static final String PREF_APP_SETUP_COMPLETE = "app_setup_complete";
    
    private SQLiteOpenHelper mDatabaseSource;
    
    public static SQLiteOpenHelper getDatabaseSource(Context c) {
        Context appAsContext = c.getApplicationContext();
        return ((App)appAsContext).getDatabaseSource();
    }
    
    public synchronized SQLiteOpenHelper getDatabaseSource() {
        if (mDatabaseSource == null) {
            mDatabaseSource = new DatabaseHelper(getApplicationContext());
        }
        return mDatabaseSource;
    }
    
    public static Uri getNoteUri(long id) {
        return Uri.parse(URI_SCHEME + URI_AUTHORITY + "/note/" + id);
    }
}
