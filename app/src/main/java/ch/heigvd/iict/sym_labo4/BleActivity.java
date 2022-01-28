/**
 * Nom de fichier: BleActivity.java
 * Description: Gestion des périphériques bluetooth et plus précisement l'écran espruino pixel.js
 * Auteurs: Basset Nils, Da Rocha Carvalho Bruno, Thurnherr Gabrielle
 * Date: 27.01.2022
 */

package ch.heigvd.iict.sym_labo4;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SimpleTimeZone;

import ch.heigvd.iict.sym_labo4.abstractactivies.BaseTemplateActivity;
import ch.heigvd.iict.sym_labo4.adapters.ResultsAdapter;
import ch.heigvd.iict.sym_labo4.viewmodels.BleOperationsViewModel;

/**
 * Project: Labo4
 * Created by fabien.dutoit on 11.05.2019
 * Updated by fabien.dutoit on 06.11.2020
 * (C) 2019 - HEIG-VD, IICT
 */
public class BleActivity extends BaseTemplateActivity {

    private static final String TAG = BleActivity.class.getSimpleName();

    //system services
    private BluetoothAdapter bluetoothAdapter = null;

    //view model
    private BleOperationsViewModel bleViewModel = null;

    //gui elements
    private View operationPanel = null;
    private View scanPanel = null;

    private ListView scanResults = null;
    private TextView emptyScanResults = null;

    //menu elements
    private MenuItem scanMenuBtn = null;
    private MenuItem disconnectMenuBtn = null;

    //adapters
    private ResultsAdapter scanResultsAdapter = null;

    //states
    private Handler handler = null;
    private boolean isScanning = false;

    //button
    private Button sendCurrentTimeBTN = null;
    private Button readTemperatureBTN = null;
    private Button sendIntegerValBTN = null;

    //textView
    private TextView readTemperatureTV = null;
    private TextView readNbrClickTV = null;
    private TextView readCurrentTimeTV = null;

    //EditText
    private EditText sendIntegerValET = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Informations :")
                .setMessage("L'application nécessite l'activation du bluetooth")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();

        this.handler = new Handler();

        //enable and start bluetooth - initialize bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();

        //link GUI
        this.operationPanel = findViewById(R.id.ble_operation);
        this.scanPanel = findViewById(R.id.ble_scan);

        this.scanResults = findViewById(R.id.ble_scanresults);
        this.emptyScanResults = findViewById(R.id.ble_scanresults_empty);

        //manage scanned item
        this.scanResultsAdapter = new ResultsAdapter(this);
        this.scanResults.setAdapter(this.scanResultsAdapter);
        this.scanResults.setEmptyView(this.emptyScanResults);

        //connect to view model
        this.bleViewModel = new ViewModelProvider(this).get(BleOperationsViewModel.class);

        updateGui();

        //events
        this.scanResults.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            runOnUiThread(() -> {
                //we stop scanning
                scanLeDevice(false);
                //we connect
                bleViewModel.connect(((ScanResult)scanResultsAdapter.getItem(position)).getDevice());
            });
        });

        //ble events
        this.bleViewModel.isConnected().observe(this, (isConnected) -> updateGui() );

        //Send current time
        this.sendCurrentTimeBTN = findViewById(R.id.send_current_time);
        this.sendCurrentTimeBTN.setOnClickListener(v -> {
            this.bleViewModel.sendCurrentTime();
        });

        //Read temperature
        this.readTemperatureBTN = findViewById(R.id.read_temperature);
        this.readTemperatureBTN.setOnClickListener(v -> {
            this.bleViewModel.readTemperature();
        });
        this.readTemperatureTV = findViewById(R.id.read_temperature_value);
        this.bleViewModel.getTemperature().observe(this, temperature -> {
            readTemperatureTV.setText(temperature.toString() + " °C");
        });

        //Read nbr click
        this.readNbrClickTV = findViewById(R.id.read_clickcounter_value);
        this.bleViewModel.getCptBtnClick().observe(this, nbrClick -> {
            readNbrClickTV.setText(nbrClick.toString());
        });

        //Read current time
        this.readCurrentTimeTV = findViewById(R.id.read_current_time_value);
        this.bleViewModel.getCalendar().observe(this, time -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
            readCurrentTimeTV.setText(dateFormat.format(time.getTime()));
        });

        //Send integer value
        this.sendIntegerValET = findViewById(R.id.send_integer_ET);
        this.sendIntegerValBTN = findViewById(R.id.send_integer);
        this.sendIntegerValBTN.setOnClickListener(v -> {
            try {
                Integer i = Integer.parseUnsignedInt(sendIntegerValET.getText().toString());
                this.bleViewModel.sendIntegerVal(i);
            } catch (final NumberFormatException e) {
                Toast.makeText(this, "Erreur saisie", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ble_menu, menu);
        //we link the two menu items
        this.scanMenuBtn = menu.findItem(R.id.menu_ble_search);
        this.disconnectMenuBtn = menu.findItem(R.id.menu_ble_disconnect);
        //we update the gui
        updateGui();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_ble_search) {
            if(isScanning)
                scanLeDevice(false);
            else
                scanLeDevice(true);
            return true;
        }
        else if (id == R.id.menu_ble_disconnect) {
            bleViewModel.disconnect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(this.isScanning)
            scanLeDevice(false);
        if(isFinishing())
            this.bleViewModel.disconnect();
    }

    /*
     * Method used to update the GUI according to BLE status:
     * - connected: display operation panel (BLE control panel)
     * - not connected: display scan result list
     */
    private void updateGui() {
        Boolean isConnected = this.bleViewModel.isConnected().getValue();
        if(isConnected != null && isConnected) {
            this.scanPanel.setVisibility(View.GONE);
            this.operationPanel.setVisibility(View.VISIBLE);

            if(this.scanMenuBtn != null && this.disconnectMenuBtn != null) {
                this.scanMenuBtn.setVisible(false);
                this.disconnectMenuBtn.setVisible(true);
            }
        } else {
            this.operationPanel.setVisibility(View.GONE);
            this.scanPanel.setVisibility(View.VISIBLE);
            if(this.scanMenuBtn != null && this.disconnectMenuBtn != null) {
                this.disconnectMenuBtn.setVisible(false);
                this.scanMenuBtn.setVisible(true);
            }
        }
    }

    //this method needs user grant localisation and/or bluetooth permissions, our demo app is requesting them on MainActivity
    private void scanLeDevice(final boolean enable) {
        final BluetoothLeScanner bluetoothScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (enable) {

            //config
            ScanSettings.Builder builderScanSettings = new ScanSettings.Builder();
            builderScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            builderScanSettings.setReportDelay(0);

            // Filtre scan blueooth
            // le service "SYM" (UUID: "3c0a1000-281d-4b48-b2a7-f15579a1c38f")
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString("3c0a1000-281d-4b48-b2a7-f15579a1c38f"))
                    .build();
            filters.add(scanFilter);

            //reset display
            scanResultsAdapter.clear();

            bluetoothScanner.startScan(filters, builderScanSettings.build(), leScanCallback);
            Log.d(TAG,"Start scanning...");
            isScanning = true;

            //we scan only for 15 seconds
            handler.postDelayed(() -> {
                scanLeDevice(false);
            }, 15*1000L);

        } else {
            bluetoothScanner.stopScan(leScanCallback);
            isScanning = false;
            Log.d(TAG,"Stop scanning (manual)");
        }
    }

    // Device scan callback.
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            runOnUiThread(() -> {
                scanResultsAdapter.addDevice(result);
            });
        }
    };

}
