package com.github.kzn.jaot.morph;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * Morphological Configuration for Language
 * Intended for reading from XML
 * @author Anton Kazennikov
 *
 */
@XmlRootElement(name = "morph")
@XmlAccessorType(XmlAccessType.FIELD)
public class MorphConfig {
	@XmlElement
	String language;
	
	@XmlElement(name = "gramTable")
	String gramTablePath;
	
	@XmlElement(name = "mrd")
	String mrdPath;
	
	@XmlElementWrapper(name = "features")
	@XmlElement(name = "group")
	List<Group> groups;
	
	@XmlElement(name = "fst")
	String fstPath;
	
	
	
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Group {
		@XmlAttribute
		String name;
		@XmlElement(name = "feat")
		List<String> feats;
		
		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("name", name)
					.add("feats", feats)
					.toString();
		}
		
		public String getName() {
			return name;
		}
		
		public List<String> getFeats() {
			return feats;
		}
		
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("language", language)
				.add("mrd", mrdPath)
				.add("gramTable", gramTablePath)
				.add("groups", groups)
				.toString();
	}
	
	protected MorphConfig() {}
	
	public MorphConfig(String language) {
		this.language = language;
	}
	
	public void addGroup(String name, List<String> feats) {
		Group g = new Group();
		g.name = name;
		g.feats = feats;
		groups.add(g);
	}
	
	List<Group> getGroups() {
		return groups;
	}
	
	

	public static MorphConfig newInstance(File file) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(MorphConfig.class);
		Unmarshaller um = jaxbContext.createUnmarshaller();
		return (MorphConfig) um.unmarshal(file);
	}
	
	public static MorphConfig newInstance(InputStream is) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(MorphConfig.class);
		Unmarshaller um = jaxbContext.createUnmarshaller();
		return (MorphConfig) um.unmarshal(is);
	}
	
	
	public static void main(String[] args) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(MorphConfig.class);
		Unmarshaller um = jaxbContext.createUnmarshaller();
		
		MorphConfig lc = (MorphConfig) um.unmarshal(new File("russian.xml"));		
	}
}
