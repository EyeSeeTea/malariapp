package org.eyeseetea.malariacare.utils;

import java.math.BigDecimal;

/**
 * Created by Jose on 22/02/2015.
 */
public class Utils {

    static final int numberOfDecimals = 2; // Number of decimals outputs will have

    public static String round(float base, int decimalPlace){
        BigDecimal bd = new BigDecimal(Float.toString(base));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_DOWN);
        return Float.toString(bd.floatValue());
    }

    public static String round(float base){
        return round(base, Utils.numberOfDecimals);
    }

}
