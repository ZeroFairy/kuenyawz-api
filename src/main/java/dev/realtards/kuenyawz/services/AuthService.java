package dev.realtards.kuenyawz.services;

import dev.realtards.kuenyawz.dtos.account.AccountRegistrationDto;
import dev.realtards.kuenyawz.dtos.account.AccountSecureDto;
import dev.realtards.kuenyawz.dtos.auth.AuthRequestDto;
import dev.realtards.kuenyawz.dtos.auth.AuthResponseDto;

public interface AuthService {
	/**
	 * Registers a new user account.
	 *
	 * @param accountRegistrationDto Registration information
	 * @return {@link AuthResponseDto} with full tokens and account information
	 */
	AuthResponseDto register(AccountRegistrationDto accountRegistrationDto);

	/**
	 * Authenticates a user credentials and returns an access token with
	 * refresh token.
	 *
	 * @param accountLoginDto Login information
	 * @return {@link AuthResponseDto} with full tokens and account information
	 */
	AuthResponseDto login(AuthRequestDto accountLoginDto);

	/**
	 * Refreshes an active access token with refresh token.
	 *
	 * @param token The refresh token to use
	 * @return {@link AuthResponseDto} with new access token
	 */
	AuthResponseDto refresh(String token);

	/**
	 * Revokes an active access token.
	 *
	 * @param token The token to revoke
	 */
	void revokeAccessToken(String token);

	/**
	 * Revokes an active refresh token.
	 *
	 * @param token The token to revoke
	 */
	void revokeRefreshToken(String token);

	/**
	 * Returns the corresponding user information of the token.
	 *
	 * @param token The token to extract user information from
	 * @return {@link AccountSecureDto} user information
	 */
	AccountSecureDto getUserInfo(String token);

	/**
	 * Validate a token to check whether it has expired or not.
	 *
	 * @param token The token to validate
	 * @return {@code true} if the token is valid, {@code false} otherwise
	 */
	boolean validateToken(String token);
}
