package main;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.misc.IntervalSet;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import antlr.*;
import error.*;
import symbol.SymbolTable;

public class Main {

	public static void main(String[] args) throws Exception {
		parse(args, "");
	}

	public static SymbolTable parse(String[] args, String filepath) throws Exception {

		String inputFile = "/home/montezano/git/INE5426/src/testes/programa_teste_classe";
		if (args.length > 0) {
			inputFile = filepath + args[0];
			int endIndex = inputFile.lastIndexOf("/");
			if (endIndex != -1) {
				filepath = inputFile.substring(0, endIndex + 1);
			} else {
				filepath = "";
			}
		}

		InputStream is = System.in;
		if (inputFile != null) {
			System.out.println("Compilando arquivo \"" + inputFile + "\"");
			is = new FileInputStream(inputFile);
		}
		ANTLRInputStream input = new ANTLRInputStream(is);

		/* Lexer */
		analisadorLexer lexer = new analisadorLexer(input);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new LexerError());
		// create a buffer of tokens pulled from the lexer
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		// create a parser that feeds off the tokens buffer
		/* Parser */
		analisadorParser parser = new analisadorParser(tokens);
		parser.setErrorHandler(new ErrorStrategy());

		/* Parse Tree */
		ParseTree tree = parser.principal(); // begin parsing at eval rule

		System.out.println(tree.toStringTree(parser)); // print tree as text
		if (Arrays.asList(args).contains("-gui")) {
			treeGui(parser, tree);
		}

		/* Semantic analysis */
		ParseTreeWalker walker = new ParseTreeWalker();
		analisadorSemanticListener semanticListener = new analisadorSemanticListener(filepath);
		walker.walk(semanticListener, tree);
		return semanticListener.getSymbolTable();

	}

	public static void treeGui(analisadorParser parser, ParseTree tree) {
		JFrame frame = new JFrame("Tree");
		TreeViewer viewr = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
		viewr.setScale(1.5);
		JPanel panel = new JPanel();
		panel.add(viewr);
		JScrollPane jsp = new JScrollPane(panel);
		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.add(jsp);
		frame.setSize(5000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
