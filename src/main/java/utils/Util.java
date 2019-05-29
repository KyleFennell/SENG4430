package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project          : Software Quality Assignment
 * Class name       : Util
 * Author(s)        : Kyle Fennell
 * Purpose          : collection of static methods used by fanin and fanout
 */

public class Util {

    /**
     * @param value the value being searched for
     * @param map the map to search
     * @param <K> type of the key of the map
     * @param <V> type of the value of the map
     * @return a list<K> that contains all the keys that
     *      had a value matching the 'value' param.
     */
    public static <K, V> List<K> getKeysFromValue(V value, Map<K, V> map){
        List<K> keys = new ArrayList<>();
        for (K s : map.keySet()){
            if (map.get(s) == value){
                keys.add(s);
            }
        }
        return keys;
    }

    /**
     * @param map the mep to calculate statistics from
     * @return strings for the max, min, mean, and mode values of the map
     *  and the keys that share those values
     */
    public static String[] calculateBasicMetrics(Map<String, Integer> map){
        int max = 0;
        int total = 0;
        int min = Integer.MAX_VALUE;

        for (String m : map.keySet()){
            int val = map.get(m);
            min = val < min ? val : min;
            max = val > max ? val : max;
            total += val;
        }
        int average = total/map.size();
        int mode = calculateMode(map);
        return new String[] {
                "Max: " + max + " (" + Util.getKeysFromValue(max, map) + ")",
                "Min: " + min + " (" + Util.getKeysFromValue(min, map) + ")",
                "Mean: " + average + " (" + Util.getKeysFromValue(average, map) + ")",
                "Mode: " + mode + " (" + Util.getKeysFromValue(mode, map) + ")"
        };
    }

    /**
     * @param map the map to calculate the mode for
     * @return the mode value from the map.
     */
    public static int calculateMode(Map<String, Integer> map){
        Map<Integer, Integer> modeHelper = new HashMap<>();

        //calculate the frequency of each frequency
        for (String m : map.keySet()){
            int val = map.get(m);
            if (!modeHelper.containsKey(val)){
                modeHelper.put(val, 0);
            }
            modeHelper.put(val, modeHelper.get(val) + 1);
        }

        int max = 0;
        for (Integer i : modeHelper.keySet()){
            max = modeHelper.get(i) > max ? modeHelper.get(i) : max;
        }
        for (Integer i : modeHelper.keySet()){
            if (modeHelper.get(i) == max){
                return i;
            }
        }
        return -1;
    }
}