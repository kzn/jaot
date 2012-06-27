package com.github.kzn.jaot.morph;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

public class MorphLanguage {
	
	public static abstract class AbstractFeature {
		public static final int EMPTY_ID = -1;
		
		String name;
		int id;
		BitSet value;
		AbstractFeature parent;
		
		public AbstractFeature(int id, String name, BitSet value, AbstractFeature parent) {
			this.name = name;
			this.id = id;
			this.value = value;
			this.parent = parent;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof Feature)
				return Objects.equal(name, ((Feature) o).name);
			
			return false;
		}
		
		public BitSet copyValue() {
			return (BitSet)value.clone();
		}

		public String getName() {
			return name;
		}

		public int getId() {
			return id;
		}

		public BitSet getValue() {
			return value;
		}
		
		public AbstractFeature getParent() {
			return parent;
		}
		
		public abstract AbstractFeature getChild(int index);
		public abstract int getChildCount();
	}
	
	
	
	public static class Feature extends AbstractFeature {
		
		public Feature(int id, String name, AbstractFeature parent) {
			super(id, name, new BitSet(), parent);
			value.set(id);
		}

		@Override
		public AbstractFeature getChild(int index) {
			return null;
		}

		@Override
		public int getChildCount() {
			return 0;
		}
		
		@Override
		public String toString() {
			return "'" + name + "'";
		}
	}
	
	public static class Group extends AbstractFeature {
		List<AbstractFeature> feats = new ArrayList<MorphLanguage.AbstractFeature>();
		
		public Group(String name, AbstractFeature parent) {
			super(EMPTY_ID, name, new BitSet(), parent);
			this.name = name;
		}
		
		
		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("name", name)
					.add("feats", feats)
					.toString();
		}
		
		public void add(AbstractFeature feat) {
			if(!value.get(feat.getId())) {
				feat.parent = this;
				feats.add(feat);
				value.set(feat.getId());
			}
		}
		
		@Override
		public AbstractFeature getChild(int index) {
			return feats.get(index);
		}
		@Override
		public int getChildCount() {
			return feats.size();
		}

	}
	
	List<String> featList = new ArrayList<String>(); // ordered list of features
	Map<String, AbstractFeature> feats = new HashMap<String, MorphLanguage.AbstractFeature>();
	// ancode mapping
	Map<String, BitSet> anCodes = new HashMap<String, BitSet>();
	// 
	
	GramTable gramTable; 
	
	public MorphLanguage(MorphConfig mc) throws IOException {
		for(MorphConfig.Group g : mc.groups) {
			Group group = new Group(g.getName(), null);
			feats.put(group.getName(), group);
			
			for(String feat : g.feats) {
				Feature f = new Feature(featList.size(), feat, group);
				featList.add(feat.intern());
				feats.put(feat, f);
			}
		}
		
		gramTable = new GramTable();
		gramTable.read(new File(mc.gramTablePath), Charset.forName("windows-1251"));
	}
	
	public int featCount() {
		return featList.size();
	}
	
	public BitSet convert(List<String> feats) {
		BitSet value = new BitSet(featList.size());
		
		for(String feat : feats) {
			AbstractFeature f = this.feats.get(feat);
			if(f == null) {
				// TODO: warning
				continue;
			}
			
			value.or(f.getValue());
		}
		
		return value;
	}
	
	public List<String> convert(BitSet value) {
		List<String> res = new ArrayList<String>();
		
		for(int i = 0; i != value.length(); i++) {
			if(value.get(i)) {
				res.add(featList.get(i));
			}
		}
		
		return res;
	}
	
	public BitSet mapAncode(String anCode) {
		BitSet res = anCodes.get(anCode);
		
		if(res != null)
			return res;
		
		GramTable.Record rec = gramTable.get(anCode);
		if(rec == null)
			return new BitSet();
		
		List<String> feats = rec.feats();
		
		res = convert(feats);
		anCodes.put(anCode, res);
		return res;
	}
	

}
