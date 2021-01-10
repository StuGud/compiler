package com.gud;

import com.gud.struct.*;

import java.io.IOException;
import java.util.*;

/**
 * Created By Gud on 2021/1/7 4:03 下午
 */
public class Test {
    public static void main(String[] args) throws IOException {
        //new YaccFileParser().parseYaccFile("yacc/src/com/gud/c99.y");
        List<TableItem> temp = new ArrayList<>(100);
        System.out.println("hello");
    }


    //所有符号
    List<String> symbols = new ArrayList<>();

    //终结符部分
    Map<String, Integer> terminalMap = new HashMap<>();
    //@todo meaning?
    Map<Integer, Boolean> leftAssociativeMap = new HashMap<>();
    Map<Integer, Integer> priorityMap = new HashMap<>();

    //非终结符部分
    Map<String, Integer> nonTerminalMap = new HashMap<>();
    Map<Integer, Boolean> isNonTerminalNullableMap = new HashMap<>();

    //产生式以及对应的action语法制导翻译
    List<ProductionItem> productionItemDeque = new ArrayList<>();
    Map<Integer, String> productionActionMap = new HashMap<>();

    int symbolIndex = 0;

    //LR1部分  ======================================

    private final int EPSILON = -1;

    //first集
    Map<Integer, Set<Integer>> firstMap;

    List<TransitionItem> lr1StateTransitionList;
    List<List<TableItem>> lr1ParseTable;

//
//    private LR1State findClosure(LR1State lr1State) {
//        for (LR1Item lr1Item : lr1State.getItemList()) {
//
//            List<Integer> productionBody = lr1Item.getProd().getBody();
//            if (lr1Item.getDotLoc() == productionBody.size()) {
//                //点在最后面
//                continue;
//            }
//            for (ProductionItem production : productionItemDeque) {
//                //A -> B·CXXX ;则加入C为开头的产生式
//                if (production.getHead() == productionBody.get(lr1Item.getDotLoc())) {
//
//
//                    //A -> B·C ;C后面没有其他符号，则C -> X 的预测符和A -> B相同
//                    //A -> B·Cc ;C -> X的预测符为FIRST
//                    int cur = lr1Item.getDotLoc() + 1;
//                    if (cur == productionBody.size()) {
//                        lr1State.addItem(new LR1Item(production, lr1Item.getPredSet()));
//                    } else {
//                        Integer nextSymbol = productionBody.get(cur);
//                        if (terminalMap.containsKey(nextSymbol)) {
//                            //说明nextSymbol是终结符
//                            Set<Integer> set = new HashSet<>();
//                            set.add(nextSymbol);
//                            lr1State.addItem(new LR1Item(production, set));
//                        } else if (isNonTerminalNullableMap.get(nextSymbol)) {
//                            //A -> a·BCDE... ,CDE可能为null
//
//                            //求C的first集
//                            Set<Integer> firstOfC = findFirst(nextSymbol);
//                            firstOfC.remove(EPSILON);
//                            firstOfC.addAll(lr1Item.getPredSet());
//
//                            //把first(DE...)加入First(C)
//                            cur++;
//                            while (cur < productionBody.size()) {
//                                nextSymbol = productionBody.get(cur);
//                                if (terminalMap.containsKey(nextSymbol)) {
//                                    Set<Integer> set = new HashSet<>();
//                                    set.add(nextSymbol);
//                                    lr1State.addItem(new LR1Item(production, set));
//                                } else if (isNonTerminalNullableMap.get(nextSymbol)) {
//                                    Set<Integer> firstOfD = findFirst(nextSymbol);
//                                    firstOfD.remove(EPSILON);
//                                    firstOfC.addAll(firstOfD);
//                                    cur++;
//                                } else {
//                                    Set<Integer> firstOfD = findFirst(nextSymbol);
//                                    firstOfC.addAll(firstOfD);
//                                    break;
//                                }
//                            }
//                            lr1State.addItem(new LR1Item(production, firstOfC));
//                        } else {
//                            lr1State.addItem(new LR1Item(production, findFirst(nextSymbol)));
//                        }
//                    }
//                }
//            }
//        }
//        return lr1State;
//    }
}
