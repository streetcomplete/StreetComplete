package de.westnordost.streetcomplete.quests.max_height.measure;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.florent37.viewtooltip.ViewTooltip;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.Prefs;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.quests.max_height.Height;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class MeasureCameraFragment extends Fragment
{
	private SensorManager sensorManager;
	private SensorListener sensorEventListener;

	@Inject SharedPreferences prefs;

	private Camera camera;
	private FrameLayout cameraView;

	private Button markBottom, markTop;

	public static final int REQUEST_PERMISSIONS_CAMERA = 123;
	public static final String IS_METRIC = "is_metric";

	private boolean isMetric;

	private MeasureUtils utils = new MeasureUtils();
	private MeasureListener listener;

	public void setListener(MeasureListener listener)
	{
		this.listener = listener;
	}

	@Override public void onCreate(@Nullable Bundle inState)
	{
		super.onCreate(inState);
		Injector.instance.getApplicationComponent().inject(this);
	}

	@Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);

		setHasOptionsMenu(true);

		View contentView = inflater.inflate(R.layout.height_measure_camera, container, false);

		if (getArguments() != null)
		{
			isMetric = getArguments().getBoolean(IS_METRIC, true);
		}

		sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		sensorEventListener = new SensorListener();

		cameraView = contentView.findViewById(R.id.camera_preview);

		markBottom = contentView.findViewById(R.id.button_mark_bottom);
		markTop = contentView.findViewById(R.id.button_mark_top);

		markBottom.setOnClickListener((v) ->
		{
			sensorEventListener.updateOrientationAngles();
			utils.setBottomAngle(Math.abs(sensorEventListener.getPitch()));

			markBottom.setVisibility(View.GONE);
			markTop.setVisibility(View.VISIBLE);

			showTopTooltip();
		});

		markTop.setOnClickListener((v) ->
		{
			sensorEventListener.updateOrientationAngles();

			float topAngle = Math.abs(sensorEventListener.getPitch());
			float quadrant = sensorEventListener.getPitchQuadrantUpDown();
			topAngle = topAngle * (Math.signum(quadrant));
			utils.setTopAngle(topAngle);

			if (utils.getUserHeight() != -1)
			{
				parseResult(utils.getHeight());
			} else {
				setUserHeight(true);
			}

			markBottom.setVisibility(View.VISIBLE);
			markTop.setVisibility(View.GONE);
		});

		if (!prefs.contains(Prefs.USER_HEIGHT))
		{
			setUserHeight(false);
		} else {
			utils.setUserHeight(prefs.getInt(Prefs.USER_HEIGHT, 160));
		}

		int hintTimesShown = prefs.getInt(Prefs.HEIGHT_MEASURE_HINT_TIMES_SHOWN, 0);
		if (hintTimesShown < 2)
		{
			showHelpDialog();
			prefs.edit().putInt(Prefs.HEIGHT_MEASURE_HINT_TIMES_SHOWN, hintTimesShown + 1).apply();
		}

		checkCameraPermission();

		return contentView;
	}

	private void checkCameraPermission()
	{
		if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
		{
			startCamera();
		} else {
			requestPermissions(
				new String[]{Manifest.permission.CAMERA},
				REQUEST_PERMISSIONS_CAMERA
			);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if (requestCode == REQUEST_PERMISSIONS_CAMERA)
		{
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				startCamera();
			} else {
				getActivity().finish();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private void startCamera()
	{
		int cameraId = findCamera();
		if (cameraId < 0)
		{
			Toast.makeText(getContext(), R.string.no_camera_found, Toast.LENGTH_LONG).show();
			getActivity().finish();
		} else {
			camera = getCameraInstance(cameraId);
			if (camera != null)
			{
				cameraView.addView(new CameraPreview(getContext(), camera));
			}
		}
	}

	private Camera getCameraInstance(int id)
	{
		Camera c = null;
		try {
			c = Camera.open(id);
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist)
			new AlertDialogBuilder(getActivity())
				.setMessage(R.string.camera_not_available_error)
				.setNegativeButton(android.R.string.no, (d, w) -> {
					getActivity().finish();
				})
				.show();
		}
		return c;
	}

	private int findCamera()
	{
		int cameraId = -1;
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++)
		{
			Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
			{
				cameraId = i;
				break;
			}
		}
		return cameraId;
	}

	private void parseResult(float result)
	{
		//always use UK as the default locale to avoid values with a comma as a decimal separator
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.UK);
		String meter = new DecimalFormat("#.##", symbols).format(result);

		if (isMetric)
		{
			confirmResult(meter);
		} else {
			double inches = Double.parseDouble(meter) / 0.0254;

			String feet = new DecimalFormat("##").format(inches / 12);
			String leftover = new DecimalFormat("##").format(inches % 12);

			String finalHeight = new Height(Integer.parseInt(feet), Integer.parseInt(leftover)).toString();
			confirmResult(finalHeight, feet, leftover);
		}
	}

	private void setUserHeight(boolean continueCalculation)
	{
		final EditText input = new EditText(getContext());
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});
		if (prefs.contains(Prefs.USER_HEIGHT))
		{
			input.setText(String.valueOf(prefs.getInt(Prefs.USER_HEIGHT, 0)));
		}

		/*TODO: is it better to ask for the height of the user and subtract 20 centimeter afterwards
		or should we ask the user for the height he's holding the phone at? (like done right now)
		The first option would be easier because everybody knows his own height,
		but the subtraction might be inaccurate because some hold their phones higher and some lower...
		The problem of the second option is that the height will probably be only some kind of guess too,
		because I can't imagine that every user checks the height he's holding the phone at before answering the question
		*/

		new AlertDialogBuilder(getActivity())
			.setMessage(R.string.input_user_height)
			.setView(input)
			.setCancelable(false)
			.setOnCancelListener((d) -> {
				getActivity().finish();
			})
			.setPositiveButton(android.R.string.ok, (d, w) ->
			{
				if (!input.getText().toString().isEmpty())
				{
					int height = Integer.parseInt(input.getText().toString());
					utils.setUserHeight(height);
					prefs.edit().putInt(Prefs.USER_HEIGHT, height).apply();

					if (continueCalculation)
					{
						parseResult(utils.getHeight());
					}
				} else {
					Toast.makeText(getContext(), R.string.quest_generic_error_field_empty, Toast.LENGTH_LONG).show();
				}
			})
			.show();
	}

	private void confirmResult(String result)
	{
		confirmResult(result, null, null);
	}

	private void confirmResult(String result, String feet, String inches)
	{
		new AlertDialogBuilder(getActivity())
			.setMessage(String.format(getActivity().getResources().getString(R.string.confirmation_measured_height), result))
			.setNegativeButton(android.R.string.no, (d, w) -> {
				d.dismiss();
			})
			.setPositiveButton(android.R.string.ok, (d, w) ->
			{
				if (isMetric)
				{
					listener.onMeasured(result);
				} else {
					listener.onMeasured(feet, inches);
				}
			})
			.show();
	}

	private void showHelpDialog()
	{
		new AlertDialogBuilder(getActivity())
			.setView(R.layout.height_measure_help)
			.setPositiveButton(android.R.string.ok, (d, w) -> showBottomTooltip())
			.show();
	}

	private void showBottomTooltip()
	{
		int tooltipBottomTimesShown = prefs.getInt(Prefs.MEASURE_HINT_MARK_BOTTOM_TIMES_SHOWN, 0);
		if (tooltipBottomTimesShown < 2)
		{
			ViewTooltip.on(markBottom)
				.position(ViewTooltip.Position.TOP)
				.text(getResources().getString(R.string.measure_mark_bottom_hint))
				.color(getResources().getColor(R.color.colorTooltip))
				.duration(6000)
				.show();
			prefs.edit().putInt(Prefs.MEASURE_HINT_MARK_BOTTOM_TIMES_SHOWN, tooltipBottomTimesShown + 1).apply();
		}
	}

	private void showTopTooltip()
	{
		int tooltipTopTimesShown = prefs.getInt(Prefs.MEASURE_HINT_MARK_TOP_TIMES_SHOWN, 0);
		if (tooltipTopTimesShown < 2)
		{
			ViewTooltip.on(markTop)
				.position(ViewTooltip.Position.TOP)
				.text(getResources().getString(R.string.measure_mark_top_hint))
				.color(getResources().getColor(R.color.colorTooltip))
				.duration(6000)
				.show();
			prefs.edit().putInt(Prefs.MEASURE_HINT_MARK_TOP_TIMES_SHOWN, tooltipTopTimesShown + 1).apply();
		}
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		switch (id)
		{
			case R.id.action_help:
				showHelpDialog();
				return true;
			case R.id.action_settings:
				setUserHeight(false);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			listener = (MeasureListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
		{
			startCamera();
		}

		sensorManager.registerListener(
			sensorEventListener,
			sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
			SensorManager.SENSOR_DELAY_NORMAL
		);
		sensorManager.registerListener(
			sensorEventListener,
			sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
			SensorManager.SENSOR_DELAY_NORMAL
		);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		if (camera != null) {
			camera.release();
			camera = null;
		}

		sensorManager.unregisterListener(sensorEventListener);
	}

	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

		private SurfaceHolder holder;
		private Camera camera;

		private List<Camera.Size> supportedPreviewSizes;
		private Camera.Size previewSize;

		public CameraPreview(Context context, Camera camera) {
			super(context);

			this.camera = camera;
			holder = getHolder();
			holder.addCallback(this);

			supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

			if (holder.getSurface() == null){
				return;
			}

			try {
				camera.stopPreview();
			} catch (Exception e){
				// ignore: tried to stop a non-existent preview
			}

			try {
				Camera.Parameters parameters = camera.getParameters();
				parameters.setPreviewSize(previewSize.width, previewSize.height);

				camera.setParameters(parameters);
				camera.setDisplayOrientation(90);
				camera.setPreviewDisplay(holder);
				camera.startPreview();
			} catch (Exception e){
				Log.d("MeasureCameraFragment", "Error starting camera preview: " + e.getMessage());
			}
		}

		public void surfaceCreated(SurfaceHolder holder)
		{
			try {
				camera.reconnect();
				camera.setPreviewDisplay(holder);
				camera.startPreview();
			} catch (IOException e) {
				Log.d("MeasureCameraFragment", "Error setting camera preview: " + e.getMessage());
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
			final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
			setMeasuredDimension(width, height);

			if (supportedPreviewSizes != null)
			{
				previewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);
			}
		}

		private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h)
		{
			final double ASPECT_TOLERANCE = 0.1;
			double targetRatio=(double)h / w;

			if (sizes == null) return null;

			Camera.Size optimalSize = null;
			double minDiff = Double.MAX_VALUE;

			for (Camera.Size size : sizes)
			{
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
				if (Math.abs(size.height - h) < minDiff)
				{
					optimalSize = size;
					minDiff = Math.abs(size.height - h);
				}
			}

			if (optimalSize == null)
			{
				minDiff = Double.MAX_VALUE;
				for (Camera.Size size : sizes)
				{
					if (Math.abs(size.height - h) < minDiff)
					{
						optimalSize = size;
						minDiff = Math.abs(size.height - h);
					}
				}
			}
			return optimalSize;
		}
	}
}
