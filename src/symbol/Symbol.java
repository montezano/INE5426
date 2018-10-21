package symbol;

public class Symbol {

	public enum SymbolType {
		VARIAVEL,
		FUNCAO,
		SERVICO;
	}

	public enum SymbolValueType {
		INT ("inteiro"),
		DOUBLE ("fracionario"),
		BOOLEAN ("logico"),
		STRING ("texto");
		
		private final String name;

	    private SymbolValueType(String s) {
	        name = s;
	    }

		public String toString() {
			return this.name;
	 	}
	}

    public SymbolType type; // Variable, function, object
    public SymbolValueType valueType;
    public Integer size = -1;

	public Symbol(SymbolType type) {
        this.type = type;
    }

    public Symbol(SymbolType type, SymbolValueType valueType) {
        this.type = type;
        this.valueType = valueType;
    }

}
