<!--
Copyright (C) 2013 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<%@ page import="com.google.api.client.auth.oauth2.Credential" %>
<%@ page import="com.google.api.services.mirror.model.Contact" %>
<%@ page import="com.google.glassware.MirrorClient" %>
<%@ page import="com.google.glassware.WebUtil" %>
<%@ page
    import="java.util.List" %>
<%@ page import="com.google.api.services.mirror.model.TimelineItem" %>
<%@ page import="com.google.api.services.mirror.model.Subscription" %>
<%@ page import="com.google.api.services.mirror.model.Attachment" %>
<%@ page import="com.google.glassware.MainServlet" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!doctype html>
<%
  String userId = com.google.glassware.AuthUtil.getUserId(request);
  String appBaseUrl = WebUtil.buildUrl(request, "/");

  Credential credential = com.google.glassware.AuthUtil.getCredential(userId);

  Contact contact = MirrorClient.getContact(credential, MainServlet.CONTACT_NAME);

  List<TimelineItem> timelineItems = MirrorClient.listItems(credential, 3L).getItems();


  List<Subscription> subscriptions = MirrorClient.listSubscriptions(credential).getItems();
  boolean timelineSubscriptionExists = false;
  boolean locationSubscriptionExists = false;


  if (subscriptions != null) {
    for (Subscription subscription : subscriptions) {
      if (subscription.getId().equals("timeline")) {
        timelineSubscriptionExists = true;
      }
      if (subscription.getId().equals("location")) {
        locationSubscriptionExists = true;
      }
    }
  }

%>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Glassware Starter Project</title>
  <link href="/static/bootstrap/css/bootstrap.min.css" rel="stylesheet"
        media="screen">

  <style>
    .button-icon {
      max-width: 75px;
    }

    .tile {
      border-left: 1px solid #444;
      padding: 5px;
      list-style: none;
    }

    .btn {
      width: 100%;
    }
  </style>
</head>
<body>
<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      <a class="brand" href="#">Glassware Starter Project: Java Edition</a>
    </div>
  </div>
</div>

<div class="container">

  <!-- Main hero unit for a primary marketing message or call to action -->
  <div class="hero-unit">
    <h1>Your Recent Timeline</h1>
    <% String flash = WebUtil.getClearFlash(request);
      if (flash != null) { %>
    <span class="label label-warning">Message: <%= flash %> </span>
    <% } %>

    <div style="margin-top: 5px;">

      <% if (timelineItems != null) {
        for (TimelineItem timelineItem : timelineItems) { %>
      <ul class="span3 tile">
        <li><strong>ID: </strong> <%= timelineItem.getId() %>
        </li>
        <li>
          <strong>Text: </strong> <%= timelineItem.getText() %>
        </li>
        <li>
          <strong>HTML: </strong> <%= timelineItem.getHtml() %>
        </li>
        <li>
          <strong>Attachments: </strong>
          <%
          if (timelineItem.getAttachments() != null) {
            for (Attachment attachment : timelineItem.getAttachments()) {
              if (MirrorClient.getAttachmentContentType(credential, timelineItem.getId(), attachment.getId()).startsWith("")) { %>
          <img src="<%= appBaseUrl + "attachmentproxy?attachment=" +
            attachment.getId() + "&timelineItem=" + timelineItem.getId() %>">
          <% } else { %>
          <a href="<%= appBaseUrl + "attachmentproxy?attachment=" +
            attachment.getId() + "&timelineItem=" + timelineItem.getId() %>">Download</a>
          <% }
            }
          } %>
        </li>
        <li>
          <form action="/" method="post">
            <input type="hidden" name="itemId" value="<%= timelineItem.getId() %>">
            <input type="hidden" name="operation" value="deleteTimelineItem">
            <button class="btn" type="submit">Delete Item</button>
          </form>
        </li>

      </ul>
      <% }
      } %>
    </div>
    <div style="clear:both;"></div>
  </div>

  <!-- Example row of columns -->
  <div class="row">
    <div class="span4">
      <h2>Timeline</h2>

      <p>When you first sign in, this Glassware inserts a welcome message. Use
        these controls to insert more items into your timeline. Learn more about
        the timeline APIs
        <a href="https://developers.google.com/glass/timeline">here</a></p>


      <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
        <input type="hidden" name="operation" value="insertItem">
        <textarea name="message">Hello World!</textarea><br/>
        <button class="btn" type="submit">The above message</button>
      </form>

      <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
        <input type="hidden" name="operation" value="insertItem">
        <input type="hidden" name="message" value="Chipotle says 'hi'!">
        <input type="hidden" name="imageUrl" value="<%= appBaseUrl +
               "static/images/chipotle-tube-640x360.jpg" %>">
        <input type="hidden" name="contentType" value="image/jpeg">

        <button class="btn" type="submit">A picture
          <img class="button-icon" src="<%= appBaseUrl +
               "static/images/chipotle-tube-640x360.jpg" %>">
        </button>
      </form>
      <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
        <input type="hidden" name="operation" value="insertItemWithAction">
        <button class="btn" type="submit">A card you can reply to</button>
      </form>
      <hr>
      <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
        <input type="hidden" name="operation" value="insertItemAllUsers">
        <button class="btn" type="submit">A card to all users</button>
      </form>

    </div>

    <div class="span4">
      <h2>Contacts</h2>

      <p>By default, this project inserts a single contact that accepts
        all content types. Learn more about contacts
        <a href="https://developers.google.com/glass/contacts">here</a>.</p>

      <% if (contact == null) { %>
      <form class="span3" action="<%= WebUtil.buildUrl(request, "/main") %>"
            method="post">
        <input type="hidden" name="operation" value="insertContact">
        <input type="hidden" name="iconUrl" value="<%= appBaseUrl +
               "static/images/chipotle-tube-640x360.jpg" %>">
        <input type="hidden" name="name"
               value="<%= MainServlet.CONTACT_NAME %>">
        <button class="btn" type="submit">Insert Java Quick Start Contact
        </button>
      </form>
      <% } else { %>
      <form class="span3" action="<%= WebUtil.buildUrl(request, "/main") %>"
            method="post">
        <input type="hidden" name="operation" value="deleteContact">
        <input type="hidden" name="id" value="<%= MainServlet.CONTACT_NAME %>">
        <button class="btn" type="submit">Delete Java Quick Start Contact
        </button>
      </form>
      <% } %>
    </div>

    <div class="span4">
      <h2>Subscriptions</h2>

      <p>By default a subscription is inserted for changes to the
        <code>timeline</code> collection. Learn more about subscriptions
        <a href="https://developers.google.com/glass/subscriptions">here</a></p>

      <p class="label label-info">Note: Subscriptions require SSL. <br>They will
        not work on localhost.</p>

      <% if (timelineSubscriptionExists) { %>
      <form action="<%= WebUtil.buildUrl(request, "/main") %>"
            method="post">
        <input type="hidden" name="subscriptionId" value="timeline">
        <input type="hidden" name="operation" value="deleteSubscription">
        <button class="btn" type="submit" class="delete">Unsubscribe from
          timeline updates
        </button>
      </form>
      <% } else { %>
      <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
        <input type="hidden" name="operation" value="insertSubscription">
        <input type="hidden" name="collection" value="timeline">
        <button class="btn" type="submit">Subscribe to timeline updates</button>
      </form>
      <% }%>

      <% if (locationSubscriptionExists) { %>
      <form action="<%= WebUtil.buildUrl(request, "/main") %>"
            method="post">
        <input type="hidden" name="subscriptionId" value="locations">
        <input type="hidden" name="operation" value="deleteSubscription">
        <button class="btn" type="submit" class="delete">Unsubscribe from
          location updates
        </button>
      </form>
      <% } else { %>
      <form action="<%= WebUtil.buildUrl(request, "/main") %>" method="post">
        <input type="hidden" name="operation" value="insertSubscription">
        <input type="hidden" name="collection" value="locations">
        <button class="btn" type="submit">Subscribe to location updates</button>
      </form>
      <% }%>
    </div>
  </div>
</div>

<script
    src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="/static/bootstrap/js/bootstrap.min.js"></script>
</body>
</html>
