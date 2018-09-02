package com.paiai.mble.util;

import android.text.TextUtils;

/**
 * 作者：dccjll<br>
 * 创建时间：2018/2/8 08 56 星期四<br>
 * 功能描述：mac地址工具类<br>
 */
public class MacAddressUtils {
    /**
     * 验证设备mac地址
     * @param address   设备mac地址
     * @return  验证结果
     */
    public static boolean checkAddress(String address){
        if(TextUtils.isEmpty(address)){
            return false;
        }
        if(address.split(":").length != 6){
            return false;
        }
        char[] macChars = address.replace(":", "").toCharArray();
        String regexChars = "0123456789ABCDEFabcdef";
        for(char c : macChars){
            if(!regexChars.contains(c + "")){
                return false;
            }
        }
        return true;
    }
}
