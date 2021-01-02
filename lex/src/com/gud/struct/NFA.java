package com.gud.struct;

import java.util.HashMap;
import java.util.Map;

/**
 * Created By Gud on 2020/12/29 2:41 下午
 */
public class NFA {
    private int startStateId =0;

    public void setStartStateId(int startStateId) {
        this.startStateId = startStateId;
    }

    private int size;
    //Map<Integer,Rules> endStateMap;

    /**
     * Integer 为128,表示空边
     */
    private Map<NFAState,String> endStateMap=new HashMap<>();
    private Map<Integer,NFAState> nfaStateMap=new HashMap<>();

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<NFAState, String> getEndStateMap() {
        return endStateMap;
    }

    public Map<Integer, NFAState> getNfaStateMap() {
        return nfaStateMap;
    }

    public NFAState getStartState(){
        if(size>=1){
            return nfaStateMap.get(startStateId);
        }else{
            NFAState startState=new NFAState(this);
            storeNfaState(startState);
            startStateId=startState.getId();
            return startState;
        }
    }

    public void storeNfaState(NFAState nfaState){
        size++;
        nfaState.setId(size);
        nfaStateMap.put(size,nfaState);
    }

    public void addEndFunc(NFAState endState,String endFunc){
        endStateMap.put(endState,endFunc);
    }


}
