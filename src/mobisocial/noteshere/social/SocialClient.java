package mobisocial.noteshere.social;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.noteshere.App;
import mobisocial.noteshere.db.FollowerManager;
import mobisocial.noteshere.db.MFollower;
import mobisocial.noteshere.db.MNote;
import mobisocial.noteshere.db.NoteManager;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

public class SocialClient {
    private static final String TAG = "SocialClient";
    
    private static final String NOTE = "noteshere_note";
    private static final String HELLO = "noteshere_hello";
    
    private static final String TO = "to";
    
    private final Context mContext;
    private final Musubi mMusubi;
    private final NoteManager mNoteManager;
    @SuppressWarnings("unused")
    private final FollowerManager mFollowerManager;
    
    public SocialClient(Context context, Musubi musubi) {
        mContext = context;
        mMusubi = musubi;
        mNoteManager = new NoteManager(App.getDatabaseSource(mContext));
        mFollowerManager = new FollowerManager(App.getDatabaseSource(mContext));
    }
    
    public void sendToFollowers(List<MNote> notes, Set<MFollower> followers, String exclude) {
        for (MNote note : notes) {
            JSONObject json = noteToJson(note);
            if (json == null) continue;
            
            for (MFollower follower : followers) {
                if (exclude != null && exclude.equals(follower.userId)) continue;
                DbFeed feed = mMusubi.getFeed(follower.feedUri);
                if (feed == null) {
                    Log.w(TAG, "feed no longer exists");
                    continue;
                }
                feed.postObj(new MemObj(NOTE, json));
                Log.d(TAG, "Sending " + MNote.COL_TIMESTAMP + " to " + follower.userId);
            }
        }
    }
    
    public void sendHello(Set<String> to) {
        JSONObject json = new JSONObject();
        JSONArray arr = new JSONArray();
        for (String uid : to) {
            arr.put(uid);
        }
        try {
            json.put(TO, arr);
        } catch (JSONException e) {
            Log.e(TAG, "bad json", e);
            return;
        }
        SharedPreferences p = mContext.getSharedPreferences(App.PREFS_NAME, 0);
        String encodedUri = p.getString(App.PREF_FEED_URI, null);
        if (encodedUri == null) {
            Log.w(TAG, "bad uri");
            return;
        }
        Uri feedUri = Uri.parse(encodedUri);
        DbFeed feed = mMusubi.getFeed(feedUri);
        if (feed == null) {
            Log.w(TAG, "bad feed");
            return;
        }
        feed.postObj(new MemObj(HELLO, json));
    }
    
    public void handleIncomingObj(DbObj obj) {
        if (obj.getSender().isOwned()) return;
        if (obj.getType().equals(NOTE)) {
            
        }
        else if (obj.getType().equals(HELLO)) {
            JSONObject json = obj.getJson();
            if (json != null && json.has(TO)) {
                JSONArray arr = json.optJSONArray(TO);
                DbFeed feed = obj.getContainingFeed();
                boolean ownOne = false;
                for (int i = 0; i < arr.length(); i++) {
                    DbIdentity ident = mMusubi.userForGlobalId(
                            feed.getUri(), arr.optString(i));
                    if (ident != null && ident.isOwned()) {
                        ownOne = true;
                        break;
                    }
                }
                if (ownOne) {
                    // handle the hello
                }
            }
        }
    }
    
    private JSONObject noteToJson(MNote note) {
        try {
            JSONObject json = new JSONObject();
            json.put(MNote.COL_LATITUDE, note.latitude);
            json.put(MNote.COL_LONGITUDE, note.longitude);
            json.put(MNote.COL_TIMESTAMP, note.timestamp);
            json.put(MNote.COL_SENDER_ID, note.senderId);
            json.put(MNote.COL_NAME, note.senderName);
            json.put(MNote.COL_OWNED, note.owned);
            
            if (note.text != null) {
                json.put(MNote.COL_TEXT, note.text);
            }
            
            if (note.attachment != null) {
                json.put(MNote.COL_ATTACHMENT, Base64.encodeToString(note.attachment, Base64.DEFAULT));
            }
            
            return json;
        } catch (JSONException e) {
            Log.e(TAG, "json error", e);
            return null;
        }
    }
    
    @SuppressWarnings("unused")
    private MNote jsonToNote(JSONObject json) {
        try {
            Long timestamp = json.getLong(MNote.COL_TIMESTAMP);
            String senderId = json.getString(MNote.COL_SENDER_ID);
            MNote note = mNoteManager.getNote(timestamp, senderId);
            if (note != null) {
                return note;
            }
            note = new MNote();
            note.latitude = json.getDouble(MNote.COL_LATITUDE);
            note.longitude = json.getDouble(MNote.COL_LONGITUDE);
            note.timestamp = timestamp;
            note.senderId = senderId;
            note.senderName = json.getString(MNote.COL_NAME);
            note.owned = false;
            if (json.has(MNote.COL_TEXT)) {
                note.text = json.getString(MNote.COL_TEXT);
            }
            if (json.has(MNote.COL_ATTACHMENT)) {
                note.attachment = Base64.decode(json.getString(MNote.COL_ATTACHMENT), Base64.DEFAULT);
            }
            mNoteManager.insertNote(note);
            return note;
        } catch (JSONException e) {
            Log.w(TAG, "json error", e);
            return null;
        }
    }
}
