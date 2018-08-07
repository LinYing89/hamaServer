package com.bairock.iot.hamaServer.data;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Untils {
    /**
     * 验证邮箱
     * @param email 邮箱
     * @return 是否是邮箱
     */
    public static boolean isEmail(String email) {
        boolean flag;
        try {
            String check = "^([a-z0-9A-Z]+[-|_.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 是否是手机号码
     * @param mobileNumber 手机号码
     * @return true如果是手机号码
     */
    public static boolean isMobileNumber(String mobileNumber) {
        boolean flag;
        try {
            Pattern regex = Pattern.compile("^1[0-9]\\d{9}$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;

        }
        return flag;
    }

    public static byte[] createByteMsg(byte[] b1) {
        if(b1 == null) {
            return null;
        }
        byte[] byVerify = getVerify(b1);
        return unitArray(b1, byVerify);
    }
    public static byte[] unitArray(byte[] b1, byte[] b2) {
        byte[] byMsg = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, byMsg, 0, b1.length);
        System.arraycopy(b2, 0, byMsg, b1.length, b2.length);
        return byMsg;
    }
    public static byte[] getVerify(byte[] by) {
        byte[] bysum = new byte[2];
        int chksum = 0;
        for (int i = 0; i < by.length; i++) {
            chksum += by[i];
        }
        bysum[1] = (byte) (chksum & 0xFF);
        bysum[0] = (byte) (chksum >> 8 & 0xFF);
        return bysum;
    }

    /**
     * accurate to the second decimal place
     * @param f 没转换之前的值
     * @return 转换后的保留2位小数的值
     */
    public static float scale(float f) {
        BigDecimal b = new BigDecimal(f);
        return b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }
}
