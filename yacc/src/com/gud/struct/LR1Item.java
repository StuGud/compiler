package com.gud.struct;

import java.util.Set;

/**
 * Created By Gud on 2021/1/6 8:08 下午
 * <p>
 * LR1产生式 （加上预测符）
 */
public class LR1Item {
    //产生式
    ProductionItem prod;

    //@todo 是否定义为int类型
    //产生式中的 · 的位置，用于预测下一个字符
    int dotLoc;

    //预测符集
    Set<Integer> predSet;

    public LR1Item(ProductionItem prod, Set<Integer> predSet) {
        this.prod = prod;
        this.predSet = predSet;
        dotLoc = 0;
    }


    public boolean equals(LR1Item lr1Item) {
        return this.dotLoc == lr1Item.dotLoc && this.prod.equals(lr1Item.prod) && deepEqualSet(this.predSet, lr1Item.predSet);
    }

    private boolean deepEqualSet(Set<?> set1, Set<?> set2) {
        if (set1 == null || set2 == null) {
            return false;
        }
        if (set1.size() != set2.size()) {
            return false;
        }
        return set1.containsAll(set2);
    }

    /**
     * 是否有相同的内核；相同时可以合并预测符
     * @param lr1Item
     * @return
     */
    public boolean equalsCore(LR1Item lr1Item){
        return this.prod.equals(lr1Item.prod)&&this.dotLoc== lr1Item.dotLoc;
    }

    public boolean isDotAtEnd(){
        return dotLoc==prod.getBodyLength();
    }

    public ProductionItem getProd() {
        return prod;
    }

    public void setProd(ProductionItem prod) {
        this.prod = prod;
    }

    public int getDotLoc() {
        return dotLoc;
    }

    public void setDotLoc(int dotLoc) {
        this.dotLoc = dotLoc;
    }

    public Set<Integer> getPredSet() {
        return predSet;
    }

    public void setPredSet(Set<Integer> predSet) {
        this.predSet = predSet;
    }
}
