package com.gud.struct;

import java.util.List;

/**
 * Created By Gud on 2021/1/6 7:51 下午
 *
 * 产生式结构
 */
public class ProductionItem {
    //产生式头部非终结符
    int head;
    //箭头后产生式主题
    List<Integer> body;
    //@todo 这个变量是否有必要
    int bodyLength;
    //第i条表达式
    int index;
    //@todo 这个变量干啥的
    int op;

    public ProductionItem(int head, List<Integer> body, int bodyLength, int index) {
        this.head = head;
        this.body = body;
        this.bodyLength = bodyLength;
        this.index = index;
        this.op=-1;
    }

    public boolean equals(ProductionItem productionItem){
        return this.index== productionItem.index;
    }
}
