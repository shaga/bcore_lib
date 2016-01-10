package net.shaga_workshop.bcore_lib;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;

import java.lang.annotation.Target;
import java.util.ArrayList;

/**
 * bCoreスキャナ
 */
public class BcoreScanner {

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner bleScanner;

    private BcoreScannerListener bcoreScannerListener;
    private boolean isScanning;
    private Handler handlerTimeout;

    /**
     * スキャンタイムアウト処理
     */
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            handlerTimeout = null;
            stopScan();
        }
    };

    private ArrayList<ScanFilter> scanFilters;
    private ScanSettings scanSettings;
    private ScanCallback scanCallback;
    private BluetoothAdapter.LeScanCallback leScanCallback;

    public BcoreScanner(Context context) {
        btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (btManager == null) return;

        btAdapter = btManager.getAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleScanner = btAdapter.getBluetoothLeScanner();
            initScanCallback();
        } else {
            initLeScanCallback();
        }
    }

    /**
     * Bluetooth有効確認
     * @return true=有効/false=無効
     */
    public boolean isBluetoothEnabled() {
        return btAdapter != null && btAdapter.isEnabled();
    }

    /**
     * スキャンイベントリスナ登録
     * @param bcoreScannerListener　イベントリスナ
     */
    public void setOnBcoreScannerListener(BcoreScannerListener bcoreScannerListener) {
        this.bcoreScannerListener = bcoreScannerListener;
    }

    /**
     * スキャン中確認
     * @return true=スキャン中/false=非スキャン中
     */
    public boolean isScanning() {
        return isScanning;
    }

    /**
     * スキャン開始
     * @return true=スキャン開始成功/false=スキャン開始失敗
     */
    public boolean startScan() {

        if (!isBluetoothEnabled()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleScanner.startScan(scanFilters, scanSettings, scanCallback);
        } else {
            btAdapter.startLeScan(leScanCallback);
        }
        isScanning = true;
        handlerTimeout = new Handler();
        handlerTimeout.postDelayed(timeoutRunnable, 10000);

        return true;
    }

    /**
     * スキャン中止
     */
    public void stopScan() {
        if (handlerTimeout != null) {
            handlerTimeout.removeCallbacks(timeoutRunnable);
            handlerTimeout = null;
        }

        isScanning = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleScanner.stopScan(scanCallback);
        } else {
            btAdapter.stopLeScan(leScanCallback);
        }

        if (bcoreScannerListener != null) {
            bcoreScannerListener.onScanCompleted();
        }
    }

    /**
     * コールバック初期化(4.3/4.4)
     */
    private void initLeScanCallback()
    {
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            /**
             * アドバタイジングサービスUUID取得
             * @param scanRecord アドバタイジングパケット
             * @return UUID文字列
             */
            private String getAdvertisingService(byte[] scanRecord) {
                int pos = 0;
                int len = scanRecord[pos];
                byte type = 0;
                while (pos < scanRecord.length - 2 && len != 0) {
                    type = scanRecord[pos + 1];

                    if (type == 6 || type == 7) {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < len - 1; i++) {
                            if (i == 4 || i == 6 || i== 8 || i == 10) builder.append("-");

                            builder.append(String.format("%02x", scanRecord[pos + len - i]));
                        }

                        return new String(builder);
                    }

                    pos += len + 1;
                    len = scanRecord[pos];
                }


                return null;
            }

            /**
             * bCoreサービス確認
             * @param scanRecord　アドバタイジングパケット
             * @return true=bCoreサービスあり/false=bCoreサービスなし
             */
            private boolean isIncludeBcoreService(byte[] scanRecord) {
                String serviceUuid = getAdvertisingService(scanRecord);
                return serviceUuid != null && serviceUuid.equalsIgnoreCase(BcoreUuid.UUID_STR_BCORE_SERVICE);
            }


            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (bcoreScannerListener != null && isIncludeBcoreService(scanRecord)) {
                    bcoreScannerListener.onFoundBcore(device.getName(), device.getAddress());
                }
            }
        };
    }

    /**
     * コールバック初期化(5.0以上)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initScanCallback()
    {
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BcoreUuid.UUID_STR_BCORE_SERVICE)).build();
        scanFilters = new ArrayList<>();
        scanFilters.add(filter);
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (bcoreScannerListener != null) {
                    bcoreScannerListener.onFoundBcore(result.getDevice().getName(), result.getDevice().getAddress());
                }
            }
        };
    }
}
