package mobisocial.noteshere.db;

import java.util.HashSet;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class FollowingManager extends ManagerBase {
    private static final String[] STANDARD_FIELDS = new String[] {
        MFollowing.COL_ID,
        MFollowing.COL_USER_ID
    };
    
    private static final int userId = 1;
    
    private SQLiteStatement sqlInsertFollowing;
    
    public FollowingManager(SQLiteDatabase db) {
        super(db);
    }
    
    public FollowingManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }
    
    public void insertFollowing(MFollowing following) {
        SQLiteDatabase db = initializeDatabase();
        if (sqlInsertFollowing == null) {
            synchronized(this) {
                if (sqlInsertFollowing == null) {
                    StringBuilder sql = new StringBuilder()
                        .append("INSERT INTO ").append(MFollowing.TABLE)
                        .append("(")
                        .append(MFollowing.COL_USER_ID)
                        .append(") VALUES (?)");
                    sqlInsertFollowing = db.compileStatement(sql.toString());
                }
            }
        }
        synchronized(sqlInsertFollowing) {
            bindField(sqlInsertFollowing, userId, following.userId);
            following.id = sqlInsertFollowing.executeInsert();
        }
    }
    
    public Set<String> getFollowing() {
        SQLiteDatabase db = initializeDatabase();
        String table = MFollowing.TABLE;
        String[] columns = STANDARD_FIELDS;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null, having = null, orderBy = null;
        Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        try {
            Set<String> result = new HashSet<String>();
            while (c.moveToNext()) {
                result.add(c.getString(userId));
            }
            return result;
        } finally {
            c.close();
        }
    }

    @Override
    public void close() {
        if (sqlInsertFollowing != null) {
            sqlInsertFollowing.close();
        }
    }

}
