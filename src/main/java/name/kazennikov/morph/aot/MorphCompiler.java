package name.kazennikov.morph.aot;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.InternalFrameAdapter;
import javax.xml.bind.JAXBException;

import org.omg.CORBA.FREE_MEM;

import name.kazennikov.dafsa.CharFSA;
import name.kazennikov.dafsa.CharTrie;
import name.kazennikov.dafsa.GenericFSA;
import name.kazennikov.dafsa.GenericTrie;
import name.kazennikov.dafsa.IntFSA;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class MorphCompiler {
	
	public static class GuesserEnding {
		TIntList ending;
		int feats;
		
		public GuesserEnding(TIntList ending, int feats) {
			super();
			this.ending = ending;
			this.feats = feats;
		}
		
		@Override 
		public int hashCode() {
			return Objects.hashCode(ending, feats);
		}
		
		@Override
		public boolean equals(Object other) {
			if(!(other instanceof GuesserEnding))
				return false;
			
			GuesserEnding o = (GuesserEnding) other;
			
			return Objects.equal(ending, o.ending) && Objects.equal(feats, o.feats);
		}

		public TIntList getEnding() {
			return ending;
		}

		public int getFeats() {
			return feats;
		}
		
		
		
		
	}
	
	MorphLanguage morphLanguage;
	
	public MorphCompiler(MorphLanguage morphLanguage) {
		this.morphLanguage = morphLanguage;	
	}
	
	public void compile(MorphDict md, int predictionDepth, int predictionFreq, File dest) throws IOException {
		
		CharFSA fsa = new CharFSA(new CharFSA.SimpleNode());
		IntFSA fst = new IntFSA(new IntFSA.SimpleNode());
		
		IntFSA fstGuesser = new IntFSA(new IntFSA.SimpleNode());
        
        TObjectIntHashMap<BitSet> featSets = new TObjectIntHashMap<BitSet>();

        int wfNum = 0;
        int lemmaNum = 0;
        Multiset<String> predictionSet = HashMultiset.create();
        Multiset<GuesserEnding> fstPredictionSet = HashMultiset.create();
        TIntArrayList fstSeq = new TIntArrayList(32);
        // main loop over the dictionary
        long elapsed = System.currentTimeMillis();
		for(MorphDict.Lemma lemma : md.getLemmas()) {
	        lemmaNum++;
			for(MorphDict.WordForm wf : lemma.expand()) {
				BitSet feats = morphLanguage.getWordFormFeats(wf.getFeats(), wf.getCommonAnCode());
				int featId = featSets.get(feats);
				
				if(featId == 0) {
					featId = featSets.size() + 1;
					featSets.put(feats, featId);
				}
				
				wfNum++;
				
				final String wordForm = wf.getWordForm();
				expand(fstSeq, wf.getWordForm(), wf.getLemma());
				
				predictionSet.add(new String(wordForm.substring(wordForm.length() > 5? wordForm.length() - 1 - 5 : 0)));
				fsa.addMinWord(wordForm, featId);
				fst.addMinWord(fstSeq, featId);
				
				TIntList ending = fstSeq.subList(fstSeq.size() > predictionDepth? fstSeq.size() - predictionDepth - 1: 0, fstSeq.size());
				ending.reverse();
				fstPredictionSet.add(new GuesserEnding(ending, featId));
			}
		}
		
	
		elapsed = System.currentTimeMillis() - elapsed;
		System.out.printf("Elapsed on fsa building:%d ms%n", elapsed);

		
		for(Multiset.Entry<GuesserEnding> e : fstPredictionSet.entrySet()) {
			if(e.getCount() < predictionFreq)
				continue;
			
			
			fstGuesser.addMinWord(e.getElement().getEnding(), e.getElement().getFeats());
		}

		elapsed = System.currentTimeMillis();
		write(dest, featSets, fsa, fst, fstGuesser);
		elapsed = System.currentTimeMillis() - elapsed;
		
		System.out.printf("Elapsed on writing:%d ms%n", elapsed);
	}
	
	private void write(File dest,TObjectIntHashMap<BitSet> featSets, CharFSA fsa,	IntFSA fst, IntFSA fstGuesser) throws IOException {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dest)));
			write(dos, featSets);
			fsa.write(new CharFSAWriter(dos));
			fst.write(new IntFSAWriter(dos));
			fstGuesser.write(new IntFSAWriter(dos));
		} finally {
			if(dos != null)
				dos.close();
		}
		
	}

	public void write(DataOutputStream s, TObjectIntHashMap<BitSet> featSets) throws IOException {
		final BitSet[] sets = new BitSet[featSets.size() + 1];
		sets[0] = new BitSet();
		featSets.forEachEntry(new TObjectIntProcedure<BitSet>() {
			@Override
			public boolean execute(BitSet a, int b) {
				sets[b] = a;
				return true;
			}
		});

		s.writeInt(sets.length);

		for(BitSet featSet : sets) {
			s.writeInt(featSet.cardinality());
			for(int i = 0; i != featSet.length(); i++) {
				if(featSet.get(i)) {
					s.writeInt(i);
				}
			}
		}
	}
	
	public static class IntFSAWriter implements IntFSA.Events { 
		DataOutputStream s;

		public IntFSAWriter(DataOutputStream s) {
			this.s = s;
		}

		@Override
		public void states(int states) throws IOException {
			s.writeInt(states);
		}

		@Override
		public void state(int state) throws IOException {
			s.writeInt(state);
		}

		@Override
		public void finals(int n) throws IOException {
			s.writeInt(n);
		}

		@Override
		public void stateFinal(int fin) throws IOException {
			s.writeInt(fin);
		}

		@Override
		public void transitions(int n) throws IOException {
			s.writeInt(n);
			
		}

		@Override
		public void transition(int input, int dest) throws IOException {
			s.writeInt(input);
			s.writeInt(dest);
		}
	}
	
	public static class CharFSAWriter implements CharFSA.Events { 
		DataOutputStream s;

		public CharFSAWriter(DataOutputStream s) {
			this.s = s;
		}

		@Override
		public void states(int states) throws IOException {
			s.writeInt(states);
		}

		@Override
		public void state(int state) throws IOException {
			s.writeInt(state);
		}

		@Override
		public void finals(int n) throws IOException {
			s.writeInt(n);
		}

		@Override
		public void stateFinal(int fin) throws IOException {
			s.writeInt(fin);
		}

		@Override
		public void transitions(int n) throws IOException {
			s.writeInt(n);
			
		}

		@Override
		public void transition(char input, int dest) throws IOException {
			s.writeInt(input);
			s.writeInt(dest);
		}
	}

	
    
    public static void expand(TIntArrayList dest, String wf, String lemma) {
    	dest.clear();
    	
    	for(int i = 0; i != Math.max(wf.length(), lemma.length()); i++) {
    		char wfch = i < wf.length()? wf.charAt(i) : 0;
    		char lmch = i < lemma.length()? lemma.charAt(i) : 0;
    		int label = wfch;
    		label <<= 16;
    		label += lmch;
    		dest.add(label);
    	}
    }
    
    public static void expand(TIntArrayList dest, String wf) {
    	dest.clear();
    	
    	for(int i = 0; i != wf.length(); i++) {
    		dest.add(wf.charAt(i));
    	}
    }
    
	public static void main(String[] args) throws JAXBException, IOException {
		MorphConfig mc = MorphConfig.newInstance(new File("russian.xml"));
		
		MorphLanguage ml = new MorphLanguage(mc);
		
		long st = System.currentTimeMillis();
		MorphDict md = ml.readDict();
		
		MorphCompiler morphCompiler = new MorphCompiler(ml);
		
		morphCompiler.compile(md, 5, 0, new File("russian.dat"));
		
	}
	
	

}
