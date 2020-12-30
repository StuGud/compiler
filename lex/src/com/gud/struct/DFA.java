package com.gud.struct;

import java.util.List;
import java.util.Map;

/**
 * Created By Gud on 2020/12/29 2:41 下午
 */
public class DFA {
    Map<Integer,Rules> endStateMap;
    int startState=0;
    //存储标号对应状态
    Map<Integer,DFAState>  stateMap;
}
