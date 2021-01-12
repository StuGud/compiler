package com.gud.struct;

import java.util.List;
import java.util.Set;

/**
 * Created By Gud on 2021/1/11 2:39 下午
 */
public class BasicBlock {
    int _begin;  //入口语句的四元式标号
    int _end;  //出口语句的四元式标号
    List<Integer> _predecessors; //该基本块的前继（基本块标号的集合）
    List<Integer> _successors; //该基本块的后继（基本块标号的集合）
    Set<String> _inLiveVar;
    Set<String> _outLiveVar;

    BasicBlock(int index) {
        _begin = index;  //入口语句标号为该基本块标号
    }
    BasicBlock() {
        _begin = -1;
    }
}
