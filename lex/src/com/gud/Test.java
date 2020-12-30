package com.gud;

/**
 * Created By Gud on 2020/12/29 2:50 上午
 */
public class Test {
    public static void main(String[] args) {
        System.out.println(".".equals("."));
        //System.out.println("\v");

        String[] split={"a","c","\t"};

        System.out.println(String.join("",split));
        System.out.println(split.toString());

        String formalizedReg = new RegFormalizer().formalize("[ \\t\\v\\n\\f]");
        System.out.println(formalizedReg);
    }
}
