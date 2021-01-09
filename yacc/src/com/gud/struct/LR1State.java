package com.gud.struct;

import java.util.List;
import java.util.Map;

/**
 * Created By Gud on 2021/1/6 8:13 下午
 *
 * LR1状态（LR1集族），即多个LR1产生式
 */
public class LR1State {

    List<LR1Item> itemList;


    public boolean addItem(LR1Item lr1Item){
        for(LR1Item item:this.itemList){
            if(item.equals(lr1Item)){
                return false;
            }else if(item.equalsCore(lr1Item)){
                item.predSet.addAll(lr1Item.predSet);
                return true;
            }
        }
        itemList.add(lr1Item);
        return true;
    }

    /**
     * 只需要比较两个lr1集族的itemList是否相等
     * @param lr1State
     * @return
     */
    public boolean equals(LR1State lr1State){
        if(this.itemList.size()!=lr1State.itemList.size()){
            return false;
        }
        int size=0;
        for (LR1Item it:this.itemList){
            for(LR1Item it2:lr1State.itemList){
                if (it.equals(it2)){
                    size++;
                    break;
                }
            }
        }
        if(size==this.itemList.size()){
            return true;
        }
        return false;
    }

    public boolean equalsCore(LR1State lr1State){
        if (this.itemList.size()!=lr1State.itemList.size()){
            return false;
        }
        int size=0;
        for (LR1Item it:this.itemList){
            for(LR1Item it2:lr1State.itemList){
                if (it.equalsCore(it2)){
                    size++;
                    break;
                }
            }
        }
        if(size==this.itemList.size()){
            return true;
        }
        return false;
    }


    public List<LR1Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<LR1Item> itemList) {
        this.itemList = itemList;
    }
}
