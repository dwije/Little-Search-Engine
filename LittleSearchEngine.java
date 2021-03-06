package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in 
	 * DESCENDING order of frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) throws FileNotFoundException {
		HashMap<String,Occurrence> result = new HashMap<String,Occurrence>();
		Scanner sc = new Scanner(new File(docFile));
		while (sc.hasNext()) {
			String word = sc.next();
			String keyWord = getKeyword(word);
			if (keyWord == null) {
				continue;
			}
			if (result.containsKey(keyWord)) {
				// This keyword has already been added to the HashMap.
				result.replace(keyWord, result.get(keyWord), new Occurrence(docFile, result.get(keyWord).frequency + 1));
			} else {
				// This keyword hasn't been added to the HashMap yet.
				result.put(keyWord, new Occurrence(docFile, 1));
			}
		}
		return result;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
		for(Map.Entry<String,Occurrence> set : kws.entrySet()) {
			if (keywordsIndex.containsKey(set.getKey())) {
				// This keyword has already been added to the master keywordsIndex HashMap. We need to add the occurrence to the ArrayList.
				keywordsIndex.get(set.getKey()).add(set.getValue());
				insertLastOccurrence(keywordsIndex.get(set.getKey()));
			} else {
				// This keyword has not been added to the master keywordsIndex HashMap yet.
				keywordsIndex.put(set.getKey(), new ArrayList<Occurrence>());
				keywordsIndex.get(set.getKey()).add(set.getValue());
				insertLastOccurrence(keywordsIndex.get(set.getKey()));
			}
		}
	}
	
	
	private String removePunctuation(String word, int startIndex) {
		String punctuations = ".,?:;!";
		for (int i=startIndex+1;i < word.length();i++) {
			if (Character.isLetter(word.charAt(i)) || !punctuations.contains(word.substring(i,i+1))) {
				return null;
			}
		}
		return word.substring(0, startIndex);
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be stripped
	 * So "word!!" will become "word", and "word?!?!" will also become "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	
	public String getKeyword(String word) {
		String result = "";
		word = word.toLowerCase();
		String punctuations = ".,?:;!";
		for (int i=0;i < word.length();i++) {
			if ((!Character.isLetter(word.charAt(i))) && (!punctuations.contains(word.substring(i,i+1)))) {
				return null;
			}
			if (punctuations.contains(word.substring(i,i+1))) {
				result = removePunctuation(word, i);
				if (result == null) {
					return null;
				} else break;
			}
		}
		if (result.contentEquals("")) {
			if (noiseWords.contains(word)) {
				return null;
			} else return word;
		}
		if (noiseWords.contains(result)) {
			return null;
		} return result;
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion is done by
	 * first finding the correct spot using binary search, then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		if(occs.size() == 1) {
			return null;
		}
		ArrayList<Integer> result = new ArrayList<Integer>();
		Occurrence temp = occs.get(occs.size()-1);
		occs.remove(occs.size()-1);
		int left = 0;
		int right = occs.size()-1;
		int mid = 0;
		while(left <= right) {
			mid = (left+right) / 2;
			result.add(mid);
			if (occs.get(mid).frequency == temp.frequency) {
				occs.add(mid, temp);
				return result;
			} else if(occs.get(mid).frequency > temp.frequency) {
				left = mid + 1;
			} else {
				right = mid - 1;
			}
		}
		if (temp.frequency < occs.get(mid).frequency) {
			occs.add(mid+1, temp);
		} else {
			occs.add(mid, temp);
		}
		return result;
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of document frequencies. 
	 * 
	 * Note that a matching document will only appear once in the result. 
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. 
	 * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
	 * frequency f1, then doc1 will take precedence over doc2 in the result. 
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all, result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matches, 
	 *         returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> result = new ArrayList<String>();
		if (keywordsIndex.containsKey(kw1) && keywordsIndex.containsKey(kw2)) {
			int a = 0;
			int b = 0;
			int n = 0;
			while(n < 5) {
				if ((a >= keywordsIndex.get(kw1).size()) && !(b >= keywordsIndex.get(kw2).size())) {
					if (result.contains(keywordsIndex.get(kw2).get(b).document)) {
						b++;
					} else {
						result.add(keywordsIndex.get(kw2).get(b).document);
						n++;
						b++;
					}
				} else if(!(a >= keywordsIndex.get(kw1).size()) && (b >= keywordsIndex.get(kw2).size())) {
					if (result.contains(keywordsIndex.get(kw1).get(a).document)) {
						a++;
					} else {
						result.add(keywordsIndex.get(kw1).get(a).document);
						n++;
						a++;
					}
				} else if((a >= keywordsIndex.get(kw1).size()) && (b >= keywordsIndex.get(kw2).size())) {
					return result;
				}
				else if (keywordsIndex.get(kw1).get(a).frequency == keywordsIndex.get(kw2).get(b).frequency) {
					if (keywordsIndex.get(kw1).get(a).document.equals(keywordsIndex.get(kw2).get(b).document)) {
						// Same document contains an equal frequency of matches.
						if (result.contains(keywordsIndex.get(kw1).get(a).document)) {
							a++;
							b++;
						} else {
							result.add(keywordsIndex.get(kw1).get(a).document);
							n++;
							a++;
							b++;
						}
					} else {
						// Two different documents with equal frequency of matches.
						if(result.contains(keywordsIndex.get(kw1).get(a).document)) {
							a++;
						} else {
							result.add(keywordsIndex.get(kw1).get(a).document);
							n++;
							a++;
						}
					}
				} else if(keywordsIndex.get(kw1).get(a).frequency > keywordsIndex.get(kw2).get(b).frequency) {
					if (result.contains(keywordsIndex.get(kw1).get(a).document)) {
						a++;
					} else {
						result.add(keywordsIndex.get(kw1).get(a).document);
						n++;
						a++;
					}
				} else if(keywordsIndex.get(kw1).get(a).frequency < keywordsIndex.get(kw2).get(b).frequency) {
					if (result.contains(keywordsIndex.get(kw2).get(b).document)) {
						b++;
					} else {
						result.add(keywordsIndex.get(kw2).get(b).document);
						n++;
						b++;
					}
				}
			}
		} else if(keywordsIndex.containsKey(kw1) && !keywordsIndex.containsKey(kw2)) {
			int i = 0;
			while (i <= 4 && i <= keywordsIndex.get(kw1).size()-1) {
				result.add(keywordsIndex.get(kw1).get(i).document);
				i++;
			}
		} else if(!keywordsIndex.containsKey(kw1) && keywordsIndex.containsKey(kw2)) {
			int i = 0;
			while (i <= 4 && i <= keywordsIndex.get(kw2).size()-1) {
				result.add(keywordsIndex.get(kw2).get(i).document);
				i++;
			}
		} else {
			return null;
		}
		return result;
	}
}