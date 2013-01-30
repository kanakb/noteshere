package mobisocial.noteshere.db;

public class MNote {
    public static final String TABLE = "notes";

    /**
     * Primary identifier
     */
    public static final String COL_ID = "id";
    
    /**
     * Latitude coordinate
     */
    public static final String COL_LATITUDE = "latitude";
    
    /**
     * Longitude coordinate
     */
    public static final String COL_LONGITUDE = "longitude";
    
    /**
     * Unix time that this note was created
     */
    public static final String COL_TIMESTAMP = "timestamp";
    
    /**
     * Musubi feed ID
     */
    public static final String COL_OBJ_ID = "obj_id";
    
    /**
     * Musubi sender ID
     */
    public static final String COL_SENDER_ID = "sender_id";
    
    public long id;
    public Double latitude;
    public Double longitude;
    public Long timestamp;
    public Long objId;
    public Long senderId;
}
