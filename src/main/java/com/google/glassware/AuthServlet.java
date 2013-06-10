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
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet manages the OAuth 2.0 dance
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class AuthServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(AuthServlet.class.getSimpleName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // If something went wrong, log the error message.
    if (req.getParameter("error") != null) {
      LOG.severe("Something went wrong during auth: " + req.getParameter("error"));
      res.setContentType("text/plain");
      res.getWriter().write("Something went wrong during auth. Please check your log for details");
      return;
    }

    // If we have a code, finish the OAuth 2.0 dance
    if (req.getParameter("code") != null) {
      LOG.info("Got a code. Attempting to exchange for access token.");

      AuthorizationCodeFlow flow = AuthUtil.newAuthorizationCodeFlow();
      TokenResponse tokenResponse =
          flow.newTokenRequest(req.getParameter("code"))
              .setRedirectUri(WebUtil.buildUrl(req, "/oauth2callback")).execute();

      // Extract the Google User ID from the ID token in the auth response
      String userId = ((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload().getUserId();

      LOG.info("Code exchange worked. User " + userId + " logged in.");

      // Set it into the session
      AuthUtil.setUserId(req, userId);
      flow.createAndStoreCredential(tokenResponse, userId);

      // The dance is done. Do our bootstrapping stuff for this user
      NewUserBootstrapper.bootstrapNewUser(req, userId);

      // Redirect back to index
      res.sendRedirect(WebUtil.buildUrl(req, "/"));
      return;
    }

    // Else, we have a new flow. Initiate a new flow.
    LOG.info("No auth context found. Kicking off a new auth flow.");

    AuthorizationCodeFlow flow = AuthUtil.newAuthorizationCodeFlow();
    GenericUrl url =
        flow.newAuthorizationUrl().setRedirectUri(WebUtil.buildUrl(req, "/oauth2callback"));
    url.set("approval_prompt", "force");
    res.sendRedirect(url.build());
  }

}
