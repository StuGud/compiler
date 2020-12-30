package com.gud;

import java.io.IOException;

/**
 * Created By Gud on 2020/12/29 12:42 上午
 */
public class Lex {
    public static void main(String[] args) throws IOException {

        new LexParser().parseLexFile("lex/src/com/gud/c99.l");
    }
}
