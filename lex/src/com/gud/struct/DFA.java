package com.gud.struct;

import java.util.*;

/**
 * Created By Gud on 2020/12/29 2:41 下午
 */
public class DFA {
    int size;

    int startStateId=0;
    //存储标号对应状态
    Map<Integer,DFAState>  stateMap=new HashMap<>();
    Map<DFAState, Set<String>> endFuncMap=new HashMap<>();


    public void storeState(DFAState state){
        size++;
        state.setId(size);
        stateMap.put(state.getId(),state);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<DFAState, Set<String>> getEndFuncMap() {
        return endFuncMap;
    }

    public void setEndFuncMap(Map<DFAState, Set<String>> endFuncMap) {
        this.endFuncMap = endFuncMap;
    }

    public int getStartStateId() {
        return startStateId;
    }

    public void setStartStateId(int startStateId) {
        this.startStateId = startStateId;
    }

    public Map<Integer, DFAState> getStateMap() {
        return stateMap;
    }

    public void setStateMap(Map<Integer, DFAState> stateMap) {
        this.stateMap = stateMap;
    }

    public void addEndFunc(DFAState dfaState, String endFunc) {
        if(endFuncMap.containsKey(dfaState)){
            endFuncMap.get(dfaState).add(endFunc);
        }else{
            Set<String> set =new HashSet<>();
            set.add(endFunc);
            endFuncMap.put(dfaState,set);
        }
    }
}
