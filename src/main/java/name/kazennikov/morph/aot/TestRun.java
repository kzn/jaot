package name.kazennikov.morph.aot;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import name.kazennikov.morph.fsa.SimpleTrie;
import name.kazennikov.morph.fsa.Trie;

public class TestRun {

    public static List<Character> expand(String s) {
        List<Character> chars = new ArrayList<Character>();

        for(int i = 0; i != s.length(); i++) {
            chars.add(s.charAt(i));
        }
        return chars;
    }

    public static class FSTNode extends Trie.SimpleNode<Character, Set<Integer>, Integer> {

        public FSTNode(Set<Integer> fin) {
            super(fin);
        }

        @Override
        public Trie.SimpleNode makeNode() {
            return new FSTNode(new HashSet<Integer>());
        }
    }
	public static void main(String[] args) throws JAXBException, IOException {
		MorphConfig mc = MorphConfig.newInstance(new File("russian.xml"));
		
		MorphLanguage ml = new MorphLanguage(mc);
		
		long st = System.currentTimeMillis();
		MorphDict md = ml.readDict();
		

		

        SimpleTrie<Character, Set<Integer>, Integer> trie =
                new SimpleTrie<Character, Set<Integer>, Integer>(new FSTNode(new HashSet<Integer>()));
        
        TObjectIntHashMap<BitSet> featSets = new TObjectIntHashMap<BitSet>();

		for(MorphDict.Lemma lemma : md.lemmas) {
			for(MorphDict.WordForm wf : lemma.expand()) {
				BitSet feats = ml.getWordFormFeats(wf.feats, wf.commonAnCode);
				int featId = featSets.get(feats);
				
				if(featId == 0) {
					featId = featSets.size() + 1;
					featSets.put(feats, featId);
				}
				
                trie.addMinWord(expand(wf.wordForm), featId);
			}
			
		}

		st = System.currentTimeMillis() - st;
		System.out.printf("Elapsed: %d ms%n", st);
        System.out.printf("FSA size: %d%n", trie.size());
		System.out.printf("Dict size: %d%n", md.lemmas.size());
		System.out.printf("featSets: %d%n", featSets.size());
		
	}

}
