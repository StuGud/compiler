package com.gud;

import com.gud.struct.NFA;
import com.gud.struct.NFAFragment;

import java.io.*;

/**
 * Created By Gud on 2020/12/29 2:50 上午
 */
public class Test {
    public static void main(String[] args) throws IOException {
        System.out.println((char) 248);

        //new Test().testNFABuilder();

        //new EscapeUtil().escapeFormalizedStr("\\+abc\\*");

        //new Test().readFile();

    }

    private void testNFABuilder(){
        String formalizedReg ="abc||*";
        NFAFragment nfaFragment = new NFABuilder().buildNFAFragment(formalizedReg.split(""), new NFA());
        System.out.println(nfaFragment);
    }

    private void readFile() throws IOException {
        File file = new File("lex/src/com/gud/test.l");
        BufferedReader buf = new BufferedReader(new FileReader(file));

        String lineStr = buf.readLine();

        System.out.println((int)"ø".toCharArray()[0]);

        String formalizedReg = new RegFormalizer().formalize(lineStr);
        System.out.println(formalizedReg);
        System.out.println(new NFABuilder().postifx(formalizedReg));
    }
}
