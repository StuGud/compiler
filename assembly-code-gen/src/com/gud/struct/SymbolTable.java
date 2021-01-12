package com.gud.struct;

import java.util.List;
import java.util.Set;

/**
 * Created By Gud on 2021/1/11 2:42 下午
 */
public class SymbolTable {
    public static final int REGISTER_NUM = 6;
    // data
    String _funName;
    //unsigned int _maxOffset = 0;
    int _returnSize = 0; //该函数返回值所占的字节数
    int _variableOffset = 0; // 局部变量所占的全部字节数
    int _paramOffset = 0; // 函数实参所占的全部字节数
    int _beginIndex = -1;
    int _endIndex = -1;

    List<varState> _field;
    Set<Integer> _leaders;
    //std::vector<std::set<String> > RValue;
    //std::vector<int> RNextUse;

    // function
    SymbolTable(String name) {
        this._funName = name;
        //RValue.resize(REGISTER_NUM);
        //RNextUse.resize(REGISTER_NUM);
        //std::fill_n(RNextUse.begin(), REGISTER_NUM, -1);
    }

    // add local variable into symbol table
    //@todo placeFlag默认值true
    String enter(String name, String type, int space, boolean placeFlag) {
        // 检索该符号表中是否已有该单词（变量）信息
        for (varState it : _field) {
            if (it._name == name) return it._place;
        }
        _variableOffset += space; // 分配空间
        varState state = new varState(name, type, _variableOffset, space);
        if (placeFlag) { //局部变量用【BP-OFFSET】基址寻址存储访问
            state._place = "[BP-" + _variableOffset + "]";
        } else {
            state._place = name;
        }
        _field.add(state);
        return state._place;
    }

    // add param into symbol table
    String enterParam(String name, String type, int space) {
        for (varState it : _field) {
            if (it._name == name) {
                //@todo return "false";
                return "";
            }
        }
        _paramOffset += space;
        varState state = new varState(name, type, _paramOffset, space);
        // 访问实参时，采用[BP+offset]的基址寻址方式
        state._place = "[BP+" + ((REGISTER_NUM - 1 + 2 - 1) * 2 + _paramOffset) + "]";
        _field.add(state);
        return state._place;
    }

    // 检测某个空间位置是否在符号表中
    varState at(String place) {
        for (varState it : _field) {
            if (it._place == place) {
                return it;
            }
        }
        return null;
        //@todo
        //std::cerr << place << " is not in symbol table " << _funName << std::endl;
        // error handle
        //exit(1);
    }

    class varState {
        boolean _live = false;  //暂存活跃与待用信息，用于生成四元式中活跃Live和待用Next-Use
        int _nextUse = -1;
        boolean _inM = false;
        int _inR = -1;
        boolean _unsigned;
        int _offset;
        int _space;
        String _name;
        String _type;
        String _place; //对应的动态栈区分配模型的变量访问地址

        varState(String name, String type
                , int offset, int space) {
            this._name = name;
            this._type = type;
            this._offset = offset;
            this._space = space;

//            String::size_type pos = _type.find("unsigned", 0);
//            if (pos != String::npos) {
//                _unsigned = true;
//            }
            if (_type.startsWith("unsigned")) {
                _unsigned = true;
            }
        }
    }
}
