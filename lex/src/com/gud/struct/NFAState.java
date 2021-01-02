package com.gud.struct;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created By Gud on 2020/12/29 2:40 下午
 */
public class NFAState {
    private int id;

    /*选择2^8是因为string[i]类型为uint8
    256 match
    257 spilt
    <256 转移字符对应的int值


    ε 949
    ø 248
    */
    private int c;
    //实边
    private NFAState out1;
    private NFAState out2;

    public NFAState(NFA nfa) {
        this.c = 256;
        nfa.storeNfaState(this);
    }

    public NFAState(NFA nfa, int c, NFAState out1) {
        if (c < 256) {
            this.c = c;
            this.out1 = out1;
            nfa.storeNfaState(this);
        }
    }

    public NFAState(NFA nfa, NFAState out1, NFAState out2) {
        if (out1 != null && out1 != null) {
            this.c = 257;
            this.out1 = out1;
            this.out2 = out2;
            nfa.storeNfaState(this);
        }
    }

    @Override
    public String toString() {
        String s = "NFAState{" +
                "id=" + id +
                ", c=" + c;
        if (out1 != null) {
            s += ", out1=" + out1.getId();
        }
        if (out2 != null) {
            s += ", out2=" + out2.getId();
        }
        return s + '}';
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public NFAState getOut1() {
        return out1;
    }

    public void setOut1(NFAState out1) {
        c = 248;
        this.out1 = out1;
    }

    public boolean setOut1(int c, NFAState out1) {
        if (c < 256) {
            this.c = c;
            this.out1 = out1;
            return true;
        }
        return false;
    }

    public NFAState getOut2() {
        return out2;
    }

    public void setOut2(NFAState out2) {
        this.c=257;
        this.out2 = out2;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOut(NFAState out1, NFAState out2) {
        if (out1 != null && out1 != null) {
            this.c = 257;
            this.out1 = out1;
            this.out2 = out2;
        }
    }
}
