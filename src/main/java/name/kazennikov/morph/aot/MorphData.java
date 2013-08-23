package name.kazennikov.morph.aot;

import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.xml.bind.JAXBException;

import name.kazennikov.dafsa.obsolete.CharFSTWalker;
import name.kazennikov.dafsa.obsolete.FSAException;
import name.kazennikov.dafsa.obsolete.IntNFSAv2;
import name.kazennikov.dafsa.obsolete.IntNFSAv2.IntNFSABuilder;
import name.kazennikov.dafsa.obsolete.IntTrie;



/**
 * Representation of binary morphological data
 * @author Anton Kazennikov
 *
 */
public class MorphData {
	List<BitSet> featSets;
	IntTrie fsa;
	IntNFSAv2 fst;
	IntNFSAv2 guesser;
	
	
	protected MorphData(List<BitSet> featSets, IntTrie fsa, IntNFSAv2 fst, IntNFSAv2 guesser) {
		super();
		this.featSets = featSets;
		this.fsa = fsa;
		this.fst = fst;
		this.guesser = guesser;
	}
	
	public static MorphData read(File file) throws IOException, FSAException {
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
			
			
			
			IntTrie.SimpleBuilder<IntTrie> intTrieBuilder = new IntTrie.SimpleBuilder<IntTrie>(new IntTrie());
			IntTrie.Reader.read(s, intTrieBuilder, 2);
			IntTrie fsa = intTrieBuilder.build();
			
			IntNFSABuilder intFSTBuilder = new IntNFSABuilder();
			IntTrie.Reader.read(s, intFSTBuilder, 4);
			IntNFSAv2 fst = intFSTBuilder.build();
			IntTrie.Reader.read(s, intFSTBuilder, 4);
			IntNFSAv2 guesser = intFSTBuilder.build();
			
			return new MorphData(featSets, fsa, fst, guesser);
			
			
		} finally {
			if(s != null)
				s.close();
		}
		
	}
	

	public IntNFSAv2 getFST() {
		return fst;
	}
	
	public IntTrie getFSA() {
		return fsa;
	}
	
	public IntNFSAv2 getGuesser() {
		return guesser;
	}
	
	public BitSet getFeats(int index) {
		return featSets.get(index);
	}
	
	public static void main(String[] args) throws IOException, JAXBException, FSAException {
		MorphConfig mc = MorphConfig.newInstance(new File("russian.xml"));
		
		final MorphLanguage ml = new MorphLanguage(mc);

		final MorphData mdata = MorphData.read(new File("russian.dat"));
		
		CharFSTWalker walker = mdata.fst.makeWalker();
		
		walker.walk("некрофил", 0, 8, new CharFSTWalker.Processor() {
			
			@Override
			public void parse(final CharSequence src, final StringBuilder out, final int start, final int end,
					TIntSet feats) {
				feats.forEach(new TIntProcedure() {
					
					@Override
					public boolean execute(int value) {
						System.out.printf("parse: %s -> %s%s%n", src.subSequence(start, end), out, ml.convert(mdata.featSets.get(value)));
						return true;
					}
				});


			}
		});
	}
}
