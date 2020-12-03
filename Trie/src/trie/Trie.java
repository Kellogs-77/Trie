package trie;

import java.util.ArrayList;

/**
 * This class implements a Trie. 
 * 
 * @author Sesh Venugopal
 *
 */
public class Trie {
	
	// prevent instantiation
	private Trie() { }
	
	/**
	 * Builds a trie by inserting all words in the input array, one at a time,
	 * in sequence FROM FIRST TO LAST. (The sequence is IMPORTANT!)
	 * The words in the input array are all lower case.
	 * 
	 * @param allWords Input array of words (lowercase) to be inserted.
	 * @return Root of trie with all words inserted from the input array
	 */
	public static TrieNode buildTrie(String[] allWords) {
		//make root
		TrieNode root = new TrieNode(null, null, null);
		if(allWords.length == 0) { //if empty array is given
			return root;
		}
		Indexes childIndex = new Indexes(0, (short)0, (short)(allWords[0].length()-1)); //make first child index
		TrieNode firstChild = new TrieNode(childIndex, null, null);
		root.firstChild = firstChild;
		int sharedOrNot = -1;
		int startIndex = 0;
		int endIndex = 0;
		int wordIndex = 0;
		TrieNode ptr = firstChild;
		TrieNode prev = root;
		for(int i = 1; i < allWords.length; i++) { //searches through rest of array and creates rest of tree
			String word = allWords[i];

			while(ptr != null) {//goes through entire trie to find match
				
				startIndex = ptr.substr.startIndex;
				endIndex = ptr.substr.endIndex;
				wordIndex = ptr.substr.wordIndex;
				String subWord = allWords[wordIndex].substring(startIndex, endIndex+1);
				sharedOrNot = sharePrefix(subWord, word.substring(startIndex));
				
				if(sharedOrNot !=-1) {
					sharedOrNot += startIndex;
				}
				
				if(sharedOrNot == -1) {
					prev = ptr;
					ptr = ptr.sibling;
				}
				
				else if(sharedOrNot >= endIndex) {
					prev = ptr;
					ptr = ptr.firstChild;
				}
				
				else if (sharedOrNot < endIndex){ //Partial match
					prev = ptr;
					break;
				}
				
			}
			if(ptr == null) {
				Indexes indexes = new Indexes(i, (short)startIndex, (short)(word.length()-1));
				prev.sibling = new TrieNode(indexes, null, null);
				
			}
			else { //need to figure out why duplicates are happening
				
				Indexes prevIndex = prev.substr;
				TrieNode prevFirstChild = prev.firstChild;
				
				Indexes newIndexes = new Indexes(prevIndex.wordIndex, (short)(sharedOrNot+1), prevIndex.endIndex);
				prevIndex.endIndex = (short)(sharedOrNot);
				
				TrieNode newFirstChild = new TrieNode(newIndexes, null, null);
				prev.firstChild = newFirstChild;
				newFirstChild.firstChild = prevFirstChild;
				
				Indexes newSibIndexes = new Indexes(i, (short)(sharedOrNot+1), (short)(word.length()-1));
				TrieNode newInsSibling = new TrieNode(newSibIndexes, null, null);
				prev.firstChild.sibling = newInsSibling;	


			}
			ptr = firstChild;
			prev = root;
			sharedOrNot = -1;
		}
		
	
		
		return root;
	}
	
	private static int sharePrefix(String nodeWord, String newWord) { //sees how big a prefix is for a given node and word
		
		int prefixIndex = 0;
		int maxLength = Math.min(nodeWord.length(), newWord.length());
		for(int i = 0; i < maxLength; i++) {
			if(nodeWord.charAt(i)==newWord.charAt(i)) {
				prefixIndex++;
			}
			else {
				break;
			}
		}
		
		return (prefixIndex-1);
		
	}
	
	/**
	 * Given a trie, returns the "completion list" for a prefix, i.e. all the leaf nodes in the 
	 * trie whose words start with this prefix. 
	 * For instance, if the trie had the words "bear", "bull", "stock", and "bell",
	 * the completion list for prefix "b" would be the leaf nodes that hold "bear", "bull", and "bell"; 
	 * for prefix "be", the completion would be the leaf nodes that hold "bear" and "bell", 
	 * and for prefix "bell", completion would be the leaf node that holds "bell". 
	 * (The last example shows that an input prefix can be an entire word.) 
	 * The order of returned leaf nodes DOES NOT MATTER. So, for prefix "be",
	 * the returned list of leaf nodes can be either hold [bear,bell] or [bell,bear].
	 *
	 * @param root Root of Trie that stores all words to search on for completion lists
	 * @param allWords Array of words that have been inserted into the trie
	 * @param prefix Prefix to be completed with words in trie
	 * @return List of all leaf nodes in trie that hold words that start with the prefix, 
	 * 			order of leaf nodes does not matter.
	 *         If there is no word in the tree that has this prefix, null is returned.
	 */
	public static ArrayList<TrieNode> completionList(TrieNode root,
										String[] allWords, String prefix) {
		ArrayList<TrieNode> completedList = new ArrayList<TrieNode>();
		int prefixStartIndex = prefixIndexLocator(allWords, prefix);
		int prefixEndIndex = 0;
		for(String s : allWords) {
			int maxIndex = sharePrefix(s, prefix);
			if( maxIndex > prefixEndIndex) {
				prefixEndIndex = sharePrefix(s, prefix);
			}
		}
		TrieNode ptr = root.firstChild;
		TrieNode prev = root;
		while(ptr!=null) {
			if(ptr.substr.wordIndex == prefixStartIndex) {
				if(ptr.substr.endIndex > prefixEndIndex) {
					break;
				}
				else {
					prev = ptr;
					ptr = ptr.firstChild;
					while(ptr!=null) {
						int wordIndex = ptr.substr.wordIndex;
						int newShared = sharePrefix(allWords[wordIndex], prefix);
						if(newShared == prefixEndIndex) {
							break;
						}
						else {
							prev = ptr;
							ptr = ptr.sibling;
						}
					}
					break;
				}
				
			}
			else {
				prev = ptr;
				ptr = ptr.sibling;
			}
		}
		if(ptr.firstChild == null) {
			completedList.add(ptr);
			if(ptr.sibling!=null) {
				int sibShared = sharePrefix(allWords[ptr.sibling.substr.wordIndex], prefix);
				if(sibShared == prefixEndIndex) {
					ptr = ptr.sibling;
					addAllSibs(completedList, ptr);
				}
			}
			return completedList;
		}
		else {
			prev = ptr;
			ptr = ptr.firstChild;
			if(ptr.firstChild!=null) {
				while(ptr.firstChild!=null) {
					prev = ptr;
					ptr = ptr.firstChild;
				}
			}
			
			String prefixInNode = allWords[ptr.substr.wordIndex].substring(0, prefix.length());
			if (prefixInNode.equals(prefix)){
				addAllSibs(completedList, ptr);
			}
			else {
				while(prev!=null) {
					addAllSibs(completedList, ptr);
					prev = prev.sibling;
					if(prev == null) {
						break;
					}
					ptr = prev.firstChild;
				}
			}
		}

			
		
		
		
		
		return completedList;
	}

	
	private static int prefixIndexLocator(String[] words, String prefix) {
		int index = -1;
		for(int i = 0; i < words.length; i++) {
			if(prefix.charAt(0) == words[i].charAt(0)) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	private static void addAllSibs(ArrayList<TrieNode> list, TrieNode node) {
		while(node!=null) {
			list.add(node);
			node = node.sibling;
		}
	}
	
	public static void print(TrieNode root, String[] allWords) {
		System.out.println("\nTRIE\n");
		print(root, 1, allWords);
	}
	
	private static void print(TrieNode root, int indent, String[] words) {
		if (root == null) {
			return;
		}
		for (int i=0; i < indent-1; i++) {
			System.out.print("    ");
		}
		
		if (root.substr != null) {
			String pre = words[root.substr.wordIndex]
							.substring(0, root.substr.endIndex+1);
			System.out.println("      " + pre);
		}
		
		for (int i=0; i < indent-1; i++) {
			System.out.print("    ");
		}
		System.out.print(" ---");
		if (root.substr == null) {
			System.out.println("root");
		} else {
			System.out.println(root.substr);
		}
		
		for (TrieNode ptr=root.firstChild; ptr != null; ptr=ptr.sibling) {
			for (int i=0; i < indent-1; i++) {
				System.out.print("    ");
			}
			System.out.println("     |");
			print(ptr, indent+1, words);
		}
	}
 }
