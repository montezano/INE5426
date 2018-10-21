package symbol;

public class VariableSymbol extends Symbol {

	public String variableType;
	public boolean initialized; // check variable initialization

	public VariableSymbol(String variableType, boolean initialized) {
		super(Symbol.SymbolType.VARIAVEL);
		if (variableType.equals("void")) {
			System.out.println("Void não pode ser tipo de variável");
			return;
		}
		this.variableType = variableType;
		this.initialized = initialized;
		setVariableType();
	}

	private void setVariableType() {
		switch(variableType) {
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
		}
	}


}
