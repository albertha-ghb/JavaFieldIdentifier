package com.github.albertha_ghb.fieldIdentifier;

import java.io.InputStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author
 */
public class FindFieldsListenerTest
{

    @Test
    public void test() throws Exception
    {

        InputStream in = FindFieldsListenerTest.class.getClassLoader()
                .getResourceAsStream("javaSourceFile/ClassToParse.java");
        Java8Lexer java8Lexer = new Java8Lexer(CharStreams.fromStream(in));

        CommonTokenStream tokens = new CommonTokenStream(java8Lexer);
        Java8Parser parser = new Java8Parser(tokens);
        ParseTree tree = parser.compilationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();
        FindFieldsListener listener = new FindFieldsListener();

        walker.walk(listener, tree);

        listener.getFields().forEach(f -> System.out.println(f));

        Assert.assertEquals(7, listener.getFields().size());
    }

}
