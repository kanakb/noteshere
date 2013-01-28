package mobisocial.noteshere;

import mobisocial.noteshere.db.DatabaseHelper;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public class App extends Application {
    public static final String TAG = "noteshere";
    
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
