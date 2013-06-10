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

import com.google.api.client.http.GenericUrl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class WebUtil {
  /**
   * Builds a URL relative to this app's root.
   */
  public static String buildUrl(HttpServletRequest req, String relativePath) {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath(relativePath);
    return url.build();
  }

  /**
   * A simple flash implementation for text messages across requests
   *
   * @param request
   * @return
   */
  public static String getClearFlash(HttpServletRequest request) {
    HttpSession session = request.getSession();
    String flash = (String) session.getAttribute("flash");
    session.removeAttribute("flash");
    return flash;
  }

  public static void setFlash(HttpServletRequest request, String flash) {
    HttpSession session = request.getSession();
    session.setAttribute("flash", flash);
  }
}
