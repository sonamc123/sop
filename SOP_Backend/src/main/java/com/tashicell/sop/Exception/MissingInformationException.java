package com.tashicell.sop.Exception;

public class MissingInformationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public MissingInformationException(String message) {
		super(message);
	}
}
