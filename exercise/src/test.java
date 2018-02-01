import jdk.nashorn.internal.objects.Global;

import javax.xml.bind.annotation.XmlElementDecl;
import java.util.Scanner;

import static jdk.nashorn.internal.objects.Global.eval;
import static jdk.nashorn.internal.objects.Global.print;

public class test {
    public static int money_total=0;
    public static int money_depositted;
    public static int money_withdrawed;
    public static void main(String[] args){
        Scanner scanner= new Scanner(System.in);

        test my_test = new test();
        boolean run = true;
 
        while(run){
            try {
                my_test.menu_print();
                int select = scanner.nextInt();
                if (select <= 4 && select >= 1) {
                    if (select == 1) {
                        System.out.println("얼마나 입금하시겠습니까?");
                        money_depositted = scanner.nextInt();
                        money_total += money_depositted;
                        my_test.get_money_total();

                    } else if (select == 2) {
                        System.out.print("얼마나 출금하시겠습니까?");
                        money_withdrawed = scanner.nextInt();
//                            if(money_withdrawed>money_total){
//                                System.out.println("최대 "+money_total+"원만 출금할 수 있습니다.");
//                                continue;
//                            }
                        money_total -= money_withdrawed;
                        my_test.get_money_total();

                    } else if (select == 3) {
                        my_test.get_money_total();
                    } else if (select == 4) {
                        System.out.println("이용해주셔서 감사합니다");
                        run = false;
                    } else{
                        System.out.println("다시 입력 해주세요");
                    }
                }
            }
            catch(Exception err){
                System.out.println("숫자를 입력해주세요");
                break;
            }
        }
    }


    public void get_money_total(){
        System.out.println("현재 계좌 잔고는 " +test.money_total + "원 입니다.");
    }


    private void menu_print(){
        System.out.println("================================");
        System.out.println("1.예금 | 2.출금 | 3.잔고 | 4.종료");
        System.out.println("================================");
        System.out.println("선택 >");
    }


}




