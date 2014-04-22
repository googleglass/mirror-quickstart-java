/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.glassware;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Account;
import com.google.api.services.mirror.model.AuthToken;

/**
 * A collection of utility functions that simplify common authentication and
 * user identity tasks
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */

public class AuthUtil {
  public static final String GLASS_SCOPE = "https://www.googleapis.com/auth/glass.timeline "
      + "https://www.googleapis.com/auth/glass.location "
      + "https://www.googleapis.com/auth/userinfo.profile";
  private static final Logger LOG = Logger.getLogger(AuthUtil.class.getSimpleName());

  	public static DataStore<StoredCredential> getDataStore()  {
  		DataStore<StoredCredential> store = null;
  		try {
  			 AppEngineDataStoreFactory.getDefaultInstance().getDataStore(StoredCredential.DEFAULT_DATA_STORE_ID);
  		} catch (IOException e) {
  			//TODO: handle this exception
  			e.printStackTrace();
  			throw new RuntimeException(e);
  		}
  		return store;
  	}
  
  /**
   * Creates and returns a new {@link AuthorizationCodeFlow} for this app.
   */
  public static AuthorizationCodeFlow newAuthorizationCodeFlow() throws IOException {
    URL resource = AuthUtil.class.getResource("/oauth.properties");
    File propertiesFile = new File("./src/main/resources/oauth.properties");
    try {
      propertiesFile = new File(resource.toURI());
      //LOG.info("Able to find oauth properties from file.");
    } catch (URISyntaxException e) {
      LOG.info(e.toString());
      LOG.info("Using default source path.");
    }
    FileInputStream authPropertiesStream = new FileInputStream(propertiesFile);
    Properties authProperties = new Properties();
    authProperties.load(authPropertiesStream);

    String clientId = authProperties.getProperty("client_id");
    String clientSecret = authProperties.getProperty("client_secret");

    return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new JacksonFactory(),
        clientId, clientSecret, Collections.singleton(GLASS_SCOPE)).setAccessType("offline")
        .setDataStoreFactory(AppEngineDataStoreFactory.getDefaultInstance()).build();
  }

  /**
   * Get the current user's ID from the session
   *
   * @return string user id or null if no one is logged in
   */
  public static String getUserId(HttpServletRequest request) {
    HttpSession session = request.getSession();
    return (String) session.getAttribute("userId");
  }

  public static void setUserId(HttpServletRequest request, String userId) {
    HttpSession session = request.getSession();
    session.setAttribute("userId", userId);
  }

  public static void clearUserId(HttpServletRequest request) throws IOException {
    // Delete the credential in the credential store
    String userId = getUserId(request);
    getDataStore().delete(userId);
    //getDataStore().delete(userId, getCredential(userId));

    // Remove their ID from the local session
    request.getSession().removeAttribute("userId");
  }

  public static Credential getCredential(String userId) throws IOException {
    if (userId == null) {
      return null;
    } else {
      return AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
    }
  }

  public static Credential getCredential(HttpServletRequest req) throws IOException {
    return AuthUtil.newAuthorizationCodeFlow().loadCredential(getUserId(req));
  }

  @SuppressWarnings("unchecked")
public static List<String> getAllUserIds() {
    try {
		return (List<String>) getDataStore().keySet();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    return null;
  }

  /**
   * {see #installMirrorAccount} 
   * 
 * @param applicationName
 * @return
 */
public static Mirror getServerToServerMirror(String applicationName) {
	     List<String> scopes = new ArrayList<String>();
	     scopes.add("https://www.googleapis.com/auth/glass.thirdpartyauth");
		 AppIdentityCredential credential = new AppIdentityCredential(scopes);
		 HttpTransport httpTransport = new NetHttpTransport();
			JacksonFactory jsonFactory = new JacksonFactory();
		Mirror service = new Mirror.Builder(httpTransport, jsonFactory, null)
		.setApplicationName(applicationName)
		.setHttpRequestInitializer(credential).build();
		return service;
  }
  
  
  /**
 * Uses {see #getServerToServerMirror} 
 * This method creates a mirror account to support authentication of your GDK application. {@see https://developers.google.com/glass/develop/gdk/authentication}
 * @param userToken -- the userToken provided by MyGlass
 * @param authTokenType
 * @param accountType
 * @param authToken -- application specific auth token
 * @param appUserId -- application specific user id
 * @param applicationName -- the name of your application 
 * @throws IOException
 */
public static void installMirrorAccount(String userToken, String authTokenType, String accountType, String authToken, String appUserId, String applicationName) throws IOException {
		try {
		    Account account = new Account();
		    	List<AuthToken> authTokens = new ArrayList<AuthToken>();
		    	authTokens.add(new AuthToken().setType(authTokenType).setAuthToken(authToken));
		    account.setAuthTokens(authTokens);
		    getServerToServerMirror(applicationName).accounts().insert(
		        userToken, accountType, appUserId, account).execute();
		  } catch (IOException e) {
			 LOG.warning("Failed to save to mirror: " + e.getMessage());
		    e.printStackTrace();
		  }
  }
  
}
