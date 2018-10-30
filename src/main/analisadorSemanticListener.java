package main;

import java.util.ArrayList;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import antlr.analisadorBaseListener;
import antlr.analisadorParser;
import antlr.analisadorParser.TipoContext;
import symbol.FunctionSymbol;
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

	public void enterFuncao_declaracao(analisadorParser.Funcao_declaracaoContext ctx) { 
		symbolTable = new SymbolTable(symbolTable);
		productionNames.put(ctx, "escopo_funcao");
	}

	public void enterBloco_comando(analisadorParser.Bloco_comandoContext ctx) {
		symbolTable = new SymbolTable(symbolTable);
		productionNames.put(ctx, "bloco_comando");
	}

	public void enterAtribuicao(analisadorParser.AtribuicaoContext ctx) {
		productionNames.put(ctx, "atribuicao");
	}

	public void enterParametros_declaracao(analisadorParser.Parametros_declaracaoContext ctx) {
		for (int i = 0; i < ctx.IDENTIFICADOR().size(); i++) {
			productionNames.put(ctx.IDENTIFICADOR(i), "parametros_declaracao");
		}
	}
	
//	public void enterParametros_chamada(analisadorParser.Parametros_chamadaContext ctx) {		
//		ctx.IDENTIFICADOR().forEach((id) ->{
//			productionNames.put(id, "parametros_chamada");
//		});
//		ctx.LITERAL().forEach((lit) ->{
//			productionNames.put(lit, "parametros_chamada");
//		});
//	}
	
	public void exitParametros_declaracao(analisadorParser.Parametros_declaracaoContext ctx) {		
		for(int i = 0; i < ctx.IDENTIFICADOR().size(); i++) {
			types.put(ctx.IDENTIFICADOR(i), types.get(ctx.tipo(i)));
		}
	}

	
	
	// public void
	// enterChamada_funcao_classe(analisadorParser.Chamada_funcao_classeContext ctx)
	// {
	// symbolTable = new SymbolTable(symbolTable);
	// productionNames.put(ctx, "bloco_funcao_classe");
	// }
	//
	// public void
	// enterChamada_funcao_servico(analisadorParser.Chamada_funcao_servicoContext
	// ctx) {
	// symbolTable = new SymbolTable(symbolTable);
	// productionNames.put(ctx, "function_block");
	// }

	public void exitBloco_comando(analisadorParser.Bloco_comandoContext ctx) {
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
		} else if (ctx.T_LOGICO() != null) {
			types.put(ctx, Tipo.LOGICO);
			sizes.put(ctx, -1);
		}

	}

	public void exitAtribuicao(analisadorParser.AtribuicaoContext ctx) {
		String id = ctx.IDENTIFICADOR(0).getText();
		//System.out.println(id);
		
		
		SymbolTable st = symbolTable;
		ParserRuleContext c = ctx;
		String rule = productionNames.get(c);
		Symbol symbol = st.lookup(id);
		if (symbol == null) { // ve se esta em loop ou if
			while (rule == "bloco_comando" /* || rule == null */) {
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

		if (ctx.tipo() == null) {
			if (symbol == null) {
				System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
				System.out.println("Variável não inicializada.");
				return;
			}
		}

		types.put(ctx, types.get(ctx.tipo()));

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
		
		if(ctx.ATRIBUICAO() != null) {
			
			if(ctx.tipo().getText().equals("texto")) {
				if(ctx.TEXTO() == null) {
					checkType(ctx, st, "texto");
				}
			} else if(ctx.tipo().getText().equals("inteiro")) {
				if(ctx.expressao() == null) {
					checkType(ctx, st, "inteiro");
				}
			} else if(ctx.tipo().getText().equals("fracionario")) {
				if(ctx.expressao() == null) {
					checkType(ctx, st, "fracionario");
				}
			} else if(ctx.tipo().getText().equals("logico")) {
				if(ctx.expressao() == null) {
					checkType(ctx, st, "logico");
				}
			}
		}
	}
	
	private void checkType(analisadorParser.AtribuicaoContext ctx, SymbolTable st, String type) {
		if(ctx.IDENTIFICADOR(1) == null) {
			System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
			System.out.println("Tipos diferentes.");
			return;
		} else {
			Symbol simbolo = st.lookup(ctx.IDENTIFICADOR(1).getText());
			if(!simbolo.valueType.toString().equals(type)) {
				System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
				System.out.println("Tipos diferentes.");
				return;
			}
		}
	}

	
	private void checkExpressao(analisadorParser.AtribuicaoContext ctx, SymbolTable st) {
		if(ctx.IDENTIFICADOR(1) == null) {
			System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
			System.out.println("Tipos diferentes.");
			return;
		} else {
			Symbol simbolo = st.lookup(ctx.IDENTIFICADOR(1).getText());
			if(!simbolo.valueType.toString().equals("inteiro")) {
				System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
				System.out.println("Tipos diferentes.");
				return;
			}
		}
	}

	public void exitFuncao_declaracao(analisadorParser.Funcao_declaracaoContext ctx) {
		symbolTable = symbolTable.parent;
		String type = null;
		if(ctx.tipo() != null) {
			type = ctx.tipo().getText();
		}
		String id = ctx.IDENTIFICADOR().getText();
		Integer nParam = ctx.parametros_declaracao().IDENTIFICADOR().size();
		ArrayList<String> paramTypes = new ArrayList<>();

		for (int i = 0; i < nParam; i++) {
			String pType = (types.get(ctx.parametros_declaracao().IDENTIFICADOR(i))).toString();
			paramTypes.add(pType);
		}

		Symbol symbol = new FunctionSymbol(type, paramTypes);
		symbolTable.put(id, symbol);
	}

	public void exitChamada_funcao_classe(analisadorParser.Chamada_funcao_classeContext ctx) {
		String idObjeto = ctx.IDENTIFICADOR(0).getText();
		String idFuncao = ctx.IDENTIFICADOR(1).getText();
		int line = ctx.getStart().getLine();

		SymbolTable st = symbolTable;
		ParserRuleContext c = ctx;
		String rule = productionNames.get(c);
		while (rule == "bloco_comando" || rule == null) {
			c = c.getParent();
			rule = productionNames.get(c);
			if (st.parent != null) {
				st = st.parent;
			}
			if (st.lookup(ctx.IDENTIFICADOR(0).getText()) != null) {
				break;
			}
		}

		if (st.lookup(idFuncao) == null) {
			System.out.print("Erro na linha " + line + ": ");
			System.out.println("Função não definida.");
			return;
		}

		ArrayList<String> paramTypes = ((FunctionSymbol) st.lookup(idFuncao)).paramType;
		int callArgSize = ctx.parametros_chamada().IDENTIFICADOR().size();
		callArgSize += ctx.parametros_chamada().LITERAL().size();
		// Checa numero de argumentos
		if (callArgSize != paramTypes.size()) {
			System.out.print("Erro na linha " + line + ": ");
			System.out.println("Número de argumentos incompatível");
			return;
		}
		boolean error = false;
		// Checa ordem dos tipos e tamanhos
		for (int i = 0; i < callArgSize; i++) {
			String callType = ctx.parametros_chamada().getChild(i).getText();
			String funcType = paramTypes.get(i);

			if (eInteiro(callType)) {
				if (!funcType.equals("inteiro")) {
					error = true;
				}
			} else if (eFracionario(callType)) {
				if (!funcType.equals("fracionario")) {
					error = true;
				}
			} else if (callType.equals("verdadeiro") || callType.equals("falso")) {
				if (!funcType.equals("logico")) {
					error = true;
				}
			} else if (callType.charAt(0) == '\"') {
				if (!funcType.equals("texto")) {
					error = true;
				}
			} else { // id
				if (st.lookup(callType) != null) {
					String symbType = st.lookup(callType).valueType.toString();
					if (!symbType.equals(funcType)) {
						System.out.print("Erro na linha " + line + ": ");
						System.out.println(
								"Tipo de argumento incompatível. Esperava-se: " + funcType + ". Recebido: " + symbType);
						return;
					}
				}
			}
			if (error) {
				System.out.print("Erro na linha " + line + ": ");
				System.out.println("Tipo de argumento incompatível. Esperava-se: " + funcType);
				return;
			}
		}
	}

	public void exitChamada_funcao_servico(analisadorParser.Chamada_funcao_servicoContext ctx) {
		String idObjeto = ctx.IDENTIFICADOR(0).getText();
		String idFuncao = ctx.IDENTIFICADOR(1).getText();
		int line = ctx.getStart().getLine();

		SymbolTable st = symbolTable;
		ParserRuleContext c = ctx;
		String rule = productionNames.get(c);
		while (rule == "bloco_comando" || rule == null) {
			c = c.getParent();
			rule = productionNames.get(c);
			if (st.parent != null) {
				st = st.parent;
			}
			if (st.lookup(ctx.IDENTIFICADOR(0).getText()) != null) {
				break;
			}
		}

		if (st.lookup(idFuncao) == null) {
			System.out.print("Erro na linha " + line + ": ");
			System.out.println("Função não definida.");
			return;
		}

		ArrayList<String> paramTypes = ((FunctionSymbol) st.lookup(idFuncao)).paramType;
		int callArgSize = ctx.parametros_chamada().IDENTIFICADOR().size();
		callArgSize += ctx.parametros_chamada().LITERAL().size();
		// Checa numero de argumentos
		if (callArgSize != paramTypes.size()) {
			System.out.print("Erro na linha " + line + ": ");
			System.out.println("Número de argumentos incompatível");
			return;
		}
		boolean error = false;
		// Checa ordem dos tipos e tamanhos
		for (int i = 0; i < callArgSize; i++) {
			String callType = ctx.parametros_chamada().getChild(i).getText();
			String funcType = paramTypes.get(i);

			if (eInteiro(callType)) {
				if (!funcType.equals("inteiro")) {
					error = true;
				}
			} else if (eFracionario(callType)) {
				if (!funcType.equals("fracionario")) {
					error = true;
				}
			} else if (callType.equals("verdadeiro") || callType.equals("falso")) {
				if (!funcType.equals("logico")) {
					error = true;
				}
			} else if (callType.charAt(0) == '\"') {
				if (!funcType.equals("texto")) {
					error = true;
				}
			} else { // id
				if (st.lookup(callType) != null) {
					String symbType = st.lookup(callType).valueType.toString();
					if (!symbType.equals(funcType)) {
						System.out.print("Erro na linha " + line + ": ");
						System.out.println(
								"Tipo de argumento incompatível. Esperava-se: " + funcType + ". Recebido: " + symbType);
						return;
					}
				}
			}
			if (error) {
				System.out.print("Erro na linha " + line + ": ");
				System.out.println("Tipo de argumento incompatível. Esperava-se: " + funcType);
				return;
			}
		}
	}
	
	private static boolean eInteiro(String str) {
	    try {
	        Integer.parseInt(str);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}

	private static boolean eFracionario(String str) {
	    try {
	        Double.parseDouble(str);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
}

}
