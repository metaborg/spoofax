package org.strategoxt.imp.runtime.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.strategoxt.imp.runtime.RuntimeActivator;
import org.strategoxt.imp.runtime.SpoofaxPreferencePage;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;

public class EditScenarioCollector {
	
	private int editNumber;
	private String editSession;
	private ITokenizer oldTokens;
	
	public EditScenarioCollector(){
		oldTokens = null;
		startNewEditSession();
	}

	public void startNewEditSession() {
		editNumber = 1;
		editSession = UUID.randomUUID().toString();
	}
	
	public void collectEditorFile(IStrategoTerm parseResult, int cursorLoc) {
		IPreferenceStore store = RuntimeActivator.getInstance().getPreferenceStore();
		if(!store.getBoolean(SpoofaxPreferencePage.COLLECT_EDIT_SCENARIOS)){
			return;
		}
		
		final ITokenizer tokens = ImploderAttachment.getTokenizer(parseResult);
		if (hasTokenChanges(tokens, oldTokens)) {
			int editDistance = editDistance(tokens, oldTokens);
			int nrOfSyntaxErrors = countSyntaxErrors(tokens);
			final IResource resource = SourceAttachment.getResource(parseResult);
			if (resource == null)
				return;
			String editNumberString = ""+editNumber;
			for (int i = editNumberString.length(); i < 5; i++) {
				editNumberString = "0" + editNumberString;
			}
			final String fileName = "edit_" + editNumberString + "_err_" + nrOfSyntaxErrors + "_dist_"+ editDistance + "_cursor_" + cursorLoc +"." + resource.getFileExtension() + ".scn";
			final IPath projectPath = resource.getProject().getLocation();
			final IPath editSessionPath = projectPath.append("edit-scenarios").append(editSession);
			try {
				if (!editSessionPath.toFile().exists()) {
					editSessionPath.toFile().mkdirs();
				}
				IPath filePath = editSessionPath.append(fileName).removeTrailingSeparator();
				File file = filePath.toFile();
				assert (!file.exists());
				file.createNewFile();
				PrintStream prs = new PrintStream(file);
				prs.print(tokens.getInput());
				prs.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			oldTokens = tokens;
			editNumber ++;
			if(editNumber > 10000){
				startNewEditSession();
			}
		}
	}

	public int countSyntaxErrors(final ITokenizer tokens) {
		int nrOfSyntaxErrors = 0;
		for (int i = 0; i < tokens.getTokenCount(); i++) {
			String error = tokens.getTokenAt(i).getError();
			if (error != null && !error.startsWith(ITokenizer.ERROR_WARNING_PREFIX)) {
				nrOfSyntaxErrors += 1;
			}
		}
		return nrOfSyntaxErrors;
	}

	public boolean hasTokenChanges(ITokenizer tokens, ITokenizer oldTokens) {
		if(tokens == null)
			return false;
		if(oldTokens == null)
			return true;
		int tokIndex = 0;
		int oldTokIndex = 0;
		while (tokIndex < tokens.getTokenCount() && oldTokIndex < oldTokens.getTokenCount()) {
			oldTokIndex = skipLayoutTokens(oldTokens, oldTokIndex);
			tokIndex = skipLayoutTokens(tokens, tokIndex);
			assert(tokIndex < tokens.getTokenCount() && oldTokIndex < oldTokens.getTokenCount());
			if (oldTokens.getTokenAt(oldTokIndex).getKind() == tokens.getTokenAt(tokIndex).getKind()){
				tokIndex ++; 
				oldTokIndex++;
			}
			else{
				return true;
			}
		}
		return !isLayoutSuffix(tokens, tokIndex) || !isLayoutSuffix(oldTokens, oldTokIndex); 
	}
	
	public int editDistance(ITokenizer tokens, ITokenizer oldTokens){
		if(tokens == null || oldTokens == null){
			return -1;
		}
		String input = tokens.getInput().replaceAll("[ \t\n\f\r]+", "");
		String inputOld = oldTokens.getInput().replaceAll("[ \t\n\f\r]+", "");
		int startIndexDiff = 0;
		while(
			startIndexDiff < input.length() && 
			startIndexDiff < inputOld.length() &&
			input.charAt(startIndexDiff) == inputOld.charAt(startIndexDiff)
		){
			startIndexDiff++;
		}		
		int endIndexDiff = input.length()-1;
		int endIndexDiffOld = inputOld.length()-1;
		while(
			endIndexDiff >= startIndexDiff && 
			endIndexDiffOld >= startIndexDiff &&
			input.charAt(endIndexDiff) == inputOld.charAt(endIndexDiffOld)
		){
			endIndexDiff--;
			endIndexDiffOld--;
		}
		return Math.max(endIndexDiff - startIndexDiff, endIndexDiffOld - startIndexDiff) + 1;
	}
	

	/*
	public int editDistance(ITokenizer tokens, ITokenizer oldTokens){
		if(tokens == null || oldTokens == null)
			return -1;
		
		//Find DiffStart
		int tokIndex = 0;
		int oldTokIndex = 0;
		int diffStart = -1;
		int oldDiffStart = -1;		
		while (tokIndex < tokens.getTokenCount() && oldTokIndex < oldTokens.getTokenCount()) {
			oldTokIndex = skipLayoutTokens(oldTokens, oldTokIndex);
			tokIndex = skipLayoutTokens(tokens, tokIndex);
			assert(tokIndex < tokens.getTokenCount() && oldTokIndex < oldTokens.getTokenCount());
			if (oldTokens.getTokenAt(oldTokIndex).toString().equals(tokens.getTokenAt(tokIndex).toString())){
			//if (oldTokens.getTokenAt(oldTokIndex).getKind() == tokens.getTokenAt(tokIndex).getKind()){
				tokIndex ++; 
				oldTokIndex++;
			}
			else{
				diffStart = tokIndex;
				oldDiffStart = oldTokIndex;
				break;
			}
		}
		if(diffStart == -1){
			assert(oldDiffStart == -1);
			diffStart = skipLayoutTokens(tokens, tokIndex);
			oldDiffStart = skipLayoutTokens(oldTokens, oldTokIndex);
		}		
		
		//Find Diff End
		int diffEnd = diffStart;
		int oldDiffEnd = oldDiffStart;
		tokIndex = tokens.getTokenCount()-1;
		oldTokIndex = oldTokens.getTokenCount()-1;
		while (tokIndex >= diffStart && oldTokIndex >= oldDiffStart) {
			oldTokIndex = skipLayoutTokensBackWards(oldTokens, oldTokIndex);
			tokIndex = skipLayoutTokensBackWards(tokens, tokIndex);
			assert(tokIndex >= 0 && oldTokIndex >=0);
			if (oldTokens.getTokenAt(oldTokIndex).toString().equals(tokens.getTokenAt(tokIndex).toString())){
			//if (oldTokens.getTokenAt(oldTokIndex).getKind() == tokens.getTokenAt(tokIndex).getKind()){
				tokIndex --; 
				oldTokIndex--;
			}
			else{
				diffEnd = tokIndex;
				oldDiffEnd = oldTokIndex;
				break;
			}
		}
		
		//Return edit distance defined as ... 
		return Math.max(diffEnd-diffStart, oldDiffEnd-oldDiffStart) + 1;
	}*/
	
	public int skipLayoutTokens(ITokenizer tokens, int tokIndex) {
		while (
				tokIndex < tokens.getTokenCount()-1 &&
				(
					tokens.getTokenAt(tokIndex).getKind() == IToken.TK_LAYOUT 
					//|| tokens.getTokenAt(tokIndex).getEndOffset()<= tokens.getTokenAt(tokIndex).getStartOffset()
				)
			){
			tokIndex ++;
		}
		return tokIndex;
	}

	public int skipLayoutTokensBackWards(ITokenizer tokens, int tokIndex) {
		while (
			tokIndex > 0 && (
				tokens.getTokenAt(tokIndex).getKind() == IToken.TK_LAYOUT) 
				//|| tokens.getTokenAt(tokIndex).getEndOffset()<= tokens.getTokenAt(tokIndex).getStartOffset()
		){
			tokIndex --;
		}
		return tokIndex;
	}

	public boolean isLayoutSuffix(final ITokenizer tokens, int tokIndex) {
		while(tokIndex < tokens.getTokenCount()){
			if(tokens.getTokenAt(tokIndex).getKind() != IToken.TK_LAYOUT && tokens.getTokenAt(tokIndex).getKind() != IToken.TK_EOF)
				return false;
		}
		return true;
	}
}
