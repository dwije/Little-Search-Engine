package lse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LSEDriver {

	public static void main(String[] args) throws FileNotFoundException {
		LittleSearchEngine lse = new LittleSearchEngine();
		lse.makeIndex("docs.txt", "noisewords.txt");
		Scanner term1 = new Scanner(System.in);
		System.out.println("Search term 1 = ");
		Scanner term2 = new Scanner(System.in);
		System.out.println("Search term 2 = ");
		ArrayList<String> result = lse.top5search(term1.next(), term2.next());
		if (result == null) {
			System.out.println("No matches found");
		} else {
			for(String item : result) {
				System.out.print(item + " ");
			}
		}
	}
}