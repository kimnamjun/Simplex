import java.util.*;
import java.io.File;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;

public class Simple_Excel_Language {
	public static void main(String args[]) throws InterruptedException {
		Scanner scan = new Scanner(System.in);
		float TEMP;

		int sheet[][] = new int[100][100];
		sheet[0][0] = 1;
		sheet[0][1] = 2;
		sheet[0][2] = 3;
		sheet[0][3] = 4;
		sheet[0][4] = 5;
		sheet[0][5] = 15;
		sheet[1][0] = 2;
		sheet[1][1] = 4;
		sheet[1][2] = 6;
		sheet[1][3] = 8;
		sheet[1][4] = 10;
		sheet[1][5] = 6;

		int score[][] = new int[2][5];
		int average[] = new int[2];
		int i = 0;

		for (int Q = 0; Q <= 1; Q++) {
			for (int W = 0; W <= 4; W++) {
				score[Q][W] = scan.nextInt();
			}
		}
		for (int E = 0; E < 2; E++) {
			Arrays.sort(score[i]);
			TEMP = 0;
			for (int I = 0; I <= 4; I++) {
				TEMP += score[i][I];
			}
			average[i] = (int) TEMP / 5;
			i = i + 1;
		}
		System.out.println("");
		System.out.println("���� ��� ����");
		System.out.println(average[0]);
		System.out.println(average[1]);
		System.out.println("");
		System.out.println("���� ����");
		for (int I = 0; I <= 1; I++) {
			for (int J = 0; J <= 4; J++) {
				System.out.print(score[I][J] + " 	");
			}
			System.out.println();
		}

		WritableWorkbook wworkbook;
		try {
			wworkbook = Workbook.createWorkbook(
					new File("C:\\Users\\NamJun\\eclipse-workspace\\Simple Excel Language\\src\\Excel_File.xls"));
			WritableSheet wsheet = wworkbook.createSheet("Simplex_Sheet", 0);
			for (int O = 0; O < 100; O++) {
				for (int P = 0; P < 100; P++) {
					if (sheet[O][P] != 0) {
						Number label = new Number(O, P, sheet[O][P]);
						wsheet.addCell(label);
					}
				}
			}
			wworkbook.write();
			wworkbook.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}