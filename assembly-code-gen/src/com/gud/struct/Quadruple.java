package com.gud.struct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 四元式
 * <p>
 * Created By Gud on 2021/1/11 2:06 下午
 */
public class Quadruple {
    int _type; //类型码
    String _label;
    String _op;

    String _des, _arg1, _arg2;
    // 表示它们对应的变量或是常量，默认为false即为变量
    boolean _typeDes = false, _typeArg1 = false, _typeArg2 = false;
    //该变量是否还会在基本块内被引用，false表示不会被引用
    boolean _liveDes, _liveArg1, _liveArg2;
    //表示会在哪个四元式被引用，数值为四元式标号，无引用则为-1
    int _nextDes, _nextArg1, _nextArg2;

    public Quadruple(String _op, String _des, String _arg1, String _arg2) {
        this._op = _op;
        this._des = _des;
        this._arg1 = _arg1;
        this._arg2 = _arg2;

        if (isNum(_arg1)) _typeArg1 = true;
        if (isNum(_arg2)) _typeArg2 = true;
        _typeDes = _typeArg1 & _typeArg2;

        // 对不同四元式进行翻译时根据类型码区分
        if ("ADD".equals(_op)) {
            _type = 10;
        } else if ("SUB".equals(_op)) {
            _type = 11;
        } else if ("MUL".equals(_op)) {
            _type = 12;
        } else if ("DIV".equals(_op)) {
            _type = 13;
        } else if ("NEG".equals(_op)) {
            _type = 2; // A = op B
        } else if (_op.isEmpty()) {
            _type = 3; // A = B
        } else if (_op.length() > 0 && 'j' == _op.toCharArray()[0]) {
            if (_op.length() == 1) {
                _type = 20; // j LABEL_xxx
            } else {
                // j rop B C LABEL_xxx
                if ("j<".equals(_op)) {
                    _type = 21;
                } else if ("j<=".equals(_op)) {
                    _type = 22;
                } else if ("j>".equals(_op)) {
                    _type = 23;
                } else if ("j>=".equals(_op)) {
                    _type = 24;
                } else if ("j==".equals(_op)) {
                    _type = 25;
                } else if ("j!=".equals(_op)) {
                    _type = 26;
                }
            }
        } else if ("param".equals(_op)) {
            _type = 6; // param p
        } else if ("call".equals(_op)) {
            _type = 7;// call N funName
        } else if ("return".equals(_op)) {
            _type = 8; // return a
        }

    }

    private boolean isNum(String s) {
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        Matcher isNum = pattern.matcher(s);
        if (isNum.matches()) {
            return true;
        }
        return false;
    }
}
