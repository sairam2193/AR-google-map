package ng.dat.ar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import java.util.ArrayList;
import java.util.Iterator;

import ng.dat.ar.model.ARPoint;

public class ARActivity extends AppCompatActivity implements GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,LocationListener,OnMarkerDragListener,GoogleMap.OnMarkerClickListener,GoogleMap.OnMapClickListener/*,,OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener,StreetViewPanorama.OnStreetViewPanoramaChangeListener */ {

    //final static String TAG = "ARActivity";
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    private TextView tvCurrentLocation;

    private SensorManager sensorManager;
    private SensorManager mapSensor;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 500;//1000 * 60 * 1; // 1 minute

    private LocationManager locationManager;
    public Location location;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;
    double oldAzimuthValues =0.0d;
    int azimuthInInt;
    double checkAzimuth=0.0d;

    private static final String MARKER_POSITION_KEY = "MarkerPosition";
    //private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);
    //private static final LatLng BRISBANE = new LatLng(-27.47093, 153.0235);
    private Marker mMarker;
    double azimuthInDegrees=0.0d;

    private GoogleMap mMap;

    /**
     * Keeps track of the selected marker.
     */
    private Marker mSelectedMarker;

    private boolean changeOnSensor = false;
    private static final String TAG = ARActivity.class.getName();

    /**
     * The amount by which to scroll the camera. Note that this amount is in raw pixels, not dp
     * (density-independent pixels).
     */
    private static final int SCROLL_BY_PX = 100;

    public static final CameraPosition BONDI =
            new CameraPosition.Builder().target(new LatLng(-33.891614, 151.276417))
                    .zoom(15.5f)
                    .bearing(300)
                    .tilt(50)
                    .build();

    public static final CameraPosition SYDNEY =
            new CameraPosition.Builder().target(new LatLng(-33.87365, 151.20689))
                    .zoom(15.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();


    /*private CompoundButton mAnimateToggle;
    private CompoundButton mCustomDurationToggle;
    private SeekBar mCustomDurationBar;
    private PolylineOptions currPolylineOptions;*/
    private boolean isCanceled = false;
    private boolean rotateTrue = false;
    public ARActivity() {
    }
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor rotationVector;
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    private float currentDegree = 0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        changeOnSensor=false;
            /*mAnimateToggle = (CompoundButton) findViewById(R.id.animate);
            mCustomDurationToggle = (CompoundButton) findViewById(R.id.duration_toggle);
            mCustomDurationBar = (SeekBar) findViewById(R.id.duration_bar);*/
        //updateEnabledState();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
            /*final LatLng markerPosition;
            if (savedInstanceState == null) {
                markerPosition = SYDNEY;
            } else {
                markerPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY);
            }
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            mapSensor = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        tvCurrentLocation = (TextView) findViewById(R.id.tv_current_location);*/
        arOverlayView = new AROverlayView(this);
        // onCreate method stub ...
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationVector= sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        //sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                rotationSensorEvent(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
               //onAccuracyChanged(sensor, accuracy);
            }
        }, rotationVector, SensorManager.SENSOR_DELAY_UI);
        // more onCreate method stub ....
        /*SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            new OnMapAndViewReadyListener(mapFragment, this);


       /* mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                map.setOnMarkerDragListener(ARActivity.this);
                // Creates a draggable marker. Long press to drag.
                mMarker = map.addMarker(new MarkerOptions()
                        .position(markerPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman))
                        .draggable(true));
            }
        });*/


    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onResume() {
        super.onResume();
        //updateEnabledState();
        requestLocationPermission();
    }

    private static final LatLng MOUNTAIN_VIEW = new LatLng(37.4, -122.1);

    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                setMarkers();
            }
        });
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                                         @Override
                                         public void onCameraMove() {
                                             setMarkers();
                                         }
                                     });
                mMap.setOnCameraMoveCanceledListener(this);
        mMap.setBuildingsEnabled(true);
        // We will provide our own zoom controls.
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        setMarkers();
        // Set listener for marker click event.  See the bottom of this class for its behavior.
        mMap.setOnMarkerClickListener(this);

        // Set listener for map click event.  See the bottom of this class for its behavior.
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMarkers();
            }
        });
        if(location!=null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to Mountain View
                    .zoom(18)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(90)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    changeOnSensor=true;
                }

                @Override
                public void onCancel() {

                }
            });
        }



        // Override the default content description on the view, for accessibility mode.
        // Ideally this string would be localized.
        map.setContentDescription("Demo showing how to close the info window when the currently"
                + " selected marker is re-tapped.");
        /*if (location != null) {
            ArrayList<ARPoint> ar_Points = (ArrayList<ARPoint>) arOverlayView.getFilteredLocations(location);
            Iterator itr = ar_Points.iterator();
            ARPoint arpoint = null;
            LatLngBounds bounds = new LatLngBounds.Builder().include(new LatLng(location.getLatitude(), location.getLongitude())).build();
            while (itr.hasNext()) {
                arpoint = (ARPoint) itr.next();
                bounds.including(new LatLng(arpoint.getLocation().getLatitude(), arpoint.getLocation().getLongitude()));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        }*/
        /*if (location.getLatitude() != -33.87365) {
            location.setLatitude(-33.87365);
            location.setLongitude(151.20689);
            onMapReady(mMap);
        }*/
        // Show Sydney
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.87365, 151.20689), 10));


    }
    private void setMarkers(){

        if (location != null && mMap != null) {
            addMarkersToMap();
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman))
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .draggable(true));
                    //.rotation((float) azimuthInDegrees)
                    //.flat(true))
                    //.setZIndex(1.0f);
            //BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.arrow_header))
        }
    }
    /**
     * When the map is not ready the CameraUpdateFactory cannot be used. This should be called on
     * all entry points that call methods on the Google Maps API.
     */
    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Called when the Go To Bondi button is clicked.
     */
    /*public void onGoToBondi(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.newCameraPosition(BONDI));
    }*/

    /**
     * Called when the Animate To Sydney button is clicked.
     */
    /*public void onGoToSydney(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.newCameraPosition(SYDNEY), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Toast.makeText(getBaseContext(), "Animation to Sydney complete", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getBaseContext(), "Animation to Sydney canceled", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }*/

    /**
     * Called when the stop button is clicked.
     */
    /*public void onStopAnimation(View view) {
        if (!checkReady()) {
            return;
        }

        mMap.stopAnimation();
    }*/

    /**
     * Called when the zoom in button (the one with the +) is clicked.
     */
    public void onZoomIn(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.zoomIn());
    }

    /**
     * Called when the zoom out button (the one with the -) is clicked.
     */
    public void onZoomOut(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.zoomOut());
    }

    /**
     * Called when the tilt more button (the one with the /) is clicked.
     */
    /*public void onTiltMore(View view) {
        if (!checkReady()) {
            return;
        }

        CameraPosition currentCameraPosition = mMap.getCameraPosition();
        float currentTilt = currentCameraPosition.tilt;
        float newTilt = currentTilt + 10;

        newTilt = (newTilt > 90) ? 90 : newTilt;

        CameraPosition cameraPosition = new CameraPosition.Builder(currentCameraPosition)
                .tilt(newTilt).build();

        changeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }*/

    /*
     * Called when the tilt less button (the one with the \) is clicked.
     */
    /*public void onTiltLess(View view) {
        if (!checkReady()) {
            return;
        }

        CameraPosition currentCameraPosition = mMap.getCameraPosition();

        float currentTilt = currentCameraPosition.tilt;

        float newTilt = currentTilt - 10;
        newTilt = (newTilt > 0) ? newTilt : 0;

        CameraPosition cameraPosition = new CameraPosition.Builder(currentCameraPosition)
                .tilt(newTilt).build();

        changeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }*/

    /**
     * Called when the left arrow button is clicked. This causes the camera to move to the left
     *//*
    /*public void onScrollLeft(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.scrollBy(-SCROLL_BY_PX, 0));
    }

    *//**
     * Called when the right arrow button is clicked. This causes the camera to move to the right.
     *//*
    public void onScrollRight(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.scrollBy(SCROLL_BY_PX, 0));
    }

    *//**
     * Called when the up arrow button is clicked. The causes the camera to move up.
     *//*
    public void onScrollUp(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.scrollBy(0, -SCROLL_BY_PX));
    }

    *//**
     * Called when the down arrow button is clicked. This causes the camera to move down.
     *//*
    public void onScrollDown(View view) {
        if (!checkReady()) {
            return;
        }

        changeCamera(CameraUpdateFactory.scrollBy(0, SCROLL_BY_PX));
    }

    *//**
     * Called when the animate button is toggled
     *//*
    public void onToggleAnimate(View view) {
        updateEnabledState();
    }

    *//**
     * Called when the custom duration checkbox is toggled
     *//*
    public void onToggleCustomDuration(View view) {
        updateEnabledState();
    }*/

    /**
     * Update the enabled state of the custom duration controls.
     */
    /*private void updateEnabledState() {
        mCustomDurationToggle.setEnabled(mAnimateToggle.isChecked());
        mCustomDurationBar
                .setEnabled(mAnimateToggle.isChecked() && mCustomDurationToggle.isChecked());
    }*/

    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }

    /**
     * Change the camera position by moving or animating the camera depending on the state of the
     * animate toggle button.
     */
    private void changeCamera(CameraUpdate update, GoogleMap.CancelableCallback callback) {
        /*if (mAnimateToggle.isChecked()) {
            if (mCustomDurationToggle.isChecked()) {
                int duration = mCustomDurationBar.getProgress();
                // The duration must be strictly positive so we make it at least 1.
                mMap.animateCamera(update, Math.max(duration, 1), callback);
            } else {
                mMap.animateCamera(update, callback);
            }
        } else {*/
            mMap.moveCamera(update);
        //}
    }
    @Override
    public void onCameraMoveStarted(int reason) {
        if (!isCanceled) {
            mMap.clear();
        }

        /*String reasonText = "UNKNOWN_REASON";
        currPolylineOptions = new PolylineOptions().width(5);
        switch (reason) {
            case GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE:
                currPolylineOptions.color(Color.BLUE);
                reasonText = "GESTURE";
                break;
            case GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION:
                currPolylineOptions.color(Color.RED);
                reasonText = "API_ANIMATION";
                break;
            case GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION:
                currPolylineOptions.color(Color.GREEN);
                reasonText = "DEVELOPER_ANIMATION";
                break;
        }
        Log.i(TAG, "onCameraMoveStarted(" + reasonText + ")");*/
        addCameraTargetToPath();
    }

    @Override
    public void onCameraMove() {
        // When the camera is moving, add its target to the current path we'll draw on the map.
     /*   if (currPolylineOptions != null) {
            addCameraTargetToPath();
        }*/
        Log.i(TAG, "onCameraMove");
    }

    @Override
    public void onCameraMoveCanceled() {
        // When the camera stops moving, add its target to the current path, and draw it on the map.
      /*  if (currPolylineOptions != null) {
            addCameraTargetToPath();
            mMap.addPolyline(currPolylineOptions);
        }
        isCanceled = true;  // Set to clear the map when dragging starts again.
        currPolylineOptions = null;*/
        Log.i(TAG, "onCameraMoveCancelled");
    }

    @Override
    public void onCameraIdle() {
        /*if (currPolylineOptions != null) {
            addCameraTargetToPath();
            mMap.addPolyline(currPolylineOptions);
        }
        currPolylineOptions = null;*/
        isCanceled = false;  // Set to *not* clear the map when dragging starts again.
        Log.i(TAG, "onCameraIdle");
    }

    private void addCameraTargetToPath() {
        LatLng target = mMap.getCameraPosition().target;
        //currPolylineOptions.add(target);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*@Override
    public void onResume() {
        super.onResume();
        requestLocationPermission();
        requestCameraPermission();
        registerSensors();
        registerMapSensors();
        initAROverlayView();
        /*if(mMap!=null) {
            location.setLatitude(-33.87365);
            location.setLongitude(151.20689);
            onMapReady(mMap);
        }
    }

    /*@Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }*/

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }
    }

    /*public void initAROverlayView() {
        //arOverlayView=
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(this, "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }

    private void releaseCamera() {
        if(camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }
    private SensorManager mSensorManager;

    private SensorEventListener mSensorListener;
    private void registerMapSensors() {
        mapSensor.registerListener(this,
                mapSensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }
        private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
        /*mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mSensorListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                    float[] rotationMatrixFromVector = new float[16];
                    float[] projectionMatrix = new float[16];
                    float[] rotatedProjectionMatrix = new float[16];

                    SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, event.values);

                    if (arCamera != null) {
                        projectionMatrix = arCamera.getProjectionMatrix();
                    }

                    Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
                    arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
                }else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    location.setLatitude(-33.87365);
                    location.setLongitude(151.20689);
                    onMapReady(mMap);                }
            }
        };

    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }*/

    private void initLocationService() {

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            this.locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled) {
                // cannot get location
                this.locationServiceAvailable = false;
            }

            this.locationServiceAvailable = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        UpdateCurrentLocation();
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStatusChanged(String provider, int status,
                                                Bundle extras) {
                        // TODO Auto-generated method stub
                    }
                });
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    updateLatestLocation();
                }
            }

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        UpdateCurrentLocation();
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStatusChanged(String provider, int status,
                                                Bundle extras) {
                        // TODO Auto-generated method stub
                    }
                });

                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLatestLocation();
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());

        }
    }

    private void UpdateCurrentLocation() {
        onLocationChanged(location);
    }

    private void updateLatestLocation() {
        if (arOverlayView != null && location != null) {
            arOverlayView.updateCurrentLocation(location);
            /*tvCurrentLocation.setText(String.format("lat: %s \nlon: %s \naltitude: %s \n",
                    location.getLatitude(), location.getLongitude(), location.getAltitude()));*/
        }
        //setMarkers();
    }

    //@Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        updateLatestLocation();
        drawNewMap();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /*@Override
    public void onLocationChanged(Location location) {
        updateLatestLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MARKER_POSITION_KEY, mMarker.getPosition());
    }

    @Override
    public void onStreetViewPanoramaChange(StreetViewPanoramaLocation location) {
        if (location != null) {
            mMarker.setPosition(location.position);
        }
    }

    /*@Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mStreetViewPanorama.setPosition(marker.getPosition(), 150);
    }*/

    /*@Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
        /*if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //if(location.getLatitude()!=-33.87365) {
            if(mMap!=null && location!=null) {
                location.setLatitude(-33.87365);
                location.setLongitude(151.20689);
                //onMapReady(mMap);
                //mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman)).draggable(true));         }
            }
        }
    }
    ///Changes for multiple marking
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Hide the zoom controls.
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Add lots of markers to the map.
        if(location!=null && mMap!=null) {
            addMarkersToMap();
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(),location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman))
                    .draggable(true));
        }
        // Set listener for marker click event.  See the bottom of this class for its behavior.
        mMap.setOnMarkerClickListener(this);

        // Set listener for map click event.  See the bottom of this class for its behavior.
        mMap.setOnMapClickListener(this);

        // Override the default content description on the view, for accessibility mode.
        // Ideally this string would be localized.
        map.setContentDescription("Demo showing how to close the info window when the currently"
                + " selected marker is re-tapped.");
        if(location!=null) {
            ArrayList<ARPoint> ar_Points = (ArrayList<ARPoint>) arOverlayView.getFilteredLocations(location);
            Iterator itr = ar_Points.iterator();
            ARPoint arpoint = null;
            LatLngBounds bounds = new LatLngBounds.Builder().include(new LatLng(location.getLatitude(), location.getLongitude())).build();
            while (itr.hasNext()) {
                arpoint = (ARPoint) itr.next();
                bounds.including(new LatLng(arpoint.getLocation().getLatitude(), arpoint.getLocation().getLongitude()));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        }
        if(location.getLatitude()!=-33.87365) {
            location.setLatitude(-33.87365);
            location.setLongitude(151.20689);
            onMapReady(mMap);
        }
    }*/

    private void addMarkersToMap() {


        ArrayList<ARPoint> ar_Points = (ArrayList<ARPoint>) arOverlayView.getFilteredLocations(location);
        Iterator itr = ar_Points.iterator();
        ARPoint arpoint = null;
        //LatLngBounds bounds = new LatLngBounds.Builder().include(new LatLng(location.getLatitude(), location.getLongitude())).build();
        while (itr.hasNext()) {
            arpoint = (ARPoint) itr.next();
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(arpoint.getLocation().getLatitude(), arpoint.getLocation().getLongitude()))
                    .title(arpoint.getName())
                    .snippet(arpoint.getDistance()));
        }
        //mMap.setMaxZoomPreference(14.0f);
        CircleOptions circleOptions = new CircleOptions();
        if(location!=null) {
            circleOptions.center(new LatLng(location.getLatitude(), location.getLongitude())).radius(5000);
        }
        circleOptions.strokeWidth(1.0f);
        mMap.addCircle(circleOptions);
        //mMap.setMaxZoomPreference(13.0f);

    }


    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMapClick(final LatLng point) {
        // Any showing info window closes when the map is clicked.
        // Clear the currently selected marker.
        //mSelectedMarker = null;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // The user has re-tapped on the marker which was already showing an info window.
        /*if (marker.equals(mSelectedMarker)) {
            // The showing info window has already been closed - that's the first thing to happen
            // when any marker is clicked.
            // Return true to indicate we have consumed the event and that we do not want the
            // the default behavior to occur (which is for the camera to move such that the
            // marker is centered and for the marker's info window to open, if it has one).
            mSelectedMarker = null;
            return true;
        }

        mSelectedMarker = marker;*/

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur.
        return false;
    }

    //@Override
    /*public void onSensorChanged(SensorEvent event) {
        if (event.sensor == this.accelerometer) {
            System.arraycopy(event.values, 0, this.lastAccelerometer, 0, event.values.length);
            this.lastAccelerometerSet = true;
        } else if (event.sensor == this.magnetometer) {
            System.arraycopy(event.values, 0, this.lastMagnetometer, 0, event.values.length);
            this.lastMagnetometerSet = true;
        }

        if (this.lastAccelerometerSet && this.lastAccelerometerSet) {
            SensorManager.getRotationMatrix(this.rotationMatrix,null, this.lastAccelerometer, this.lastMagnetometer);
            SensorManager.getOrientation(this.rotationMatrix, this.orientation);


            float azimuthInRadiands = this.orientation[0];

            // this is now the heading of the phone. If you want
            // to rotate a view to north don´t forget that you have
            // to rotate by the negative value.
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadiands);
            if(azimuthInDegrees<0){
                azimuthInDegrees=azimuthInDegrees+360;
            }
            String result = String.valueOf(azimuthInRadiands)+"   "+String.valueOf(azimuthInDegrees);
            Log.d("***********************",result);
            //mMap.animateCamera(new CameraUpdate());

            if ( (360 >= azimuthInDegrees && azimuthInDegrees >= 337.5) || (0 <= azimuthInDegrees && azimuthInDegrees <= 22.5) ) azimuthInDegrees = 345.0f;
            else if (azimuthInDegrees > 22.5 && azimuthInDegrees < 67.5) azimuthInDegrees = 42.0f;
            else if (azimuthInDegrees >= 67.5 && azimuthInDegrees <= 112.5) azimuthInDegrees = 80.0f;
            else if (azimuthInDegrees > 112.5 && azimuthInDegrees < 157.5) azimuthInDegrees = 135.0f;
            else if (azimuthInDegrees >= 157.5 && azimuthInDegrees <= 202.5) azimuthInDegrees = 175.0f;
            else if (azimuthInDegrees > 202.5 && azimuthInDegrees < 247.5) azimuthInDegrees = 220.0f;
            else if (azimuthInDegrees >= 247.5 && azimuthInDegrees <= 292.5) azimuthInDegrees = 270.0f;
            else if (azimuthInDegrees > 292.5 && azimuthInDegrees < 337.5) azimuthInDegrees = 315.0f;
            
            if(location!=null && mMap!=null  && changeOnSensor) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to Mountain View
                        .zoom(18)                   // Sets the zoom
                        .bearing(azimuthInDegrees)                // Sets the orientation of the camera to east
                        .tilt(90)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                //mMap.stopAnimation();
            }
        }
    }*/
    //@Override
    public void rotationSensorEvent(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            double[] g = convertFloatsToDoubles(event.values.clone());

            //Normalise
            double norm = Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2] + g[3] * g[3]);
            g[0] /= norm;
            g[1] /= norm;
            g[2] /= norm;
            g[3] /= norm;

            //Set values to commonly known quaternion letter representatives
            double x = g[0];
            double y = g[1];
            double z = g[2];
            double w = g[3];

            //Calculate Pitch in degrees (-180 to 180)
            double sinP = 2.0 * (w * x + y * z);
            double cosP = 1.0 - 2.0 * (x * x + y * y);
            double pitch = Math.atan2(sinP, cosP) * (180 / Math.PI);

            //Calculate Tilt in degrees (-90 to 90)
            double tilt;
            double sinT = 2.0 * (w * y - z * x);
            if (Math.abs(sinT) >= 1)
                tilt = Math.copySign(Math.PI / 2, sinT) * (180 / Math.PI);
            else
                tilt = Math.asin(sinT) * (180 / Math.PI);

            //Calculate Azimuth in degrees (0 to 360; 0 = North, 90 = East, 180 = South, 270 = West)
            double sinA = 2.0 * (w * z + x * y);
            double cosA = 1.0 - 2.0 * (y * y + z * z);
            azimuthInDegrees = Math.atan2(sinA, cosA) * (180 / Math.PI);
            if(azimuthInDegrees<0){
                azimuthInDegrees=azimuthInDegrees+360;
            }
            checkAzimuth=azimuthInDegrees;
            //Log.d("logloglogloglogloglog", String.valueOf(mMap.getCameraPosition().bearing)+"      "+String.valueOf(azimuthInDegrees));



            rotateTrue=false;
            /*if(mMap!=null && mMap.getCameraPosition().bearing-azimuthInDegrees>10 || azimuthInDegrees-mMap.getCameraPosition().bearing>10){
                rotateTrue=true;
                Log.d("logloglogloglogloglog", String.valueOf(rotateTrue) );
            }*/

            /*if ( (360 >= azimuthInDegrees && azimuthInDegrees >= 337.5) || (0 <= azimuthInDegrees && azimuthInDegrees <= 22.5) ) azimuthInDegrees = 345.0f;
            else if (azimuthInDegrees > 22.5 && azimuthInDegrees < 67.5) azimuthInDegrees = 42.0f;
            else if (azimuthInDegrees >= 67.5 && azimuthInDegrees <= 112.5) azimuthInDegrees = 80.0f;
            else if (azimuthInDegrees > 112.5 && azimuthInDegrees < 157.5) azimuthInDegrees = 135.0f;
            else if (azimuthInDegrees >= 157.5 && azimuthInDegrees <= 202.5) azimuthInDegrees = 175.0f;
            else if (azimuthInDegrees > 202.5 && azimuthInDegrees < 247.5) azimuthInDegrees = 220.0f;
            else if (azimuthInDegrees >= 247.5 && azimuthInDegrees <= 292.5) azimuthInDegrees = 270.0f;
            else if (azimuthInDegrees > 292.5 && azimuthInDegrees < 337.5) azimuthInDegrees = 315.0f;*/
            if(changeOnSensor){
                azimuthInDegrees=azimuthInDegrees;
            }
            azimuthInInt=(int)azimuthInDegrees;
            if ( (azimuthInInt>0 && azimuthInInt < 45)) azimuthInDegrees =332.5d ;
            else if (azimuthInInt > 45 && azimuthInInt < 90) azimuthInDegrees = 292.5d;
            else if (azimuthInInt > 90 && azimuthInInt < 135) azimuthInDegrees = 247.5;
            else if (azimuthInInt > 135 && azimuthInInt < 180) azimuthInDegrees = 202.5d;
            else if (azimuthInInt > 180 && azimuthInInt < 225) azimuthInDegrees = 157.5d;
            else if (azimuthInInt > 225 && azimuthInInt < 270) azimuthInDegrees = 112.5d;
            else if (azimuthInInt >270 && azimuthInInt < 315) azimuthInDegrees = 67.5d;
            else if (azimuthInInt >315 && azimuthInInt < 360) azimuthInDegrees = 22.5d;

            /*if ( (azimuthInInt>0 && azimuthInInt < 22.5)) azimuthInDegrees = 348.75;
            else if (azimuthInInt > 22.5 && azimuthInInt < 45) azimuthInDegrees = 326.25;
            else if (azimuthInInt > 45 && azimuthInInt < 67.5) azimuthInDegrees = 303.75;
            else if (azimuthInInt > 67.5 && azimuthInInt < 90) azimuthInDegrees = 281.25;
            else if (azimuthInInt > 90 && azimuthInInt < 112.5) azimuthInDegrees = 258.75;
            else if (azimuthInInt > 112.5 && azimuthInInt < 135) azimuthInDegrees = 236.25;
            else if (azimuthInInt > 135 && azimuthInInt < 157.5) azimuthInDegrees = 191.25;
            else if (azimuthInInt > 157.5 && azimuthInInt < 180) azimuthInDegrees = 168.75;
            else if (azimuthInInt > 180 && azimuthInInt < 202.5) azimuthInDegrees = 146.25;
            else if (azimuthInInt > 202.5 && azimuthInInt < 225) azimuthInDegrees = 213.75;
            else if (azimuthInInt > 225 && azimuthInInt < 247.5) azimuthInDegrees = 123.75;
            else if (azimuthInInt > 247.5 && azimuthInInt < 270) azimuthInDegrees = 101.25;
            else if (azimuthInInt >270 && azimuthInInt < 292.5) azimuthInDegrees = 78.75;
            else if (azimuthInInt >292.5 && azimuthInInt < 315) azimuthInDegrees =56.25 ;
            else if (azimuthInInt >315 && azimuthInInt < 337.5) azimuthInDegrees = 33.75;
            else if (azimuthInInt >337.5 && azimuthInInt < 360) azimuthInDegrees = 11.25d;*/

            /*if ( (360 >= azimuthInDegrees && azimuthInDegrees >= 337.5) || (0 <= azimuthInDegrees && azimuthInDegrees <= 22.5) ) azimuthInDegrees = 345.0f;
            else if (azimuthInDegrees > 22.5 && azimuthInDegrees < 67.5) azimuthInDegrees = 315.0f;
            else if (azimuthInDegrees >= 67.5 && azimuthInDegrees <= 112.5) azimuthInDegrees = 270.0f;
            else if (azimuthInDegrees > 112.5 && azimuthInDegrees < 157.5) azimuthInDegrees =  175.0f;
            else if (azimuthInDegrees >= 157.5 && azimuthInDegrees <= 202.5) azimuthInDegrees =135.0f;
            else if (azimuthInDegrees > 202.5 && azimuthInDegrees < 247.5) azimuthInDegrees = 220.0f;
            else if (azimuthInDegrees >= 247.5 && azimuthInDegrees <= 292.5) azimuthInDegrees = 80.0f;
            else if (azimuthInDegrees > 292.5 && azimuthInDegrees < 337.5) azimuthInDegrees = 42.0f;*/


            if(location!=null && mMap!=null  && changeOnSensor && oldAzimuthValues!=azimuthInDegrees) {
                oldAzimuthValues=azimuthInDegrees;
                Log.d("Azimuth in degrees", String.valueOf(checkAzimuth) + "        " + String.valueOf(azimuthInDegrees));
                drawNewMap();
                //mMap.stopAnimation();
            }

        }
    }
    private void drawNewMap(){
        if(location!=null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to Mountain View
                    .zoom(18)                   // Sets the zoom
                    .bearing((float) azimuthInDegrees)                // Sets the orientation of the camera to east
                    .tilt(90)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            setMarkers();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
    private double[] convertFloatsToDoubles(float[] input)
    {
        if (input == null)
            return null;

        double[] output = new double[input.length];

        for (int i = 0; i < input.length; i++)
            output[i] = input[i];

        return output;
    }
    //@Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        int acc = accuracy;
        String sensorType = sensor.getStringType();
        Log.d("-----------------------",acc + sensorType);
    }
}
    //Chnages for multiple marking

