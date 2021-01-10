package com.gud;

import com.gud.struct.*;

import java.util.*;

/**
 * Created By Gud on 2021/1/7 7:39 下午
 */
public class LR1Builder {

    //所有符号
    List<String> symbols = new ArrayList<>();

    //终结符部分
    Map<String, Integer> terminalMap = new HashMap<>();

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
    Map<Integer, Set<Integer>> firstMap=new HashMap<>();

    List<TransitionItem> lr1StateTransitionList=new ArrayList<>();
    List<List<TableItem>> lr1ParseTable=new ArrayList<>();

    public LR1Builder(YaccFileParser fileParser) {
        terminalMap = fileParser.terminalMap;
        leftAssociativeMap = fileParser.leftAssociativeMap;
        priorityMap = fileParser.priorityMap;
        nonTerminalMap = fileParser.nonTerminalMap;
        isNonTerminalNullableMap = fileParser.isNonTerminalNullableMap;
        productionItemDeque= fileParser.productionItemDeque;;
        productionActionMap= fileParser.productionActionMap;
        symbolIndex= fileParser.symbolIndex;

        symbols= fileParser.symbols;
    }

    //记录非终结符是否产生空串
    private void findEPSILON() {
        boolean flag = true;
        while (flag) {
            flag = false;
            for (Map.Entry<Integer, Boolean> entry : isNonTerminalNullableMap.entrySet()) {
                if (entry.getValue()) {
                    continue;
                } else {
                    for (ProductionItem production : productionItemDeque) {
                        if (production.getHead() == entry.getKey()) {
                            //X -> ABC,ABC都为null,X可为null
                            int i = 0;
                            for (int z : production.getBody()) {
                                if (!isNonTerminalNullableMap.containsKey(z)){
                                    //终结符
                                    break;
                                }
                                if (isNonTerminalNullableMap.get(z)) {
                                    i++;
                                }else{
                                    break;
                                }
                            }
                            if (i == production.getBody().size()) {
                                isNonTerminalNullableMap.put(entry.getKey(), true);
                                flag = true;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param x
     * @return
     * @todo todo
     * 求First(X)
     */
    private Set<Integer> findFirst(Integer x) {
        //如果已经求过
        if (firstMap.containsKey(x)) {
            return firstMap.get(x);
        }

        //求first
        Set<Integer> firstSet = new HashSet<>();
        if (!isNonTerminalNullableMap.containsKey(x)) {
            //x为终结符，first集是他本身
            firstSet.add(x);
        } else {


            //==========
            if (isNonTerminalNullableMap.get(x)) {
                firstSet.add(EPSILON);
            }

            for (ProductionItem production : productionItemDeque) {
                List<Integer> productionBody = production.getBody();
                if (production.getHead() == x) {
                    //X -> empty 或者 X -> X...
                    if (productionBody.isEmpty() || productionBody.get(0) == x) {
                        continue;
                    }
                    //X -> AYB
                    for (Integer symbol : productionBody) {
                        if (!isNonTerminalNullableMap.containsKey(symbol)) {
                            //终结符
                            firstSet.add(symbol);
                            break;
                        } else if (!isNonTerminalNullableMap.get(symbol)) {
                            //symbol不会推导出空串
                            if(symbol==x){
                                break;
                            }
                            firstSet.addAll(findFirst(symbol));
                            break;
                        } else {
                            //symbol会推导出空串
                            if(symbol==x){
                                continue;
                            }
                            //@todo 会不会导致死循环？ 比如X->AXB，A可以推导出空串
                            Set<Integer> firstOfSymbol = findFirst(symbol);
                            firstOfSymbol.remove(EPSILON);
                            firstSet.addAll(firstOfSymbol);
                        }
                    }
                }
            }
        }
        firstMap.put(x, firstSet);
        return firstSet;
    }

    private Set<Integer> findFirst(List<Integer> symbolString) {
        Set<Integer> firstSet = new HashSet<>();
        for (int i = 0; i < symbolString.size(); i++) {
            int curSymbol = symbolString.get(i);
            if (!isNonTerminalNullableMap.containsKey(curSymbol)) {
                //终结符
                firstSet.add(curSymbol);
                break;
            } else {
                firstSet.addAll(findFirst(curSymbol));
                if (!isNonTerminalNullableMap.containsKey(curSymbol)) {
                    //推导不出空串
                    break;
                }
            }
        }
        return firstSet;
    }


    /**
     * 闭合操作步骤（设集合名为 I）：
     *
     * （1） 遍历 I ，对 I 中的每一条黑点后是非终结符的形态 [ A -> u.Bv , a ] ，对 B 的每一个产生式 B -> w 、
     * 以及 First(va) 中的每一个符号 b ，将形态 [ B -> .w, b ] 添加进 I 。
     * （2） 重复（1），直到不再出现新的形态。
     *
     * @param lr1State
     * @return
     */

    //@todo 如果没有v
    private LR1State findClosure(LR1State lr1State) {
        List<LR1Item> lr1ItemList = lr1State.getItemList();
        for (int i = 0; i < lr1ItemList.size(); i++) {
            LR1Item lr1Item = lr1ItemList.get(i);
            List<Integer> productionBody = lr1Item.getProd().getBody();
            List<Integer> nextSymbols = new ArrayList<>();


            if (lr1Item.isDotAtEnd() ) {
                //点在最后面 或者 点后面是终结符
                continue;
            }

            if(lr1Item.getDotLoc()+1<= productionBody.size()){
                nextSymbols= productionBody.subList(lr1Item.getDotLoc()+1, productionBody.size());
            }

            Set<Integer> predictSet=new HashSet<>();
            if( nextSymbols.isEmpty()){
                predictSet.addAll(lr1Item.getPredSet());
            }else{
                predictSet = findFirst(nextSymbols);
                if (predictSet.contains(EPSILON)) {
                    predictSet.addAll(lr1Item.getPredSet());
                    predictSet.remove(EPSILON);
                }
            }

            for (ProductionItem production : productionItemDeque) {
                //A -> B·CXXX ;则加入C为开头的产生式
                if (production.getHead() == productionBody.get(lr1Item.getDotLoc())) {

                    lr1State.addItem(new LR1Item(production, predictSet));
                }
            }
        }
        return lr1State;
    }


    private LR1State findGOTO(LR1State lr1State, int edge) {
        LR1State dest = new LR1State();
        for (LR1Item lr1Item : lr1State.getItemList()) {
            if (!lr1Item.isDotAtEnd() && lr1Item.getProd().getBody().get(lr1Item.getDotLoc()) == edge) {
                //shift 保留相同的预测符集
                LR1Item item = new LR1Item(lr1Item.getProd(), lr1Item.getPredSet());
                item.setDotLoc(lr1Item.getDotLoc() + 1);
                dest.addItem(item);
            }
        }
        if (!dest.getItemList().isEmpty()) {
            return findClosure(dest);
        } else {
            return dest;
        }
    }



    private ACTION_TYPE solveConflict(int shiftOP, int reduceOP) {
        if (!priorityMap.containsKey(shiftOP)) {
            return ACTION_TYPE.REDUCTION;
        } else if (!productionActionMap.containsKey(reduceOP)) {
            return ACTION_TYPE.SHIFT;
        } else {
            Integer sp = priorityMap.get(shiftOP);
            Integer rp = priorityMap.get(reduceOP);
            if (sp < rp) {
                return ACTION_TYPE.REDUCTION;
            } else if (sp > rp) {
                return ACTION_TYPE.SHIFT;
            } else {
                return leftAssociativeMap.get(shiftOP) ? ACTION_TYPE.REDUCTION : ACTION_TYPE.SHIFT;
            }
        }
    }

    /**
     * 初始化LR1下推自动机，先构造I0状态，并不断扩展得到最终的下推自动机
     */
    public void initTransition() {

        //记录非终结符是否产生空串
        findEPSILON();

        System.out.println("开始构造LR1下推自动机");

        LR1State startState = new LR1State();
        Set<Integer> predictSet = new HashSet<>();
        predictSet.add(terminalMap.get("$"));
        startState.addItem(new LR1Item(productionItemDeque.get(0), predictSet));
        lr1StateTransitionList.add(new TransitionItem(0, findClosure(startState)));

        //扩展状态边
        int size = 1;
        for (int i = 0; i < size; i++) {
            //System.out.println("size"+size);
            for (int edge = 0; edge < symbolIndex; edge++) {
                LR1State newState = findGOTO(lr1StateTransitionList.get(i).getLr1State(), edge);
                if (!newState.getItemList().isEmpty()) {
                    boolean newStateFlag = true;
                    for (int j = 0; j < lr1StateTransitionList.size(); j++) {
                        TransitionItem item= lr1StateTransitionList.get(j);
                        //@todo equals函数有问题
                        if (item.getLr1State().equals(newState)) {
                            //edge 26，j 7
                            lr1StateTransitionList.get(i).getTransMap().put(edge, item.getId());
                            newStateFlag = false;
                            break;
                        }
                    }
                    if (newStateFlag) {
                        lr1StateTransitionList.add(new TransitionItem(size, newState));
                        lr1StateTransitionList.get(i).getTransMap().put(edge, size);
                        size++;
                    }
                }
            }
        }

        System.out.println("LR1下推自动机构造完成");
    }

    public void initParseTable() {
        System.out.println("开始构造LR1分析表");

        for (int state = 0 ; state < lr1StateTransitionList.size(); state++) {
            List<TableItem> temp = Collections.nCopies(symbolIndex, new TableItem());

            //reduce
            for (LR1Item lr1Item : lr1StateTransitionList.get(state).getLr1State().getItemList()) {

                //·在最后，需要进行规约
                if (lr1Item.isDotAtEnd()) {
                    //填入预测符 的规约项
                    for (Integer pred : lr1Item.getPredSet()) {
                        TableItem curTableItem = temp.get(pred);
                        switch (curTableItem.getAction()) {
                            case ERROR: {
                                if (lr1Item.getProd().getIndex() == 0) {
                                    curTableItem = new TableItem(ACTION_TYPE.ACCEPT, lr1Item.getProd().getIndex());
                                } else {
                                    curTableItem = new TableItem(ACTION_TYPE.REDUCTION, lr1Item.getProd().getIndex());
                                }
                                break;
                            }
                            default: {
                                //规约-规约冲突
                                break;
                            }
                        }
                    }
                }
            }

            //shift
            for (Map.Entry<Integer, Integer> transEntry : lr1StateTransitionList.get(state).getTransMap().entrySet()) {
                TableItem curTableItem = temp.get(transEntry.getKey());
                switch (curTableItem.getAction()) {
                    case ERROR: {
                        if (!isNonTerminalNullableMap.containsKey(transEntry.getKey())) {
                            //终结符 @todo 是引用吗//
                            curTableItem = new TableItem(ACTION_TYPE.SHIFT, transEntry.getValue());
                        } else {
                            //非终结符
                            curTableItem = new TableItem(ACTION_TYPE.GOTO_STATE, transEntry.getValue());
                        }
                        break;
                    }
                    case REDUCTION: {
                        //产生 移入-规约冲突 利用附加条件消除冲突
                        //@todo 应该定义为-1
                        int reduceOP = -1;
                        //@todo something wrong
                        List<Integer> productionBody = productionItemDeque.get(curTableItem.getIndex()).getBody();
                        for (int i = productionBody.size() - 1; i >= 0; i--) {
                            if (!isNonTerminalNullableMap.containsKey(productionBody.get(i))) ;
                            {
                                reduceOP = productionBody.get(i);
                                break;
                            }
                        }
                        //根据冲突消除规则应保留SHIFT操作
                        if (reduceOP != -1 && solveConflict(transEntry.getKey(), reduceOP) == ACTION_TYPE.SHIFT) {
                            curTableItem = new TableItem(ACTION_TYPE.SHIFT, transEntry.getValue());
                        }
                        break;
                    }
                    case SHIFT: {
                        //不可能
                        break;
                    }
                    default: {
                        //不可能
                    }
                }
            }
            lr1ParseTable.add(temp);
        }

        System.out.println("LR1分析表构造完成");
    }


}

