package com.gud.struct;

import java.util.Map;
import java.util.Set;

/**
 * Created By Gud on 2020/12/29 2:41 下午
 */
public class DFAState {
    private int number=0;
    private Set<Integer> identitySet;
    private Map<Character,Integer> edge2StateMap;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Set<Integer> getIdentitySet() {
        return identitySet;
    }

    public Map<Character, Integer> getEdge2StateMap() {
        return edge2StateMap;
    }
}
