package symbol;

import java.util.ArrayList;

public class FunctionSymbol extends Symbol {

 	public ArrayList<String> paramType = new ArrayList<>(); // Parameters types
	// Lista com tipos dos parametros e tamanho de cada parametro
	// Precisa do tipo e tamanho do retorno
	public String functionType;

	public FunctionSymbol(String functionType, ArrayList<String> paramType) {
		super(Symbol.SymbolType.FUNCAO);
		this.functionType = functionType;
		this.paramType = paramType;
		setFunctionType();
	}

	private void setFunctionType() {
		switch(functionType) {
			case "inteiro":
				super.valueType = Symbol.SymbolValueType.INT;
				break;
			case "fracionario":
				super.valueType = Symbol.SymbolValueType.DOUBLE;
				break;
			case "texto":
				super.valueType = Symbol.SymbolValueType.STRING;
				break;
			case "logico":
				super.valueType = Symbol.SymbolValueType.BOOLEAN;
				break;
			case "vazio":
				super.valueType = Symbol.SymbolValueType.VAZIO;
				break;
		}
	}

}
