// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   SpiderTest.java

package senti.test;

import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class Test
{

    /**
     * 无参构造函数
     */
    public Test()
    {
    }

    /**
     * 测试硬编码嵌入的文本是否为纯ascii码，如果不是的话，输出非ascii码内容
     * @param args null
     */
    public static void main(String args[])
    {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String test = "R\351al";
        System.out.println((new StringBuilder(String.valueOf(test))).append(" isPureAscii() : ").append(asciiEncoder.canEncode(test)).toString());
        for(int i = 0; i < test.length(); i++)
            if(!asciiEncoder.canEncode(test.charAt(i)))
                System.out.println((new StringBuilder(String.valueOf(test.charAt(i)))).append(" isn't Ascii() : ").toString());

        test = "Real";
        System.out.println((new StringBuilder(String.valueOf(test))).append(" isPureAscii() : ").append(asciiEncoder.canEncode(test)).toString());
        test = "a\u2665c";
        System.out.println((new StringBuilder(String.valueOf(test))).append(" isPureAscii() : ").append(asciiEncoder.canEncode(test)).toString());
        for(int i = 0; i < test.length(); i++)
            if(!asciiEncoder.canEncode(test.charAt(i)))
                System.out.println((new StringBuilder(String.valueOf(test.charAt(i)))).append(" isn't Ascii() : ").toString());

        System.out.println((new StringBuilder("Encoded Word = ")).append(URLEncoder.encode(test)).toString());
    }
}
