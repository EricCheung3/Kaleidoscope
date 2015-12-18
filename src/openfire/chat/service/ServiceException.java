package openfire.chat.service;

public class ServiceException extends Exception{

	/**
	 * User social interactions servce exception.
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceException(String msg){
		super(msg);
	}
}
