package ch.heigvd.iict.sym_labo4;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import ch.heigvd.iict.sym_labo4.gl.OpenGLRenderer;

public class CompassActivity extends AppCompatActivity implements SensorEventListener{

    //opengl
    private OpenGLRenderer  opglr           = null;
    private GLSurfaceView   m3DView         = null;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magneticField;

    private float[] rotation = new float[16];
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // we need fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // we initiate the view
        setContentView(R.layout.activity_compass);

        //we create the renderer
        this.opglr = new OpenGLRenderer(getApplicationContext());

        // link to GUI
        this.m3DView = findViewById(R.id.compass_opengl);

        //init opengl surface view
        this.m3DView.setRenderer(this.opglr);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    /*
     TODO
        your activity need to register to accelerometer and magnetometer sensors' updates
        then you may want to call
        opglr.swapRotMatrix()
        with the 4x4 rotation matrix, every time a new matrix is computed
        more information on rotation matrix can be found online:
        https://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[],%20float[],%20float[],%20float[])
    */

    @Override
    public void onSensorChanged(SensorEvent event){
        switch(event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, gravity, 0, gravity.length);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, geomagnetic, 0, geomagnetic.length);
                break;
            default:
                break;
        }
        sensorManager.getRotationMatrix(this.opglr.swapRotMatrix(rotation), null, gravity, geomagnetic);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume(){
        super.onResume();
        sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener((SensorEventListener) this, magneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
