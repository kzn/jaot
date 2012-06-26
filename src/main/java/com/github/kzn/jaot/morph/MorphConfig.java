package com.github.kzn.jaot.morph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Language-specific morphological configuration.
 * Organizes features from AOT projects in more NLP oriented hier
 * @author Anton Kazennikov
 *
 */
public class MorphConfig {
	public static class Group {
		List<Feature> feats = new ArrayList<MorphConfig.Feature>();
		
		public void add(Feature feat) {
			feats.add(feat);
			feat.group = this;
		}
	}
	
	public static class Feature {
		String name;
		Group group;
		
		public Feature(String name, Group group) {
			this.name = name;
			this.group = group;
		}
		
		public String getName() {
			return name;
		}
		
		public Group getGroup() {
			return group;
		}
	}
	
	Map<String, Group> groups = new HashMap<String, MorphConfig.Group>();
	Map<String, Feature> feats = new HashMap<String, MorphConfig.Feature>();
	

}
