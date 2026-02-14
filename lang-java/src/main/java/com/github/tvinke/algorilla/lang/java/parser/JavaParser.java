// Generated from JavaParser.g4 by ANTLR 4.13.2
package com.github.tvinke.algorilla.lang.java.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class JavaParser extends JavaParserBase {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ABSTRACT=1, ASSERT=2, BOOLEAN=3, BREAK=4, BYTE=5, CASE=6, CATCH=7, CHAR=8, 
		CLASS=9, CONST=10, CONTINUE=11, DEFAULT=12, DO=13, DOUBLE=14, ELSE=15, 
		ENUM=16, EXPORTS=17, EXTENDS=18, FINAL=19, FINALLY=20, FLOAT=21, FOR=22, 
		GOTO=23, IF=24, IMPLEMENTS=25, IMPORT=26, INSTANCEOF=27, INT=28, INTERFACE=29, 
		LONG=30, MODULE=31, NATIVE=32, NEW=33, NON_SEALED=34, OPEN=35, OPENS=36, 
		PACKAGE=37, PERMITS=38, PRIVATE=39, PROTECTED=40, PROVIDES=41, PUBLIC=42, 
		RECORD=43, REQUIRES=44, RETURN=45, SEALED=46, SHORT=47, STATIC=48, STRICTFP=49, 
		SUPER=50, SWITCH=51, SYNCHRONIZED=52, THIS=53, THROW=54, THROWS=55, TO=56, 
		TRANSIENT=57, TRANSITIVE=58, TRY=59, USES=60, VAR=61, VOID=62, VOLATILE=63, 
		WHEN=64, WHILE=65, WITH=66, YIELD=67, DECIMAL_LITERAL=68, HEX_LITERAL=69, 
		OCT_LITERAL=70, BINARY_LITERAL=71, FLOAT_LITERAL=72, HEX_FLOAT_LITERAL=73, 
		BOOL_LITERAL=74, CHAR_LITERAL=75, STRING_LITERAL=76, TEXT_BLOCK=77, NULL_LITERAL=78, 
		LPAREN=79, RPAREN=80, LBRACE=81, RBRACE=82, LBRACK=83, RBRACK=84, SEMI=85, 
		COMMA=86, DOT=87, ASSIGN=88, GT=89, LT=90, BANG=91, TILDE=92, QUESTION=93, 
		COLON=94, EQUAL=95, LE=96, GE=97, NOTEQUAL=98, AND=99, OR=100, INC=101, 
		DEC=102, ADD=103, SUB=104, MUL=105, DIV=106, BITAND=107, BITOR=108, CARET=109, 
		MOD=110, ADD_ASSIGN=111, SUB_ASSIGN=112, MUL_ASSIGN=113, DIV_ASSIGN=114, 
		AND_ASSIGN=115, OR_ASSIGN=116, XOR_ASSIGN=117, MOD_ASSIGN=118, LSHIFT_ASSIGN=119, 
		RSHIFT_ASSIGN=120, URSHIFT_ASSIGN=121, ARROW=122, COLONCOLON=123, AT=124, 
		ELLIPSIS=125, WS=126, COMMENT=127, LINE_COMMENT=128, IDENTIFIER=129;
	public static final int
		RULE_compilationUnit = 0, RULE_modularCompulationUnit = 1, RULE_packageDeclaration = 2, 
		RULE_importDeclaration = 3, RULE_typeDeclaration = 4, RULE_modifier = 5, 
		RULE_classOrInterfaceModifier = 6, RULE_variableModifier = 7, RULE_classDeclaration = 8, 
		RULE_typeParameters = 9, RULE_typeParameter = 10, RULE_typeBound = 11, 
		RULE_enumDeclaration = 12, RULE_enumConstants = 13, RULE_enumConstant = 14, 
		RULE_enumBodyDeclarations = 15, RULE_interfaceDeclaration = 16, RULE_classBody = 17, 
		RULE_interfaceBody = 18, RULE_classBodyDeclaration = 19, RULE_memberDeclaration = 20, 
		RULE_methodDeclaration = 21, RULE_methodBody = 22, RULE_typeTypeOrVoid = 23, 
		RULE_genericMethodDeclaration = 24, RULE_genericConstructorDeclaration = 25, 
		RULE_constructorDeclaration = 26, RULE_compactConstructorDeclaration = 27, 
		RULE_fieldDeclaration = 28, RULE_interfaceBodyDeclaration = 29, RULE_interfaceMemberDeclaration = 30, 
		RULE_constDeclaration = 31, RULE_constantDeclarator = 32, RULE_interfaceMethodDeclaration = 33, 
		RULE_interfaceMethodModifier = 34, RULE_genericInterfaceMethodDeclaration = 35, 
		RULE_interfaceCommonBodyDeclaration = 36, RULE_variableDeclarators = 37, 
		RULE_variableDeclarator = 38, RULE_variableDeclaratorId = 39, RULE_variableInitializer = 40, 
		RULE_arrayInitializer = 41, RULE_classType = 42, RULE_packageName = 43, 
		RULE_typeArgument = 44, RULE_qualifiedNameList = 45, RULE_formalParameters = 46, 
		RULE_receiverParameter = 47, RULE_formalParameterList = 48, RULE_formalParameter = 49, 
		RULE_lambdaLVTIList = 50, RULE_lambdaLVTIParameter = 51, RULE_qualifiedName = 52, 
		RULE_literal = 53, RULE_integerLiteral = 54, RULE_floatLiteral = 55, RULE_altAnnotationQualifiedName = 56, 
		RULE_annotation = 57, RULE_annotationFieldValues = 58, RULE_annotationFieldValue = 59, 
		RULE_annotationValue = 60, RULE_elementValue = 61, RULE_elementValueArrayInitializer = 62, 
		RULE_annotationTypeDeclaration = 63, RULE_annotationTypeBody = 64, RULE_annotationTypeElementDeclaration = 65, 
		RULE_annotationTypeElementRest = 66, RULE_annotationMethodOrConstantRest = 67, 
		RULE_annotationMethodRest = 68, RULE_annotationConstantRest = 69, RULE_defaultValue = 70, 
		RULE_moduleDeclaration = 71, RULE_moduleDirective = 72, RULE_requiresModifier = 73, 
		RULE_recordDeclaration = 74, RULE_recordHeader = 75, RULE_recordComponentList = 76, 
		RULE_recordComponent = 77, RULE_recordBody = 78, RULE_block = 79, RULE_blockStatement = 80, 
		RULE_localVariableDeclaration = 81, RULE_identifier = 82, RULE_typeIdentifier = 83, 
		RULE_localTypeDeclaration = 84, RULE_statement = 85, RULE_catchClause = 86, 
		RULE_catchType = 87, RULE_finallyBlock = 88, RULE_resourceSpecification = 89, 
		RULE_resources = 90, RULE_resource = 91, RULE_switchBlockStatementGroup = 92, 
		RULE_switchLabel = 93, RULE_forControl = 94, RULE_forInit = 95, RULE_enhancedForControl = 96, 
		RULE_expressionList = 97, RULE_methodCall = 98, RULE_expression = 99, 
		RULE_pattern = 100, RULE_componentPatternList = 101, RULE_componentPattern = 102, 
		RULE_lambdaExpression = 103, RULE_lambdaParameters = 104, RULE_lambdaBody = 105, 
		RULE_primary = 106, RULE_switchExpression = 107, RULE_switchLabeledRule = 108, 
		RULE_guard = 109, RULE_casePattern = 110, RULE_switchRuleOutcome = 111, 
		RULE_classOrInterfaceType = 112, RULE_creator = 113, RULE_createdName = 114, 
		RULE_innerCreator = 115, RULE_arrayCreatorRest = 116, RULE_classCreatorRest = 117, 
		RULE_explicitGenericInvocation = 118, RULE_typeArgumentsOrDiamond = 119, 
		RULE_nonWildcardTypeArgumentsOrDiamond = 120, RULE_nonWildcardTypeArguments = 121, 
		RULE_typeList = 122, RULE_typeType = 123, RULE_primitiveType = 124, RULE_typeArguments = 125, 
		RULE_superSuffix = 126, RULE_explicitGenericInvocationSuffix = 127, RULE_arguments = 128;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "modularCompulationUnit", "packageDeclaration", "importDeclaration", 
			"typeDeclaration", "modifier", "classOrInterfaceModifier", "variableModifier", 
			"classDeclaration", "typeParameters", "typeParameter", "typeBound", "enumDeclaration", 
			"enumConstants", "enumConstant", "enumBodyDeclarations", "interfaceDeclaration", 
			"classBody", "interfaceBody", "classBodyDeclaration", "memberDeclaration", 
			"methodDeclaration", "methodBody", "typeTypeOrVoid", "genericMethodDeclaration", 
			"genericConstructorDeclaration", "constructorDeclaration", "compactConstructorDeclaration", 
			"fieldDeclaration", "interfaceBodyDeclaration", "interfaceMemberDeclaration", 
			"constDeclaration", "constantDeclarator", "interfaceMethodDeclaration", 
			"interfaceMethodModifier", "genericInterfaceMethodDeclaration", "interfaceCommonBodyDeclaration", 
			"variableDeclarators", "variableDeclarator", "variableDeclaratorId", 
			"variableInitializer", "arrayInitializer", "classType", "packageName", 
			"typeArgument", "qualifiedNameList", "formalParameters", "receiverParameter", 
			"formalParameterList", "formalParameter", "lambdaLVTIList", "lambdaLVTIParameter", 
			"qualifiedName", "literal", "integerLiteral", "floatLiteral", "altAnnotationQualifiedName", 
			"annotation", "annotationFieldValues", "annotationFieldValue", "annotationValue", 
			"elementValue", "elementValueArrayInitializer", "annotationTypeDeclaration", 
			"annotationTypeBody", "annotationTypeElementDeclaration", "annotationTypeElementRest", 
			"annotationMethodOrConstantRest", "annotationMethodRest", "annotationConstantRest", 
			"defaultValue", "moduleDeclaration", "moduleDirective", "requiresModifier", 
			"recordDeclaration", "recordHeader", "recordComponentList", "recordComponent", 
			"recordBody", "block", "blockStatement", "localVariableDeclaration", 
			"identifier", "typeIdentifier", "localTypeDeclaration", "statement", 
			"catchClause", "catchType", "finallyBlock", "resourceSpecification", 
			"resources", "resource", "switchBlockStatementGroup", "switchLabel", 
			"forControl", "forInit", "enhancedForControl", "expressionList", "methodCall", 
			"expression", "pattern", "componentPatternList", "componentPattern", 
			"lambdaExpression", "lambdaParameters", "lambdaBody", "primary", "switchExpression", 
			"switchLabeledRule", "guard", "casePattern", "switchRuleOutcome", "classOrInterfaceType", 
			"creator", "createdName", "innerCreator", "arrayCreatorRest", "classCreatorRest", 
			"explicitGenericInvocation", "typeArgumentsOrDiamond", "nonWildcardTypeArgumentsOrDiamond", 
			"nonWildcardTypeArguments", "typeList", "typeType", "primitiveType", 
			"typeArguments", "superSuffix", "explicitGenericInvocationSuffix", "arguments"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'abstract'", "'assert'", "'boolean'", "'break'", "'byte'", "'case'", 
			"'catch'", "'char'", "'class'", "'const'", "'continue'", "'default'", 
			"'do'", "'double'", "'else'", "'enum'", "'exports'", "'extends'", "'final'", 
			"'finally'", "'float'", "'for'", "'goto'", "'if'", "'implements'", "'import'", 
			"'instanceof'", "'int'", "'interface'", "'long'", "'module'", "'native'", 
			"'new'", "'non-sealed'", "'open'", "'opens'", "'package'", "'permits'", 
			"'private'", "'protected'", "'provides'", "'public'", "'record'", "'requires'", 
			"'return'", "'sealed'", "'short'", "'static'", "'strictfp'", "'super'", 
			"'switch'", "'synchronized'", "'this'", "'throw'", "'throws'", "'to'", 
			"'transient'", "'transitive'", "'try'", "'uses'", "'var'", "'void'", 
			"'volatile'", "'when'", "'while'", "'with'", "'yield'", null, null, null, 
			null, null, null, null, null, null, null, "'null'", "'('", "')'", "'{'", 
			"'}'", "'['", "']'", "';'", "','", "'.'", "'='", "'>'", "'<'", "'!'", 
			"'~'", "'?'", "':'", "'=='", "'<='", "'>='", "'!='", "'&&'", "'||'", 
			"'++'", "'--'", "'+'", "'-'", "'*'", "'/'", "'&'", "'|'", "'^'", "'%'", 
			"'+='", "'-='", "'*='", "'/='", "'&='", "'|='", "'^='", "'%='", "'<<='", 
			"'>>='", "'>>>='", "'->'", "'::'", "'@'", "'...'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ABSTRACT", "ASSERT", "BOOLEAN", "BREAK", "BYTE", "CASE", "CATCH", 
			"CHAR", "CLASS", "CONST", "CONTINUE", "DEFAULT", "DO", "DOUBLE", "ELSE", 
			"ENUM", "EXPORTS", "EXTENDS", "FINAL", "FINALLY", "FLOAT", "FOR", "GOTO", 
			"IF", "IMPLEMENTS", "IMPORT", "INSTANCEOF", "INT", "INTERFACE", "LONG", 
			"MODULE", "NATIVE", "NEW", "NON_SEALED", "OPEN", "OPENS", "PACKAGE", 
			"PERMITS", "PRIVATE", "PROTECTED", "PROVIDES", "PUBLIC", "RECORD", "REQUIRES", 
			"RETURN", "SEALED", "SHORT", "STATIC", "STRICTFP", "SUPER", "SWITCH", 
			"SYNCHRONIZED", "THIS", "THROW", "THROWS", "TO", "TRANSIENT", "TRANSITIVE", 
			"TRY", "USES", "VAR", "VOID", "VOLATILE", "WHEN", "WHILE", "WITH", "YIELD", 
			"DECIMAL_LITERAL", "HEX_LITERAL", "OCT_LITERAL", "BINARY_LITERAL", "FLOAT_LITERAL", 
			"HEX_FLOAT_LITERAL", "BOOL_LITERAL", "CHAR_LITERAL", "STRING_LITERAL", 
			"TEXT_BLOCK", "NULL_LITERAL", "LPAREN", "RPAREN", "LBRACE", "RBRACE", 
			"LBRACK", "RBRACK", "SEMI", "COMMA", "DOT", "ASSIGN", "GT", "LT", "BANG", 
			"TILDE", "QUESTION", "COLON", "EQUAL", "LE", "GE", "NOTEQUAL", "AND", 
			"OR", "INC", "DEC", "ADD", "SUB", "MUL", "DIV", "BITAND", "BITOR", "CARET", 
			"MOD", "ADD_ASSIGN", "SUB_ASSIGN", "MUL_ASSIGN", "DIV_ASSIGN", "AND_ASSIGN", 
			"OR_ASSIGN", "XOR_ASSIGN", "MOD_ASSIGN", "LSHIFT_ASSIGN", "RSHIFT_ASSIGN", 
			"URSHIFT_ASSIGN", "ARROW", "COLONCOLON", "AT", "ELLIPSIS", "WS", "COMMENT", 
			"LINE_COMMENT", "IDENTIFIER"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "JavaParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public JavaParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilationUnitContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(JavaParser.EOF, 0); }
		public PackageDeclarationContext packageDeclaration() {
			return getRuleContext(PackageDeclarationContext.class,0);
		}
		public List<ImportDeclarationContext> importDeclaration() {
			return getRuleContexts(ImportDeclarationContext.class);
		}
		public ImportDeclarationContext importDeclaration(int i) {
			return getRuleContext(ImportDeclarationContext.class,i);
		}
		public List<TerminalNode> SEMI() { return getTokens(JavaParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(JavaParser.SEMI, i);
		}
		public List<TypeDeclarationContext> typeDeclaration() {
			return getRuleContexts(TypeDeclarationContext.class);
		}
		public TypeDeclarationContext typeDeclaration(int i) {
			return getRuleContext(TypeDeclarationContext.class,i);
		}
		public ModularCompulationUnitContext modularCompulationUnit() {
			return getRuleContext(ModularCompulationUnitContext.class,0);
		}
		public CompilationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilationUnit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitCompilationUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompilationUnitContext compilationUnit() throws RecognitionException {
		CompilationUnitContext _localctx = new CompilationUnitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_compilationUnit);
		int _la;
		try {
			int _alt;
			setState(279);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(259);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(258);
					packageDeclaration();
					}
					break;
				}
				setState(265);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						setState(263);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case IMPORT:
							{
							setState(261);
							importDeclaration();
							}
							break;
						case SEMI:
							{
							setState(262);
							match(SEMI);
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						} 
					}
					setState(267);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				}
				setState(272);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 929654798615042L) != 0) || _la==SEMI || _la==AT) {
					{
					setState(270);
					_errHandler.sync(this);
					switch (_input.LA(1)) {
					case ABSTRACT:
					case CLASS:
					case ENUM:
					case FINAL:
					case INTERFACE:
					case NON_SEALED:
					case PRIVATE:
					case PROTECTED:
					case PUBLIC:
					case RECORD:
					case SEALED:
					case STATIC:
					case STRICTFP:
					case AT:
						{
						setState(268);
						typeDeclaration();
						}
						break;
					case SEMI:
						{
						setState(269);
						match(SEMI);
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					}
					setState(274);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(275);
				match(EOF);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(276);
				modularCompulationUnit();
				setState(277);
				match(EOF);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModularCompulationUnitContext extends ParserRuleContext {
		public ModuleDeclarationContext moduleDeclaration() {
			return getRuleContext(ModuleDeclarationContext.class,0);
		}
		public List<ImportDeclarationContext> importDeclaration() {
			return getRuleContexts(ImportDeclarationContext.class);
		}
		public ImportDeclarationContext importDeclaration(int i) {
			return getRuleContext(ImportDeclarationContext.class,i);
		}
		public ModularCompulationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modularCompulationUnit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitModularCompulationUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModularCompulationUnitContext modularCompulationUnit() throws RecognitionException {
		ModularCompulationUnitContext _localctx = new ModularCompulationUnitContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_modularCompulationUnit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(284);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(281);
				importDeclaration();
				}
				}
				setState(286);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(287);
			moduleDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PackageDeclarationContext extends ParserRuleContext {
		public TerminalNode PACKAGE() { return getToken(JavaParser.PACKAGE, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public PackageDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitPackageDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PackageDeclarationContext packageDeclaration() throws RecognitionException {
		PackageDeclarationContext _localctx = new PackageDeclarationContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_packageDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(292);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(289);
				annotation();
				}
				}
				setState(294);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(295);
			match(PACKAGE);
			setState(296);
			qualifiedName();
			setState(297);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ImportDeclarationContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(JavaParser.IMPORT, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public TerminalNode STATIC() { return getToken(JavaParser.STATIC, 0); }
		public TerminalNode DOT() { return getToken(JavaParser.DOT, 0); }
		public TerminalNode MUL() { return getToken(JavaParser.MUL, 0); }
		public ImportDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitImportDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImportDeclarationContext importDeclaration() throws RecognitionException {
		ImportDeclarationContext _localctx = new ImportDeclarationContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_importDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			match(IMPORT);
			setState(301);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STATIC) {
				{
				setState(300);
				match(STATIC);
				}
			}

			setState(303);
			qualifiedName();
			setState(306);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DOT) {
				{
				setState(304);
				match(DOT);
				setState(305);
				match(MUL);
				}
			}

			setState(308);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeDeclarationContext extends ParserRuleContext {
		public ClassDeclarationContext classDeclaration() {
			return getRuleContext(ClassDeclarationContext.class,0);
		}
		public EnumDeclarationContext enumDeclaration() {
			return getRuleContext(EnumDeclarationContext.class,0);
		}
		public InterfaceDeclarationContext interfaceDeclaration() {
			return getRuleContext(InterfaceDeclarationContext.class,0);
		}
		public AnnotationTypeDeclarationContext annotationTypeDeclaration() {
			return getRuleContext(AnnotationTypeDeclarationContext.class,0);
		}
		public RecordDeclarationContext recordDeclaration() {
			return getRuleContext(RecordDeclarationContext.class,0);
		}
		public List<ClassOrInterfaceModifierContext> classOrInterfaceModifier() {
			return getRuleContexts(ClassOrInterfaceModifierContext.class);
		}
		public ClassOrInterfaceModifierContext classOrInterfaceModifier(int i) {
			return getRuleContext(ClassOrInterfaceModifierContext.class,i);
		}
		public TypeDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeDeclarationContext typeDeclaration() throws RecognitionException {
		TypeDeclarationContext _localctx = new TypeDeclarationContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_typeDeclaration);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(313);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(310);
					classOrInterfaceModifier();
					}
					} 
				}
				setState(315);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			setState(321);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CLASS:
				{
				setState(316);
				classDeclaration();
				}
				break;
			case ENUM:
				{
				setState(317);
				enumDeclaration();
				}
				break;
			case INTERFACE:
				{
				setState(318);
				interfaceDeclaration();
				}
				break;
			case AT:
				{
				setState(319);
				annotationTypeDeclaration();
				}
				break;
			case RECORD:
				{
				setState(320);
				recordDeclaration();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModifierContext extends ParserRuleContext {
		public ClassOrInterfaceModifierContext classOrInterfaceModifier() {
			return getRuleContext(ClassOrInterfaceModifierContext.class,0);
		}
		public TerminalNode NATIVE() { return getToken(JavaParser.NATIVE, 0); }
		public TerminalNode SYNCHRONIZED() { return getToken(JavaParser.SYNCHRONIZED, 0); }
		public TerminalNode TRANSIENT() { return getToken(JavaParser.TRANSIENT, 0); }
		public TerminalNode VOLATILE() { return getToken(JavaParser.VOLATILE, 0); }
		public ModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModifierContext modifier() throws RecognitionException {
		ModifierContext _localctx = new ModifierContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_modifier);
		try {
			setState(328);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case FINAL:
			case NON_SEALED:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case SEALED:
			case STATIC:
			case STRICTFP:
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(323);
				classOrInterfaceModifier();
				}
				break;
			case NATIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(324);
				match(NATIVE);
				}
				break;
			case SYNCHRONIZED:
				enterOuterAlt(_localctx, 3);
				{
				setState(325);
				match(SYNCHRONIZED);
				}
				break;
			case TRANSIENT:
				enterOuterAlt(_localctx, 4);
				{
				setState(326);
				match(TRANSIENT);
				}
				break;
			case VOLATILE:
				enterOuterAlt(_localctx, 5);
				{
				setState(327);
				match(VOLATILE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassOrInterfaceModifierContext extends ParserRuleContext {
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public TerminalNode PUBLIC() { return getToken(JavaParser.PUBLIC, 0); }
		public TerminalNode PROTECTED() { return getToken(JavaParser.PROTECTED, 0); }
		public TerminalNode PRIVATE() { return getToken(JavaParser.PRIVATE, 0); }
		public TerminalNode STATIC() { return getToken(JavaParser.STATIC, 0); }
		public TerminalNode ABSTRACT() { return getToken(JavaParser.ABSTRACT, 0); }
		public TerminalNode FINAL() { return getToken(JavaParser.FINAL, 0); }
		public TerminalNode STRICTFP() { return getToken(JavaParser.STRICTFP, 0); }
		public TerminalNode SEALED() { return getToken(JavaParser.SEALED, 0); }
		public TerminalNode NON_SEALED() { return getToken(JavaParser.NON_SEALED, 0); }
		public ClassOrInterfaceModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceModifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitClassOrInterfaceModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassOrInterfaceModifierContext classOrInterfaceModifier() throws RecognitionException {
		ClassOrInterfaceModifierContext _localctx = new ClassOrInterfaceModifierContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_classOrInterfaceModifier);
		try {
			setState(340);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(330);
				annotation();
				}
				break;
			case PUBLIC:
				enterOuterAlt(_localctx, 2);
				{
				setState(331);
				match(PUBLIC);
				}
				break;
			case PROTECTED:
				enterOuterAlt(_localctx, 3);
				{
				setState(332);
				match(PROTECTED);
				}
				break;
			case PRIVATE:
				enterOuterAlt(_localctx, 4);
				{
				setState(333);
				match(PRIVATE);
				}
				break;
			case STATIC:
				enterOuterAlt(_localctx, 5);
				{
				setState(334);
				match(STATIC);
				}
				break;
			case ABSTRACT:
				enterOuterAlt(_localctx, 6);
				{
				setState(335);
				match(ABSTRACT);
				}
				break;
			case FINAL:
				enterOuterAlt(_localctx, 7);
				{
				setState(336);
				match(FINAL);
				}
				break;
			case STRICTFP:
				enterOuterAlt(_localctx, 8);
				{
				setState(337);
				match(STRICTFP);
				}
				break;
			case SEALED:
				enterOuterAlt(_localctx, 9);
				{
				setState(338);
				match(SEALED);
				}
				break;
			case NON_SEALED:
				enterOuterAlt(_localctx, 10);
				{
				setState(339);
				match(NON_SEALED);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableModifierContext extends ParserRuleContext {
		public TerminalNode FINAL() { return getToken(JavaParser.FINAL, 0); }
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public VariableModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableModifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitVariableModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableModifierContext variableModifier() throws RecognitionException {
		VariableModifierContext _localctx = new VariableModifierContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_variableModifier);
		try {
			setState(344);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FINAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(342);
				match(FINAL);
				}
				break;
			case AT:
				enterOuterAlt(_localctx, 2);
				{
				setState(343);
				annotation();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassDeclarationContext extends ParserRuleContext {
		public TerminalNode CLASS() { return getToken(JavaParser.CLASS, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ClassBodyContext classBody() {
			return getRuleContext(ClassBodyContext.class,0);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode EXTENDS() { return getToken(JavaParser.EXTENDS, 0); }
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode IMPLEMENTS() { return getToken(JavaParser.IMPLEMENTS, 0); }
		public List<TypeListContext> typeList() {
			return getRuleContexts(TypeListContext.class);
		}
		public TypeListContext typeList(int i) {
			return getRuleContext(TypeListContext.class,i);
		}
		public TerminalNode PERMITS() { return getToken(JavaParser.PERMITS, 0); }
		public ClassDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitClassDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassDeclarationContext classDeclaration() throws RecognitionException {
		ClassDeclarationContext _localctx = new ClassDeclarationContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_classDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(346);
			match(CLASS);
			setState(347);
			identifier();
			setState(349);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(348);
				typeParameters();
				}
			}

			setState(353);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(351);
				match(EXTENDS);
				setState(352);
				typeType();
				}
			}

			setState(357);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(355);
				match(IMPLEMENTS);
				setState(356);
				typeList();
				}
			}

			setState(361);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PERMITS) {
				{
				setState(359);
				match(PERMITS);
				setState(360);
				typeList();
				}
			}

			setState(363);
			classBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeParametersContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(JavaParser.LT, 0); }
		public List<TypeParameterContext> typeParameter() {
			return getRuleContexts(TypeParameterContext.class);
		}
		public TypeParameterContext typeParameter(int i) {
			return getRuleContext(TypeParameterContext.class,i);
		}
		public TerminalNode GT() { return getToken(JavaParser.GT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public TypeParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameters; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeParametersContext typeParameters() throws RecognitionException {
		TypeParametersContext _localctx = new TypeParametersContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_typeParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(365);
			match(LT);
			setState(366);
			typeParameter();
			setState(371);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(367);
				match(COMMA);
				setState(368);
				typeParameter();
				}
				}
				setState(373);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(374);
			match(GT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeParameterContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TerminalNode EXTENDS() { return getToken(JavaParser.EXTENDS, 0); }
		public TypeBoundContext typeBound() {
			return getRuleContext(TypeBoundContext.class,0);
		}
		public TypeParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeParameter; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeParameterContext typeParameter() throws RecognitionException {
		TypeParameterContext _localctx = new TypeParameterContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_typeParameter);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(376);
				annotation();
				}
				}
				setState(381);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(382);
			identifier();
			setState(391);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(383);
				match(EXTENDS);
				setState(387);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(384);
						annotation();
						}
						} 
					}
					setState(389);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				}
				setState(390);
				typeBound();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeBoundContext extends ParserRuleContext {
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public List<TerminalNode> BITAND() { return getTokens(JavaParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(JavaParser.BITAND, i);
		}
		public TypeBoundContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeBound; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeBound(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeBoundContext typeBound() throws RecognitionException {
		TypeBoundContext _localctx = new TypeBoundContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_typeBound);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(393);
			typeType();
			setState(398);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITAND) {
				{
				{
				setState(394);
				match(BITAND);
				setState(395);
				typeType();
				}
				}
				setState(400);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumDeclarationContext extends ParserRuleContext {
		public TerminalNode ENUM() { return getToken(JavaParser.ENUM, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public TerminalNode IMPLEMENTS() { return getToken(JavaParser.IMPLEMENTS, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public EnumConstantsContext enumConstants() {
			return getRuleContext(EnumConstantsContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(JavaParser.COMMA, 0); }
		public EnumBodyDeclarationsContext enumBodyDeclarations() {
			return getRuleContext(EnumBodyDeclarationsContext.class,0);
		}
		public EnumDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitEnumDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumDeclarationContext enumDeclaration() throws RecognitionException {
		EnumDeclarationContext _localctx = new EnumDeclarationContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_enumDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(401);
			match(ENUM);
			setState(402);
			identifier();
			setState(405);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(403);
				match(IMPLEMENTS);
				setState(404);
				typeList();
				}
			}

			setState(407);
			match(LBRACE);
			setState(409);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 17)) & ~0x3f) == 0 && ((1L << (_la - 17)) & 1858725164630017L) != 0) || _la==AT || _la==IDENTIFIER) {
				{
				setState(408);
				enumConstants();
				}
			}

			setState(412);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(411);
				match(COMMA);
				}
			}

			setState(415);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(414);
				enumBodyDeclarations();
				}
			}

			setState(417);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumConstantsContext extends ParserRuleContext {
		public List<EnumConstantContext> enumConstant() {
			return getRuleContexts(EnumConstantContext.class);
		}
		public EnumConstantContext enumConstant(int i) {
			return getRuleContext(EnumConstantContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public EnumConstantsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumConstants; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitEnumConstants(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumConstantsContext enumConstants() throws RecognitionException {
		EnumConstantsContext _localctx = new EnumConstantsContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_enumConstants);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(419);
			enumConstant();
			setState(424);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(420);
					match(COMMA);
					setState(421);
					enumConstant();
					}
					} 
				}
				setState(426);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumConstantContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ClassBodyContext classBody() {
			return getRuleContext(ClassBodyContext.class,0);
		}
		public EnumConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumConstant; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitEnumConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumConstantContext enumConstant() throws RecognitionException {
		EnumConstantContext _localctx = new EnumConstantContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_enumConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(427);
				annotation();
				}
				}
				setState(432);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(433);
			identifier();
			setState(435);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(434);
				arguments();
				}
			}

			setState(438);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LBRACE) {
				{
				setState(437);
				classBody();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnumBodyDeclarationsContext extends ParserRuleContext {
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public List<ClassBodyDeclarationContext> classBodyDeclaration() {
			return getRuleContexts(ClassBodyDeclarationContext.class);
		}
		public ClassBodyDeclarationContext classBodyDeclaration(int i) {
			return getRuleContext(ClassBodyDeclarationContext.class,i);
		}
		public EnumBodyDeclarationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumBodyDeclarations; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitEnumBodyDeclarations(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumBodyDeclarationsContext enumBodyDeclarations() throws RecognitionException {
		EnumBodyDeclarationsContext _localctx = new EnumBodyDeclarationsContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_enumBodyDeclarations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(440);
			match(SEMI);
			setState(444);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 1)) & ~0x3f) == 0 && ((1L << (_la - 1)) & -321462088736857707L) != 0) || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & -8935141660685729789L) != 0)) {
				{
				{
				setState(441);
				classBodyDeclaration();
				}
				}
				setState(446);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceDeclarationContext extends ParserRuleContext {
		public TerminalNode INTERFACE() { return getToken(JavaParser.INTERFACE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public InterfaceBodyContext interfaceBody() {
			return getRuleContext(InterfaceBodyContext.class,0);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode EXTENDS() { return getToken(JavaParser.EXTENDS, 0); }
		public List<TypeListContext> typeList() {
			return getRuleContexts(TypeListContext.class);
		}
		public TypeListContext typeList(int i) {
			return getRuleContext(TypeListContext.class,i);
		}
		public TerminalNode PERMITS() { return getToken(JavaParser.PERMITS, 0); }
		public InterfaceDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitInterfaceDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceDeclarationContext interfaceDeclaration() throws RecognitionException {
		InterfaceDeclarationContext _localctx = new InterfaceDeclarationContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_interfaceDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(447);
			match(INTERFACE);
			setState(448);
			identifier();
			setState(450);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(449);
				typeParameters();
				}
			}

			setState(454);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXTENDS) {
				{
				setState(452);
				match(EXTENDS);
				setState(453);
				typeList();
				}
			}

			setState(458);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PERMITS) {
				{
				setState(456);
				match(PERMITS);
				setState(457);
				typeList();
				}
			}

			setState(460);
			interfaceBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassBodyContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<ClassBodyDeclarationContext> classBodyDeclaration() {
			return getRuleContexts(ClassBodyDeclarationContext.class);
		}
		public ClassBodyDeclarationContext classBodyDeclaration(int i) {
			return getRuleContext(ClassBodyDeclarationContext.class,i);
		}
		public ClassBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classBody; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitClassBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassBodyContext classBody() throws RecognitionException {
		ClassBodyContext _localctx = new ClassBodyContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_classBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(462);
			match(LBRACE);
			setState(466);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 1)) & ~0x3f) == 0 && ((1L << (_la - 1)) & -321462088736857707L) != 0) || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & -8935141660685729789L) != 0)) {
				{
				{
				setState(463);
				classBodyDeclaration();
				}
				}
				setState(468);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(469);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceBodyContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<InterfaceBodyDeclarationContext> interfaceBodyDeclaration() {
			return getRuleContexts(InterfaceBodyDeclarationContext.class);
		}
		public InterfaceBodyDeclarationContext interfaceBodyDeclaration(int i) {
			return getRuleContext(InterfaceBodyDeclarationContext.class,i);
		}
		public InterfaceBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceBody; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitInterfaceBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceBodyContext interfaceBody() throws RecognitionException {
		InterfaceBodyContext _localctx = new InterfaceBodyContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_interfaceBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(471);
			match(LBRACE);
			setState(475);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 1)) & ~0x3f) == 0 && ((1L << (_la - 1)) & -321462088736855659L) != 0) || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & -8935141660685762557L) != 0)) {
				{
				{
				setState(472);
				interfaceBodyDeclaration();
				}
				}
				setState(477);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(478);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassBodyDeclarationContext extends ParserRuleContext {
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode STATIC() { return getToken(JavaParser.STATIC, 0); }
		public MemberDeclarationContext memberDeclaration() {
			return getRuleContext(MemberDeclarationContext.class,0);
		}
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public ClassBodyDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classBodyDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitClassBodyDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassBodyDeclarationContext classBodyDeclaration() throws RecognitionException {
		ClassBodyDeclarationContext _localctx = new ClassBodyDeclarationContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_classBodyDeclaration);
		int _la;
		try {
			int _alt;
			setState(492);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(480);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(482);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==STATIC) {
					{
					setState(481);
					match(STATIC);
					}
				}

				setState(484);
				block();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(488);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(485);
						modifier();
						}
						} 
					}
					setState(490);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
				}
				setState(491);
				memberDeclaration();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MemberDeclarationContext extends ParserRuleContext {
		public RecordDeclarationContext recordDeclaration() {
			return getRuleContext(RecordDeclarationContext.class,0);
		}
		public MethodDeclarationContext methodDeclaration() {
			return getRuleContext(MethodDeclarationContext.class,0);
		}
		public GenericMethodDeclarationContext genericMethodDeclaration() {
			return getRuleContext(GenericMethodDeclarationContext.class,0);
		}
		public FieldDeclarationContext fieldDeclaration() {
			return getRuleContext(FieldDeclarationContext.class,0);
		}
		public ConstructorDeclarationContext constructorDeclaration() {
			return getRuleContext(ConstructorDeclarationContext.class,0);
		}
		public GenericConstructorDeclarationContext genericConstructorDeclaration() {
			return getRuleContext(GenericConstructorDeclarationContext.class,0);
		}
		public InterfaceDeclarationContext interfaceDeclaration() {
			return getRuleContext(InterfaceDeclarationContext.class,0);
		}
		public AnnotationTypeDeclarationContext annotationTypeDeclaration() {
			return getRuleContext(AnnotationTypeDeclarationContext.class,0);
		}
		public ClassDeclarationContext classDeclaration() {
			return getRuleContext(ClassDeclarationContext.class,0);
		}
		public EnumDeclarationContext enumDeclaration() {
			return getRuleContext(EnumDeclarationContext.class,0);
		}
		public MemberDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitMemberDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberDeclarationContext memberDeclaration() throws RecognitionException {
		MemberDeclarationContext _localctx = new MemberDeclarationContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_memberDeclaration);
		try {
			setState(504);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(494);
				recordDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(495);
				methodDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(496);
				genericMethodDeclaration();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(497);
				fieldDeclaration();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(498);
				constructorDeclaration();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(499);
				genericConstructorDeclaration();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(500);
				interfaceDeclaration();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(501);
				annotationTypeDeclaration();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(502);
				classDeclaration();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(503);
				enumDeclaration();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodDeclarationContext extends ParserRuleContext {
		public TypeTypeOrVoidContext typeTypeOrVoid() {
			return getRuleContext(TypeTypeOrVoidContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public MethodBodyContext methodBody() {
			return getRuleContext(MethodBodyContext.class,0);
		}
		public List<TerminalNode> LBRACK() { return getTokens(JavaParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(JavaParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(JavaParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(JavaParser.RBRACK, i);
		}
		public TerminalNode THROWS() { return getToken(JavaParser.THROWS, 0); }
		public QualifiedNameListContext qualifiedNameList() {
			return getRuleContext(QualifiedNameListContext.class,0);
		}
		public MethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitMethodDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodDeclarationContext methodDeclaration() throws RecognitionException {
		MethodDeclarationContext _localctx = new MethodDeclarationContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_methodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(506);
			typeTypeOrVoid();
			setState(507);
			identifier();
			setState(508);
			formalParameters();
			setState(513);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(509);
				match(LBRACK);
				setState(510);
				match(RBRACK);
				}
				}
				setState(515);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(518);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(516);
				match(THROWS);
				setState(517);
				qualifiedNameList();
				}
			}

			setState(520);
			methodBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodBodyContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public MethodBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodBody; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitMethodBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodBodyContext methodBody() throws RecognitionException {
		MethodBodyContext _localctx = new MethodBodyContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_methodBody);
		try {
			setState(524);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(522);
				block();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(523);
				match(SEMI);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeTypeOrVoidContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode VOID() { return getToken(JavaParser.VOID, 0); }
		public TypeTypeOrVoidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeTypeOrVoid; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeTypeOrVoid(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeTypeOrVoidContext typeTypeOrVoid() throws RecognitionException {
		TypeTypeOrVoidContext _localctx = new TypeTypeOrVoidContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_typeTypeOrVoid);
		try {
			setState(528);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case EXPORTS:
			case FLOAT:
			case INT:
			case LONG:
			case MODULE:
			case OPEN:
			case OPENS:
			case PERMITS:
			case PROVIDES:
			case RECORD:
			case REQUIRES:
			case SEALED:
			case SHORT:
			case TO:
			case TRANSITIVE:
			case USES:
			case VAR:
			case WHEN:
			case WITH:
			case YIELD:
			case AT:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(526);
				typeType();
				}
				break;
			case VOID:
				enterOuterAlt(_localctx, 2);
				{
				setState(527);
				match(VOID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GenericMethodDeclarationContext extends ParserRuleContext {
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public MethodDeclarationContext methodDeclaration() {
			return getRuleContext(MethodDeclarationContext.class,0);
		}
		public GenericMethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericMethodDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitGenericMethodDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericMethodDeclarationContext genericMethodDeclaration() throws RecognitionException {
		GenericMethodDeclarationContext _localctx = new GenericMethodDeclarationContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_genericMethodDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(530);
			typeParameters();
			setState(531);
			methodDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GenericConstructorDeclarationContext extends ParserRuleContext {
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public ConstructorDeclarationContext constructorDeclaration() {
			return getRuleContext(ConstructorDeclarationContext.class,0);
		}
		public GenericConstructorDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericConstructorDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitGenericConstructorDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericConstructorDeclarationContext genericConstructorDeclaration() throws RecognitionException {
		GenericConstructorDeclarationContext _localctx = new GenericConstructorDeclarationContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_genericConstructorDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(533);
			typeParameters();
			setState(534);
			constructorDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstructorDeclarationContext extends ParserRuleContext {
		public BlockContext constructorBody;
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode THROWS() { return getToken(JavaParser.THROWS, 0); }
		public QualifiedNameListContext qualifiedNameList() {
			return getRuleContext(QualifiedNameListContext.class,0);
		}
		public ConstructorDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constructorDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitConstructorDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstructorDeclarationContext constructorDeclaration() throws RecognitionException {
		ConstructorDeclarationContext _localctx = new ConstructorDeclarationContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_constructorDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(536);
			identifier();
			setState(537);
			formalParameters();
			setState(540);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(538);
				match(THROWS);
				setState(539);
				qualifiedNameList();
				}
			}

			setState(542);
			((ConstructorDeclarationContext)_localctx).constructorBody = block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompactConstructorDeclarationContext extends ParserRuleContext {
		public BlockContext constructorBody;
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public CompactConstructorDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compactConstructorDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitCompactConstructorDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompactConstructorDeclarationContext compactConstructorDeclaration() throws RecognitionException {
		CompactConstructorDeclarationContext _localctx = new CompactConstructorDeclarationContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_compactConstructorDeclaration);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(547);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(544);
					modifier();
					}
					} 
				}
				setState(549);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
			}
			setState(550);
			identifier();
			setState(551);
			((CompactConstructorDeclarationContext)_localctx).constructorBody = block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldDeclarationContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public VariableDeclaratorsContext variableDeclarators() {
			return getRuleContext(VariableDeclaratorsContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public FieldDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitFieldDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldDeclarationContext fieldDeclaration() throws RecognitionException {
		FieldDeclarationContext _localctx = new FieldDeclarationContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_fieldDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(553);
			typeType();
			setState(554);
			variableDeclarators();
			setState(555);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceBodyDeclarationContext extends ParserRuleContext {
		public InterfaceMemberDeclarationContext interfaceMemberDeclaration() {
			return getRuleContext(InterfaceMemberDeclarationContext.class,0);
		}
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public InterfaceBodyDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceBodyDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitInterfaceBodyDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceBodyDeclarationContext interfaceBodyDeclaration() throws RecognitionException {
		InterfaceBodyDeclarationContext _localctx = new InterfaceBodyDeclarationContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_interfaceBodyDeclaration);
		try {
			int _alt;
			setState(565);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case CLASS:
			case DEFAULT:
			case DOUBLE:
			case ENUM:
			case EXPORTS:
			case FINAL:
			case FLOAT:
			case INT:
			case INTERFACE:
			case LONG:
			case MODULE:
			case NATIVE:
			case NON_SEALED:
			case OPEN:
			case OPENS:
			case PERMITS:
			case PRIVATE:
			case PROTECTED:
			case PROVIDES:
			case PUBLIC:
			case RECORD:
			case REQUIRES:
			case SEALED:
			case SHORT:
			case STATIC:
			case STRICTFP:
			case SYNCHRONIZED:
			case TO:
			case TRANSIENT:
			case TRANSITIVE:
			case USES:
			case VAR:
			case VOID:
			case VOLATILE:
			case WHEN:
			case WITH:
			case YIELD:
			case LT:
			case AT:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(560);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(557);
						modifier();
						}
						} 
					}
					setState(562);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,48,_ctx);
				}
				setState(563);
				interfaceMemberDeclaration();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(564);
				match(SEMI);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceMemberDeclarationContext extends ParserRuleContext {
		public RecordDeclarationContext recordDeclaration() {
			return getRuleContext(RecordDeclarationContext.class,0);
		}
		public ConstDeclarationContext constDeclaration() {
			return getRuleContext(ConstDeclarationContext.class,0);
		}
		public InterfaceMethodDeclarationContext interfaceMethodDeclaration() {
			return getRuleContext(InterfaceMethodDeclarationContext.class,0);
		}
		public GenericInterfaceMethodDeclarationContext genericInterfaceMethodDeclaration() {
			return getRuleContext(GenericInterfaceMethodDeclarationContext.class,0);
		}
		public InterfaceDeclarationContext interfaceDeclaration() {
			return getRuleContext(InterfaceDeclarationContext.class,0);
		}
		public AnnotationTypeDeclarationContext annotationTypeDeclaration() {
			return getRuleContext(AnnotationTypeDeclarationContext.class,0);
		}
		public ClassDeclarationContext classDeclaration() {
			return getRuleContext(ClassDeclarationContext.class,0);
		}
		public EnumDeclarationContext enumDeclaration() {
			return getRuleContext(EnumDeclarationContext.class,0);
		}
		public InterfaceMemberDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceMemberDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitInterfaceMemberDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceMemberDeclarationContext interfaceMemberDeclaration() throws RecognitionException {
		InterfaceMemberDeclarationContext _localctx = new InterfaceMemberDeclarationContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_interfaceMemberDeclaration);
		try {
			setState(575);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(567);
				recordDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(568);
				constDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(569);
				interfaceMethodDeclaration();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(570);
				genericInterfaceMethodDeclaration();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(571);
				interfaceDeclaration();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(572);
				annotationTypeDeclaration();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(573);
				classDeclaration();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(574);
				enumDeclaration();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstDeclarationContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public List<ConstantDeclaratorContext> constantDeclarator() {
			return getRuleContexts(ConstantDeclaratorContext.class);
		}
		public ConstantDeclaratorContext constantDeclarator(int i) {
			return getRuleContext(ConstantDeclaratorContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public ConstDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitConstDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstDeclarationContext constDeclaration() throws RecognitionException {
		ConstDeclarationContext _localctx = new ConstDeclarationContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_constDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(577);
			typeType();
			setState(578);
			constantDeclarator();
			setState(583);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(579);
				match(COMMA);
				setState(580);
				constantDeclarator();
				}
				}
				setState(585);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(586);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstantDeclaratorContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(JavaParser.ASSIGN, 0); }
		public VariableInitializerContext variableInitializer() {
			return getRuleContext(VariableInitializerContext.class,0);
		}
		public List<TerminalNode> LBRACK() { return getTokens(JavaParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(JavaParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(JavaParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(JavaParser.RBRACK, i);
		}
		public ConstantDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constantDeclarator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitConstantDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantDeclaratorContext constantDeclarator() throws RecognitionException {
		ConstantDeclaratorContext _localctx = new ConstantDeclaratorContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_constantDeclarator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(588);
			identifier();
			setState(593);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(589);
				match(LBRACK);
				setState(590);
				match(RBRACK);
				}
				}
				setState(595);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(596);
			match(ASSIGN);
			setState(597);
			variableInitializer();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceMethodDeclarationContext extends ParserRuleContext {
		public InterfaceCommonBodyDeclarationContext interfaceCommonBodyDeclaration() {
			return getRuleContext(InterfaceCommonBodyDeclarationContext.class,0);
		}
		public List<InterfaceMethodModifierContext> interfaceMethodModifier() {
			return getRuleContexts(InterfaceMethodModifierContext.class);
		}
		public InterfaceMethodModifierContext interfaceMethodModifier(int i) {
			return getRuleContext(InterfaceMethodModifierContext.class,i);
		}
		public InterfaceMethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceMethodDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitInterfaceMethodDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceMethodDeclarationContext interfaceMethodDeclaration() throws RecognitionException {
		InterfaceMethodDeclarationContext _localctx = new InterfaceMethodDeclarationContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_interfaceMethodDeclaration);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(602);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(599);
					interfaceMethodModifier();
					}
					} 
				}
				setState(604);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,53,_ctx);
			}
			setState(605);
			interfaceCommonBodyDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceMethodModifierContext extends ParserRuleContext {
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public TerminalNode PUBLIC() { return getToken(JavaParser.PUBLIC, 0); }
		public TerminalNode ABSTRACT() { return getToken(JavaParser.ABSTRACT, 0); }
		public TerminalNode DEFAULT() { return getToken(JavaParser.DEFAULT, 0); }
		public TerminalNode STATIC() { return getToken(JavaParser.STATIC, 0); }
		public TerminalNode STRICTFP() { return getToken(JavaParser.STRICTFP, 0); }
		public InterfaceMethodModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceMethodModifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitInterfaceMethodModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceMethodModifierContext interfaceMethodModifier() throws RecognitionException {
		InterfaceMethodModifierContext _localctx = new InterfaceMethodModifierContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_interfaceMethodModifier);
		try {
			setState(613);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AT:
				enterOuterAlt(_localctx, 1);
				{
				setState(607);
				annotation();
				}
				break;
			case PUBLIC:
				enterOuterAlt(_localctx, 2);
				{
				setState(608);
				match(PUBLIC);
				}
				break;
			case ABSTRACT:
				enterOuterAlt(_localctx, 3);
				{
				setState(609);
				match(ABSTRACT);
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 4);
				{
				setState(610);
				match(DEFAULT);
				}
				break;
			case STATIC:
				enterOuterAlt(_localctx, 5);
				{
				setState(611);
				match(STATIC);
				}
				break;
			case STRICTFP:
				enterOuterAlt(_localctx, 6);
				{
				setState(612);
				match(STRICTFP);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GenericInterfaceMethodDeclarationContext extends ParserRuleContext {
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public InterfaceCommonBodyDeclarationContext interfaceCommonBodyDeclaration() {
			return getRuleContext(InterfaceCommonBodyDeclarationContext.class,0);
		}
		public List<InterfaceMethodModifierContext> interfaceMethodModifier() {
			return getRuleContexts(InterfaceMethodModifierContext.class);
		}
		public InterfaceMethodModifierContext interfaceMethodModifier(int i) {
			return getRuleContext(InterfaceMethodModifierContext.class,i);
		}
		public GenericInterfaceMethodDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericInterfaceMethodDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitGenericInterfaceMethodDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericInterfaceMethodDeclarationContext genericInterfaceMethodDeclaration() throws RecognitionException {
		GenericInterfaceMethodDeclarationContext _localctx = new GenericInterfaceMethodDeclarationContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_genericInterfaceMethodDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(618);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 848822976647170L) != 0) || _la==AT) {
				{
				{
				setState(615);
				interfaceMethodModifier();
				}
				}
				setState(620);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(621);
			typeParameters();
			setState(622);
			interfaceCommonBodyDeclaration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InterfaceCommonBodyDeclarationContext extends ParserRuleContext {
		public TypeTypeOrVoidContext typeTypeOrVoid() {
			return getRuleContext(TypeTypeOrVoidContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public MethodBodyContext methodBody() {
			return getRuleContext(MethodBodyContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<TerminalNode> LBRACK() { return getTokens(JavaParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(JavaParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(JavaParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(JavaParser.RBRACK, i);
		}
		public TerminalNode THROWS() { return getToken(JavaParser.THROWS, 0); }
		public QualifiedNameListContext qualifiedNameList() {
			return getRuleContext(QualifiedNameListContext.class,0);
		}
		public InterfaceCommonBodyDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interfaceCommonBodyDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitInterfaceCommonBodyDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterfaceCommonBodyDeclarationContext interfaceCommonBodyDeclaration() throws RecognitionException {
		InterfaceCommonBodyDeclarationContext _localctx = new InterfaceCommonBodyDeclarationContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_interfaceCommonBodyDeclaration);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(627);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(624);
					annotation();
					}
					} 
				}
				setState(629);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,56,_ctx);
			}
			setState(630);
			typeTypeOrVoid();
			setState(631);
			identifier();
			setState(632);
			formalParameters();
			setState(637);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LBRACK) {
				{
				{
				setState(633);
				match(LBRACK);
				setState(634);
				match(RBRACK);
				}
				}
				setState(639);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(642);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THROWS) {
				{
				setState(640);
				match(THROWS);
				setState(641);
				qualifiedNameList();
				}
			}

			setState(644);
			methodBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableDeclaratorsContext extends ParserRuleContext {
		public List<VariableDeclaratorContext> variableDeclarator() {
			return getRuleContexts(VariableDeclaratorContext.class);
		}
		public VariableDeclaratorContext variableDeclarator(int i) {
			return getRuleContext(VariableDeclaratorContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public VariableDeclaratorsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableDeclarators; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitVariableDeclarators(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableDeclaratorsContext variableDeclarators() throws RecognitionException {
		VariableDeclaratorsContext _localctx = new VariableDeclaratorsContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_variableDeclarators);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(646);
			variableDeclarator();
			setState(651);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(647);
					match(COMMA);
					setState(648);
					variableDeclarator();
					}
					} 
				}
				setState(653);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableDeclaratorContext extends ParserRuleContext {
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(JavaParser.ASSIGN, 0); }
		public VariableInitializerContext variableInitializer() {
			return getRuleContext(VariableInitializerContext.class,0);
		}
		public VariableDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableDeclarator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitVariableDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableDeclaratorContext variableDeclarator() throws RecognitionException {
		VariableDeclaratorContext _localctx = new VariableDeclaratorContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_variableDeclarator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(654);
			variableDeclaratorId();
			setState(657);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				{
				setState(655);
				match(ASSIGN);
				setState(656);
				variableInitializer();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableDeclaratorIdContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<TerminalNode> LBRACK() { return getTokens(JavaParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(JavaParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(JavaParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(JavaParser.RBRACK, i);
		}
		public VariableDeclaratorIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableDeclaratorId; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitVariableDeclaratorId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableDeclaratorIdContext variableDeclaratorId() throws RecognitionException {
		VariableDeclaratorIdContext _localctx = new VariableDeclaratorIdContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_variableDeclaratorId);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(659);
			identifier();
			setState(664);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(660);
					match(LBRACK);
					setState(661);
					match(RBRACK);
					}
					} 
				}
				setState(666);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableInitializerContext extends ParserRuleContext {
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public VariableInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableInitializer; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitVariableInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableInitializerContext variableInitializer() throws RecognitionException {
		VariableInitializerContext _localctx = new VariableInitializerContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_variableInitializer);
		try {
			setState(669);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(667);
				arrayInitializer();
				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case EXPORTS:
			case FLOAT:
			case INT:
			case LONG:
			case MODULE:
			case NEW:
			case OPEN:
			case OPENS:
			case PERMITS:
			case PROVIDES:
			case RECORD:
			case REQUIRES:
			case SEALED:
			case SHORT:
			case SUPER:
			case SWITCH:
			case THIS:
			case TO:
			case TRANSITIVE:
			case USES:
			case VAR:
			case VOID:
			case WHEN:
			case WITH:
			case YIELD:
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
			case BOOL_LITERAL:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case TEXT_BLOCK:
			case NULL_LITERAL:
			case LPAREN:
			case LT:
			case BANG:
			case TILDE:
			case INC:
			case DEC:
			case ADD:
			case SUB:
			case AT:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(668);
				expression(0);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayInitializerContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<VariableInitializerContext> variableInitializer() {
			return getRuleContexts(VariableInitializerContext.class);
		}
		public VariableInitializerContext variableInitializer(int i) {
			return getRuleContext(VariableInitializerContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public ArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayInitializer; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitArrayInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayInitializerContext arrayInitializer() throws RecognitionException {
		ArrayInitializerContext _localctx = new ArrayInitializerContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_arrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(671);
			match(LBRACE);
			setState(683);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -5862108592017749979L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801464260026367L) != 0)) {
				{
				setState(672);
				variableInitializer();
				setState(677);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,63,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(673);
						match(COMMA);
						setState(674);
						variableInitializer();
						}
						} 
					}
					setState(679);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,63,_ctx);
				}
				setState(681);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(680);
					match(COMMA);
					}
				}

				}
			}

			setState(685);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassTypeContext extends ParserRuleContext {
		public List<TypeIdentifierContext> typeIdentifier() {
			return getRuleContexts(TypeIdentifierContext.class);
		}
		public TypeIdentifierContext typeIdentifier(int i) {
			return getRuleContext(TypeIdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(JavaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(JavaParser.DOT, i);
		}
		public List<PackageNameContext> packageName() {
			return getRuleContexts(PackageNameContext.class);
		}
		public PackageNameContext packageName(int i) {
			return getRuleContext(PackageNameContext.class,i);
		}
		public List<TypeArgumentsContext> typeArguments() {
			return getRuleContexts(TypeArgumentsContext.class);
		}
		public TypeArgumentsContext typeArguments(int i) {
			return getRuleContext(TypeArgumentsContext.class,i);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public ClassTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classType; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitClassType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassTypeContext classType() throws RecognitionException {
		ClassTypeContext _localctx = new ClassTypeContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_classType);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(701); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(695);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
					case 1:
						{
						setState(687);
						packageName();
						setState(688);
						match(DOT);
						setState(692);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==AT) {
							{
							{
							setState(689);
							annotation();
							}
							}
							setState(694);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
						break;
					}
					setState(697);
					typeIdentifier();
					setState(699);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,68,_ctx) ) {
					case 1:
						{
						setState(698);
						typeArguments();
						}
						break;
					}
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(703); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,69,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(718);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(705);
					match(DOT);
					setState(709);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==AT) {
						{
						{
						setState(706);
						annotation();
						}
						}
						setState(711);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(712);
					typeIdentifier();
					setState(714);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
					case 1:
						{
						setState(713);
						typeArguments();
						}
						break;
					}
					}
					} 
				}
				setState(720);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PackageNameContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(JavaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(JavaParser.DOT, i);
		}
		public PackageNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitPackageName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PackageNameContext packageName() throws RecognitionException {
		PackageNameContext _localctx = new PackageNameContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_packageName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(721);
			identifier();
			setState(726);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(722);
					match(DOT);
					setState(723);
					identifier();
					}
					} 
				}
				setState(728);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeArgumentContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode QUESTION() { return getToken(JavaParser.QUESTION, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TerminalNode EXTENDS() { return getToken(JavaParser.EXTENDS, 0); }
		public TerminalNode SUPER() { return getToken(JavaParser.SUPER, 0); }
		public TypeArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArgument; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeArgumentContext typeArgument() throws RecognitionException {
		TypeArgumentContext _localctx = new TypeArgumentContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_typeArgument);
		int _la;
		try {
			setState(741);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(729);
				typeType();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(733);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==AT) {
					{
					{
					setState(730);
					annotation();
					}
					}
					setState(735);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(736);
				match(QUESTION);
				setState(739);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXTENDS || _la==SUPER) {
					{
					setState(737);
					_la = _input.LA(1);
					if ( !(_la==EXTENDS || _la==SUPER) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(738);
					typeType();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedNameListContext extends ParserRuleContext {
		public List<QualifiedNameContext> qualifiedName() {
			return getRuleContexts(QualifiedNameContext.class);
		}
		public QualifiedNameContext qualifiedName(int i) {
			return getRuleContext(QualifiedNameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public QualifiedNameListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedNameList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitQualifiedNameList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameListContext qualifiedNameList() throws RecognitionException {
		QualifiedNameListContext _localctx = new QualifiedNameListContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_qualifiedNameList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(743);
			qualifiedName();
			setState(748);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(744);
				match(COMMA);
				setState(745);
				qualifiedName();
				}
				}
				setState(750);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FormalParametersContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public ReceiverParameterContext receiverParameter() {
			return getRuleContext(ReceiverParameterContext.class,0);
		}
		public FormalParameterContext formalParameter() {
			return getRuleContext(FormalParameterContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public List<FormalParameterListContext> formalParameterList() {
			return getRuleContexts(FormalParameterListContext.class);
		}
		public FormalParameterListContext formalParameterList(int i) {
			return getRuleContext(FormalParameterListContext.class,i);
		}
		public FormalParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameters; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitFormalParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParametersContext formalParameters() throws RecognitionException {
		FormalParametersContext _localctx = new FormalParametersContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_formalParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(751);
			match(LPAREN);
			setState(763);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -6440117457766758363L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801206503243777L) != 0)) {
				{
				setState(754);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
				case 1:
					{
					setState(752);
					receiverParameter();
					}
					break;
				case 2:
					{
					setState(753);
					formalParameter();
					}
					break;
				}
				setState(760);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(756);
					match(COMMA);
					setState(757);
					formalParameterList();
					}
					}
					setState(762);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(765);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReceiverParameterContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode THIS() { return getToken(JavaParser.THIS, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(JavaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(JavaParser.DOT, i);
		}
		public ReceiverParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_receiverParameter; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitReceiverParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReceiverParameterContext receiverParameter() throws RecognitionException {
		ReceiverParameterContext _localctx = new ReceiverParameterContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_receiverParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(767);
			typeType();
			setState(773);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 17)) & ~0x3f) == 0 && ((1L << (_la - 17)) & 1858725164630017L) != 0) || _la==IDENTIFIER) {
				{
				{
				setState(768);
				identifier();
				setState(769);
				match(DOT);
				}
				}
				setState(775);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(776);
			match(THIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FormalParameterListContext extends ParserRuleContext {
		public List<FormalParameterContext> formalParameter() {
			return getRuleContexts(FormalParameterContext.class);
		}
		public FormalParameterContext formalParameter(int i) {
			return getRuleContext(FormalParameterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public FormalParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameterList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitFormalParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParameterListContext formalParameterList() throws RecognitionException {
		FormalParameterListContext _localctx = new FormalParameterListContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_formalParameterList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(778);
			formalParameter();
			setState(783);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,82,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(779);
					match(COMMA);
					setState(780);
					formalParameter();
					}
					} 
				}
				setState(785);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,82,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FormalParameterContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public List<VariableModifierContext> variableModifier() {
			return getRuleContexts(VariableModifierContext.class);
		}
		public VariableModifierContext variableModifier(int i) {
			return getRuleContext(VariableModifierContext.class,i);
		}
		public TerminalNode ELLIPSIS() { return getToken(JavaParser.ELLIPSIS, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public FormalParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameter; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitFormalParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParameterContext formalParameter() throws RecognitionException {
		FormalParameterContext _localctx = new FormalParameterContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_formalParameter);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(789);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(786);
					variableModifier();
					}
					} 
				}
				setState(791);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
			}
			setState(792);
			typeType();
			setState(800);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT || _la==ELLIPSIS) {
				{
				setState(796);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==AT) {
					{
					{
					setState(793);
					annotation();
					}
					}
					setState(798);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(799);
				match(ELLIPSIS);
				}
			}

			setState(802);
			variableDeclaratorId();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaLVTIListContext extends ParserRuleContext {
		public List<LambdaLVTIParameterContext> lambdaLVTIParameter() {
			return getRuleContexts(LambdaLVTIParameterContext.class);
		}
		public LambdaLVTIParameterContext lambdaLVTIParameter(int i) {
			return getRuleContext(LambdaLVTIParameterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public LambdaLVTIListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaLVTIList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitLambdaLVTIList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaLVTIListContext lambdaLVTIList() throws RecognitionException {
		LambdaLVTIListContext _localctx = new LambdaLVTIListContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_lambdaLVTIList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(804);
			lambdaLVTIParameter();
			setState(809);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(805);
				match(COMMA);
				setState(806);
				lambdaLVTIParameter();
				}
				}
				setState(811);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaLVTIParameterContext extends ParserRuleContext {
		public TerminalNode VAR() { return getToken(JavaParser.VAR, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<VariableModifierContext> variableModifier() {
			return getRuleContexts(VariableModifierContext.class);
		}
		public VariableModifierContext variableModifier(int i) {
			return getRuleContext(VariableModifierContext.class,i);
		}
		public LambdaLVTIParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaLVTIParameter; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitLambdaLVTIParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaLVTIParameterContext lambdaLVTIParameter() throws RecognitionException {
		LambdaLVTIParameterContext _localctx = new LambdaLVTIParameterContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_lambdaLVTIParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(815);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==FINAL || _la==AT) {
				{
				{
				setState(812);
				variableModifier();
				}
				}
				setState(817);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(818);
			match(VAR);
			setState(819);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedNameContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(JavaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(JavaParser.DOT, i);
		}
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_qualifiedName);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(821);
			identifier();
			setState(826);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(822);
					match(DOT);
					setState(823);
					identifier();
					}
					} 
				}
				setState(828);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,88,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public FloatLiteralContext floatLiteral() {
			return getRuleContext(FloatLiteralContext.class,0);
		}
		public TerminalNode CHAR_LITERAL() { return getToken(JavaParser.CHAR_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(JavaParser.STRING_LITERAL, 0); }
		public TerminalNode BOOL_LITERAL() { return getToken(JavaParser.BOOL_LITERAL, 0); }
		public TerminalNode NULL_LITERAL() { return getToken(JavaParser.NULL_LITERAL, 0); }
		public TerminalNode TEXT_BLOCK() { return getToken(JavaParser.TEXT_BLOCK, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_literal);
		try {
			setState(836);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(829);
				integerLiteral();
				}
				break;
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
				enterOuterAlt(_localctx, 2);
				{
				setState(830);
				floatLiteral();
				}
				break;
			case CHAR_LITERAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(831);
				match(CHAR_LITERAL);
				}
				break;
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(832);
				match(STRING_LITERAL);
				}
				break;
			case BOOL_LITERAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(833);
				match(BOOL_LITERAL);
				}
				break;
			case NULL_LITERAL:
				enterOuterAlt(_localctx, 6);
				{
				setState(834);
				match(NULL_LITERAL);
				}
				break;
			case TEXT_BLOCK:
				enterOuterAlt(_localctx, 7);
				{
				setState(835);
				match(TEXT_BLOCK);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IntegerLiteralContext extends ParserRuleContext {
		public TerminalNode DECIMAL_LITERAL() { return getToken(JavaParser.DECIMAL_LITERAL, 0); }
		public TerminalNode HEX_LITERAL() { return getToken(JavaParser.HEX_LITERAL, 0); }
		public TerminalNode OCT_LITERAL() { return getToken(JavaParser.OCT_LITERAL, 0); }
		public TerminalNode BINARY_LITERAL() { return getToken(JavaParser.BINARY_LITERAL, 0); }
		public IntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitIntegerLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntegerLiteralContext integerLiteral() throws RecognitionException {
		IntegerLiteralContext _localctx = new IntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_integerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(838);
			_la = _input.LA(1);
			if ( !(((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & 15L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FloatLiteralContext extends ParserRuleContext {
		public TerminalNode FLOAT_LITERAL() { return getToken(JavaParser.FLOAT_LITERAL, 0); }
		public TerminalNode HEX_FLOAT_LITERAL() { return getToken(JavaParser.HEX_FLOAT_LITERAL, 0); }
		public FloatLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitFloatLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FloatLiteralContext floatLiteral() throws RecognitionException {
		FloatLiteralContext _localctx = new FloatLiteralContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_floatLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(840);
			_la = _input.LA(1);
			if ( !(_la==FLOAT_LITERAL || _la==HEX_FLOAT_LITERAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AltAnnotationQualifiedNameContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(JavaParser.AT, 0); }
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(JavaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(JavaParser.DOT, i);
		}
		public AltAnnotationQualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_altAnnotationQualifiedName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAltAnnotationQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AltAnnotationQualifiedNameContext altAnnotationQualifiedName() throws RecognitionException {
		AltAnnotationQualifiedNameContext _localctx = new AltAnnotationQualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_altAnnotationQualifiedName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(847);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 17)) & ~0x3f) == 0 && ((1L << (_la - 17)) & 1858725164630017L) != 0) || _la==IDENTIFIER) {
				{
				{
				setState(842);
				identifier();
				setState(843);
				match(DOT);
				}
				}
				setState(849);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(850);
			match(AT);
			setState(851);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(JavaParser.AT, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public AnnotationFieldValuesContext annotationFieldValues() {
			return getRuleContext(AnnotationFieldValuesContext.class,0);
		}
		public AnnotationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotation; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationContext annotation() throws RecognitionException {
		AnnotationContext _localctx = new AnnotationContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_annotation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(853);
			match(AT);
			setState(854);
			qualifiedName();
			}
			setState(857);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(856);
				annotationFieldValues();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationFieldValuesContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public List<AnnotationFieldValueContext> annotationFieldValue() {
			return getRuleContexts(AnnotationFieldValueContext.class);
		}
		public AnnotationFieldValueContext annotationFieldValue(int i) {
			return getRuleContext(AnnotationFieldValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public AnnotationFieldValuesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationFieldValues; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationFieldValues(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationFieldValuesContext annotationFieldValues() throws RecognitionException {
		AnnotationFieldValuesContext _localctx = new AnnotationFieldValuesContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_annotationFieldValues);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(859);
			match(LPAREN);
			setState(868);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
			case 1:
				{
				setState(860);
				annotationFieldValue();
				setState(865);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(861);
					match(COMMA);
					setState(862);
					annotationFieldValue();
					}
					}
					setState(867);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(870);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationFieldValueContext extends ParserRuleContext {
		public AnnotationValueContext annotationValue() {
			return getRuleContext(AnnotationValueContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(JavaParser.ASSIGN, 0); }
		public AnnotationFieldValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationFieldValue; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationFieldValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationFieldValueContext annotationFieldValue() throws RecognitionException {
		AnnotationFieldValueContext _localctx = new AnnotationFieldValueContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_annotationFieldValue);
		try {
			setState(878);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(872);
				if (!( this.IsNotIdentifierAssign() )) throw new FailedPredicateException(this, " this.IsNotIdentifierAssign() ");
				setState(873);
				annotationValue();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(874);
				identifier();
				setState(875);
				match(ASSIGN);
				setState(876);
				annotationValue();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationValueContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<AnnotationValueContext> annotationValue() {
			return getRuleContexts(AnnotationValueContext.class);
		}
		public AnnotationValueContext annotationValue(int i) {
			return getRuleContext(AnnotationValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public AnnotationValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationValue; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationValueContext annotationValue() throws RecognitionException {
		AnnotationValueContext _localctx = new AnnotationValueContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_annotationValue);
		int _la;
		try {
			int _alt;
			setState(897);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(880);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(881);
				annotation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(882);
				match(LBRACE);
				setState(891);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -5862108592017749979L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801464260026367L) != 0)) {
					{
					setState(883);
					annotationValue();
					setState(888);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,95,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(884);
							match(COMMA);
							setState(885);
							annotationValue();
							}
							} 
						}
						setState(890);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,95,_ctx);
					}
					}
				}

				setState(894);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(893);
					match(COMMA);
					}
				}

				setState(896);
				match(RBRACE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElementValueContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AnnotationContext annotation() {
			return getRuleContext(AnnotationContext.class,0);
		}
		public ElementValueArrayInitializerContext elementValueArrayInitializer() {
			return getRuleContext(ElementValueArrayInitializerContext.class,0);
		}
		public ElementValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValue; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitElementValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementValueContext elementValue() throws RecognitionException {
		ElementValueContext _localctx = new ElementValueContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_elementValue);
		try {
			setState(902);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(899);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(900);
				annotation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(901);
				elementValueArrayInitializer();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElementValueArrayInitializerContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<ElementValueContext> elementValue() {
			return getRuleContexts(ElementValueContext.class);
		}
		public ElementValueContext elementValue(int i) {
			return getRuleContext(ElementValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public ElementValueArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementValueArrayInitializer; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitElementValueArrayInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElementValueArrayInitializerContext elementValueArrayInitializer() throws RecognitionException {
		ElementValueArrayInitializerContext _localctx = new ElementValueArrayInitializerContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_elementValueArrayInitializer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(904);
			match(LBRACE);
			setState(913);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -5862108592017749979L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801464260026367L) != 0)) {
				{
				setState(905);
				elementValue();
				setState(910);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,100,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(906);
						match(COMMA);
						setState(907);
						elementValue();
						}
						} 
					}
					setState(912);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,100,_ctx);
				}
				}
			}

			setState(916);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(915);
				match(COMMA);
				}
			}

			setState(918);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationTypeDeclarationContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(JavaParser.AT, 0); }
		public TerminalNode INTERFACE() { return getToken(JavaParser.INTERFACE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public AnnotationTypeBodyContext annotationTypeBody() {
			return getRuleContext(AnnotationTypeBodyContext.class,0);
		}
		public AnnotationTypeDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationTypeDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationTypeDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationTypeDeclarationContext annotationTypeDeclaration() throws RecognitionException {
		AnnotationTypeDeclarationContext _localctx = new AnnotationTypeDeclarationContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_annotationTypeDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(920);
			match(AT);
			setState(921);
			match(INTERFACE);
			setState(922);
			identifier();
			setState(923);
			annotationTypeBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationTypeBodyContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<AnnotationTypeElementDeclarationContext> annotationTypeElementDeclaration() {
			return getRuleContexts(AnnotationTypeElementDeclarationContext.class);
		}
		public AnnotationTypeElementDeclarationContext annotationTypeElementDeclaration(int i) {
			return getRuleContext(AnnotationTypeElementDeclarationContext.class,i);
		}
		public AnnotationTypeBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationTypeBody; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationTypeBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationTypeBodyContext annotationTypeBody() throws RecognitionException {
		AnnotationTypeBodyContext _localctx = new AnnotationTypeBodyContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_annotationTypeBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(925);
			match(LBRACE);
			setState(929);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 1)) & ~0x3f) == 0 && ((1L << (_la - 1)) & -2627305097950551659L) != 0) || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & -8935141660702539773L) != 0)) {
				{
				{
				setState(926);
				annotationTypeElementDeclaration();
				}
				}
				setState(931);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(932);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationTypeElementDeclarationContext extends ParserRuleContext {
		public AnnotationTypeElementRestContext annotationTypeElementRest() {
			return getRuleContext(AnnotationTypeElementRestContext.class,0);
		}
		public List<ModifierContext> modifier() {
			return getRuleContexts(ModifierContext.class);
		}
		public ModifierContext modifier(int i) {
			return getRuleContext(ModifierContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public AnnotationTypeElementDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationTypeElementDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationTypeElementDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationTypeElementDeclarationContext annotationTypeElementDeclaration() throws RecognitionException {
		AnnotationTypeElementDeclarationContext _localctx = new AnnotationTypeElementDeclarationContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_annotationTypeElementDeclaration);
		try {
			int _alt;
			setState(942);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSTRACT:
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case CLASS:
			case DOUBLE:
			case ENUM:
			case EXPORTS:
			case FINAL:
			case FLOAT:
			case INT:
			case INTERFACE:
			case LONG:
			case MODULE:
			case NATIVE:
			case NON_SEALED:
			case OPEN:
			case OPENS:
			case PERMITS:
			case PRIVATE:
			case PROTECTED:
			case PROVIDES:
			case PUBLIC:
			case RECORD:
			case REQUIRES:
			case SEALED:
			case SHORT:
			case STATIC:
			case STRICTFP:
			case SYNCHRONIZED:
			case TO:
			case TRANSIENT:
			case TRANSITIVE:
			case USES:
			case VAR:
			case VOLATILE:
			case WHEN:
			case WITH:
			case YIELD:
			case AT:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(937);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(934);
						modifier();
						}
						} 
					}
					setState(939);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
				}
				setState(940);
				annotationTypeElementRest();
				}
				break;
			case SEMI:
				enterOuterAlt(_localctx, 2);
				{
				setState(941);
				match(SEMI);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationTypeElementRestContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public AnnotationMethodOrConstantRestContext annotationMethodOrConstantRest() {
			return getRuleContext(AnnotationMethodOrConstantRestContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public ClassDeclarationContext classDeclaration() {
			return getRuleContext(ClassDeclarationContext.class,0);
		}
		public InterfaceDeclarationContext interfaceDeclaration() {
			return getRuleContext(InterfaceDeclarationContext.class,0);
		}
		public EnumDeclarationContext enumDeclaration() {
			return getRuleContext(EnumDeclarationContext.class,0);
		}
		public AnnotationTypeDeclarationContext annotationTypeDeclaration() {
			return getRuleContext(AnnotationTypeDeclarationContext.class,0);
		}
		public RecordDeclarationContext recordDeclaration() {
			return getRuleContext(RecordDeclarationContext.class,0);
		}
		public AnnotationTypeElementRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationTypeElementRest; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationTypeElementRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationTypeElementRestContext annotationTypeElementRest() throws RecognitionException {
		AnnotationTypeElementRestContext _localctx = new AnnotationTypeElementRestContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_annotationTypeElementRest);
		try {
			setState(968);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(944);
				typeType();
				setState(945);
				annotationMethodOrConstantRest();
				setState(946);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(948);
				classDeclaration();
				setState(950);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,106,_ctx) ) {
				case 1:
					{
					setState(949);
					match(SEMI);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(952);
				interfaceDeclaration();
				setState(954);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,107,_ctx) ) {
				case 1:
					{
					setState(953);
					match(SEMI);
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(956);
				enumDeclaration();
				setState(958);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,108,_ctx) ) {
				case 1:
					{
					setState(957);
					match(SEMI);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(960);
				annotationTypeDeclaration();
				setState(962);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,109,_ctx) ) {
				case 1:
					{
					setState(961);
					match(SEMI);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(964);
				recordDeclaration();
				setState(966);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,110,_ctx) ) {
				case 1:
					{
					setState(965);
					match(SEMI);
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationMethodOrConstantRestContext extends ParserRuleContext {
		public AnnotationMethodRestContext annotationMethodRest() {
			return getRuleContext(AnnotationMethodRestContext.class,0);
		}
		public AnnotationConstantRestContext annotationConstantRest() {
			return getRuleContext(AnnotationConstantRestContext.class,0);
		}
		public AnnotationMethodOrConstantRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationMethodOrConstantRest; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationMethodOrConstantRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationMethodOrConstantRestContext annotationMethodOrConstantRest() throws RecognitionException {
		AnnotationMethodOrConstantRestContext _localctx = new AnnotationMethodOrConstantRestContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_annotationMethodOrConstantRest);
		try {
			setState(972);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,112,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(970);
				annotationMethodRest();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(971);
				annotationConstantRest();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationMethodRestContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public DefaultValueContext defaultValue() {
			return getRuleContext(DefaultValueContext.class,0);
		}
		public AnnotationMethodRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationMethodRest; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationMethodRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationMethodRestContext annotationMethodRest() throws RecognitionException {
		AnnotationMethodRestContext _localctx = new AnnotationMethodRestContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_annotationMethodRest);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(974);
			identifier();
			setState(975);
			match(LPAREN);
			setState(976);
			match(RPAREN);
			setState(978);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEFAULT) {
				{
				setState(977);
				defaultValue();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationConstantRestContext extends ParserRuleContext {
		public VariableDeclaratorsContext variableDeclarators() {
			return getRuleContext(VariableDeclaratorsContext.class,0);
		}
		public AnnotationConstantRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_annotationConstantRest; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitAnnotationConstantRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnnotationConstantRestContext annotationConstantRest() throws RecognitionException {
		AnnotationConstantRestContext _localctx = new AnnotationConstantRestContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_annotationConstantRest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(980);
			variableDeclarators();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DefaultValueContext extends ParserRuleContext {
		public TerminalNode DEFAULT() { return getToken(JavaParser.DEFAULT, 0); }
		public ElementValueContext elementValue() {
			return getRuleContext(ElementValueContext.class,0);
		}
		public DefaultValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultValue; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitDefaultValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultValueContext defaultValue() throws RecognitionException {
		DefaultValueContext _localctx = new DefaultValueContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_defaultValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(982);
			match(DEFAULT);
			setState(983);
			elementValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModuleDeclarationContext extends ParserRuleContext {
		public TerminalNode MODULE() { return getToken(JavaParser.MODULE, 0); }
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TerminalNode OPEN() { return getToken(JavaParser.OPEN, 0); }
		public List<ModuleDirectiveContext> moduleDirective() {
			return getRuleContexts(ModuleDirectiveContext.class);
		}
		public ModuleDirectiveContext moduleDirective(int i) {
			return getRuleContext(ModuleDirectiveContext.class,i);
		}
		public ModuleDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_moduleDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitModuleDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModuleDeclarationContext moduleDeclaration() throws RecognitionException {
		ModuleDeclarationContext _localctx = new ModuleDeclarationContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_moduleDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(988);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(985);
				annotation();
				}
				}
				setState(990);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(992);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPEN) {
				{
				setState(991);
				match(OPEN);
				}
			}

			setState(994);
			match(MODULE);
			setState(995);
			qualifiedName();
			setState(996);
			match(LBRACE);
			setState(1000);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1152941364535754752L) != 0)) {
				{
				{
				setState(997);
				moduleDirective();
				}
				}
				setState(1002);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1003);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ModuleDirectiveContext extends ParserRuleContext {
		public TerminalNode REQUIRES() { return getToken(JavaParser.REQUIRES, 0); }
		public List<QualifiedNameContext> qualifiedName() {
			return getRuleContexts(QualifiedNameContext.class);
		}
		public QualifiedNameContext qualifiedName(int i) {
			return getRuleContext(QualifiedNameContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public List<RequiresModifierContext> requiresModifier() {
			return getRuleContexts(RequiresModifierContext.class);
		}
		public RequiresModifierContext requiresModifier(int i) {
			return getRuleContext(RequiresModifierContext.class,i);
		}
		public TerminalNode EXPORTS() { return getToken(JavaParser.EXPORTS, 0); }
		public TerminalNode TO() { return getToken(JavaParser.TO, 0); }
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public TerminalNode OPENS() { return getToken(JavaParser.OPENS, 0); }
		public TerminalNode USES() { return getToken(JavaParser.USES, 0); }
		public TerminalNode PROVIDES() { return getToken(JavaParser.PROVIDES, 0); }
		public TerminalNode WITH() { return getToken(JavaParser.WITH, 0); }
		public ModuleDirectiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_moduleDirective; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitModuleDirective(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModuleDirectiveContext moduleDirective() throws RecognitionException {
		ModuleDirectiveContext _localctx = new ModuleDirectiveContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_moduleDirective);
		int _la;
		try {
			int _alt;
			setState(1062);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case REQUIRES:
				enterOuterAlt(_localctx, 1);
				{
				setState(1005);
				match(REQUIRES);
				setState(1009);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1006);
						requiresModifier();
						}
						} 
					}
					setState(1011);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
				}
				setState(1012);
				qualifiedName();
				setState(1013);
				match(SEMI);
				}
				break;
			case EXPORTS:
				enterOuterAlt(_localctx, 2);
				{
				setState(1015);
				match(EXPORTS);
				setState(1016);
				qualifiedName();
				setState(1026);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TO) {
					{
					setState(1017);
					match(TO);
					setState(1018);
					qualifiedName();
					setState(1023);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(1019);
						match(COMMA);
						setState(1020);
						qualifiedName();
						}
						}
						setState(1025);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(1028);
				match(SEMI);
				}
				break;
			case OPENS:
				enterOuterAlt(_localctx, 3);
				{
				setState(1030);
				match(OPENS);
				setState(1031);
				qualifiedName();
				setState(1041);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TO) {
					{
					setState(1032);
					match(TO);
					setState(1033);
					qualifiedName();
					setState(1038);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(1034);
						match(COMMA);
						setState(1035);
						qualifiedName();
						}
						}
						setState(1040);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(1043);
				match(SEMI);
				}
				break;
			case USES:
				enterOuterAlt(_localctx, 4);
				{
				setState(1045);
				match(USES);
				setState(1046);
				qualifiedName();
				setState(1047);
				match(SEMI);
				}
				break;
			case PROVIDES:
				enterOuterAlt(_localctx, 5);
				{
				setState(1049);
				match(PROVIDES);
				setState(1050);
				qualifiedName();
				setState(1051);
				match(WITH);
				setState(1052);
				qualifiedName();
				setState(1057);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1053);
					match(COMMA);
					setState(1054);
					qualifiedName();
					}
					}
					setState(1059);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1060);
				match(SEMI);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RequiresModifierContext extends ParserRuleContext {
		public TerminalNode TRANSITIVE() { return getToken(JavaParser.TRANSITIVE, 0); }
		public TerminalNode STATIC() { return getToken(JavaParser.STATIC, 0); }
		public RequiresModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_requiresModifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitRequiresModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RequiresModifierContext requiresModifier() throws RecognitionException {
		RequiresModifierContext _localctx = new RequiresModifierContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_requiresModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1064);
			_la = _input.LA(1);
			if ( !(_la==STATIC || _la==TRANSITIVE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RecordDeclarationContext extends ParserRuleContext {
		public TerminalNode RECORD() { return getToken(JavaParser.RECORD, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public RecordHeaderContext recordHeader() {
			return getRuleContext(RecordHeaderContext.class,0);
		}
		public RecordBodyContext recordBody() {
			return getRuleContext(RecordBodyContext.class,0);
		}
		public TypeParametersContext typeParameters() {
			return getRuleContext(TypeParametersContext.class,0);
		}
		public TerminalNode IMPLEMENTS() { return getToken(JavaParser.IMPLEMENTS, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public RecordDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recordDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitRecordDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RecordDeclarationContext recordDeclaration() throws RecognitionException {
		RecordDeclarationContext _localctx = new RecordDeclarationContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_recordDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1066);
			match(RECORD);
			setState(1067);
			identifier();
			setState(1069);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(1068);
				typeParameters();
				}
			}

			setState(1071);
			recordHeader();
			setState(1074);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IMPLEMENTS) {
				{
				setState(1072);
				match(IMPLEMENTS);
				setState(1073);
				typeList();
				}
			}

			setState(1076);
			recordBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RecordHeaderContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public RecordComponentListContext recordComponentList() {
			return getRuleContext(RecordComponentListContext.class,0);
		}
		public RecordHeaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recordHeader; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitRecordHeader(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RecordHeaderContext recordHeader() throws RecognitionException {
		RecordHeaderContext _localctx = new RecordHeaderContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_recordHeader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1078);
			match(LPAREN);
			setState(1080);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -6440117457766823899L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801206503243777L) != 0)) {
				{
				setState(1079);
				recordComponentList();
				}
			}

			setState(1082);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RecordComponentListContext extends ParserRuleContext {
		public List<RecordComponentContext> recordComponent() {
			return getRuleContexts(RecordComponentContext.class);
		}
		public RecordComponentContext recordComponent(int i) {
			return getRuleContext(RecordComponentContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public RecordComponentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recordComponentList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitRecordComponentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RecordComponentListContext recordComponentList() throws RecognitionException {
		RecordComponentListContext _localctx = new RecordComponentListContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_recordComponentList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1084);
			recordComponent();
			setState(1089);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1085);
					match(COMMA);
					setState(1086);
					recordComponent();
					}
					} 
				}
				setState(1091);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
			}
			setState(1092);
			if (!( this.DoLastRecordComponent() )) throw new FailedPredicateException(this, " this.DoLastRecordComponent() ");
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RecordComponentContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TerminalNode ELLIPSIS() { return getToken(JavaParser.ELLIPSIS, 0); }
		public RecordComponentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recordComponent; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitRecordComponent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RecordComponentContext recordComponent() throws RecognitionException {
		RecordComponentContext _localctx = new RecordComponentContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_recordComponent);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1097);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,128,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1094);
					annotation();
					}
					} 
				}
				setState(1099);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,128,_ctx);
			}
			setState(1100);
			typeType();
			setState(1108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AT || _la==ELLIPSIS) {
				{
				setState(1104);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==AT) {
					{
					{
					setState(1101);
					annotation();
					}
					}
					setState(1106);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1107);
				match(ELLIPSIS);
				}
			}

			setState(1110);
			identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RecordBodyContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<ClassBodyDeclarationContext> classBodyDeclaration() {
			return getRuleContexts(ClassBodyDeclarationContext.class);
		}
		public ClassBodyDeclarationContext classBodyDeclaration(int i) {
			return getRuleContext(ClassBodyDeclarationContext.class,i);
		}
		public List<CompactConstructorDeclarationContext> compactConstructorDeclaration() {
			return getRuleContexts(CompactConstructorDeclarationContext.class);
		}
		public CompactConstructorDeclarationContext compactConstructorDeclaration(int i) {
			return getRuleContext(CompactConstructorDeclarationContext.class,i);
		}
		public RecordBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recordBody; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitRecordBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RecordBodyContext recordBody() throws RecognitionException {
		RecordBodyContext _localctx = new RecordBodyContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_recordBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1112);
			match(LBRACE);
			setState(1117);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 1)) & ~0x3f) == 0 && ((1L << (_la - 1)) & -321462088736857707L) != 0) || ((((_la - 66)) & ~0x3f) == 0 && ((1L << (_la - 66)) & -8935141660685729789L) != 0)) {
				{
				setState(1115);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
				case 1:
					{
					setState(1113);
					classBodyDeclaration();
					}
					break;
				case 2:
					{
					setState(1114);
					compactConstructorDeclaration();
					}
					break;
				}
				}
				setState(1119);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1120);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<BlockStatementContext> blockStatement() {
			return getRuleContexts(BlockStatementContext.class);
		}
		public BlockStatementContext blockStatement(int i) {
			return getRuleContext(BlockStatementContext.class,i);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1122);
			match(LBRACE);
			setState(1126);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 9043227909781416766L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1152923566663204863L) != 0) || _la==IDENTIFIER) {
				{
				{
				setState(1123);
				blockStatement();
				}
				}
				setState(1128);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1129);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockStatementContext extends ParserRuleContext {
		public LocalVariableDeclarationContext localVariableDeclaration() {
			return getRuleContext(LocalVariableDeclarationContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public LocalTypeDeclarationContext localTypeDeclaration() {
			return getRuleContext(LocalTypeDeclarationContext.class,0);
		}
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public BlockStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockStatement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitBlockStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockStatementContext blockStatement() throws RecognitionException {
		BlockStatementContext _localctx = new BlockStatementContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_blockStatement);
		try {
			setState(1136);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,134,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1131);
				localVariableDeclaration();
				setState(1132);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1134);
				localTypeDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1135);
				statement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LocalVariableDeclarationContext extends ParserRuleContext {
		public TerminalNode VAR() { return getToken(JavaParser.VAR, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(JavaParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public VariableDeclaratorsContext variableDeclarators() {
			return getRuleContext(VariableDeclaratorsContext.class,0);
		}
		public List<VariableModifierContext> variableModifier() {
			return getRuleContexts(VariableModifierContext.class);
		}
		public VariableModifierContext variableModifier(int i) {
			return getRuleContext(VariableModifierContext.class,i);
		}
		public LocalVariableDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localVariableDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitLocalVariableDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocalVariableDeclarationContext localVariableDeclaration() throws RecognitionException {
		LocalVariableDeclarationContext _localctx = new LocalVariableDeclarationContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_localVariableDeclaration);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1141);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,135,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1138);
					variableModifier();
					}
					} 
				}
				setState(1143);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,135,_ctx);
			}
			setState(1152);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,136,_ctx) ) {
			case 1:
				{
				setState(1144);
				match(VAR);
				setState(1145);
				identifier();
				setState(1146);
				match(ASSIGN);
				setState(1147);
				expression(0);
				}
				break;
			case 2:
				{
				setState(1149);
				typeType();
				setState(1150);
				variableDeclarators();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(JavaParser.IDENTIFIER, 0); }
		public TerminalNode MODULE() { return getToken(JavaParser.MODULE, 0); }
		public TerminalNode OPEN() { return getToken(JavaParser.OPEN, 0); }
		public TerminalNode REQUIRES() { return getToken(JavaParser.REQUIRES, 0); }
		public TerminalNode EXPORTS() { return getToken(JavaParser.EXPORTS, 0); }
		public TerminalNode OPENS() { return getToken(JavaParser.OPENS, 0); }
		public TerminalNode TO() { return getToken(JavaParser.TO, 0); }
		public TerminalNode USES() { return getToken(JavaParser.USES, 0); }
		public TerminalNode PROVIDES() { return getToken(JavaParser.PROVIDES, 0); }
		public TerminalNode WHEN() { return getToken(JavaParser.WHEN, 0); }
		public TerminalNode WITH() { return getToken(JavaParser.WITH, 0); }
		public TerminalNode TRANSITIVE() { return getToken(JavaParser.TRANSITIVE, 0); }
		public TerminalNode YIELD() { return getToken(JavaParser.YIELD, 0); }
		public TerminalNode SEALED() { return getToken(JavaParser.SEALED, 0); }
		public TerminalNode PERMITS() { return getToken(JavaParser.PERMITS, 0); }
		public TerminalNode RECORD() { return getToken(JavaParser.RECORD, 0); }
		public TerminalNode VAR() { return getToken(JavaParser.VAR, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1154);
			_la = _input.LA(1);
			if ( !(((((_la - 17)) & ~0x3f) == 0 && ((1L << (_la - 17)) & 1858725164630017L) != 0) || _la==IDENTIFIER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeIdentifierContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(JavaParser.IDENTIFIER, 0); }
		public TerminalNode MODULE() { return getToken(JavaParser.MODULE, 0); }
		public TerminalNode OPEN() { return getToken(JavaParser.OPEN, 0); }
		public TerminalNode REQUIRES() { return getToken(JavaParser.REQUIRES, 0); }
		public TerminalNode EXPORTS() { return getToken(JavaParser.EXPORTS, 0); }
		public TerminalNode OPENS() { return getToken(JavaParser.OPENS, 0); }
		public TerminalNode TO() { return getToken(JavaParser.TO, 0); }
		public TerminalNode USES() { return getToken(JavaParser.USES, 0); }
		public TerminalNode PROVIDES() { return getToken(JavaParser.PROVIDES, 0); }
		public TerminalNode WITH() { return getToken(JavaParser.WITH, 0); }
		public TerminalNode TRANSITIVE() { return getToken(JavaParser.TRANSITIVE, 0); }
		public TerminalNode SEALED() { return getToken(JavaParser.SEALED, 0); }
		public TypeIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeIdentifier; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeIdentifierContext typeIdentifier() throws RecognitionException {
		TypeIdentifierContext _localctx = new TypeIdentifierContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_typeIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1156);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1513299739976794112L) != 0) || _la==WITH || _la==IDENTIFIER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LocalTypeDeclarationContext extends ParserRuleContext {
		public ClassDeclarationContext classDeclaration() {
			return getRuleContext(ClassDeclarationContext.class,0);
		}
		public InterfaceDeclarationContext interfaceDeclaration() {
			return getRuleContext(InterfaceDeclarationContext.class,0);
		}
		public RecordDeclarationContext recordDeclaration() {
			return getRuleContext(RecordDeclarationContext.class,0);
		}
		public EnumDeclarationContext enumDeclaration() {
			return getRuleContext(EnumDeclarationContext.class,0);
		}
		public List<ClassOrInterfaceModifierContext> classOrInterfaceModifier() {
			return getRuleContexts(ClassOrInterfaceModifierContext.class);
		}
		public ClassOrInterfaceModifierContext classOrInterfaceModifier(int i) {
			return getRuleContext(ClassOrInterfaceModifierContext.class,i);
		}
		public LocalTypeDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localTypeDeclaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitLocalTypeDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LocalTypeDeclarationContext localTypeDeclaration() throws RecognitionException {
		LocalTypeDeclarationContext _localctx = new LocalTypeDeclarationContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_localTypeDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1161);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 920858168655874L) != 0) || _la==AT) {
				{
				{
				setState(1158);
				classOrInterfaceModifier();
				}
				}
				setState(1163);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1168);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CLASS:
				{
				setState(1164);
				classDeclaration();
				}
				break;
			case INTERFACE:
				{
				setState(1165);
				interfaceDeclaration();
				}
				break;
			case RECORD:
				{
				setState(1166);
				recordDeclaration();
				}
				break;
			case ENUM:
				{
				setState(1167);
				enumDeclaration();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public BlockContext blockLabel;
		public ExpressionContext statementExpression;
		public IdentifierContext identifierLabel;
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode ASSERT() { return getToken(JavaParser.ASSERT, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public TerminalNode COLON() { return getToken(JavaParser.COLON, 0); }
		public TerminalNode IF() { return getToken(JavaParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(JavaParser.ELSE, 0); }
		public TerminalNode FOR() { return getToken(JavaParser.FOR, 0); }
		public ForControlContext forControl() {
			return getRuleContext(ForControlContext.class,0);
		}
		public TerminalNode WHILE() { return getToken(JavaParser.WHILE, 0); }
		public TerminalNode DO() { return getToken(JavaParser.DO, 0); }
		public TerminalNode TRY() { return getToken(JavaParser.TRY, 0); }
		public FinallyBlockContext finallyBlock() {
			return getRuleContext(FinallyBlockContext.class,0);
		}
		public List<CatchClauseContext> catchClause() {
			return getRuleContexts(CatchClauseContext.class);
		}
		public CatchClauseContext catchClause(int i) {
			return getRuleContext(CatchClauseContext.class,i);
		}
		public ResourceSpecificationContext resourceSpecification() {
			return getRuleContext(ResourceSpecificationContext.class,0);
		}
		public TerminalNode SWITCH() { return getToken(JavaParser.SWITCH, 0); }
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<SwitchBlockStatementGroupContext> switchBlockStatementGroup() {
			return getRuleContexts(SwitchBlockStatementGroupContext.class);
		}
		public SwitchBlockStatementGroupContext switchBlockStatementGroup(int i) {
			return getRuleContext(SwitchBlockStatementGroupContext.class,i);
		}
		public List<SwitchLabelContext> switchLabel() {
			return getRuleContexts(SwitchLabelContext.class);
		}
		public SwitchLabelContext switchLabel(int i) {
			return getRuleContext(SwitchLabelContext.class,i);
		}
		public TerminalNode SYNCHRONIZED() { return getToken(JavaParser.SYNCHRONIZED, 0); }
		public TerminalNode RETURN() { return getToken(JavaParser.RETURN, 0); }
		public TerminalNode THROW() { return getToken(JavaParser.THROW, 0); }
		public TerminalNode BREAK() { return getToken(JavaParser.BREAK, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode CONTINUE() { return getToken(JavaParser.CONTINUE, 0); }
		public TerminalNode YIELD() { return getToken(JavaParser.YIELD, 0); }
		public SwitchExpressionContext switchExpression() {
			return getRuleContext(SwitchExpressionContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_statement);
		int _la;
		try {
			int _alt;
			setState(1293);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,152,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1170);
				((StatementContext)_localctx).blockLabel = block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1171);
				match(ASSERT);
				setState(1172);
				expression(0);
				setState(1175);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COLON) {
					{
					setState(1173);
					match(COLON);
					setState(1174);
					expression(0);
					}
				}

				setState(1177);
				match(SEMI);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1179);
				match(IF);
				setState(1180);
				match(LPAREN);
				setState(1181);
				expression(0);
				setState(1182);
				match(RPAREN);
				setState(1183);
				statement();
				setState(1186);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,140,_ctx) ) {
				case 1:
					{
					setState(1184);
					match(ELSE);
					setState(1185);
					statement();
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1188);
				match(FOR);
				setState(1189);
				match(LPAREN);
				setState(1190);
				forControl();
				setState(1191);
				match(RPAREN);
				setState(1192);
				statement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1194);
				match(WHILE);
				setState(1195);
				match(LPAREN);
				setState(1196);
				expression(0);
				setState(1197);
				match(RPAREN);
				setState(1198);
				statement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1200);
				match(DO);
				setState(1201);
				statement();
				setState(1202);
				match(WHILE);
				setState(1203);
				match(LPAREN);
				setState(1204);
				expression(0);
				setState(1205);
				match(RPAREN);
				setState(1206);
				match(SEMI);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1208);
				match(TRY);
				setState(1209);
				block();
				setState(1219);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case CATCH:
					{
					setState(1211); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(1210);
						catchClause();
						}
						}
						setState(1213); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==CATCH );
					setState(1216);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==FINALLY) {
						{
						setState(1215);
						finallyBlock();
						}
					}

					}
					break;
				case FINALLY:
					{
					setState(1218);
					finallyBlock();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1221);
				match(TRY);
				setState(1222);
				resourceSpecification();
				setState(1223);
				block();
				setState(1227);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CATCH) {
					{
					{
					setState(1224);
					catchClause();
					}
					}
					setState(1229);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1231);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FINALLY) {
					{
					setState(1230);
					finallyBlock();
					}
				}

				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1233);
				match(SWITCH);
				setState(1234);
				match(LPAREN);
				setState(1235);
				expression(0);
				setState(1236);
				match(RPAREN);
				setState(1237);
				match(LBRACE);
				setState(1241);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,146,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1238);
						switchBlockStatementGroup();
						}
						} 
					}
					setState(1243);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,146,_ctx);
				}
				setState(1247);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE || _la==DEFAULT) {
					{
					{
					setState(1244);
					switchLabel();
					}
					}
					setState(1249);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1250);
				match(RBRACE);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1252);
				match(SYNCHRONIZED);
				setState(1253);
				match(LPAREN);
				setState(1254);
				expression(0);
				setState(1255);
				match(RPAREN);
				setState(1256);
				block();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1258);
				match(RETURN);
				setState(1260);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -5862108592017749979L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801464260009983L) != 0)) {
					{
					setState(1259);
					expression(0);
					}
				}

				setState(1262);
				match(SEMI);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1263);
				match(THROW);
				setState(1264);
				expression(0);
				setState(1265);
				match(SEMI);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(1267);
				match(BREAK);
				setState(1269);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 17)) & ~0x3f) == 0 && ((1L << (_la - 17)) & 1858725164630017L) != 0) || _la==IDENTIFIER) {
					{
					setState(1268);
					identifier();
					}
				}

				setState(1271);
				match(SEMI);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(1272);
				match(CONTINUE);
				setState(1274);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 17)) & ~0x3f) == 0 && ((1L << (_la - 17)) & 1858725164630017L) != 0) || _la==IDENTIFIER) {
					{
					setState(1273);
					identifier();
					}
				}

				setState(1276);
				match(SEMI);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(1277);
				match(YIELD);
				setState(1278);
				expression(0);
				setState(1279);
				match(SEMI);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(1281);
				match(SEMI);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(1282);
				((StatementContext)_localctx).statementExpression = expression(0);
				setState(1283);
				match(SEMI);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(1285);
				switchExpression();
				setState(1287);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
				case 1:
					{
					setState(1286);
					match(SEMI);
					}
					break;
				}
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(1289);
				((StatementContext)_localctx).identifierLabel = identifier();
				setState(1290);
				match(COLON);
				setState(1291);
				statement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CatchClauseContext extends ParserRuleContext {
		public TerminalNode CATCH() { return getToken(JavaParser.CATCH, 0); }
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public CatchTypeContext catchType() {
			return getRuleContext(CatchTypeContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public List<VariableModifierContext> variableModifier() {
			return getRuleContexts(VariableModifierContext.class);
		}
		public VariableModifierContext variableModifier(int i) {
			return getRuleContext(VariableModifierContext.class,i);
		}
		public CatchClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchClause; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitCatchClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchClauseContext catchClause() throws RecognitionException {
		CatchClauseContext _localctx = new CatchClauseContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_catchClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1295);
			match(CATCH);
			setState(1296);
			match(LPAREN);
			setState(1300);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==FINAL || _la==AT) {
				{
				{
				setState(1297);
				variableModifier();
				}
				}
				setState(1302);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1303);
			catchType();
			setState(1304);
			identifier();
			setState(1305);
			match(RPAREN);
			setState(1306);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CatchTypeContext extends ParserRuleContext {
		public List<QualifiedNameContext> qualifiedName() {
			return getRuleContexts(QualifiedNameContext.class);
		}
		public QualifiedNameContext qualifiedName(int i) {
			return getRuleContext(QualifiedNameContext.class,i);
		}
		public List<TerminalNode> BITOR() { return getTokens(JavaParser.BITOR); }
		public TerminalNode BITOR(int i) {
			return getToken(JavaParser.BITOR, i);
		}
		public CatchTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_catchType; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitCatchType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CatchTypeContext catchType() throws RecognitionException {
		CatchTypeContext _localctx = new CatchTypeContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_catchType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1308);
			qualifiedName();
			setState(1313);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BITOR) {
				{
				{
				setState(1309);
				match(BITOR);
				setState(1310);
				qualifiedName();
				}
				}
				setState(1315);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FinallyBlockContext extends ParserRuleContext {
		public TerminalNode FINALLY() { return getToken(JavaParser.FINALLY, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public FinallyBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_finallyBlock; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitFinallyBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FinallyBlockContext finallyBlock() throws RecognitionException {
		FinallyBlockContext _localctx = new FinallyBlockContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_finallyBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1316);
			match(FINALLY);
			setState(1317);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ResourceSpecificationContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public ResourcesContext resources() {
			return getRuleContext(ResourcesContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public TerminalNode SEMI() { return getToken(JavaParser.SEMI, 0); }
		public ResourceSpecificationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resourceSpecification; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitResourceSpecification(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceSpecificationContext resourceSpecification() throws RecognitionException {
		ResourceSpecificationContext _localctx = new ResourceSpecificationContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_resourceSpecification);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1319);
			match(LPAREN);
			setState(1320);
			resources();
			setState(1322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(1321);
				match(SEMI);
				}
			}

			setState(1324);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ResourcesContext extends ParserRuleContext {
		public List<ResourceContext> resource() {
			return getRuleContexts(ResourceContext.class);
		}
		public ResourceContext resource(int i) {
			return getRuleContext(ResourceContext.class,i);
		}
		public List<TerminalNode> SEMI() { return getTokens(JavaParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(JavaParser.SEMI, i);
		}
		public ResourcesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resources; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitResources(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourcesContext resources() throws RecognitionException {
		ResourcesContext _localctx = new ResourcesContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_resources);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1326);
			resource();
			setState(1331);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,156,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1327);
					match(SEMI);
					setState(1328);
					resource();
					}
					} 
				}
				setState(1333);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,156,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ResourceContext extends ParserRuleContext {
		public TerminalNode ASSIGN() { return getToken(JavaParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public TerminalNode VAR() { return getToken(JavaParser.VAR, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public List<VariableModifierContext> variableModifier() {
			return getRuleContexts(VariableModifierContext.class);
		}
		public VariableModifierContext variableModifier(int i) {
			return getRuleContext(VariableModifierContext.class,i);
		}
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitResource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_resource);
		int _la;
		try {
			setState(1351);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1337);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FINAL || _la==AT) {
					{
					{
					setState(1334);
					variableModifier();
					}
					}
					setState(1339);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1345);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
				case 1:
					{
					setState(1340);
					classOrInterfaceType();
					setState(1341);
					variableDeclaratorId();
					}
					break;
				case 2:
					{
					setState(1343);
					match(VAR);
					setState(1344);
					identifier();
					}
					break;
				}
				setState(1347);
				match(ASSIGN);
				setState(1348);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1350);
				qualifiedName();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SwitchBlockStatementGroupContext extends ParserRuleContext {
		public List<SwitchLabelContext> switchLabel() {
			return getRuleContexts(SwitchLabelContext.class);
		}
		public SwitchLabelContext switchLabel(int i) {
			return getRuleContext(SwitchLabelContext.class,i);
		}
		public List<TerminalNode> COLON() { return getTokens(JavaParser.COLON); }
		public TerminalNode COLON(int i) {
			return getToken(JavaParser.COLON, i);
		}
		public List<BlockStatementContext> blockStatement() {
			return getRuleContexts(BlockStatementContext.class);
		}
		public BlockStatementContext blockStatement(int i) {
			return getRuleContext(BlockStatementContext.class,i);
		}
		public SwitchBlockStatementGroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchBlockStatementGroup; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitSwitchBlockStatementGroup(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchBlockStatementGroupContext switchBlockStatementGroup() throws RecognitionException {
		SwitchBlockStatementGroupContext _localctx = new SwitchBlockStatementGroupContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_switchBlockStatementGroup);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1356); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1353);
				switchLabel();
				setState(1354);
				match(COLON);
				}
				}
				setState(1358); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==CASE || _la==DEFAULT );
			setState(1361); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1360);
				blockStatement();
				}
				}
				setState(1363); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 9043227909781416766L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1152923566663204863L) != 0) || _la==IDENTIFIER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SwitchLabelContext extends ParserRuleContext {
		public ExpressionContext constantExpression;
		public Token enumConstantName;
		public IdentifierContext varName;
		public TerminalNode CASE() { return getToken(JavaParser.CASE, 0); }
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(JavaParser.IDENTIFIER, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(JavaParser.DEFAULT, 0); }
		public SwitchLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchLabel; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitSwitchLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchLabelContext switchLabel() throws RecognitionException {
		SwitchLabelContext _localctx = new SwitchLabelContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_switchLabel);
		try {
			setState(1374);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1365);
				match(CASE);
				setState(1371);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,162,_ctx) ) {
				case 1:
					{
					setState(1366);
					((SwitchLabelContext)_localctx).constantExpression = expression(0);
					}
					break;
				case 2:
					{
					setState(1367);
					((SwitchLabelContext)_localctx).enumConstantName = match(IDENTIFIER);
					}
					break;
				case 3:
					{
					setState(1368);
					typeType();
					setState(1369);
					((SwitchLabelContext)_localctx).varName = identifier();
					}
					break;
				}
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1373);
				match(DEFAULT);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ForControlContext extends ParserRuleContext {
		public ExpressionListContext forUpdate;
		public EnhancedForControlContext enhancedForControl() {
			return getRuleContext(EnhancedForControlContext.class,0);
		}
		public List<TerminalNode> SEMI() { return getTokens(JavaParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(JavaParser.SEMI, i);
		}
		public ForInitContext forInit() {
			return getRuleContext(ForInitContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ForControlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forControl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitForControl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForControlContext forControl() throws RecognitionException {
		ForControlContext _localctx = new ForControlContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_forControl);
		int _la;
		try {
			setState(1388);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,167,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1376);
				enhancedForControl();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1378);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -5862108592017684443L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801464260009983L) != 0)) {
					{
					setState(1377);
					forInit();
					}
				}

				setState(1380);
				match(SEMI);
				setState(1382);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -5862108592017749979L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801464260009983L) != 0)) {
					{
					setState(1381);
					expression(0);
					}
				}

				setState(1384);
				match(SEMI);
				setState(1386);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -5862108592017749979L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801464260009983L) != 0)) {
					{
					setState(1385);
					((ForControlContext)_localctx).forUpdate = expressionList();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ForInitContext extends ParserRuleContext {
		public LocalVariableDeclarationContext localVariableDeclaration() {
			return getRuleContext(LocalVariableDeclarationContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ForInitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forInit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitForInit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForInitContext forInit() throws RecognitionException {
		ForInitContext _localctx = new ForInitContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_forInit);
		try {
			setState(1392);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1390);
				localVariableDeclaration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1391);
				expressionList();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EnhancedForControlContext extends ParserRuleContext {
		public VariableDeclaratorIdContext variableDeclaratorId() {
			return getRuleContext(VariableDeclaratorIdContext.class,0);
		}
		public TerminalNode COLON() { return getToken(JavaParser.COLON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode VAR() { return getToken(JavaParser.VAR, 0); }
		public List<VariableModifierContext> variableModifier() {
			return getRuleContexts(VariableModifierContext.class);
		}
		public VariableModifierContext variableModifier(int i) {
			return getRuleContext(VariableModifierContext.class,i);
		}
		public EnhancedForControlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enhancedForControl; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitEnhancedForControl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnhancedForControlContext enhancedForControl() throws RecognitionException {
		EnhancedForControlContext _localctx = new EnhancedForControlContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_enhancedForControl);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1397);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,169,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1394);
					variableModifier();
					}
					} 
				}
				setState(1399);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,169,_ctx);
			}
			setState(1402);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,170,_ctx) ) {
			case 1:
				{
				setState(1400);
				typeType();
				}
				break;
			case 2:
				{
				setState(1401);
				match(VAR);
				}
				break;
			}
			setState(1404);
			variableDeclaratorId();
			setState(1405);
			match(COLON);
			setState(1406);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionListContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitExpressionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionListContext expressionList() throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1408);
			expression(0);
			setState(1413);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1409);
				match(COMMA);
				setState(1410);
				expression(0);
				}
				}
				setState(1415);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodCallContext extends ParserRuleContext {
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode THIS() { return getToken(JavaParser.THIS, 0); }
		public TerminalNode SUPER() { return getToken(JavaParser.SUPER, 0); }
		public MethodCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodCall; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitMethodCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodCallContext methodCall() throws RecognitionException {
		MethodCallContext _localctx = new MethodCallContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_methodCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1419);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXPORTS:
			case MODULE:
			case OPEN:
			case OPENS:
			case PERMITS:
			case PROVIDES:
			case RECORD:
			case REQUIRES:
			case SEALED:
			case TO:
			case TRANSITIVE:
			case USES:
			case VAR:
			case WHEN:
			case WITH:
			case YIELD:
			case IDENTIFIER:
				{
				setState(1416);
				identifier();
				}
				break;
			case THIS:
				{
				setState(1417);
				match(THIS);
				}
				break;
			case SUPER:
				{
				setState(1418);
				match(SUPER);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1421);
			arguments();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TernaryExpressionContext extends ExpressionContext {
		public Token bop;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode COLON() { return getToken(JavaParser.COLON, 0); }
		public TerminalNode QUESTION() { return getToken(JavaParser.QUESTION, 0); }
		public TernaryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTernaryExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InstanceOfOperatorExpressionContext extends ExpressionContext {
		public Token bop;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode INSTANCEOF() { return getToken(JavaParser.INSTANCEOF, 0); }
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public InstanceOfOperatorExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitInstanceOfOperatorExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryOperatorExpressionContext extends ExpressionContext {
		public Token prefix;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ADD() { return getToken(JavaParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(JavaParser.SUB, 0); }
		public TerminalNode INC() { return getToken(JavaParser.INC, 0); }
		public TerminalNode DEC() { return getToken(JavaParser.DEC, 0); }
		public TerminalNode TILDE() { return getToken(JavaParser.TILDE, 0); }
		public TerminalNode BANG() { return getToken(JavaParser.BANG, 0); }
		public UnaryOperatorExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitUnaryOperatorExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryExpressionContext extends ExpressionContext {
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public PrimaryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitPrimaryExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ObjectCreationExpressionContext extends ExpressionContext {
		public TerminalNode NEW() { return getToken(JavaParser.NEW, 0); }
		public CreatorContext creator() {
			return getRuleContext(CreatorContext.class,0);
		}
		public ObjectCreationExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitObjectCreationExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionLambdaContext extends ExpressionContext {
		public LambdaExpressionContext lambdaExpression() {
			return getRuleContext(LambdaExpressionContext.class,0);
		}
		public ExpressionLambdaContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitExpressionLambda(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PostIncrementDecrementOperatorExpressionContext extends ExpressionContext {
		public Token postfix;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode INC() { return getToken(JavaParser.INC, 0); }
		public TerminalNode DEC() { return getToken(JavaParser.DEC, 0); }
		public PostIncrementDecrementOperatorExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitPostIncrementDecrementOperatorExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MemberReferenceExpressionContext extends ExpressionContext {
		public Token bop;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DOT() { return getToken(JavaParser.DOT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public MethodCallContext methodCall() {
			return getRuleContext(MethodCallContext.class,0);
		}
		public TerminalNode THIS() { return getToken(JavaParser.THIS, 0); }
		public TerminalNode NEW() { return getToken(JavaParser.NEW, 0); }
		public InnerCreatorContext innerCreator() {
			return getRuleContext(InnerCreatorContext.class,0);
		}
		public TerminalNode SUPER() { return getToken(JavaParser.SUPER, 0); }
		public SuperSuffixContext superSuffix() {
			return getRuleContext(SuperSuffixContext.class,0);
		}
		public ExplicitGenericInvocationContext explicitGenericInvocation() {
			return getRuleContext(ExplicitGenericInvocationContext.class,0);
		}
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public MemberReferenceExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitMemberReferenceExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionSwitchContext extends ExpressionContext {
		public SwitchExpressionContext switchExpression() {
			return getRuleContext(SwitchExpressionContext.class,0);
		}
		public ExpressionSwitchContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitExpressionSwitch(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BinaryOperatorExpressionContext extends ExpressionContext {
		public Token bop;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode MUL() { return getToken(JavaParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(JavaParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(JavaParser.MOD, 0); }
		public TerminalNode ADD() { return getToken(JavaParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(JavaParser.SUB, 0); }
		public List<TerminalNode> LT() { return getTokens(JavaParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(JavaParser.LT, i);
		}
		public List<TerminalNode> GT() { return getTokens(JavaParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(JavaParser.GT, i);
		}
		public TerminalNode LE() { return getToken(JavaParser.LE, 0); }
		public TerminalNode GE() { return getToken(JavaParser.GE, 0); }
		public TerminalNode EQUAL() { return getToken(JavaParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(JavaParser.NOTEQUAL, 0); }
		public TerminalNode BITAND() { return getToken(JavaParser.BITAND, 0); }
		public TerminalNode CARET() { return getToken(JavaParser.CARET, 0); }
		public TerminalNode BITOR() { return getToken(JavaParser.BITOR, 0); }
		public TerminalNode AND() { return getToken(JavaParser.AND, 0); }
		public TerminalNode OR() { return getToken(JavaParser.OR, 0); }
		public TerminalNode ASSIGN() { return getToken(JavaParser.ASSIGN, 0); }
		public TerminalNode ADD_ASSIGN() { return getToken(JavaParser.ADD_ASSIGN, 0); }
		public TerminalNode SUB_ASSIGN() { return getToken(JavaParser.SUB_ASSIGN, 0); }
		public TerminalNode MUL_ASSIGN() { return getToken(JavaParser.MUL_ASSIGN, 0); }
		public TerminalNode DIV_ASSIGN() { return getToken(JavaParser.DIV_ASSIGN, 0); }
		public TerminalNode AND_ASSIGN() { return getToken(JavaParser.AND_ASSIGN, 0); }
		public TerminalNode OR_ASSIGN() { return getToken(JavaParser.OR_ASSIGN, 0); }
		public TerminalNode XOR_ASSIGN() { return getToken(JavaParser.XOR_ASSIGN, 0); }
		public TerminalNode RSHIFT_ASSIGN() { return getToken(JavaParser.RSHIFT_ASSIGN, 0); }
		public TerminalNode URSHIFT_ASSIGN() { return getToken(JavaParser.URSHIFT_ASSIGN, 0); }
		public TerminalNode LSHIFT_ASSIGN() { return getToken(JavaParser.LSHIFT_ASSIGN, 0); }
		public TerminalNode MOD_ASSIGN() { return getToken(JavaParser.MOD_ASSIGN, 0); }
		public BinaryOperatorExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitBinaryOperatorExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodCallExpressionContext extends ExpressionContext {
		public MethodCallContext methodCall() {
			return getRuleContext(MethodCallContext.class,0);
		}
		public MethodCallExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitMethodCallExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodReferenceExpressionContext extends ExpressionContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public TerminalNode COLONCOLON() { return getToken(JavaParser.COLONCOLON, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode NEW() { return getToken(JavaParser.NEW, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public ClassTypeContext classType() {
			return getRuleContext(ClassTypeContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public MethodReferenceExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitMethodReferenceExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SquareBracketExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode LBRACK() { return getToken(JavaParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(JavaParser.RBRACK, 0); }
		public SquareBracketExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitSquareBracketExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<TerminalNode> BITAND() { return getTokens(JavaParser.BITAND); }
		public TerminalNode BITAND(int i) {
			return getToken(JavaParser.BITAND, i);
		}
		public CastExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitCastExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 198;
		enterRecursionRule(_localctx, 198, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1466);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
			case 1:
				{
				_localctx = new PrimaryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(1424);
				primary();
				}
				break;
			case 2:
				{
				_localctx = new MethodCallExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1425);
				methodCall();
				}
				break;
			case 3:
				{
				_localctx = new MethodReferenceExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1426);
				typeType();
				setState(1427);
				match(COLONCOLON);
				setState(1433);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case EXPORTS:
				case MODULE:
				case OPEN:
				case OPENS:
				case PERMITS:
				case PROVIDES:
				case RECORD:
				case REQUIRES:
				case SEALED:
				case TO:
				case TRANSITIVE:
				case USES:
				case VAR:
				case WHEN:
				case WITH:
				case YIELD:
				case LT:
				case IDENTIFIER:
					{
					setState(1429);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LT) {
						{
						setState(1428);
						typeArguments();
						}
					}

					setState(1431);
					identifier();
					}
					break;
				case NEW:
					{
					setState(1432);
					match(NEW);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 4:
				{
				_localctx = new MethodReferenceExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1435);
				classType();
				setState(1436);
				match(COLONCOLON);
				setState(1438);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(1437);
					typeArguments();
					}
				}

				setState(1440);
				match(NEW);
				}
				break;
			case 5:
				{
				_localctx = new ExpressionSwitchContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1442);
				switchExpression();
				}
				break;
			case 6:
				{
				_localctx = new UnaryOperatorExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1443);
				((UnaryOperatorExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 91)) & ~0x3f) == 0 && ((1L << (_la - 91)) & 15363L) != 0)) ) {
					((UnaryOperatorExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1444);
				expression(17);
				}
				break;
			case 7:
				{
				_localctx = new CastExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1445);
				match(LPAREN);
				setState(1449);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,176,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1446);
						annotation();
						}
						} 
					}
					setState(1451);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,176,_ctx);
				}
				setState(1452);
				typeType();
				setState(1457);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==BITAND) {
					{
					{
					setState(1453);
					match(BITAND);
					setState(1454);
					typeType();
					}
					}
					setState(1459);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1460);
				match(RPAREN);
				setState(1461);
				expression(16);
				}
				break;
			case 8:
				{
				_localctx = new ObjectCreationExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1463);
				match(NEW);
				setState(1464);
				creator();
				}
				break;
			case 9:
				{
				_localctx = new ExpressionLambdaContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(1465);
				lambdaExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1551);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,185,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1549);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,184,_ctx) ) {
					case 1:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1468);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(1469);
						((BinaryOperatorExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 105)) & ~0x3f) == 0 && ((1L << (_la - 105)) & 35L) != 0)) ) {
							((BinaryOperatorExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1470);
						expression(15);
						}
						break;
					case 2:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1471);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(1472);
						((BinaryOperatorExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==ADD || _la==SUB) ) {
							((BinaryOperatorExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1473);
						expression(14);
						}
						break;
					case 3:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1474);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(1482);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,179,_ctx) ) {
						case 1:
							{
							setState(1475);
							match(LT);
							setState(1476);
							match(LT);
							}
							break;
						case 2:
							{
							setState(1477);
							match(GT);
							setState(1478);
							match(GT);
							setState(1479);
							match(GT);
							}
							break;
						case 3:
							{
							setState(1480);
							match(GT);
							setState(1481);
							match(GT);
							}
							break;
						}
						setState(1484);
						expression(13);
						}
						break;
					case 4:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1485);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(1486);
						((BinaryOperatorExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 89)) & ~0x3f) == 0 && ((1L << (_la - 89)) & 387L) != 0)) ) {
							((BinaryOperatorExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1487);
						expression(12);
						}
						break;
					case 5:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1488);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(1489);
						((BinaryOperatorExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==EQUAL || _la==NOTEQUAL) ) {
							((BinaryOperatorExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1490);
						expression(10);
						}
						break;
					case 6:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1491);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(1492);
						((BinaryOperatorExpressionContext)_localctx).bop = match(BITAND);
						setState(1493);
						expression(9);
						}
						break;
					case 7:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1494);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(1495);
						((BinaryOperatorExpressionContext)_localctx).bop = match(CARET);
						setState(1496);
						expression(8);
						}
						break;
					case 8:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1497);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(1498);
						((BinaryOperatorExpressionContext)_localctx).bop = match(BITOR);
						setState(1499);
						expression(7);
						}
						break;
					case 9:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1500);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(1501);
						((BinaryOperatorExpressionContext)_localctx).bop = match(AND);
						setState(1502);
						expression(6);
						}
						break;
					case 10:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1503);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(1504);
						((BinaryOperatorExpressionContext)_localctx).bop = match(OR);
						setState(1505);
						expression(5);
						}
						break;
					case 11:
						{
						_localctx = new TernaryExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1506);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1507);
						((TernaryExpressionContext)_localctx).bop = match(QUESTION);
						setState(1508);
						expression(0);
						setState(1509);
						match(COLON);
						setState(1510);
						expression(3);
						}
						break;
					case 12:
						{
						_localctx = new BinaryOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1512);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1513);
						((BinaryOperatorExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 17171480577L) != 0)) ) {
							((BinaryOperatorExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1514);
						expression(2);
						}
						break;
					case 13:
						{
						_localctx = new SquareBracketExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1515);
						if (!(precpred(_ctx, 25))) throw new FailedPredicateException(this, "precpred(_ctx, 25)");
						setState(1516);
						match(LBRACK);
						setState(1517);
						expression(0);
						setState(1518);
						match(RBRACK);
						}
						break;
					case 14:
						{
						_localctx = new MemberReferenceExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1520);
						if (!(precpred(_ctx, 24))) throw new FailedPredicateException(this, "precpred(_ctx, 24)");
						setState(1521);
						((MemberReferenceExpressionContext)_localctx).bop = match(DOT);
						setState(1533);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,181,_ctx) ) {
						case 1:
							{
							setState(1522);
							identifier();
							}
							break;
						case 2:
							{
							setState(1523);
							methodCall();
							}
							break;
						case 3:
							{
							setState(1524);
							match(THIS);
							}
							break;
						case 4:
							{
							setState(1525);
							match(NEW);
							setState(1527);
							_errHandler.sync(this);
							_la = _input.LA(1);
							if (_la==LT) {
								{
								setState(1526);
								nonWildcardTypeArguments();
								}
							}

							setState(1529);
							innerCreator();
							}
							break;
						case 5:
							{
							setState(1530);
							match(SUPER);
							setState(1531);
							superSuffix();
							}
							break;
						case 6:
							{
							setState(1532);
							explicitGenericInvocation();
							}
							break;
						}
						}
						break;
					case 15:
						{
						_localctx = new MethodReferenceExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1535);
						if (!(precpred(_ctx, 22))) throw new FailedPredicateException(this, "precpred(_ctx, 22)");
						setState(1536);
						match(COLONCOLON);
						setState(1538);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LT) {
							{
							setState(1537);
							typeArguments();
							}
						}

						setState(1540);
						identifier();
						}
						break;
					case 16:
						{
						_localctx = new PostIncrementDecrementOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1541);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(1542);
						((PostIncrementDecrementOperatorExpressionContext)_localctx).postfix = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==INC || _la==DEC) ) {
							((PostIncrementDecrementOperatorExpressionContext)_localctx).postfix = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					case 17:
						{
						_localctx = new InstanceOfOperatorExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1543);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(1544);
						((InstanceOfOperatorExpressionContext)_localctx).bop = match(INSTANCEOF);
						setState(1547);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
						case 1:
							{
							setState(1545);
							typeType();
							}
							break;
						case 2:
							{
							setState(1546);
							pattern();
							}
							break;
						}
						}
						break;
					}
					} 
				}
				setState(1553);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,185,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PatternContext extends ParserRuleContext {
		public TypeTypeContext typeType() {
			return getRuleContext(TypeTypeContext.class,0);
		}
		public VariableDeclaratorsContext variableDeclarators() {
			return getRuleContext(VariableDeclaratorsContext.class,0);
		}
		public List<VariableModifierContext> variableModifier() {
			return getRuleContexts(VariableModifierContext.class);
		}
		public VariableModifierContext variableModifier(int i) {
			return getRuleContext(VariableModifierContext.class,i);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public ComponentPatternListContext componentPatternList() {
			return getRuleContext(ComponentPatternListContext.class,0);
		}
		public PatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pattern; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PatternContext pattern() throws RecognitionException {
		PatternContext _localctx = new PatternContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_pattern);
		int _la;
		try {
			int _alt;
			setState(1576);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,189,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1557);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1554);
						variableModifier();
						}
						} 
					}
					setState(1559);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,186,_ctx);
				}
				setState(1560);
				typeType();
				setState(1564);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==AT) {
					{
					{
					setState(1561);
					annotation();
					}
					}
					setState(1566);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1567);
				variableDeclarators();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1569);
				typeType();
				setState(1570);
				match(LPAREN);
				setState(1572);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -6440117457766758363L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801206503243777L) != 0)) {
					{
					setState(1571);
					componentPatternList();
					}
				}

				setState(1574);
				match(RPAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComponentPatternListContext extends ParserRuleContext {
		public List<ComponentPatternContext> componentPattern() {
			return getRuleContexts(ComponentPatternContext.class);
		}
		public ComponentPatternContext componentPattern(int i) {
			return getRuleContext(ComponentPatternContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public ComponentPatternListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_componentPatternList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitComponentPatternList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComponentPatternListContext componentPatternList() throws RecognitionException {
		ComponentPatternListContext _localctx = new ComponentPatternListContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_componentPatternList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1578);
			componentPattern();
			setState(1583);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1579);
				match(COMMA);
				setState(1580);
				componentPattern();
				}
				}
				setState(1585);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComponentPatternContext extends ParserRuleContext {
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public ComponentPatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_componentPattern; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitComponentPattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComponentPatternContext componentPattern() throws RecognitionException {
		ComponentPatternContext _localctx = new ComponentPatternContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_componentPattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1586);
			pattern();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaExpressionContext extends ParserRuleContext {
		public LambdaParametersContext lambdaParameters() {
			return getRuleContext(LambdaParametersContext.class,0);
		}
		public TerminalNode ARROW() { return getToken(JavaParser.ARROW, 0); }
		public LambdaBodyContext lambdaBody() {
			return getRuleContext(LambdaBodyContext.class,0);
		}
		public LambdaExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitLambdaExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaExpressionContext lambdaExpression() throws RecognitionException {
		LambdaExpressionContext _localctx = new LambdaExpressionContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_lambdaExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1588);
			lambdaParameters();
			setState(1589);
			match(ARROW);
			setState(1590);
			lambdaBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaParametersContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public FormalParameterListContext formalParameterList() {
			return getRuleContext(FormalParameterListContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public LambdaLVTIListContext lambdaLVTIList() {
			return getRuleContext(LambdaLVTIListContext.class,0);
		}
		public LambdaParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaParameters; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitLambdaParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaParametersContext lambdaParameters() throws RecognitionException {
		LambdaParametersContext _localctx = new LambdaParametersContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_lambdaParameters);
		int _la;
		try {
			setState(1614);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,194,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1592);
				identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1593);
				match(LPAREN);
				setState(1595);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -6440117457766758363L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801206503243777L) != 0)) {
					{
					setState(1594);
					formalParameterList();
					}
				}

				setState(1597);
				match(RPAREN);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1598);
				match(LPAREN);
				setState(1599);
				identifier();
				setState(1604);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1600);
					match(COMMA);
					setState(1601);
					identifier();
					}
					}
					setState(1606);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1607);
				match(RPAREN);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1609);
				match(LPAREN);
				setState(1611);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FINAL || _la==VAR || _la==AT) {
					{
					setState(1610);
					lambdaLVTIList();
					}
				}

				setState(1613);
				match(RPAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaBodyContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public LambdaBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaBody; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitLambdaBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaBodyContext lambdaBody() throws RecognitionException {
		LambdaBodyContext _localctx = new LambdaBodyContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_lambdaBody);
		try {
			setState(1618);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case EXPORTS:
			case FLOAT:
			case INT:
			case LONG:
			case MODULE:
			case NEW:
			case OPEN:
			case OPENS:
			case PERMITS:
			case PROVIDES:
			case RECORD:
			case REQUIRES:
			case SEALED:
			case SHORT:
			case SUPER:
			case SWITCH:
			case THIS:
			case TO:
			case TRANSITIVE:
			case USES:
			case VAR:
			case VOID:
			case WHEN:
			case WITH:
			case YIELD:
			case DECIMAL_LITERAL:
			case HEX_LITERAL:
			case OCT_LITERAL:
			case BINARY_LITERAL:
			case FLOAT_LITERAL:
			case HEX_FLOAT_LITERAL:
			case BOOL_LITERAL:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case TEXT_BLOCK:
			case NULL_LITERAL:
			case LPAREN:
			case LT:
			case BANG:
			case TILDE:
			case INC:
			case DEC:
			case ADD:
			case SUB:
			case AT:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(1616);
				expression(0);
				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1617);
				block();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public TerminalNode THIS() { return getToken(JavaParser.THIS, 0); }
		public TerminalNode SUPER() { return getToken(JavaParser.SUPER, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypeTypeOrVoidContext typeTypeOrVoid() {
			return getRuleContext(TypeTypeOrVoidContext.class,0);
		}
		public TerminalNode DOT() { return getToken(JavaParser.DOT, 0); }
		public TerminalNode CLASS() { return getToken(JavaParser.CLASS, 0); }
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public ExplicitGenericInvocationSuffixContext explicitGenericInvocationSuffix() {
			return getRuleContext(ExplicitGenericInvocationSuffixContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_primary);
		try {
			setState(1638);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,197,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1620);
				match(LPAREN);
				setState(1621);
				expression(0);
				setState(1622);
				match(RPAREN);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1624);
				match(THIS);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1625);
				match(SUPER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1626);
				literal();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1627);
				identifier();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1628);
				typeTypeOrVoid();
				setState(1629);
				match(DOT);
				setState(1630);
				match(CLASS);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1632);
				nonWildcardTypeArguments();
				setState(1636);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case EXPORTS:
				case MODULE:
				case OPEN:
				case OPENS:
				case PERMITS:
				case PROVIDES:
				case RECORD:
				case REQUIRES:
				case SEALED:
				case SUPER:
				case TO:
				case TRANSITIVE:
				case USES:
				case VAR:
				case WHEN:
				case WITH:
				case YIELD:
				case IDENTIFIER:
					{
					setState(1633);
					explicitGenericInvocationSuffix();
					}
					break;
				case THIS:
					{
					setState(1634);
					match(THIS);
					setState(1635);
					arguments();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SwitchExpressionContext extends ParserRuleContext {
		public TerminalNode SWITCH() { return getToken(JavaParser.SWITCH, 0); }
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public TerminalNode LBRACE() { return getToken(JavaParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(JavaParser.RBRACE, 0); }
		public List<SwitchLabeledRuleContext> switchLabeledRule() {
			return getRuleContexts(SwitchLabeledRuleContext.class);
		}
		public SwitchLabeledRuleContext switchLabeledRule(int i) {
			return getRuleContext(SwitchLabeledRuleContext.class,i);
		}
		public SwitchExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitSwitchExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchExpressionContext switchExpression() throws RecognitionException {
		SwitchExpressionContext _localctx = new SwitchExpressionContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_switchExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1640);
			match(SWITCH);
			setState(1641);
			match(LPAREN);
			setState(1642);
			expression(0);
			setState(1643);
			match(RPAREN);
			setState(1644);
			match(LBRACE);
			setState(1648);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CASE || _la==DEFAULT) {
				{
				{
				setState(1645);
				switchLabeledRule();
				}
				}
				setState(1650);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1651);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SwitchLabeledRuleContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(JavaParser.CASE, 0); }
		public SwitchRuleOutcomeContext switchRuleOutcome() {
			return getRuleContext(SwitchRuleOutcomeContext.class,0);
		}
		public TerminalNode ARROW() { return getToken(JavaParser.ARROW, 0); }
		public TerminalNode COLON() { return getToken(JavaParser.COLON, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode NULL_LITERAL() { return getToken(JavaParser.NULL_LITERAL, 0); }
		public List<CasePatternContext> casePattern() {
			return getRuleContexts(CasePatternContext.class);
		}
		public CasePatternContext casePattern(int i) {
			return getRuleContext(CasePatternContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public TerminalNode DEFAULT() { return getToken(JavaParser.DEFAULT, 0); }
		public GuardContext guard() {
			return getRuleContext(GuardContext.class,0);
		}
		public SwitchLabeledRuleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchLabeledRule; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitSwitchLabeledRule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchLabeledRuleContext switchLabeledRule() throws RecognitionException {
		SwitchLabeledRuleContext _localctx = new SwitchLabeledRuleContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_switchLabeledRule);
		int _la;
		try {
			setState(1678);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1653);
				match(CASE);
				setState(1671);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,202,_ctx) ) {
				case 1:
					{
					setState(1654);
					expressionList();
					}
					break;
				case 2:
					{
					setState(1655);
					match(NULL_LITERAL);
					setState(1658);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(1656);
						match(COMMA);
						setState(1657);
						match(DEFAULT);
						}
					}

					}
					break;
				case 3:
					{
					setState(1660);
					casePattern();
					setState(1665);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(1661);
						match(COMMA);
						setState(1662);
						casePattern();
						}
						}
						setState(1667);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(1669);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==WHEN) {
						{
						setState(1668);
						guard();
						}
					}

					}
					break;
				}
				setState(1673);
				_la = _input.LA(1);
				if ( !(_la==COLON || _la==ARROW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1674);
				switchRuleOutcome();
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1675);
				match(DEFAULT);
				setState(1676);
				_la = _input.LA(1);
				if ( !(_la==COLON || _la==ARROW) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1677);
				switchRuleOutcome();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GuardContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(JavaParser.WHEN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public GuardContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_guard; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitGuard(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GuardContext guard() throws RecognitionException {
		GuardContext _localctx = new GuardContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_guard);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1680);
			match(WHEN);
			setState(1681);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CasePatternContext extends ParserRuleContext {
		public PatternContext pattern() {
			return getRuleContext(PatternContext.class,0);
		}
		public CasePatternContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_casePattern; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitCasePattern(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CasePatternContext casePattern() throws RecognitionException {
		CasePatternContext _localctx = new CasePatternContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_casePattern);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1683);
			pattern();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SwitchRuleOutcomeContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public List<BlockStatementContext> blockStatement() {
			return getRuleContexts(BlockStatementContext.class);
		}
		public BlockStatementContext blockStatement(int i) {
			return getRuleContext(BlockStatementContext.class,i);
		}
		public SwitchRuleOutcomeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchRuleOutcome; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitSwitchRuleOutcome(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchRuleOutcomeContext switchRuleOutcome() throws RecognitionException {
		SwitchRuleOutcomeContext _localctx = new SwitchRuleOutcomeContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_switchRuleOutcome);
		int _la;
		try {
			setState(1692);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,205,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1685);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1689);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 9043227909781416766L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 1152923566663204863L) != 0) || _la==IDENTIFIER) {
					{
					{
					setState(1686);
					blockStatement();
					}
					}
					setState(1691);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassOrInterfaceTypeContext extends ParserRuleContext {
		public ClassTypeContext classType() {
			return getRuleContext(ClassTypeContext.class,0);
		}
		public ClassOrInterfaceTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classOrInterfaceType; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitClassOrInterfaceType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassOrInterfaceTypeContext classOrInterfaceType() throws RecognitionException {
		ClassOrInterfaceTypeContext _localctx = new ClassOrInterfaceTypeContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_classOrInterfaceType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1694);
			classType();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CreatorContext extends ParserRuleContext {
		public CreatedNameContext createdName() {
			return getRuleContext(CreatedNameContext.class,0);
		}
		public ClassCreatorRestContext classCreatorRest() {
			return getRuleContext(ClassCreatorRestContext.class,0);
		}
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public ArrayCreatorRestContext arrayCreatorRest() {
			return getRuleContext(ArrayCreatorRestContext.class,0);
		}
		public CreatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_creator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitCreator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreatorContext creator() throws RecognitionException {
		CreatorContext _localctx = new CreatorContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_creator);
		int _la;
		try {
			setState(1705);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1697);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(1696);
					nonWildcardTypeArguments();
					}
				}

				setState(1699);
				createdName();
				setState(1700);
				classCreatorRest();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1702);
				createdName();
				setState(1703);
				arrayCreatorRest();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CreatedNameContext extends ParserRuleContext {
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TypeArgumentsOrDiamondContext> typeArgumentsOrDiamond() {
			return getRuleContexts(TypeArgumentsOrDiamondContext.class);
		}
		public TypeArgumentsOrDiamondContext typeArgumentsOrDiamond(int i) {
			return getRuleContext(TypeArgumentsOrDiamondContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(JavaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(JavaParser.DOT, i);
		}
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public CreatedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createdName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitCreatedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreatedNameContext createdName() throws RecognitionException {
		CreatedNameContext _localctx = new CreatedNameContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_createdName);
		int _la;
		try {
			setState(1722);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXPORTS:
			case MODULE:
			case OPEN:
			case OPENS:
			case PERMITS:
			case PROVIDES:
			case RECORD:
			case REQUIRES:
			case SEALED:
			case TO:
			case TRANSITIVE:
			case USES:
			case VAR:
			case WHEN:
			case WITH:
			case YIELD:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(1707);
				identifier();
				setState(1709);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(1708);
					typeArgumentsOrDiamond();
					}
				}

				setState(1718);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(1711);
					match(DOT);
					setState(1712);
					identifier();
					setState(1714);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LT) {
						{
						setState(1713);
						typeArgumentsOrDiamond();
						}
					}

					}
					}
					setState(1720);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1721);
				primitiveType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InnerCreatorContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ClassCreatorRestContext classCreatorRest() {
			return getRuleContext(ClassCreatorRestContext.class,0);
		}
		public NonWildcardTypeArgumentsOrDiamondContext nonWildcardTypeArgumentsOrDiamond() {
			return getRuleContext(NonWildcardTypeArgumentsOrDiamondContext.class,0);
		}
		public InnerCreatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_innerCreator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitInnerCreator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InnerCreatorContext innerCreator() throws RecognitionException {
		InnerCreatorContext _localctx = new InnerCreatorContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_innerCreator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1724);
			identifier();
			setState(1726);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LT) {
				{
				setState(1725);
				nonWildcardTypeArgumentsOrDiamond();
				}
			}

			setState(1728);
			classCreatorRest();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayCreatorRestContext extends ParserRuleContext {
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public List<TerminalNode> LBRACK() { return getTokens(JavaParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(JavaParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(JavaParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(JavaParser.RBRACK, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public ArrayCreatorRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayCreatorRest; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitArrayCreatorRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayCreatorRestContext arrayCreatorRest() throws RecognitionException {
		ArrayCreatorRestContext _localctx = new ArrayCreatorRestContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_arrayCreatorRest);
		int _la;
		try {
			int _alt;
			setState(1752);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,216,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1732); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1730);
					match(LBRACK);
					setState(1731);
					match(RBRACK);
					}
					}
					setState(1734); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==LBRACK );
				setState(1736);
				arrayInitializer();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1741); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1737);
						match(LBRACK);
						setState(1738);
						expression(0);
						setState(1739);
						match(RBRACK);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1743); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,214,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1749);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,215,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1745);
						match(LBRACK);
						setState(1746);
						match(RBRACK);
						}
						} 
					}
					setState(1751);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,215,_ctx);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClassCreatorRestContext extends ParserRuleContext {
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ClassBodyContext classBody() {
			return getRuleContext(ClassBodyContext.class,0);
		}
		public ClassCreatorRestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_classCreatorRest; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitClassCreatorRest(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClassCreatorRestContext classCreatorRest() throws RecognitionException {
		ClassCreatorRestContext _localctx = new ClassCreatorRestContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_classCreatorRest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1754);
			arguments();
			setState(1756);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,217,_ctx) ) {
			case 1:
				{
				setState(1755);
				classBody();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExplicitGenericInvocationContext extends ParserRuleContext {
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public ExplicitGenericInvocationSuffixContext explicitGenericInvocationSuffix() {
			return getRuleContext(ExplicitGenericInvocationSuffixContext.class,0);
		}
		public ExplicitGenericInvocationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_explicitGenericInvocation; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitExplicitGenericInvocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplicitGenericInvocationContext explicitGenericInvocation() throws RecognitionException {
		ExplicitGenericInvocationContext _localctx = new ExplicitGenericInvocationContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_explicitGenericInvocation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1758);
			nonWildcardTypeArguments();
			setState(1759);
			explicitGenericInvocationSuffix();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeArgumentsOrDiamondContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(JavaParser.LT, 0); }
		public TerminalNode GT() { return getToken(JavaParser.GT, 0); }
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public TypeArgumentsOrDiamondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArgumentsOrDiamond; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeArgumentsOrDiamond(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeArgumentsOrDiamondContext typeArgumentsOrDiamond() throws RecognitionException {
		TypeArgumentsOrDiamondContext _localctx = new TypeArgumentsOrDiamondContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_typeArgumentsOrDiamond);
		try {
			setState(1764);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,218,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1761);
				match(LT);
				setState(1762);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1763);
				typeArguments();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NonWildcardTypeArgumentsOrDiamondContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(JavaParser.LT, 0); }
		public TerminalNode GT() { return getToken(JavaParser.GT, 0); }
		public NonWildcardTypeArgumentsContext nonWildcardTypeArguments() {
			return getRuleContext(NonWildcardTypeArgumentsContext.class,0);
		}
		public NonWildcardTypeArgumentsOrDiamondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonWildcardTypeArgumentsOrDiamond; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitNonWildcardTypeArgumentsOrDiamond(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonWildcardTypeArgumentsOrDiamondContext nonWildcardTypeArgumentsOrDiamond() throws RecognitionException {
		NonWildcardTypeArgumentsOrDiamondContext _localctx = new NonWildcardTypeArgumentsOrDiamondContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_nonWildcardTypeArgumentsOrDiamond);
		try {
			setState(1769);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,219,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1766);
				match(LT);
				setState(1767);
				match(GT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1768);
				nonWildcardTypeArguments();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NonWildcardTypeArgumentsContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(JavaParser.LT, 0); }
		public TypeListContext typeList() {
			return getRuleContext(TypeListContext.class,0);
		}
		public TerminalNode GT() { return getToken(JavaParser.GT, 0); }
		public NonWildcardTypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonWildcardTypeArguments; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitNonWildcardTypeArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NonWildcardTypeArgumentsContext nonWildcardTypeArguments() throws RecognitionException {
		NonWildcardTypeArgumentsContext _localctx = new NonWildcardTypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_nonWildcardTypeArguments);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1771);
			match(LT);
			setState(1772);
			typeList();
			setState(1773);
			match(GT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeListContext extends ParserRuleContext {
		public List<TypeTypeContext> typeType() {
			return getRuleContexts(TypeTypeContext.class);
		}
		public TypeTypeContext typeType(int i) {
			return getRuleContext(TypeTypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public TypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeListContext typeList() throws RecognitionException {
		TypeListContext _localctx = new TypeListContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_typeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1775);
			typeType();
			setState(1780);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1776);
				match(COMMA);
				setState(1777);
				typeType();
				}
				}
				setState(1782);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeTypeContext extends ParserRuleContext {
		public ClassOrInterfaceTypeContext classOrInterfaceType() {
			return getRuleContext(ClassOrInterfaceTypeContext.class,0);
		}
		public PrimitiveTypeContext primitiveType() {
			return getRuleContext(PrimitiveTypeContext.class,0);
		}
		public List<AnnotationContext> annotation() {
			return getRuleContexts(AnnotationContext.class);
		}
		public AnnotationContext annotation(int i) {
			return getRuleContext(AnnotationContext.class,i);
		}
		public List<TerminalNode> LBRACK() { return getTokens(JavaParser.LBRACK); }
		public TerminalNode LBRACK(int i) {
			return getToken(JavaParser.LBRACK, i);
		}
		public List<TerminalNode> RBRACK() { return getTokens(JavaParser.RBRACK); }
		public TerminalNode RBRACK(int i) {
			return getToken(JavaParser.RBRACK, i);
		}
		public TypeTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeType; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeTypeContext typeType() throws RecognitionException {
		TypeTypeContext _localctx = new TypeTypeContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_typeType);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1786);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AT) {
				{
				{
				setState(1783);
				annotation();
				}
				}
				setState(1788);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1791);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXPORTS:
			case MODULE:
			case OPEN:
			case OPENS:
			case PERMITS:
			case PROVIDES:
			case RECORD:
			case REQUIRES:
			case SEALED:
			case TO:
			case TRANSITIVE:
			case USES:
			case VAR:
			case WHEN:
			case WITH:
			case YIELD:
			case IDENTIFIER:
				{
				setState(1789);
				classOrInterfaceType();
				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				{
				setState(1790);
				primitiveType();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1803);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,224,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1796);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==AT) {
						{
						{
						setState(1793);
						annotation();
						}
						}
						setState(1798);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(1799);
					match(LBRACK);
					setState(1800);
					match(RBRACK);
					}
					} 
				}
				setState(1805);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,224,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimitiveTypeContext extends ParserRuleContext {
		public TerminalNode BOOLEAN() { return getToken(JavaParser.BOOLEAN, 0); }
		public TerminalNode CHAR() { return getToken(JavaParser.CHAR, 0); }
		public TerminalNode BYTE() { return getToken(JavaParser.BYTE, 0); }
		public TerminalNode SHORT() { return getToken(JavaParser.SHORT, 0); }
		public TerminalNode INT() { return getToken(JavaParser.INT, 0); }
		public TerminalNode LONG() { return getToken(JavaParser.LONG, 0); }
		public TerminalNode FLOAT() { return getToken(JavaParser.FLOAT, 0); }
		public TerminalNode DOUBLE() { return getToken(JavaParser.DOUBLE, 0); }
		public PrimitiveTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitiveType; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitPrimitiveType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimitiveTypeContext primitiveType() throws RecognitionException {
		PrimitiveTypeContext _localctx = new PrimitiveTypeContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_primitiveType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1806);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 140738832646440L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeArgumentsContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(JavaParser.LT, 0); }
		public List<TypeArgumentContext> typeArgument() {
			return getRuleContexts(TypeArgumentContext.class);
		}
		public TypeArgumentContext typeArgument(int i) {
			return getRuleContext(TypeArgumentContext.class,i);
		}
		public TerminalNode GT() { return getToken(JavaParser.GT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(JavaParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(JavaParser.COMMA, i);
		}
		public TypeArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeArguments; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitTypeArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeArgumentsContext typeArguments() throws RecognitionException {
		TypeArgumentsContext _localctx = new TypeArgumentsContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_typeArguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1808);
			match(LT);
			setState(1809);
			typeArgument();
			setState(1814);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1810);
				match(COMMA);
				setState(1811);
				typeArgument();
				}
				}
				setState(1816);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1817);
			match(GT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SuperSuffixContext extends ParserRuleContext {
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public TerminalNode DOT() { return getToken(JavaParser.DOT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TypeArgumentsContext typeArguments() {
			return getRuleContext(TypeArgumentsContext.class,0);
		}
		public SuperSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_superSuffix; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitSuperSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SuperSuffixContext superSuffix() throws RecognitionException {
		SuperSuffixContext _localctx = new SuperSuffixContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_superSuffix);
		int _la;
		try {
			setState(1828);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LPAREN:
				enterOuterAlt(_localctx, 1);
				{
				setState(1819);
				arguments();
				}
				break;
			case DOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(1820);
				match(DOT);
				setState(1822);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LT) {
					{
					setState(1821);
					typeArguments();
					}
				}

				setState(1824);
				identifier();
				setState(1826);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,227,_ctx) ) {
				case 1:
					{
					setState(1825);
					arguments();
					}
					break;
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExplicitGenericInvocationSuffixContext extends ParserRuleContext {
		public TerminalNode SUPER() { return getToken(JavaParser.SUPER, 0); }
		public SuperSuffixContext superSuffix() {
			return getRuleContext(SuperSuffixContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ArgumentsContext arguments() {
			return getRuleContext(ArgumentsContext.class,0);
		}
		public ExplicitGenericInvocationSuffixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_explicitGenericInvocationSuffix; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitExplicitGenericInvocationSuffix(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExplicitGenericInvocationSuffixContext explicitGenericInvocationSuffix() throws RecognitionException {
		ExplicitGenericInvocationSuffixContext _localctx = new ExplicitGenericInvocationSuffixContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_explicitGenericInvocationSuffix);
		try {
			setState(1835);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SUPER:
				enterOuterAlt(_localctx, 1);
				{
				setState(1830);
				match(SUPER);
				setState(1831);
				superSuffix();
				}
				break;
			case EXPORTS:
			case MODULE:
			case OPEN:
			case OPENS:
			case PERMITS:
			case PROVIDES:
			case RECORD:
			case REQUIRES:
			case SEALED:
			case TO:
			case TRANSITIVE:
			case USES:
			case VAR:
			case WHEN:
			case WITH:
			case YIELD:
			case IDENTIFIER:
				enterOuterAlt(_localctx, 2);
				{
				setState(1832);
				identifier();
				setState(1833);
				arguments();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgumentsContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(JavaParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(JavaParser.RPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ArgumentsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arguments; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof JavaParserVisitor ) return ((JavaParserVisitor<? extends T>)visitor).visitArguments(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentsContext arguments() throws RecognitionException {
		ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_arguments);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1837);
			match(LPAREN);
			setState(1839);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -5862108592017749979L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 4755801464260009983L) != 0)) {
				{
				setState(1838);
				expressionList();
				}
			}

			setState(1841);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 59:
			return annotationFieldValue_sempred((AnnotationFieldValueContext)_localctx, predIndex);
		case 76:
			return recordComponentList_sempred((RecordComponentListContext)_localctx, predIndex);
		case 99:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean annotationFieldValue_sempred(AnnotationFieldValueContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return  this.IsNotIdentifierAssign() ;
		}
		return true;
	}
	private boolean recordComponentList_sempred(RecordComponentListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return  this.DoLastRecordComponent() ;
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 14);
		case 3:
			return precpred(_ctx, 13);
		case 4:
			return precpred(_ctx, 12);
		case 5:
			return precpred(_ctx, 11);
		case 6:
			return precpred(_ctx, 9);
		case 7:
			return precpred(_ctx, 8);
		case 8:
			return precpred(_ctx, 7);
		case 9:
			return precpred(_ctx, 6);
		case 10:
			return precpred(_ctx, 5);
		case 11:
			return precpred(_ctx, 4);
		case 12:
			return precpred(_ctx, 3);
		case 13:
			return precpred(_ctx, 2);
		case 14:
			return precpred(_ctx, 25);
		case 15:
			return precpred(_ctx, 24);
		case 16:
			return precpred(_ctx, 22);
		case 17:
			return precpred(_ctx, 18);
		case 18:
			return precpred(_ctx, 10);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0081\u0734\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007"+
		"\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007"+
		"\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007"+
		"\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007"+
		"\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007"+
		"\'\u0002(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007"+
		",\u0002-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u0007"+
		"1\u00022\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u0007"+
		"6\u00027\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007"+
		";\u0002<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007"+
		"@\u0002A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0002E\u0007"+
		"E\u0002F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007"+
		"J\u0002K\u0007K\u0002L\u0007L\u0002M\u0007M\u0002N\u0007N\u0002O\u0007"+
		"O\u0002P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0002T\u0007"+
		"T\u0002U\u0007U\u0002V\u0007V\u0002W\u0007W\u0002X\u0007X\u0002Y\u0007"+
		"Y\u0002Z\u0007Z\u0002[\u0007[\u0002\\\u0007\\\u0002]\u0007]\u0002^\u0007"+
		"^\u0002_\u0007_\u0002`\u0007`\u0002a\u0007a\u0002b\u0007b\u0002c\u0007"+
		"c\u0002d\u0007d\u0002e\u0007e\u0002f\u0007f\u0002g\u0007g\u0002h\u0007"+
		"h\u0002i\u0007i\u0002j\u0007j\u0002k\u0007k\u0002l\u0007l\u0002m\u0007"+
		"m\u0002n\u0007n\u0002o\u0007o\u0002p\u0007p\u0002q\u0007q\u0002r\u0007"+
		"r\u0002s\u0007s\u0002t\u0007t\u0002u\u0007u\u0002v\u0007v\u0002w\u0007"+
		"w\u0002x\u0007x\u0002y\u0007y\u0002z\u0007z\u0002{\u0007{\u0002|\u0007"+
		"|\u0002}\u0007}\u0002~\u0007~\u0002\u007f\u0007\u007f\u0002\u0080\u0007"+
		"\u0080\u0001\u0000\u0003\u0000\u0104\b\u0000\u0001\u0000\u0001\u0000\u0005"+
		"\u0000\u0108\b\u0000\n\u0000\f\u0000\u010b\t\u0000\u0001\u0000\u0001\u0000"+
		"\u0005\u0000\u010f\b\u0000\n\u0000\f\u0000\u0112\t\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0003\u0000\u0118\b\u0000\u0001\u0001\u0005"+
		"\u0001\u011b\b\u0001\n\u0001\f\u0001\u011e\t\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0005\u0002\u0123\b\u0002\n\u0002\f\u0002\u0126\t\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0003"+
		"\u0003\u012e\b\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u0133"+
		"\b\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0005\u0004\u0138\b\u0004"+
		"\n\u0004\f\u0004\u013b\t\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0003\u0004\u0142\b\u0004\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u0149\b\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u0155\b\u0006\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u0159\b\u0007\u0001\b\u0001\b\u0001\b\u0003\b\u015e"+
		"\b\b\u0001\b\u0001\b\u0003\b\u0162\b\b\u0001\b\u0001\b\u0003\b\u0166\b"+
		"\b\u0001\b\u0001\b\u0003\b\u016a\b\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0005\t\u0172\b\t\n\t\f\t\u0175\t\t\u0001\t\u0001\t\u0001\n"+
		"\u0005\n\u017a\b\n\n\n\f\n\u017d\t\n\u0001\n\u0001\n\u0001\n\u0005\n\u0182"+
		"\b\n\n\n\f\n\u0185\t\n\u0001\n\u0003\n\u0188\b\n\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0005\u000b\u018d\b\u000b\n\u000b\f\u000b\u0190\t\u000b\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0003\f\u0196\b\f\u0001\f\u0001\f\u0003\f\u019a"+
		"\b\f\u0001\f\u0003\f\u019d\b\f\u0001\f\u0003\f\u01a0\b\f\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0001\r\u0005\r\u01a7\b\r\n\r\f\r\u01aa\t\r\u0001\u000e"+
		"\u0005\u000e\u01ad\b\u000e\n\u000e\f\u000e\u01b0\t\u000e\u0001\u000e\u0001"+
		"\u000e\u0003\u000e\u01b4\b\u000e\u0001\u000e\u0003\u000e\u01b7\b\u000e"+
		"\u0001\u000f\u0001\u000f\u0005\u000f\u01bb\b\u000f\n\u000f\f\u000f\u01be"+
		"\t\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u01c3\b\u0010"+
		"\u0001\u0010\u0001\u0010\u0003\u0010\u01c7\b\u0010\u0001\u0010\u0001\u0010"+
		"\u0003\u0010\u01cb\b\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011"+
		"\u0005\u0011\u01d1\b\u0011\n\u0011\f\u0011\u01d4\t\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0012\u0001\u0012\u0005\u0012\u01da\b\u0012\n\u0012\f\u0012"+
		"\u01dd\t\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0003\u0013"+
		"\u01e3\b\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u01e7\b\u0013\n\u0013"+
		"\f\u0013\u01ea\t\u0013\u0001\u0013\u0003\u0013\u01ed\b\u0013\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u01f9\b\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u0200\b\u0015"+
		"\n\u0015\f\u0015\u0203\t\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u0207"+
		"\b\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0003\u0016\u020d"+
		"\b\u0016\u0001\u0017\u0001\u0017\u0003\u0017\u0211\b\u0017\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u021d\b\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001b\u0005\u001b\u0222\b\u001b\n\u001b\f\u001b\u0225"+
		"\t\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001d\u0005\u001d\u022f\b\u001d\n\u001d\f\u001d"+
		"\u0232\t\u001d\u0001\u001d\u0001\u001d\u0003\u001d\u0236\b\u001d\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0003\u001e\u0240\b\u001e\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0005\u001f\u0246\b\u001f\n\u001f\f\u001f\u0249\t\u001f"+
		"\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0005 \u0250\b \n \f \u0253"+
		"\t \u0001 \u0001 \u0001 \u0001!\u0005!\u0259\b!\n!\f!\u025c\t!\u0001!"+
		"\u0001!\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0003\"\u0266"+
		"\b\"\u0001#\u0005#\u0269\b#\n#\f#\u026c\t#\u0001#\u0001#\u0001#\u0001"+
		"$\u0005$\u0272\b$\n$\f$\u0275\t$\u0001$\u0001$\u0001$\u0001$\u0001$\u0005"+
		"$\u027c\b$\n$\f$\u027f\t$\u0001$\u0001$\u0003$\u0283\b$\u0001$\u0001$"+
		"\u0001%\u0001%\u0001%\u0005%\u028a\b%\n%\f%\u028d\t%\u0001&\u0001&\u0001"+
		"&\u0003&\u0292\b&\u0001\'\u0001\'\u0001\'\u0005\'\u0297\b\'\n\'\f\'\u029a"+
		"\t\'\u0001(\u0001(\u0003(\u029e\b(\u0001)\u0001)\u0001)\u0001)\u0005)"+
		"\u02a4\b)\n)\f)\u02a7\t)\u0001)\u0003)\u02aa\b)\u0003)\u02ac\b)\u0001"+
		")\u0001)\u0001*\u0001*\u0001*\u0005*\u02b3\b*\n*\f*\u02b6\t*\u0003*\u02b8"+
		"\b*\u0001*\u0001*\u0003*\u02bc\b*\u0004*\u02be\b*\u000b*\f*\u02bf\u0001"+
		"*\u0001*\u0005*\u02c4\b*\n*\f*\u02c7\t*\u0001*\u0001*\u0003*\u02cb\b*"+
		"\u0005*\u02cd\b*\n*\f*\u02d0\t*\u0001+\u0001+\u0001+\u0005+\u02d5\b+\n"+
		"+\f+\u02d8\t+\u0001,\u0001,\u0005,\u02dc\b,\n,\f,\u02df\t,\u0001,\u0001"+
		",\u0001,\u0003,\u02e4\b,\u0003,\u02e6\b,\u0001-\u0001-\u0001-\u0005-\u02eb"+
		"\b-\n-\f-\u02ee\t-\u0001.\u0001.\u0001.\u0003.\u02f3\b.\u0001.\u0001."+
		"\u0005.\u02f7\b.\n.\f.\u02fa\t.\u0003.\u02fc\b.\u0001.\u0001.\u0001/\u0001"+
		"/\u0001/\u0001/\u0005/\u0304\b/\n/\f/\u0307\t/\u0001/\u0001/\u00010\u0001"+
		"0\u00010\u00050\u030e\b0\n0\f0\u0311\t0\u00011\u00051\u0314\b1\n1\f1\u0317"+
		"\t1\u00011\u00011\u00051\u031b\b1\n1\f1\u031e\t1\u00011\u00031\u0321\b"+
		"1\u00011\u00011\u00012\u00012\u00012\u00052\u0328\b2\n2\f2\u032b\t2\u0001"+
		"3\u00053\u032e\b3\n3\f3\u0331\t3\u00013\u00013\u00013\u00014\u00014\u0001"+
		"4\u00054\u0339\b4\n4\f4\u033c\t4\u00015\u00015\u00015\u00015\u00015\u0001"+
		"5\u00015\u00035\u0345\b5\u00016\u00016\u00017\u00017\u00018\u00018\u0001"+
		"8\u00058\u034e\b8\n8\f8\u0351\t8\u00018\u00018\u00018\u00019\u00019\u0001"+
		"9\u00019\u00039\u035a\b9\u0001:\u0001:\u0001:\u0001:\u0005:\u0360\b:\n"+
		":\f:\u0363\t:\u0003:\u0365\b:\u0001:\u0001:\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0003;\u036f\b;\u0001<\u0001<\u0001<\u0001<\u0001<\u0001"+
		"<\u0005<\u0377\b<\n<\f<\u037a\t<\u0003<\u037c\b<\u0001<\u0003<\u037f\b"+
		"<\u0001<\u0003<\u0382\b<\u0001=\u0001=\u0001=\u0003=\u0387\b=\u0001>\u0001"+
		">\u0001>\u0001>\u0005>\u038d\b>\n>\f>\u0390\t>\u0003>\u0392\b>\u0001>"+
		"\u0003>\u0395\b>\u0001>\u0001>\u0001?\u0001?\u0001?\u0001?\u0001?\u0001"+
		"@\u0001@\u0005@\u03a0\b@\n@\f@\u03a3\t@\u0001@\u0001@\u0001A\u0005A\u03a8"+
		"\bA\nA\fA\u03ab\tA\u0001A\u0001A\u0003A\u03af\bA\u0001B\u0001B\u0001B"+
		"\u0001B\u0001B\u0001B\u0003B\u03b7\bB\u0001B\u0001B\u0003B\u03bb\bB\u0001"+
		"B\u0001B\u0003B\u03bf\bB\u0001B\u0001B\u0003B\u03c3\bB\u0001B\u0001B\u0003"+
		"B\u03c7\bB\u0003B\u03c9\bB\u0001C\u0001C\u0003C\u03cd\bC\u0001D\u0001"+
		"D\u0001D\u0001D\u0003D\u03d3\bD\u0001E\u0001E\u0001F\u0001F\u0001F\u0001"+
		"G\u0005G\u03db\bG\nG\fG\u03de\tG\u0001G\u0003G\u03e1\bG\u0001G\u0001G"+
		"\u0001G\u0001G\u0005G\u03e7\bG\nG\fG\u03ea\tG\u0001G\u0001G\u0001H\u0001"+
		"H\u0005H\u03f0\bH\nH\fH\u03f3\tH\u0001H\u0001H\u0001H\u0001H\u0001H\u0001"+
		"H\u0001H\u0001H\u0001H\u0005H\u03fe\bH\nH\fH\u0401\tH\u0003H\u0403\bH"+
		"\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0005H\u040d"+
		"\bH\nH\fH\u0410\tH\u0003H\u0412\bH\u0001H\u0001H\u0001H\u0001H\u0001H"+
		"\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0001H\u0005H\u0420\bH\nH\f"+
		"H\u0423\tH\u0001H\u0001H\u0003H\u0427\bH\u0001I\u0001I\u0001J\u0001J\u0001"+
		"J\u0003J\u042e\bJ\u0001J\u0001J\u0001J\u0003J\u0433\bJ\u0001J\u0001J\u0001"+
		"K\u0001K\u0003K\u0439\bK\u0001K\u0001K\u0001L\u0001L\u0001L\u0005L\u0440"+
		"\bL\nL\fL\u0443\tL\u0001L\u0001L\u0001M\u0005M\u0448\bM\nM\fM\u044b\t"+
		"M\u0001M\u0001M\u0005M\u044f\bM\nM\fM\u0452\tM\u0001M\u0003M\u0455\bM"+
		"\u0001M\u0001M\u0001N\u0001N\u0001N\u0005N\u045c\bN\nN\fN\u045f\tN\u0001"+
		"N\u0001N\u0001O\u0001O\u0005O\u0465\bO\nO\fO\u0468\tO\u0001O\u0001O\u0001"+
		"P\u0001P\u0001P\u0001P\u0001P\u0003P\u0471\bP\u0001Q\u0005Q\u0474\bQ\n"+
		"Q\fQ\u0477\tQ\u0001Q\u0001Q\u0001Q\u0001Q\u0001Q\u0001Q\u0001Q\u0001Q"+
		"\u0003Q\u0481\bQ\u0001R\u0001R\u0001S\u0001S\u0001T\u0005T\u0488\bT\n"+
		"T\fT\u048b\tT\u0001T\u0001T\u0001T\u0001T\u0003T\u0491\bT\u0001U\u0001"+
		"U\u0001U\u0001U\u0001U\u0003U\u0498\bU\u0001U\u0001U\u0001U\u0001U\u0001"+
		"U\u0001U\u0001U\u0001U\u0001U\u0003U\u04a3\bU\u0001U\u0001U\u0001U\u0001"+
		"U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001"+
		"U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0004"+
		"U\u04bc\bU\u000bU\fU\u04bd\u0001U\u0003U\u04c1\bU\u0001U\u0003U\u04c4"+
		"\bU\u0001U\u0001U\u0001U\u0001U\u0005U\u04ca\bU\nU\fU\u04cd\tU\u0001U"+
		"\u0003U\u04d0\bU\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0005U\u04d8"+
		"\bU\nU\fU\u04db\tU\u0001U\u0005U\u04de\bU\nU\fU\u04e1\tU\u0001U\u0001"+
		"U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0003U\u04ed"+
		"\bU\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0003U\u04f6\bU\u0001"+
		"U\u0001U\u0001U\u0003U\u04fb\bU\u0001U\u0001U\u0001U\u0001U\u0001U\u0001"+
		"U\u0001U\u0001U\u0001U\u0001U\u0001U\u0003U\u0508\bU\u0001U\u0001U\u0001"+
		"U\u0001U\u0003U\u050e\bU\u0001V\u0001V\u0001V\u0005V\u0513\bV\nV\fV\u0516"+
		"\tV\u0001V\u0001V\u0001V\u0001V\u0001V\u0001W\u0001W\u0001W\u0005W\u0520"+
		"\bW\nW\fW\u0523\tW\u0001X\u0001X\u0001X\u0001Y\u0001Y\u0001Y\u0003Y\u052b"+
		"\bY\u0001Y\u0001Y\u0001Z\u0001Z\u0001Z\u0005Z\u0532\bZ\nZ\fZ\u0535\tZ"+
		"\u0001[\u0005[\u0538\b[\n[\f[\u053b\t[\u0001[\u0001[\u0001[\u0001[\u0001"+
		"[\u0003[\u0542\b[\u0001[\u0001[\u0001[\u0001[\u0003[\u0548\b[\u0001\\"+
		"\u0001\\\u0001\\\u0004\\\u054d\b\\\u000b\\\f\\\u054e\u0001\\\u0004\\\u0552"+
		"\b\\\u000b\\\f\\\u0553\u0001]\u0001]\u0001]\u0001]\u0001]\u0001]\u0003"+
		"]\u055c\b]\u0001]\u0003]\u055f\b]\u0001^\u0001^\u0003^\u0563\b^\u0001"+
		"^\u0001^\u0003^\u0567\b^\u0001^\u0001^\u0003^\u056b\b^\u0003^\u056d\b"+
		"^\u0001_\u0001_\u0003_\u0571\b_\u0001`\u0005`\u0574\b`\n`\f`\u0577\t`"+
		"\u0001`\u0001`\u0003`\u057b\b`\u0001`\u0001`\u0001`\u0001`\u0001a\u0001"+
		"a\u0001a\u0005a\u0584\ba\na\fa\u0587\ta\u0001b\u0001b\u0001b\u0003b\u058c"+
		"\bb\u0001b\u0001b\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0003c\u0596"+
		"\bc\u0001c\u0001c\u0003c\u059a\bc\u0001c\u0001c\u0001c\u0003c\u059f\b"+
		"c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0005c\u05a8\bc\nc"+
		"\fc\u05ab\tc\u0001c\u0001c\u0001c\u0005c\u05b0\bc\nc\fc\u05b3\tc\u0001"+
		"c\u0001c\u0001c\u0001c\u0001c\u0001c\u0003c\u05bb\bc\u0001c\u0001c\u0001"+
		"c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001"+
		"c\u0001c\u0003c\u05cb\bc\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001"+
		"c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001"+
		"c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001"+
		"c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001"+
		"c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0003c\u05f8\bc\u0001c\u0001"+
		"c\u0001c\u0001c\u0003c\u05fe\bc\u0001c\u0001c\u0001c\u0003c\u0603\bc\u0001"+
		"c\u0001c\u0001c\u0001c\u0001c\u0001c\u0001c\u0003c\u060c\bc\u0005c\u060e"+
		"\bc\nc\fc\u0611\tc\u0001d\u0005d\u0614\bd\nd\fd\u0617\td\u0001d\u0001"+
		"d\u0005d\u061b\bd\nd\fd\u061e\td\u0001d\u0001d\u0001d\u0001d\u0001d\u0003"+
		"d\u0625\bd\u0001d\u0001d\u0003d\u0629\bd\u0001e\u0001e\u0001e\u0005e\u062e"+
		"\be\ne\fe\u0631\te\u0001f\u0001f\u0001g\u0001g\u0001g\u0001g\u0001h\u0001"+
		"h\u0001h\u0003h\u063c\bh\u0001h\u0001h\u0001h\u0001h\u0001h\u0005h\u0643"+
		"\bh\nh\fh\u0646\th\u0001h\u0001h\u0001h\u0001h\u0003h\u064c\bh\u0001h"+
		"\u0003h\u064f\bh\u0001i\u0001i\u0003i\u0653\bi\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0003j\u0665\bj\u0003j\u0667\bj\u0001k\u0001k\u0001k\u0001"+
		"k\u0001k\u0001k\u0005k\u066f\bk\nk\fk\u0672\tk\u0001k\u0001k\u0001l\u0001"+
		"l\u0001l\u0001l\u0001l\u0003l\u067b\bl\u0001l\u0001l\u0001l\u0005l\u0680"+
		"\bl\nl\fl\u0683\tl\u0001l\u0003l\u0686\bl\u0003l\u0688\bl\u0001l\u0001"+
		"l\u0001l\u0001l\u0001l\u0003l\u068f\bl\u0001m\u0001m\u0001m\u0001n\u0001"+
		"n\u0001o\u0001o\u0005o\u0698\bo\no\fo\u069b\to\u0003o\u069d\bo\u0001p"+
		"\u0001p\u0001q\u0003q\u06a2\bq\u0001q\u0001q\u0001q\u0001q\u0001q\u0001"+
		"q\u0003q\u06aa\bq\u0001r\u0001r\u0003r\u06ae\br\u0001r\u0001r\u0001r\u0003"+
		"r\u06b3\br\u0005r\u06b5\br\nr\fr\u06b8\tr\u0001r\u0003r\u06bb\br\u0001"+
		"s\u0001s\u0003s\u06bf\bs\u0001s\u0001s\u0001t\u0001t\u0004t\u06c5\bt\u000b"+
		"t\ft\u06c6\u0001t\u0001t\u0001t\u0001t\u0001t\u0004t\u06ce\bt\u000bt\f"+
		"t\u06cf\u0001t\u0001t\u0005t\u06d4\bt\nt\ft\u06d7\tt\u0003t\u06d9\bt\u0001"+
		"u\u0001u\u0003u\u06dd\bu\u0001v\u0001v\u0001v\u0001w\u0001w\u0001w\u0003"+
		"w\u06e5\bw\u0001x\u0001x\u0001x\u0003x\u06ea\bx\u0001y\u0001y\u0001y\u0001"+
		"y\u0001z\u0001z\u0001z\u0005z\u06f3\bz\nz\fz\u06f6\tz\u0001{\u0005{\u06f9"+
		"\b{\n{\f{\u06fc\t{\u0001{\u0001{\u0003{\u0700\b{\u0001{\u0005{\u0703\b"+
		"{\n{\f{\u0706\t{\u0001{\u0001{\u0005{\u070a\b{\n{\f{\u070d\t{\u0001|\u0001"+
		"|\u0001}\u0001}\u0001}\u0001}\u0005}\u0715\b}\n}\f}\u0718\t}\u0001}\u0001"+
		"}\u0001~\u0001~\u0001~\u0003~\u071f\b~\u0001~\u0001~\u0003~\u0723\b~\u0003"+
		"~\u0725\b~\u0001\u007f\u0001\u007f\u0001\u007f\u0001\u007f\u0001\u007f"+
		"\u0003\u007f\u072c\b\u007f\u0001\u0080\u0001\u0080\u0003\u0080\u0730\b"+
		"\u0080\u0001\u0080\u0001\u0080\u0001\u0080\u0000\u0001\u00c6\u0081\u0000"+
		"\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c"+
		"\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084"+
		"\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c"+
		"\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4"+
		"\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc"+
		"\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8\u00da\u00dc\u00de\u00e0\u00e2\u00e4"+
		"\u00e6\u00e8\u00ea\u00ec\u00ee\u00f0\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc"+
		"\u00fe\u0100\u0000\u000f\u0002\u0000\u0012\u001222\u0001\u0000DG\u0001"+
		"\u0000HI\u0002\u000000::\r\u0000\u0011\u0011\u001f\u001f#$&&))+,..88:"+
		":<=@@BC\u0081\u0081\u000b\u0000\u0011\u0011\u001f\u001f#$)),,..88::<<"+
		"BB\u0081\u0081\u0002\u0000[\\eh\u0002\u0000ijnn\u0001\u0000gh\u0002\u0000"+
		"YZ`a\u0002\u0000__bb\u0002\u0000XXoy\u0001\u0000ef\u0002\u0000^^zz\b\u0000"+
		"\u0003\u0003\u0005\u0005\b\b\u000e\u000e\u0015\u0015\u001c\u001c\u001e"+
		"\u001e//\u0801\u0000\u0117\u0001\u0000\u0000\u0000\u0002\u011c\u0001\u0000"+
		"\u0000\u0000\u0004\u0124\u0001\u0000\u0000\u0000\u0006\u012b\u0001\u0000"+
		"\u0000\u0000\b\u0139\u0001\u0000\u0000\u0000\n\u0148\u0001\u0000\u0000"+
		"\u0000\f\u0154\u0001\u0000\u0000\u0000\u000e\u0158\u0001\u0000\u0000\u0000"+
		"\u0010\u015a\u0001\u0000\u0000\u0000\u0012\u016d\u0001\u0000\u0000\u0000"+
		"\u0014\u017b\u0001\u0000\u0000\u0000\u0016\u0189\u0001\u0000\u0000\u0000"+
		"\u0018\u0191\u0001\u0000\u0000\u0000\u001a\u01a3\u0001\u0000\u0000\u0000"+
		"\u001c\u01ae\u0001\u0000\u0000\u0000\u001e\u01b8\u0001\u0000\u0000\u0000"+
		" \u01bf\u0001\u0000\u0000\u0000\"\u01ce\u0001\u0000\u0000\u0000$\u01d7"+
		"\u0001\u0000\u0000\u0000&\u01ec\u0001\u0000\u0000\u0000(\u01f8\u0001\u0000"+
		"\u0000\u0000*\u01fa\u0001\u0000\u0000\u0000,\u020c\u0001\u0000\u0000\u0000"+
		".\u0210\u0001\u0000\u0000\u00000\u0212\u0001\u0000\u0000\u00002\u0215"+
		"\u0001\u0000\u0000\u00004\u0218\u0001\u0000\u0000\u00006\u0223\u0001\u0000"+
		"\u0000\u00008\u0229\u0001\u0000\u0000\u0000:\u0235\u0001\u0000\u0000\u0000"+
		"<\u023f\u0001\u0000\u0000\u0000>\u0241\u0001\u0000\u0000\u0000@\u024c"+
		"\u0001\u0000\u0000\u0000B\u025a\u0001\u0000\u0000\u0000D\u0265\u0001\u0000"+
		"\u0000\u0000F\u026a\u0001\u0000\u0000\u0000H\u0273\u0001\u0000\u0000\u0000"+
		"J\u0286\u0001\u0000\u0000\u0000L\u028e\u0001\u0000\u0000\u0000N\u0293"+
		"\u0001\u0000\u0000\u0000P\u029d\u0001\u0000\u0000\u0000R\u029f\u0001\u0000"+
		"\u0000\u0000T\u02bd\u0001\u0000\u0000\u0000V\u02d1\u0001\u0000\u0000\u0000"+
		"X\u02e5\u0001\u0000\u0000\u0000Z\u02e7\u0001\u0000\u0000\u0000\\\u02ef"+
		"\u0001\u0000\u0000\u0000^\u02ff\u0001\u0000\u0000\u0000`\u030a\u0001\u0000"+
		"\u0000\u0000b\u0315\u0001\u0000\u0000\u0000d\u0324\u0001\u0000\u0000\u0000"+
		"f\u032f\u0001\u0000\u0000\u0000h\u0335\u0001\u0000\u0000\u0000j\u0344"+
		"\u0001\u0000\u0000\u0000l\u0346\u0001\u0000\u0000\u0000n\u0348\u0001\u0000"+
		"\u0000\u0000p\u034f\u0001\u0000\u0000\u0000r\u0355\u0001\u0000\u0000\u0000"+
		"t\u035b\u0001\u0000\u0000\u0000v\u036e\u0001\u0000\u0000\u0000x\u0381"+
		"\u0001\u0000\u0000\u0000z\u0386\u0001\u0000\u0000\u0000|\u0388\u0001\u0000"+
		"\u0000\u0000~\u0398\u0001\u0000\u0000\u0000\u0080\u039d\u0001\u0000\u0000"+
		"\u0000\u0082\u03ae\u0001\u0000\u0000\u0000\u0084\u03c8\u0001\u0000\u0000"+
		"\u0000\u0086\u03cc\u0001\u0000\u0000\u0000\u0088\u03ce\u0001\u0000\u0000"+
		"\u0000\u008a\u03d4\u0001\u0000\u0000\u0000\u008c\u03d6\u0001\u0000\u0000"+
		"\u0000\u008e\u03dc\u0001\u0000\u0000\u0000\u0090\u0426\u0001\u0000\u0000"+
		"\u0000\u0092\u0428\u0001\u0000\u0000\u0000\u0094\u042a\u0001\u0000\u0000"+
		"\u0000\u0096\u0436\u0001\u0000\u0000\u0000\u0098\u043c\u0001\u0000\u0000"+
		"\u0000\u009a\u0449\u0001\u0000\u0000\u0000\u009c\u0458\u0001\u0000\u0000"+
		"\u0000\u009e\u0462\u0001\u0000\u0000\u0000\u00a0\u0470\u0001\u0000\u0000"+
		"\u0000\u00a2\u0475\u0001\u0000\u0000\u0000\u00a4\u0482\u0001\u0000\u0000"+
		"\u0000\u00a6\u0484\u0001\u0000\u0000\u0000\u00a8\u0489\u0001\u0000\u0000"+
		"\u0000\u00aa\u050d\u0001\u0000\u0000\u0000\u00ac\u050f\u0001\u0000\u0000"+
		"\u0000\u00ae\u051c\u0001\u0000\u0000\u0000\u00b0\u0524\u0001\u0000\u0000"+
		"\u0000\u00b2\u0527\u0001\u0000\u0000\u0000\u00b4\u052e\u0001\u0000\u0000"+
		"\u0000\u00b6\u0547\u0001\u0000\u0000\u0000\u00b8\u054c\u0001\u0000\u0000"+
		"\u0000\u00ba\u055e\u0001\u0000\u0000\u0000\u00bc\u056c\u0001\u0000\u0000"+
		"\u0000\u00be\u0570\u0001\u0000\u0000\u0000\u00c0\u0575\u0001\u0000\u0000"+
		"\u0000\u00c2\u0580\u0001\u0000\u0000\u0000\u00c4\u058b\u0001\u0000\u0000"+
		"\u0000\u00c6\u05ba\u0001\u0000\u0000\u0000\u00c8\u0628\u0001\u0000\u0000"+
		"\u0000\u00ca\u062a\u0001\u0000\u0000\u0000\u00cc\u0632\u0001\u0000\u0000"+
		"\u0000\u00ce\u0634\u0001\u0000\u0000\u0000\u00d0\u064e\u0001\u0000\u0000"+
		"\u0000\u00d2\u0652\u0001\u0000\u0000\u0000\u00d4\u0666\u0001\u0000\u0000"+
		"\u0000\u00d6\u0668\u0001\u0000\u0000\u0000\u00d8\u068e\u0001\u0000\u0000"+
		"\u0000\u00da\u0690\u0001\u0000\u0000\u0000\u00dc\u0693\u0001\u0000\u0000"+
		"\u0000\u00de\u069c\u0001\u0000\u0000\u0000\u00e0\u069e\u0001\u0000\u0000"+
		"\u0000\u00e2\u06a9\u0001\u0000\u0000\u0000\u00e4\u06ba\u0001\u0000\u0000"+
		"\u0000\u00e6\u06bc\u0001\u0000\u0000\u0000\u00e8\u06d8\u0001\u0000\u0000"+
		"\u0000\u00ea\u06da\u0001\u0000\u0000\u0000\u00ec\u06de\u0001\u0000\u0000"+
		"\u0000\u00ee\u06e4\u0001\u0000\u0000\u0000\u00f0\u06e9\u0001\u0000\u0000"+
		"\u0000\u00f2\u06eb\u0001\u0000\u0000\u0000\u00f4\u06ef\u0001\u0000\u0000"+
		"\u0000\u00f6\u06fa\u0001\u0000\u0000\u0000\u00f8\u070e\u0001\u0000\u0000"+
		"\u0000\u00fa\u0710\u0001\u0000\u0000\u0000\u00fc\u0724\u0001\u0000\u0000"+
		"\u0000\u00fe\u072b\u0001\u0000\u0000\u0000\u0100\u072d\u0001\u0000\u0000"+
		"\u0000\u0102\u0104\u0003\u0004\u0002\u0000\u0103\u0102\u0001\u0000\u0000"+
		"\u0000\u0103\u0104\u0001\u0000\u0000\u0000\u0104\u0109\u0001\u0000\u0000"+
		"\u0000\u0105\u0108\u0003\u0006\u0003\u0000\u0106\u0108\u0005U\u0000\u0000"+
		"\u0107\u0105\u0001\u0000\u0000\u0000\u0107\u0106\u0001\u0000\u0000\u0000"+
		"\u0108\u010b\u0001\u0000\u0000\u0000\u0109\u0107\u0001\u0000\u0000\u0000"+
		"\u0109\u010a\u0001\u0000\u0000\u0000\u010a\u0110\u0001\u0000\u0000\u0000"+
		"\u010b\u0109\u0001\u0000\u0000\u0000\u010c\u010f\u0003\b\u0004\u0000\u010d"+
		"\u010f\u0005U\u0000\u0000\u010e\u010c\u0001\u0000\u0000\u0000\u010e\u010d"+
		"\u0001\u0000\u0000\u0000\u010f\u0112\u0001\u0000\u0000\u0000\u0110\u010e"+
		"\u0001\u0000\u0000\u0000\u0110\u0111\u0001\u0000\u0000\u0000\u0111\u0113"+
		"\u0001\u0000\u0000\u0000\u0112\u0110\u0001\u0000\u0000\u0000\u0113\u0118"+
		"\u0005\u0000\u0000\u0001\u0114\u0115\u0003\u0002\u0001\u0000\u0115\u0116"+
		"\u0005\u0000\u0000\u0001\u0116\u0118\u0001\u0000\u0000\u0000\u0117\u0103"+
		"\u0001\u0000\u0000\u0000\u0117\u0114\u0001\u0000\u0000\u0000\u0118\u0001"+
		"\u0001\u0000\u0000\u0000\u0119\u011b\u0003\u0006\u0003\u0000\u011a\u0119"+
		"\u0001\u0000\u0000\u0000\u011b\u011e\u0001\u0000\u0000\u0000\u011c\u011a"+
		"\u0001\u0000\u0000\u0000\u011c\u011d\u0001\u0000\u0000\u0000\u011d\u011f"+
		"\u0001\u0000\u0000\u0000\u011e\u011c\u0001\u0000\u0000\u0000\u011f\u0120"+
		"\u0003\u008eG\u0000\u0120\u0003\u0001\u0000\u0000\u0000\u0121\u0123\u0003"+
		"r9\u0000\u0122\u0121\u0001\u0000\u0000\u0000\u0123\u0126\u0001\u0000\u0000"+
		"\u0000\u0124\u0122\u0001\u0000\u0000\u0000\u0124\u0125\u0001\u0000\u0000"+
		"\u0000\u0125\u0127\u0001\u0000\u0000\u0000\u0126\u0124\u0001\u0000\u0000"+
		"\u0000\u0127\u0128\u0005%\u0000\u0000\u0128\u0129\u0003h4\u0000\u0129"+
		"\u012a\u0005U\u0000\u0000\u012a\u0005\u0001\u0000\u0000\u0000\u012b\u012d"+
		"\u0005\u001a\u0000\u0000\u012c\u012e\u00050\u0000\u0000\u012d\u012c\u0001"+
		"\u0000\u0000\u0000\u012d\u012e\u0001\u0000\u0000\u0000\u012e\u012f\u0001"+
		"\u0000\u0000\u0000\u012f\u0132\u0003h4\u0000\u0130\u0131\u0005W\u0000"+
		"\u0000\u0131\u0133\u0005i\u0000\u0000\u0132\u0130\u0001\u0000\u0000\u0000"+
		"\u0132\u0133\u0001\u0000\u0000\u0000\u0133\u0134\u0001\u0000\u0000\u0000"+
		"\u0134\u0135\u0005U\u0000\u0000\u0135\u0007\u0001\u0000\u0000\u0000\u0136"+
		"\u0138\u0003\f\u0006\u0000\u0137\u0136\u0001\u0000\u0000\u0000\u0138\u013b"+
		"\u0001\u0000\u0000\u0000\u0139\u0137\u0001\u0000\u0000\u0000\u0139\u013a"+
		"\u0001\u0000\u0000\u0000\u013a\u0141\u0001\u0000\u0000\u0000\u013b\u0139"+
		"\u0001\u0000\u0000\u0000\u013c\u0142\u0003\u0010\b\u0000\u013d\u0142\u0003"+
		"\u0018\f\u0000\u013e\u0142\u0003 \u0010\u0000\u013f\u0142\u0003~?\u0000"+
		"\u0140\u0142\u0003\u0094J\u0000\u0141\u013c\u0001\u0000\u0000\u0000\u0141"+
		"\u013d\u0001\u0000\u0000\u0000\u0141\u013e\u0001\u0000\u0000\u0000\u0141"+
		"\u013f\u0001\u0000\u0000\u0000\u0141\u0140\u0001\u0000\u0000\u0000\u0142"+
		"\t\u0001\u0000\u0000\u0000\u0143\u0149\u0003\f\u0006\u0000\u0144\u0149"+
		"\u0005 \u0000\u0000\u0145\u0149\u00054\u0000\u0000\u0146\u0149\u00059"+
		"\u0000\u0000\u0147\u0149\u0005?\u0000\u0000\u0148\u0143\u0001\u0000\u0000"+
		"\u0000\u0148\u0144\u0001\u0000\u0000\u0000\u0148\u0145\u0001\u0000\u0000"+
		"\u0000\u0148\u0146\u0001\u0000\u0000\u0000\u0148\u0147\u0001\u0000\u0000"+
		"\u0000\u0149\u000b\u0001\u0000\u0000\u0000\u014a\u0155\u0003r9\u0000\u014b"+
		"\u0155\u0005*\u0000\u0000\u014c\u0155\u0005(\u0000\u0000\u014d\u0155\u0005"+
		"\'\u0000\u0000\u014e\u0155\u00050\u0000\u0000\u014f\u0155\u0005\u0001"+
		"\u0000\u0000\u0150\u0155\u0005\u0013\u0000\u0000\u0151\u0155\u00051\u0000"+
		"\u0000\u0152\u0155\u0005.\u0000\u0000\u0153\u0155\u0005\"\u0000\u0000"+
		"\u0154\u014a\u0001\u0000\u0000\u0000\u0154\u014b\u0001\u0000\u0000\u0000"+
		"\u0154\u014c\u0001\u0000\u0000\u0000\u0154\u014d\u0001\u0000\u0000\u0000"+
		"\u0154\u014e\u0001\u0000\u0000\u0000\u0154\u014f\u0001\u0000\u0000\u0000"+
		"\u0154\u0150\u0001\u0000\u0000\u0000\u0154\u0151\u0001\u0000\u0000\u0000"+
		"\u0154\u0152\u0001\u0000\u0000\u0000\u0154\u0153\u0001\u0000\u0000\u0000"+
		"\u0155\r\u0001\u0000\u0000\u0000\u0156\u0159\u0005\u0013\u0000\u0000\u0157"+
		"\u0159\u0003r9\u0000\u0158\u0156\u0001\u0000\u0000\u0000\u0158\u0157\u0001"+
		"\u0000\u0000\u0000\u0159\u000f\u0001\u0000\u0000\u0000\u015a\u015b\u0005"+
		"\t\u0000\u0000\u015b\u015d\u0003\u00a4R\u0000\u015c\u015e\u0003\u0012"+
		"\t\u0000\u015d\u015c\u0001\u0000\u0000\u0000\u015d\u015e\u0001\u0000\u0000"+
		"\u0000\u015e\u0161\u0001\u0000\u0000\u0000\u015f\u0160\u0005\u0012\u0000"+
		"\u0000\u0160\u0162\u0003\u00f6{\u0000\u0161\u015f\u0001\u0000\u0000\u0000"+
		"\u0161\u0162\u0001\u0000\u0000\u0000\u0162\u0165\u0001\u0000\u0000\u0000"+
		"\u0163\u0164\u0005\u0019\u0000\u0000\u0164\u0166\u0003\u00f4z\u0000\u0165"+
		"\u0163\u0001\u0000\u0000\u0000\u0165\u0166\u0001\u0000\u0000\u0000\u0166"+
		"\u0169\u0001\u0000\u0000\u0000\u0167\u0168\u0005&\u0000\u0000\u0168\u016a"+
		"\u0003\u00f4z\u0000\u0169\u0167\u0001\u0000\u0000\u0000\u0169\u016a\u0001"+
		"\u0000\u0000\u0000\u016a\u016b\u0001\u0000\u0000\u0000\u016b\u016c\u0003"+
		"\"\u0011\u0000\u016c\u0011\u0001\u0000\u0000\u0000\u016d\u016e\u0005Z"+
		"\u0000\u0000\u016e\u0173\u0003\u0014\n\u0000\u016f\u0170\u0005V\u0000"+
		"\u0000\u0170\u0172\u0003\u0014\n\u0000\u0171\u016f\u0001\u0000\u0000\u0000"+
		"\u0172\u0175\u0001\u0000\u0000\u0000\u0173\u0171\u0001\u0000\u0000\u0000"+
		"\u0173\u0174\u0001\u0000\u0000\u0000\u0174\u0176\u0001\u0000\u0000\u0000"+
		"\u0175\u0173\u0001\u0000\u0000\u0000\u0176\u0177\u0005Y\u0000\u0000\u0177"+
		"\u0013\u0001\u0000\u0000\u0000\u0178\u017a\u0003r9\u0000\u0179\u0178\u0001"+
		"\u0000\u0000\u0000\u017a\u017d\u0001\u0000\u0000\u0000\u017b\u0179\u0001"+
		"\u0000\u0000\u0000\u017b\u017c\u0001\u0000\u0000\u0000\u017c\u017e\u0001"+
		"\u0000\u0000\u0000\u017d\u017b\u0001\u0000\u0000\u0000\u017e\u0187\u0003"+
		"\u00a4R\u0000\u017f\u0183\u0005\u0012\u0000\u0000\u0180\u0182\u0003r9"+
		"\u0000\u0181\u0180\u0001\u0000\u0000\u0000\u0182\u0185\u0001\u0000\u0000"+
		"\u0000\u0183\u0181\u0001\u0000\u0000\u0000\u0183\u0184\u0001\u0000\u0000"+
		"\u0000\u0184\u0186\u0001\u0000\u0000\u0000\u0185\u0183\u0001\u0000\u0000"+
		"\u0000\u0186\u0188\u0003\u0016\u000b\u0000\u0187\u017f\u0001\u0000\u0000"+
		"\u0000\u0187\u0188\u0001\u0000\u0000\u0000\u0188\u0015\u0001\u0000\u0000"+
		"\u0000\u0189\u018e\u0003\u00f6{\u0000\u018a\u018b\u0005k\u0000\u0000\u018b"+
		"\u018d\u0003\u00f6{\u0000\u018c\u018a\u0001\u0000\u0000\u0000\u018d\u0190"+
		"\u0001\u0000\u0000\u0000\u018e\u018c\u0001\u0000\u0000\u0000\u018e\u018f"+
		"\u0001\u0000\u0000\u0000\u018f\u0017\u0001\u0000\u0000\u0000\u0190\u018e"+
		"\u0001\u0000\u0000\u0000\u0191\u0192\u0005\u0010\u0000\u0000\u0192\u0195"+
		"\u0003\u00a4R\u0000\u0193\u0194\u0005\u0019\u0000\u0000\u0194\u0196\u0003"+
		"\u00f4z\u0000\u0195\u0193\u0001\u0000\u0000\u0000\u0195\u0196\u0001\u0000"+
		"\u0000\u0000\u0196\u0197\u0001\u0000\u0000\u0000\u0197\u0199\u0005Q\u0000"+
		"\u0000\u0198\u019a\u0003\u001a\r\u0000\u0199\u0198\u0001\u0000\u0000\u0000"+
		"\u0199\u019a\u0001\u0000\u0000\u0000\u019a\u019c\u0001\u0000\u0000\u0000"+
		"\u019b\u019d\u0005V\u0000\u0000\u019c\u019b\u0001\u0000\u0000\u0000\u019c"+
		"\u019d\u0001\u0000\u0000\u0000\u019d\u019f\u0001\u0000\u0000\u0000\u019e"+
		"\u01a0\u0003\u001e\u000f\u0000\u019f\u019e\u0001\u0000\u0000\u0000\u019f"+
		"\u01a0\u0001\u0000\u0000\u0000\u01a0\u01a1\u0001\u0000\u0000\u0000\u01a1"+
		"\u01a2\u0005R\u0000\u0000\u01a2\u0019\u0001\u0000\u0000\u0000\u01a3\u01a8"+
		"\u0003\u001c\u000e\u0000\u01a4\u01a5\u0005V\u0000\u0000\u01a5\u01a7\u0003"+
		"\u001c\u000e\u0000\u01a6\u01a4\u0001\u0000\u0000\u0000\u01a7\u01aa\u0001"+
		"\u0000\u0000\u0000\u01a8\u01a6\u0001\u0000\u0000\u0000\u01a8\u01a9\u0001"+
		"\u0000\u0000\u0000\u01a9\u001b\u0001\u0000\u0000\u0000\u01aa\u01a8\u0001"+
		"\u0000\u0000\u0000\u01ab\u01ad\u0003r9\u0000\u01ac\u01ab\u0001\u0000\u0000"+
		"\u0000\u01ad\u01b0\u0001\u0000\u0000\u0000\u01ae\u01ac\u0001\u0000\u0000"+
		"\u0000\u01ae\u01af\u0001\u0000\u0000\u0000\u01af\u01b1\u0001\u0000\u0000"+
		"\u0000\u01b0\u01ae\u0001\u0000\u0000\u0000\u01b1\u01b3\u0003\u00a4R\u0000"+
		"\u01b2\u01b4\u0003\u0100\u0080\u0000\u01b3\u01b2\u0001\u0000\u0000\u0000"+
		"\u01b3\u01b4\u0001\u0000\u0000\u0000\u01b4\u01b6\u0001\u0000\u0000\u0000"+
		"\u01b5\u01b7\u0003\"\u0011\u0000\u01b6\u01b5\u0001\u0000\u0000\u0000\u01b6"+
		"\u01b7\u0001\u0000\u0000\u0000\u01b7\u001d\u0001\u0000\u0000\u0000\u01b8"+
		"\u01bc\u0005U\u0000\u0000\u01b9\u01bb\u0003&\u0013\u0000\u01ba\u01b9\u0001"+
		"\u0000\u0000\u0000\u01bb\u01be\u0001\u0000\u0000\u0000\u01bc\u01ba\u0001"+
		"\u0000\u0000\u0000\u01bc\u01bd\u0001\u0000\u0000\u0000\u01bd\u001f\u0001"+
		"\u0000\u0000\u0000\u01be\u01bc\u0001\u0000\u0000\u0000\u01bf\u01c0\u0005"+
		"\u001d\u0000\u0000\u01c0\u01c2\u0003\u00a4R\u0000\u01c1\u01c3\u0003\u0012"+
		"\t\u0000\u01c2\u01c1\u0001\u0000\u0000\u0000\u01c2\u01c3\u0001\u0000\u0000"+
		"\u0000\u01c3\u01c6\u0001\u0000\u0000\u0000\u01c4\u01c5\u0005\u0012\u0000"+
		"\u0000\u01c5\u01c7\u0003\u00f4z\u0000\u01c6\u01c4\u0001\u0000\u0000\u0000"+
		"\u01c6\u01c7\u0001\u0000\u0000\u0000\u01c7\u01ca\u0001\u0000\u0000\u0000"+
		"\u01c8\u01c9\u0005&\u0000\u0000\u01c9\u01cb\u0003\u00f4z\u0000\u01ca\u01c8"+
		"\u0001\u0000\u0000\u0000\u01ca\u01cb\u0001\u0000\u0000\u0000\u01cb\u01cc"+
		"\u0001\u0000\u0000\u0000\u01cc\u01cd\u0003$\u0012\u0000\u01cd!\u0001\u0000"+
		"\u0000\u0000\u01ce\u01d2\u0005Q\u0000\u0000\u01cf\u01d1\u0003&\u0013\u0000"+
		"\u01d0\u01cf\u0001\u0000\u0000\u0000\u01d1\u01d4\u0001\u0000\u0000\u0000"+
		"\u01d2\u01d0\u0001\u0000\u0000\u0000\u01d2\u01d3\u0001\u0000\u0000\u0000"+
		"\u01d3\u01d5\u0001\u0000\u0000\u0000\u01d4\u01d2\u0001\u0000\u0000\u0000"+
		"\u01d5\u01d6\u0005R\u0000\u0000\u01d6#\u0001\u0000\u0000\u0000\u01d7\u01db"+
		"\u0005Q\u0000\u0000\u01d8\u01da\u0003:\u001d\u0000\u01d9\u01d8\u0001\u0000"+
		"\u0000\u0000\u01da\u01dd\u0001\u0000\u0000\u0000\u01db\u01d9\u0001\u0000"+
		"\u0000\u0000\u01db\u01dc\u0001\u0000\u0000\u0000\u01dc\u01de\u0001\u0000"+
		"\u0000\u0000\u01dd\u01db\u0001\u0000\u0000\u0000\u01de\u01df\u0005R\u0000"+
		"\u0000\u01df%\u0001\u0000\u0000\u0000\u01e0\u01ed\u0005U\u0000\u0000\u01e1"+
		"\u01e3\u00050\u0000\u0000\u01e2\u01e1\u0001\u0000\u0000\u0000\u01e2\u01e3"+
		"\u0001\u0000\u0000\u0000\u01e3\u01e4\u0001\u0000\u0000\u0000\u01e4\u01ed"+
		"\u0003\u009eO\u0000\u01e5\u01e7\u0003\n\u0005\u0000\u01e6\u01e5\u0001"+
		"\u0000\u0000\u0000\u01e7\u01ea\u0001\u0000\u0000\u0000\u01e8\u01e6\u0001"+
		"\u0000\u0000\u0000\u01e8\u01e9\u0001\u0000\u0000\u0000\u01e9\u01eb\u0001"+
		"\u0000\u0000\u0000\u01ea\u01e8\u0001\u0000\u0000\u0000\u01eb\u01ed\u0003"+
		"(\u0014\u0000\u01ec\u01e0\u0001\u0000\u0000\u0000\u01ec\u01e2\u0001\u0000"+
		"\u0000\u0000\u01ec\u01e8\u0001\u0000\u0000\u0000\u01ed\'\u0001\u0000\u0000"+
		"\u0000\u01ee\u01f9\u0003\u0094J\u0000\u01ef\u01f9\u0003*\u0015\u0000\u01f0"+
		"\u01f9\u00030\u0018\u0000\u01f1\u01f9\u00038\u001c\u0000\u01f2\u01f9\u0003"+
		"4\u001a\u0000\u01f3\u01f9\u00032\u0019\u0000\u01f4\u01f9\u0003 \u0010"+
		"\u0000\u01f5\u01f9\u0003~?\u0000\u01f6\u01f9\u0003\u0010\b\u0000\u01f7"+
		"\u01f9\u0003\u0018\f\u0000\u01f8\u01ee\u0001\u0000\u0000\u0000\u01f8\u01ef"+
		"\u0001\u0000\u0000\u0000\u01f8\u01f0\u0001\u0000\u0000\u0000\u01f8\u01f1"+
		"\u0001\u0000\u0000\u0000\u01f8\u01f2\u0001\u0000\u0000\u0000\u01f8\u01f3"+
		"\u0001\u0000\u0000\u0000\u01f8\u01f4\u0001\u0000\u0000\u0000\u01f8\u01f5"+
		"\u0001\u0000\u0000\u0000\u01f8\u01f6\u0001\u0000\u0000\u0000\u01f8\u01f7"+
		"\u0001\u0000\u0000\u0000\u01f9)\u0001\u0000\u0000\u0000\u01fa\u01fb\u0003"+
		".\u0017\u0000\u01fb\u01fc\u0003\u00a4R\u0000\u01fc\u0201\u0003\\.\u0000"+
		"\u01fd\u01fe\u0005S\u0000\u0000\u01fe\u0200\u0005T\u0000\u0000\u01ff\u01fd"+
		"\u0001\u0000\u0000\u0000\u0200\u0203\u0001\u0000\u0000\u0000\u0201\u01ff"+
		"\u0001\u0000\u0000\u0000\u0201\u0202\u0001\u0000\u0000\u0000\u0202\u0206"+
		"\u0001\u0000\u0000\u0000\u0203\u0201\u0001\u0000\u0000\u0000\u0204\u0205"+
		"\u00057\u0000\u0000\u0205\u0207\u0003Z-\u0000\u0206\u0204\u0001\u0000"+
		"\u0000\u0000\u0206\u0207\u0001\u0000\u0000\u0000\u0207\u0208\u0001\u0000"+
		"\u0000\u0000\u0208\u0209\u0003,\u0016\u0000\u0209+\u0001\u0000\u0000\u0000"+
		"\u020a\u020d\u0003\u009eO\u0000\u020b\u020d\u0005U\u0000\u0000\u020c\u020a"+
		"\u0001\u0000\u0000\u0000\u020c\u020b\u0001\u0000\u0000\u0000\u020d-\u0001"+
		"\u0000\u0000\u0000\u020e\u0211\u0003\u00f6{\u0000\u020f\u0211\u0005>\u0000"+
		"\u0000\u0210\u020e\u0001\u0000\u0000\u0000\u0210\u020f\u0001\u0000\u0000"+
		"\u0000\u0211/\u0001\u0000\u0000\u0000\u0212\u0213\u0003\u0012\t\u0000"+
		"\u0213\u0214\u0003*\u0015\u0000\u02141\u0001\u0000\u0000\u0000\u0215\u0216"+
		"\u0003\u0012\t\u0000\u0216\u0217\u00034\u001a\u0000\u02173\u0001\u0000"+
		"\u0000\u0000\u0218\u0219\u0003\u00a4R\u0000\u0219\u021c\u0003\\.\u0000"+
		"\u021a\u021b\u00057\u0000\u0000\u021b\u021d\u0003Z-\u0000\u021c\u021a"+
		"\u0001\u0000\u0000\u0000\u021c\u021d\u0001\u0000\u0000\u0000\u021d\u021e"+
		"\u0001\u0000\u0000\u0000\u021e\u021f\u0003\u009eO\u0000\u021f5\u0001\u0000"+
		"\u0000\u0000\u0220\u0222\u0003\n\u0005\u0000\u0221\u0220\u0001\u0000\u0000"+
		"\u0000\u0222\u0225\u0001\u0000\u0000\u0000\u0223\u0221\u0001\u0000\u0000"+
		"\u0000\u0223\u0224\u0001\u0000\u0000\u0000\u0224\u0226\u0001\u0000\u0000"+
		"\u0000\u0225\u0223\u0001\u0000\u0000\u0000\u0226\u0227\u0003\u00a4R\u0000"+
		"\u0227\u0228\u0003\u009eO\u0000\u02287\u0001\u0000\u0000\u0000\u0229\u022a"+
		"\u0003\u00f6{\u0000\u022a\u022b\u0003J%\u0000\u022b\u022c\u0005U\u0000"+
		"\u0000\u022c9\u0001\u0000\u0000\u0000\u022d\u022f\u0003\n\u0005\u0000"+
		"\u022e\u022d\u0001\u0000\u0000\u0000\u022f\u0232\u0001\u0000\u0000\u0000"+
		"\u0230\u022e\u0001\u0000\u0000\u0000\u0230\u0231\u0001\u0000\u0000\u0000"+
		"\u0231\u0233\u0001\u0000\u0000\u0000\u0232\u0230\u0001\u0000\u0000\u0000"+
		"\u0233\u0236\u0003<\u001e\u0000\u0234\u0236\u0005U\u0000\u0000\u0235\u0230"+
		"\u0001\u0000\u0000\u0000\u0235\u0234\u0001\u0000\u0000\u0000\u0236;\u0001"+
		"\u0000\u0000\u0000\u0237\u0240\u0003\u0094J\u0000\u0238\u0240\u0003>\u001f"+
		"\u0000\u0239\u0240\u0003B!\u0000\u023a\u0240\u0003F#\u0000\u023b\u0240"+
		"\u0003 \u0010\u0000\u023c\u0240\u0003~?\u0000\u023d\u0240\u0003\u0010"+
		"\b\u0000\u023e\u0240\u0003\u0018\f\u0000\u023f\u0237\u0001\u0000\u0000"+
		"\u0000\u023f\u0238\u0001\u0000\u0000\u0000\u023f\u0239\u0001\u0000\u0000"+
		"\u0000\u023f\u023a\u0001\u0000\u0000\u0000\u023f\u023b\u0001\u0000\u0000"+
		"\u0000\u023f\u023c\u0001\u0000\u0000\u0000\u023f\u023d\u0001\u0000\u0000"+
		"\u0000\u023f\u023e\u0001\u0000\u0000\u0000\u0240=\u0001\u0000\u0000\u0000"+
		"\u0241\u0242\u0003\u00f6{\u0000\u0242\u0247\u0003@ \u0000\u0243\u0244"+
		"\u0005V\u0000\u0000\u0244\u0246\u0003@ \u0000\u0245\u0243\u0001\u0000"+
		"\u0000\u0000\u0246\u0249\u0001\u0000\u0000\u0000\u0247\u0245\u0001\u0000"+
		"\u0000\u0000\u0247\u0248\u0001\u0000\u0000\u0000\u0248\u024a\u0001\u0000"+
		"\u0000\u0000\u0249\u0247\u0001\u0000\u0000\u0000\u024a\u024b\u0005U\u0000"+
		"\u0000\u024b?\u0001\u0000\u0000\u0000\u024c\u0251\u0003\u00a4R\u0000\u024d"+
		"\u024e\u0005S\u0000\u0000\u024e\u0250\u0005T\u0000\u0000\u024f\u024d\u0001"+
		"\u0000\u0000\u0000\u0250\u0253\u0001\u0000\u0000\u0000\u0251\u024f\u0001"+
		"\u0000\u0000\u0000\u0251\u0252\u0001\u0000\u0000\u0000\u0252\u0254\u0001"+
		"\u0000\u0000\u0000\u0253\u0251\u0001\u0000\u0000\u0000\u0254\u0255\u0005"+
		"X\u0000\u0000\u0255\u0256\u0003P(\u0000\u0256A\u0001\u0000\u0000\u0000"+
		"\u0257\u0259\u0003D\"\u0000\u0258\u0257\u0001\u0000\u0000\u0000\u0259"+
		"\u025c\u0001\u0000\u0000\u0000\u025a\u0258\u0001\u0000\u0000\u0000\u025a"+
		"\u025b\u0001\u0000\u0000\u0000\u025b\u025d\u0001\u0000\u0000\u0000\u025c"+
		"\u025a\u0001\u0000\u0000\u0000\u025d\u025e\u0003H$\u0000\u025eC\u0001"+
		"\u0000\u0000\u0000\u025f\u0266\u0003r9\u0000\u0260\u0266\u0005*\u0000"+
		"\u0000\u0261\u0266\u0005\u0001\u0000\u0000\u0262\u0266\u0005\f\u0000\u0000"+
		"\u0263\u0266\u00050\u0000\u0000\u0264\u0266\u00051\u0000\u0000\u0265\u025f"+
		"\u0001\u0000\u0000\u0000\u0265\u0260\u0001\u0000\u0000\u0000\u0265\u0261"+
		"\u0001\u0000\u0000\u0000\u0265\u0262\u0001\u0000\u0000\u0000\u0265\u0263"+
		"\u0001\u0000\u0000\u0000\u0265\u0264\u0001\u0000\u0000\u0000\u0266E\u0001"+
		"\u0000\u0000\u0000\u0267\u0269\u0003D\"\u0000\u0268\u0267\u0001\u0000"+
		"\u0000\u0000\u0269\u026c\u0001\u0000\u0000\u0000\u026a\u0268\u0001\u0000"+
		"\u0000\u0000\u026a\u026b\u0001\u0000\u0000\u0000\u026b\u026d\u0001\u0000"+
		"\u0000\u0000\u026c\u026a\u0001\u0000\u0000\u0000\u026d\u026e\u0003\u0012"+
		"\t\u0000\u026e\u026f\u0003H$\u0000\u026fG\u0001\u0000\u0000\u0000\u0270"+
		"\u0272\u0003r9\u0000\u0271\u0270\u0001\u0000\u0000\u0000\u0272\u0275\u0001"+
		"\u0000\u0000\u0000\u0273\u0271\u0001\u0000\u0000\u0000\u0273\u0274\u0001"+
		"\u0000\u0000\u0000\u0274\u0276\u0001\u0000\u0000\u0000\u0275\u0273\u0001"+
		"\u0000\u0000\u0000\u0276\u0277\u0003.\u0017\u0000\u0277\u0278\u0003\u00a4"+
		"R\u0000\u0278\u027d\u0003\\.\u0000\u0279\u027a\u0005S\u0000\u0000\u027a"+
		"\u027c\u0005T\u0000\u0000\u027b\u0279\u0001\u0000\u0000\u0000\u027c\u027f"+
		"\u0001\u0000\u0000\u0000\u027d\u027b\u0001\u0000\u0000\u0000\u027d\u027e"+
		"\u0001\u0000\u0000\u0000\u027e\u0282\u0001\u0000\u0000\u0000\u027f\u027d"+
		"\u0001\u0000\u0000\u0000\u0280\u0281\u00057\u0000\u0000\u0281\u0283\u0003"+
		"Z-\u0000\u0282\u0280\u0001\u0000\u0000\u0000\u0282\u0283\u0001\u0000\u0000"+
		"\u0000\u0283\u0284\u0001\u0000\u0000\u0000\u0284\u0285\u0003,\u0016\u0000"+
		"\u0285I\u0001\u0000\u0000\u0000\u0286\u028b\u0003L&\u0000\u0287\u0288"+
		"\u0005V\u0000\u0000\u0288\u028a\u0003L&\u0000\u0289\u0287\u0001\u0000"+
		"\u0000\u0000\u028a\u028d\u0001\u0000\u0000\u0000\u028b\u0289\u0001\u0000"+
		"\u0000\u0000\u028b\u028c\u0001\u0000\u0000\u0000\u028cK\u0001\u0000\u0000"+
		"\u0000\u028d\u028b\u0001\u0000\u0000\u0000\u028e\u0291\u0003N\'\u0000"+
		"\u028f\u0290\u0005X\u0000\u0000\u0290\u0292\u0003P(\u0000\u0291\u028f"+
		"\u0001\u0000\u0000\u0000\u0291\u0292\u0001\u0000\u0000\u0000\u0292M\u0001"+
		"\u0000\u0000\u0000\u0293\u0298\u0003\u00a4R\u0000\u0294\u0295\u0005S\u0000"+
		"\u0000\u0295\u0297\u0005T\u0000\u0000\u0296\u0294\u0001\u0000\u0000\u0000"+
		"\u0297\u029a\u0001\u0000\u0000\u0000\u0298\u0296\u0001\u0000\u0000\u0000"+
		"\u0298\u0299\u0001\u0000\u0000\u0000\u0299O\u0001\u0000\u0000\u0000\u029a"+
		"\u0298\u0001\u0000\u0000\u0000\u029b\u029e\u0003R)\u0000\u029c\u029e\u0003"+
		"\u00c6c\u0000\u029d\u029b\u0001\u0000\u0000\u0000\u029d\u029c\u0001\u0000"+
		"\u0000\u0000\u029eQ\u0001\u0000\u0000\u0000\u029f\u02ab\u0005Q\u0000\u0000"+
		"\u02a0\u02a5\u0003P(\u0000\u02a1\u02a2\u0005V\u0000\u0000\u02a2\u02a4"+
		"\u0003P(\u0000\u02a3\u02a1\u0001\u0000\u0000\u0000\u02a4\u02a7\u0001\u0000"+
		"\u0000\u0000\u02a5\u02a3\u0001\u0000\u0000\u0000\u02a5\u02a6\u0001\u0000"+
		"\u0000\u0000\u02a6\u02a9\u0001\u0000\u0000\u0000\u02a7\u02a5\u0001\u0000"+
		"\u0000\u0000\u02a8\u02aa\u0005V\u0000\u0000\u02a9\u02a8\u0001\u0000\u0000"+
		"\u0000\u02a9\u02aa\u0001\u0000\u0000\u0000\u02aa\u02ac\u0001\u0000\u0000"+
		"\u0000\u02ab\u02a0\u0001\u0000\u0000\u0000\u02ab\u02ac\u0001\u0000\u0000"+
		"\u0000\u02ac\u02ad\u0001\u0000\u0000\u0000\u02ad\u02ae\u0005R\u0000\u0000"+
		"\u02aeS\u0001\u0000\u0000\u0000\u02af\u02b0\u0003V+\u0000\u02b0\u02b4"+
		"\u0005W\u0000\u0000\u02b1\u02b3\u0003r9\u0000\u02b2\u02b1\u0001\u0000"+
		"\u0000\u0000\u02b3\u02b6\u0001\u0000\u0000\u0000\u02b4\u02b2\u0001\u0000"+
		"\u0000\u0000\u02b4\u02b5\u0001\u0000\u0000\u0000\u02b5\u02b8\u0001\u0000"+
		"\u0000\u0000\u02b6\u02b4\u0001\u0000\u0000\u0000\u02b7\u02af\u0001\u0000"+
		"\u0000\u0000\u02b7\u02b8\u0001\u0000\u0000\u0000\u02b8\u02b9\u0001\u0000"+
		"\u0000\u0000\u02b9\u02bb\u0003\u00a6S\u0000\u02ba\u02bc\u0003\u00fa}\u0000"+
		"\u02bb\u02ba\u0001\u0000\u0000\u0000\u02bb\u02bc\u0001\u0000\u0000\u0000"+
		"\u02bc\u02be\u0001\u0000\u0000\u0000\u02bd\u02b7\u0001\u0000\u0000\u0000"+
		"\u02be\u02bf\u0001\u0000\u0000\u0000\u02bf\u02bd\u0001\u0000\u0000\u0000"+
		"\u02bf\u02c0\u0001\u0000\u0000\u0000\u02c0\u02ce\u0001\u0000\u0000\u0000"+
		"\u02c1\u02c5\u0005W\u0000\u0000\u02c2\u02c4\u0003r9\u0000\u02c3\u02c2"+
		"\u0001\u0000\u0000\u0000\u02c4\u02c7\u0001\u0000\u0000\u0000\u02c5\u02c3"+
		"\u0001\u0000\u0000\u0000\u02c5\u02c6\u0001\u0000\u0000\u0000\u02c6\u02c8"+
		"\u0001\u0000\u0000\u0000\u02c7\u02c5\u0001\u0000\u0000\u0000\u02c8\u02ca"+
		"\u0003\u00a6S\u0000\u02c9\u02cb\u0003\u00fa}\u0000\u02ca\u02c9\u0001\u0000"+
		"\u0000\u0000\u02ca\u02cb\u0001\u0000\u0000\u0000\u02cb\u02cd\u0001\u0000"+
		"\u0000\u0000\u02cc\u02c1\u0001\u0000\u0000\u0000\u02cd\u02d0\u0001\u0000"+
		"\u0000\u0000\u02ce\u02cc\u0001\u0000\u0000\u0000\u02ce\u02cf\u0001\u0000"+
		"\u0000\u0000\u02cfU\u0001\u0000\u0000\u0000\u02d0\u02ce\u0001\u0000\u0000"+
		"\u0000\u02d1\u02d6\u0003\u00a4R\u0000\u02d2\u02d3\u0005W\u0000\u0000\u02d3"+
		"\u02d5\u0003\u00a4R\u0000\u02d4\u02d2\u0001\u0000\u0000\u0000\u02d5\u02d8"+
		"\u0001\u0000\u0000\u0000\u02d6\u02d4\u0001\u0000\u0000\u0000\u02d6\u02d7"+
		"\u0001\u0000\u0000\u0000\u02d7W\u0001\u0000\u0000\u0000\u02d8\u02d6\u0001"+
		"\u0000\u0000\u0000\u02d9\u02e6\u0003\u00f6{\u0000\u02da\u02dc\u0003r9"+
		"\u0000\u02db\u02da\u0001\u0000\u0000\u0000\u02dc\u02df\u0001\u0000\u0000"+
		"\u0000\u02dd\u02db\u0001\u0000\u0000\u0000\u02dd\u02de\u0001\u0000\u0000"+
		"\u0000\u02de\u02e0\u0001\u0000\u0000\u0000\u02df\u02dd\u0001\u0000\u0000"+
		"\u0000\u02e0\u02e3\u0005]\u0000\u0000\u02e1\u02e2\u0007\u0000\u0000\u0000"+
		"\u02e2\u02e4\u0003\u00f6{\u0000\u02e3\u02e1\u0001\u0000\u0000\u0000\u02e3"+
		"\u02e4\u0001\u0000\u0000\u0000\u02e4\u02e6\u0001\u0000\u0000\u0000\u02e5"+
		"\u02d9\u0001\u0000\u0000\u0000\u02e5\u02dd\u0001\u0000\u0000\u0000\u02e6"+
		"Y\u0001\u0000\u0000\u0000\u02e7\u02ec\u0003h4\u0000\u02e8\u02e9\u0005"+
		"V\u0000\u0000\u02e9\u02eb\u0003h4\u0000\u02ea\u02e8\u0001\u0000\u0000"+
		"\u0000\u02eb\u02ee\u0001\u0000\u0000\u0000\u02ec\u02ea\u0001\u0000\u0000"+
		"\u0000\u02ec\u02ed\u0001\u0000\u0000\u0000\u02ed[\u0001\u0000\u0000\u0000"+
		"\u02ee\u02ec\u0001\u0000\u0000\u0000\u02ef\u02fb\u0005O\u0000\u0000\u02f0"+
		"\u02f3\u0003^/\u0000\u02f1\u02f3\u0003b1\u0000\u02f2\u02f0\u0001\u0000"+
		"\u0000\u0000\u02f2\u02f1\u0001\u0000\u0000\u0000\u02f3\u02f8\u0001\u0000"+
		"\u0000\u0000\u02f4\u02f5\u0005V\u0000\u0000\u02f5\u02f7\u0003`0\u0000"+
		"\u02f6\u02f4\u0001\u0000\u0000\u0000\u02f7\u02fa\u0001\u0000\u0000\u0000"+
		"\u02f8\u02f6\u0001\u0000\u0000\u0000\u02f8\u02f9\u0001\u0000\u0000\u0000"+
		"\u02f9\u02fc\u0001\u0000\u0000\u0000\u02fa\u02f8\u0001\u0000\u0000\u0000"+
		"\u02fb\u02f2\u0001\u0000\u0000\u0000\u02fb\u02fc\u0001\u0000\u0000\u0000"+
		"\u02fc\u02fd\u0001\u0000\u0000\u0000\u02fd\u02fe\u0005P\u0000\u0000\u02fe"+
		"]\u0001\u0000\u0000\u0000\u02ff\u0305\u0003\u00f6{\u0000\u0300\u0301\u0003"+
		"\u00a4R\u0000\u0301\u0302\u0005W\u0000\u0000\u0302\u0304\u0001\u0000\u0000"+
		"\u0000\u0303\u0300\u0001\u0000\u0000\u0000\u0304\u0307\u0001\u0000\u0000"+
		"\u0000\u0305\u0303\u0001\u0000\u0000\u0000\u0305\u0306\u0001\u0000\u0000"+
		"\u0000\u0306\u0308\u0001\u0000\u0000\u0000\u0307\u0305\u0001\u0000\u0000"+
		"\u0000\u0308\u0309\u00055\u0000\u0000\u0309_\u0001\u0000\u0000\u0000\u030a"+
		"\u030f\u0003b1\u0000\u030b\u030c\u0005V\u0000\u0000\u030c\u030e\u0003"+
		"b1\u0000\u030d\u030b\u0001\u0000\u0000\u0000\u030e\u0311\u0001\u0000\u0000"+
		"\u0000\u030f\u030d\u0001\u0000\u0000\u0000\u030f\u0310\u0001\u0000\u0000"+
		"\u0000\u0310a\u0001\u0000\u0000\u0000\u0311\u030f\u0001\u0000\u0000\u0000"+
		"\u0312\u0314\u0003\u000e\u0007\u0000\u0313\u0312\u0001\u0000\u0000\u0000"+
		"\u0314\u0317\u0001\u0000\u0000\u0000\u0315\u0313\u0001\u0000\u0000\u0000"+
		"\u0315\u0316\u0001\u0000\u0000\u0000\u0316\u0318\u0001\u0000\u0000\u0000"+
		"\u0317\u0315\u0001\u0000\u0000\u0000\u0318\u0320\u0003\u00f6{\u0000\u0319"+
		"\u031b\u0003r9\u0000\u031a\u0319\u0001\u0000\u0000\u0000\u031b\u031e\u0001"+
		"\u0000\u0000\u0000\u031c\u031a\u0001\u0000\u0000\u0000\u031c\u031d\u0001"+
		"\u0000\u0000\u0000\u031d\u031f\u0001\u0000\u0000\u0000\u031e\u031c\u0001"+
		"\u0000\u0000\u0000\u031f\u0321\u0005}\u0000\u0000\u0320\u031c\u0001\u0000"+
		"\u0000\u0000\u0320\u0321\u0001\u0000\u0000\u0000\u0321\u0322\u0001\u0000"+
		"\u0000\u0000\u0322\u0323\u0003N\'\u0000\u0323c\u0001\u0000\u0000\u0000"+
		"\u0324\u0329\u0003f3\u0000\u0325\u0326\u0005V\u0000\u0000\u0326\u0328"+
		"\u0003f3\u0000\u0327\u0325\u0001\u0000\u0000\u0000\u0328\u032b\u0001\u0000"+
		"\u0000\u0000\u0329\u0327\u0001\u0000\u0000\u0000\u0329\u032a\u0001\u0000"+
		"\u0000\u0000\u032ae\u0001\u0000\u0000\u0000\u032b\u0329\u0001\u0000\u0000"+
		"\u0000\u032c\u032e\u0003\u000e\u0007\u0000\u032d\u032c\u0001\u0000\u0000"+
		"\u0000\u032e\u0331\u0001\u0000\u0000\u0000\u032f\u032d\u0001\u0000\u0000"+
		"\u0000\u032f\u0330\u0001\u0000\u0000\u0000\u0330\u0332\u0001\u0000\u0000"+
		"\u0000\u0331\u032f\u0001\u0000\u0000\u0000\u0332\u0333\u0005=\u0000\u0000"+
		"\u0333\u0334\u0003\u00a4R\u0000\u0334g\u0001\u0000\u0000\u0000\u0335\u033a"+
		"\u0003\u00a4R\u0000\u0336\u0337\u0005W\u0000\u0000\u0337\u0339\u0003\u00a4"+
		"R\u0000\u0338\u0336\u0001\u0000\u0000\u0000\u0339\u033c\u0001\u0000\u0000"+
		"\u0000\u033a\u0338\u0001\u0000\u0000\u0000\u033a\u033b\u0001\u0000\u0000"+
		"\u0000\u033bi\u0001\u0000\u0000\u0000\u033c\u033a\u0001\u0000\u0000\u0000"+
		"\u033d\u0345\u0003l6\u0000\u033e\u0345\u0003n7\u0000\u033f\u0345\u0005"+
		"K\u0000\u0000\u0340\u0345\u0005L\u0000\u0000\u0341\u0345\u0005J\u0000"+
		"\u0000\u0342\u0345\u0005N\u0000\u0000\u0343\u0345\u0005M\u0000\u0000\u0344"+
		"\u033d\u0001\u0000\u0000\u0000\u0344\u033e\u0001\u0000\u0000\u0000\u0344"+
		"\u033f\u0001\u0000\u0000\u0000\u0344\u0340\u0001\u0000\u0000\u0000\u0344"+
		"\u0341\u0001\u0000\u0000\u0000\u0344\u0342\u0001\u0000\u0000\u0000\u0344"+
		"\u0343\u0001\u0000\u0000\u0000\u0345k\u0001\u0000\u0000\u0000\u0346\u0347"+
		"\u0007\u0001\u0000\u0000\u0347m\u0001\u0000\u0000\u0000\u0348\u0349\u0007"+
		"\u0002\u0000\u0000\u0349o\u0001\u0000\u0000\u0000\u034a\u034b\u0003\u00a4"+
		"R\u0000\u034b\u034c\u0005W\u0000\u0000\u034c\u034e\u0001\u0000\u0000\u0000"+
		"\u034d\u034a\u0001\u0000\u0000\u0000\u034e\u0351\u0001\u0000\u0000\u0000"+
		"\u034f\u034d\u0001\u0000\u0000\u0000\u034f\u0350\u0001\u0000\u0000\u0000"+
		"\u0350\u0352\u0001\u0000\u0000\u0000\u0351\u034f\u0001\u0000\u0000\u0000"+
		"\u0352\u0353\u0005|\u0000\u0000\u0353\u0354\u0003\u00a4R\u0000\u0354q"+
		"\u0001\u0000\u0000\u0000\u0355\u0356\u0005|\u0000\u0000\u0356\u0357\u0003"+
		"h4\u0000\u0357\u0359\u0001\u0000\u0000\u0000\u0358\u035a\u0003t:\u0000"+
		"\u0359\u0358\u0001\u0000\u0000\u0000\u0359\u035a\u0001\u0000\u0000\u0000"+
		"\u035as\u0001\u0000\u0000\u0000\u035b\u0364\u0005O\u0000\u0000\u035c\u0361"+
		"\u0003v;\u0000\u035d\u035e\u0005V\u0000\u0000\u035e\u0360\u0003v;\u0000"+
		"\u035f\u035d\u0001\u0000\u0000\u0000\u0360\u0363\u0001\u0000\u0000\u0000"+
		"\u0361\u035f\u0001\u0000\u0000\u0000\u0361\u0362\u0001\u0000\u0000\u0000"+
		"\u0362\u0365\u0001\u0000\u0000\u0000\u0363\u0361\u0001\u0000\u0000\u0000"+
		"\u0364\u035c\u0001\u0000\u0000\u0000\u0364\u0365\u0001\u0000\u0000\u0000"+
		"\u0365\u0366\u0001\u0000\u0000\u0000\u0366\u0367\u0005P\u0000\u0000\u0367"+
		"u\u0001\u0000\u0000\u0000\u0368\u0369\u0004;\u0000\u0000\u0369\u036f\u0003"+
		"x<\u0000\u036a\u036b\u0003\u00a4R\u0000\u036b\u036c\u0005X\u0000\u0000"+
		"\u036c\u036d\u0003x<\u0000\u036d\u036f\u0001\u0000\u0000\u0000\u036e\u0368"+
		"\u0001\u0000\u0000\u0000\u036e\u036a\u0001\u0000\u0000\u0000\u036fw\u0001"+
		"\u0000\u0000\u0000\u0370\u0382\u0003\u00c6c\u0000\u0371\u0382\u0003r9"+
		"\u0000\u0372\u037b\u0005Q\u0000\u0000\u0373\u0378\u0003x<\u0000\u0374"+
		"\u0375\u0005V\u0000\u0000\u0375\u0377\u0003x<\u0000\u0376\u0374\u0001"+
		"\u0000\u0000\u0000\u0377\u037a\u0001\u0000\u0000\u0000\u0378\u0376\u0001"+
		"\u0000\u0000\u0000\u0378\u0379\u0001\u0000\u0000\u0000\u0379\u037c\u0001"+
		"\u0000\u0000\u0000\u037a\u0378\u0001\u0000\u0000\u0000\u037b\u0373\u0001"+
		"\u0000\u0000\u0000\u037b\u037c\u0001\u0000\u0000\u0000\u037c\u037e\u0001"+
		"\u0000\u0000\u0000\u037d\u037f\u0005V\u0000\u0000\u037e\u037d\u0001\u0000"+
		"\u0000\u0000\u037e\u037f\u0001\u0000\u0000\u0000\u037f\u0380\u0001\u0000"+
		"\u0000\u0000\u0380\u0382\u0005R\u0000\u0000\u0381\u0370\u0001\u0000\u0000"+
		"\u0000\u0381\u0371\u0001\u0000\u0000\u0000\u0381\u0372\u0001\u0000\u0000"+
		"\u0000\u0382y\u0001\u0000\u0000\u0000\u0383\u0387\u0003\u00c6c\u0000\u0384"+
		"\u0387\u0003r9\u0000\u0385\u0387\u0003|>\u0000\u0386\u0383\u0001\u0000"+
		"\u0000\u0000\u0386\u0384\u0001\u0000\u0000\u0000\u0386\u0385\u0001\u0000"+
		"\u0000\u0000\u0387{\u0001\u0000\u0000\u0000\u0388\u0391\u0005Q\u0000\u0000"+
		"\u0389\u038e\u0003z=\u0000\u038a\u038b\u0005V\u0000\u0000\u038b\u038d"+
		"\u0003z=\u0000\u038c\u038a\u0001\u0000\u0000\u0000\u038d\u0390\u0001\u0000"+
		"\u0000\u0000\u038e\u038c\u0001\u0000\u0000\u0000\u038e\u038f\u0001\u0000"+
		"\u0000\u0000\u038f\u0392\u0001\u0000\u0000\u0000\u0390\u038e\u0001\u0000"+
		"\u0000\u0000\u0391\u0389\u0001\u0000\u0000\u0000\u0391\u0392\u0001\u0000"+
		"\u0000\u0000\u0392\u0394\u0001\u0000\u0000\u0000\u0393\u0395\u0005V\u0000"+
		"\u0000\u0394\u0393\u0001\u0000\u0000\u0000\u0394\u0395\u0001\u0000\u0000"+
		"\u0000\u0395\u0396\u0001\u0000\u0000\u0000\u0396\u0397\u0005R\u0000\u0000"+
		"\u0397}\u0001\u0000\u0000\u0000\u0398\u0399\u0005|\u0000\u0000\u0399\u039a"+
		"\u0005\u001d\u0000\u0000\u039a\u039b\u0003\u00a4R\u0000\u039b\u039c\u0003"+
		"\u0080@\u0000\u039c\u007f\u0001\u0000\u0000\u0000\u039d\u03a1\u0005Q\u0000"+
		"\u0000\u039e\u03a0\u0003\u0082A\u0000\u039f\u039e\u0001\u0000\u0000\u0000"+
		"\u03a0\u03a3\u0001\u0000\u0000\u0000\u03a1\u039f\u0001\u0000\u0000\u0000"+
		"\u03a1\u03a2\u0001\u0000\u0000\u0000\u03a2\u03a4\u0001\u0000\u0000\u0000"+
		"\u03a3\u03a1\u0001\u0000\u0000\u0000\u03a4\u03a5\u0005R\u0000\u0000\u03a5"+
		"\u0081\u0001\u0000\u0000\u0000\u03a6\u03a8\u0003\n\u0005\u0000\u03a7\u03a6"+
		"\u0001\u0000\u0000\u0000\u03a8\u03ab\u0001\u0000\u0000\u0000\u03a9\u03a7"+
		"\u0001\u0000\u0000\u0000\u03a9\u03aa\u0001\u0000\u0000\u0000\u03aa\u03ac"+
		"\u0001\u0000\u0000\u0000\u03ab\u03a9\u0001\u0000\u0000\u0000\u03ac\u03af"+
		"\u0003\u0084B\u0000\u03ad\u03af\u0005U\u0000\u0000\u03ae\u03a9\u0001\u0000"+
		"\u0000\u0000\u03ae\u03ad\u0001\u0000\u0000\u0000\u03af\u0083\u0001\u0000"+
		"\u0000\u0000\u03b0\u03b1\u0003\u00f6{\u0000\u03b1\u03b2\u0003\u0086C\u0000"+
		"\u03b2\u03b3\u0005U\u0000\u0000\u03b3\u03c9\u0001\u0000\u0000\u0000\u03b4"+
		"\u03b6\u0003\u0010\b\u0000\u03b5\u03b7\u0005U\u0000\u0000\u03b6\u03b5"+
		"\u0001\u0000\u0000\u0000\u03b6\u03b7\u0001\u0000\u0000\u0000\u03b7\u03c9"+
		"\u0001\u0000\u0000\u0000\u03b8\u03ba\u0003 \u0010\u0000\u03b9\u03bb\u0005"+
		"U\u0000\u0000\u03ba\u03b9\u0001\u0000\u0000\u0000\u03ba\u03bb\u0001\u0000"+
		"\u0000\u0000\u03bb\u03c9\u0001\u0000\u0000\u0000\u03bc\u03be\u0003\u0018"+
		"\f\u0000\u03bd\u03bf\u0005U\u0000\u0000\u03be\u03bd\u0001\u0000\u0000"+
		"\u0000\u03be\u03bf\u0001\u0000\u0000\u0000\u03bf\u03c9\u0001\u0000\u0000"+
		"\u0000\u03c0\u03c2\u0003~?\u0000\u03c1\u03c3\u0005U\u0000\u0000\u03c2"+
		"\u03c1\u0001\u0000\u0000\u0000\u03c2\u03c3\u0001\u0000\u0000\u0000\u03c3"+
		"\u03c9\u0001\u0000\u0000\u0000\u03c4\u03c6\u0003\u0094J\u0000\u03c5\u03c7"+
		"\u0005U\u0000\u0000\u03c6\u03c5\u0001\u0000\u0000\u0000\u03c6\u03c7\u0001"+
		"\u0000\u0000\u0000\u03c7\u03c9\u0001\u0000\u0000\u0000\u03c8\u03b0\u0001"+
		"\u0000\u0000\u0000\u03c8\u03b4\u0001\u0000\u0000\u0000\u03c8\u03b8\u0001"+
		"\u0000\u0000\u0000\u03c8\u03bc\u0001\u0000\u0000\u0000\u03c8\u03c0\u0001"+
		"\u0000\u0000\u0000\u03c8\u03c4\u0001\u0000\u0000\u0000\u03c9\u0085\u0001"+
		"\u0000\u0000\u0000\u03ca\u03cd\u0003\u0088D\u0000\u03cb\u03cd\u0003\u008a"+
		"E\u0000\u03cc\u03ca\u0001\u0000\u0000\u0000\u03cc\u03cb\u0001\u0000\u0000"+
		"\u0000\u03cd\u0087\u0001\u0000\u0000\u0000\u03ce\u03cf\u0003\u00a4R\u0000"+
		"\u03cf\u03d0\u0005O\u0000\u0000\u03d0\u03d2\u0005P\u0000\u0000\u03d1\u03d3"+
		"\u0003\u008cF\u0000\u03d2\u03d1\u0001\u0000\u0000\u0000\u03d2\u03d3\u0001"+
		"\u0000\u0000\u0000\u03d3\u0089\u0001\u0000\u0000\u0000\u03d4\u03d5\u0003"+
		"J%\u0000\u03d5\u008b\u0001\u0000\u0000\u0000\u03d6\u03d7\u0005\f\u0000"+
		"\u0000\u03d7\u03d8\u0003z=\u0000\u03d8\u008d\u0001\u0000\u0000\u0000\u03d9"+
		"\u03db\u0003r9\u0000\u03da\u03d9\u0001\u0000\u0000\u0000\u03db\u03de\u0001"+
		"\u0000\u0000\u0000\u03dc\u03da\u0001\u0000\u0000\u0000\u03dc\u03dd\u0001"+
		"\u0000\u0000\u0000\u03dd\u03e0\u0001\u0000\u0000\u0000\u03de\u03dc\u0001"+
		"\u0000\u0000\u0000\u03df\u03e1\u0005#\u0000\u0000\u03e0\u03df\u0001\u0000"+
		"\u0000\u0000\u03e0\u03e1\u0001\u0000\u0000\u0000\u03e1\u03e2\u0001\u0000"+
		"\u0000\u0000\u03e2\u03e3\u0005\u001f\u0000\u0000\u03e3\u03e4\u0003h4\u0000"+
		"\u03e4\u03e8\u0005Q\u0000\u0000\u03e5\u03e7\u0003\u0090H\u0000\u03e6\u03e5"+
		"\u0001\u0000\u0000\u0000\u03e7\u03ea\u0001\u0000\u0000\u0000\u03e8\u03e6"+
		"\u0001\u0000\u0000\u0000\u03e8\u03e9\u0001\u0000\u0000\u0000\u03e9\u03eb"+
		"\u0001\u0000\u0000\u0000\u03ea\u03e8\u0001\u0000\u0000\u0000\u03eb\u03ec"+
		"\u0005R\u0000\u0000\u03ec\u008f\u0001\u0000\u0000\u0000\u03ed\u03f1\u0005"+
		",\u0000\u0000\u03ee\u03f0\u0003\u0092I\u0000\u03ef\u03ee\u0001\u0000\u0000"+
		"\u0000\u03f0\u03f3\u0001\u0000\u0000\u0000\u03f1\u03ef\u0001\u0000\u0000"+
		"\u0000\u03f1\u03f2\u0001\u0000\u0000\u0000\u03f2\u03f4\u0001\u0000\u0000"+
		"\u0000\u03f3\u03f1\u0001\u0000\u0000\u0000\u03f4\u03f5\u0003h4\u0000\u03f5"+
		"\u03f6\u0005U\u0000\u0000\u03f6\u0427\u0001\u0000\u0000\u0000\u03f7\u03f8"+
		"\u0005\u0011\u0000\u0000\u03f8\u0402\u0003h4\u0000\u03f9\u03fa\u00058"+
		"\u0000\u0000\u03fa\u03ff\u0003h4\u0000\u03fb\u03fc\u0005V\u0000\u0000"+
		"\u03fc\u03fe\u0003h4\u0000\u03fd\u03fb\u0001\u0000\u0000\u0000\u03fe\u0401"+
		"\u0001\u0000\u0000\u0000\u03ff\u03fd\u0001\u0000\u0000\u0000\u03ff\u0400"+
		"\u0001\u0000\u0000\u0000\u0400\u0403\u0001\u0000\u0000\u0000\u0401\u03ff"+
		"\u0001\u0000\u0000\u0000\u0402\u03f9\u0001\u0000\u0000\u0000\u0402\u0403"+
		"\u0001\u0000\u0000\u0000\u0403\u0404\u0001\u0000\u0000\u0000\u0404\u0405"+
		"\u0005U\u0000\u0000\u0405\u0427\u0001\u0000\u0000\u0000\u0406\u0407\u0005"+
		"$\u0000\u0000\u0407\u0411\u0003h4\u0000\u0408\u0409\u00058\u0000\u0000"+
		"\u0409\u040e\u0003h4\u0000\u040a\u040b\u0005V\u0000\u0000\u040b\u040d"+
		"\u0003h4\u0000\u040c\u040a\u0001\u0000\u0000\u0000\u040d\u0410\u0001\u0000"+
		"\u0000\u0000\u040e\u040c\u0001\u0000\u0000\u0000\u040e\u040f\u0001\u0000"+
		"\u0000\u0000\u040f\u0412\u0001\u0000\u0000\u0000\u0410\u040e\u0001\u0000"+
		"\u0000\u0000\u0411\u0408\u0001\u0000\u0000\u0000\u0411\u0412\u0001\u0000"+
		"\u0000\u0000\u0412\u0413\u0001\u0000\u0000\u0000\u0413\u0414\u0005U\u0000"+
		"\u0000\u0414\u0427\u0001\u0000\u0000\u0000\u0415\u0416\u0005<\u0000\u0000"+
		"\u0416\u0417\u0003h4\u0000\u0417\u0418\u0005U\u0000\u0000\u0418\u0427"+
		"\u0001\u0000\u0000\u0000\u0419\u041a\u0005)\u0000\u0000\u041a\u041b\u0003"+
		"h4\u0000\u041b\u041c\u0005B\u0000\u0000\u041c\u0421\u0003h4\u0000\u041d"+
		"\u041e\u0005V\u0000\u0000\u041e\u0420\u0003h4\u0000\u041f\u041d\u0001"+
		"\u0000\u0000\u0000\u0420\u0423\u0001\u0000\u0000\u0000\u0421\u041f\u0001"+
		"\u0000\u0000\u0000\u0421\u0422\u0001\u0000\u0000\u0000\u0422\u0424\u0001"+
		"\u0000\u0000\u0000\u0423\u0421\u0001\u0000\u0000\u0000\u0424\u0425\u0005"+
		"U\u0000\u0000\u0425\u0427\u0001\u0000\u0000\u0000\u0426\u03ed\u0001\u0000"+
		"\u0000\u0000\u0426\u03f7\u0001\u0000\u0000\u0000\u0426\u0406\u0001\u0000"+
		"\u0000\u0000\u0426\u0415\u0001\u0000\u0000\u0000\u0426\u0419\u0001\u0000"+
		"\u0000\u0000\u0427\u0091\u0001\u0000\u0000\u0000\u0428\u0429\u0007\u0003"+
		"\u0000\u0000\u0429\u0093\u0001\u0000\u0000\u0000\u042a\u042b\u0005+\u0000"+
		"\u0000\u042b\u042d\u0003\u00a4R\u0000\u042c\u042e\u0003\u0012\t\u0000"+
		"\u042d\u042c\u0001\u0000\u0000\u0000\u042d\u042e\u0001\u0000\u0000\u0000"+
		"\u042e\u042f\u0001\u0000\u0000\u0000\u042f\u0432\u0003\u0096K\u0000\u0430"+
		"\u0431\u0005\u0019\u0000\u0000\u0431\u0433\u0003\u00f4z\u0000\u0432\u0430"+
		"\u0001\u0000\u0000\u0000\u0432\u0433\u0001\u0000\u0000\u0000\u0433\u0434"+
		"\u0001\u0000\u0000\u0000\u0434\u0435\u0003\u009cN\u0000\u0435\u0095\u0001"+
		"\u0000\u0000\u0000\u0436\u0438\u0005O\u0000\u0000\u0437\u0439\u0003\u0098"+
		"L\u0000\u0438\u0437\u0001\u0000\u0000\u0000\u0438\u0439\u0001\u0000\u0000"+
		"\u0000\u0439\u043a\u0001\u0000\u0000\u0000\u043a\u043b\u0005P\u0000\u0000"+
		"\u043b\u0097\u0001\u0000\u0000\u0000\u043c\u0441\u0003\u009aM\u0000\u043d"+
		"\u043e\u0005V\u0000\u0000\u043e\u0440\u0003\u009aM\u0000\u043f\u043d\u0001"+
		"\u0000\u0000\u0000\u0440\u0443\u0001\u0000\u0000\u0000\u0441\u043f\u0001"+
		"\u0000\u0000\u0000\u0441\u0442\u0001\u0000\u0000\u0000\u0442\u0444\u0001"+
		"\u0000\u0000\u0000\u0443\u0441\u0001\u0000\u0000\u0000\u0444\u0445\u0004"+
		"L\u0001\u0000\u0445\u0099\u0001\u0000\u0000\u0000\u0446\u0448\u0003r9"+
		"\u0000\u0447\u0446\u0001\u0000\u0000\u0000\u0448\u044b\u0001\u0000\u0000"+
		"\u0000\u0449\u0447\u0001\u0000\u0000\u0000\u0449\u044a\u0001\u0000\u0000"+
		"\u0000\u044a\u044c\u0001\u0000\u0000\u0000\u044b\u0449\u0001\u0000\u0000"+
		"\u0000\u044c\u0454\u0003\u00f6{\u0000\u044d\u044f\u0003r9\u0000\u044e"+
		"\u044d\u0001\u0000\u0000\u0000\u044f\u0452\u0001\u0000\u0000\u0000\u0450"+
		"\u044e\u0001\u0000\u0000\u0000\u0450\u0451\u0001\u0000\u0000\u0000\u0451"+
		"\u0453\u0001\u0000\u0000\u0000\u0452\u0450\u0001\u0000\u0000\u0000\u0453"+
		"\u0455\u0005}\u0000\u0000\u0454\u0450\u0001\u0000\u0000\u0000\u0454\u0455"+
		"\u0001\u0000\u0000\u0000\u0455\u0456\u0001\u0000\u0000\u0000\u0456\u0457"+
		"\u0003\u00a4R\u0000\u0457\u009b\u0001\u0000\u0000\u0000\u0458\u045d\u0005"+
		"Q\u0000\u0000\u0459\u045c\u0003&\u0013\u0000\u045a\u045c\u00036\u001b"+
		"\u0000\u045b\u0459\u0001\u0000\u0000\u0000\u045b\u045a\u0001\u0000\u0000"+
		"\u0000\u045c\u045f\u0001\u0000\u0000\u0000\u045d\u045b\u0001\u0000\u0000"+
		"\u0000\u045d\u045e\u0001\u0000\u0000\u0000\u045e\u0460\u0001\u0000\u0000"+
		"\u0000\u045f\u045d\u0001\u0000\u0000\u0000\u0460\u0461\u0005R\u0000\u0000"+
		"\u0461\u009d\u0001\u0000\u0000\u0000\u0462\u0466\u0005Q\u0000\u0000\u0463"+
		"\u0465\u0003\u00a0P\u0000\u0464\u0463\u0001\u0000\u0000\u0000\u0465\u0468"+
		"\u0001\u0000\u0000\u0000\u0466\u0464\u0001\u0000\u0000\u0000\u0466\u0467"+
		"\u0001\u0000\u0000\u0000\u0467\u0469\u0001\u0000\u0000\u0000\u0468\u0466"+
		"\u0001\u0000\u0000\u0000\u0469\u046a\u0005R\u0000\u0000\u046a\u009f\u0001"+
		"\u0000\u0000\u0000\u046b\u046c\u0003\u00a2Q\u0000\u046c\u046d\u0005U\u0000"+
		"\u0000\u046d\u0471\u0001\u0000\u0000\u0000\u046e\u0471\u0003\u00a8T\u0000"+
		"\u046f\u0471\u0003\u00aaU\u0000\u0470\u046b\u0001\u0000\u0000\u0000\u0470"+
		"\u046e\u0001\u0000\u0000\u0000\u0470\u046f\u0001\u0000\u0000\u0000\u0471"+
		"\u00a1\u0001\u0000\u0000\u0000\u0472\u0474\u0003\u000e\u0007\u0000\u0473"+
		"\u0472\u0001\u0000\u0000\u0000\u0474\u0477\u0001\u0000\u0000\u0000\u0475"+
		"\u0473\u0001\u0000\u0000\u0000\u0475\u0476\u0001\u0000\u0000\u0000\u0476"+
		"\u0480\u0001\u0000\u0000\u0000\u0477\u0475\u0001\u0000\u0000\u0000\u0478"+
		"\u0479\u0005=\u0000\u0000\u0479\u047a\u0003\u00a4R\u0000\u047a\u047b\u0005"+
		"X\u0000\u0000\u047b\u047c\u0003\u00c6c\u0000\u047c\u0481\u0001\u0000\u0000"+
		"\u0000\u047d\u047e\u0003\u00f6{\u0000\u047e\u047f\u0003J%\u0000\u047f"+
		"\u0481\u0001\u0000\u0000\u0000\u0480\u0478\u0001\u0000\u0000\u0000\u0480"+
		"\u047d\u0001\u0000\u0000\u0000\u0481\u00a3\u0001\u0000\u0000\u0000\u0482"+
		"\u0483\u0007\u0004\u0000\u0000\u0483\u00a5\u0001\u0000\u0000\u0000\u0484"+
		"\u0485\u0007\u0005\u0000\u0000\u0485\u00a7\u0001\u0000\u0000\u0000\u0486"+
		"\u0488\u0003\f\u0006\u0000\u0487\u0486\u0001\u0000\u0000\u0000\u0488\u048b"+
		"\u0001\u0000\u0000\u0000\u0489\u0487\u0001\u0000\u0000\u0000\u0489\u048a"+
		"\u0001\u0000\u0000\u0000\u048a\u0490\u0001\u0000\u0000\u0000\u048b\u0489"+
		"\u0001\u0000\u0000\u0000\u048c\u0491\u0003\u0010\b\u0000\u048d\u0491\u0003"+
		" \u0010\u0000\u048e\u0491\u0003\u0094J\u0000\u048f\u0491\u0003\u0018\f"+
		"\u0000\u0490\u048c\u0001\u0000\u0000\u0000\u0490\u048d\u0001\u0000\u0000"+
		"\u0000\u0490\u048e\u0001\u0000\u0000\u0000\u0490\u048f\u0001\u0000\u0000"+
		"\u0000\u0491\u00a9\u0001\u0000\u0000\u0000\u0492\u050e\u0003\u009eO\u0000"+
		"\u0493\u0494\u0005\u0002\u0000\u0000\u0494\u0497\u0003\u00c6c\u0000\u0495"+
		"\u0496\u0005^\u0000\u0000\u0496\u0498\u0003\u00c6c\u0000\u0497\u0495\u0001"+
		"\u0000\u0000\u0000\u0497\u0498\u0001\u0000\u0000\u0000\u0498\u0499\u0001"+
		"\u0000\u0000\u0000\u0499\u049a\u0005U\u0000\u0000\u049a\u050e\u0001\u0000"+
		"\u0000\u0000\u049b\u049c\u0005\u0018\u0000\u0000\u049c\u049d\u0005O\u0000"+
		"\u0000\u049d\u049e\u0003\u00c6c\u0000\u049e\u049f\u0005P\u0000\u0000\u049f"+
		"\u04a2\u0003\u00aaU\u0000\u04a0\u04a1\u0005\u000f\u0000\u0000\u04a1\u04a3"+
		"\u0003\u00aaU\u0000\u04a2\u04a0\u0001\u0000\u0000\u0000\u04a2\u04a3\u0001"+
		"\u0000\u0000\u0000\u04a3\u050e\u0001\u0000\u0000\u0000\u04a4\u04a5\u0005"+
		"\u0016\u0000\u0000\u04a5\u04a6\u0005O\u0000\u0000\u04a6\u04a7\u0003\u00bc"+
		"^\u0000\u04a7\u04a8\u0005P\u0000\u0000\u04a8\u04a9\u0003\u00aaU\u0000"+
		"\u04a9\u050e\u0001\u0000\u0000\u0000\u04aa\u04ab\u0005A\u0000\u0000\u04ab"+
		"\u04ac\u0005O\u0000\u0000\u04ac\u04ad\u0003\u00c6c\u0000\u04ad\u04ae\u0005"+
		"P\u0000\u0000\u04ae\u04af\u0003\u00aaU\u0000\u04af\u050e\u0001\u0000\u0000"+
		"\u0000\u04b0\u04b1\u0005\r\u0000\u0000\u04b1\u04b2\u0003\u00aaU\u0000"+
		"\u04b2\u04b3\u0005A\u0000\u0000\u04b3\u04b4\u0005O\u0000\u0000\u04b4\u04b5"+
		"\u0003\u00c6c\u0000\u04b5\u04b6\u0005P\u0000\u0000\u04b6\u04b7\u0005U"+
		"\u0000\u0000\u04b7\u050e\u0001\u0000\u0000\u0000\u04b8\u04b9\u0005;\u0000"+
		"\u0000\u04b9\u04c3\u0003\u009eO\u0000\u04ba\u04bc\u0003\u00acV\u0000\u04bb"+
		"\u04ba\u0001\u0000\u0000\u0000\u04bc\u04bd\u0001\u0000\u0000\u0000\u04bd"+
		"\u04bb\u0001\u0000\u0000\u0000\u04bd\u04be\u0001\u0000\u0000\u0000\u04be"+
		"\u04c0\u0001\u0000\u0000\u0000\u04bf\u04c1\u0003\u00b0X\u0000\u04c0\u04bf"+
		"\u0001\u0000\u0000\u0000\u04c0\u04c1\u0001\u0000\u0000\u0000\u04c1\u04c4"+
		"\u0001\u0000\u0000\u0000\u04c2\u04c4\u0003\u00b0X\u0000\u04c3\u04bb\u0001"+
		"\u0000\u0000\u0000\u04c3\u04c2\u0001\u0000\u0000\u0000\u04c4\u050e\u0001"+
		"\u0000\u0000\u0000\u04c5\u04c6\u0005;\u0000\u0000\u04c6\u04c7\u0003\u00b2"+
		"Y\u0000\u04c7\u04cb\u0003\u009eO\u0000\u04c8\u04ca\u0003\u00acV\u0000"+
		"\u04c9\u04c8\u0001\u0000\u0000\u0000\u04ca\u04cd\u0001\u0000\u0000\u0000"+
		"\u04cb\u04c9\u0001\u0000\u0000\u0000\u04cb\u04cc\u0001\u0000\u0000\u0000"+
		"\u04cc\u04cf\u0001\u0000\u0000\u0000\u04cd\u04cb\u0001\u0000\u0000\u0000"+
		"\u04ce\u04d0\u0003\u00b0X\u0000\u04cf\u04ce\u0001\u0000\u0000\u0000\u04cf"+
		"\u04d0\u0001\u0000\u0000\u0000\u04d0\u050e\u0001\u0000\u0000\u0000\u04d1"+
		"\u04d2\u00053\u0000\u0000\u04d2\u04d3\u0005O\u0000\u0000\u04d3\u04d4\u0003"+
		"\u00c6c\u0000\u04d4\u04d5\u0005P\u0000\u0000\u04d5\u04d9\u0005Q\u0000"+
		"\u0000\u04d6\u04d8\u0003\u00b8\\\u0000\u04d7\u04d6\u0001\u0000\u0000\u0000"+
		"\u04d8\u04db\u0001\u0000\u0000\u0000\u04d9\u04d7\u0001\u0000\u0000\u0000"+
		"\u04d9\u04da\u0001\u0000\u0000\u0000\u04da\u04df\u0001\u0000\u0000\u0000"+
		"\u04db\u04d9\u0001\u0000\u0000\u0000\u04dc\u04de\u0003\u00ba]\u0000\u04dd"+
		"\u04dc\u0001\u0000\u0000\u0000\u04de\u04e1\u0001\u0000\u0000\u0000\u04df"+
		"\u04dd\u0001\u0000\u0000\u0000\u04df\u04e0\u0001\u0000\u0000\u0000\u04e0"+
		"\u04e2\u0001\u0000\u0000\u0000\u04e1\u04df\u0001\u0000\u0000\u0000\u04e2"+
		"\u04e3\u0005R\u0000\u0000\u04e3\u050e\u0001\u0000\u0000\u0000\u04e4\u04e5"+
		"\u00054\u0000\u0000\u04e5\u04e6\u0005O\u0000\u0000\u04e6\u04e7\u0003\u00c6"+
		"c\u0000\u04e7\u04e8\u0005P\u0000\u0000\u04e8\u04e9\u0003\u009eO\u0000"+
		"\u04e9\u050e\u0001\u0000\u0000\u0000\u04ea\u04ec\u0005-\u0000\u0000\u04eb"+
		"\u04ed\u0003\u00c6c\u0000\u04ec\u04eb\u0001\u0000\u0000\u0000\u04ec\u04ed"+
		"\u0001\u0000\u0000\u0000\u04ed\u04ee\u0001\u0000\u0000\u0000\u04ee\u050e"+
		"\u0005U\u0000\u0000\u04ef\u04f0\u00056\u0000\u0000\u04f0\u04f1\u0003\u00c6"+
		"c\u0000\u04f1\u04f2\u0005U\u0000\u0000\u04f2\u050e\u0001\u0000\u0000\u0000"+
		"\u04f3\u04f5\u0005\u0004\u0000\u0000\u04f4\u04f6\u0003\u00a4R\u0000\u04f5"+
		"\u04f4\u0001\u0000\u0000\u0000\u04f5\u04f6\u0001\u0000\u0000\u0000\u04f6"+
		"\u04f7\u0001\u0000\u0000\u0000\u04f7\u050e\u0005U\u0000\u0000\u04f8\u04fa"+
		"\u0005\u000b\u0000\u0000\u04f9\u04fb\u0003\u00a4R\u0000\u04fa\u04f9\u0001"+
		"\u0000\u0000\u0000\u04fa\u04fb\u0001\u0000\u0000\u0000\u04fb\u04fc\u0001"+
		"\u0000\u0000\u0000\u04fc\u050e\u0005U\u0000\u0000\u04fd\u04fe\u0005C\u0000"+
		"\u0000\u04fe\u04ff\u0003\u00c6c\u0000\u04ff\u0500\u0005U\u0000\u0000\u0500"+
		"\u050e\u0001\u0000\u0000\u0000\u0501\u050e\u0005U\u0000\u0000\u0502\u0503"+
		"\u0003\u00c6c\u0000\u0503\u0504\u0005U\u0000\u0000\u0504\u050e\u0001\u0000"+
		"\u0000\u0000\u0505\u0507\u0003\u00d6k\u0000\u0506\u0508\u0005U\u0000\u0000"+
		"\u0507\u0506\u0001\u0000\u0000\u0000\u0507\u0508\u0001\u0000\u0000\u0000"+
		"\u0508\u050e\u0001\u0000\u0000\u0000\u0509\u050a\u0003\u00a4R\u0000\u050a"+
		"\u050b\u0005^\u0000\u0000\u050b\u050c\u0003\u00aaU\u0000\u050c\u050e\u0001"+
		"\u0000\u0000\u0000\u050d\u0492\u0001\u0000\u0000\u0000\u050d\u0493\u0001"+
		"\u0000\u0000\u0000\u050d\u049b\u0001\u0000\u0000\u0000\u050d\u04a4\u0001"+
		"\u0000\u0000\u0000\u050d\u04aa\u0001\u0000\u0000\u0000\u050d\u04b0\u0001"+
		"\u0000\u0000\u0000\u050d\u04b8\u0001\u0000\u0000\u0000\u050d\u04c5\u0001"+
		"\u0000\u0000\u0000\u050d\u04d1\u0001\u0000\u0000\u0000\u050d\u04e4\u0001"+
		"\u0000\u0000\u0000\u050d\u04ea\u0001\u0000\u0000\u0000\u050d\u04ef\u0001"+
		"\u0000\u0000\u0000\u050d\u04f3\u0001\u0000\u0000\u0000\u050d\u04f8\u0001"+
		"\u0000\u0000\u0000\u050d\u04fd\u0001\u0000\u0000\u0000\u050d\u0501\u0001"+
		"\u0000\u0000\u0000\u050d\u0502\u0001\u0000\u0000\u0000\u050d\u0505\u0001"+
		"\u0000\u0000\u0000\u050d\u0509\u0001\u0000\u0000\u0000\u050e\u00ab\u0001"+
		"\u0000\u0000\u0000\u050f\u0510\u0005\u0007\u0000\u0000\u0510\u0514\u0005"+
		"O\u0000\u0000\u0511\u0513\u0003\u000e\u0007\u0000\u0512\u0511\u0001\u0000"+
		"\u0000\u0000\u0513\u0516\u0001\u0000\u0000\u0000\u0514\u0512\u0001\u0000"+
		"\u0000\u0000\u0514\u0515\u0001\u0000\u0000\u0000\u0515\u0517\u0001\u0000"+
		"\u0000\u0000\u0516\u0514\u0001\u0000\u0000\u0000\u0517\u0518\u0003\u00ae"+
		"W\u0000\u0518\u0519\u0003\u00a4R\u0000\u0519\u051a\u0005P\u0000\u0000"+
		"\u051a\u051b\u0003\u009eO\u0000\u051b\u00ad\u0001\u0000\u0000\u0000\u051c"+
		"\u0521\u0003h4\u0000\u051d\u051e\u0005l\u0000\u0000\u051e\u0520\u0003"+
		"h4\u0000\u051f\u051d\u0001\u0000\u0000\u0000\u0520\u0523\u0001\u0000\u0000"+
		"\u0000\u0521\u051f\u0001\u0000\u0000\u0000\u0521\u0522\u0001\u0000\u0000"+
		"\u0000\u0522\u00af\u0001\u0000\u0000\u0000\u0523\u0521\u0001\u0000\u0000"+
		"\u0000\u0524\u0525\u0005\u0014\u0000\u0000\u0525\u0526\u0003\u009eO\u0000"+
		"\u0526\u00b1\u0001\u0000\u0000\u0000\u0527\u0528\u0005O\u0000\u0000\u0528"+
		"\u052a\u0003\u00b4Z\u0000\u0529\u052b\u0005U\u0000\u0000\u052a\u0529\u0001"+
		"\u0000\u0000\u0000\u052a\u052b\u0001\u0000\u0000\u0000\u052b\u052c\u0001"+
		"\u0000\u0000\u0000\u052c\u052d\u0005P\u0000\u0000\u052d\u00b3\u0001\u0000"+
		"\u0000\u0000\u052e\u0533\u0003\u00b6[\u0000\u052f\u0530\u0005U\u0000\u0000"+
		"\u0530\u0532\u0003\u00b6[\u0000\u0531\u052f\u0001\u0000\u0000\u0000\u0532"+
		"\u0535\u0001\u0000\u0000\u0000\u0533\u0531\u0001\u0000\u0000\u0000\u0533"+
		"\u0534\u0001\u0000\u0000\u0000\u0534\u00b5\u0001\u0000\u0000\u0000\u0535"+
		"\u0533\u0001\u0000\u0000\u0000\u0536\u0538\u0003\u000e\u0007\u0000\u0537"+
		"\u0536\u0001\u0000\u0000\u0000\u0538\u053b\u0001\u0000\u0000\u0000\u0539"+
		"\u0537\u0001\u0000\u0000\u0000\u0539\u053a\u0001\u0000\u0000\u0000\u053a"+
		"\u0541\u0001\u0000\u0000\u0000\u053b\u0539\u0001\u0000\u0000\u0000\u053c"+
		"\u053d\u0003\u00e0p\u0000\u053d\u053e\u0003N\'\u0000\u053e\u0542\u0001"+
		"\u0000\u0000\u0000\u053f\u0540\u0005=\u0000\u0000\u0540\u0542\u0003\u00a4"+
		"R\u0000\u0541\u053c\u0001\u0000\u0000\u0000\u0541\u053f\u0001\u0000\u0000"+
		"\u0000\u0542\u0543\u0001\u0000\u0000\u0000\u0543\u0544\u0005X\u0000\u0000"+
		"\u0544\u0545\u0003\u00c6c\u0000\u0545\u0548\u0001\u0000\u0000\u0000\u0546"+
		"\u0548\u0003h4\u0000\u0547\u0539\u0001\u0000\u0000\u0000\u0547\u0546\u0001"+
		"\u0000\u0000\u0000\u0548\u00b7\u0001\u0000\u0000\u0000\u0549\u054a\u0003"+
		"\u00ba]\u0000\u054a\u054b\u0005^\u0000\u0000\u054b\u054d\u0001\u0000\u0000"+
		"\u0000\u054c\u0549\u0001\u0000\u0000\u0000\u054d\u054e\u0001\u0000\u0000"+
		"\u0000\u054e\u054c\u0001\u0000\u0000\u0000\u054e\u054f\u0001\u0000\u0000"+
		"\u0000\u054f\u0551\u0001\u0000\u0000\u0000\u0550\u0552\u0003\u00a0P\u0000"+
		"\u0551\u0550\u0001\u0000\u0000\u0000\u0552\u0553\u0001\u0000\u0000\u0000"+
		"\u0553\u0551\u0001\u0000\u0000\u0000\u0553\u0554\u0001\u0000\u0000\u0000"+
		"\u0554\u00b9\u0001\u0000\u0000\u0000\u0555\u055b\u0005\u0006\u0000\u0000"+
		"\u0556\u055c\u0003\u00c6c\u0000\u0557\u055c\u0005\u0081\u0000\u0000\u0558"+
		"\u0559\u0003\u00f6{\u0000\u0559\u055a\u0003\u00a4R\u0000\u055a\u055c\u0001"+
		"\u0000\u0000\u0000\u055b\u0556\u0001\u0000\u0000\u0000\u055b\u0557\u0001"+
		"\u0000\u0000\u0000\u055b\u0558\u0001\u0000\u0000\u0000\u055c\u055f\u0001"+
		"\u0000\u0000\u0000\u055d\u055f\u0005\f\u0000\u0000\u055e\u0555\u0001\u0000"+
		"\u0000\u0000\u055e\u055d\u0001\u0000\u0000\u0000\u055f\u00bb\u0001\u0000"+
		"\u0000\u0000\u0560\u056d\u0003\u00c0`\u0000\u0561\u0563\u0003\u00be_\u0000"+
		"\u0562\u0561\u0001\u0000\u0000\u0000\u0562\u0563\u0001\u0000\u0000\u0000"+
		"\u0563\u0564\u0001\u0000\u0000\u0000\u0564\u0566\u0005U\u0000\u0000\u0565"+
		"\u0567\u0003\u00c6c\u0000\u0566\u0565\u0001\u0000\u0000\u0000\u0566\u0567"+
		"\u0001\u0000\u0000\u0000\u0567\u0568\u0001\u0000\u0000\u0000\u0568\u056a"+
		"\u0005U\u0000\u0000\u0569\u056b\u0003\u00c2a\u0000\u056a\u0569\u0001\u0000"+
		"\u0000\u0000\u056a\u056b\u0001\u0000\u0000\u0000\u056b\u056d\u0001\u0000"+
		"\u0000\u0000\u056c\u0560\u0001\u0000\u0000\u0000\u056c\u0562\u0001\u0000"+
		"\u0000\u0000\u056d\u00bd\u0001\u0000\u0000\u0000\u056e\u0571\u0003\u00a2"+
		"Q\u0000\u056f\u0571\u0003\u00c2a\u0000\u0570\u056e\u0001\u0000\u0000\u0000"+
		"\u0570\u056f\u0001\u0000\u0000\u0000\u0571\u00bf\u0001\u0000\u0000\u0000"+
		"\u0572\u0574\u0003\u000e\u0007\u0000\u0573\u0572\u0001\u0000\u0000\u0000"+
		"\u0574\u0577\u0001\u0000\u0000\u0000\u0575\u0573\u0001\u0000\u0000\u0000"+
		"\u0575\u0576\u0001\u0000\u0000\u0000\u0576\u057a\u0001\u0000\u0000\u0000"+
		"\u0577\u0575\u0001\u0000\u0000\u0000\u0578\u057b\u0003\u00f6{\u0000\u0579"+
		"\u057b\u0005=\u0000\u0000\u057a\u0578\u0001\u0000\u0000\u0000\u057a\u0579"+
		"\u0001\u0000\u0000\u0000\u057b\u057c\u0001\u0000\u0000\u0000\u057c\u057d"+
		"\u0003N\'\u0000\u057d\u057e\u0005^\u0000\u0000\u057e\u057f\u0003\u00c6"+
		"c\u0000\u057f\u00c1\u0001\u0000\u0000\u0000\u0580\u0585\u0003\u00c6c\u0000"+
		"\u0581\u0582\u0005V\u0000\u0000\u0582\u0584\u0003\u00c6c\u0000\u0583\u0581"+
		"\u0001\u0000\u0000\u0000\u0584\u0587\u0001\u0000\u0000\u0000\u0585\u0583"+
		"\u0001\u0000\u0000\u0000\u0585\u0586\u0001\u0000\u0000\u0000\u0586\u00c3"+
		"\u0001\u0000\u0000\u0000\u0587\u0585\u0001\u0000\u0000\u0000\u0588\u058c"+
		"\u0003\u00a4R\u0000\u0589\u058c\u00055\u0000\u0000\u058a\u058c\u00052"+
		"\u0000\u0000\u058b\u0588\u0001\u0000\u0000\u0000\u058b\u0589\u0001\u0000"+
		"\u0000\u0000\u058b\u058a\u0001\u0000\u0000\u0000\u058c\u058d\u0001\u0000"+
		"\u0000\u0000\u058d\u058e\u0003\u0100\u0080\u0000\u058e\u00c5\u0001\u0000"+
		"\u0000\u0000\u058f\u0590\u0006c\uffff\uffff\u0000\u0590\u05bb\u0003\u00d4"+
		"j\u0000\u0591\u05bb\u0003\u00c4b\u0000\u0592\u0593\u0003\u00f6{\u0000"+
		"\u0593\u0599\u0005{\u0000\u0000\u0594\u0596\u0003\u00fa}\u0000\u0595\u0594"+
		"\u0001\u0000\u0000\u0000\u0595\u0596\u0001\u0000\u0000\u0000\u0596\u0597"+
		"\u0001\u0000\u0000\u0000\u0597\u059a\u0003\u00a4R\u0000\u0598\u059a\u0005"+
		"!\u0000\u0000\u0599\u0595\u0001\u0000\u0000\u0000\u0599\u0598\u0001\u0000"+
		"\u0000\u0000\u059a\u05bb\u0001\u0000\u0000\u0000\u059b\u059c\u0003T*\u0000"+
		"\u059c\u059e\u0005{\u0000\u0000\u059d\u059f\u0003\u00fa}\u0000\u059e\u059d"+
		"\u0001\u0000\u0000\u0000\u059e\u059f\u0001\u0000\u0000\u0000\u059f\u05a0"+
		"\u0001\u0000\u0000\u0000\u05a0\u05a1\u0005!\u0000\u0000\u05a1\u05bb\u0001"+
		"\u0000\u0000\u0000\u05a2\u05bb\u0003\u00d6k\u0000\u05a3\u05a4\u0007\u0006"+
		"\u0000\u0000\u05a4\u05bb\u0003\u00c6c\u0011\u05a5\u05a9\u0005O\u0000\u0000"+
		"\u05a6\u05a8\u0003r9\u0000\u05a7\u05a6\u0001\u0000\u0000\u0000\u05a8\u05ab"+
		"\u0001\u0000\u0000\u0000\u05a9\u05a7\u0001\u0000\u0000\u0000\u05a9\u05aa"+
		"\u0001\u0000\u0000\u0000\u05aa\u05ac\u0001\u0000\u0000\u0000\u05ab\u05a9"+
		"\u0001\u0000\u0000\u0000\u05ac\u05b1\u0003\u00f6{\u0000\u05ad\u05ae\u0005"+
		"k\u0000\u0000\u05ae\u05b0\u0003\u00f6{\u0000\u05af\u05ad\u0001\u0000\u0000"+
		"\u0000\u05b0\u05b3\u0001\u0000\u0000\u0000\u05b1\u05af\u0001\u0000\u0000"+
		"\u0000\u05b1\u05b2\u0001\u0000\u0000\u0000\u05b2\u05b4\u0001\u0000\u0000"+
		"\u0000\u05b3\u05b1\u0001\u0000\u0000\u0000\u05b4\u05b5\u0005P\u0000\u0000"+
		"\u05b5\u05b6\u0003\u00c6c\u0010\u05b6\u05bb\u0001\u0000\u0000\u0000\u05b7"+
		"\u05b8\u0005!\u0000\u0000\u05b8\u05bb\u0003\u00e2q\u0000\u05b9\u05bb\u0003"+
		"\u00ceg\u0000\u05ba\u058f\u0001\u0000\u0000\u0000\u05ba\u0591\u0001\u0000"+
		"\u0000\u0000\u05ba\u0592\u0001\u0000\u0000\u0000\u05ba\u059b\u0001\u0000"+
		"\u0000\u0000\u05ba\u05a2\u0001\u0000\u0000\u0000\u05ba\u05a3\u0001\u0000"+
		"\u0000\u0000\u05ba\u05a5\u0001\u0000\u0000\u0000\u05ba\u05b7\u0001\u0000"+
		"\u0000\u0000\u05ba\u05b9\u0001\u0000\u0000\u0000\u05bb\u060f\u0001\u0000"+
		"\u0000\u0000\u05bc\u05bd\n\u000e\u0000\u0000\u05bd\u05be\u0007\u0007\u0000"+
		"\u0000\u05be\u060e\u0003\u00c6c\u000f\u05bf\u05c0\n\r\u0000\u0000\u05c0"+
		"\u05c1\u0007\b\u0000\u0000\u05c1\u060e\u0003\u00c6c\u000e\u05c2\u05ca"+
		"\n\f\u0000\u0000\u05c3\u05c4\u0005Z\u0000\u0000\u05c4\u05cb\u0005Z\u0000"+
		"\u0000\u05c5\u05c6\u0005Y\u0000\u0000\u05c6\u05c7\u0005Y\u0000\u0000\u05c7"+
		"\u05cb\u0005Y\u0000\u0000\u05c8\u05c9\u0005Y\u0000\u0000\u05c9\u05cb\u0005"+
		"Y\u0000\u0000\u05ca\u05c3\u0001\u0000\u0000\u0000\u05ca\u05c5\u0001\u0000"+
		"\u0000\u0000\u05ca\u05c8\u0001\u0000\u0000\u0000\u05cb\u05cc\u0001\u0000"+
		"\u0000\u0000\u05cc\u060e\u0003\u00c6c\r\u05cd\u05ce\n\u000b\u0000\u0000"+
		"\u05ce\u05cf\u0007\t\u0000\u0000\u05cf\u060e\u0003\u00c6c\f\u05d0\u05d1"+
		"\n\t\u0000\u0000\u05d1\u05d2\u0007\n\u0000\u0000\u05d2\u060e\u0003\u00c6"+
		"c\n\u05d3\u05d4\n\b\u0000\u0000\u05d4\u05d5\u0005k\u0000\u0000\u05d5\u060e"+
		"\u0003\u00c6c\t\u05d6\u05d7\n\u0007\u0000\u0000\u05d7\u05d8\u0005m\u0000"+
		"\u0000\u05d8\u060e\u0003\u00c6c\b\u05d9\u05da\n\u0006\u0000\u0000\u05da"+
		"\u05db\u0005l\u0000\u0000\u05db\u060e\u0003\u00c6c\u0007\u05dc\u05dd\n"+
		"\u0005\u0000\u0000\u05dd\u05de\u0005c\u0000\u0000\u05de\u060e\u0003\u00c6"+
		"c\u0006\u05df\u05e0\n\u0004\u0000\u0000\u05e0\u05e1\u0005d\u0000\u0000"+
		"\u05e1\u060e\u0003\u00c6c\u0005\u05e2\u05e3\n\u0003\u0000\u0000\u05e3"+
		"\u05e4\u0005]\u0000\u0000\u05e4\u05e5\u0003\u00c6c\u0000\u05e5\u05e6\u0005"+
		"^\u0000\u0000\u05e6\u05e7\u0003\u00c6c\u0003\u05e7\u060e\u0001\u0000\u0000"+
		"\u0000\u05e8\u05e9\n\u0002\u0000\u0000\u05e9\u05ea\u0007\u000b\u0000\u0000"+
		"\u05ea\u060e\u0003\u00c6c\u0002\u05eb\u05ec\n\u0019\u0000\u0000\u05ec"+
		"\u05ed\u0005S\u0000\u0000\u05ed\u05ee\u0003\u00c6c\u0000\u05ee\u05ef\u0005"+
		"T\u0000\u0000\u05ef\u060e\u0001\u0000\u0000\u0000\u05f0\u05f1\n\u0018"+
		"\u0000\u0000\u05f1\u05fd\u0005W\u0000\u0000\u05f2\u05fe\u0003\u00a4R\u0000"+
		"\u05f3\u05fe\u0003\u00c4b\u0000\u05f4\u05fe\u00055\u0000\u0000\u05f5\u05f7"+
		"\u0005!\u0000\u0000\u05f6\u05f8\u0003\u00f2y\u0000\u05f7\u05f6\u0001\u0000"+
		"\u0000\u0000\u05f7\u05f8\u0001\u0000\u0000\u0000\u05f8\u05f9\u0001\u0000"+
		"\u0000\u0000\u05f9\u05fe\u0003\u00e6s\u0000\u05fa\u05fb\u00052\u0000\u0000"+
		"\u05fb\u05fe\u0003\u00fc~\u0000\u05fc\u05fe\u0003\u00ecv\u0000\u05fd\u05f2"+
		"\u0001\u0000\u0000\u0000\u05fd\u05f3\u0001\u0000\u0000\u0000\u05fd\u05f4"+
		"\u0001\u0000\u0000\u0000\u05fd\u05f5\u0001\u0000\u0000\u0000\u05fd\u05fa"+
		"\u0001\u0000\u0000\u0000\u05fd\u05fc\u0001\u0000\u0000\u0000\u05fe\u060e"+
		"\u0001\u0000\u0000\u0000\u05ff\u0600\n\u0016\u0000\u0000\u0600\u0602\u0005"+
		"{\u0000\u0000\u0601\u0603\u0003\u00fa}\u0000\u0602\u0601\u0001\u0000\u0000"+
		"\u0000\u0602\u0603\u0001\u0000\u0000\u0000\u0603\u0604\u0001\u0000\u0000"+
		"\u0000\u0604\u060e\u0003\u00a4R\u0000\u0605\u0606\n\u0012\u0000\u0000"+
		"\u0606\u060e\u0007\f\u0000\u0000\u0607\u0608\n\n\u0000\u0000\u0608\u060b"+
		"\u0005\u001b\u0000\u0000\u0609\u060c\u0003\u00f6{\u0000\u060a\u060c\u0003"+
		"\u00c8d\u0000\u060b\u0609\u0001\u0000\u0000\u0000\u060b\u060a\u0001\u0000"+
		"\u0000\u0000\u060c\u060e\u0001\u0000\u0000\u0000\u060d\u05bc\u0001\u0000"+
		"\u0000\u0000\u060d\u05bf\u0001\u0000\u0000\u0000\u060d\u05c2\u0001\u0000"+
		"\u0000\u0000\u060d\u05cd\u0001\u0000\u0000\u0000\u060d\u05d0\u0001\u0000"+
		"\u0000\u0000\u060d\u05d3\u0001\u0000\u0000\u0000\u060d\u05d6\u0001\u0000"+
		"\u0000\u0000\u060d\u05d9\u0001\u0000\u0000\u0000\u060d\u05dc\u0001\u0000"+
		"\u0000\u0000\u060d\u05df\u0001\u0000\u0000\u0000\u060d\u05e2\u0001\u0000"+
		"\u0000\u0000\u060d\u05e8\u0001\u0000\u0000\u0000\u060d\u05eb\u0001\u0000"+
		"\u0000\u0000\u060d\u05f0\u0001\u0000\u0000\u0000\u060d\u05ff\u0001\u0000"+
		"\u0000\u0000\u060d\u0605\u0001\u0000\u0000\u0000\u060d\u0607\u0001\u0000"+
		"\u0000\u0000\u060e\u0611\u0001\u0000\u0000\u0000\u060f\u060d\u0001\u0000"+
		"\u0000\u0000\u060f\u0610\u0001\u0000\u0000\u0000\u0610\u00c7\u0001\u0000"+
		"\u0000\u0000\u0611\u060f\u0001\u0000\u0000\u0000\u0612\u0614\u0003\u000e"+
		"\u0007\u0000\u0613\u0612\u0001\u0000\u0000\u0000\u0614\u0617\u0001\u0000"+
		"\u0000\u0000\u0615\u0613\u0001\u0000\u0000\u0000\u0615\u0616\u0001\u0000"+
		"\u0000\u0000\u0616\u0618\u0001\u0000\u0000\u0000\u0617\u0615\u0001\u0000"+
		"\u0000\u0000\u0618\u061c\u0003\u00f6{\u0000\u0619\u061b\u0003r9\u0000"+
		"\u061a\u0619\u0001\u0000\u0000\u0000\u061b\u061e\u0001\u0000\u0000\u0000"+
		"\u061c\u061a\u0001\u0000\u0000\u0000\u061c\u061d\u0001\u0000\u0000\u0000"+
		"\u061d\u061f\u0001\u0000\u0000\u0000\u061e\u061c\u0001\u0000\u0000\u0000"+
		"\u061f\u0620\u0003J%\u0000\u0620\u0629\u0001\u0000\u0000\u0000\u0621\u0622"+
		"\u0003\u00f6{\u0000\u0622\u0624\u0005O\u0000\u0000\u0623\u0625\u0003\u00ca"+
		"e\u0000\u0624\u0623\u0001\u0000\u0000\u0000\u0624\u0625\u0001\u0000\u0000"+
		"\u0000\u0625\u0626\u0001\u0000\u0000\u0000\u0626\u0627\u0005P\u0000\u0000"+
		"\u0627\u0629\u0001\u0000\u0000\u0000\u0628\u0615\u0001\u0000\u0000\u0000"+
		"\u0628\u0621\u0001\u0000\u0000\u0000\u0629\u00c9\u0001\u0000\u0000\u0000"+
		"\u062a\u062f\u0003\u00ccf\u0000\u062b\u062c\u0005V\u0000\u0000\u062c\u062e"+
		"\u0003\u00ccf\u0000\u062d\u062b\u0001\u0000\u0000\u0000\u062e\u0631\u0001"+
		"\u0000\u0000\u0000\u062f\u062d\u0001\u0000\u0000\u0000\u062f\u0630\u0001"+
		"\u0000\u0000\u0000\u0630\u00cb\u0001\u0000\u0000\u0000\u0631\u062f\u0001"+
		"\u0000\u0000\u0000\u0632\u0633\u0003\u00c8d\u0000\u0633\u00cd\u0001\u0000"+
		"\u0000\u0000\u0634\u0635\u0003\u00d0h\u0000\u0635\u0636\u0005z\u0000\u0000"+
		"\u0636\u0637\u0003\u00d2i\u0000\u0637\u00cf\u0001\u0000\u0000\u0000\u0638"+
		"\u064f\u0003\u00a4R\u0000\u0639\u063b\u0005O\u0000\u0000\u063a\u063c\u0003"+
		"`0\u0000\u063b\u063a\u0001\u0000\u0000\u0000\u063b\u063c\u0001\u0000\u0000"+
		"\u0000\u063c\u063d\u0001\u0000\u0000\u0000\u063d\u064f\u0005P\u0000\u0000"+
		"\u063e\u063f\u0005O\u0000\u0000\u063f\u0644\u0003\u00a4R\u0000\u0640\u0641"+
		"\u0005V\u0000\u0000\u0641\u0643\u0003\u00a4R\u0000\u0642\u0640\u0001\u0000"+
		"\u0000\u0000\u0643\u0646\u0001\u0000\u0000\u0000\u0644\u0642\u0001\u0000"+
		"\u0000\u0000\u0644\u0645\u0001\u0000\u0000\u0000\u0645\u0647\u0001\u0000"+
		"\u0000\u0000\u0646\u0644\u0001\u0000\u0000\u0000\u0647\u0648\u0005P\u0000"+
		"\u0000\u0648\u064f\u0001\u0000\u0000\u0000\u0649\u064b\u0005O\u0000\u0000"+
		"\u064a\u064c\u0003d2\u0000\u064b\u064a\u0001\u0000\u0000\u0000\u064b\u064c"+
		"\u0001\u0000\u0000\u0000\u064c\u064d\u0001\u0000\u0000\u0000\u064d\u064f"+
		"\u0005P\u0000\u0000\u064e\u0638\u0001\u0000\u0000\u0000\u064e\u0639\u0001"+
		"\u0000\u0000\u0000\u064e\u063e\u0001\u0000\u0000\u0000\u064e\u0649\u0001"+
		"\u0000\u0000\u0000\u064f\u00d1\u0001\u0000\u0000\u0000\u0650\u0653\u0003"+
		"\u00c6c\u0000\u0651\u0653\u0003\u009eO\u0000\u0652\u0650\u0001\u0000\u0000"+
		"\u0000\u0652\u0651\u0001\u0000\u0000\u0000\u0653\u00d3\u0001\u0000\u0000"+
		"\u0000\u0654\u0655\u0005O\u0000\u0000\u0655\u0656\u0003\u00c6c\u0000\u0656"+
		"\u0657\u0005P\u0000\u0000\u0657\u0667\u0001\u0000\u0000\u0000\u0658\u0667"+
		"\u00055\u0000\u0000\u0659\u0667\u00052\u0000\u0000\u065a\u0667\u0003j"+
		"5\u0000\u065b\u0667\u0003\u00a4R\u0000\u065c\u065d\u0003.\u0017\u0000"+
		"\u065d\u065e\u0005W\u0000\u0000\u065e\u065f\u0005\t\u0000\u0000\u065f"+
		"\u0667\u0001\u0000\u0000\u0000\u0660\u0664\u0003\u00f2y\u0000\u0661\u0665"+
		"\u0003\u00fe\u007f\u0000\u0662\u0663\u00055\u0000\u0000\u0663\u0665\u0003"+
		"\u0100\u0080\u0000\u0664\u0661\u0001\u0000\u0000\u0000\u0664\u0662\u0001"+
		"\u0000\u0000\u0000\u0665\u0667\u0001\u0000\u0000\u0000\u0666\u0654\u0001"+
		"\u0000\u0000\u0000\u0666\u0658\u0001\u0000\u0000\u0000\u0666\u0659\u0001"+
		"\u0000\u0000\u0000\u0666\u065a\u0001\u0000\u0000\u0000\u0666\u065b\u0001"+
		"\u0000\u0000\u0000\u0666\u065c\u0001\u0000\u0000\u0000\u0666\u0660\u0001"+
		"\u0000\u0000\u0000\u0667\u00d5\u0001\u0000\u0000\u0000\u0668\u0669\u0005"+
		"3\u0000\u0000\u0669\u066a\u0005O\u0000\u0000\u066a\u066b\u0003\u00c6c"+
		"\u0000\u066b\u066c\u0005P\u0000\u0000\u066c\u0670\u0005Q\u0000\u0000\u066d"+
		"\u066f\u0003\u00d8l\u0000\u066e\u066d\u0001\u0000\u0000\u0000\u066f\u0672"+
		"\u0001\u0000\u0000\u0000\u0670\u066e\u0001\u0000\u0000\u0000\u0670\u0671"+
		"\u0001\u0000\u0000\u0000\u0671\u0673\u0001\u0000\u0000\u0000\u0672\u0670"+
		"\u0001\u0000\u0000\u0000\u0673\u0674\u0005R\u0000\u0000\u0674\u00d7\u0001"+
		"\u0000\u0000\u0000\u0675\u0687\u0005\u0006\u0000\u0000\u0676\u0688\u0003"+
		"\u00c2a\u0000\u0677\u067a\u0005N\u0000\u0000\u0678\u0679\u0005V\u0000"+
		"\u0000\u0679\u067b\u0005\f\u0000\u0000\u067a\u0678\u0001\u0000\u0000\u0000"+
		"\u067a\u067b\u0001\u0000\u0000\u0000\u067b\u0688\u0001\u0000\u0000\u0000"+
		"\u067c\u0681\u0003\u00dcn\u0000\u067d\u067e\u0005V\u0000\u0000\u067e\u0680"+
		"\u0003\u00dcn\u0000\u067f\u067d\u0001\u0000\u0000\u0000\u0680\u0683\u0001"+
		"\u0000\u0000\u0000\u0681\u067f\u0001\u0000\u0000\u0000\u0681\u0682\u0001"+
		"\u0000\u0000\u0000\u0682\u0685\u0001\u0000\u0000\u0000\u0683\u0681\u0001"+
		"\u0000\u0000\u0000\u0684\u0686\u0003\u00dam\u0000\u0685\u0684\u0001\u0000"+
		"\u0000\u0000\u0685\u0686\u0001\u0000\u0000\u0000\u0686\u0688\u0001\u0000"+
		"\u0000\u0000\u0687\u0676\u0001\u0000\u0000\u0000\u0687\u0677\u0001\u0000"+
		"\u0000\u0000\u0687\u067c\u0001\u0000\u0000\u0000\u0688\u0689\u0001\u0000"+
		"\u0000\u0000\u0689\u068a\u0007\r\u0000\u0000\u068a\u068f\u0003\u00deo"+
		"\u0000\u068b\u068c\u0005\f\u0000\u0000\u068c\u068d\u0007\r\u0000\u0000"+
		"\u068d\u068f\u0003\u00deo\u0000\u068e\u0675\u0001\u0000\u0000\u0000\u068e"+
		"\u068b\u0001\u0000\u0000\u0000\u068f\u00d9\u0001\u0000\u0000\u0000\u0690"+
		"\u0691\u0005@\u0000\u0000\u0691\u0692\u0003\u00c6c\u0000\u0692\u00db\u0001"+
		"\u0000\u0000\u0000\u0693\u0694\u0003\u00c8d\u0000\u0694\u00dd\u0001\u0000"+
		"\u0000\u0000\u0695\u069d\u0003\u009eO\u0000\u0696\u0698\u0003\u00a0P\u0000"+
		"\u0697\u0696\u0001\u0000\u0000\u0000\u0698\u069b\u0001\u0000\u0000\u0000"+
		"\u0699\u0697\u0001\u0000\u0000\u0000\u0699\u069a\u0001\u0000\u0000\u0000"+
		"\u069a\u069d\u0001\u0000\u0000\u0000\u069b\u0699\u0001\u0000\u0000\u0000"+
		"\u069c\u0695\u0001\u0000\u0000\u0000\u069c\u0699\u0001\u0000\u0000\u0000"+
		"\u069d\u00df\u0001\u0000\u0000\u0000\u069e\u069f\u0003T*\u0000\u069f\u00e1"+
		"\u0001\u0000\u0000\u0000\u06a0\u06a2\u0003\u00f2y\u0000\u06a1\u06a0\u0001"+
		"\u0000\u0000\u0000\u06a1\u06a2\u0001\u0000\u0000\u0000\u06a2\u06a3\u0001"+
		"\u0000\u0000\u0000\u06a3\u06a4\u0003\u00e4r\u0000\u06a4\u06a5\u0003\u00ea"+
		"u\u0000\u06a5\u06aa\u0001\u0000\u0000\u0000\u06a6\u06a7\u0003\u00e4r\u0000"+
		"\u06a7\u06a8\u0003\u00e8t\u0000\u06a8\u06aa\u0001\u0000\u0000\u0000\u06a9"+
		"\u06a1\u0001\u0000\u0000\u0000\u06a9\u06a6\u0001\u0000\u0000\u0000\u06aa"+
		"\u00e3\u0001\u0000\u0000\u0000\u06ab\u06ad\u0003\u00a4R\u0000\u06ac\u06ae"+
		"\u0003\u00eew\u0000\u06ad\u06ac\u0001\u0000\u0000\u0000\u06ad\u06ae\u0001"+
		"\u0000\u0000\u0000\u06ae\u06b6\u0001\u0000\u0000\u0000\u06af\u06b0\u0005"+
		"W\u0000\u0000\u06b0\u06b2\u0003\u00a4R\u0000\u06b1\u06b3\u0003\u00eew"+
		"\u0000\u06b2\u06b1\u0001\u0000\u0000\u0000\u06b2\u06b3\u0001\u0000\u0000"+
		"\u0000\u06b3\u06b5\u0001\u0000\u0000\u0000\u06b4\u06af\u0001\u0000\u0000"+
		"\u0000\u06b5\u06b8\u0001\u0000\u0000\u0000\u06b6\u06b4\u0001\u0000\u0000"+
		"\u0000\u06b6\u06b7\u0001\u0000\u0000\u0000\u06b7\u06bb\u0001\u0000\u0000"+
		"\u0000\u06b8\u06b6\u0001\u0000\u0000\u0000\u06b9\u06bb\u0003\u00f8|\u0000"+
		"\u06ba\u06ab\u0001\u0000\u0000\u0000\u06ba\u06b9\u0001\u0000\u0000\u0000"+
		"\u06bb\u00e5\u0001\u0000\u0000\u0000\u06bc\u06be\u0003\u00a4R\u0000\u06bd"+
		"\u06bf\u0003\u00f0x\u0000\u06be\u06bd\u0001\u0000\u0000\u0000\u06be\u06bf"+
		"\u0001\u0000\u0000\u0000\u06bf\u06c0\u0001\u0000\u0000\u0000\u06c0\u06c1"+
		"\u0003\u00eau\u0000\u06c1\u00e7\u0001\u0000\u0000\u0000\u06c2\u06c3\u0005"+
		"S\u0000\u0000\u06c3\u06c5\u0005T\u0000\u0000\u06c4\u06c2\u0001\u0000\u0000"+
		"\u0000\u06c5\u06c6\u0001\u0000\u0000\u0000\u06c6\u06c4\u0001\u0000\u0000"+
		"\u0000\u06c6\u06c7\u0001\u0000\u0000\u0000\u06c7\u06c8\u0001\u0000\u0000"+
		"\u0000\u06c8\u06d9\u0003R)\u0000\u06c9\u06ca\u0005S\u0000\u0000\u06ca"+
		"\u06cb\u0003\u00c6c\u0000\u06cb\u06cc\u0005T\u0000\u0000\u06cc\u06ce\u0001"+
		"\u0000\u0000\u0000\u06cd\u06c9\u0001\u0000\u0000\u0000\u06ce\u06cf\u0001"+
		"\u0000\u0000\u0000\u06cf\u06cd\u0001\u0000\u0000\u0000\u06cf\u06d0\u0001"+
		"\u0000\u0000\u0000\u06d0\u06d5\u0001\u0000\u0000\u0000\u06d1\u06d2\u0005"+
		"S\u0000\u0000\u06d2\u06d4\u0005T\u0000\u0000\u06d3\u06d1\u0001\u0000\u0000"+
		"\u0000\u06d4\u06d7\u0001\u0000\u0000\u0000\u06d5\u06d3\u0001\u0000\u0000"+
		"\u0000\u06d5\u06d6\u0001\u0000\u0000\u0000\u06d6\u06d9\u0001\u0000\u0000"+
		"\u0000\u06d7\u06d5\u0001\u0000\u0000\u0000\u06d8\u06c4\u0001\u0000\u0000"+
		"\u0000\u06d8\u06cd\u0001\u0000\u0000\u0000\u06d9\u00e9\u0001\u0000\u0000"+
		"\u0000\u06da\u06dc\u0003\u0100\u0080\u0000\u06db\u06dd\u0003\"\u0011\u0000"+
		"\u06dc\u06db\u0001\u0000\u0000\u0000\u06dc\u06dd\u0001\u0000\u0000\u0000"+
		"\u06dd\u00eb\u0001\u0000\u0000\u0000\u06de\u06df\u0003\u00f2y\u0000\u06df"+
		"\u06e0\u0003\u00fe\u007f\u0000\u06e0\u00ed\u0001\u0000\u0000\u0000\u06e1"+
		"\u06e2\u0005Z\u0000\u0000\u06e2\u06e5\u0005Y\u0000\u0000\u06e3\u06e5\u0003"+
		"\u00fa}\u0000\u06e4\u06e1\u0001\u0000\u0000\u0000\u06e4\u06e3\u0001\u0000"+
		"\u0000\u0000\u06e5\u00ef\u0001\u0000\u0000\u0000\u06e6\u06e7\u0005Z\u0000"+
		"\u0000\u06e7\u06ea\u0005Y\u0000\u0000\u06e8\u06ea\u0003\u00f2y\u0000\u06e9"+
		"\u06e6\u0001\u0000\u0000\u0000\u06e9\u06e8\u0001\u0000\u0000\u0000\u06ea"+
		"\u00f1\u0001\u0000\u0000\u0000\u06eb\u06ec\u0005Z\u0000\u0000\u06ec\u06ed"+
		"\u0003\u00f4z\u0000\u06ed\u06ee\u0005Y\u0000\u0000\u06ee\u00f3\u0001\u0000"+
		"\u0000\u0000\u06ef\u06f4\u0003\u00f6{\u0000\u06f0\u06f1\u0005V\u0000\u0000"+
		"\u06f1\u06f3\u0003\u00f6{\u0000\u06f2\u06f0\u0001\u0000\u0000\u0000\u06f3"+
		"\u06f6\u0001\u0000\u0000\u0000\u06f4\u06f2\u0001\u0000\u0000\u0000\u06f4"+
		"\u06f5\u0001\u0000\u0000\u0000\u06f5\u00f5\u0001\u0000\u0000\u0000\u06f6"+
		"\u06f4\u0001\u0000\u0000\u0000\u06f7\u06f9\u0003r9\u0000\u06f8\u06f7\u0001"+
		"\u0000\u0000\u0000\u06f9\u06fc\u0001\u0000\u0000\u0000\u06fa\u06f8\u0001"+
		"\u0000\u0000\u0000\u06fa\u06fb\u0001\u0000\u0000\u0000\u06fb\u06ff\u0001"+
		"\u0000\u0000\u0000\u06fc\u06fa\u0001\u0000\u0000\u0000\u06fd\u0700\u0003"+
		"\u00e0p\u0000\u06fe\u0700\u0003\u00f8|\u0000\u06ff\u06fd\u0001\u0000\u0000"+
		"\u0000\u06ff\u06fe\u0001\u0000\u0000\u0000\u0700\u070b\u0001\u0000\u0000"+
		"\u0000\u0701\u0703\u0003r9\u0000\u0702\u0701\u0001\u0000\u0000\u0000\u0703"+
		"\u0706\u0001\u0000\u0000\u0000\u0704\u0702\u0001\u0000\u0000\u0000\u0704"+
		"\u0705\u0001\u0000\u0000\u0000\u0705\u0707\u0001\u0000\u0000\u0000\u0706"+
		"\u0704\u0001\u0000\u0000\u0000\u0707\u0708\u0005S\u0000\u0000\u0708\u070a"+
		"\u0005T\u0000\u0000\u0709\u0704\u0001\u0000\u0000\u0000\u070a\u070d\u0001"+
		"\u0000\u0000\u0000\u070b\u0709\u0001\u0000\u0000\u0000\u070b\u070c\u0001"+
		"\u0000\u0000\u0000\u070c\u00f7\u0001\u0000\u0000\u0000\u070d\u070b\u0001"+
		"\u0000\u0000\u0000\u070e\u070f\u0007\u000e\u0000\u0000\u070f\u00f9\u0001"+
		"\u0000\u0000\u0000\u0710\u0711\u0005Z\u0000\u0000\u0711\u0716\u0003X,"+
		"\u0000\u0712\u0713\u0005V\u0000\u0000\u0713\u0715\u0003X,\u0000\u0714"+
		"\u0712\u0001\u0000\u0000\u0000\u0715\u0718\u0001\u0000\u0000\u0000\u0716"+
		"\u0714\u0001\u0000\u0000\u0000\u0716\u0717\u0001\u0000\u0000\u0000\u0717"+
		"\u0719\u0001\u0000\u0000\u0000\u0718\u0716\u0001\u0000\u0000\u0000\u0719"+
		"\u071a\u0005Y\u0000\u0000\u071a\u00fb\u0001\u0000\u0000\u0000\u071b\u0725"+
		"\u0003\u0100\u0080\u0000\u071c\u071e\u0005W\u0000\u0000\u071d\u071f\u0003"+
		"\u00fa}\u0000\u071e\u071d\u0001\u0000\u0000\u0000\u071e\u071f\u0001\u0000"+
		"\u0000\u0000\u071f\u0720\u0001\u0000\u0000\u0000\u0720\u0722\u0003\u00a4"+
		"R\u0000\u0721\u0723\u0003\u0100\u0080\u0000\u0722\u0721\u0001\u0000\u0000"+
		"\u0000\u0722\u0723\u0001\u0000\u0000\u0000\u0723\u0725\u0001\u0000\u0000"+
		"\u0000\u0724\u071b\u0001\u0000\u0000\u0000\u0724\u071c\u0001\u0000\u0000"+
		"\u0000\u0725\u00fd\u0001\u0000\u0000\u0000\u0726\u0727\u00052\u0000\u0000"+
		"\u0727\u072c\u0003\u00fc~\u0000\u0728\u0729\u0003\u00a4R\u0000\u0729\u072a"+
		"\u0003\u0100\u0080\u0000\u072a\u072c\u0001\u0000\u0000\u0000\u072b\u0726"+
		"\u0001\u0000\u0000\u0000\u072b\u0728\u0001\u0000\u0000\u0000\u072c\u00ff"+
		"\u0001\u0000\u0000\u0000\u072d\u072f\u0005O\u0000\u0000\u072e\u0730\u0003"+
		"\u00c2a\u0000\u072f\u072e\u0001\u0000\u0000\u0000\u072f\u0730\u0001\u0000"+
		"\u0000\u0000\u0730\u0731\u0001\u0000\u0000\u0000\u0731\u0732\u0005P\u0000"+
		"\u0000\u0732\u0101\u0001\u0000\u0000\u0000\u00e7\u0103\u0107\u0109\u010e"+
		"\u0110\u0117\u011c\u0124\u012d\u0132\u0139\u0141\u0148\u0154\u0158\u015d"+
		"\u0161\u0165\u0169\u0173\u017b\u0183\u0187\u018e\u0195\u0199\u019c\u019f"+
		"\u01a8\u01ae\u01b3\u01b6\u01bc\u01c2\u01c6\u01ca\u01d2\u01db\u01e2\u01e8"+
		"\u01ec\u01f8\u0201\u0206\u020c\u0210\u021c\u0223\u0230\u0235\u023f\u0247"+
		"\u0251\u025a\u0265\u026a\u0273\u027d\u0282\u028b\u0291\u0298\u029d\u02a5"+
		"\u02a9\u02ab\u02b4\u02b7\u02bb\u02bf\u02c5\u02ca\u02ce\u02d6\u02dd\u02e3"+
		"\u02e5\u02ec\u02f2\u02f8\u02fb\u0305\u030f\u0315\u031c\u0320\u0329\u032f"+
		"\u033a\u0344\u034f\u0359\u0361\u0364\u036e\u0378\u037b\u037e\u0381\u0386"+
		"\u038e\u0391\u0394\u03a1\u03a9\u03ae\u03b6\u03ba\u03be\u03c2\u03c6\u03c8"+
		"\u03cc\u03d2\u03dc\u03e0\u03e8\u03f1\u03ff\u0402\u040e\u0411\u0421\u0426"+
		"\u042d\u0432\u0438\u0441\u0449\u0450\u0454\u045b\u045d\u0466\u0470\u0475"+
		"\u0480\u0489\u0490\u0497\u04a2\u04bd\u04c0\u04c3\u04cb\u04cf\u04d9\u04df"+
		"\u04ec\u04f5\u04fa\u0507\u050d\u0514\u0521\u052a\u0533\u0539\u0541\u0547"+
		"\u054e\u0553\u055b\u055e\u0562\u0566\u056a\u056c\u0570\u0575\u057a\u0585"+
		"\u058b\u0595\u0599\u059e\u05a9\u05b1\u05ba\u05ca\u05f7\u05fd\u0602\u060b"+
		"\u060d\u060f\u0615\u061c\u0624\u0628\u062f\u063b\u0644\u064b\u064e\u0652"+
		"\u0664\u0666\u0670\u067a\u0681\u0685\u0687\u068e\u0699\u069c\u06a1\u06a9"+
		"\u06ad\u06b2\u06b6\u06ba\u06be\u06c6\u06cf\u06d5\u06d8\u06dc\u06e4\u06e9"+
		"\u06f4\u06fa\u06ff\u0704\u070b\u0716\u071e\u0722\u0724\u072b\u072f";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}