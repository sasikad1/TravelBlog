package uk.ac.wlv.travelblog.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import uk.ac.wlv.travelblog.models.Message;  // Add this import if using model

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BlogApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_MESSAGES = "messages";

    // Common columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_DATE = "created_date";

    // Users table columns
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // Messages table columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_UPDATED_DATE = "updated_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_CREATED_DATE + " TEXT NOT NULL" + ")";
        db.execSQL(createUsersTable);

        // Create messages table
        String createMessagesTable = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER NOT NULL,"
                + COLUMN_TITLE + " TEXT NOT NULL,"
                + COLUMN_CONTENT + " TEXT NOT NULL,"
                + COLUMN_IMAGE_PATH + " TEXT,"
                + COLUMN_CREATED_DATE + " TEXT NOT NULL,"
                + COLUMN_UPDATED_DATE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE" + ")";
        db.execSQL(createMessagesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ==================== USER OPERATIONS ====================

    public long registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_CREATED_DATE, getCurrentDateTime());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE "
                + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS
                + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public String getUserEmail(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_EMAIL + " FROM " + TABLE_USERS
                + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        String email = "";
        if (cursor.moveToFirst()) {
            email = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return email;
    }

    // ==================== CURRENT MESSAGE OPERATIONS (Working) ====================

    public long addMessage(int userId, String title, String content, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_CREATED_DATE, getCurrentDateTime());

        long result = db.insert(TABLE_MESSAGES, null, values);
        db.close();
        return result;
    }

    public Cursor getAllMessagesForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_MESSAGES
                + " WHERE " + COLUMN_USER_ID + " = ?"
                + " ORDER BY " + COLUMN_CREATED_DATE + " DESC";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    public Cursor getMessage(int messageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_MESSAGES
                + " WHERE " + COLUMN_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(messageId)});
    }

    public int updateMessage(int messageId, String title, String content, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_UPDATED_DATE, getCurrentDateTime());

        return db.update(TABLE_MESSAGES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(messageId)});
    }

    public int deleteMessage(int messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MESSAGES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(messageId)});
    }

    public int deleteMultipleMessages(int[] messageIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedCount = 0;
        for (int id : messageIds) {
            deletedCount += db.delete(TABLE_MESSAGES, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});
        }
        db.close();
        return deletedCount;
    }

    public Cursor searchMessages(int userId, String searchQuery) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_MESSAGES
                + " WHERE " + COLUMN_USER_ID + " = ?"
                + " AND (" + COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?)"
                + " ORDER BY " + COLUMN_CREATED_DATE + " DESC";
        String likeQuery = "%" + searchQuery + "%";
        return db.rawQuery(query, new String[]{String.valueOf(userId), likeQuery, likeQuery});
    }

    // ==================== OPTIONAL: MESSAGE MODEL METHODS (Add if you want) ====================

    // Get single message as Message object
    public Message getMessageAsObject(int messageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(messageId)});

        Message message = null;
        if (cursor.moveToFirst()) {
            message = new Message();
            message.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            message.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            message.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
            message.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
            message.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
            message.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_DATE)));
            message.setUpdatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_DATE)));
        }
        cursor.close();
        db.close();
        return message;
    }

    // Get all messages as List of Message objects
    public List<Message> getAllMessagesAsList(int userId) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MESSAGES
                + " WHERE " + COLUMN_USER_ID + " = ?"
                + " ORDER BY " + COLUMN_CREATED_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        while (cursor.moveToNext()) {
            Message message = new Message();
            message.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            message.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            message.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
            message.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
            message.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
            message.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_DATE)));
            message.setUpdatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_DATE)));
            messages.add(message);
        }
        cursor.close();
        db.close();
        return messages;
    }

    // Search messages and return as List
    public List<Message> searchMessagesAsList(int userId, String searchQuery) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MESSAGES
                + " WHERE " + COLUMN_USER_ID + " = ?"
                + " AND (" + COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?)"
                + " ORDER BY " + COLUMN_CREATED_DATE + " DESC";
        String likeQuery = "%" + searchQuery + "%";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), likeQuery, likeQuery});

        while (cursor.moveToNext()) {
            Message message = new Message();
            message.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            message.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            message.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
            message.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
            message.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
            message.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_DATE)));
            message.setUpdatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_DATE)));
            messages.add(message);
        }
        cursor.close();
        db.close();
        return messages;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
    public List<Message> getAllMessagesFromAllUsers() {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MESSAGES
                + " ORDER BY " + COLUMN_CREATED_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            Message message = new Message();
            message.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            message.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            message.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
            message.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
            message.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
            message.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_DATE)));
            message.setUpdatedDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_DATE)));
            messages.add(message);
        }
        cursor.close();
        db.close();
        return messages;
    }

    // Get user email by user ID
    public String getUserEmailById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_EMAIL + " FROM " + TABLE_USERS
                + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        String email = "Unknown";
        if (cursor.moveToFirst()) {
            email = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return email;
    }
}