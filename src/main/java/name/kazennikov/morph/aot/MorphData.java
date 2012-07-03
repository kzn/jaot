package name.kazennikov.morph.aot;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import name.kazennikov.dafsa.IntNFSA;
import name.kazennikov.dafsa.IntNFSA.IntNFSABuilder;
import name.kazennikov.dafsa.IntTrie;


/**
 * Representation of binary morphological data
 * @author Anton Kazennikov
 *
 */
public class MorphData {
	List<BitSet> featSets;
	IntTrie fsa;
	IntNFSA fst;
	IntNFSA guesser;
	
	
	protected MorphData(List<BitSet> featSets, IntTrie fsa, IntNFSA fst, IntNFSA guesser) {
		super();
		this.featSets = featSets;
		this.fsa = fsa;
		this.fst = fst;
		this.guesser = guesser;
	}
	
	public static MorphData read(File file) throws IOException {
		DataInputStream s = null;

		try {
			s = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			List<BitSet> featSets = new ArrayList<BitSet>();
			int featSetCount = s.readInt();

			// featsets read
			for(int i = 0; i != featSetCount; i++) {
				int len = s.readInt();
				BitSet feats = new BitSet();
				
				for(int j = 0; j < len; j++) {
					int feat = s.readInt();
					feats.set(feat);
				}
				featSets.add(feats);
			}
			
			
			
			IntTrie.SimpleBuilder intTrieBuilder = new IntTrie.SimpleBuilder();
			IntTrie.Reader.read(s, intTrieBuilder, 2);
			IntTrie fsa = intTrieBuilder.build();
			
			IntNFSABuilder intFSTBuilder = new IntNFSABuilder();
			IntTrie.Reader.read(s, intFSTBuilder, 4);
			IntNFSA fst = intFSTBuilder.build();
			IntTrie.Reader.read(s, intFSTBuilder, 4);
			IntNFSA guesser = intFSTBuilder.build();
			
			return new MorphData(featSets, fsa, fst, guesser);
			
			
		} finally {
			if(s != null)
				s.close();
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		MorphData mdata = MorphData.read(new File("russian.dat"));
	}
	
	
	
	
	
	

}
