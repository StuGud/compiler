package com.gud.struct;

/**
 * 汇编代码
 * _label : _op _des _arg
 * <p>
 * Created By Gud on 2021/1/11 2:34 下午
 */
public class Assembly {
    String _op; //操作符
    String _des;  //目的操作数
    String _arg; //源操作数
    String _label;  //标签，对应中间代码中的标号

    // 源和目的不能都为存储器直接寻址
    Assembly(String op, String des, String arg) {
        this._op = op;
        this._des = des;
        this._arg = arg;
    }

    Assembly(String op, int des, String arg) {
        this._op = op;

        this._arg = arg;
        this._des = transformToRegName(des);
    }

    Assembly(String op, int des, int arg) {
        this._op = op;
        _des = transformToRegName(des);
        _arg = transformToRegName(arg);
    }

    Assembly(String op, String des, int arg) {
        this._op = op;
        _des = des;
        _arg = transformToRegName(arg);
    }

    //寄存器寻址，根据序号选择不同的寄存器
    String transformToRegName(int reg) {
        if (reg == 0) return "AX";
        else if (reg == 1) return "BX";
        else if (reg == 2) return "CX";
        else if (reg == 3) return "DX";
        else if (reg == 4) return "SI";
        else if (reg == 5) return "DI";
        else return "AX";
    }
}
