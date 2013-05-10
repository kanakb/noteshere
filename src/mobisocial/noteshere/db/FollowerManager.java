package mobisocial.noteshere.db;

import java.util.HashSet;
import java.util.Set;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;


public class FollowerManager extends ManagerBase {

    private static final String[] STANDARD_FIELDS = new String[] {
        MFollower.COL_ID,
        MFollower.COL_USER_ID,
        MFollower.COL_FEED_URI
    };
    
    private static final int _id = 0;
    private static final int userId = 1;
    private static final int feedUri = 2;
    
    private SQLiteStatement sqlInsertFollower;
    private SQLiteStatement sqlUpdateFollower;

    public FollowerManager(SQLiteDatabase db) {
        super(db);
    }
    
    public FollowerManager(SQLiteOpenHelper databaseSource) {
        super(databaseSource);
    }
    
    /**
     * Add a follower
     * @param follower Details about the follower
     */
    public void insertFollower(MFollower follower) {
        SQLiteDatabase db = initializeDatabase();
        if (sqlInsertFollower == null) {
            synchronized(this) {
                if (sqlInsertFollower == null) {
                    StringBuilder sql = new StringBuilder()
                        .append("INSERT INTO ").append(MFollower.TABLE)
                        .append("(")
                        .append(MFollower.COL_USER_ID).append(",")
                        .append(MFollower.COL_FEED_URI)
                        .append(") VALUES (?,?)");
                    sqlInsertFollower = db.compileStatement(sql.toString());
                }
            }
        }
        synchronized(sqlInsertFollower) {
            bindField(sqlInsertFollower, userId, follower.userId);
            bindField(sqlInsertFollower, feedUri, follower.feedUri);
            follower.id = sqlInsertFollower.executeInsert();
        }
    }

    
    /**
     * Update a follower entry
     * @param follower MFollower object
     */
    public void updateFollower(MFollower follower) {
        SQLiteDatabase db = initializeDatabase();
        if (sqlUpdateFollower == null) {
            synchronized(this) {
                if (sqlUpdateFollower == null) {
                    StringBuilder sql = new StringBuilder()
                        .append("UPDATE ").append(MFollower.TABLE)
                        .append(" SET ")
                        .append(MFollower.COL_USER_ID).append("=?,")
                        .append(MFollower.COL_FEED_URI).append("=?")
                        .append(" WHERE ").append(MFollower.COL_ID).append("=?");
                    sqlUpdateFollower = db.compileStatement(sql.toString());
                }
            }
        }
        synchronized(sqlUpdateFollower) {
            bindField(sqlUpdateFollower, userId, follower.userId);
            bindField(sqlUpdateFollower, feedUri, follower.feedUri);
            bindField(sqlUpdateFollower, 3, follower.id);
            sqlUpdateFollower.executeUpdateDelete();
        }
    }
    
    /**
     * Get a follower or insert one if none exists.
     * @param userId String that identifies the follower
     * @param feedUri Uri to reach the follower
     * @return MFollower object
     */
    public MFollower ensureFollower(String userId, Uri feedUri) {
        SQLiteDatabase db = initializeDatabase();
        db.beginTransaction();
        try {
            MFollower follower = getFollower(userId);
            if (follower != null && !follower.feedUri.equals(feedUri)) {
                follower.feedUri = feedUri;
                updateFollower(follower);
            } else if (follower == null) {
                follower = new MFollower();
                follower.userId = userId;
                follower.feedUri = feedUri;
                insertFollower(follower);
            }
            db.setTransactionSuccessful();
            return follower;
        } finally {
            db.endTransaction();
        }
    }
    
    /**
     * Get all followers
     * @param type EntryType of followers
     * @return Set of MFollower objects
     */
    public Set<MFollower> getFollowers() {
        SQLiteDatabase db = initializeDatabase();
        String table = MFollower.TABLE;
        String[] columns = STANDARD_FIELDS;
        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null, having = null, orderBy = null;
        Cursor c = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        try {
            Set<MFollower> followers = new HashSet<MFollower>();
            while (c.moveToNext()) {
                followers.add(fillInStandardFields(c));
            }
            return followers;
        } finally {
            c.close();
        }
    }
    
    /**
     * Determine if we know how to reach a follower
     * @param userId Unique user identifier
     * @return MFollower object
     */
    public MFollower getFollower(String userId) {
        SQLiteDatabase db = initializeDatabase();
        String table = MFollower.TABLE;
        String[] columns = STANDARD_FIELDS;
        String selection = MFollower.COL_USER_ID + "=?";
        String[] selectionArgs = new String[] { userId };
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

    private MFollower fillInStandardFields(Cursor c) {
        MFollower follower = new MFollower();
        follower.id = c.getLong(_id);
        follower.userId = c.getString(userId);
        follower.feedUri = Uri.parse(c.getString(feedUri));
        return follower;
    }
    
    @Override
    public void close() {
        if (sqlInsertFollower != null) {
            sqlInsertFollower.close();
        }

        if (sqlUpdateFollower != null) {
            sqlUpdateFollower.close();
        }
    }

}
