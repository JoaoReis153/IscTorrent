package Services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DownloadTasksManager {
	private final int DEFAULT_MAX_THREADS = 5;
	ExecutorService threadPool;

	public DownloadTasksManager(int max_threads) {
		this.threadPool = Executors.newFixedThreadPool(max_threads);
	}
	
	public DownloadTasksManager() {
		this.threadPool = Executors.newFixedThreadPool(DEFAULT_MAX_THREADS);
	}

	
}
