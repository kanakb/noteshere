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
     * Musubi sender ID
     */
    public static final String COL_SENDER_ID = "sender_id";
    
    /**
     * Musubi sender name
     */
    public static final String COL_NAME = "sender_name";
    
    /**
     * Whether or not the user created this
     */
    public static final String COL_OWNED = "owned";
    
    /**
     * Text to show
     */
    public static final String COL_TEXT = "note_text";
    
    /**
     * Attachment blob (a picture)
     */
    public static final String COL_ATTACHMENT = "attachment";
    
    public long id;
    public Double latitude;
    public Double longitude;
    public Long timestamp;
    public String senderId;
    public String senderName;
    public Boolean owned;
    public String text;
    public byte[] attachment;
}
