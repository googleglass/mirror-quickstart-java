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
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * A new credential store for App Engine. It's exactly the same as
 * com.google.api
 * .client.extensions.appengine.auth.oauth2.AppEngineCredentialStore except it
 * has the added ability to list all of the users.
 * 
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class ListableAppEngineCredentialStore implements CredentialStore {

  private static final String KIND = ListableAppEngineCredentialStore.class.getName();

  public List<String> listAllUsers() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query userQuery = new Query(KIND);
    Iterable<Entity> userEntities = datastore.prepare(userQuery).asIterable();

    List<String> userIds = new ArrayList<String>();
    for (Entity userEntity : userEntities) {
      userIds.add(userEntity.getKey().getName());
    }
    return userIds;
  }


  @Override
  public void store(String userId, Credential credential) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity = new Entity(KIND, userId);
    entity.setProperty("accessToken", credential.getAccessToken());
    entity.setProperty("refreshToken", credential.getRefreshToken());
    entity.setProperty("expirationTimeMillis", credential.getExpirationTimeMilliseconds());
    datastore.put(entity);
  }

  @Override
  public void delete(String userId, Credential credential) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(KIND, userId);
    datastore.delete(key);
  }

  @Override
  public boolean load(String userId, Credential credential) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(KIND, userId);
    try {
      Entity entity = datastore.get(key);
      credential.setAccessToken((String) entity.getProperty("accessToken"));
      credential.setRefreshToken((String) entity.getProperty("refreshToken"));
      credential.setExpirationTimeMilliseconds((Long) entity.getProperty("expirationTimeMillis"));
      return true;
    } catch (EntityNotFoundException exception) {
      return false;
    }
  }
}
