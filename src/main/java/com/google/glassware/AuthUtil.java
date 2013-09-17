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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * A collection of utility functions that simplify common authentication and
 * user identity tasks
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class AuthUtil {
  public static ListableMemoryCredentialStore store = new ListableMemoryCredentialStore();
  public static final String GLASS_SCOPE = "https://www.googleapis.com/auth/glass.timeline "
      + "https://www.googleapis.com/auth/glass.location "
      + "https://www.googleapis.com/auth/userinfo.profile";
  private static final Logger LOG = Logger.getLogger(AuthUtil.class.getSimpleName());

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
        .setCredentialStore(store).build();
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
    store.delete(userId, getCredential(userId));

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

  public static List<String> getAllUserIds() {
    return store.listAllUsers();
  }
}
