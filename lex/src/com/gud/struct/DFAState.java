package com.gud.struct;

import java.util.Map;
import java.util.Set;

/**
 * Created By Gud on 2020/12/29 2:41 下午
 */
public class DFAState {
    private int id=0;
    private Set<NFAState> nfaStateSet;
    private Map<Integer,DFAState> edge2StateMap;

    public DFAState(Set<NFAState> nfaStateSet) {
        this.nfaStateSet = nfaStateSet;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<NFAState> getNFAStateSet() {
        return nfaStateSet;
    }

    public void setNFAStateSet(Set<NFAState> nfaSet) {
        this.nfaStateSet = nfaSet;
    }

    public Map<Integer, DFAState> getEdge2StateMap() {
        return edge2StateMap;
    }

    public void setEdge2StateMap(Map<Integer, DFAState> edge2StateMap) {
        this.edge2StateMap = edge2StateMap;
    }
}
