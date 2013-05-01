package es.uniovi.miw.uo213299.movil.taskmanager.persistence;

import java.util.List;

import android.database.SQLException;
import es.uniovi.miw.uo213299.movil.taskmanager.model.Task;

/**
 * Fachada que separa las operaciones de persistencia para el modelo
 * {@link Task}
 * 
 * @author Daniel Machado Fernández
 * 
 */
public interface TaskDataService {

	/**
	 * Abre una conexión con la base de datos
	 * 
	 * @throws SQLException
	 */
	public abstract void open() throws SQLException;

	/**
	 * Cierra la conexión con la base de datos
	 */
	public abstract void close();

	/**
	 * Almacena una {@link Task} en la base de datos
	 * 
	 * @param taskToInsert
	 * 
	 * @return id
	 */
	public abstract long createTask(final Task taskToInsert);

	/**
	 * Obtiene todas las {@link Task} creadas en al aplicación
	 * 
	 * @return
	 */
	public abstract List<Task> getAllTasks();

	/**
	 * Obtiene las {@link Task} asociadas a una tag concreta
	 * 
	 * @param tag
	 * 
	 * @return
	 */
	public abstract List<Task> getAllTasksByTag(String tag);

	/**
	 * Obtiene una {@link Task} concreta
	 * 
	 * @param title
	 * 
	 * @return
	 */
	public abstract Task findTaskByTitle(String title);

	/**
	 * 
	 * 
	 * @param task
	 */
	public abstract void delete(Task task);

	public abstract void updateTask(Task taskToInsert);

}