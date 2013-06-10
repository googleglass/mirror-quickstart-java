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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;


import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A new credential store. It's exactly the same as
 * com.google.api.client.auth.oauth2.MemoryCredentialStore except it
 * has the added ability to list all of the users.
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class ListableMemoryCredentialStore implements CredentialStore {

  /**
   * Lock on access to the store.
   */
  private final Lock lock = new ReentrantLock();

  /**
   * Store of memory persisted credentials, indexed by userId.
   */
  private final Map<String, MemoryPersistedCredential> store =
      new HashMap<String, MemoryPersistedCredential>();

  public void store(String userId, Credential credential) {
    lock.lock();
    try {
      MemoryPersistedCredential item = store.get(userId);
      if (item == null) {
        item = new MemoryPersistedCredential();
        store.put(userId, item);
      }
      item.store(credential);
    } finally {
      lock.unlock();
    }
  }

  public void delete(String userId, Credential credential) {
    lock.lock();
    try {
      store.remove(userId);
    } finally {
      lock.unlock();
    }
  }

  public boolean load(String userId, Credential credential) {
    lock.lock();
    try {
      MemoryPersistedCredential item = store.get(userId);
      if (item != null) {
        item.load(credential);
      }
      return item != null;
    } finally {
      lock.unlock();
    }
  }

  public List<String> listAllUsers() {
    List<String> allUsers = new ArrayList<String>();
    // Is that a 47 character long generic for one line of behavior? Yes, yes it is.
    for (Iterator<Map.Entry<String, MemoryPersistedCredential>> iterator = store.entrySet()
        .iterator();
         iterator.hasNext(); ) {
      allUsers.add(iterator.next().getKey());
    }
    return allUsers;
  }

  class MemoryPersistedCredential {

    /**
     * Access token or {@code null} for none.
     */
    private String accessToken;

    /**
     * Refresh token {@code null} for none.
     */
    private String refreshToken;

    /**
     * Expiration time in milliseconds {@code null} for none.
     */
    private Long expirationTimeMillis;

    /**
     * Store information from the credential.
     *
     * @param credential credential whose {@link Credential#getAccessToken access token},
     *                   {@link Credential#getRefreshToken refresh token}, and
     *                   {@link Credential#getExpirationTimeMilliseconds expiration time} need to be stored
     */
    void store(Credential credential) {
      accessToken = credential.getAccessToken();
      refreshToken = credential.getRefreshToken();
      expirationTimeMillis = credential.getExpirationTimeMilliseconds();
    }

    /**
     * Load information into the credential.
     *
     * @param credential credential whose {@link Credential#setAccessToken access token},
     *                   {@link Credential#setRefreshToken refresh token}, and
     *                   {@link Credential#setExpirationTimeMilliseconds expiration time} need to be set if the
     *                   credential already exists in storage
     */
    void load(Credential credential) {
      credential.setAccessToken(accessToken);
      credential.setRefreshToken(refreshToken);
      credential.setExpirationTimeMilliseconds(expirationTimeMillis);
    }
  }
}