package org.strategoxt.imp.runtime.services;

import org.eclipse.core.runtime.IProgressMonitor;

public class StrategoProgressMonitor {

	IProgressMonitor monitor;
	int progress = 0;
	int tasks = 0;
	int currentTask = 0;
	
	public static final int TOTAL_WORK = Integer.MAX_VALUE;
	
	public StrategoProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
		monitor.beginTask("", TOTAL_WORK);
	}
	
	public void setPercentage(int p) {
		if (p<0 || p>100) return;
		
		int work = (TOTAL_WORK / 100) * p; 
		setWorkInternal(work);	
	}
	
	private void setWorkInternal(int work) {
		int diff = work - progress;
		if (diff <= 0) return;
		this.progress = work;
		monitor.worked(diff);
	}
	
	public void setSubTasks(int tasks) {
		this.tasks = tasks;
		updateTaskWork();
	}
	
	public void completeTask() {
		if (currentTask >= tasks) return;
		this.currentTask ++;
		updateTaskWork();
	}
	
	private void updateTaskWork() {
		int work = tasks == 0 ? 0 : (TOTAL_WORK / tasks) * currentTask;
		setWorkInternal(work);
	}
	
}
