package listener.main;

import java.util.Hashtable;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.ProgramContext;
import generated.MiniCParser.StmtContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;

import static listener.main.BytecodeGenListenerHelper.*;
import static listener.main.SymbolTable.*;

public class BytecodeGenListener extends MiniCBaseListener implements ParseTreeListener {
	ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
	SymbolTable symbolTable = new SymbolTable();
	
	int tab = 0;
	int label = 0;
	
	// program	: decl+
	//메서드를 들어갈때 실행되는 메서드입니다.
	@Override
	public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
		symbolTable.initFunDecl();		//심볼테이블을 초기화합니다.
		
		String fname = getFunName(ctx);	//메서드이름을 저장합니다.
		ParamsContext params;

		if (fname.equals("main")) {//메서드의 이름이 main인지를 검사합니다.
			symbolTable.putLocalVar("args", Type.INTARRAY);	//매개변수를 심볼테이플에 배열타입으로 넣습니다.
		} else {//메서드의 이름이 main이 아닌 경우입니다.
			symbolTable.putFunSpecStr(ctx);								//해당 메소드를 함수심볼테이블에 넣습니다.
			params = (MiniCParser.ParamsContext) ctx.getChild(3);	//매개변수 노드들을 임시변수에 넣습니다.
			symbolTable.putParams(params);								//매개변수들을 지역심볼테이블에 넣습니다.
		}
		System.out.println("enterFun_decl");
	}

	
	// var_decl	: type_spec IDENT ';' | type_spec IDENT '=' LITERAL ';'|type_spec IDENT '[' LITERAL ']' ';'
	@Override
	public void enterVar_decl(MiniCParser.Var_declContext ctx) {
		String varName = ctx.IDENT().getText();
		
		if (isArrayDecl(ctx)) {	//전역변수 배열인경우
			symbolTable.putGlobalVar(varName, Type.INTARRAY);
		}
		else if (isDeclWithInit(ctx)) {	//전역변수 초기값 선언 형식
			symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal(ctx));

		}
		else  { // simple decl 전역 일반변수선언
			symbolTable.putGlobalVar(varName, Type.INT);
		}

        System.out.println("enterVar_decl");
	}

	
	@Override
	public void enterLocal_decl(MiniCParser.Local_declContext ctx) {			
		if (isArrayDecl(ctx)) {	//지역 배열변수 선언인경우
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INTARRAY);
		}
		else if (isDeclWithInit(ctx)) {	//지역 변수 초기값 선언인 경우
			symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));	
		}
		else  { // simple decl	//지역 일반 변수 선언인 경우
			symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
		}

        System.out.println("enterLocal_decl");
	}

	
	@Override
	public void exitProgram(MiniCParser.ProgramContext ctx) {

		
		String fun_decl = "", var_decl = "";

		for(int i = 0; i < ctx.getChildCount(); i++) {
			if(isFunDecl(ctx, i))	//노드가 메소드이면 newText맵에서 불러와서 문자열에 저장한다.
				fun_decl += newTexts.get(ctx.decl(i));
			else					//노드가 변수라면 newText맵에서 불러와서 문자열에 저장한다.
				var_decl += newTexts.get(ctx.decl(i));
		}
		String classProlog = getFunProlog(var_decl);

		//완성된 bytecode들을 ctx를 키값으로 테이블에 넣는다.
		newTexts.put(ctx, classProlog+ fun_decl);

		System.out.println(newTexts.get(ctx));	//완성된 bytecode를 출력한다.
        System.out.println("exitProgram");
	}	
	
	
	// decl	: var_decl | fun_decl
	@Override
	public void exitDecl(MiniCParser.DeclContext ctx) {
		String decl = "";
		if(ctx.getChildCount() == 1)	//자식노드가 하나인 경우
		{
			if(ctx.var_decl() != null)				//var_decl
				decl += newTexts.get(ctx.var_decl());	//newTexts에서 불러와서 문자열에 저장한다.
			else							//fun_decl
				decl += newTexts.get(ctx.fun_decl());	//newText에서 불러와서 문자열에 저장한다.
		}
		newTexts.put(ctx, decl);						//상위 노드에 자식노드들의 문자열을 저장한다.
        System.out.println("exitDecl");
	}
	
	// stmt	: expr_stmt | compound_stmt | if_stmt | while_stmt | return_stmt
	@Override
	public void exitStmt(MiniCParser.StmtContext ctx) {
		String stmt = "";
		if(ctx.getChildCount() > 0)
		{
			if(ctx.expr_stmt() != null)				// expr_stmt
				stmt += newTexts.get(ctx.expr_stmt());
			else if(ctx.compound_stmt() != null)	// compound_stmt
				stmt += newTexts.get(ctx.compound_stmt());
			// <(0) Fill here>
			else if(ctx.if_stmt() != null)			// if_stmt
				stmt += newTexts.get(ctx.if_stmt());
			else if(ctx.while_stmt() != null)		// while_stmt
				stmt += newTexts.get(ctx.while_stmt());
			else if(ctx.return_stmt() != null)		//return_stmt
				stmt += newTexts.get(ctx.return_stmt());
		}
		newTexts.put(ctx, stmt);
        System.out.println("exitStmt");
	}
	
	// expr_stmt	: expr ';'
	@Override
	public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		String stmt = "";
		if(ctx.getChildCount() == 2)	// expr ; 인지를 검사한다.
		{
			stmt += newTexts.get(ctx.expr());	// expr
		}
		newTexts.put(ctx, stmt);	//수식을 상위의 노드에 저장한다.
        System.out.println("exitExpr_stmt");
	}
	
	
	// while_stmt	: WHILE '(' expr ')' stmt
	@Override
	public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
		// <(1) Fill here!>
		String l2 = symbolTable.newLabel();		//label을 새로 만들어 저장한다.
		String lend = symbolTable.newLabel();	//label을 새로 만들어 저장한다.
        String str = "";
		str +=  l2 + ": " + "\n"
				+ newTexts.get(ctx.expr()) + "\n"
				+ "ifeq " + lend + "\n"
				+ newTexts.get(ctx.stmt()) + "\n"
				+ "goto " + l2 + "\n"
				+ lend + ":" + "\n";


        newTexts.put(ctx,str);						//반복문 bytecode 문자열을 현재 노드에 저장합니다.
        System.out.println("exitWhile_stmt");
	}
	
	
	@Override
	public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
			// <(2) Fill here!>
        String str = funcHeader(ctx,getFunName(ctx));	//funcHeader메서드를 통하여 bytecode에서 필수로 넣어줘야할
														// bytecode를 가공하여 문자열에 저장합니다.

        str+= newTexts.get(ctx.compound_stmt());		//compound_stmt에 해당하는 bytecode를 문자열에 저장합니다.

        if(isVoidF(ctx))								//메서드의 리턴타입이 void인지를 검사합니다.
		{
			str += "return" + "\n";						//void인 경우 마지막 문자열에 return을 넣어줍니다.
		}

        str+= ".end method" + "\n";						//bytecode 메서드 마지막에 end method를 넣어줍니다.
        newTexts.put(ctx,str);
        System.out.println("exitFun_decl");
	}
	

	private String funcHeader(MiniCParser.Fun_declContext ctx, String fname) {
        System.out.println("funcHeader");
		return ".method public static " + symbolTable.getFunSpecStr(fname) + "\n"	//매개변수로 fname을 받아 메소드의 이름을 만들어줍니다.
				+ "\t" + ".limit stack " 	+ getStackSize(ctx) + "\n"				//스택의 사이즈를 반환받습니다.
				+ "\t" + ".limit locals " 	+ getLocalVarSize(ctx) + "\n";			//지역변수의 사이즈를 반환받습니다.

	}
	
	
	
	@Override
	public void exitVar_decl(MiniCParser.Var_declContext ctx) {
		String varName = ctx.IDENT().getText();	//변수이름을 반환받아 저장합니다.
		String varDecl = "";

		if (isDeclWithInit(ctx)) {		//해당 노드가 변수 선언 및 초기화 꼴인지를 검사합니다.
			varDecl += ".field public static " + varName + " I" + "\n";
			// v. initialization => Later! skip now..:

		}else if(isDecl(ctx))
		{
			varDecl += ".field public static " + varName + " I" + "\n";
		}

		newTexts.put(ctx, varDecl);
        System.out.println("exitVar_decl");
	}
	
	
	@Override
	public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
		String varDecl = "";

		if (isDeclWithInit(ctx)) {		//변수 선언 및 초기화 꼴인지 검사합니다.
			String vId = symbolTable.getVarId(ctx);	//심볼테이블에서 변수아이디를 불러옵니다.
			varDecl += "ldc " + ctx.LITERAL().getText() + "\n"	//ctx노드에서 상수를 문자열로 변환하여 불러옵니다.
					+ "istore_" + vId + "\n"; 					//지역변수에 저장하는 bytecode를 작성합니다.
		}else if(isArrayDecl(ctx)){
		    String vId = symbolTable.getVarId(ctx);
		    varDecl += "bipush " + ctx.LITERAL().getText() + "\n"
                    + "newarray int" + "\n"
                    + "istore_" + vId + "\n";
        }

		newTexts.put(ctx, varDecl);
        System.out.println("exitLocal_decl");
	}

	
	// compound_stmt	: '{' local_decl* stmt* '}'
	@Override
	public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		// <(3) Fill here>
        String str="";
        for(int i=0;i<ctx.getChildCount();i++)              //Compound_stmt 노드의 자식노드들을 반복문을 진행하며 참조합니다.
		{
			if(ctx.getChild(i).getText().equals("{") ||     //자식 노드중 "{", "}"이면 다음 자식노드로 잔행합니다.
			ctx.getChild(i).getText().equals("}"))
			{
				continue;
			}else{                                          //그 이외의 자식 노드는 맵에서 불러와서 문자열에 저장합니다.
				str += newTexts.get(ctx.getChild(i));
			}
		}
        newTexts.put(ctx,str);
        System.out.println("exitCompound_stmt");
	}

	// if_stmt	: IF '(' expr ')' stmt | IF '(' expr ')' stmt ELSE stmt;
	@Override
	public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
		String stmt = "";
		String condExpr= newTexts.get(ctx.expr());          //if문의 bytecode 조건식을 문자열에 저장합니다.
		String thenStmt = newTexts.get(ctx.stmt(0));        //if문의 참인 내용의 bytecode 문자열을 저장합니다.

		String lend = symbolTable.newLabel();               //if문의 탈출 lbael입니다.
		String lelse = symbolTable.newLabel();              //if문의 else label입니다.

		if(noElse(ctx)) {                                   // ctx노드가 else문이 없는 경우
			stmt += condExpr + "\n"							//if문에서 bytecode로 작성한 조건식에 사용되는 수식입니다.
				+ "ifeq " + lend + "\n"                     //조건의 값이 참인경우 lend label로 이동합니다.
				+ thenStmt + "\n"                           //ifeq가 거짓인경우 thenStmt을 실행합니다.
				+ lend + ":"  + "\n";	                    //lend label의 시작부분입니다.
		}
		else {                                              //if문에서 else도 있는 경우입니다.
			String elseStmt = newTexts.get(ctx.stmt(1));	//else인 경우의 내용을 문자열에 넣는다.
			stmt += condExpr + "\n"							//if문에서 bytecode로 작성한 조건식에 사용되는 수식입니다.
					+ "ifeq " + lelse + "\n"				//ifeq에서 참이면 else 쪽으로 이동합니다.
					+ thenStmt + "\n"						//ifeq에서 거짓이면 참인부분을 수행합니다.
					+ "goto " + lend + "\n"					//조건문을 탈출합니다.
					+ lelse + ": " + "\n"					//else 내용이 시작되는 label입니다.
					+ elseStmt + "\n"						//else의 내용입니다.
					+ lend + ":"  + "\n";					//조건문을 탈출하는 label입니다.
		}
		
		newTexts.put(ctx, stmt);
        System.out.println("exitIf_stmt");
	}
	
	
	// return_stmt	: RETURN ';' | RETURN expr ';'
	@Override
	public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
			// <(4) Fill here>
        String temp="";
		if(isIntReturn(ctx))			//return 1 or return x 이런꼴인 경우입니다.
		{
			temp = newTexts.get(ctx.expr()) + "\n"
					+ "ireturn" + "\n";
		}else if(isVoidReturn(ctx))				//메서드의 리턴타입이 없는 경우입니다.(void)
		{
			temp = symbolTable.getVarId(ctx.getChild(0).getText());	//bytecode의 함수 마지막에 return을 붙여줍니다.
		}
		newTexts.put(ctx,temp);
        System.out.println("exitReturn_stmt");
	}

	
	@Override
	public void exitExpr(MiniCParser.ExprContext ctx) {
		String expr = "";

		if(ctx.getChildCount() <= 0) {
			newTexts.put(ctx, ""); 
			return;
		}		
		
		if(ctx.getChildCount() == 1) { // IDENT | LITERAL
			if(ctx.IDENT() != null) {
				String idName = ctx.IDENT().getText();	//변수명을 저장합니다.
				if(symbolTable.getVarType(idName) == Type.INT) {				//변수가 int타입인지 검사합니다.
					expr += "iload_" + symbolTable.getVarId(idName) + " \n";	//해당 변수의 아이디번호를 호출하여 문자열에 저장합니다.
				}
				//else	// Type int array => Later! skip now..
				//	expr += "           lda " + symbolTable.get(ctx.IDENT().getText()).value + " \n";
			} else if (ctx.LITERAL() != null) {	//상수인지 검사합니다.
					String literalStr = ctx.LITERAL().getText();				//상수를 문자열화하여 저장합니다.
					expr += "ldc " + literalStr + " \n";						//ldc [숫자] 꼴로 저장합니다. "ldc "(원래소스)
				}
		}
		else if(ctx.getChildCount() == 2) { // UnaryOperation
			expr = handleUnaryExpr(ctx,expr);		// ++a, --a 같은 꼴인 경우입니다.

		}
		else if(ctx.getChildCount() == 3) {	 
			if(ctx.getChild(0).getText().equals("(")) { 		// '(' expr ')'
				expr = newTexts.get(ctx.expr(0));	//맵에서 키값으로 값을 불러와서 저장합니다.
				
			} else if(ctx.getChild(1).getText().equals("=")) { 	// IDENT '=' expr
				expr = newTexts.get(ctx.expr(0))
						+ "istore_" + symbolTable.getVarId(ctx.IDENT().getText()) + " \n"; //expr에 대한 값이나 변수를 불러와서 변수에 저장합니다.
				
			} else { 											// binary operation
				expr = handleBinExpr(ctx, expr);	// a+b, a*b 같은 꼴인경우를 bytecode로 변환하여 저장합니다.
				
			}
		}
		// IDENT '(' args ')' |  IDENT '[' expr ']'
		else if(ctx.getChildCount() == 4) {
			if(ctx.args() != null){		// function calls
				expr = handleFunCall(ctx, expr); //메서드 호출을 bytecode로 변환하여 저장한다.
			} else { // expr
				// Arrays: TODO
				expr += "iload_" + symbolTable.getVarId(ctx.IDENT().getText()) + "\n"
						+ "iload_" + symbolTable.getVarId(ctx.expr(0).getText()) + "\n"
						+ "iaload" + "\n";
			}
		}
		// IDENT '[' expr ']' '=' expr
		else { // Arrays: TODO			*/
		    expr += "iload_" + symbolTable.getVarId(ctx.IDENT().getText()) + "\n"
                    + newTexts.get(ctx.getChild(2))
                    + newTexts.get(ctx.getChild(5))
                    + "iastore" + "\n";
		}

		newTexts.put(ctx, expr);
        System.out.println("exitExpr");
	}


	private String handleUnaryExpr(MiniCParser.ExprContext ctx, String expr) {
		String l1 = symbolTable.newLabel();
		String l2 = symbolTable.newLabel();
		String lend = symbolTable.newLabel();
		
		expr += newTexts.get(ctx.expr(0));	// 수식을 bytecode로 변환하여 문자열에 저장한다.
        String str_number = symbolTable.getVarId(ctx.getChild(1).getText());  //변수의 아이디를 찾는다.

		switch(ctx.getChild(0).getText()) {	//ctx의 0번째 노도를 참조하여 어떤 UnaryOperation인지 검사한다.
		case "-":
			expr += "           ineg \n"; break;	//bytecode로 ineg를 넣어 반대로 만든다.
		case "--":
			expr += "ldc 1" + "\n"					// 상수 1을 만들고 불러온 변수와 1을 isub명령어로 뺀다.
					+ "isub" + "\n"
					+ "istore_" + str_number + "\n";
			break;
		case "++":
			expr += "ldc 1" + "\n"
					+ "iadd" + "\n"
                    + "istore_"+str_number + "\n";
			break;
		case "!":
			expr += "ifeq " + l2 + "\n"				//ifeq의 값이 참이면 l2 label로 이동한다.
					+ l1 + ": " + "ldc 0" + "\n"	//ifeq의 값이 거짓이면 상수 0을 만든다.
					+ "goto " + lend + "\n"			//lend label로 이동하여 빠져나간다.
					+ l2 + ": " + "ldc 1" + "\n"	//ifeq의 값이 참인 경우 상수 1을 만든다.
					+ lend + ": " + "\n";
			break;
		}
        System.out.println("handleUnaryExpr");
		return expr;
	}


	private String handleBinExpr(MiniCParser.ExprContext ctx, String expr) {
		String l2 = symbolTable.newLabel();		//label을 새로 만든다.
		String lend = symbolTable.newLabel();

		//binary operation을 다루기 위해 변수 두개를 호출하여 저장한다.
		expr += newTexts.get(ctx.expr(0));
		expr += newTexts.get(ctx.expr(1));
		//어떤 연산장인지 검사한다.
		switch (ctx.getChild(1).getText()) {
			case "*":
				expr += "imul \n"; break;
			case "/":
				expr += "idiv \n"; break;
			case "%":
				expr += "irem \n"; break;
			case "+":		// expr(0) expr(1) iadd
				expr += "iadd \n"; break;
			case "-":
				expr += "isub \n"; break;

				//피연산자 두개를 불러와 뺀다. 그 뺀값이 1보다 크거나 같으면 참값입니다.
			case "==":
				expr += "if_cmpne " + "\n"
						+ "ldc 0" + "\n"			//==연산자의 결과가 거짓인 경우
						+ "goto " + lend + "\n"
						+ l2 + ": " + "\n"
						+ "ldc 1" + "\n"			//==연산자의 결과가 참인 경우
						+ lend + ": " + "\n";
				break;
			case "!=":
				expr += "if_icmpeq " + l2 + "\n"
						+ "ldc 0" + "\n"			//같은 경우 거짓인 경우입니다.
						+ "goto " + lend + "\n"
						+ l2 + ": " + "\n"
						+ "ldc 1" + "\n"			//같지 않을때 참인 경우입니다.
						+ lend + ": " + "\n";
				break;
			case "<=":
				// <(5) Fill here>
                expr += "if_icmpgt " + l2 + "\n"
						+ "ldc 1" + "\n"			// 상수 1을 만들어 <=의 결과가 참인경우입니다.
						+ "goto " + lend + "\n"		// 수식을 빠져나갑니다.
						+ l2 + ": " + "\n"
						+ "ldc 0" + "\n"			// 상수 0을 만들어 <=의 결과가 거짓입니다.
						+ lend + ": " + "\n";
				break;
			case "<":
				// <(6) Fill here>
				expr += "if_icmpge " + l2 + "\n"
						+ "ldc 1" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "\n"
						+ "ldc 0" + "\n"
						+ lend + ": " + "\n";
				break;

			case ">=":
				// <(7) Fill here>
				expr += "if_icmplt" + l2 + "\n"
						+ "ldc 1" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "\n"
						+ "ldc 0" + "\n"
						+ lend + ": " + "\n";
				break;

			case ">":
				// <(8) Fill here>
				expr += "if_icmple" + l2 + "\n"
						+ "ldc 1" + "\n"
						+ "goto " + lend + "\n"
						+ l2 + ": " + "\n"
						+ "ldc 0" + "\n"
						+ lend + ": " + "\n";
				break;

			case "and":
				expr +=  "ifne "+ lend + "\n"				//2개의 피연산자가 동일하지 않으면 lend lable로 이동합니다.
						+ "pop" + "\n" + "ldc 0" + "\n"		//ifne의 결과가 거짓이면 스택에서 top을 하나 제거하고 상수 0을 저장한다.
						+ lend + ": " + "\n"; break;
			case "or":
				// <(9) Fill here>
				expr += "ifeq " + lend + "\n"				//2개의 피연산자가 동일하면 lend lable로 이동합니다.
						+ "pop" + "\n" + "ldc 0" + "\n"		//ifeq의 결과가 거짓이면 스택에서 top을 하나 제거하고 상수 0을 저장한다.
						+ lend + ": " + "\n";
				break;

		}
        System.out.println("handleBinExpr");
		return expr;
	}
	private String handleFunCall(MiniCParser.ExprContext ctx, String expr) {
		String fname = getFunName(ctx);
		if (fname.equals("_print")) {		// System.out.println	
			expr = "getstatic java/lang/System/out Ljava/io/PrintStream; " + "\n"
			  		+ newTexts.get(ctx.args()) 
			  		+ "invokevirtual " + symbolTable.getFunSpecStr("_print") + "\n";
		} else {	//그 이외의 메소드인경우
			expr = newTexts.get(ctx.args()) 
					+ "invokestatic " + getCurrentClassName()+ "/" + symbolTable.getFunSpecStr(fname) + "\n";
		}
        System.out.println("handleFunCall");
		return expr;
			
	}

	// args	: expr (',' expr)* | ;
	@Override
	public void exitArgs(MiniCParser.ArgsContext ctx) {

		String argsStr = "\n";
		
		for (int i=0; i < ctx.expr().size() ; i++) {	//노드의 expr을 계속 참조합니다.
			argsStr += newTexts.get(ctx.expr(i)) ; 		//argsStr 문자열 변수에 bytecode 수식을 저장합니다.
		}
		newTexts.put(ctx, argsStr);
        System.out.println("exitArgs");
	}

}
