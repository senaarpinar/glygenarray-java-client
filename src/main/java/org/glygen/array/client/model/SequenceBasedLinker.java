package org.glygen.array.client.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.glygen.array.client.SequenceUtils;
import org.grits.toolbox.glycanarray.om.parser.cfg.CFGMasterListParser;

public class SequenceBasedLinker extends Linker {
	
    static {
        BuilderWorkspace glycanWorkspace = new BuilderWorkspace(new GlycanRendererAWT());
        glycanWorkspace.initData();
    }
	
	String sequence;
	
	public Map<Integer, Glycan> extractGlycans() {
		if (this.sequence == null || this.sequence.isEmpty())
			return null;
		Map<Integer, Glycan> positionMap = new HashMap<>();
		// extract all glycan sequences enclosed in { } and keep track of their position
		int position = 1; // start from 1
 		boolean start = false;
		Stack<Character> glycanStack = new Stack<Character>();
		for (int i=0; i < sequence.length(); i++) {
			if (sequence.charAt(i) == '}') {
				start = false;
				// pop characters from stack and try to parse the sequence as Glycan
				String glycanSequence = "";
				while (!glycanStack.isEmpty()) {
					glycanSequence = glycanStack.pop() + glycanSequence;
				}
				// get rid of amino acid at the end
				if (glycanSequence.contains("-")) {
					glycanSequence = glycanSequence.substring (0, glycanSequence.indexOf("-")-1);  // remove the linkage and the amino acid at the reducing end
				}
				// parse the sequence and create SequenceDefinedGlycan
				SequenceDefinedGlycan glycan = new SequenceDefinedGlycan();
				CFGMasterListParser parser = new CFGMasterListParser();
				String glycoCT =  parser.translateSequence(SequenceUtils.cleanupSequence(glycanSequence));
				glycan.setSequence(glycoCT);
				glycan.setSequenceType(GlycanSequenceFormat.GLYCOCT);
				positionMap.put(new Integer(position), glycan);
				position ++;
			}
			if (start) {
				glycanStack.push(new Character(sequence.charAt(i)));
			}	
			if (sequence.charAt(i) == '{') {
				start = true;
			}
		}
		return positionMap;
	}
	
	public String getSequence() {
		return sequence;
	}
	
	public void setSequence(String sequence) {
		if (sequence != null) sequence = sequence.replaceAll("\n", "").trim();
		this.sequence = sequence;
	}
}
