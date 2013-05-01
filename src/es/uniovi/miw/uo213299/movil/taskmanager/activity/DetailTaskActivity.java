package es.uniovi.miw.uo213299.movil.taskmanager.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import es.uniovi.miw.uo213299.movil.taskmanager.R;
import es.uniovi.miw.uo213299.movil.taskmanager.model.Constants;
import es.uniovi.miw.uo213299.movil.taskmanager.model.Task;
import es.uniovi.miw.uo213299.movil.taskmanager.persistence.TaskDataService;
import es.uniovi.miw.uo213299.movil.taskmanager.persistence.dao.TaskDAO;

/**
 * Activity destinada a la representación del Detalle de la {@link Task}
 * 
 * @author Daniel Machado Fernández
 * 
 */
public class DetailTaskActivity extends Activity implements OnInitListener {

	private Task task = null;
	private GoogleMap mMap;
	private ShareActionProvider shareProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_task);

		Bundle bundle = getIntent().getExtras();

		final TaskDataService taskDAO = new TaskDAO(getApplicationContext());
		taskDAO.open();
		// obtenemos las tarea por título (id)
		task = taskDAO.findTaskByTitle(bundle.getString("title"));

		showTask(task);

		taskDAO.close();
		// Damos la opción a reproducir mediante el TTS el título de la tarea
		final TextToSpeech tts = new TextToSpeech(this, this);
		Button btListen = (Button) findViewById(R.id.bt_listen);
		btListen.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				tts.speak(task.getTitle(), TextToSpeech.QUEUE_ADD, null);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.detail_task, menu);

		MenuItem item = menu.findItem(R.id.menu_item_share);

		// Fetch and store ShareActionProvider
		shareProvider = (ShareActionProvider) item.getActionProvider();
		setShareIntent(share());

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Comprobamos que la acción seleccionada es Editar o Borrar
		switch (item.getItemId()) {
		case R.id.mn_delete_task:
			TaskDataService taskDAO = new TaskDAO(getApplicationContext());
			taskDAO.open();
			taskDAO.delete(task);
			taskDAO.close();
			finish();
			break;
		case R.id.mn_edit_task:
			Intent eIntent = new Intent(DetailTaskActivity.this, NewTaskActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("title", task.getTitle());
			eIntent.putExtras(bundle);

			startActivityForResult(eIntent, Constants.REQUEST_EDITTASK);
			break;
		}
		return true;
	}

	@Override
	public void onInit(int status) {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Si venimos de una edición, refrescamos.
		if (requestCode == Constants.REQUEST_EDITTASK) {

			if (resultCode == RESULT_OK) {

				String title = data.getExtras().getString("title");

				TaskDataService taskDAO = new TaskDAO(getApplicationContext());
				taskDAO.open();
				task = taskDAO.findTaskByTitle(title);
				taskDAO.close();
				showTask(task);

			}

		}

	}

	/**
	 * Refactorización para mostrar la tarea y sus adjuntos
	 * 
	 * @param task
	 *            {@link Task}
	 */
	private void showTask(Task task) {
		// Mostramos el título y el texto
		((TextView) findViewById(R.id.tv_title)).setText(task.getTitle());
		((TextView) findViewById(R.id.tv_text)).setText(task.getText());
		// Mostramos la imagen asociada al tipo de tarea
		ImageView mIV = (ImageView) findViewById(R.id.iv_image);
		Bitmap bitmap = setTagImage(task);

		mIV.setImageBitmap(bitmap);
		// Si tiene un video asociado, lo mostramos en caso de no tenerlo,
		// ocultamos la label y el VideoView
		if (task.getVideo() != null) {
			videoStuff(task);
		} else {
			findViewById(R.id.tv_video).setVisibility(View.INVISIBLE);
			findViewById(R.id.vv_video).setVisibility(View.INVISIBLE);
		}
		// Si tiene un GEO asociado, lo mostramos, si no lo tiene, ocultamos la
		// label y el fragment
		if (task.getLat() != null && task.getLon() != null
				&& task.getLat() != 0.0 && task.getLon() != 0.0) {
			geoStuff(task);

		} else {
			findViewById(R.id.tv_geo).setVisibility(View.INVISIBLE);
			findViewById(R.id.map_detail).setVisibility(View.INVISIBLE);
		}

	}

	/**
	 * Settea en un Bitmap la imagen asociada a la etiqueta de la tarea
	 * 
	 * @param task
	 * 
	 * @return
	 */
	private Bitmap setTagImage(Task task) {
		Bitmap bitmap = null;
		if (task.getTag().contains(Constants.TAGS[0])) {
			bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.personal);
		} else if (task.getTag().contains(Constants.TAGS[1])) {
			bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.trabajo);
		} else if (task.getTag().contains(Constants.TAGS[2])) {
			bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.ocio);
		} else if (task.getTag().contains(Constants.TAGS[3])) {
			bitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.compra);
		}
		return bitmap;
	}

	/**
	 * Muestra la geolocalización de la forma en la que lo hicimos en clase
	 * 
	 * @param task
	 */
	private void geoStuff(Task task) {

		final MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map_detail);

		mMap = mapFragment.getMap();

		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		mMap.addMarker(new MarkerOptions().position(new LatLng(task.getLat(),
				task.getLon())));
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
				new LatLng(task.getLat(), task.getLon()), 15));

	}

	/**
	 * Muestra un video al que asocia un MediaController grabado previamente en
	 * la creación de la {@link Task}
	 * 
	 * @param task
	 */
	private void videoStuff(Task task) {

		VideoView myVideoView = (VideoView) findViewById(R.id.vv_video);
		myVideoView.setVideoURI(Uri.parse(task.getVideo()));
		System.out.println(Uri.parse(task.getVideo()));
		MediaController controller = new MediaController(this);
		myVideoView.setMediaController(controller);
		controller.setMediaPlayer(myVideoView);
		myVideoView.requestFocus();
	}

	private void setShareIntent(Intent shareIntent) {
		if (shareProvider != null) {
			shareProvider.setShareIntent(shareIntent);
		}
	}
	
	private Intent share() {

		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

		shareIntent.setType("text/plain");

		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				task.getTitle());

		String shareMessage = task.getText();

		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);

		return shareIntent;
	}

}
