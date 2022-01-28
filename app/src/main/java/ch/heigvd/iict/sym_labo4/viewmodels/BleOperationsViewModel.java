/**
 * Nom de fichier: BleOperationViewModel.java
 * Description: Opération des différentes interaction entre l'utilisateur et le device
 * Auteurs: Basset Nils, Da Rocha Carvalho Bruno, Thurnherr Gabrielle
 * Date: 27.01.2022
 */

package ch.heigvd.iict.sym_labo4.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.lang.Integer;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.observer.ConnectionObserver;

/**
 * Project: Labo4
 * Created by fabien.dutoit on 11.05.2019
 * Updated by fabien.dutoit on 19.10.2021
 * (C) 2019 - HEIG-VD, IICT
 */
public class BleOperationsViewModel extends AndroidViewModel {

    private static final String TAG = BleOperationsViewModel.class.getSimpleName();

    private SYMBleManager ble = null;
    private BluetoothGatt mConnection = null;

    //live data - observer
    private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>(false);
    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    private final MutableLiveData<Calendar> mCalendar = new MutableLiveData<>();
    public LiveData<Calendar> getCalendar() { return mCalendar; }

    private final MutableLiveData<Integer> mCptBtnClick = new MutableLiveData<>();
    public LiveData<Integer> getCptBtnClick() { return mCptBtnClick; }

    private final MutableLiveData<Float> mTemperature = new MutableLiveData<>();
    public LiveData<Float> getTemperature() { return mTemperature; }

    //Services and Characteristics of the SYM Pixl
    private BluetoothGattService timeService = null, symService = null;
    private BluetoothGattCharacteristic currentTimeChar = null, integerChar = null, temperatureChar = null, buttonClickChar = null;

    //UUID current time service
    private UUID CURRENT_TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    private UUID CURRENT_TIME = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");

    //UUID custom sym service
    private UUID CUSTOM_SYM_SERVICE = UUID.fromString("3c0a1000-281d-4b48-b2a7-f15579a1c38f");
    private UUID CUSTOM_SYM_INT = UUID.fromString("3c0a1001-281d-4b48-b2a7-f15579a1c38f");
    private UUID CUSTOM_SYM_TEMPERATURE = UUID.fromString("3c0a1002-281d-4b48-b2a7-f15579a1c38f");
    private UUID CUSTOM_SYM_BTN = UUID.fromString("3c0a1003-281d-4b48-b2a7-f15579a1c38f");


    public BleOperationsViewModel(Application application) {
        super(application);
        this.mIsConnected.setValue(false);
        this.ble = new SYMBleManager(application.getApplicationContext());
        this.ble.setConnectionObserver(this.bleConnectionObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared");
        this.ble.disconnect();
    }

    public void connect(BluetoothDevice device) {
        Log.d(TAG, "User request connection to: " + device);
        if(!mIsConnected.getValue()) {
            this.ble.connect(device)
                    .retry(1, 100)
                    .useAutoConnect(false)
                    .enqueue();
        }
    }

    public void disconnect() {
        Log.d(TAG, "User request disconnection");
        this.ble.disconnect();
        if(mConnection != null) {
            mConnection.disconnect();
        }
    }


    //Méthode permettant l'intéraction entre l'utilisateur et le device *****
    public boolean sendCurrentTime() {
        if(!isConnected().getValue() || currentTimeChar == null) return false;
        return ble.sendCurrentTime();
    }

    public boolean readTemperature() {
        if(!isConnected().getValue() || temperatureChar == null) return false;
        return ble.readTemperature();
    }

    public boolean sendIntegerVal(long val) {
        if(!isConnected().getValue() || integerChar == null) return false;
        return ble.sendIntegerVal(val);
    }
    //*****

    private final ConnectionObserver bleConnectionObserver = new ConnectionObserver() {
        @Override
        public void onDeviceConnecting(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceConnecting");
            mIsConnected.setValue(false);
        }

        @Override
        public void onDeviceConnected(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceConnected");
            mIsConnected.setValue(true);
        }

        @Override
        public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceDisconnecting");
            mIsConnected.setValue(false);
        }

        @Override
        public void onDeviceReady(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceReady");
        }

        @Override
        public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
            Log.d(TAG, "onDeviceFailedToConnect");
        }

        @Override
        public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
            if(reason == ConnectionObserver.REASON_NOT_SUPPORTED) {
                Log.d(TAG, "onDeviceDisconnected - not supported");
                Toast.makeText(getApplication(), "Device not supported - implement method isRequiredServiceSupported()", Toast.LENGTH_LONG).show();
            }
            else
                Log.d(TAG, "onDeviceDisconnected");

            mIsConnected.setValue(false);
        }
    };

    private class SYMBleManager extends BleManager {

        private SYMBleManager(Context applicationContext) {
            super(applicationContext);
        }

        /**
         * BluetoothGatt callbacks object.
         */
        private BleManagerGattCallback mGattCallback = null;
        @Override
        @NonNull
        public BleManagerGattCallback getGattCallback() {
            //we initiate the mGattCallback on first call
            if(mGattCallback == null) {
                this.mGattCallback = new BleManagerGattCallback() {

                    @Override
                    public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
                        mConnection = gatt; //trick to force disconnection
                        Log.d(TAG, "isRequiredServiceSupported - TODO");

                        //Check si tous les services/charactristic sont présent
                        for(BluetoothGattService service : gatt.getServices()){ //Get les service du device
                            if(CURRENT_TIME_SERVICE.equals(service.getUuid())) {
                                timeService = service;
                                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){ //Get les caractéristique des service du device
                                    if(CURRENT_TIME.equals(characteristic.getUuid())){
                                        currentTimeChar = characteristic;
                                    }
                                }
                            }else if(CUSTOM_SYM_SERVICE.equals(service.getUuid())){
                                symService = service;
                                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){ //Get les caractéristique des service du device
                                    if(CUSTOM_SYM_INT.equals(characteristic.getUuid())){
                                        integerChar = characteristic;
                                    }else if(CUSTOM_SYM_TEMPERATURE.equals(characteristic.getUuid())){
                                        temperatureChar = characteristic;
                                    }else if(CUSTOM_SYM_BTN.equals(characteristic.getUuid())){
                                        buttonClickChar = characteristic;
                                    }
                                }
                            }
                        }
                        //Return true si tous les services/caractéristiques ont été trouvés
                        return (currentTimeChar != null && integerChar != null && temperatureChar != null && buttonClickChar != null);
                    }

                    //Initialisation des services qui reçoivent des notifications
                    @Override
                    protected void initialize() {
                        //Réception de la date/heure
                        setNotificationCallback(currentTimeChar).with(((device, data) -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.YEAR, data.getIntValue(Data.FORMAT_UINT16,0));
                            calendar.set(Calendar.MONTH, data.getIntValue(Data.FORMAT_UINT8,2) - 1);
                            calendar.set(Calendar.DAY_OF_MONTH, data.getIntValue(Data.FORMAT_UINT8,3));
                            calendar.set(Calendar.HOUR, data.getIntValue(Data.FORMAT_UINT8,4));
                            calendar.set(Calendar.MINUTE, data.getIntValue(Data.FORMAT_UINT8,5));
                            calendar.set(Calendar.SECOND, data.getIntValue(Data.FORMAT_UINT8,6));
                            mCalendar.setValue(calendar);
                        }));
                        enableNotifications(currentTimeChar).enqueue();

                        //Réception du nombre de clique sur les bouttons
                        setNotificationCallback(buttonClickChar).with((device, data) -> {
                            mCptBtnClick.setValue(data.getIntValue(Data.FORMAT_UINT8, 0));
                        });
                        enableNotifications(buttonClickChar).enqueue();
                    }

                    @Override
                    protected void onServicesInvalidated() {
                        //we reset services and characteristics
                        timeService = null;
                        currentTimeChar = null;

                        symService = null;
                        integerChar = null;
                        temperatureChar = null;
                        buttonClickChar = null;
                    }

                };
            }
            return mGattCallback;
        };

        public boolean sendCurrentTime() {
            Calendar calendar = Calendar.getInstance();

            //Creation tableau de byte pour l'envoi de la date/heure au device
            byte[] val = new byte[8];
            val[0] = (byte)(calendar.get(Calendar.YEAR));
            val[1] = (byte)(calendar.get(Calendar.YEAR) >> 8);
            val[2] = (byte)(calendar.get(Calendar.MONTH) + 1);
            val[3] = (byte)(calendar.get(Calendar.DAY_OF_MONTH));
            val[4] = (byte)(calendar.get(Calendar.HOUR));
            val[5] = (byte)(calendar.get(Calendar.MINUTE));
            val[6] = (byte)(calendar.get(Calendar.SECOND));

            currentTimeChar.setValue(val);
            writeCharacteristic(currentTimeChar, val, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT).enqueue();
            return true;
        }

        public boolean readTemperature() {
            readCharacteristic(temperatureChar).with((device, data) -> {
                //On reçoit la température *10 (pour avoir un uint) donc on divise par 10 et on transforme en float pour avoir un chiffre à virgule
                mTemperature.setValue(data.getIntValue(Data.FORMAT_UINT16, 0).floatValue() / 10);
            }).enqueue();
            return true;
        }

        public boolean sendIntegerVal(long val) {
            //Création tableau 4 byte pour mettre le uint32 pour l'envoie au device
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (val & 0xff);
            bytes[1] = (byte) ((val >> 8) & 0xff);
            bytes[2] = (byte) ((val >> 16) & 0xff);
            bytes[3] = (byte) ((val >> 24) & 0xff);
            integerChar.setValue(bytes);
            writeCharacteristic(integerChar, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT).enqueue();
            return true;
        }
    }
}
