package mobisocial.noteshere.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class NoteManager extends ManagerBase {
    private static final String[] STANDARD_FIELDS = new String[] {
        MNote.COL_ID,
        MNote.COL_LATITUDE,
        MNote.COL_LONGITUDE,
        MNote.COL_TIMESTAMP,
        MNote.COL_SENDER_ID,
        MNote.COL_NAME,
        MNote.COL_OWNED,
        MNote.COL_TEXT,
        MNote.COL_ATTACHMENT
    };
    
    private static final String[] LIMITED_FIELDS = new String[] {
        MNote.COL_ID,
        MNote.COL_LATITUDE,
        MNote.COL_LONGITUDE,
        MNote.COL_TIMESTAMP,
        MNote.COL_SENDER_ID,
        MNote.COL_NAME,
        MNote.COL_OWNED
    };
    
    private static final int _id = 0;
    private static final int latitude = 1;
    private static final int longitude = 2;
    private static final int timestamp = 3;
    private static final int senderId = 4;
    private static final int name = 5;
    private static final int owned = 6;
    private static final int text = 7;
    private static final int attachment = 8;
    
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
                        .append(MNote.COL_SENDER_ID).append(",")
                        .append(MNote.COL_NAME).append(",")
                        .append(MNote.COL_OWNED).append(",")
                        .append(MNote.COL_TEXT).append(",")
                        .append(MNote.COL_ATTACHMENT)
                        .append(") VALUES (?,?,?,?,?,?,?,?)");
                    sqlInsertNote = db.compileStatement(sql.toString());
                }
            }
        }
        synchronized(sqlInsertNote) {
            bindField(sqlInsertNote, latitude, note.latitude);
            bindField(sqlInsertNote, longitude, note.longitude);
            bindField(sqlInsertNote, timestamp, note.timestamp);
            bindField(sqlInsertNote, senderId, note.senderId);
            bindField(sqlInsertNote, name, note.senderName);
            bindField(sqlInsertNote, owned, note.owned);
            bindField(sqlInsertNote, text, note.text);
            bindField(sqlInsertNote, attachment, note.attachment);
            note.id = sqlInsertNote.executeInsert();
        }
    }
    
    public MNote getNote(long id) {
        SQLiteDatabase db = initializeDatabase();
        String table = MNote.TABLE;
        String[] columns = STANDARD_FIELDS;
        String selection = MNote.COL_ID + "=?";
        String[] selectionArgs = new String[] { Long.toString(id) };
        String groupBy = null, having = null, orderBy = null;
        Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        try {
            if (c.moveToFirst()) {
                return fillInStandardFields(c);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
    }
    
    public MNote getNote(Long timestamp) {
        SQLiteDatabase db = initializeDatabase();
        String table = MNote.TABLE;
        String[] columns = LIMITED_FIELDS;
        String selection = MNote.COL_TIMESTAMP + "=?";
        String[] selectionArgs = new String[] {
                timestamp.toString()
        };
        String groupBy = null, having = null, orderBy = null;
        Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        try {
            if (c.moveToFirst()) {
                return fillInLimitedFields(c);
            } else {
                return null;
            }
        } finally {
            c.close();
        }
    }
    
    public Cursor getOneLevelNoteCursor() {
        SQLiteDatabase db = initializeDatabase();
        String table = MNote.TABLE;
        String[] columns = STANDARD_FIELDS;
        String innerSelection = "SELECT " + MFollowing.COL_USER_ID + " FROM " + MFollowing.TABLE;
        String selection = "(" + MNote.COL_OWNED + "=1) OR (" + MNote.COL_SENDER_ID + " IN (" +
                innerSelection + "))";
        String[] selectionArgs = null;
        String groupBy = null, having = null, orderBy = null;
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }
    
    public Cursor getMyNotesCursor() {
        SQLiteDatabase db = initializeDatabase();
        String table = MNote.TABLE;
        String[] columns = LIMITED_FIELDS;
        String selection = MNote.COL_OWNED + "=1";
        String[] selectionArgs = null;
        String groupBy = null, having = null;
        String orderBy = MNote.COL_ID + " DESC";
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }
    
    public MNote fillInLimitedFields(Cursor c) {
        MNote note = new MNote();
        note.id = c.getLong(_id);
        note.latitude = c.getDouble(latitude);
        note.longitude = c.getDouble(longitude);
        note.timestamp = c.getLong(timestamp);
        note.senderId = c.getString(senderId);
        note.senderName = c.getString(name);
        note.owned = (c.getLong(owned) == 0L) ? false : true;
        return note;
    }
    
    public MNote fillInStandardFields(Cursor c) {
        MNote note = fillInLimitedFields(c);
        if (!c.isNull(text)) {
            note.text = c.getString(text);
        }
        if (!c.isNull(attachment)) {
            note.attachment = c.getBlob(attachment);
        }
        return note;
    }

    @Override
    public void close() {
        if (sqlInsertNote != null) {
            sqlInsertNote.close();
        }
    }

}
