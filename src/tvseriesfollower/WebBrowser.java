package tvseriesfollower;

import java.io.IOException;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WebBrowser {
	
	public String getTorrentPageSource(String domain, Serie serie) {
		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		String pageSource = null;
		String url = domain + "/usearch/" + serie.getName().toLowerCase() + " " + serie.getStringifiedSeasonAndEpisode() + "/?field=seeders&sorder=desc";
		
		WebClient webClient = new WebClient();
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
	    webClient.getOptions().setPrintContentOnFailingStatusCode(false);
	    webClient.getOptions().setThrowExceptionOnScriptError(false);
	    webClient.getOptions().setCssEnabled(false);
	    webClient.getOptions().setJavaScriptEnabled(false);
	    webClient.getOptions().setGeolocationEnabled(false);
	    webClient.getOptions().setDoNotTrackEnabled(true);
	    webClient.getOptions().setPopupBlockerEnabled(true);
	    webClient.getOptions().setRedirectEnabled(false);
		webClient.setRefreshHandler(new ThreadedRefreshHandler());
		
		try {
		    int status = webClient.getPage(url).getWebResponse().getStatusCode();
		    if (status >= 200 && status <= 299) {
		    	pageSource = getPageSource(webClient.getPage(url));
		    }
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		webClient.close();
		
		return pageSource;
	}
	
	

	private String getPageSource(Page page) {
		if(page instanceof HtmlPage) {
			return ((HtmlPage)page).asXml();
		} else if(page instanceof JavaScriptPage) {
			return ((JavaScriptPage)page).getContent();
		} else if(page instanceof TextPage) {
			return ((TextPage)page).getContent();
		} else {
			return ((UnexpectedPage)page).getWebResponse().getContentAsString();
		}
	}
	
}
