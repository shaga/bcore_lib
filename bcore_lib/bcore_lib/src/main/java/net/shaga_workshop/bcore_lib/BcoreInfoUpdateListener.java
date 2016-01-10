package net.shaga_workshop.bcore_lib;

/**
 * bCore更新イベントリスナ
 */
public interface BcoreInfoUpdateListener {

    /**
     * bCore接続状態更新イベント
     * @param state 接続状態
     */
    void onConnectionChanged(int state);

    /**
     * bCoreサービス発見イベント
     */
    void onDiscoveredService(boolean isDiscovered);

    /**
     * bCoreバッテリ値読込完了イベント
     * @param vol バッテリ値(mV)
     */
    void onReadBatteryVoltage(int vol);

    /**
     * bCoreファンクション値読込イベント
     * @param value ファンクションキャラクタリスティック値
     */
    void onReadBcoreFunctions(byte[] value);
}
