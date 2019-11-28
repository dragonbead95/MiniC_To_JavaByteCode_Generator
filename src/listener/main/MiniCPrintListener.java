package listener.main;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class MiniCPrintListener extends MiniCBaseListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();   //파서트리의 노드들을 저장하는 맵입니다. 키값은 노드 그 자체입니다.
    int compound_count=-1;    // 들여써야할 갯수이다. 초기값이 -1인 이유는 가장 바깥쪽에 있는 중괄호는 들여쓰면 안되기 때문입니다.
    int noncompound_count=0;  //만약 if에 중괄호를 안썻다면 그 바로 아래에 소스코드를 넣기위해서입니다.
    String tab="....",tabsum="";

    /**
    * 피연산자 2개에 가운데 연산자가 있는 형식인지 검사하는 메소드
    * 예) "expr1" + "expr2"
    * */
    boolean isBinaryOperation(MiniCParser.ExprContext ctx)
    {
        //"expr1" + "expr2"의 형태를 자식으로 둔 노드는 자식의 수가 3개이고 가운데가 피연산자가 아니여야 합니다.
        return ctx.getChildCount()==3 && ctx.getChild(1) != ctx.expr();
    }

    /*
    * 괄호가 있는 수식인지 아닌지 검사하는 메소드
    * 예) "( expr1+expr2 )
    * 이 메소드는 ( expr1 + expr2 )와 같이 소괄호와 피연산자가 떨어지는걸 (expr1 + expr2)와 같이 붙여주는 메소드에 사용됩니다.
    * */
    boolean isParentheses(MiniCParser.ExprContext ctx)
    {
        //해당 노드의 첫번째로 "("로 시작하고 끝은 ")"로 끝나는 꼴인지 검사합니다.
        return ctx.start.getText().equals("(") && ctx.stop.getText().equals(")");
    }

    /**
     * 프로그램으로 들어가는 논터미널입니다.
     * 메소드의 내용은 매개변수로 받은 ctx의 자식 노드들을 newTexts 맵에 넣어줍니다.
     * 그 이유는 맵에 자식 노드들을 넣어두면 맵을 참조할때 키값으로 참조하여 내용을 참조할 수 있기 때문입니다.
     */
    @Override public void enterProgram(MiniCParser.ProgramContext ctx)
    {
        int i=0;    //while문의 인덱스 변수입니다.
        String s1=null; //자식노드들의 문자열을 저장하는 문자열 변수입니다.

        //반복문을 계속 반복하여 ctx의 모든 자식노드들을 newTexts 맵에 put메소드로 넣습니다.
        while(true)
        {
            /*newTexts 맵에 노드를 저장하기 위해서는 키값으로 노드 그 자체와 그 노드에 해당하는 value값을 넣습니다.*/
            s1 = ctx.getChild(i).getText();     //자식노드들의 문자열을 s1에 넣습니다.
            newTexts.put(ctx.getChild(i),s1);   //해당하는 자식노드를 키값으로 하고 그 자식노드의 문자열을 값으로 넣습니다.
            i++;

            /*만약 자식노드를 전부 newTexts맵에 넣었으면 null이 반환됩니다.*/
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }

    }
    /**
     * 프로그램을 나가는 논터미널 메소드입니다.
     * 프로그램을 나가기전 자식노드들의 문자열들을 불러서 취합하여 newTexts 맵에 있는 자식노드들의 부모 노드에 다시 저장합니다.
     */
    @Override public void exitProgram(MiniCParser.ProgramContext ctx)
    {
        String s1,str="";   /*s1은 자식노드들의 문자열을 저장하는 변수입니다.
                                str은 자식노드들의 문자열을 취합한 변수입니다.*/
        int i=0;            //i는 자식노드의 인덱스변수입니다.


        /*
         * 반복문을 진행하며 자식노드들을 참조합니다.
         * 자식노드들을 키로 삼음으로써 맵에 있는 해당 노드들의 문자열값을 s1에 저장합니다.
         * 해당 자식 노드들의 문자열을 str에 계속 추가하여 저장합니다.
         */
        while(true)
        {
            if(ctx.getChild(i)==null)
            {
                break;
            }else{
                s1 = newTexts.get(ctx.getChild(i));
                str += s1;
            }
            i++;
        }
        newTexts.put(ctx,str);  //모든 자식노드들의 문자열값을 취합한 str문자열 변수를 다시 맵에 있는 부모 노드에 저장합니다.
        System.out.println(newTexts.get(ctx));  //맵에 있는 노드의 문자열을 출력합니다.
    }
    /**
     * 논터미널 Decl으로 들어가는 메소드입니다.
     * 후에 변수와 함수로 구분된다.
     * enterProgram메소드와 합니다.
     */
    @Override public void enterDecl(MiniCParser.DeclContext ctx)
    {
        int i=0;
        String s1=null;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * 논터미널 Decl을 나가는 메소드입니다.
     * exitProgram메소드와 설명이 동일합니다.
     */
    @Override public void exitDecl(MiniCParser.DeclContext ctx)
    {
        String s1,str="";
        int i=0;
        while(true)
        {
            if(ctx.getChild(i)==null)
            {
                break;
            }else{
                s1 = newTexts.get(ctx.getChild(i));
                str +=s1;
            }
            i++;
        }
        newTexts.put(ctx,str);
    }
    /**
     * 변수설정 논터미널로 들어가는 메소드입니다.
     * enterProgram 메소드와 설명이 동일합니다.
     */
    @Override public void enterVar_decl(MiniCParser.Var_declContext ctx)
    {
        int i=0;
        String s1=null;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * 변수설정 논터미널을 나가는 메소드입니다.
     * 변수선언시 변수의 타입과 이름, = 등은 떨어져있어야 합니다.
     * ";"(세미콜론)은 마지막 자식노드와 같이 붙어있어야 합니다.
     */
    @Override public void exitVar_decl(MiniCParser.Var_declContext ctx)
    {
        String s1=null, str="";
        int i=0;
        /*반복문을 진행하며 자식노드들을 취합하니다.
        * 단, 자식노드가 세미콜론이면 s1 문자열 마지막에 줄바꿈을 넣습니다.
        * 예) int a=5;
        *     int x= 5;
        * */
        while(true)
        {
            if(ctx.getChild(i+1)==null) //만약 자식 노드가 ";"(세미콜론)이면 문자열에 더하고 반복문 탈출
            {
                s1 = newTexts.get(ctx.getChild(i)) + "\n";
                str += s1;
                break;
            }else if(ctx.getChild(i+1).getText().equals(";")==true) //만약 자식 노드의 다음 노드가 ";"이면 띄우면 안되기 때문에 붙여서 쓴다.
            {
                s1 = newTexts.get(ctx.getChild(i));
                str += s1;
            }
            else{
                s1 = newTexts.get(ctx.getChild(i)) + " ";
                str += s1;
            }
            i++;
        }

        newTexts.put(ctx,str);
    }
    /**
     * 변수 타입을 설정하는 논터미널 메소드입니다.
     * enterProgram메소드와 설명이 동일합니다.
     */
    @Override public void enterType_spec(MiniCParser.Type_specContext ctx)
    {
        int i=0;
        String s1=null;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * 변수 타입을 설정하는 논터미널을 나가는 메소드입니다.
     * exitProgram메소드와 설명이 동일합니다.
     */
    @Override public void exitType_spec(MiniCParser.Type_specContext ctx)
    {
        String s1,str="";
        int i=0;
        while(true)
        {
            if(ctx.getChild(i)==null)
            {
                break;
            }else{
                s1 = newTexts.get(ctx.getChild(i));
                str +=s1;
            }
            i++;
        }
        newTexts.put(ctx,str);
    }
    /**
     * 함수설정을 하는 논터미널 메소드입니다.
     * enterProgram메소드와 설명이 동일하나 한가지 추가된 것이 noncompound_count 변수의 증가입니다.
     * noncompound_count변수를 ++연산으로 1 증가시킨 이유는 함수 생성시 괄호를 생성하기 때문입니다.
     * 그래서 함수 안에 있는 수식들을 몇번을 들여쓸지 정하기 위해서 noncompound_count변수를 선언하여 증가시킵니다.
     */
    @Override public void enterFun_decl(MiniCParser.Fun_declContext ctx)
    {
        int i=0;
        String s1=null;
        noncompound_count++;    //괄호안에 있는 수식들을 들여쓰기 위해서 몇번 들여쓸지 세어주는 변수입니다.
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * 함수설정을 하는 논터미널을 나가는메소드입니다.
     * 함수를 구성하기 위한 문자열을 취합합니다.
     * 단, 함수의 반환타입과 함수이름은 한칸 띄어있어야하고 함수이름과 함수매개변수 부분은 붙어있어야합니다.
     * 그리고 함수의 중괄호는 함수 다음줄에 표시됩니다.
     * 예)
     * before:
     * int main(){
     *
     * }
     *
     * after:
     * int main()
     * {
     *
     * }
     */
    @Override public void exitFun_decl(MiniCParser.Fun_declContext ctx)
    {
        String s1,str="";
        int i=0;
        while(true)
        {
            if(ctx.getChild(i)==null)
            {
                break;
            }else{  //만약 자식노드가 있는 경우
                if(i==0)    //자식노드의 0번째는 반환문이기 때문에 s1변수 마지막에 공백을 삽입하여 넣습니다.
                {
                    s1 = newTexts.get(ctx.getChild(i)) + " ";
                    str += s1;
                }else if(ctx.getChild(i).getText().equals(")")==true)   /*함수 다음줄에 중괄호를 표시하기 위하여 ")"를 찾습니다.
                                                                            ")"를 찾았다면 s1변수 마지막에 줄바꿈을 넣습니다.*/
                {
                    s1 = newTexts.get(ctx.getChild(i)) + "\n";
                    str += s1;
                }else{                                                  /*그외의 자식노드들의 문자열을 붙여서 넣습니다.*/
                    s1 = newTexts.get(ctx.getChild(i));
                    str += s1;
                }
            }
            i++;
        }

        newTexts.put(ctx,str);
    }

    /**
     * Stmt에 들어가는 메소드입니다.
     * enterProgram와 설명이 동일합니다.
     */
    @Override public void enterStmt(MiniCParser.StmtContext ctx)
    {
        int i=0;
        String s1=null;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * Stmt를 나가는 메소드입니다.
     * exitProgram메소드와 설명이 동일합니다.
     */
    @Override public void exitStmt(MiniCParser.StmtContext ctx)
    {
        String s1,str="";
        int i=0;
        while(true)
        {
            if(ctx.getChild(i)==null)
            {
                break;
            }else{
                s1 = newTexts.get(ctx.getChild(i));
                str +=s1;
            }
            i++;
        }
        newTexts.put(ctx,str);
    }
    /**
     * Expr_stmt에 들어가는 메소드입니다.
     * enterProgram메소드와 설명이 동일합니다.
     */
    @Override public void enterExpr_stmt(MiniCParser.Expr_stmtContext ctx)
    {
        int i=0;
        String s1=null;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * Expr_stmt를 나가는 메소드입니다.
     * exitProgram과 설명이 동일합니다.
     */
    @Override public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx)
    {
        String s1,str="";
        int i=0;
        while(true)
        {
            if(ctx.getChild(i)==null)
            {
                break;
            }else{
                s1 = newTexts.get(ctx.getChild(i));
                str +=s1;
            }
            i++;
        }
        newTexts.put(ctx,str);
    }
    /**
     * While_stmt에 들어가는 메소드입니다.
     * enterProgram메소드와 설명이 동일하고 noncompound_count 변수를 1 증가시킵니다.
     * noncompound_count 변수를 사용하는 이유는 들여쓰는 횟수를 정하기 위해서입니다.
     */
    @Override public void enterWhile_stmt(MiniCParser.While_stmtContext ctx)
    {
        int i=0;
        String s1=null;
        noncompound_count++;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * While_stmt을 나가는 메소드입니다.
     * while문을 구성하기 위해 자식노드들의 문자열을 취합합니다.
     * 단, while문과 조건식의 괄호는 떨어져야 합니다.
     * 예)before: while(i >= 5)
     * after : while (i >= 5)
     *
     * 그리고 중괄호가 있는 경우 반복문 다음줄에 표시합니다.
     * 예)before: while (i >= 5){
     *
     * }
     * after : while (i >= 5)
     * {
     *
     * }
     *
     * 그리고 while문에서 중괄호를 작성하지 않고 바로 수식을 작성하는 경우
     * while문 바로 아래줄에 들여써서 while문을 구성한다.
     * 예)before : while ( i >= 5) write(i);
     * after : while ( i>= 5)
     *              write(i);
     *
     * 마지막으로 while문에서 중괄호가 있는 경우 Compound_stmt에 있는 노드의 문자열을 그대로 취합하여 불러와 자신의 노드에 저장한다.
     */
    @Override public void exitWhile_stmt(MiniCParser.While_stmtContext ctx)
    {
        String s1,str="";
        int i=0;
        while(true)
        {
            if(ctx.getChild(i)==null)   //자식노드가 더 없다면 null이 반환되어 반복문을 빠져나간다.
            {
                break;
            }else if(ctx.getChild(i).getText().equals("while")==true)   //자식노드중 "while"가 있다면 s1 변수 마지막에 한칸 띄우고 저장한다.
            {
                s1 = newTexts.get(ctx.getChild(i)) + " ";
                str += s1;
            }else if(ctx.getChild(i).getText().equals(")")==true)   /*자식노드 중 ")"가 있다는 것은 줄바꿈을 해야한다는 의미로
                                                                        s1변수에 줄바꿈을 넣고 저장한다.*/
            {
                s1 = newTexts.get(ctx.getChild(i))+"\n";
                str +=s1;
            }
            else if(ctx.getChild(i+1)==null)    //ctx 노드 중 마지막 자식 노드인 경우입니다.
            {
                /*ctx 마지막 자식 노드의 자식노드가 Compound_stmtContext인지 검사합니다.
                    Compound_stmtContext이면 중괄호가 있다는 뜻으로 이미 줄바꿈 같은 처리가 다 된 것이므로 s1에 그대로 불러와서 넣습니다.*/
                if(ctx.getChild(i).getChild(0) instanceof  MiniCParser.Compound_stmtContext)
                {
                    s1 = newTexts.get(ctx.getChild(i));
                }else{  /*Compound_stmtContext가 아닌 경우는 while문 다음에 바로 수식을 쓰는 경우입니다. 예) while(i>=5) write(i);
                        ctx 자식노드를 0부터 참조하면서 ")"가 들어있는 자식노드에서 str문자열에 이미 줄바꿈을 했기 때문에 들여쓰기를 몇번 할지
                        계산합니다.*/

                    /*반복문을 통하여 tab("....")을 tabsum에 계속 추가하여 저장합니다.
                    * noncompound_count의 갯수를 정하는 기준
                    * 첫번째 : 함수 진입시
                    * 두번째 : while문 진입시
                    * 세번째 : if문 진입시
                    * */
                    for(int k=0;k<noncompound_count;k++)
                    {
                        tabsum += tab;
                    }
                    s1 = tabsum + newTexts.get(ctx.getChild(i));    //여려번의 tab(들여쓰기)가 저장된 tabsum과 자식노드의 문자열을 저장합니다.
                }
                str += s1;
            }
            else{
                s1 = newTexts.get(ctx.getChild(i));
                str += s1;
            }
            i++;
        }
        newTexts.put(ctx,str);
        noncompound_count--;
        tabsum="";
    }
    /**
     * Compound_stmt에 들어가는 메소드입니다.
     * enterProgram과 설명이 동일하나 블록에 진입했기 때문에 compound_count를 1 증가시킵니다.
     * compound_count를 증가시키는 기준은
     * Compound_stmt에 들어갈때입니다.
     */
    @Override public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx)
    {
        int i=0;
        String s1=null;
        compound_count++;    //괄호가 생기므로 들여쓰기해야할 갯수를 1 증가시킵니다.

        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * Compound_stmt를 나가는 메소드입니다.
     * 중괄호 안에 있는 수식들을 들여쓰기 하기 위해서 tabsum에 compound_count 갯수만큼의 tab을 추가적으로 저장합니다.
     * compound_count가 2인경우 tabsum은 "........"이다.
     */
    @Override public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx)
    {
        String s1=null,str="";
        int i=0;

        /*반복문을 진행하며 들여쓰기할 tabsum을 저장합니다.*/
        for(int k=0;k<compound_count;k++)
        {
            tabsum += tab;
        }

        while(true)
        {
            if(ctx.getChild(i)==null)   //자식노드가 더이상 없으면 null을 반환하고 반복문을 빠져나갑니다.
            {
                break;
            }

            if(ctx.getChild(i).getText().equals("{")==true) //자식노드가 "{"인 경우 그 다음줄에 수식을 표현하기 위해서 줄바꿈을 넣습니다.
            {
                s1 = tabsum + newTexts.get(ctx.getChild(i))+"\n";

            }else if(ctx.getChild(i).getText().equals("}")==true)   //자식노드가 "}"인 경우 그대로 문자열에 넣습니다.
            {
                s1 = tabsum + newTexts.get(ctx.getChild(i));
            }
            else{                                                   /*그외의 수식은 tabsum만큼의 들여쓰기와 추가로 중괄호안에 있으므로
                                                                        한번더 tab을 저장하여 문자열을 구성합니다.
                                                                        예) while(i>=5)
                                                                            {
                                                                                if(i>=3)
                                                                                {
                                                                                    write(i);
                                                                                }
                                                                            }*/
                s1 = tabsum + tab + newTexts.get(ctx.getChild(i))+"\n";
            }

            str += s1;
            i++;

        }

        newTexts.put(ctx,str);
        compound_count--;   //괄호를 빠져나가므로 compound_count를 하나 감소시킵니다.
        tabsum="";          //다 사용한 tabsum을 초기화시킵니다.
    }
    /**
     * Local_decl에 들어가는 메소드입니다.
     * enterProgram과 설명이 동일합니다.
     */
    @Override public void enterLocal_decl(MiniCParser.Local_declContext ctx)
    {
        int i=0;
        String s1=null;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * Local_decl을 나가는 메소드입니다.
     * 변수타입과 변수이름, "="은 서로 떨어져 있어야합니다. 예) int x = 50;
     * 단 ";"(세미콜론)은 마지막 수식과 붙어져 있어야합니다. 예) int x = 50;
     */
    @Override public void exitLocal_decl(MiniCParser.Local_declContext ctx)
    {
        String s1=null, str="";
        int i=0;
        while(true)
        {
            if(ctx.getChild(i+1)==null) //만약 자식 노드가 ";"(세미콜론)이면 문자열에 더하고 반복문 탈출
            {
                s1 = newTexts.get(ctx.getChild(i));
                str += s1;
                break;
            }else if(ctx.getChild(i+1).getText().equals(";")==true) //만약 자식 노드의 다음 노드가 ";"이면 띄우면 안되기 때문에 붙여서 쓴다.
            {
                s1 = newTexts.get(ctx.getChild(i));
                str += s1;
            }
            else{
                s1 = newTexts.get(ctx.getChild(i)) + " ";
                str += s1;
            }
            i++;
        }

        newTexts.put(ctx,str);
    }
    /**
     * enterIf_stmt에 들어가는 메소드입니다.
     * enterProgram과 설명이 동일하나 if문에 들어가는 것이기때문에 noncompound_count를 1증가시킵니다.
     */
    @Override public void enterIf_stmt(MiniCParser.If_stmtContext ctx)
    {
        int i=0;
        String s1=null;
        noncompound_count++;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * If_stmt을 나가는 메소드입니다.
     * 조건문 이름인 if와 조건식은 떨어져 있어야합니다.
     * 예) before: if(i >= 5)
     * after : if (i >= 5)
     *
     * 조건문의 시작은 괄호를 다음줄에 표시한다.
     * 예) before : if ( i >= 5){
     *
     * }
     * after : if ( i >= 5)
     * {
     *
     * }
     *
     * if문에서 조건식을 작성하고 중괄호없이 바로 조건내용을 작성하는 경우 줄바꿈을 하고 들여쓰기 합니다.
     * 예) before : if ( i >= 5) write(i);
     * after : if ( i >= 5)
     *              write(i);
     *
     * 만약 중괄호가 있다면 줄바꿈과 들여쓰기 처리된 compound_stmt를 그대로 문자열에 저장합니다.
     */
    @Override public void exitIf_stmt(MiniCParser.If_stmtContext ctx)
    {
        String s1,str = "";
        int i=0;
        while(true)
        {
            if(ctx.getChild(i)==null)   //자식노드가 더이상 없다면 반복문을 빠져나갑니다.
            {
                break;
            }else if(ctx.getChild(i).getText().equals("if")==true)  //자식노드가 "if"인 경우 s1 변수 마지막에 한칸 띄웁니다.
            {
                s1 = newTexts.get(ctx.getChild(i)) + " ";
                str += s1;
            }
            else if(ctx.getChild(i).getText().equals(")")==true)    /*자식노드가 ")"인 경우 조건내용을 다음줄에 표현하기 위해
                                                                        s1 마지막에 줄바꿈을 넣어 저장합니다.*/
            {
                s1 = newTexts.get(ctx.getChild(i))+"\n";
                str +=s1;
            }else if(ctx.getChild(i+1)==null)   //마지막 자식노드인 경우
            {
                //Compound_stmt인지 확인한다.
                if(ctx.getChild(i).getChild(0) instanceof  MiniCParser.Compound_stmtContext)
                {
                    s1 = newTexts.get(ctx.getChild(i));
                }else{  //Compound_stmt가 아닌경우는 if조건문 다음에 바로 조건내용을 쓴 경우이다. 예)if ( i >= 5) write(i);
                    //반복문을 진행하며 들여쓰기 해야할 tabsum을 지정합니다.
                    for(int k=0;k<noncompound_count;k++)
                    {
                        tabsum += tab;
                    }
                    s1 = tabsum + newTexts.get(ctx.getChild(i));
                }
                str += s1;
            }
            else{
                s1 = newTexts.get(ctx.getChild(i));
                str +=s1;
            }
            i++;
        }

        newTexts.put(ctx,str);
        noncompound_count--;    //if문이 끝났으므로 noncompound_count를 1 감소시킵니다.
        tabsum="";
    }
    /**
     * Return_stmt에 들어가는 메소드입니다.
     * enterProgram메소드와 설명이 동일합니다.
     */
    @Override public void enterReturn_stmt(MiniCParser.Return_stmtContext ctx)
    {
        int i=0;
        String s1=null;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }
    }
    /**
     * Return_stmt를 나가는 메소드입니다.
     * exitProgram메소드와 설명이 동일합니다.
     */
    @Override public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx)
    {
        String s1,str="";
        int i=0;
        while(true)
        {
            if(ctx.getChild(i)==null)
            {
                break;
            }else{
                s1 = newTexts.get(ctx.getChild(i));
                str +=s1;
            }
            i++;
        }
        newTexts.put(ctx,str);
    }
    /**
     * Expr에 들어가는 메소드입니다.
     * enterProgram메소드와 설명이 동일합니다.
     */
    @Override public void enterExpr(MiniCParser.ExprContext ctx)
    {
        int i=0;
        String s1=null;
        while(true)
        {
            s1 = ctx.getChild(i).getText();
            newTexts.put(ctx.getChild(i),s1);
            i++;
            if(ctx.getChild(i)==null)
            {
                break;
            }
        }

    }
    /**
     * Expr을 나가는 메소드입니다.
     * 수식을 나갈때 isBinaryOperation(MiniCParser.ExprContext)메소드로 피연선자2개와 가운데에 연산자가 있는 수식인지 확인합니다.
     * 예) x+y
     * 2진 연산자와 피연산자 사이에는 빈칸을 1칸 둔다.
     * 만약 수식이 소괄호로 둘러싸였으면 피연산자에 붙여서 둔다. 예) ( x + y ) -> (x + y)
     */
    @Override public void exitExpr(MiniCParser.ExprContext ctx)
    {
        String s1 = null, s2= null, op=null,expr=null;  /* s1과 s2는 피연산자입니다.
                                                            op는 연산자입니다. expr은 괄호안에 있는 수식입니다.*/
        if(isBinaryOperation(ctx))  // expr + expr 꼴인지 검사합니다.
        {
            //예: expr '+' expr
            s1 = newTexts.get(ctx.getChild(0));
            s2 = newTexts.get(ctx.getChild(2));
            op = ctx.getChild(1).getText();
            newTexts.put(ctx,s1 +" " +op+" "+s2); // x+y -> x + y 꼴로 바꿔서 저장합니다.

        }
        if(isParentheses(ctx))  //소괄호가 있는 수식인지 검사합니다.
        {
            s1 = newTexts.get(ctx.getChild(0));
            expr = newTexts.get(ctx.getChild(1));
            s2 = newTexts.get(ctx.getChild(2));
            newTexts.put(ctx,s1 + expr + s2); // ( x + y ) -> (x + y) 꼴로 바꿔서 저장합니다.
        }
    }


}
