package com.mindbar.cycloid.helpers;

import com.mindbar.cycloid.pojo.CyclopusStatus;

/**
 * Created with IntelliJ IDEA.
 * User: Andrey Voloshin
 * Date: 10/06/13
 * Time: 14:54
 */
public class CyclopusMsg {

    public static CyclopusStatus parseMessage(String input) {
        try {
            // format message from Arduino firmware: intCadence,floatSpeed,longOdometer,longDistance

            String[] values = input.split(",");
            CyclopusStatus cs = new CyclopusStatus(Integer.parseInt(values[0]), Float.parseFloat(values[1]), Long.parseLong(values[2]), Long.parseLong(values[3]));
            return cs;
        } catch (Exception e) {
            return null;
        }
    }
}
