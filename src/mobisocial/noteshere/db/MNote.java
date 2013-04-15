package mobisocial.noteshere.db;

import android.net.Uri;

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
     * Musubi object uri
     */
    public static final String COL_OBJ_URI = "obj_uri";
    
    /**
     * Musubi sender ID
     */
    public static final String COL_SENDER_ID = "sender_id";
    
    /**
     * Text to show
     */
    public static final String COL_TEXT = "note_text";
    
    public long id;
    public Double latitude;
    public Double longitude;
    public Long timestamp;
    public Uri objUri;
    public String senderId;
    public String text;
}
