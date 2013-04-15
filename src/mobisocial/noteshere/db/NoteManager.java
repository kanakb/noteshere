package mobisocial.noteshere.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class NoteManager extends ManagerBase {
    private static final String[] STANDARD_FIELDS = new String[] {
        MNote.COL_ID,
        MNote.COL_LATITUDE,
        MNote.COL_LONGITUDE,
        MNote.COL_TIMESTAMP,
        MNote.COL_OBJ_URI,
        MNote.COL_SENDER_ID,
        MNote.COL_TEXT
    };
    
    private static final int _id = 0;
    private static final int latitude = 1;
    private static final int longitude = 2;
    private static final int timestamp = 3;
    private static final int objUri = 4;
    private static final int senderId = 5;
    private static final int text = 6;
    
    private SQLiteStatement sqlInsertNote;
    
    public NoteManager(SQLiteDatabase db) {
        super(db);
    }

    public NoteManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }
    
    public void insertNote(MNote note) {
        SQLiteDatabase db = initializeDatabase();
        if (sqlInsertNote == null) {
            synchronized(this) {
                if (sqlInsertNote == null) {
                    StringBuilder sql = new StringBuilder()
                        .append("INSERT INTO ").append(MNote.TABLE)
                        .append("(")
                        .append(MNote.COL_LATITUDE).append(",")
                        .append(MNote.COL_LONGITUDE).append(",")
                        .append(MNote.COL_TIMESTAMP).append(",")
                        .append(MNote.COL_OBJ_URI).append(",")
                        .append(MNote.COL_SENDER_ID).append(",")
                        .append(MNote.COL_TEXT)
                        .append(") VALUES (?,?,?,?,?,?)");
                    sqlInsertNote = db.compileStatement(sql.toString());
                }
            }
        }
        synchronized(sqlInsertNote) {
            bindField(sqlInsertNote, latitude, note.latitude);
            bindField(sqlInsertNote, longitude, note.longitude);
            bindField(sqlInsertNote, timestamp, note.timestamp);
            bindField(sqlInsertNote, objUri, note.objUri);
            bindField(sqlInsertNote, senderId, note.senderId);
            bindField(sqlInsertNote, text, note.text);
            note.id = sqlInsertNote.executeInsert();
        }
    }

    @Override
    public void close() {
        if (sqlInsertNote != null) {
            sqlInsertNote.close();
        }
    }

}
