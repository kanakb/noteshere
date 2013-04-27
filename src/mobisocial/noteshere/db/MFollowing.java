package mobisocial.noteshere.db;

public class MFollowing {
    public static final String TABLE = "following";
    
    /**
     * Primary identifier
     */
    public static final String COL_ID = "_id";
    
    /**
     * User identifier
     */
    public static final String COL_USER_ID = "user_id";
    
    public long id;
    public String userId;
}
