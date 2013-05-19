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
        MNote.COL_DESCRIPTION,
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
        MNote.COL_OWNED,
        MNote.COL_DESCRIPTION
    };
    
    private static final int _id = 0;
    private static final int latitude = 1;
    private static final int longitude = 2;
    private static final int timestamp = 3;
    private static final int senderId = 4;
    private static final int name = 5;
    private static final int owned = 6;
    private static final int description = 7;
    private static final int text = 8;
    private static final int attachment = 9;
    
    private SQLiteStatement sqlInsertNote;
    private SQLiteStatement sqlUpdateSenderName;
    
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
                        .append(MNote.COL_DESCRIPTION).append(",")
                        .append(MNote.COL_TEXT).append(",")
                        .append(MNote.COL_ATTACHMENT)
                        .append(") VALUES (?,?,?,?,?,?,?,?,?)");
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
            bindField(sqlInsertNote, description, note.description);
            bindField(sqlInsertNote, text, note.text);
            bindField(sqlInsertNote, attachment, note.attachment);
            note.id = sqlInsertNote.executeInsert();
        }
    }
    
    public void updateSenderName(MNote note) {
        SQLiteDatabase db = initializeDatabase();
        if (sqlUpdateSenderName == null) {
            synchronized(this) {
                if (sqlUpdateSenderName == null) {
                    StringBuilder sql = new StringBuilder()
                        .append("UPDATE ").append(MNote.TABLE)
                        .append(" SET ")
                        .append(MNote.COL_NAME).append("=?")
                        .append(" WHERE ").append(MNote.COL_ID).append("=?");
                    sqlUpdateSenderName = db.compileStatement(sql.toString());
                }
            }
        }
        synchronized(sqlUpdateSenderName) {
            bindField(sqlUpdateSenderName, 1, note.senderName);
            bindField(sqlUpdateSenderName, 2, note.id);
            sqlUpdateSenderName.executeUpdateDelete();
        }
    }
    
    public boolean deleteNote(Long id) {
        SQLiteDatabase db = initializeDatabase();
        String table = MNote.TABLE;
        String whereClause = MNote.COL_ID + "=?";
        String[] whereArgs = new String[] { id.toString() };
        return db.delete(table, whereClause, whereArgs) > 0;
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
    
    public Cursor getNearbyNoteCursor(Double latitude, Double longitude) {
        SQLiteDatabase db = initializeDatabase();
        String table = MNote.TABLE;
        String[] columns = LIMITED_FIELDS;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null, having = null;
        String orderBy = "((" + latitude.toString() + " - " + MNote.COL_LATITUDE + ") * " +
                "(" + latitude.toString() + " - " + MNote.COL_LATITUDE + ") + " +
                "(" + longitude.toString() + " - " + MNote.COL_LONGITUDE + ") * " +
                "(" + longitude.toString() + " - " + MNote.COL_LONGITUDE + ")) ASC";
        String limit = "100";
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
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
        if (!c.isNull(description)) {
            note.description = c.getString(description);
        }
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
