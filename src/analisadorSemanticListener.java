
//public class analisadorSemanticListener {
//
//}


import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.ParserRuleContext;


import symbol.*;

import java.util.ArrayList;

public class analisadorSemanticListener extends analisadorBaseListener {

	private enum Type {
		INT ("inteiro"),
		DOUBLE ("fracionario"),
		BOOLEAN ("logico"),
		STRING ("texto");

		private final String name;

	    private Type(String s) {
	        name = s;
	    }

		public String toString() {
			return this.name;
	 	}

	 	public static Type getEnumByString(String s){
     		for(Type e : Type.values()){
				if(s == e.name) return e;
		     }
      		return null;
  		}

	}

	private String filepath;

	public SymbolTable symbolTable;

	//anotacoes
	private ParseTreeProperty<Type> types = new ParseTreeProperty<>();
	private ParseTreeProperty<String> productionNames = new ParseTreeProperty<>();
	// private ParseTreeProperty<String> id = new ParseTreeProperty<>();
	private ParseTreeProperty<Integer> sizes = new ParseTreeProperty<>();

	

	public analisadorSemanticListener(String filepath) {
		this.filepath = filepath;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}
	
	public void exitAtribuicao(analisadorParser.AtribuicaoContext ctx) {
		String id = ctx.IDENTIFICADOR().getText();

		SymbolTable st = symbolTable;
		ParserRuleContext c = ctx;
		String rule = productionNames.get(c);
		Symbol symbol = st.lookup(id);
		if (symbol == null) { // ve se esta em loop ou if
			while (rule == "block_command" || rule == "CmdDeclAttrib" || rule == null) {
				c = c.getParent();
				rule = productionNames.get(c);
				if (st.parent != null) {
					st = st.parent;
				}
				if (st.lookup(ctx.IDENTIFICADOR().getText()) != null) {
					break;
				}
			}

		}
		symbol = st.lookup(id);

		types.put(ctx, types.get(ctx.TIPO()));

		//verifica se o simbolo(ID) ja ta na tabela de simbolos se tiver printa erro semantico e retorna

		if (symbol != null) {
			switch (symbol.type) {
				case VARIAVEL:
					System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
					System.err.println("Variável já declarada");
					break;
				case FUNCAO:
					System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
					System.err.println("Função já declarada");
					break;
				case SERVICO:
					System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
					System.err.println("Serviço já declarado");
					break;
				}
				return;
		} else {
			String type = ctx.TIPO().getText();

			if (productionNames.get(ctx) != null) {
				 rule = productionNames.get(ctx);
				boolean initialized = true;
//		PRECISA TRATAR DE ONDE VEM A ATRIBUICAO
//		SE FOR DE UM COMANDO QUE INICIALIZA OU
//		NAO.		
//				if (rule.equals("CmdDecl")) {
//					initialized = false;
//				}
//				if (!rule.equals("function_block")) {
//					symbol = new VariableSymbol(type, initialized);
//				}

				symbolTable.put(id, symbol);
			}
		}
	}

}
