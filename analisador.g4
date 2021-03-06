/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
grammar analisador;

IMPORTE : 'importe' ;
CLASSE : ( ESC_CLASSE | ESC_SERVICO );
VISIBILIDADE : ( V_PUBLICO | V_PRIVADO | V_PROTEGIDO | V_VISUALIZAVEL );
LITERAL : ( DIGITO+ (PONTO (DIGITO)*)? | VERDADEIRO | FALSO ) ;
TIPO : ( T_INTEIRO | T_FRACIONARIO | T_LOGICO | T_TEXTO ) ;
LOGICO : ( VERDADEIRO | FALSO ) ;
OPERADORES_MATEMATICOS : ( MAIS | MENOS | DIV | MULT | MODULO | EXP ) ;
OPERADORES_LOGICOS : (E | OU );
OPERADORES_COMPARACAO: (IGUAL | DIFERENTE | MAIOR | MENOR | MAIORIGUAL | MENORIGUAL); 
ESC_CLASSE      : 'classe';
ESC_SERVICO     : 'servico';
MAIS 		: '+';
MENOS		: '-' ;
MULT		: '*' ;
DIV		: '/' ;
EXP             : '^';
SE		: 'se';
SENAO		: 'senao';	
ENQUANTO	: 'enquanto';
PARA		: 'para';
E		: '&&';
NAO		: '!';
OU		: '||';
VERDADEIRO	: 'verdadeiro';
FALSO		: 'falso';
T_INTEIRO 	: 'inteiro';
T_FRACIONARIO   : 'fracionario';
T_TEXTO   	: 'texto';
T_LOGICO  	: 'logico';
RETORNO		: 'retorno';	
ESCOLHA		: 'escolha';
CASO		: 'caso';
PARAR           : 'parar';
MAISMAIS	: '++';
MENOSMENOS	: '--';
MODULO		: '%';
E_COLCHETE	: '[';
D_COLCHETE	: ']';
E_PARENTESES	: '(';
D_PARENTESES	: ')';
E_CHAVE 	: '{';
D_CHAVE 	: '}';
DOISPONTOS	: ':';
VIRGULA		: ',';
PONTO		: '.';
PONTOVIRGULA	: ';';
MAIOR		: '>';
MENOR		: '<';
IGUAL   	: '==';
DIFERENTE	: '!=';
MAIORIGUAL	: '>=';
MENORIGUAL	: '<=';
ATRIBUICAO	: '=';
JOGODAVELHA	: '#';
V_PUBLICO       : 'publico';
V_PRIVADO       : 'privado';
V_PROTEGIDO     : 'protegido';
V_VISUALIZAVEL  : 'visualizavel';
SETA            : '->';
PRINCIPAL       : 'principal' ;
QUALIFICADOR : 'constante';
FUNCAO : 'funcao';
NOVO : 'novo';


fragment LETRAMI: ('a'..'z');
fragment LETRAMU: ('A'..'Z');
fragment LETRA :  ( LETRAMI | LETRAMU ) ;

DIGITO : ( '0'..'9' ) ;

TEXTO : '"'(LETRA | DIGITO | ESPACOBRANCO )*'"';


ESPACOBRANCO :( '\t' | ' ' | '\r' | '\n' | '\u000C' )+ 	-> skip ;
COMENTARIOLINHA : '//' (~('\n'|'\r'))*   -> skip   ;
COMENTARIOMULTIPLO : '/*' (LETRA | DIGITO | ESPACOBRANCO)* '*/' -> skip ;


IDENTIFICADOR : LETRA (LETRA | DIGITO | '_')* ;


// ANALISADOR SINTATICO

principal : importar? ( escopo | (PRINCIPAL E_CHAVE (logica_da_aplicacao) D_CHAVE ))+ ;

importar : IMPORTE IDENTIFICADOR PONTOVIRGULA;

escopo   : CLASSE IDENTIFICADOR E_CHAVE ( ( VISIBILIDADE atribuicao PONTOVIRGULA | funcao_declaracao ) )* D_CHAVE ;

digito   : DIGITO+ (PONTO (DIGITO)*)?;

expressao
	: expressao_matematica
	| E_PARENTESES expressao_matematica D_PARENTESES 
	| expressao_matematica OPERADORES_MATEMATICOS expressao
	| expressao OPERADORES_MATEMATICOS expressao ;

expressao_matematica : (( IDENTIFICADOR (MAISMAIS | MENOSMENOS)? ) | LITERAL ) (OPERADORES_MATEMATICOS (( IDENTIFICADOR (MAISMAIS | MENOSMENOS)? ) | LITERAL ))* ; 


atribuicao : (QUALIFICADOR? TIPO)? IDENTIFICADOR (ATRIBUICAO (expressao | ternario | TEXTO | chamada_funcao_classe | chamada_funcao_servico ) )? ;

atribuicao_classe : IDENTIFICADOR IDENTIFICADOR ATRIBUICAO NOVO E_PARENTESES ((TIPO IDENTIFICADOR VIRGULA)* (TIPO IDENTIFICADOR))? D_PARENTESES ;

atribuicao_ternario : (QUALIFICADOR? TIPO)? IDENTIFICADOR ATRIBUICAO ternario;


operacao_matematica : (digito | IDENTIFICADOR) ((OPERADORES_MATEMATICOS operacao_matematica)* | MAISMAIS | MENOSMENOS);

funcao_declaracao : VISIBILIDADE TIPO? FUNCAO IDENTIFICADOR E_PARENTESES ((TIPO IDENTIFICADOR VIRGULA)* (TIPO IDENTIFICADOR))? D_PARENTESES E_CHAVE 
         ( logica_da_aplicacao )* 
         D_CHAVE;

condicao: ( NAO E_PARENTESES NAO? (IDENTIFICADOR |LITERAL | TEXTO) (OPERADORES_COMPARACAO NAO?(IDENTIFICADOR | LITERAL | TEXTO))? D_PARENTESES |
          NAO?(IDENTIFICADOR | LITERAL | TEXTO) (OPERADORES_COMPARACAO NAO?(IDENTIFICADOR | LITERAL | TEXTO))? (OPERADORES_LOGICOS condicao)*);

se : SE E_PARENTESES (condicao) D_PARENTESES E_CHAVE
     (logica_da_aplicacao)*
     D_CHAVE (SENAO E_CHAVE (logica_da_aplicacao)* D_CHAVE)?;

para : PARA E_PARENTESES atribuicao PONTOVIRGULA condicao PONTOVIRGULA operacao_matematica D_PARENTESES E_CHAVE (logica_da_aplicacao)+ D_CHAVE ;

enquanto : ENQUANTO E_PARENTESES condicao D_PARENTESES E_CHAVE (logica_da_aplicacao)+ D_CHAVE;

caso: CASO ( LITERAL | TEXTO ) DOISPONTOS (logica_da_aplicacao )*;
      
escolha : ESCOLHA E_PARENTESES IDENTIFICADOR D_PARENTESES E_CHAVE 
          caso+
          D_CHAVE;



chamada_funcao_classe : IDENTIFICADOR PONTO IDENTIFICADOR E_PARENTESES ((IDENTIFICADOR | LITERAL) (VIRGULA (IDENTIFICADOR | LITERAL))*)? D_PARENTESES ;

chamada_funcao_servico : IDENTIFICADOR SETA IDENTIFICADOR E_PARENTESES ((IDENTIFICADOR | LITERAL) (VIRGULA (IDENTIFICADOR | LITERAL))*)? D_PARENTESES ;

logica_da_aplicacao : ( chamada_funcao_classe
	| chamada_funcao_servico 
	| atribuicao
	| atribuicao_classe 
	| atribuicao_ternario
	| PARAR
	| RETORNO (LITERAL | IDENTIFICADOR | TEXTO ) ) PONTOVIRGULA 
	| ( se | para | enquanto | escolha ) ;

ternario 
	: ( IDENTIFICADOR | condicao ) '?' ( expressao | chamada_funcao_classe | chamada_funcao_classe)* DOISPONTOS (( expressao | chamada_funcao_classe | chamada_funcao_classe)* | PONTOVIRGULA );