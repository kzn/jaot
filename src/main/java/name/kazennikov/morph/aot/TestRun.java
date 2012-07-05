package name.kazennikov.morph.aot;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import name.kazennikov.dafsa.GenericTrie;
import name.kazennikov.dafsa.IntFSA;
import name.kazennikov.dafsa.IntNFSA;
import name.kazennikov.dafsa.IntNFSA.IntNFSABuilder;

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
			if(!lemma.lemma.equals("МАМА"))
				continue;
			
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
		PrintWriter pw = new PrintWriter("mama.dot");
		fst.write(new IntFSA.FSTDotFormatter(pw));
		pw.close();

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
		
	}

}
