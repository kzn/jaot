package name.kazennikov.morph.aot;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.set.TIntSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import name.kazennikov.dafsa.GenericFSA;
import name.kazennikov.dafsa.GenericTrie;
import name.kazennikov.dafsa.IntFSA;
import name.kazennikov.dafsa.IntNFSA;
import name.kazennikov.dafsa.IntNFSA.IntNFSABuilder;
import name.kazennikov.morph.aot.MorphData.ParseProcessor;

public class TestRun {

    public static List<Character> expand(String s) {
        List<Character> chars = new ArrayList<Character>();

        for(int i = 0; i != s.length(); i++) {
            chars.add(s.charAt(i));
        }
        return chars;
    }

    public static class FSTNode extends GenericTrie.SimpleNode<Character, Set<Integer>, Integer> {

        public FSTNode(Set<Integer> fin) {
            super(fin);
        }

        @Override
        public GenericTrie.SimpleNode makeNode() {
            return new FSTNode(new HashSet<Integer>());
        }
    }
	public static void main(String[] args) throws JAXBException, IOException {
		MorphConfig mc = MorphConfig.newInstance(new File("russian.xml"));
		
		final MorphLanguage ml = new MorphLanguage(mc);
		
		long st = System.currentTimeMillis();
		MorphDict md = ml.readDict();
		

		

        final TObjectIntHashMap<BitSet> featSets = new TObjectIntHashMap<BitSet>();
        IntFSA fst = new IntFSA(new IntFSA.SimpleNode());
        TIntArrayList fstInput = new TIntArrayList();
		for(MorphDict.Lemma lemma : md.lemmas) {
			for(MorphDict.WordForm wf : lemma.expand()) {
				BitSet feats = ml.getWordFormFeats(wf.feats, wf.commonAnCode);
				int featId = featSets.get(feats);
				
				if(featId == 0) {
					featId = featSets.size() + 1;
					featSets.put(feats, featId);
				}
				MorphCompiler.expand(fstInput, wf.wordForm, wf.lemma);
                fst.addMinWord(fstInput, featId);
			}
			
		}

		st = System.currentTimeMillis() - st;
		System.out.printf("Elapsed: %d ms%n", st);
        System.out.printf("FSA size: %d%n", fst.size());
		System.out.printf("Dict size: %d%n", md.lemmas.size());
		System.out.printf("featSets: %d%n", featSets.size());
		IntNFSABuilder intFSTBuilder = new IntNFSABuilder();
		fst.write(intFSTBuilder);
		IntNFSA nfsa = intFSTBuilder.build();
		
		final BitSet[] fss = new BitSet[featSets.size() + 1];
		
		featSets.forEachEntry(new TObjectIntProcedure<BitSet>() {

			@Override
			public boolean execute(BitSet a, int b) {
				fss[b] = a;
				return true;
			}
		});
		
		MorphData.walkIterative(nfsa, "СТЕНА", new StringBuilder(), 0, 4, 0, 1, new ParseProcessor() {
			
			@Override
			public boolean process(final CharSequence s, final StringBuilder out, final int startIndex,
					final int endIndex, TIntSet fin) {
				fin.forEach(new TIntProcedure() {
					
					@Override
					public boolean execute(int value) {
						System.out.printf("parse: %s-%s%s%n", s.subSequence(startIndex, endIndex), out, ml.convert(fss[value]));
						return true;
					}
				});

				return true;
			}
		});

		
		
	}

}
