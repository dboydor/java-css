package main;

import com.baobei.css.Parser;

public class Main {
	public static void main(String[] args) {
		System.out.println("================");
		System.out.println(" CSS Parse Test");
		System.out.println("================");

		Parser parser = new Parser();

		try {
			parser.parse(
				"body { color: red } "
				+ "html {"
				+ "font-family: sans-serif;"
				+ "line-height: 1.15;"
				+ "-ms-text-size-adjust: 100%;"
				+ "-webkit-text-size-adjust: 100% }");

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}
