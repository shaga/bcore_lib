package net.shaga_workshop.bcore_lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * bCore通信結果レシーバ
 */
public class BcoreInfoReceiver extends BroadcastReceiver {
    public static final String BCORE_ACTION_KEY_READ_BATTERY = "net.shaga_workshop.bcore_lib.BcoreInfoReceiver.action_read_battery";
    public static final String BCORE_EXTRA_KEY_BATTERY = "net.shaga_workshop.bcore_lib.BcoreInfoReceiver.extra_battery";
    public static final String BCORE_ACTION_KEY_READ_FUNCTIONS = "net.shaga_workshop.bcore_lib.BcoreInfoReceiver.action_read_functions";
    public static final String BCORE_EXTRA_KEY_FUNCTIONS = "net.shaga_workshop.bcore_lib.BcoreInfoReceiver.extra_functions";
    public static final String BCORE_ACTION_KEY_CONNECTION_CHANGED = "net.shaga_workshop.bcore_lib.BcoreInfoReceiver.action_connection_changed";
    public static final String BCORE_EXTRA_KEY_CONNECTION_CHANGED = "net.shaga_workshop.bcore_lib.BcoreInfoReceiver.extra_connection_state";
    public static final String BCORE_ACTION_KEY_DISCOVERED_SERVICE = "net.shaga_workshop.bcore_lib.BcoreInfoReceiver.action_discovered_service";
    public static final String BCORE_EXTRA_KEY_DISCOVERED_SERVICE = "net.shaga_workshop.bcore_lib.BcoreInfoReceiver.extra_discovered_service";

    private BcoreInfoUpdateListener listener;

    /**
     * レシーバ向けIntentFilter生成
     * @return IntentFilter
     */
    public static IntentFilter createIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BCORE_ACTION_KEY_CONNECTION_CHANGED);
        filter.addAction(BCORE_ACTION_KEY_DISCOVERED_SERVICE);
        filter.addAction(BCORE_ACTION_KEY_READ_BATTERY);
        filter.addAction(BCORE_ACTION_KEY_READ_FUNCTIONS);
        return filter;
    }

    /**
     * イベントリスナ登録
     * @param listener イベントリスナ
     */
    public void setBcoreInfoUpdateListener(BcoreInfoUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener == null) return;

        String action = intent.getAction();

        switch (action) {
            // bCore接続状態変更
            case BCORE_ACTION_KEY_CONNECTION_CHANGED:
                if (intent.getExtras().containsKey(BCORE_EXTRA_KEY_CONNECTION_CHANGED)) {
                    int state = intent.getExtras().getInt(BCORE_EXTRA_KEY_CONNECTION_CHANGED);
                    listener.onConnectionChanged(state);
                }
                break;
            // bCoreサービス発見
            case BCORE_ACTION_KEY_DISCOVERED_SERVICE:
                boolean isDiscovered = false;
                if (intent.getExtras().containsKey(BCORE_EXTRA_KEY_DISCOVERED_SERVICE)) {
                    isDiscovered = intent.getExtras().getBoolean(BCORE_EXTRA_KEY_DISCOVERED_SERVICE);
                }
                listener.onDiscoveredService(isDiscovered);
                break;
            // bCoreバッテリ値読込完了
            case BCORE_ACTION_KEY_READ_BATTERY:
                if (intent.getExtras().containsKey(BCORE_EXTRA_KEY_BATTERY)) {
                    int voltage = intent.getExtras().getInt(BCORE_EXTRA_KEY_BATTERY);
                    listener.onReadBatteryVoltage(voltage);
                }
                break;
            // bCoreファンクション読込完了
            case BCORE_ACTION_KEY_READ_FUNCTIONS:
                if (intent.getExtras().containsKey(BCORE_EXTRA_KEY_FUNCTIONS)) {
                    byte[] value = intent.getExtras().getByteArray(BCORE_EXTRA_KEY_FUNCTIONS);
                    listener.onReadBcoreFunctions(value);
                }
                break;
        }

    }
}
