package mobisocial.noteshere;

import mobisocial.noteshere.db.DatabaseHelper;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public class App extends Application {
    public static final String TAG = "noteshere";
    
    public static final String PREFS_NAME = "noteshere_prefs";
    public static final String PREF_FEED_URI = "feed_uri";
    public static final String PREF_FOLLOWING = "following";
    
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
}
