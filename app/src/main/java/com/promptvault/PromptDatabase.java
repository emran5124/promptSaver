package com.promptvault;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class PromptDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "promptvault.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE = "prompts";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_CONTENT = "content";
    private static final String COL_CATEGORY = "category";
    private static final String COL_TAGS = "tags";
    private static final String COL_AI_MODEL = "ai_model";
    private static final String COL_CREATED = "created_at";
    private static final String COL_UPDATED = "updated_at";
    private static final String COL_FAVORITE = "is_favorite";

    private static PromptDatabase instance;

    public static synchronized PromptDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new PromptDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private PromptDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " TEXT PRIMARY KEY," +
                COL_TITLE + " TEXT NOT NULL," +
                COL_CONTENT + " TEXT NOT NULL," +
                COL_CATEGORY + " TEXT," +
                COL_TAGS + " TEXT," +
                COL_AI_MODEL + " TEXT," +
                COL_CREATED + " INTEGER," +
                COL_UPDATED + " INTEGER," +
                COL_FAVORITE + " INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void insertOrUpdate(Prompt p) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toContentValues(p);
        db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void insertOrUpdateAll(List<Prompt> prompts) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Prompt p : prompts) {
                db.insertWithOnConflict(TABLE, null, toContentValues(p), SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public boolean delete(String id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE, COL_ID + "=?", new String[]{id}) > 0;
    }

    public List<Prompt> getAll() {
        return query(null, null, COL_UPDATED + " DESC");
    }

    public List<Prompt> getFavorites() {
        return query(COL_FAVORITE + "=1", null, COL_UPDATED + " DESC");
    }

    public List<Prompt> getByCategory(String category) {
        return query(COL_CATEGORY + "=?", new String[]{category}, COL_UPDATED + " DESC");
    }

    public List<Prompt> search(String q) {
        String like = "%" + q + "%";
        return query(
                COL_TITLE + " LIKE ? OR " + COL_CONTENT + " LIKE ? OR " + COL_TAGS + " LIKE ? OR " + COL_AI_MODEL + " LIKE ?",
                new String[]{like, like, like, like},
                COL_UPDATED + " DESC"
        );
    }

    public List<String> getAllCategories() {
        List<String> cats = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT " + COL_CATEGORY + " FROM " + TABLE +
                " WHERE " + COL_CATEGORY + " IS NOT NULL AND " + COL_CATEGORY + " != '' ORDER BY " + COL_CATEGORY, null);
        while (c.moveToNext()) cats.add(c.getString(0));
        c.close();
        return cats;
    }

    public Prompt getById(String id) {
        List<Prompt> list = query(COL_ID + "=?", new String[]{id}, null);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<Prompt> query(String selection, String[] args, String orderBy) {
        List<Prompt> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE, null, selection, args, null, null, orderBy);
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();
        return list;
    }

    private ContentValues toContentValues(Prompt p) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ID, p.getId());
        cv.put(COL_TITLE, p.getTitle());
        cv.put(COL_CONTENT, p.getContent());
        cv.put(COL_CATEGORY, p.getCategory());
        cv.put(COL_TAGS, p.getTags());
        cv.put(COL_AI_MODEL, p.getAiModel());
        cv.put(COL_CREATED, p.getCreatedAt());
        cv.put(COL_UPDATED, p.getUpdatedAt());
        cv.put(COL_FAVORITE, p.isFavorite() ? 1 : 0);
        return cv;
    }

    private Prompt fromCursor(Cursor c) {
        return new Prompt(
                c.getString(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
                c.getString(c.getColumnIndexOrThrow(COL_CONTENT)),
                c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)),
                c.getString(c.getColumnIndexOrThrow(COL_TAGS)),
                c.getString(c.getColumnIndexOrThrow(COL_AI_MODEL)),
                c.getLong(c.getColumnIndexOrThrow(COL_CREATED)),
                c.getLong(c.getColumnIndexOrThrow(COL_UPDATED)),
                c.getInt(c.getColumnIndexOrThrow(COL_FAVORITE)) == 1
        );
    }
}
