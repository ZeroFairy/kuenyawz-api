package dev.realtards.kuenyawz.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {
	public InvalidRefreshTokenException() {
		super("Invalid refresh token");
	}
}
