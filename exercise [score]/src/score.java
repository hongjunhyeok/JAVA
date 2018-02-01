import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class score {

    //전체적인 상수 선언 & 인스턴스화
    public static int _STUDENT_NUMBER_;
    public static ArrayList<Integer> student_list=new ArrayList<Integer>();
    public static boolean run = true;
    public static Scanner scanner= new Scanner(System.in);
    public static score my_score = new score();

    //전체적인 함수 실행
    public static void main(String[] args){
        while(run){
            try {
                my_score.score_menu();
                int select = scanner.nextInt();

                // 학생수 변경
                if (select == 1) {
                    my_score.set_student_number();
                    System.out.println("입력완료");
                }
                //점수입력
                else if (select == 2) {
                    my_score.score_input();
                    System.out.println("입력완료");
                }
                //점수 리스트
                else if (select == 3) {
                    my_score.score_list();

                }
                //분석
                else if (select == 4) {
                    System.out.println("평균은 " + my_score.score_average(student_list) + "입니다.");
                    System.out.println("최고점은 " + my_score.score_rank(student_list) + "입니다.");

                }
                //종료
                else if (select == 5) {
                    System.out.println("프로그램을 종료합니다.");
                    run = false;
                }
                //예외
                else {
                    System.out.println("잘못 입력하였습니다.");
                }
            }
            catch(Exception err){
                System.out.format("잘못 입력하였습니다. 에러 이유 : %s\n",err);
                run=false;
            }
        }
    }

    public void set_student_number(){
        System.out.println("학생 수를 입력하세요");
        int student_number = scanner.nextInt();
        _STUDENT_NUMBER_=student_number;
    }
    public void score_input() {
        for (int i = 0; i < _STUDENT_NUMBER_; i++) {
            System.out.format("%d번째 학생의 점수를 입력하세요\n", i+1);
            int point =scanner.nextInt();
            student_list.add(i,point);

        }
    }
    public void score_list(){


            System.out.println("====================================================");
            for(int i=0;i<_STUDENT_NUMBER_;i++) {
                System.out.format("%d번째 학생의 점수는 %d 점입니다.\n", i + 1, student_list.get(i));
            }
            System.out.println("====================================================");

    }

    // 메소드를 따로 선언하기위해선 공개범위/ return의 형태 / title을 적어주어야한다.
    public double score_sum(ArrayList<Integer> grade){
        double sum=0.0;
        for(int i=0;i<grade.size();i++){
            sum+=grade.get(i);
        }

        return sum;
    }

    public double score_average(ArrayList<Integer> grade){
        double average=0.0;
        double sum =this.score_sum(grade);
        return average=sum/_STUDENT_NUMBER_;
    }

    public double score_rank(ArrayList<Integer> grade){
        int max = Collections.max(grade);
        return max;
    }
    public void score_menu(){
        System.out.println("====================================================");
        System.out.println("1.학생수 | 2.점수입력 | 3.점수리스트 | 4.분석 | 5.종료");
        System.out.println("====================================================");
        System.out.print("선택 >");
    }


}
