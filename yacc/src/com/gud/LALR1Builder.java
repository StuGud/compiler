package com.gud;

import com.gud.struct.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created By Gud on 2021/1/8 9:02 下午
 */
public class LALR1Builder {

    List<TransitionItem> lr1StateTransitionList;
    List<List<TableItem>> lr1ParseTable;

    Map<Integer,List<TableItem>> lalr1ParseTable;
    Map<Integer,TransitionItem> lalr1StateTransitionMap;
    /**
     * LR1转LALR1
     * @return
     */
    private boolean transformIntoLALR(){
        Map<Integer,Integer> mergeMap=new HashMap<>();

        //find same core from last to one
        for(int checkState= lr1ParseTable.size()-1;checkState>0;checkState--){
            LR1State toMergeState = lr1StateTransitionList.get(checkState).getLr1State();
            for(int it2state=0;it2state<checkState;it2state++){
                LR1State finalState = lr1StateTransitionList.get(it2state).getLr1State();
                //找到两个产生式核相同的部分
                if(finalState.equalsCore(toMergeState)){
                    mergeMap.put(checkState,it2state);
                    break;
                }
            }
        }

        //LALR状态转移
        for(int index=0;index<lr1StateTransitionList.size();index++){
            if(!mergeMap.containsKey(index)){
                //不需要merge的状态，preserve
                lalr1StateTransitionMap.put(index,lr1StateTransitionList.get(index));
            }
        }

        //
        for(Map.Entry<Integer,Integer> entry: mergeMap.entrySet()){
           LR1State mergeFinalState=lalr1StateTransitionMap.get(entry.getValue()).getLr1State();
           for (LR1Item lr1Item: lr1StateTransitionList.get(entry.getKey()).getLr1State().getItemList()){
               mergeFinalState.addItem(lr1Item);
           }
        }

        //构造LALR1分析表
        for (int curState=0;curState<lr1ParseTable.size();curState++){
            List<TableItem> line=lr1ParseTable.get(curState);
            for(TableItem tableItem:line){
                if(tableItem.getAction()== ACTION_TYPE.SHIFT||tableItem.getAction()==ACTION_TYPE.GOTO_STATE){
                    if(mergeMap.containsKey(tableItem.getIndex())){
                        tableItem.setIndex(mergeMap.get(tableItem.getIndex()));
                    }
                }
            }
            if(!mergeMap.containsKey(curState)){
                //不是mergeState
                lalr1ParseTable.put(curState,line);
            }else{
                //合并核相同的状态
                int cur=0;
                TableItem mergeFinalTranItem = lalr1ParseTable.get(mergeMap.get(curState)).get(cur);
                for(TableItem toMergeTranItem:line){
                    if(toMergeTranItem.getAction()==ACTION_TYPE.REDUCTION||toMergeTranItem.getAction()==ACTION_TYPE.SHIFT||toMergeTranItem.getAction()==ACTION_TYPE.GOTO_STATE){
                        if(toMergeTranItem.getAction()==mergeFinalTranItem.getAction()&&toMergeTranItem.getIndex()!=mergeFinalTranItem.getIndex()){
                            return false;
                        }
                        mergeFinalTranItem=toMergeTranItem;
                    }
                    cur++;
                    mergeFinalTranItem = lalr1ParseTable.get(mergeMap.get(curState)).get(cur);
                }
            }
        }
        return true;
    }
}
