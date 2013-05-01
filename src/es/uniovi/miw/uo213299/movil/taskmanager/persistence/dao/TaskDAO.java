package es.uniovi.miw.uo213299.movil.taskmanager.persistence.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import es.uniovi.miw.uo213299.movil.taskmanager.model.Task;
import es.uniovi.miw.uo213299.movil.taskmanager.persistence.SQLiteHelper;
import es.uniovi.miw.uo213299.movil.taskmanager.persistence.TaskDataService;

/**
 * Implementación de la fachada DataService que presenta las operaciones de
 * persistencia
 * 
 * @author Daniel Machado Fernández
 * 
 */
public class TaskDAO implements TaskDataService {

	private SQLiteDatabase database;

	private final SQLiteHelper dbHelper;

	private final String[] allColumns = { SQLiteHelper.COLUMN_ID,
			SQLiteHelper.COLUMN_LAT, SQLiteHelper.COLUMN_LON,
			SQLiteHelper.COLUMN_TAG, SQLiteHelper.COLUMN_TEXT,
			SQLiteHelper.COLUMN_TITLE, SQLiteHelper.COLUMN_VIDEO };

	public TaskDAO(final Context context) {
		dbHelper = new SQLiteHelper(context);
	}

	@Override
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	@Override
	public void close() {
		dbHelper.close();
	}

	@Override
	public long createTask(final Task taskToInsert) {

		final ContentValues values = setContentValues(taskToInsert);

		final long insertId = database.insert(SQLiteHelper.TABLE_TASKS, null,
				values);

		return insertId;
	}

	@Override
	public List<Task> getAllTasks() {

		final List<Task> taskList = new ArrayList<Task>();
		final Cursor cursor = database.query(SQLiteHelper.TABLE_TASKS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {

			final Task task = taskMapper(cursor);

			taskList.add(task);
			cursor.moveToNext();
		}

		cursor.close();

		return taskList;
	}

	@Override
	public List<Task> getAllTasksByTag(String tag) {

		final List<Task> taskList = new ArrayList<Task>();
		final Cursor cursor = database.rawQuery("select * from "
				+ SQLiteHelper.TABLE_TASKS + " where "
				+ SQLiteHelper.COLUMN_TAG + " = ?", new String[] { tag });

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {

			final Task task = taskMapper(cursor);

			taskList.add(task);
			cursor.moveToNext();
		}

		cursor.close();

		return taskList;
	}

	@Override
	public Task findTaskByTitle(String title) {

		final Cursor cursor = database.rawQuery("select * from "
				+ SQLiteHelper.TABLE_TASKS + " where "
				+ SQLiteHelper.COLUMN_TITLE + " = ?", new String[] { title });

		cursor.moveToFirst();

		final Task task = taskMapper(cursor);

		cursor.close();

		return task;
	}

	@Override
	public void delete(Task task) {
		database.delete(SQLiteHelper.TABLE_TASKS, SQLiteHelper.COLUMN_TITLE
				+ " = ?", new String[] { task.getTitle() });

	}

	@Override
	public void updateTask(Task taskToInsert) {

		final ContentValues values = setContentValues(taskToInsert);

		database.update(SQLiteHelper.TABLE_TASKS, values, "_id = "
				+ taskToInsert.getId(), null);

	}

	/**
	 * Refactorización para settear los {@link ContentValues}
	 * 
	 * @param taskToInsert
	 *            task a crear/actualizar
	 * 
	 * @return
	 */
	private ContentValues setContentValues(Task taskToInsert) {
		final ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_LAT, taskToInsert.getLat());
		values.put(SQLiteHelper.COLUMN_LON, taskToInsert.getLon());
		values.put(SQLiteHelper.COLUMN_TAG, taskToInsert.getTag());
		values.put(SQLiteHelper.COLUMN_TEXT, taskToInsert.getText());
		values.put(SQLiteHelper.COLUMN_TITLE, taskToInsert.getTitle());
		values.put(SQLiteHelper.COLUMN_VIDEO, taskToInsert.getVideo());
		return values;
	}

	/**
	 * Mapeador del cursor en una {@link Task}
	 * 
	 * @param cursor
	 * 
	 * @return
	 */
	private Task taskMapper(final Cursor cursor) {
		final Task task = new Task();
		task.setId(cursor.getLong(0));
		task.setLat(cursor.getDouble(1));
		task.setLon(cursor.getDouble(2));
		task.setTag(cursor.getString(3));
		task.setText(cursor.getString(4));
		task.setTitle(cursor.getString(5));
		task.setVideo(cursor.getString(6));
		return task;
	}

}
