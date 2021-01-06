package com.gud.struct;

import java.util.HashMap;
import java.util.Map;

/**
 * Created By Gud on 2021/1/6 9:22 下午
 *
 * LR1集族（加上转换map）构成goto graph
 */
public class TransitionItem {

    int id;
    LR1State lr1State;
    //key:边 value:移入的状态
    Map<Integer,Integer> transMap;

    public void addEdge(int edge,int next){
        transMap.put(edge,next);
    }

    public boolean equals(TransitionItem transitionItem){
        return this.lr1State.equals(transitionItem.lr1State)&&deepEqualsMap(this.transMap,transitionItem.transMap);
    }

    /**
     * 貌似只能比较基本数据类型的map
     * @param map1
     * @param map2
     * @return
     */
    public boolean deepEqualsMap(Map<?,?> map1,Map<?,?> map2){
        if (map1 == null || map2 == null) {
            return false;
        }
        if (map1.size() != map2.size()) {
            return false;
        }
        return map1.equals(map2);
    }
}
