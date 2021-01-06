package com.gud;

import com.gud.struct.DFA;
import com.gud.struct.DFAState;

import java.util.List;
import java.util.Map;

/**
 * Created By Gud on 2021/1/2 4:42 下午
 */
public class CppBuilder {

    public void buildCpp(DFA dfa,LexParser lexParser) {
        StringBuilder sb = new StringBuilder();

        List<String> includeStrList = lexParser.getIncludeStrList();
        for(String s:includeStrList){
            sb.append(s+"\n");
        }

        String head = "#include <iostream>\n" +
                "#include <string>\n" +
                "#include <fstream>\n" +
                "#include <streambuf>\n" +
                "\n" +
                "std::string inputSrc;\n" +
                "\n" +
                "   int cp = 0;\n" +
                "   int state = 0;\n" +
                "   int keyState=0;//关键字对应的state\n" +
                "\n" +
                "   void dfa(char c);\n" +
                "\n" +
                "   int main(int argc, const char * argv[]) {\n" +
                "       std::ifstream f(argv[1]);\n" +
                "       std::string str((std::istreambuf_iterator<char>(f)),\n" +
                "                        std::istreambuf_iterator<char>());\n" +
                "       inputSrc = str;\n" +
                "       while (cp < inputSrc.length()) {\n" +
                "           try {\n" +
                "               dfa(inputSrc[cp]);\n" +
                "           } catch(int e) {\n" +
                "               std::cout << \"出现错误\";\n" +
                "               break;\n" +
                "           }\n" +
                "       }\n" +
                "       return 0;\n" +
                "   }";
        sb.append(head);

        List<String> commentStrList = lexParser.getCommentStrList();
        //@todo 这边格式注意一下
        for(String s:commentStrList){
            sb.append(s+"\n");
        }

        for(Map.Entry<Integer, DFAState> entry:dfa.getStateMap().entrySet()){

        }



    }

}
