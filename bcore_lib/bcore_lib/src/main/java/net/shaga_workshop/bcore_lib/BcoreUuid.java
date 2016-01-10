package net.shaga_workshop.bcore_lib;

import java.util.UUID;

/**
 * bCore UUID
 */
public class BcoreUuid {
    public static final String UUID_STR_BCORE_SERVICE = "389CAAF0-843F-4D3B-959D-C954CCE14655";
    public static final String UUID_STR_BCORE_BATTERY_VOLTAGE = "389CAAF1-843F-4D3B-959D-C954CCE14655";
    public static final String UUID_STR_BCORE_MOTOR_PWM = "389CAAF2-843F-4D3B-959D-C954CCE14655";
    public static final String UUID_STR_BCORE_PORT_OUT = "389CAAF3-843F-4D3B-959D-C954CCE14655";
    public static final String UUID_STR_BCORE_SERVO_POS = "389CAAF4-843F-4D3B-959D-C954CCE14655";
    public static final String UUID_STR_BCORE_FUNCTIONS = "389CAAFF-843F-4D3B-959D-C954CCE14655";

    public static final UUID UUID_BCORE_SERVICE = UUID.fromString(UUID_STR_BCORE_SERVICE);
    public static final UUID UUID_BCORE_BATTERY_VOLTAGE = UUID.fromString(UUID_STR_BCORE_BATTERY_VOLTAGE);
    public static final UUID UUID_BCORE_MOTOR_PWM = UUID.fromString(UUID_STR_BCORE_MOTOR_PWM);
    public static final UUID UUID_BCORE_PORT_OUT = UUID.fromString(UUID_STR_BCORE_PORT_OUT);
    public static final UUID UUID_BCORE_SERVO_POS = UUID.fromString(UUID_STR_BCORE_SERVO_POS);
    public static final UUID UUID_BCORE_FUNCTIONS = UUID.fromString(UUID_STR_BCORE_FUNCTIONS);
}
