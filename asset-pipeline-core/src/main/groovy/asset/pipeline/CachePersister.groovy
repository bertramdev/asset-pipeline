package asset.pipeline

/**
* Receives asynchronous cache persistance requests and executes them.
* Also acts as a debouncer
*/
public class CachePersister extends Thread {
	public Integer delay = 0
	public Boolean ran = true
	public final Integer RUN_DELAY = 1000
	public void run() {
		while(true) {
			sleep(RUN_DELAY)
			if(ran = false) {
				delay -= RUN_DELAY
				if(delay <= 0) {
					CacheManager.save()
					ran = true
				}
			}
			
		}
	}

	public void debounceSave(Integer delay) {
		this.delay = delay
		ran = false
	}
}