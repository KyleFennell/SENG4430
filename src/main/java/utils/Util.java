package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

    public static <K, V> List<K> getKeysFromValue(V value, Map<K, V> map){
        List<K> keys = new ArrayList<>();
        for (K s : map.keySet()){
            if (map.get(s) == value){
                keys.add(s);
            }
        }
        return keys;
    }

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