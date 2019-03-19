package com.yimi.netutil;

import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

/**
 * refs:
 * <P>http://androidxref.com/9.0.0_r3/xref/frameworks/base/telephony/java/android/telephony/TelephonyManager.java
 */
public class NetInfoUtils {

    /**
     * Current network is LTE_CA
     */
    public static final int NETWORK_TYPE_LTE_CA = 19;

    /**
     * Network Class Definitions.
     * Do not change this order, it is used for sorting during emergency calling in
     * {@link TelephonyConnectionService#getFirstPhoneForEmergencyCall()}. Any newer technologies
     * should be added after the current definitions.
     */
    /**
     * Unknown network class.
     */
    public static final String NETWORK_CLASS_UNKNOWN = "UNKNOWN";
    /**
     * Class of broadly defined "2G" networks.
     */
    public static final String NETWORK_CLASS_2_G = "2G";
    /**
     * Class of broadly defined "3G" networks.
     */
    public static final String NETWORK_CLASS_3_G = "3G";
    /**
     * Class of broadly defined "4G" networks.
     */
    public static final String NETWORK_CLASS_4_G = "4G";

    /**
     * Return general class of network type, such as "3G" or "4G". In cases
     * where classification is contentious, this method is conservative.
     *
     * @param subType subType of network info
     */
    public static String getNetworkClass(int subType) {
        switch (subType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
            case NETWORK_TYPE_LTE_CA:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    /**
     * generate network msgs like:
     * <pre>
     * [network change] type:MOBILE(0) subType:EDGE(2) class:2G extra:cmnet
     * [network change] type:MOBILE(0) subType:LTE(13) class:4G operator:cmnet
     * [network change] type:WIFI(1) subType:(0) class:UNKNOWN extra:"wifi-name"
     * [network change] disconnect
     * </pre>
     */
    public static String getNetInfoMsg(@Nullable NetworkInfo netInfo) {
        StringBuilder sb = new StringBuilder(" [network change]");
        if (netInfo != null && netInfo.isConnected()) {
            int type = netInfo.getType();
            int subtype = netInfo.getSubtype();
            sb.append(" type:")
                    .append(netInfo.getTypeName())
                    .append("(").append(type).append(")");
            sb.append(" subType:")
                    .append(netInfo.getSubtypeName())
                    .append("(").append(subtype).append(")");
            sb.append(" class:")
                    .append(NetInfoUtils.getNetworkClass(subtype));
            sb.append(" extra:").append(netInfo.getExtraInfo());
        } else {
            sb.append(" disconnect");
        }
        return sb.toString();
    }

}
