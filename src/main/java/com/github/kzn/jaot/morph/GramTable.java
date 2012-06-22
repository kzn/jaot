package com.github.kzn.jaot.morph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GramTable {
	public static class Record {
		String pos;
		String type;
		List<String> feats;
	}
	
	Map<String, Record> content = new HashMap<String, GramTable.Record>();
	
	public Record getRecord(String key) {
		return content.get(key);
	}
	
	
	public void read(File f) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "CP1251"));
		
		while(true) {
			String s = br.readLine();
			if(s == null)
				break;
			
			
		}
		
		
	}
	
	
	

}
