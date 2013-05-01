package es.uniovi.miw.uo213299.movil.taskmanager.activity;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import es.uniovi.miw.uo213299.movil.taskmanager.R;

/**
 * Activity que representa la pantalla principal de la aplicación, en ella se
 * mostrarán las tareas categorizadas o todas, además de permitir la ejecución
 * de comandos por voz
 * 
 * @author Daniel Machado Fernández
 * 
 */
public class MainActivity extends Activity {

	private static final int NEWTASK_CODE = 23;
	private MediaPlayer mediaPlayer = null;
	private SensorEventListener mEventListenerLight;
	private SensorManager mSensorManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		initListeners();

		// Reproducimos un agradable sonido en el inicio de la aplicación que
		// puede pararse en cualquier momento
		mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.guitar_audio);
		mediaPlayer.start();

		// Asociamos los botones a cada etiqueta por las que están categorizadas
		// las tareas creadas, si presionamos sobre la etiqueta iremos al
		// listado.

		ImageButton ibOcio = (ImageButton) findViewById(R.id.bt_ocio);
		ibOcio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				callIntent("Ocio");

			}
		});

		ImageButton ibPersonal = (ImageButton) findViewById(R.id.bt_personal);
		ibPersonal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				callIntent("Personal");

			}
		});

		ImageButton ibCompra = (ImageButton) findViewById(R.id.bt_compra);
		ibCompra.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				callIntent("Compra");

			}
		});

		ImageButton ibTrabajo = (ImageButton) findViewById(R.id.bt_trabajo);
		ibTrabajo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				callIntent("Trabajo");

			}
		});

		Button btTodas = (Button) findViewById(R.id.bt_todas);
		btTodas.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				callIntent("todas");

			}

		});

		// Agregamos el evento de click para los comandos de voz
		Button btSpeak = (Button) findViewById(R.id.bt_speak);
		btSpeak.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
						"Task Manager Command Service");
				startActivityForResult(intent, 0);

			}
		});

	}

	private void prepare() {
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// En función de la opción de menú seleccionada, crearemos una tarea o
		// pararemos/reproduciremos el audio.
		switch (item.getItemId()) {
		case R.id.mn_new_task:
			newTask();
			break;
		case R.id.mn_play:
			mediaPlayer.start();
			break;
		case R.id.mn_stop:
			mediaPlayer.stop();
			prepare();
			break;

		}
		return true;
	}

	@SuppressLint("DefaultLocale")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		// Recogemos lo recibido del sistema de comandos por voz (ASR) para
		// comprobar cual se debe ejecutar
		if (requestCode != NEWTASK_CODE) {
			if (resultCode == RESULT_OK) {
				ArrayList<String> matches = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				for (String str : matches) {
					String res = str.toLowerCase();
					if (res.contains("nueva tarea")) {
						newTask();
						break;
					} else if (res.contains("ocio")) {
						callIntent("Ocio");
						break;
					} else if (res.contains("todas")) {
						callIntent("todas");
						break;
					} else if (res.contains("trabajo")) {
						callIntent("Trabajo");
						break;
					} else if (res.contains("compra")) {
						callIntent("Compra");
						break;
					} else if (res.contains("personal")) {
						callIntent("Personal");
						break;
					}
				}
			}
		}
	}

	/**
	 * Refactorización que ejecuta el Intent necesario para mostrar el listado
	 * de tareas por categoría
	 * 
	 * @param tag
	 */
	private void callIntent(String tag) {
		Intent mIntent = new Intent(MainActivity.this, TagsActivity.class);
		Bundle b = new Bundle();
		b.putString("tag", tag);
		mIntent.putExtras(b);
		startActivity(mIntent);
	}

	/**
	 * Refactorización que llama el Intent de una nueva tarea
	 */
	private void newTask() {
		Intent myIntent = new Intent(MainActivity.this, NewTaskActivity.class);
		startActivityForResult(myIntent, NEWTASK_CODE);
	}
	
	private void initListeners() {
	
		mEventListenerLight = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
				if(values[0]<5){
					Toast.makeText(getApplicationContext(),"["+ values[0]+"]"+ "Deberías encender la luz, tu pantalla tiene poca luminosidad", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		mSensorManager.registerListener(mEventListenerLight,
				mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
				SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(mEventListenerLight);
		super.onStop();
	}

}
