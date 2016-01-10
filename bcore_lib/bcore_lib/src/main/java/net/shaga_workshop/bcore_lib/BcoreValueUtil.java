package net.shaga_workshop.bcore_lib;

/**
 * Created by shaga on 2015/11/09.
 */
public class BcoreValueUtil {

    /**
     * バッテリ値変換
     * @param value バッテリ値キャラクタリスティック値
     * @return バッテリ電圧(mV)
     */
    public static int convertBatteryVoltage(byte[] value) {
        if (value.length != BcoreConsts.BATTERY_VALUE_LEN) return 0;

        int voltage = (value[BcoreConsts.BATTERY_LOWER_IDX] & 0xff) + ((int)value[BcoreConsts.BATTERY_UPPER_IDX] << 8);
        return voltage;
    }

    /**
     * 有効モータ確認
     * @param value ファンクションキャラクタリスティック値
     * @return true=モータ対応/false=モータ非対応
     */
    public static boolean hasEnableMotor(byte[] value) {
        for (int i = 0; i < BcoreConsts.MAX_MOTOR_COUNT; i++) {
            if (isEnabledMotorIdx(value, i)) return true;
        }
        return false;
    }

    /**
     * 有効サーボ確認
     * @param value ファンクションキャラクタリスティック値
     * @return true=サーボ対応/false=サーボ非対応
     */
    public static boolean hasEnableServo(byte[] value) {
        for (int i = 0; i < BcoreConsts.MAX_SERVO_COUNT; i++) {
            if (isEnabledServoIdx(value, i)) return true;
        }
        return false;
    }

    /**
     * 有効ポート出力確認
     * @param value ファンクションキャラクタリスティック値
     * @return true=ポート出力対応/false=ポート出力非対応
     */
    public static boolean hasEnablePortOut(byte[] value) {
        for (int i = 0; i < BcoreConsts.MAX_PORT_COUNT; i++) {
            if (isEnabledPortIdx(value, i)) return true;
        }
        return false;
    }

    /**
     * モータポート確認
     * @param value ファンクションキャラクタリスティック値
     * @param idx モータポートインデックス
     * @return true=モータポート有効/false=モーターポート無効
     */
    public static boolean isEnabledMotorIdx(byte[] value, int idx) {
        return isEnabledFunction(value, idx, BcoreConsts.FUNCTIONS_MOTOR_IDX, BcoreConsts.MOTOR_FUNCTION_BITS);
    }

    /**
     * サーボポート確認
     * @param value ファンクションキャラクタリスティック値
     * @param idx サーボポートインデックス
     * @return true=サーボポート有効/false=サーボポート無効
     */
    public static boolean isEnabledServoIdx(byte[] value, int idx) {
        return isEnabledFunction(value, idx, BcoreConsts.FUNCTIONS_SERVO_PORT_IDX, BcoreConsts.SERVO_FUNCTION_BITS);
    }

    /**
     * ポート出力確認
     * @param value ファンクションキャラクタリスティック値
     * @param idx 出力ポートインデックス
     * @return true=出力ポート有効/false=出力ポート無効
     */
    public static boolean isEnabledPortIdx(byte[] value, int idx) {if (value == null) return false;
        return isEnabledFunction(value, idx, BcoreConsts.FUNCTIONS_SERVO_PORT_IDX, BcoreConsts.PORT_FUNCTION_BITS);
    }

    /**
     * ポート確認
     * @param value ファンクションキャラクタリスティック値
     * @param idx ポートインデックス
     * @param valueIdx ファンクションキャラクタリスティックインデックス
     * @param bits ポートビット配列
     * @return ture=ポート有効/false=ポート無効
     */
    private static boolean isEnabledFunction(byte[] value, int idx, int valueIdx, int[] bits) {
        if (value == null) return  false;
        if (value.length != BcoreConsts.FUNCTIONS_VALUE_LEN ||
                valueIdx < 0 || value.length <= valueIdx ||
                idx < 0 || bits.length <= idx) {
            return false;
        }

        byte data = value[valueIdx];
        int bit = bits[idx];
        return (data & bit) == bit;
    }

    /**
     * モータ出力値変換
     * @param value 変換元値
     * @param isFlip true=反転あり/false=反転なし
     * @return モータ出力値
     */
    public static byte convertMotorValue(int value, boolean isFlip) {
        value = convertValueRange(value, isFlip);
        return (byte)(value & 0xff);
    }

    /**
     * サーボ出力値変換
     * @param value 変換元値
     * @param isFlip true=反転あり/false=反転なし
     * @param trim 微調整値
     * @return サーボ出力値
     */
    public static byte convertServoValue(int value, boolean isFlip, int trim) {
        value = convertValueRange(value, false);
        if (isFlip) value = BcoreConsts.MOTOR_SERVO_MAX -value;
        value += trim;
        return (byte)(convertValueRange(value, false) & 0xff);
    }

    /**
     * モータ/サーボ出力値変換
     * @param value 変換元
     * @param isFlip true=反転あり/false=反転なし
     * @return 変換後出力値
     */
    private static int convertValueRange(int value, boolean isFlip) {
        if (value > BcoreConsts.MOTOR_SERVO_MAX) value = BcoreConsts.MOTOR_SERVO_MAX;
        else if (value < BcoreConsts.MOTOR_SERVO_MIN) value = BcoreConsts.MOTOR_SERVO_MIN;

        if (isFlip) value = BcoreConsts.MOTOR_SERVO_MAX - value;

        return value;
    }
}
