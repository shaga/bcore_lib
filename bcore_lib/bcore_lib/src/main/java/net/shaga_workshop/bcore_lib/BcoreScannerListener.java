package net.shaga_workshop.bcore_lib;

/**
 * bCoreスキャンイベントリスナ
 */
public interface BcoreScannerListener {
    /**
     * bCore発見イベント
     * @param name bCore デバイス名
     * @param addr bCore MACアドレス
     */
    void onFoundBcore(String name, String addr);

    /**
     * bCoreスキャン終了イベント
     */
    void onScanCompleted();
}
