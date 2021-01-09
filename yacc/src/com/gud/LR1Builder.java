package com.gud;

import com.gud.struct.*;

import java.util.*;

/**
 * Created By Gud on 2021/1/7 7:39 下午
 */
public class LR1Builder {
    YaccFileParser yaccFileParser;

    Map<String, Integer> nonTerminalMap = new HashMap<>();
    Map<String, Integer> terminalMap = new HashMap<>();
    Map<Integer, Boolean> isNonTerminalNullableMap = new HashMap<>();
    //@todo meaning?
    Map<Integer, Boolean> leftAssociativeMap = new HashMap<>();

    Map<Integer, Integer> priorityMap = new HashMap<>();

    Vector<String> words = new Vector<>();

    Map<Integer, String> productionActionMap = new HashMap<>();
    List<ProductionItem> productionItemDeque = new ArrayList<>();

    private final int EPSILON=-1;

    int indexSymbol = 0;

    //======================================

    //first集
    Map<Integer,Set<Integer>> firstMap;

    //
    List<TransitionItem> LR1StateTransitionList;
    List<List<TableItem>> lr1ParseTable;

    private void analyzeNullable() {
        boolean flag = true;
        while (flag) {
            flag = false;
            for (Map.Entry<Integer, Boolean> entry : yaccFileParser.isNonTerminalNullableMap.entrySet()) {
                if (entry.getValue()) {
                    continue;
                } else {
                    for (ProductionItem production : yaccFileParser.productionItemDeque) {
                        if (production.getHead() == entry.getKey()) {
                            //X -> ABC,ABC都为null,X可为null
                            int i = 0;
                            for (int z : production.getBody()) {
                                if (yaccFileParser.isNonTerminalNullableMap.get(z)) {
                                    i++;
                                }
                            }
                            if (i == production.getBody().size()) {
                                yaccFileParser.isNonTerminalNullableMap.put(entry.getKey(), true);
                                flag = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private LR1State findClosure(LR1State lr1State) {
        for (LR1Item lr1Item : lr1State.getItemList()) {

            List<Integer> productionBody = lr1Item.getProd().getBody();
            if (lr1Item.getDotLoc() == productionBody.size()) {
                //点在最后面
                continue;
            }
            for (ProductionItem production : productionItemDeque) {
                //A -> B·CXXX ;则加入C为开头的产生式
                if(production.getHead()==productionBody.get(lr1Item.getDotLoc())){

                    //@todo 理解
                    //A -> B·C ;C后面没有其他符号，则C -> X 的预测符和A -> B相同
                    //A -> B·Cc ;C -> X的预测符为FIRST
                    int cur=lr1Item.getDotLoc()+1;
                    if(cur == productionBody.size()){
                        lr1State.addItem(new LR1Item(production,lr1Item.getPredSet()));
                    }else{
                        Integer nextSymbol = productionBody.get(cur);
                        if (terminalMap.containsKey(nextSymbol)){
                            //说明nextSymbol是终结符
                            Set<Integer> set=new HashSet<>();
                            set.add(nextSymbol);
                            lr1State.addItem(new LR1Item(production,set));
                        }else if(isNonTerminalNullableMap.get(nextSymbol)){
                            //A -> a·BCDE... ,CDE可能为null

                            //求C的first集
                            Set<Integer> firstOfC=findFirst(nextSymbol);
                            firstOfC.remove(EPSILON);
                            firstOfC.addAll(lr1Item.getPredSet());

                            //把first(DE...)加入First(C)
                            cur++;
                            while(cur<productionBody.size()){
                                nextSymbol=productionBody.get(cur);
                                if(terminalMap.containsKey(nextSymbol)){
                                    Set<Integer> set=new HashSet<>();
                                    set.add(nextSymbol);
                                    lr1State.addItem(new LR1Item(production,set));
                                }else if(isNonTerminalNullableMap.get(nextSymbol)){
                                    Set<Integer> firstOfD=findFirst(nextSymbol);
                                    firstOfD.remove(EPSILON);
                                    firstOfC.addAll(firstOfD);
                                    cur++;
                                }else{
                                    Set<Integer> firstOfD=findFirst(nextSymbol);
                                    firstOfC.addAll(firstOfD);
                                    break;
                                }
                            }
                            lr1State.addItem(new LR1Item(production,firstOfC));
                        }else {
                            lr1State.addItem(new LR1Item(production,findFirst(nextSymbol)));
                        }
                    }
                }
            }
        }
        return lr1State;
    }

    private  LR1State findGOTO(LR1State lr1State,int edge){
        LR1State des=new LR1State();
        for (LR1Item lr1Item: lr1State.getItemList()){
            if(lr1Item.getDotLoc()!=lr1Item.getProd().getBodyLength()&&lr1Item.getProd().getBody().get(lr1Item.getDotLoc())==edge){
                //shift 保留相同的预测符集
                LR1Item item = new LR1Item(lr1Item.getProd(), lr1Item.getPredSet());
                item.setDotLoc(lr1Item.getDotLoc()+1);
                des.addItem(item);
            }
        }
        if(!des.getItemList().isEmpty()){
            return findClosure(des);
        }else{
            return des;
        }
    }

    /**
     * @todo todo
     * 求First(X)
     * @param x
     * @return
     */
    private Set<Integer> findFirst(Integer x){
        //如果已经求过
        if(firstMap.containsKey(x)){
            return firstMap.get(x);
        }

        //求first
        Set<Integer> firstSet=new HashSet<>();
        if(terminalMap.containsKey(x)){
            //x为终结符，first集是他本身
            firstSet.add(x);
        }else{
            if(isNonTerminalNullableMap.get(x)){
                firstSet.add(EPSILON);
            }

            for(ProductionItem production : productionItemDeque ){
                List<Integer> productionBody = production.getBody();
                if(production.getHead()==x){
                    //X -> empty 或者 X -> X...
                    if(productionBody.isEmpty()||productionBody.get(0)==x){
                        continue;
                    }
                    //X -> AYB
                    for(Integer symbol:productionBody){
                        if(terminalMap.containsKey(symbol)){
                            firstSet.add(symbol);
                            break;
                        }else if(!isNonTerminalNullableMap.get(symbol)){
                            //symbol不会推导出空串
                            //@todo 会不会导致死循环？
                            firstSet.addAll(findFirst(symbol));
                            break;
                        }else{
                            Set<Integer> firstOfSymbol = findFirst(symbol);
                            firstOfSymbol.remove(EPSILON);
                            firstOfSymbol.addAll(firstOfSymbol);
                        }
                    }

                }
            }

        }

        firstMap.put(x,firstSet);
        return firstSet;
    }

    private ACTION_TYPE solveConflict(int shiftOP,int reduceOP){
        if(!priorityMap.containsKey(shiftOP)){
            return ACTION_TYPE.REDUCTION;
        }else if(!productionActionMap.containsKey(reduceOP)){
            return ACTION_TYPE.SHIFT;
        }else{
            Integer sp = priorityMap.get(shiftOP);
            Integer rp = priorityMap.get(reduceOP);
            if(sp<rp){
                return ACTION_TYPE.REDUCTION;
            }else if(sp>rp){
                return ACTION_TYPE.SHIFT;
            }else{
                return leftAssociativeMap.get(shiftOP)?ACTION_TYPE.REDUCTION:ACTION_TYPE.SHIFT;
            }
        }
    }

    private void initTransition(){
        LR1State startState=new LR1State();
        Set<Integer> predictSet=new HashSet<>();
        predictSet.add(terminalMap.get("$"));
        startState.addItem(new LR1Item(productionItemDeque.get(0),predictSet));
        LR1StateTransitionList.add(new TransitionItem(0,findClosure(startState)));

        //扩展状态边
        int size=1;
        for (int i = 0; i < size; i++) {
            for (int edge = 0; edge < indexSymbol; edge++) {
                LR1State newState=findGOTO(LR1StateTransitionList.get(i).getLr1State(),edge);
                if(!newState.getItemList().isEmpty()){
                    boolean newStateFlag=true;
                    for(TransitionItem item:LR1StateTransitionList){
                        if(item.getLr1State().equals(newState)){
                            LR1StateTransitionList.get(i).getTransMap().put(edge,item.getId());
                            newStateFlag=false;
                            break;
                        }
                        if(newStateFlag){
                            LR1StateTransitionList.add(new TransitionItem(size,newState));
                            LR1StateTransitionList.get(i).getTransMap().put(edge,size);
                            size++;
                        }
                    }
                }
            }
        }
    }

    private void initParseTable(){
        for(int state=0,maxState=LR1StateTransitionList.size();state<maxState;state++){
            List<TableItem> temp=Collections.nCopies(indexSymbol,new TableItem());

            //reduce
            for (LR1Item  lr1Item: LR1StateTransitionList.get(state).getLr1State().getItemList()){

                //·在最后，需要进行规约
                if(lr1Item.isDotAtEnd()){
                    //填入预测符 的规约项
                    for(Integer pred: lr1Item.getPredSet()){
                        TableItem curTableItem = temp.get(pred);
                        switch (curTableItem.getAction()){
                            case ERROR:{
                                if(lr1Item.getProd().getIndex()==0){
                                    curTableItem=new TableItem(ACTION_TYPE.ACCEPT,lr1Item.getProd().getIndex());
                                }else{
                                    curTableItem=new TableItem(ACTION_TYPE.REDUCTION,lr1Item.getProd().getIndex());
                                }
                                break;
                            }
                            default:{
                                //规约-规约冲突
                                break;
                            }
                        }
                    }
                }
            }

            //shift
            for(Map.Entry<Integer,Integer> entry:LR1StateTransitionList.get(state).getTransMap().entrySet()){
                TableItem curTableItem = temp.get(entry.getKey());
                switch (curTableItem.getAction()){
                    case ERROR:{
                        if(terminalMap.containsKey(entry.getKey())){
                            //终结符
                            curTableItem=new TableItem(ACTION_TYPE.SHIFT,entry.getValue());
                        }else{
                            //非终结符
                            curTableItem=new TableItem(ACTION_TYPE.GOTO_STATE, entry.getValue());
                        }
                        break;
                    }
                    case REDUCTION:{
                        //产生 移入-规约冲突 利用附加条件消除冲突
                        //@todo 应该定义为-1
                        int reduceOP=-1;
                        //@todo something wrong
                        List<Integer> productionBody = productionItemDeque.get(curTableItem.getIndex()).getBody();
                        for (int i = productionBody.size()-1; i >=0 ; i--) {
                            if(terminalMap.containsKey(productionBody.get(i)));{
                                reduceOP=productionBody.get(i);
                                break;
                            }
                        }
                        //根据冲突消除规则应保留SHIFT操作
                        if(reduceOP!=-1&&solveConflict(entry.getKey(), reduceOP)==ACTION_TYPE.SHIFT){
                            curTableItem=new TableItem(ACTION_TYPE.SHIFT, entry.getValue());
                        }
                        break;
                    }
                    case SHIFT:{
                        //不可能
                        break;
                    }
                    default:{
                        //不可能
                    }

                }
            }
            lr1ParseTable.add(temp);
        }
    }


}

