package mobisocial.noteshere.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseHelper";
    
    private static final String DB_NAME = "noteshere.db";
    private static final int VERSION = 1;
    
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db, MNote.TABLE,
                MNote.COL_ID, "INTEGER PRIMARY KEY",
                MNote.COL_LATITUDE, "REAL NOT NULL",
                MNote.COL_LONGITUDE, "REAL NOT NULL",
                MNote.COL_TIMESTAMP, "INTEGER NOT NULL",
                MNote.COL_SENDER_ID, "TEXT NOT NULL",
                MNote.COL_NAME, "TEXT NOT NULL",
                MNote.COL_OWNED, "INTEGER NOT NULL",
                MNote.COL_TEXT, "TEXT",
                MNote.COL_ATTACHMENT, "BLOB");
        db.execSQL("CREATE INDEX " + MNote.TABLE + "_latlon ON " +
                MNote.TABLE + "(" + MNote.COL_LATITUDE + "," + MNote.COL_LONGITUDE + ")");
        db.execSQL("CREATE INDEX " + MNote.TABLE + "_senderwhen ON " +
                MNote.TABLE + "(" + MNote.COL_TIMESTAMP + "," + MNote.COL_SENDER_ID + ")");
        
        createTable(db, MFollower.TABLE,
                MFollower.COL_ID, "INTEGER PRIMARY KEY",
                MFollower.COL_USER_ID, "TEXT NOT NULL",
                MFollower.COL_FEED_URI, "TEXT NOT NULL");
        db.execSQL("CREATE INDEX " + MFollower.TABLE + "_uid ON " +
                MFollower.TABLE + "(" + MFollower.COL_USER_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= newVersion) {
            return;
        }
        
        if (oldVersion <= 1) {
            // etc...
        }
        
        db.setVersion(VERSION);
    }
    
    private void createTable(SQLiteDatabase db, String tableName, String... cols){
        assert cols.length % 2 == 0;
        String s = "CREATE TABLE " + tableName + " (";
        for(int i = 0; i < cols.length; i += 2){
            s += cols[i] + " " + cols[i + 1];
            if(i < (cols.length - 2)){
                s += ", ";
            }
            else{
                s += " ";
            }
        }
        s += ")";
        Log.i(TAG, s);
        db.execSQL(s);
    }
}
