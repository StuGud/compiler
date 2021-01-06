package com.gud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Created By Gud on 2021/1/2 7:56 下午
 */
public class YaccFileParser {


    List<String> tokens;
    String start;
    Map grammarMap;
    String programHead;
    String programBody;

    private void loadFile(String filename) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        String[] segments = content.split("%%");
        if(segments.length!=3){
            throw new IOException("YACC文件不完整");
        }

        parseDefineSegment(segments[0]);
        parseGrammarSegment(segments[1]);
        parseProgramSegment(segments[2]);
    }

    private void parseDefineSegment(String defineSegment){
        String[] split = defineSegment.split(System.lineSeparator());
        for (int i = 0; i < split.length; i++) {
            String[] split1 = split[i].split(" ");
            if("%token".equals(split1[0])){
                for (int j = 1; j < split1.length; j++) {
                    if(!"".equals(split1[j])){
                        tokens.add(split1[j]);
                    }
                }
            }else if("%start".equals(split1[0])){
                for (int j = 1; j < split1.length; j++) {
                    if(!"".equals(split1[j])){
                        start=split1[j];
                        break;
                    }
                }

            }
        }
    }


    private void parseGrammarSegment(String grammarSegment){


    }

    private void parseProgramSegment(String programSegment){
        String[] split = programSegment.split(System.lineSeparator());
        StringBuilder sb1=new StringBuilder();
        StringBuilder sb2=new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            if(split[i].startsWith("#include")){
                sb1.append(split[i]);
            }else{
                sb2.append(split[i]);
            }
        }
        programHead=sb1.toString();
        programBody=sb2.toString();

    }


}
