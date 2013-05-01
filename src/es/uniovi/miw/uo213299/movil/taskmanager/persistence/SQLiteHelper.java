package es.uniovi.miw.uo213299.movil.taskmanager.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Clase necesaria para utilizar la base de datos SQLite interna del dispositivo
 * 
 * @author Daniel Machado Fernández
 * 
 */
public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_TASKS = "tasks";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_TEXT = "text";
	public static final String COLUMN_TAG = "tag";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LON = "lon";
	public static final String COLUMN_VIDEO = "video";

	private static final String DATABASE_NAME = "tasks.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table " + TABLE_TASKS
			+ "( " + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_LAT + " float, " + COLUMN_LON + " float, " + COLUMN_TAG
			+ " string not null, " + COLUMN_TEXT + " string not null, "
			+ COLUMN_TITLE + " string not null, " + COLUMN_VIDEO + " string"
			+ ");";

	private static final String DATABASE_DROP = "DROP TABLE IF EXISTS "
			+ TABLE_TASKS;

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("UPDATE", "updating database from version " + oldVersion
				+ " to version " + newVersion);
		db.execSQL(DATABASE_DROP);
		this.onCreate(db);
	}

}
