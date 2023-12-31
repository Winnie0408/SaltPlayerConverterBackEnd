package com.hwinzniej.saltplayerconverter.backend.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: 对Map类型的数据进行排序
 * @author: HWinZnieJ
 * @create: 2023-09-04 19:39
 **/

public class MapSort {
    /**
     * 对Map类型的数据根据值(Value)进行排序
     *
     * @param map   要计算的Map集合
     * @param order A:升序 D:降序
     * @return 排序完成后的List
     */
    public static List<Map.Entry<String, Double>> sortByValue(Map<String, Double> map, char order) {
        List<Map.Entry<String, Double>> entryList2 = new ArrayList<>(map.entrySet());
        entryList2.sort((me1, me2) -> {
            if (order == 'A') {
                return me1.getValue().compareTo(me2.getValue()); // 升序排序
            } else {
                return me2.getValue().compareTo(me1.getValue()); // 降序排序
            }
        });
        return entryList2;
    }

    /**
     * 对Map类型的数据根据键(Key)进行排序
     *
     * @param map   要计算的Map集合
     * @param order A:升序 D:降序
     * @return 排序完成后的List
     */
    public static List<Map.Entry<Integer, Integer>> sortByKey(Map<Integer, Integer> map, char order) {
        List<Map.Entry<Integer, Integer>> entryList1 = new ArrayList<>(map.entrySet());
        entryList1.sort((me1, me2) -> {
            if (order == 'A') {
                return me1.getKey().compareTo(me2.getKey()); // 升序排序
            } else {
                return me2.getKey().compareTo(me1.getKey()); // 降序排序
            }
        });
        return entryList1;
    }

    /**
     * 获取Map中Value最大的键值对
     *
     * @param map 要计算的Map集合
     * @return Map.Entry<String, Double> 集合中最大值(Value)的键值对
     */
    public static Map.Entry<String, Double> getMaxValue(Map<String, Double> map) {
        Map.Entry<String, Double> maxEntry = null;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        return maxEntry;
    }

    /**
     * 获取Map中Value最小的键值对
     *
     * @param map 要计算的Map集合
     * @return Map.Entry<String, Double> 集合中最小值(Value)的键值对
     */
    public static Map.Entry<String, Double> getMinValue(Map<String, Double> map) {
        Map.Entry<String, Double> minEntry = null;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                minEntry = entry;
            }
        }
        return minEntry;
    }
}
