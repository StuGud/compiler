package com.gud;

import com.gud.struct.DFA;
import com.gud.struct.DFAState;
import com.gud.struct.NFA;
import com.gud.struct.NFAState;

import java.util.*;

/**
 * Created By Gud
 */
public class DFABuilder {

    private int nfaSize;
    private NFA nfa;

    public DFABuilder(NFA nfa) {
        this.nfaSize = nfa.getSize();
        this.nfa = nfa;
    }

    /**
     * 求一个nfa状态的闭包，包含自身
     *
     * @param nfaState
     * @return
     */
    private Set<NFAState> searchClosure(NFAState nfaState) {

        Set<NFAState> closure = new HashSet<>();
        boolean[] flag = new boolean[nfaSize];
        Queue<NFAState> nfaStateQueue = new LinkedList<>();
        nfaStateQueue.add(nfaState);

        while (!nfaStateQueue.isEmpty()) {
            NFAState curState = nfaStateQueue.poll();
            closure.add(curState);
            if (curState.getC() > 256) {
                if (curState.getOut1() != null) {
                    if (!flag[curState.getOut1().getId()]) {
                        nfaStateQueue.add(curState.getOut1());
                        flag[curState.getOut1().getId()] = true;
                    }
                }
                if (curState.getOut2() != null) {
                    if (!flag[curState.getOut2().getId()]) {
                        nfaStateQueue.add(curState.getOut2());
                        flag[curState.getOut2().getId()] = true;
                    }
                }
            }
        }

        return closure;
    }

    //map 通过key(c)到达的所有Nfa状态的集合,即DFAState的集合;	c StateId NfaState

    private Map<Integer, Set<NFAState>> getOuts(DFAState dfaState) {
        Map<Integer, Set<NFAState>> outsMap = new HashMap<>();
        for (NFAState nfaState : dfaState.getNFAStateSet()) {
            if (nfaState.getC() < 256) {

                assert nfaState.getOut1() != null;
                NFAState out = nfaState.getOut1();
                Set<NFAState> states = searchClosure(out);

                if (outsMap.containsKey(nfaState.getC())) {
                    outsMap.get(nfaState.getC()).addAll(states);
                } else {
                    outsMap.put(nfaState.getC(), states);
                }


            }
        }
        return outsMap;

    }


    public DFA buildDFA() {

        DFA dfa = new DFA();

        //类似表格法
        //状态转换表
        Map<DFAState, Map<Integer, DFAState>> transTable;

        //1.构造初试状态
        NFAState nfaStartState = nfa.getStartState();
        Set<NFAState> S = searchClosure(nfaStartState);
        DFAState dfaStartState = new DFAState(S);
        dfa.storeState(dfaStartState);

        Queue<DFAState> queue = new LinkedList<>();
        queue.add(dfaStartState);

        while (!queue.isEmpty()) {
            DFAState curState = queue.poll();
            Map<Integer, Set<NFAState>> outs = getOuts(curState);
            for (Map.Entry<Integer, Set<NFAState>> entry : outs.entrySet()) {
                Set<NFAState> nfaStateSet = entry.getValue();
                DFAState newDFAState = createDFAState(dfa, queue, nfaStateSet);
                curState.getEdge2StateMap().put(entry.getKey(), newDFAState);
            }
        }

        //EndFunc
        Map<Integer, DFAState> dfaStateMap = dfa.getStateMap();
        Map<NFAState, String> endStateMap = nfa.getEndStateMap();
        for (Map.Entry<Integer, DFAState> entry : dfaStateMap.entrySet()) {
            for (NFAState nfaState : entry.getValue().getNFAStateSet()) {
                if (endStateMap.containsKey(nfaState)) {
                    dfa.addEndFunc(entry.getValue(), endStateMap.get(nfaState));
                }
            }
        }


        return dfa;
    }

    /**
     * 如果已存在，返回已存在；否则新建
     *
     * @param dfa
     * @param nfaStateSet
     * @return
     */
    private DFAState createDFAState(DFA dfa, Queue<DFAState> queue, Set<NFAState> nfaStateSet) {
        //判断这个集合是否已经创建过DFAState
        for (Map.Entry<Integer, DFAState> entry : dfa.getStateMap().entrySet()) {
            DFAState dfaState = entry.getValue();
            if (deepEqualSet(dfaState.getNFAStateSet(), nfaStateSet)) {
                return dfaState;
            }
        }
        DFAState dfaState = new DFAState(nfaStateSet);
        dfa.storeState(dfaState);
        queue.add(dfaState);
        return dfaState;
    }

    private boolean deepEqualSet(Set<?> set1, Set<?> set2) {
        if (set1 == null || set2 == null) {//null就直接不比了
            return false;
        }
        if (set1.size() != set2.size()) {//大小不同也不用比了
            return false;
        }
        return set1.containsAll(set2);//最后比containsAll
    }
}
