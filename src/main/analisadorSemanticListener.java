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

	//code generation
	private String llcode = "";
	private boolean compile_error = false;
	private int counter_it = 0;
	private ParseTreeProperty<String> intermediateCode = new ParseTreeProperty<>();
	private ParseTreeProperty<String> intermediateVars = new ParseTreeProperty<>();

	// anotacoes
	private ParseTreeProperty<Tipo> types = new ParseTreeProperty<>();
	private ParseTreeProperty<String> productionNames = new ParseTreeProperty<>();
	// private ParseTreeProperty<String> id = new ParseTreeProperty<>();
	private ParseTreeProperty<Integer> sizes = new ParseTreeProperty<>();

	public String getllcode(){
		return llcode;
	}
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

		//codegeneration
		// Type type = Type.getEnumByString(ctx.tipo().getText());
		String typ = ctx.tipo().getText();
		String id = ctx.IDENTIFICADOR().getText();
		Integer nParam = ctx.parametros_declaracao().IDENTIFICADOR().size();

		if(typ.equals("inteiro")) {
				String ic = "define i32 @" + id + "(";
				for(int i = 0; i < nParam; i++) {
					if(i != 0) {
						ic += ", ";
					}
					String typParam = ctx.parametros_declaracao().tipo(i).getText();
					String idParam = "%" + ctx.parametros_declaracao().IDENTIFICADOR(i).getText();
					if(typParam.equals("inteiro")) {
						ic += "i32 " + idParam;
					}
				}
				ic += ") {\n";
				llcode += ic;
		}
	}

	// CRIAR CÓDIGO PARA IF (SE)
	// SE a == b
	public void enterBloco_comando(analisadorParser.Bloco_comandoContext ctx) {
		symbolTable = new SymbolTable(symbolTable);
		productionNames.put(ctx, "bloco_comando");
		if (compile_error == false) {
			if (ctx.se() != null) {
				//analisadorParser.If_blockContext blockCtx = (analisadorParser.If_blockContext) ctx.getParent();
				if (compile_error == false) {
					llcode += "%t_0 = add i32 0, %"+ctx.se().condicao().IDENTIFICADOR(0).getText()+"\n";
					llcode += "%t_1 = add i32 0, %"+ctx.se().condicao().IDENTIFICADOR(1).getText()+"\n";
					llcode += "%t_2 = icmp eq i32 %t_0, %t_1\n";					
					String if_label = "%l_" + counter_it;
					String if_labelX = "l_" + counter_it;
					counter_it++;
					String exit_label = "%l_exit";
					counter_it++;
					llcode += "br i1 %t2, label " + if_label + ", label " + exit_label + "\n";
					llcode += if_labelX + ":\n";
					//intermediateVars.put(ctx, exit_label);
				}
				
			}
		}
	}

	public void enterAtribuicao(analisadorParser.AtribuicaoContext ctx) {
		symbolTable = new SymbolTable(symbolTable);
		productionNames.put(ctx, "atribuicao");
	}
	
	public void enterAtribuicao_ternario(analisadorParser.Atribuicao_ternarioContext ctx) {
		productionNames.put(ctx, "atribuicao_ternario");
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
		//l_exit
		llcode += "br label %l_exit \n";
		llcode += "%l_exit :\n";
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
	
		
		SymbolTable st = symbolTable;
		ParserRuleContext c = ctx;
		String rule = productionNames.get(c);
		Symbol symbol = st.lookup(id);
		if (symbol == null) { 
			while (rule == "atribuicao" || rule == null && c != null) {
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
		types.put(ctx.IDENTIFICADOR(0), types.get(ctx.tipo()));
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
				symbol = new VariableSymbol(type, initialized);

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
		// ADICIONAR CODIGO PARA ACEITAR inteiro a = 2 (TEM QUE TESTE, FIZ ALGUMAS MODIFICACOES)
		if (compile_error == false) {
			if (ctx.tipo().getText().equals("inteiro")){
					if(ctx.expressao().expressao_matematica().IDENTIFICADOR(0) == null){
						// GERACAO DE CODIGO int a = 3
						String var_name = "%" + ctx.IDENTIFICADOR(0).getText();
						
						llcode += var_name + " = add i32 0, " + ctx.expressao().expressao_matematica().getText() + "\n";
					}else{
						// GERACAO DE CODIGO int c = a + b
						String var_name = "%" + ctx.IDENTIFICADOR(0).getText();
						String operation = ctx.expressao().expressao_matematica().OPERADORES_MATEMATICOS(0).getText();
						String op_name = "";
						if (operation.equals("+")) {
							op_name = "add";
						} else if (operation.equals("-")) {
							op_name = "sub";
						} else if (operation.equals("*")) {
							op_name = "mul";
						} else if (operation.equals("/")) {
							op_name = "udiv";
						}
						llcode += var_name + " = " + op_name + " i32 %" + ctx.expressao().expressao_matematica().IDENTIFICADOR(0).getText()+", %"+ctx.expressao().expressao_matematica().IDENTIFICADOR(1).getText() + "\n";					
						

					}
			}

		}
	}
	
	private void checkType(analisadorParser.AtribuicaoContext ctx, SymbolTable st, String type) {
		if(ctx.IDENTIFICADOR(1) == null) {
			System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
			System.out.println("Tipos diferentes.");
			compile_error = true;
			return;
		} else {
			Symbol simbolo = st.lookup(ctx.IDENTIFICADOR(1).getText());
			if(!simbolo.valueType.toString().equals(type)) {
				System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
				System.out.println("Tipos diferentes.");
				compile_error = true;
				return;
			}
		}
	}


	
	public void exitAtribuicao_ternario(analisadorParser.Atribuicao_ternarioContext ctx) {
		if(ctx.ternario().IDENTIFICADOR() == null && ctx.ternario().condicao() == null) {
			System.out.print("Erro na linha " + ctx.getStart().getLine() + ": ");
			System.out.println("Atribuicao de ternario invalida.");
			return;
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
