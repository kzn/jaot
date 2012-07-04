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
			//IntTrie.Reader.read(s, intFSTBuilder, 4);
			//IntNFSA guesser = intFSTBuilder.build();
			
			return new MorphData(featSets, fsa, fst, null);
			
			
		} finally {
			if(s != null)
				s.close();
		}
		
	}
	
	
	public static interface ParseProcessor {
		public boolean process(CharSequence s, StringBuilder out, int startIndex, int endIndex, TIntSet fin);
	}
	
	protected void walkIterativeInternal(IntNFSA fst, CharSequence s, StringBuilder sb, int startIndex, int endIndex, int currentIndex, 
			int state, char ch, ParseProcessor parseProcessor) {
		long value = fst.getTransitionsInfo(state, ch);
		int start = fst.getTransitionsStart(value);
		int end = start + fst.getTransitionsLength(value);
		
		while(start < end) {
			
			char outCh = (char) fst.getTransitionOut(start);
			int nextState = fst.getTransitionNext(start);
			if(outCh != 0)
				sb.append(outCh);
			walkIterative(fst, s, sb, startIndex, endIndex, currentIndex != endIndex? currentIndex + 1: endIndex, 
					nextState, parseProcessor);
			if(outCh != 0)
				sb.deleteCharAt(sb.length() - 1);

			start++;
		}

	}
	
	public void walkIterative(IntNFSA fst, CharSequence s, StringBuilder sb, int startIndex, int endIndex, int currentIndex, 
			int state, ParseProcessor parseProcessor) {
		TIntSet fin = fst.getFinals(state);

			if(fin != null && !fin.isEmpty()) {
				parseProcessor.process(s, sb, startIndex, currentIndex, fin);
			}
			
			char ch = currentIndex < endIndex? s.charAt(currentIndex) : 0;
			walkIterativeInternal(fst, s, sb, startIndex, endIndex, currentIndex, state, ch, parseProcessor);
			char toUpper = Character.toUpperCase(ch);
			if(toUpper != ch) {
				walkIterativeInternal(fst, s, sb, startIndex, endIndex, currentIndex, state, toUpper, parseProcessor);
			}
			
			// for single-pass morphan on fst only
			ch = 0;
			walkIterativeInternal(fst, s, sb, startIndex, endIndex, currentIndex, state, (char)0, parseProcessor);

	}
	
	public static void main(String[] args) throws IOException, JAXBException {
		MorphConfig mc = MorphConfig.newInstance(new File("russian.xml"));
		
		final MorphLanguage ml = new MorphLanguage(mc);

		final MorphData mdata = MorphData.read(new File("russian.dat"));
		
		mdata.walkIterative(mdata.fst, "МАМЫ", new StringBuilder(), 0, 4, 0, 1, new ParseProcessor() {
			
			@Override
			public boolean process(final CharSequence s, final StringBuilder out, final int startIndex,
					final int endIndex, TIntSet fin) {
				fin.forEach(new TIntProcedure() {
					
					@Override
					public boolean execute(int value) {
						System.out.printf("parse: %s-%s%s%n", s.subSequence(startIndex, endIndex), out, ml.convert(mdata.featSets.get(value)));
						return true;
					}
				});

				return true;
			}
		});
	}

	
	
	
	
	
	
	

}
