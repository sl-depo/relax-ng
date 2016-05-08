package test.util;

public class StopWatch {
	private Long startTime;
	private Long endTime;
	
	private String name;
	
	public StopWatch(String name, boolean start) {
		this.name = name;
		if (start) {
			startTime = System.nanoTime();
		}
	}
	
	public void start() {
		if (startTime != null) {
			throw new IllegalStateException();
		}
		startTime = System.nanoTime();
	}
	
	public void stop() {
		if (endTime != null) {
			throw new IllegalArgumentException();
		}
		endTime = System.nanoTime();
	}
	
	@Override
	public String toString() {
		Long eTime = endTime == null ? System.nanoTime() : endTime;
		Long time = eTime - startTime;
		String timeStr = String.format("* %f ms\n", ((double)time / (double)1000000));
		
		String out = "\n**************************\n";
		out += "* SW '" + name + "' " + (endTime == null ? "[running]" : "[stopped]") + "\n";
		out += timeStr;
		out += "**************************\n";
		
		return out;
	}
}
