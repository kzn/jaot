package name.kazennikov.morph.aot;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

/**
 * Paradigm - set of word inflections modifiers.
 * 
 * The first inflection modifier is the modifier for the normal form.
 * 
 * @author Anton Kazennikov
 *
 */
public class Paradigm {
	
	/**
	 * Single paradigm entry: 
	 * <ul>
	 * <li> prefix of the stem
	 * <li> ending (suffix) for the stem
	 * <li> attached morphological features
	 * </ul>
	 * @author Anton Kazennikov
	 *
	 */
	public static class Entry {
		String ending;
		String prefix;
		GramTable.Record rec;
		
		public Entry() {
			
		}
		
		public Entry(String ending, String prefix, GramTable.Record rec) {
			this.ending = ending;
			this.prefix = prefix;
			this.rec = rec;
		}
		
		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("ending", ending)
					.add("grammems", rec)
					.add("prefix", prefix)
					.toString();
		}

		public String getEnding() {
			return ending;
		}

		public String getPrefix() {
			return prefix;
		}
		
		public boolean hasPrefix() {
			return prefix != null;
		}

		public GramTable.Record getRec() {
			return rec;
		}
		
		
	}
	
	List<Paradigm.Entry> entries = new ArrayList<Paradigm.Entry>();
	
	public Paradigm() {
		
	}
	
	public void addEntry(String ending, String prefix, GramTable.Record rec) {
		entries.add(new Entry(ending, prefix, rec));
	}
	
	public int size() {
		return entries.size();
	}
	
	public Paradigm.Entry getNormal() {
		return entries.get(0);
	}
	
	public Paradigm.Entry get(int index) {
		return entries.get(index);
	}
	
	@Override
	public String toString() {
		return String.format("Paradigm:%s", entries);
	}

    public List<MorphDict.WordForm> expand(String stem, String lemma, GramTable.Record commonFeats, boolean processYo) {
        List<MorphDict.WordForm> result = new ArrayList<MorphDict.WordForm>();
        StringBuilder sb = new StringBuilder(stem.length());

        for(int i = 0; i != size(); i++) {
            sb.setLength(0);
            Paradigm.Entry parEntry = get(i);

            GramTable.Record feats = parEntry.getRec();

            if(parEntry.hasPrefix()) {
                sb.append(parEntry.getPrefix());
            }

            sb.append(stem);
            sb.append(parEntry.getEnding());
            String wf = sb.toString();
            result.add(new MorphDict.WordForm(wf, lemma, feats, commonFeats));


            if(processYo && wf.indexOf("Ё") != -1) {
                wf = wf.replace('Ё', 'Е');
                result.add(new MorphDict.WordForm(wf, lemma, feats, commonFeats));
            }
        }

        return result;
    }

}
