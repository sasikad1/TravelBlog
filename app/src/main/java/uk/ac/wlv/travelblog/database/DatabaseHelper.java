package uk.ac.wlv.travelblog.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BlogApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_MESSAGES = "messages";

    // Common columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_DATE = "created_date";

    // Users table columns (username removed)
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
        // Create users table (without username)
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

    // Register new user (email + password only)
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

    // Check if user exists (for login) - using email
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

    // Get user ID by email
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

    // Check if email exists
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Get user email by ID (for dashboard)
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

    // ==================== MESSAGE OPERATIONS ====================

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

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}