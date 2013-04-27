package mobisocial.noteshere.social;

import java.util.HashSet;
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
import android.database.Cursor;
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
    private final FollowerManager mFollowerManager;
    private final SharedPreferences mPrefs;
    
    public SocialClient(Context context, Musubi musubi) {
        mContext = context;
        mMusubi = musubi;
        mNoteManager = new NoteManager(App.getDatabaseSource(mContext));
        mFollowerManager = new FollowerManager(App.getDatabaseSource(mContext));
        mPrefs = mContext.getSharedPreferences(App.PREFS_NAME, 0);
    }
    
    public void sendToFollowers(MNote note, Set<MFollower> followers, String exclude) {
        String encodedUri = mPrefs.getString(App.PREF_FEED_URI, null);
        Uri feedUri;
        if (encodedUri == null) {
            feedUri = null;
        } else {
            feedUri = Uri.parse(encodedUri);
        }
        JSONObject json = noteToJson(note, feedUri);
        if (json == null) return;
        
        for (MFollower follower : followers) {
            if (exclude != null && follower.userId.equals(exclude)) continue;
            DbFeed feed = mMusubi.getFeed(follower.feedUri);
            if (feed == null) {
                Log.w(TAG, "feed no longer exists");
                continue;
            }
            feed.postObj(new MemObj(NOTE, json));
            Log.d(TAG, "Sending " + MNote.COL_TIMESTAMP + " to " + follower.userId);
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
        String encodedUri = mPrefs.getString(App.PREF_FEED_URI, null);
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
            String feedString = obj.getContainingFeed().getUri().toString();
            String myFeedString = mPrefs.getString(App.PREF_FEED_URI, null);
            if (feedString.equals(myFeedString)) {
                handleNote(obj);
            }
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
                    handleHello(obj);
                }
            }
        }
    }
    
    private void handleHello(DbObj obj) {
        DbFeed feed = obj.getContainingFeed();
        DbIdentity sender = obj.getSender();
        
        // Send all of my and following notes to a single follower
        MFollower follower = mFollowerManager.ensureFollower(sender.getId(), feed.getUri());
        Set<MFollower> followers = new HashSet<MFollower>();
        followers.add(follower);
        Cursor c = mNoteManager.getOneLevelNoteCursor();
        try {
            while (c.moveToNext()) {
                MNote note = mNoteManager.fillInStandardFields(c);
                sendToFollowers(note, followers, null);
            }
        } finally {
            c.close();
        }
    }
    
    private void handleNote(DbObj obj) {
        JSONObject json = obj.getJson();
        MNote note = jsonToNote(json, obj.getContainingFeed().getUri());
        if (note == null) {
            Log.d(TAG, "bad note");
            return;
        }
        
        if (note.owned || note.followOwned) {
            Set<MFollower> followers = mFollowerManager.getFollowers();
            sendToFollowers(note, followers, obj.getSender().getId());
        }
    }
    
    private JSONObject noteToJson(MNote note, Uri feedUri) {
        try {
            // May have started using this before Musubi was installed
            if (note.owned) {
                DbIdentity ident = mMusubi.userForLocalDevice(feedUri);
                if (ident == null) return null;
                note.senderId = ident.getId();
                note.senderName = ident.getName();
            }
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
    
    private MNote jsonToNote(JSONObject json, Uri feedUri) {
        try {
            Long timestamp = json.getLong(MNote.COL_TIMESTAMP);
            String senderId = json.getString(MNote.COL_SENDER_ID);
            MNote note = mNoteManager.getNote(timestamp);
            if (note != null) {
                if (note.owned) {
                    DbIdentity ident = mMusubi.userForLocalDevice(feedUri);
                    if (ident == null) return null;
                    note.senderId = ident.getId();
                    note.senderName = ident.getName();
                }
                note.followOwned = json.getBoolean(MNote.COL_OWNED);
                return note;
            }
            note = new MNote();
            note.latitude = json.getDouble(MNote.COL_LATITUDE);
            note.longitude = json.getDouble(MNote.COL_LONGITUDE);
            note.timestamp = timestamp;
            note.senderId = senderId;
            note.senderName = json.getString(MNote.COL_NAME);
            note.owned = false;
            note.followOwned = json.getBoolean(MNote.COL_OWNED);
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
