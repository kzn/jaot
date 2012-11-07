package name.kazennikov.morph.aot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class SplitSource {
	
	public static void writeSection(BufferedReader src, File dest) throws IOException {
		PrintWriter pw = new PrintWriter(dest, "utf-8");
		int count = Integer.parseInt(src.readLine());
		
		for(int i = 0; i < count; i++) {
			String s = src.readLine();
			pw.println(s);
		}
		pw.close();
	}
	
	public static void skipSection(BufferedReader src) throws IOException {
		int count = Integer.parseInt(src.readLine());
		
		for(int i = 0; i < count; i++) {
			String s = src.readLine();

		}
	}
	
	public static void splitDict(String dictBase) throws IOException {
		File base = new File(dictBase + ".mrd");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(base), Charset.forName("cp1251")));
		writeSection(br, new File(dictBase + ".paradigm"));
		writeSection(br, new File(dictBase + ".accents"));
		skipSection(br);
		writeSection(br, new File(dictBase + ".prefixset"));
		writeSection(br, new File(dictBase + ".lemma"));
	}

	
	
	
	public static void main(String[] args) throws IOException {
		splitDict("russian");
		
//		readParadigms(br);
//		readAccents(br);
//		skipSection(br); // skip session section
//		readPrefixSets(br);

		
	}

}
