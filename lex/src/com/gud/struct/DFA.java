package com.gud.struct;

import java.util.*;

/**
 * Created By Gud
 */
public class DFA {
    int size = 0;

    int startStateId = 0;
    //存储标号对应状态
    Map<Integer, DFAState> stateMap = new HashMap<>();
    Map<DFAState, Set<String>> endFuncMap = new HashMap<>();

    public void storeState(DFAState state) {
        //System.out.println("构建第"+size+"dfa状态");
        state.setId(size);
        stateMap.put(state.getId(), state);
        size++;
    }

    public int getSize() {
        return size;
    }

    public Map<DFAState, Set<String>> getEndFuncMap() {
        return endFuncMap;
    }

    public int getStartStateId() {
        return startStateId;
    }

    public Map<Integer, DFAState> getStateMap() {
        return stateMap;
    }

    public void addEndFunc(DFAState dfaState, String endFunc) {
        if (endFuncMap.containsKey(dfaState)) {
            endFuncMap.get(dfaState).add(endFunc);
        } else {
            Set<String> set = new HashSet<>();
            set.add(endFunc);
            endFuncMap.put(dfaState, set);
        }
    }
}
