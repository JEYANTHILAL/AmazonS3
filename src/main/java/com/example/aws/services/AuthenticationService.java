package com.example.aws.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AdminUserGlobalSignOutRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.cognitoidp.model.ChangePasswordRequest;
import com.amazonaws.services.cognitoidp.model.ChangePasswordResult;
import com.amazonaws.services.cognitoidp.model.CodeDeliveryDetailsType;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordResult;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ForgotPasswordResult;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.UserType;


public class AuthenticationService implements AuthenticationInterface, CognitoResources {
	
	
	private final static String USERNAME = "USERNAME";
	private final static String PASSWORD = "PASSWORD";
	private final static String NEW_PASSWORD = "NEW_PASSWORD";

	protected static AWSCognitoIdentityProvider mIdentityProvider = null;

	public AuthenticationService() {
		if (mIdentityProvider == null) {
			mIdentityProvider = getAmazonCognitoIdentityClient();
		}
	}

	protected AWSCredentials getCredentials(String AWS_ID, String AWS_KEY) {
		AWSCredentials credentials = new BasicAWSCredentials(AWS_ID, AWS_KEY);
		return credentials;
	}

	protected AWSCognitoIdentityProvider getAmazonCognitoIdentityClient() {
		AWSCredentials credentials = getCredentials(cognitoID, cognitoKey);
		AWSCredentialsProvider credProvider = new AWSStaticCredentialsProvider(credentials);
		AWSCognitoIdentityProvider client = AWSCognitoIdentityProviderClientBuilder.standard()
				.withCredentials(credProvider).withRegion(region).build();
		return client;
	}

	public void createNewUser(final UserInfo userInfo) throws AWSCognitoIdentityProviderException {
		final String emailAddr = userInfo.getEmailAddr();
		if (emailAddr != null && emailAddr.length() > 0) {
			// There should be no user with this email address, so info should
			// be null
			UserInfo info = findUserByEmailAddr(emailAddr);
			if (info == null) {
				AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest().withUserPoolId(poolID)
						.withUsername(userInfo.getUserName())
						.withUserAttributes(new AttributeType().withName(EMAIL).withValue(emailAddr),
								new AttributeType().withName("email_verified").withValue("true"));
				// The AdminCreateUserResult resturned by this function doesn't
				// contain useful information so the
				// result is ignored.
				mIdentityProvider.adminCreateUser(cognitoRequest);
			} else {
				// The caller should have checked that the email address is not
				// already used by a user. If this is not
				// done, then it's an exception (e.g., something is wrong).
				throw new DuplicateEmailException("The email address " + emailAddr + " is already in the database");
			}
		}
	} // createNewUser

	public void deleteUser(final String userName, final String password) throws AWSCognitoIdentityProviderException {
		SessionInfo sessionInfo = sessionLogin(userName, password);
		if (sessionInfo != null) {
			AdminDeleteUserRequest deleteRequest = new AdminDeleteUserRequest().withUsername(userName)
					.withUserPoolId(poolID);
			// the adminDeleteUserRequest returns an AdminDeleteUserResult which
			// doesn't contain anything useful.
			// So the result is ignored.
			mIdentityProvider.adminDeleteUser(deleteRequest);
		}
	}

	protected SessionInfo sessionLogin(final String userName, final String password)
			throws AWSCognitoIdentityProviderException {
		SessionInfo info = null;
		HashMap<String, String> authParams = new HashMap<String, String>();
		authParams.put("USERNAME", userName);
		authParams.put("PASSWORD", password);
		AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
				.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH).withUserPoolId(poolID).withClientId(clientID)
				.withAuthParameters(authParams);
		AdminInitiateAuthResult authResult = mIdentityProvider.adminInitiateAuth(authRequest);
		// If there is a bad username the adminInitiateAuth() call will throw a
		// UserNotFoundException.
		// Unfortunately the AWS documentation doesn't say what happens if the
		// password is incorrect.
		// Perhaps the NotAuthorizedException is thrown?
		if (authResult != null) {
			final String session = authResult.getSession();
			String accessToken = null;
			AuthenticationResultType resultType = authResult.getAuthenticationResult();
			if (resultType != null) {
				accessToken = resultType.getAccessToken();
			}
			final String challengeResult = authResult.getChallengeName();
			info = new SessionInfo(session, accessToken, challengeResult);
		}
		return info;
	}

	@Override
	public LoginInfo userLogin(final String userName, final String password)
			throws AWSCognitoIdentityProviderException {
		LoginInfo loginInfo = null;
		SessionInfo sessionInfo = sessionLogin(userName, password);
		// The process of sessionLogin should either return a session ID (if the
		// account has not been verified) or a
		// token ID (if the account has been verified).
		if (sessionInfo != null) {
			UserInfo userInfo = getUserInfo(userName);
			loginInfo = new LoginInfo(userInfo);
			// check to see if the password used was a temporary password. If
			// this is the case, the password
			// must be reset.
			String challengeResult = sessionInfo.getChallengeResult();
			if (challengeResult != null && challengeResult.length() > 0) {
				loginInfo
						.setNewPasswordRequired(challengeResult.equals(ChallengeNameType.NEW_PASSWORD_REQUIRED.name()));
			}
		}
		return loginInfo;
	}

	@Override
	public void userLogout(final String userName) throws AWSCognitoIdentityProviderException {
		AdminUserGlobalSignOutRequest signOutRequest = new AdminUserGlobalSignOutRequest().withUsername(userName)
				.withUserPoolId(poolID);
		// The AdminUserGlobalSignOutResult returned by this function does not
		// contain any useful information so the
		// result is ignored.
		mIdentityProvider.adminUserGlobalSignOut(signOutRequest);
	}

	public void changePassword(final PasswordRequest passwordRequest) throws AWSCognitoIdentityProviderException {
		// Signin with the old/temporary password. Apparently this is needed to
		// establish a session for the
		// password change.
		final SessionInfo sessionInfo = sessionLogin(passwordRequest.getUserName(), passwordRequest.getOldPassword());
		if (sessionInfo != null && sessionInfo.getAccessToken() != null) {
			ChangePasswordRequest changeRequest = new ChangePasswordRequest()
					.withAccessToken(sessionInfo.getAccessToken())
					.withPreviousPassword(passwordRequest.getOldPassword())
					.withProposedPassword(passwordRequest.getNewPassword());
			ChangePasswordResult rslt = mIdentityProvider.changePassword(changeRequest);
		} else {
			String msg = "Access token was not returned from session login";
			throw new AWSCognitoIdentityProviderException(msg);
		}
	}

	public void changeEmail(final String userName, final String newEmailAddr)
			throws AWSCognitoIdentityProviderException {
		AdminUpdateUserAttributesRequest updateRequest = new AdminUpdateUserAttributesRequest().withUsername(userName)
				.withUserPoolId(poolID).withUserAttributes(new AttributeType().withName(EMAIL).withValue(newEmailAddr),
						new AttributeType().withName("email_verified").withValue("true"));
		mIdentityProvider.adminUpdateUserAttributes(updateRequest);
	}

	@Override
	public void changeFromTemporaryPassword(final PasswordRequest passwordRequest)
			throws AWSCognitoIdentityProviderException {
		// Signin with the old/temporary password. Apparently this is needed to
		// establish a session for the
		// password change.
		final SessionInfo sessionInfo = sessionLogin(passwordRequest.getUserName(), passwordRequest.getOldPassword());
		final String sessionString = sessionInfo.getSession();
		if (sessionString != null && sessionString.length() > 0) {
			Map<String, String> challengeResponses = new HashMap<String, String>();
			challengeResponses.put(USERNAME, passwordRequest.getUserName());
			challengeResponses.put(PASSWORD, passwordRequest.getOldPassword());
			challengeResponses.put(NEW_PASSWORD, passwordRequest.getNewPassword());
			AdminRespondToAuthChallengeRequest changeRequest = new AdminRespondToAuthChallengeRequest()
					.withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
					.withChallengeResponses(challengeResponses).withClientId(clientID).withUserPoolId(poolID)
					.withSession(sessionString);
			AdminRespondToAuthChallengeResult challengeResponse = mIdentityProvider.adminRespondToAuthChallenge(changeRequest);
		}
	} // changePassword

	public void resetPassword(ResetPasswordRequest resetRequest) throws AWSCognitoIdentityProviderException {
		ConfirmForgotPasswordRequest passwordRequest = new ConfirmForgotPasswordRequest()
				.withUsername(resetRequest.getUserName()).withConfirmationCode(resetRequest.getResetCode())
				.withClientId(clientID).withPassword(resetRequest.getNewPassword());
		ConfirmForgotPasswordResult rslt = mIdentityProvider.confirmForgotPassword(passwordRequest);
	}

	@Override
	public void forgotPassword(final String userName) throws AWSCognitoIdentityProviderException {
		ForgotPasswordRequest passwordRequest = new ForgotPasswordRequest().withClientId(clientID)
				.withUsername(userName);
		ForgotPasswordResult rslt = mIdentityProvider.forgotPassword(passwordRequest);
		CodeDeliveryDetailsType delivery = rslt.getCodeDeliveryDetails();
	}

	@Override
	public UserInfo getUserInfo(final String userName) throws AWSCognitoIdentityProviderException {
		AdminGetUserRequest userRequest = new AdminGetUserRequest().withUsername(userName).withUserPoolId(poolID);
		AdminGetUserResult userResult = mIdentityProvider.adminGetUser(userRequest);
		List<AttributeType> userAttributes = userResult.getUserAttributes();
		final String rsltUserName = userResult.getUsername();
		String emailAddr = null;
		String location = null;
		for (AttributeType attr : userAttributes) {
			if (attr.getName().equals(EMAIL)) {
				emailAddr = attr.getValue();
			}
		}
		UserInfo info = null;
		if (rsltUserName != null && emailAddr != null) {
			info = new UserInfo(rsltUserName, emailAddr);
		}
		return info;
	} // getUserInfo

	public UserInfo findUserByEmailAddr(String email) throws AWSCognitoIdentityProviderException {
		UserInfo info = null;
		if (email != null && email.length() > 0) {
			final String emailQuery = "email=\"" + email + "\"";
			ListUsersRequest usersRequest = new ListUsersRequest().withUserPoolId(poolID)
					.withAttributesToGet(EMAIL).withFilter(emailQuery);
			ListUsersResult usersRslt = mIdentityProvider.listUsers(usersRequest);
			List<UserType> users = usersRslt.getUsers();
			if (users != null && users.size() > 0) {
				// There should only be a single instance of an email address in
				// the Cognito database
				// (e.g., there should not be multiple users with the same email
				// address).
				if (users.size() == 1) {
					UserType user = users.get(0);
					final String userName = user.getUsername();
					String emailAddr = null;
					String location = null;
					List<AttributeType> attributes = user.getAttributes();
					if (attributes != null) {
						for (AttributeType attr : attributes) {
							if (attr.getName().equals(EMAIL)) {
								emailAddr = attr.getValue();
							} 
						}
						if (userName != null && emailAddr != null) {
							info = new UserInfo(userName, emailAddr);
						}
					}
				} else {
					throw new DuplicateEmailException("More than one user has the email address " + email);
				}
			}
		}
		return info;
	}

	@Override
	public boolean hasUser(final String userName) {
		boolean userExists = false;
		try {
			UserInfo info = getUserInfo(userName);
			if (info != null && info.getUserName() != null && info.getUserName().length() > 0
					&& info.getUserName().equals(userName)) {
				userExists = true;
			}
		} catch (SdkBaseException ex) {
		}
		return userExists;
	}

}
