package main;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import antlr.analisadorBaseListener;
import antlr.analisadorParser;
import antlr.analisadorParser.TipoContext;
import symbol.Symbol;
import symbol.SymbolTable;
import symbol.VariableSymbol;

public class analisadorSemanticListener extends analisadorBaseListener {

	private enum Tipo {
		INT("inteiro"), FRACIONARIO("fracionario"), LOGICO("logico"), TEXTO("texto");

		private final String name;

		private Tipo(String s) {
			name = s;
		}

		public String toString() {
			return this.name;
		}

		public static Tipo getEnumByString(String s) {
			for (Tipo e : Tipo.values()) {
				if (s == e.name)
					return e;
			}
			return null;
		}

	}

	private String filepath;

	public SymbolTable symbolTable;

	// anotacoes
	private ParseTreeProperty<Tipo> types = new ParseTreeProperty<>();
	private ParseTreeProperty<String> productionNames = new ParseTreeProperty<>();
	// private ParseTreeProperty<String> id = new ParseTreeProperty<>();
	private ParseTreeProperty<Integer> sizes = new ParseTreeProperty<>();

	public analisadorSemanticListener(String filepath) {
		this.filepath = filepath;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}

	public void enterEscopo(analisadorParser.EscopoContext ctx) {
		symbolTable = new SymbolTable(symbolTable);
		productionNames.put(ctx, "escopo_classe");
	}

	public void enterFuncaoDeclaracao(analisadorParser.Funcao_declaracaoContext ctx) {
		symbolTable = new SymbolTable(symbolTable);
		productionNames.put(ctx, "escopo_funcao");
	}

	public void enterBlocoComando(analisadorParser.Bloco_comandoContext ctx) {
		symbolTable = new SymbolTable(symbolTable);
		productionNames.put(ctx, "bloco_comando");
	}
	
	public void enterAtribuicao(analisadorParser.AtribuicaoContext ctx) {
		productionNames.put(ctx, "atribuicao");
	}

	public void exitBlocoComando(analisadorParser.Bloco_comandoContext ctx) {
		symbolTable = symbolTable.parent;
	}

	public void exitTipo(analisadorParser.TipoContext ctx) {
		if (ctx.T_FRACIONARIO() != null) {
			types.put(ctx, Tipo.FRACIONARIO);
			sizes.put(ctx, -1);
		} else if (ctx.T_INTEIRO() != null) {
			types.put(ctx, Tipo.INT);
			sizes.put(ctx, -1);
		} else if (ctx.T_TEXTO() != null) {
			types.put(ctx, Tipo.TEXTO);
			sizes.put(ctx, -1);
		} else if(ctx.T_LOGICO() != null) {
			types.put(ctx, Tipo.LOGICO);
			sizes.put(ctx, -1);
		}
		
	}

	public void exitAtribuicao(analisadorParser.AtribuicaoContext ctx) {
		String id = ctx.IDENTIFICADOR(0).getText();

		
		SymbolTable st = symbolTable;
		ParserRuleContext c = ctx;
		String rule = productionNames.get(c);
		Symbol symbol = st.lookup(id);
		if (symbol == null) { // ve se esta em loop ou if
			while (rule == "bloco_comando" /*|| rule == null*/) {
				c = c.getParent();
				rule = productionNames.get(c);
				if (st.parent != null) {
					st = st.parent;
				}
				if (st.lookup(ctx.IDENTIFICADOR(0).getText()) != null) {
					break;
				}
			}

		}
		symbol = st.lookup(id);
		if(ctx.tipo() == null ) {
			if(symbol == null) {
				System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
				System.out.println("Variável não inicializada.");
				return;
			}
		}
		
		types.put(ctx, types.get(ctx.tipo()));

		// verifica se o simbolo(ID) ja ta na tabela de simbolos se tiver printa erro
		// semantico e retorna

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
			String type = ctx.tipo().getText();

			if (productionNames.get(ctx) != null) {
				rule = productionNames.get(ctx);
				boolean initialized = true;

				 if (ctx.ATRIBUICAO() == null) {
					 initialized = false;
				 }
				// if (!rule.equals("function_block")) {
				 symbol = new VariableSymbol(type, initialized);
				// }

				symbolTable.put(id, symbol);
			}
		}
	}

}
