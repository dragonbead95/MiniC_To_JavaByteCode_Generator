package listener.main;

import java.util.Hashtable;

import generated.MiniCParser;
import generated.MiniCParser.ExprContext;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.If_stmtContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;
import listener.main.SymbolTable;
import listener.main.SymbolTable.VarInfo;

public class BytecodeGenListenerHelper {
	
	// <boolean functions>
	// 메서드인지 확인하는 메서드입니다.
	static boolean isFunDecl(MiniCParser.ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof MiniCParser.Fun_declContext;
	}
	
	// type_spec IDENT '[' ']'
	// 배열매개변수인지 확인하는 메서드입니다.
	static boolean isArrayParamDecl(ParamContext param) {
		return param.getChildCount() == 4;
	}
	
	// global vars
	// 전역변수를 초기화하는 메서드입니다.
	static int initVal(Var_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	// var_decl	: type_spec IDENT '=' LITERAL ';
	// 변수를 선언 및 초기화하는 문장인지를 검사하는 메서드입니다.
	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}

	// var_decl	: type_spec IDENT '=' LITERAL ';
	// 변수를 선언 및 초기화하는 문장인지를 검사하는 메서드입니다.
	static boolean isDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 3 ;
	}
	// var_decl	: type_spec IDENT '[' LITERAL ']' ';'
	// 배열인지 검사하는 메서드입니다.
	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// <local vars>
	// local_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static int initVal(Local_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	//배열인지 확인하는 메서드입니다.
	static boolean isArrayDecl(Local_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// 변수를 선언 및 초기화하는 문장인지를 검사하는 메서드입니다.
	static boolean isDeclWithInit(Local_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}

	// 리턴타입이 void, int인지 검사하는 메서드입니다.
	static boolean isVoidF(Fun_declContext ctx) {
			// <Fill in>
		if(getTypeText((Type_specContext) ctx.getChild(0)).equals("void"))
		{
			return true;
		}else {
			return false;
		}
	}

	// 리턴을 할때 int 변수를 리턴하는지 검사하는 메서드입니다. ex) return a;
	static boolean isIntReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() ==3;
	}

	//리턴타입이 void인 경우인지를 검사하는 메서드입니다. ex) return;
	static boolean isVoidReturn(MiniCParser.Return_stmtContext ctx) {
		return ctx.getChildCount() == 2;
	}
	
	// <information extraction>
	static String getStackSize(Fun_declContext ctx) {
		return "32";
	}		//스택사이즈 반환
	static String getLocalVarSize(Fun_declContext ctx) {
		return "32";
	}		//지역변수 사이즈 반환
	static String getTypeText(Type_specContext typespec) {	//변수타입 텍스트 반환
			// <Fill in>
		return typespec.getText();
	}

	// params
	//매개변수의 변수명을 반환합니다.
	static String getParamName(ParamContext param) {
		// <Fill in>
		return param.IDENT().getText();
	}

	//매개변수의 타입 텍스트를 반환합니다.
	static String getParamTypesText(ParamsContext params) {
		String typeText = "";
		
		for(int i = 0; i < params.param().size(); i++) {
			MiniCParser.Type_specContext typespec = (MiniCParser.Type_specContext)  params.param(i).getChild(0);
			typeText += getTypeText(typespec); // + ";";
		}
		return typeText;
	}

	//지역변수 이름을 반환하는 메서드입니다.
	static String getLocalVarName(Local_declContext local_decl) {
		// <Fill in>
		return local_decl.getChild(1).getText();	//ex) 변수는 int i; 이러한 꼴로 시작하므로 1번째 자식노드를 반환한다.
	}

	//메서드이름을 가져온다.
	static String getFunName(Fun_declContext ctx) {
		// <Fill in>
		return ctx.getChild(1).getText();	//메서드는 int main(...꼴로 시작하므로 1번째 자식노드를 반환한다.
	}

	//메서드이름을 가져온다.
	//메서드를 호출을 할때 0번째 자식노드를 가져온다.
	static String getFunName(ExprContext ctx) {
		// <Fill in>
		String fname = ctx.getChild(0).getText();
		return fname;
	}

	//if문에서 if문다음에 else가 있는지 검사한다.
	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() <= 5;
	}

	//Test클래스의 도입부문을 문자열에 저장하고 반환합니다.
	static String getFunProlog(String var_decl) {
		// return ".class public Test .....
		// ...
		// invokenonvirtual java/lang/Object/<init>()
		// return
		// .end method"
        String str = ".class public Test" + "\n" +
                        ".super java/lang/Object" + "\n" +
						var_decl +
                        ".method public <init>()V" + "\n" + "\t" +
                        "aload_0" + "\n" + "\t" +
                        "invokenonvirtual java/lang/Object/<init>()V" + "\n" + "\t" +
                        "return" + "\n" +
                        ".end method" + "\n";
		return str;
	}

	//클래스이름을 반환합니다.
	static String getCurrentClassName() {
		return "Test";
	}
}
