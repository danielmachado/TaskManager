package es.uniovi.miw.uo213299.movil.taskmanager.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import es.uniovi.miw.uo213299.movil.taskmanager.R;
import es.uniovi.miw.uo213299.movil.taskmanager.model.Task;
import es.uniovi.miw.uo213299.movil.taskmanager.persistence.TaskDataService;
import es.uniovi.miw.uo213299.movil.taskmanager.persistence.dao.TaskDAO;

/**
 * Activity que muestra el formulario de creación de una nueva {@link Task}
 * 
 * @author Daniel Machado Fernández
 * 
 */
public class NewTaskActivity extends Activity implements OnItemSelectedListener {

	protected static final int VIDEO_INTENT = 12;
	private Task task = new Task();
	private Location nowLoc;
	private LocationManager mLocationManager;

	private static final int TEN_SECONDS = 10000;
	private static final int TEN_METERS = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_task);

		Bundle bundle = getIntent().getExtras();

		// Comprobamos si es una nueva tarea o una edición de una ya creada
		if (bundle != null) {
			((TextView) findViewById(R.id.tvTitle)).setText("Editar Tarea");
			fillFields(bundle);
			((Button) findViewById(R.id.bt_create)).setText("Editar");
		}

		// Rellenamos el Spinner con las categorias que puede tener una tarea
		setUpSpinner();
		// Preparamos el escenario para la grabación del video
		setUpVideo();
		// Preparamos el escenario para obtener la geolocalización de la tarea
		setUpGeo();

		Button mButton = (Button) findViewById(R.id.bt_create);

		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Comprobamos que los campos necesarios sean correctos
				if (checkFields()) {
					// Recogemos los campos introducidos y rellenamos la tarea
					task.setTitle(((EditText) findViewById(R.id.et_title))
							.getText().toString());
					task.setText(((EditText) findViewById(R.id.et_text))
							.getText().toString());

					// Obtenemos la instancia del DAO y guardamos la tarea
					final TaskDataService taskDAO = new TaskDAO(
							getApplicationContext());
					taskDAO.open();
					// Comprobamos si es una update o un insert
					if (getIntent().getExtras() != null) {
						taskDAO.updateTask(task);
						Toast.makeText(
								getApplicationContext(),
								task.getTitle() + " actualizada correctamente.",
								Toast.LENGTH_SHORT).show();
					} else {
						taskDAO.createTask(task);
						Toast.makeText(getApplicationContext(),
								task.getTitle() + " creada correctamente.",
								Toast.LENGTH_SHORT).show();
					}

					taskDAO.close();
					// Preparamos el retorno para decir que todo fue OK
					fillReturn();
					finish();
				}

			}

		});

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	/**
	 * Refactorización para preparar el GPS y obtener la localización
	 */
	private void setUpGeo() {
		// Obtenemos el GPS y la última localización registrada por el sensor
		// (si hubiese)
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
			nowLoc = mLocationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		else if (mLocationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
			nowLoc = mLocationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		requestUpdatesFromProvider(LocationManager.GPS_PROVIDER,
				R.string.not_support_gps);
		requestUpdatesFromProvider(LocationManager.NETWORK_PROVIDER,
				R.string.not_support_network);

		// Asociamos el listener al botón para recoger el click
		Button btGeo = (Button) findViewById(R.id.bt_geo);
		btGeo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Si la localización no es nula, la recogemos, en caso de
				// serlo, debemos esperar a que se actualice
				if (nowLoc != null) {
					task.setLat(nowLoc.getLatitude());
					task.setLon(nowLoc.getLongitude());
				} else {
					Toast.makeText(getApplicationContext(),
							"Localización no disponible, inténtelo más tarde.",
							Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	private Location requestUpdatesFromProvider(final String provider,
			final int errorResId) {
		Location location = null;
		if (mLocationManager.isProviderEnabled(provider)) {
			mLocationManager.requestLocationUpdates(provider, TEN_SECONDS,
					TEN_METERS, new GeoUpdateHandler());
			location = mLocationManager.getLastKnownLocation(provider);
		} else {
			Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
		}
		return location;
	}

	/**
	 * Actualiza el TextView de la pantalla con la posición del GPS recogida
	 * 
	 * @param loc
	 */
	private void updatePosition(Location loc) {
		if (loc != null) {
			nowLoc = loc;
			TextView tvLoc = (TextView) findViewById(R.id.tv_geo_result);
			tvLoc.setText("[" + nowLoc.getLatitude() + " , "
					+ nowLoc.getLongitude() + "]");
		}

	}

	/**
	 * Rellena el Intent con los parametros necesarios de retorno de Activity en
	 * caso de actualización
	 */
	private void fillReturn() {

		Intent resultadoIntent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("title", task.getTitle());
		resultadoIntent.putExtras(bundle);
		this.setResult(RESULT_OK, resultadoIntent);

	}

	/**
	 * Rellena los campos si es una edición
	 * 
	 * @param bundle
	 */
	private void fillFields(Bundle bundle) {

		final TaskDataService taskDAO = new TaskDAO(getApplicationContext());

		taskDAO.open();

		task = taskDAO.findTaskByTitle(bundle.getString("title"));

		taskDAO.close();

		((EditText) findViewById(R.id.et_title)).setText(task.getTitle());
		((EditText) findViewById(R.id.et_text)).setText(task.getText());

	}

	/**
	 * Prepara para grabar un vídeo con la cámara del dispositivo
	 */
	private void setUpVideo() {
		Button bVideo = (Button) findViewById(R.id.bt_video);
		bVideo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent vIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
				vIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);

				startActivityForResult(vIntent, VIDEO_INTENT);

			}
		});
	}

	/**
	 * Rellena las etiquetas disponibles definidas en el Array en el Spinner
	 */
	private void setUpSpinner() {
		Spinner spinner = (Spinner) findViewById(R.id.spin_tags);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.tags, android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
	}

	/**
	 * Comprueba que los campos necesarios estan rellenos
	 * 
	 * @return
	 */
	private boolean checkFields() {
		boolean flag = true;
		if (((EditText) findViewById(R.id.et_title)).getText() == null || ((EditText) findViewById(R.id.et_title)).getText().toString() == "")
			flag = false;
		else if (((Spinner) findViewById(R.id.spin_tags)).getSelectedItem() == null)
			flag = false;

		return flag;
	}

	/**
	 * Recoge las selecciones del Spinner
	 */
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		task.setTag(parent.getItemAtPosition(pos).toString());

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// Comprueba que la grabación de video ha salido como esperaba
		if (requestCode == VIDEO_INTENT) {

			if (resultCode == RESULT_OK) {

				Toast.makeText(this, "Video guardado en:\n" + data.getData(),
						Toast.LENGTH_LONG).show();
				task.setVideo(data.getData().toString());

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Captura cancelada", Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this, "La captura ha fallado", Toast.LENGTH_LONG)
						.show();
			}

		}

	}

	public class GeoUpdateHandler implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			updatePosition(location);
			nowLoc = location;

		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	}

}
