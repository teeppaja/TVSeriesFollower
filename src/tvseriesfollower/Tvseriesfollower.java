package tvseriesfollower;

import java.util.concurrent.TimeUnit;

public class Tvseriesfollower {

    public static void main(String[] args) {
    	TorrentChecker torrentChecker = new TorrentChecker();
    	while (true) {
    		torrentChecker.checkNewStuff();
    		try {
				TimeUnit.MINUTES.sleep(45);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }	
}