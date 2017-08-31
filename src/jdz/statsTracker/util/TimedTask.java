
package jdz.statsTracker.util;

import org.bukkit.scheduler.BukkitRunnable;

import jdz.statsTracker.main.Main;

public class TimedTask {
	private boolean isRunning = false;
	private final BukkitRunnable runnable;
	private int nextDuration = 0;

	public TimedTask(int minTime, int maxTime, Task t){
		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (nextDuration-- == 0){
					t.execute();
					nextDuration = (int)(Math.random()*(maxTime-minTime)+minTime);
				}
			}
		};
	}
	
	public TimedTask(int time, Task t){
		this(time, time, t);
	}
	
	public void run(){
		runnable.run();
	}
	
	public void start() {
		if (!isRunning) {
			runnable.runTaskTimerAsynchronously(Main.plugin, 0, 1);
			isRunning = true;
		}
	}

	public void stop() {
		if (!isRunning) {
			runnable.cancel();
			isRunning = false;
		}
	}
	
	public boolean isRunning(){
		return isRunning;
	}

	public interface Task{
		public void execute();
	}
}
