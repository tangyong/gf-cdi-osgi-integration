package org.acme.stockquoteservice.api;

public class StockQuoteUnavailableException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public StockQuoteUnavailableException() {
        super();
    }
	
	public StockQuoteUnavailableException(String msg) {
        super(msg);
    }
	
	public StockQuoteUnavailableException(String msg, Throwable cause) {
        super(msg,  cause);
    } 
}
