package com.gud.struct;

import java.util.HashMap;
import java.util.Map;

/**
 * Created By Gud
 */
public class NFA {

    private int startStateId = 0;

    private int size = 0;
    //Map<Integer,Rules> endStateMap;

    private Map<NFAState, String> endStateMap = new HashMap<>();
    private Map<Integer, NFAState> nfaStateMap = new HashMap<>();

    public int getSize() {
        return size;
    }

    public void setStartStateId(int startStateId) {
        this.startStateId = startStateId;
    }

    public Map<NFAState, String> getEndStateMap() {
        return endStateMap;
    }

    public Map<Integer, NFAState> getNfaStateMap() {
        return nfaStateMap;
    }

    public NFAState getStartState() {
        if (size >= 1) {
            return nfaStateMap.get(startStateId);
        } else {
            NFAState startState = new NFAState(this);
            storeNfaState(startState);
            startStateId = startState.getId();
            return startState;
        }
    }

    public void storeNfaState(NFAState nfaState) {
        nfaState.setId(size);
        nfaStateMap.put(size, nfaState);
        size++;
    }

    public void addEndFunc(NFAState endState, String endFunc) {
        endStateMap.put(endState, endFunc);
    }
}
