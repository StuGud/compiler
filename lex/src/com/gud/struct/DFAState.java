package com.gud.struct;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created By Gud
 */
public class DFAState {

    private int id = -1;
    private Set<NFAState> nfaStateSet = new HashSet<>();
    private Map<Integer, DFAState> edge2StateMap = new HashMap<>();

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
