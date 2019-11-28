package listener.main;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.sun.jdi.IntegerType;
import generated.MiniCParser;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.ParamsContext;
import generated.MiniCParser.Type_specContext;
import generated.MiniCParser.Var_declContext;
import listener.main.SymbolTable.Type;
import org.antlr.v4.runtime.tree.ParseTree;

import static listener.main.BytecodeGenListenerHelper.*;


public class SymbolTable {
	enum Type {	//정수, 정수의 배열, 공백, 에러를 0~3순으로 지정
		INT, INTARRAY, VOID, ERROR
	}
	
	static public class VarInfo {
		Type type; 		//변수의 타입
		int id;			//변수의 번호
		int initVal;	//변수 값
		
		public VarInfo(Type type,  int id, int initVal) {
			this.type = type;
			this.id = id;
			this.initVal = initVal;
		}
		public VarInfo(Type type,  int id) {
			this.type = type;
			this.id = id;
			this.initVal = 0;
		}
	}

	//함수 정보 클래스
	static public class FInfo {
		public String sigStr;
	}
	
	private Map<String, VarInfo> _lsymtable = new HashMap<>();	// local v.
	private Map<String, VarInfo> _gsymtable = new HashMap<>();	// global v.
	private Map<String, FInfo> _fsymtable = new HashMap<>();	// function 
	
		
	private int _globalVarID = 0;	//전역변수에 대한 아이디
	private int _localVarID = 0;	//지역변수에 대한 아이디
	private int _labelID = 0;		//레이블에 대한 아이디
	private int _tempVarID = 0;		//임시변수에 대한 아이디
	
	SymbolTable(){
		initFunDecl();
		initFunTable();
	}
	
	void initFunDecl(){		// at each func decl
		_lsymtable.clear();	//지역심볼테이블 초기화
		_localVarID = 0;
		_labelID = 0;
		_tempVarID = 32;		
	}
	//지역변수를 지역심볼테이블에 넣어줍니다.
	void putLocalVar(String varname, Type type){
		//<Fill here>
		VarInfo vInfo = new VarInfo(type,_localVarID++);	//생성자로 변수의 타입과 지역변수를 구분하는 아이디를 넣어줍니다.
		_lsymtable.put(varname,vInfo);						//변수이름을 키값으로 지역심볼 테이블에 넣어줍니다.
	}

	//글로벌변수를 심볼 테이블에 넣어줌.
	void putGlobalVar(String varname, Type type){
		//<Fill here>
		VarInfo vInfo = new VarInfo(type,_globalVarID++);	//생성자로 변수의 타입과 전역변수를 구분하는 아이디를 넣어줍니다.
		_gsymtable.put(varname,vInfo);						//변수이름을 키값으로 전역심볼 테이블에 넣어줍니다.
	}

	//값이 있는 지역변수를 심볼 테이블에 넣어줌.
	void putLocalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>
		VarInfo vInfo = new VarInfo(type,_localVarID++,initVar);	//생성자로 변수의 타입과 지역변수 아이디, 초기값을 넣어줍니다.
		_lsymtable.put(varname,vInfo);								//변수이름을 키값으로 지역심볼 테이블에 넣어줍니다.
	}
	//값이 있는 전역변수를 심볼 테이블에 넣어줌.
	void putGlobalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>
		VarInfo vInfo = new VarInfo(type,_globalVarID++,initVar);	//생성자로 변수의 타입과 전역변수 아이디, 초기값을 넣어줍니다.
		_gsymtable.put(varname,vInfo);								//변수이름을 키값으로 전역심볼 테이블에 넣어줍니다.
	}

	//매개변수를 지역심볼 테이블에 넣어줍니다.
	void putParams(MiniCParser.ParamsContext params) {
		VarInfo vInfo;
		for(int i = 0; i < params.param().size(); i++) {	//매개변수의 갯수만큼 반복합니다.
		//<Fill here>
			if(params.param(i).type_spec().getText().equals("int"))	//param 파서트리의 i번째에서 타입의 문자열이 int인지 확인합니다.
			{
				vInfo = new VarInfo(Type.INT,_localVarID++);	//int가 맞다면 생성자로 int와 지역변수 아이디를 넣어줍니다.
				String lname =params.param(i).IDENT().getText();	//매개변수의 이름을 추출합니다.
				_lsymtable.put(lname,vInfo);						//매개변수의 이름을 키값으로 변수정보를 지역심볼테이블에 저장합니다.
			}

		}
	}

	//메소드들을 메서드 심볼테이블에 넣어주는 메소드
	private void initFunTable() {
		FInfo printlninfo = new FInfo();
		printlninfo.sigStr = "java/io/PrintStream/println(I)V";
		
		FInfo maininfo = new FInfo();
		maininfo.sigStr = "main([Ljava/lang/String;)V";
		_fsymtable.put("_print", printlninfo);
		_fsymtable.put("main", maininfo);
	}

	//심볼테이블에서 메소드를 호출함.
	public String getFunSpecStr(String fname) {		
		// <Fill here>
        FInfo fInfo = _fsymtable.get(fname);	// 메서드심볼테이블에서 fname을 키값으로 변수정보를 가져옵니다.
		return fInfo.sigStr;					//fInfo 객체에 저장되어있는 bytecode 메소드 문장을 반환합니다.
	}

	//심볼테이블에서 메소드를 호출함.
	public String getFunSpecStr(Fun_declContext ctx) {
		// <Fill here>
		FInfo fInfo = _fsymtable.get(ctx);	// 메서드심볼테이블에서 ctx을 키값으로 변수정보를 가져옵니다.
		return fInfo.sigStr;				//fInfo 객체에 저장되어있는 bytecode 메소드 문장을 반환합니다.
	}

	//심볼테이블에 메서드를 넣음.
	public String putFunSpecStr(Fun_declContext ctx) {
		String fname = getFunName(ctx);	//메서드이름을 넣습니다.
		String argtype = "";	//매개변수 타입
		String rtype = "";		//리턴 타입
		String res = "";		//result 결과

		// <Fill here>
		String temp = ctx.getChild(0).getText();	//리턴타입을 검사하기 위해 임시변수에 넣습니다.
		if(temp.equals("void") || temp.equals(""))	//void이거나 공백이면 rtype에 "V"를 넣습니다.
		{
			rtype = "V";
		}else{	//그 외에는 int이므로 "I"를 넣습니다.
			rtype = "I";
		}

		ParamsContext temp_ctx = (ParamsContext)ctx.getChild(3);	//매개변수가 몇개 인지 확인하기 위해 추출합니다.
		int i=0;
		while(i<temp_ctx.getChildCount())	//매개변수를 다 검사할때까지 반복합니다.
		{
			if(temp_ctx.getChild(i) instanceof MiniCParser.ParamContext)	//매개변수가 있다면 argType에 "I"를 덧붙입니다.
			{
				argtype += "I";
			}
			i++;
		}

		res =  fname + "(" + argtype + ")" + rtype;	//res 변수에 메서드이름과 매개변수, 그리고 리턴타입을 완성하여 저장합니다.

		FInfo finfo = new FInfo();	//메서드정보 객체를 생성합니다.
		finfo.sigStr = res;			//메서드정보 객체의 sigStr 속성에 res를 저장합니다.
		_fsymtable.put(fname, finfo);	//메서드이름을 키값으로 함수정보 객체를 함수심볼테이블에 저장합니다.
		
		return res;
	}

	//변수명을 호출합니다.
	String getVarId(String name){
		// <Fill here>
		VarInfo lvar = _lsymtable.get(name);	//지역심볼테이블에서 변수명을 키값으로 bytecode의 변수 아이디를 반환합니다.
		if(lvar != null)
		{
			return Integer.toString(lvar.id);
		}

		VarInfo gvar = _gsymtable.get(name);
		if(gvar != null)
		{
			return Integer.toString(gvar.id);
		}
		return null;
	}
	//변수 타입을 호출합니다.
	Type getVarType(String name){
		//지역변수인 경우
		VarInfo lvar = (VarInfo) _lsymtable.get(name); //지역심볼테이블에서 변수명을 키값으로 변수정보 객체를 반환합니다.
		if (lvar != null) {	//null이 아니라면 변수정보 객체의 타입을 반환합니다.
			return lvar.type;
		}

		//전역변수인 경우
		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return gvar.type;
		}

		//테이블에 없는 경우 에러를 반환한다.
		return Type.ERROR;	
	}

	//새로운 레이블을 생성한다.
	String newLabel() {
		return "label" + _labelID++;
	}

	String newTempVar() {
		String id = "";
		return id + _tempVarID--;
	}

	// global
	//전역변수 아이디를 호출합니다.
	public String getVarId(Var_declContext ctx) {
		// <Fill here>
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());	//파서트리 노드의 변수를 매개변수로 넣어 아이디를 저장한다.
		return sname;								//전역변수에 대한 아이디를 반환한다.
	}

	// local
	//지역변수 아이디를 호출합니다.
	public String getVarId(Local_declContext ctx) {
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());	//파서트리 노드의 변수를 매개변수로 넣어 아이디를 저장한다.
		return sname;								//지역변수에 대한 아이디를 반환한다.
	}
	
}
