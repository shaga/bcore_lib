package net.shaga_workshop.bcore_lib;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * bCore接続サービス
 */
public class BcoreControlService extends Service {
    public static final int BCORE_CONNECTION_STATE_DISCONNECTED = 0;
    public static final int BCORE_CONNECTION_STATE_CONNECTING = 1;
    public static final int BCORE_CONNECTION_STATE_CONNECTED = 2;
    public static final int BCORE_CONNECTION_STATE_DISCONNECTING = 3;

    /**
     * Binder for BcoreControlService
     */
    public class LocalBinder extends Binder {
        public BcoreControlService getService() { return BcoreControlService.this; }
    }

    private LocalBinder binder = new LocalBinder();
    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothGatt btGatt;
    private String connectAddress;
    private BluetoothGattService bcoreService;
    private BluetoothGattCharacteristic bcoreBatteryCharacteristic;
    private BluetoothGattCharacteristic bcoreMotorCharacteristic;
    private BluetoothGattCharacteristic bcorePortOutCharacteristic;
    private BluetoothGattCharacteristic bcoreServoCharacteristic;
    private BluetoothGattCharacteristic bcoreFunctionsCharacteristic;
    private Handler timerHandlerReadBattery;
    private int connectionState;
    private int timerSpanReadBattery;

    private boolean isExecute = false;

    private Runnable mUpdateBatteryVoltageRunnable = new Runnable() {
        @Override
        public void run() {
            readBatteryVoltage();
        }
    };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            switch (newState) {
                // 接続完了
                case BluetoothGatt.STATE_CONNECTED:
                    connectionState = BCORE_CONNECTION_STATE_CONNECTED;
                    // 接続完了通知
                    sendBroadcast(BcoreInfoReceiver.BCORE_ACTION_KEY_CONNECTION_CHANGED, BcoreInfoReceiver.BCORE_EXTRA_KEY_CONNECTION_CHANGED, BCORE_CONNECTION_STATE_CONNECTED);
                    // サービス検索
                    gatt.discoverServices();
                    break;
                // 切断完了
                case BluetoothGatt.STATE_DISCONNECTED:
                    connectionState = BCORE_CONNECTION_STATE_DISCONNECTED;
                    // 切断完了通知
                    sendBroadcast(BcoreInfoReceiver.BCORE_ACTION_KEY_CONNECTION_CHANGED, BcoreInfoReceiver.BCORE_EXTRA_KEY_CONNECTION_CHANGED, BCORE_CONNECTION_STATE_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            // bCoreサービス取得
            bcoreService = gatt.getService(BcoreUuid.UUID_BCORE_SERVICE);

            if (bcoreService == null) {
                // サービス検索失敗通知
                sendBroadcast(BcoreInfoReceiver.BCORE_ACTION_KEY_DISCOVERED_SERVICE, BcoreInfoReceiver.BCORE_EXTRA_KEY_DISCOVERED_SERVICE, false);
                return;
            }

            // キャラクタリスティック取得
            bcoreBatteryCharacteristic = bcoreService.getCharacteristic(BcoreUuid.UUID_BCORE_BATTERY_VOLTAGE);
            bcoreMotorCharacteristic = bcoreService.getCharacteristic(BcoreUuid.UUID_BCORE_MOTOR_PWM);
            bcorePortOutCharacteristic = bcoreService.getCharacteristic(BcoreUuid.UUID_BCORE_PORT_OUT);
            bcoreServoCharacteristic = bcoreService.getCharacteristic(BcoreUuid.UUID_BCORE_SERVO_POS);
            bcoreFunctionsCharacteristic = bcoreService.getCharacteristic(BcoreUuid.UUID_BCORE_FUNCTIONS);

            // サービス検索成功通知
            sendBroadcast(BcoreInfoReceiver.BCORE_ACTION_KEY_DISCOVERED_SERVICE, BcoreInfoReceiver.BCORE_EXTRA_KEY_DISCOVERED_SERVICE, true);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            byte[] data = characteristic.getValue();

            if (characteristic == bcoreBatteryCharacteristic) {
                int battery = BcoreValueUtil.convertBatteryVoltage(data);
                // バッテリー値通知
                sendBroadcast(BcoreInfoReceiver.BCORE_ACTION_KEY_READ_BATTERY, BcoreInfoReceiver.BCORE_EXTRA_KEY_BATTERY, battery);

                // タイマ有効なら次の読込を設定
                if (timerSpanReadBattery > 0)
                    timerHandlerReadBattery.postDelayed(mUpdateBatteryVoltageRunnable, timerSpanReadBattery);
            } else if (characteristic == bcoreFunctionsCharacteristic) {
                sendBroadcast(BcoreInfoReceiver.BCORE_ACTION_KEY_READ_FUNCTIONS, BcoreInfoReceiver.BCORE_EXTRA_KEY_FUNCTIONS, data);

                // バッテリ読込タイマが設定されていれば読込開始
                if (timerSpanReadBattery > 0)
                    readBatteryVoltage();
            }

            isExecute = false;
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            isExecute = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (btManager == null) return;

        btAdapter = btManager.getAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * バッテリ値読込タイマ設定
     * @param spanReadBatteryMsec タイマ間隔(msec)
     */
    public void setTimerSpanReadBattery(int spanReadBatteryMsec) {
        timerSpanReadBattery = spanReadBatteryMsec;

        if (timerSpanReadBattery > 0){
            timerHandlerReadBattery = new Handler();
            if (connectionState == BCORE_CONNECTION_STATE_CONNECTED)
                readBatteryVoltage();
        } else {
            if (timerHandlerReadBattery != null) {
                timerHandlerReadBattery.removeCallbacks(mUpdateBatteryVoltageRunnable);
                timerHandlerReadBattery = null;
            }
        }
    }

    /**
     * bCore接続
     * @param address　接続bCoreアドレス
     * @param spanReadBatteryMsec バッテリ値読込タイマ間隔
     * @return
     */
    public boolean connect(String address, int spanReadBatteryMsec) {
        setTimerSpanReadBattery(spanReadBatteryMsec);
        return connect(address);
    }

    /**
     * bCore接続
     * @param address 接続bCoreアドレス
     * @return 成功/失敗
     */
    public boolean connect(String address) {
        if (btAdapter == null || !btAdapter.isEnabled()) return false;

        if (connectionState == BCORE_CONNECTION_STATE_CONNECTED ||
                connectionState == BCORE_CONNECTION_STATE_CONNECTING) return true;

        if (btGatt != null && address.equalsIgnoreCase(connectAddress)) {
            if (btGatt.connect()) {
                return true;
            }
        }

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        if (device == null) return false;

        connectAddress = address;
        btGatt = device.connectGatt(this, false, gattCallback);

        connectionState = BCORE_CONNECTION_STATE_CONNECTING;
        sendBroadcast(BcoreInfoReceiver.BCORE_ACTION_KEY_CONNECTION_CHANGED, BcoreInfoReceiver.BCORE_EXTRA_KEY_CONNECTION_CHANGED, connectionState);

        return true;
    }

    /**
     * bCore切断
     */
    public void disconnect() {
        if (btGatt == null) return;;

        if (timerHandlerReadBattery != null) {
            timerHandlerReadBattery.removeCallbacksAndMessages(null);
            timerHandlerReadBattery = null;
        }

        btGatt.disconnect();
        connectionState = BCORE_CONNECTION_STATE_DISCONNECTING;
        sendBroadcast(BcoreInfoReceiver.BCORE_ACTION_KEY_CONNECTION_CHANGED, BcoreInfoReceiver.BCORE_EXTRA_KEY_CONNECTION_CHANGED, connectionState);
    }

    /**
     * 接続状態取得
     * @return 接続状態
     */
    public int getConnectionState() {
        return connectionState;
    }

    /**
     * バッテリ電圧読込
     */
    public void readBatteryVoltage() {
        readCharacteristic(bcoreBatteryCharacteristic);
    }

    /**
     * モータPWM値書き込み
     * @param idx 設定モータインデックス
     * @param pwm PWM値
     */
    public void writeMotorPwm(byte idx, byte pwm) {
        writeCharacteristic(bcoreMotorCharacteristic, new byte[]{idx, pwm});
    }

    /**
     * ポートアウト設定書き込み
     * @param data ポートアウト値
     */
    public void writePortOut(byte data) {
        writeCharacteristic(bcorePortOutCharacteristic, new byte[]{data});
    }

    /**
     * サーボ位置書き込み
     * @param servoIdx 設定サーボインデックス
     * @param data サーボ位置
     */
    public void writeServoPos(int servoIdx, byte data) {
        writeCharacteristic(bcoreServoCharacteristic, new byte[]{(byte) servoIdx, data});
    }

    /**
     * bCoreチャンネル設定読み込み
     */
    public void readFunctions() {
        readCharacteristic(bcoreFunctionsCharacteristic);
    }

    /**
     * キャラクタリスティック書き込み
     * @param characteristic 対象キャラクタリスティック
     * @param value キャラクタリスティック値
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (characteristic == null || value == null ||
                ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != BluetoothGattCharacteristic.PROPERTY_WRITE &&
                        (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE))
        {
            return;
        }
        synchronized (this) {
            while(isExecute) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    break;
                }
            }
            isExecute = true;
            characteristic.setValue(value);
            btGatt.writeCharacteristic(characteristic);
        }
    }

    /**
     * キャラクタリスティック読み込み
     * @param characteristic 対象キャラクタリスティック
     */
    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null ||
                ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != BluetoothGattCharacteristic.PROPERTY_READ))
        {
            return;
        }

        synchronized (this) {
            while(isExecute) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    break;
                }
            }
            btGatt.readCharacteristic(characteristic);
        }
    }

    /**
     * ブロードキャスト送信
     * @param actionKey  アクション
     * @param extraKey 引数名
     * @param extraValue 引数値(boolean)
     */
    private void sendBroadcast(String actionKey, String extraKey, boolean extraValue) {
        Intent intent = new Intent(actionKey);
        intent.putExtra(extraKey, extraValue);
        sendBroadcast(intent);
    }

    /**
     * ブロードキャスト送信
     * @param actionKey アクション
     * @param extraKey 引数名
     * @param extraValue 引数値(byte array)
     */
    private void sendBroadcast(String actionKey, String extraKey, byte[] extraValue) {
        Intent intent = new Intent(actionKey);
        intent.putExtra(extraKey, extraValue);
        sendBroadcast(intent);
    }

    /**
     * ブロードキャスト送信
     * @param actionKey アクション
     * @param extraKey 引数名
     * @param extraValue 引数値(int)
     */
    private void sendBroadcast(String actionKey, String extraKey, int extraValue) {
        Intent intent = new Intent(actionKey);
        intent.putExtra(extraKey, extraValue);
        sendBroadcast(intent);
    }
}
