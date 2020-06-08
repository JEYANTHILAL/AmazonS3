package com.example.aws.services;

import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;

public interface AuthenticationInterface {

	void createNewUser(UserInfo userInfo) throws Exception;

	void deleteUser(String userName, String password) throws Exception;

	UserInfo findUserByEmailAddr(final String email) throws Exception;

	LoginInfo userLogin(String userName, String password) throws Exception;

	void userLogout(String userName) throws Exception;

	public void changeFromTemporaryPassword(final PasswordRequest passwordRequest) throws Exception;

	void forgotPassword(final String userName) throws Exception;

	UserInfo getUserInfo(String userName) throws Exception; // getUserInfo

	boolean hasUser(String userName);

	void resetPassword(ResetPasswordRequest resetRequest) throws Exception;

	void changePassword(PasswordRequest passwordRequest) throws Exception;

	void changeEmail(String userName, String newEmailAddr) throws Exception;

}
