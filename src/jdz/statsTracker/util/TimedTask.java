
package jdz.statsTracker.util;

import org.bukkit.scheduler.BukkitRunnable;

import jdz.statsTracker.main.Main;

public class TimedTask {
	private boolean isRunning = false;
	private final BukkitRunnable runnable;
	private final int time;
	
	public TimedTask(int time, Task t){
		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				t.execute();
			}
		};
		this.time = time;
	}
	
	public void run(){
		runnable.run();
	}
	
	public void start() {
		if (!isRunning) {
			runnable.runTaskTimerAsynchronously(Main.plugin, 0, time);
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
