package es.uniovi.miw.uo213299.movil.taskmanager.activity;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import es.uniovi.miw.uo213299.movil.taskmanager.R;
import es.uniovi.miw.uo213299.movil.taskmanager.model.Task;
import es.uniovi.miw.uo213299.movil.taskmanager.persistence.TaskDataService;
import es.uniovi.miw.uo213299.movil.taskmanager.persistence.dao.TaskDAO;

/**
 * Muestra las tareas agrupadas por una categoría
 * 
 * @author Daniel Machado Fernández
 * 
 */
public class TagsActivity extends ListActivity {

	private String tag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tags);

		// Rellena el listado con las tareas de la categoría
		fillTasks(getIntent().getExtras().getString("tag"));

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Cuando hay un click, se muestra el detalle de la tarea sobre la que
		// se generó el evento
		Intent myIntent = new Intent(TagsActivity.this, DetailTaskActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("title", l.getItemAtPosition(position).toString());
		myIntent.putExtras(bundle);

		startActivity(myIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tags, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Se comprueba que se presiona la acción del menú de la nueva tarea
		switch (item.getItemId()) {
		case R.id.mn_new_task:
			Intent myIntent = new Intent(TagsActivity.this, NewTaskActivity.class);
			startActivityForResult(myIntent, 23);
			break;

		}
		return true;
	}

	@Override
	public void onResume() {
		// Se ha redefinido este metodo por ser el que se llama cada vez que se
		// retorna el foco a la Activity. De esta forma puedo actualizar el
		// listado cuando se cree / edite una tarea
		super.onResume();
		tag = getIntent().getExtras().getString("tag");
		fillTasks(tag);

	}

	/**
	 * Rellena el listado con las tareas disponibles para la categoría
	 * 
	 * @param tag
	 */
	private void fillTasks(String tag) {

		TaskDataService taskDAO = new TaskDAO(getApplicationContext());
		taskDAO.open();
		List<Task> tasks;

		if (tag == null || tag.contains("todas")) {
			tasks = taskDAO.getAllTasks();
		} else {
			tasks = taskDAO.getAllTasksByTag(tag);
		}
		taskDAO.close();

		ArrayAdapter<Task> adapter = new ArrayAdapter<Task>(this,
				android.R.layout.simple_list_item_1, tasks);

		setListAdapter(adapter);
	}

}
