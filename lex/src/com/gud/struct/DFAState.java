package com.gud.struct;

import java.util.Map;
import java.util.Set;

/**
 * Created By Gud on 2020/12/29 2:41 下午
 */
public class DFAState {
    int number=0;
    Set<Integer> identitySet;
    Map<Character,Integer> edge2StateMap;
}
