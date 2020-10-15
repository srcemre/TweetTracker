package com.emresarac.tweettracker;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class UserAuthManager {

    private static String OAUTH_ACCESS_TOKEN;
    private static String OAUTH_TOKEN_SECRET;

    private static long userID;
    private static String userName;
    private static String userProfileImgUrl;

    private static UserAuthManager userAuthManager = null;

    public static UserAuthManager getDefault() {
        if (userAuthManager == null)
            userAuthManager = new UserAuthManager();
        return userAuthManager;
    }

    public void logout() {
        OAUTH_ACCESS_TOKEN = null;
        OAUTH_TOKEN_SECRET = null;
        userName           = null;
        userProfileImgUrl  = null;
        userID             = -1;
    }

    public void setAccessTokens(AccessToken accessToken) {
        OAUTH_ACCESS_TOKEN = accessToken.getToken();
        OAUTH_TOKEN_SECRET = accessToken.getTokenSecret();
    }

    public void setUserInfos(Twitter twitter) throws TwitterException {

        userID     = twitter.getId();
        userName   = twitter.getScreenName();
        userProfileImgUrl = twitter.showUser(userID).get400x400ProfileImageURLHttps();
    }

    public AccessToken getAccessToken() {
        return new AccessToken(OAUTH_ACCESS_TOKEN, OAUTH_TOKEN_SECRET);
    }

    public String getUserName() {
        return "@" + userName;
    }

    public String getUserProfileImgUrl() {
        return userProfileImgUrl;
    }
}
