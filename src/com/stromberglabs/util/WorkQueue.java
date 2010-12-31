package com.stromberglabs.util;

import java.util.LinkedList;

public class WorkQueue {
	private LinkedList<Runnable> queue;
	private int mNumThreads;
	private WorkerThread[] workers;
	private Integer mNumActiveThreads = 0;
	
	public WorkQueue(int numThreads){
		mNumThreads = numThreads;
		workers = new WorkerThread[mNumThreads];
		queue = new LinkedList<Runnable>();
	}
	
	public void start(){
		for ( int i = 0; i < mNumThreads; i++ ){
			workers[i] = new WorkerThread();
			workers[i].start();
		}
	}
	
	public void enqueue(Runnable r){
		synchronized(queue){
			queue.addLast(r);
			queue.notify();
		}
		//System.out.println("queue size: " + queue.size());
	}
	
	public int remainingItems(){
		return queue.size() + mNumActiveThreads;
	}
	
	public void stop(){
		for ( WorkerThread thread : workers ){
			thread.halt();
			thread.interrupt();
		}
	}
	
	private class WorkerThread extends Thread {
		private boolean running = true;
		
		public void halt(){
			running = false;
		}
		
		public void run(){
			Runnable r;
			while(running == true){
				synchronized(queue){
					while ( queue.isEmpty() ){
						try {
							queue.wait();
						} catch (InterruptedException e){
							if ( running == false ){
								return;
							}
						}
					}
					
					r = queue.removeFirst();
					//System.out.println(queue.size());
					//System.out.println("r was removed " + r);
				}
				try {
					synchronized(mNumActiveThreads){
						mNumActiveThreads++;
					}
					r.run();
					synchronized(mNumActiveThreads){
						mNumActiveThreads--;
					}
				} catch (RuntimeException e){
					e.printStackTrace();
				}
			}
		}
	}
}
