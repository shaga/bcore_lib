package net.shaga_workshop.bcore_lib;

/**
 * bCore定数
 */
public class BcoreConsts {
    public static final int MOTOR_STOP_VALUE = 0x80;
    public static final int MOTOR_SERVO_MAX = 0xff;
    public static final int MOTOR_SERVO_MIN = 0x00;
    public static final int SERVO_CENTER_POS = 0x80;

    public static final int FUNCTIONS_VALUE_LEN = 2;
    public static final int FUNCTIONS_MOTOR_IDX = 0;
    public static final int FUNCTIONS_SERVO_PORT_IDX = 1;
    public static final int MAX_MOTOR_COUNT = 4;
    public static final int[] MOTOR_FUNCTION_BITS = {0x01, 0x02, 0x04, 0x08};
    public static final int MAX_SERVO_COUNT = 4;
    public static final int[] SERVO_FUNCTION_BITS = {0x01, 0x02, 0x04, 0x08};
    public static final int MAX_PORT_COUNT = 4;
    public static final int[] PORT_FUNCTION_BITS = {0x10, 0x20, 0x40, 0x80};

    public static final int BATTERY_VALUE_LEN = 2;
    public static final int BATTERY_LOWER_IDX = 0;
    public static final int BATTERY_UPPER_IDX = 1;
}
