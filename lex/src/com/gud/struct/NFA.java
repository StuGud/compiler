package com.gud.struct;

import java.util.Map;

/**
 * Created By Gud on 2020/12/29 2:41 下午
 */
public class NFA {
    int startState=0;
    Map<Integer,Rules> endStateMap;
    Map<Integer,NFAState> nfaStateMap;
}
