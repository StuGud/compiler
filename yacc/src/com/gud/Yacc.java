package com.gud;

import java.io.IOException;

/**
 * Created By Gud on 2021/1/9 10:25 下午
 */
public class Yacc {

    public static void main(String[] args) throws IOException {
        YaccFileParser fileParser = new YaccFileParser();
        fileParser.parseYaccFile("input/Cminus.y");

        LR1Builder lr1Builder = new LR1Builder(fileParser);
        lr1Builder.initTransition();
        lr1Builder.initParseTable();

        CppBuilder cppBuilder=new CppBuilder(lr1Builder);
        cppBuilder.generateParseProgram();

        System.out.println("end");
    }
}
