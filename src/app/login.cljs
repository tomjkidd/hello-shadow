(ns app.login
  "Manage the concerns of logging a user into the application.

  TODO: Handle errors in any of the cases!
  TODO: May be useful to build a protocol, but for now just
  using Auth0 out-of-the-box."
  (:require [clojure.pprint :refer [pprint]]
            [clojure.string :as string]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as rf]
            ["@auth0/auth0-spa-js" :as auth0-spa-js]
            ["@material-ui/core" :as mui]
            ["@material-ui/icons" :as mui-icons]
            [goog.object :as go]))

;; Refer to shadow-cljs.edn [:builds :build-id :closure-defines]
;; for how to provide these as env vars so that they get compiled in!
(goog-define ^String DOMAIN "")
(goog-define ^String CLIENT_ID "")
(goog-define ^String AUDIENCE "")

(def domain DOMAIN)
(def client-id CLIENT_ID)
(def audience AUDIENCE)

(def auth-config
  {:domain        domain
   :client_id     client-id
   :audience      audience
   ;:cacheLocation "localstorage"
   })

(def get-token-silently-options
  (clj->js {:timeoutInSeconds 10}))

(defn contains-auth-redirect?
  "This is the way the auth0 docs indicate to manage the concern for vanilla js

  https://auth0.com/docs/quickstart/spa/vanillajs"
  [window-location-search]
  (and
   (string/includes? window-location-search "code=")
   (string/includes? window-location-search "state=")))

(defn- store-user-and-token!
  [context-str auth-client]
  (do
    (.then (.getTokenSilently auth-client get-token-silently-options)
           (fn [token]
             (rf/dispatch [:store-auth-token token])))
    (.then (do
             (js/console.log (str context-str
                                  ": User is authenticated."))
             (.getUser auth-client))
           #(let [user (js->clj % :keywordize-keys true)]
              (js/console.log
               (str context-str
                    ": User:"
                    \newline
                    (with-out-str
                      (pprint user))))
              (rf/dispatch [:store-user user])))))

(defn- cleanup-url!
  "Remove the auth0-specific URL search-params that are added as part of
  the login redirect"
  []
  (let [search-params (js/URLSearchParams. js/window.location.search)]
    (doseq [k ["code" "state"]]
      (.delete search-params k))
    (let [new-url    (js/window.URL. js/window.location.href)
          new-search (.toString search-params)]
      ;; NOTE: This is mutating new-url!
      (go/set new-url "search" new-search)
      (js/window.history.pushState (clj->js {})
                                   js/document.title
                                   new-url))))

(defn request-auth-client!
  "Manages the concern of storing the auth-client,
  which is used to manage the OIDC dance to ensure that a user
  is authenticated."
  []
  (cond-> (auth0-spa-js/createAuth0Client
           (clj->js auth-config))
    true
    (.then (fn [auth-client]
             (rf/dispatch-sync [:store-auth-client auth-client])
             auth-client))

    true
    (.then (fn [auth-client]
             (.then (.isAuthenticated auth-client)
                    (fn [is-authenticated?]
                      (cond
                        is-authenticated?
                        (store-user-and-token! "request-auth-client!" auth-client)

                        (and (not is-authenticated?)
                             (contains-auth-redirect? js/window.location.search))
                        (rf/dispatch [:handle-auth-redirect])

                        (and (not is-authenticated?)
                             (not (contains-auth-redirect? js/window.location.search)))
                        (rf/dispatch [:login]))))))))

(defn request-login!
  "Manages the concern of entering the OIDC dance to authenticate a user"
  [auth-client]
  (.loginWithRedirect auth-client
                      (clj->js {:redirect_uri js/window.location.origin})))

(defn handle-auth-redirect!
  "Manages the concerns of storing auth-token and user, assumes that the
  application is in a state where the auth-client can manage the redirect
  portion of OIDC dance."
  [auth-client]
  (cond-> (.handleRedirectCallback auth-client)
    true
    (.then #(.isAuthenticated auth-client))

    true
    (.then #(when %
              (store-user-and-token! "handle-auth-redirect!" auth-client)
              (cleanup-url!)))))

(defn request-logout!
  "Manages the concern of logging out a user"
  [auth-client]
  (.logout auth-client
   (clj->js {:returnTo js/window.location.origin})))

(defn welcome
  []
  (let [user @(rf/subscribe [:user])]
    [:div
     (str "Welcome, " (:name user))
     [:> mui/Button
      {:variant  "outlined"
       :color    "secondary"
       :size     "small"
       :on-click #(rf/dispatch [:logout])}
      "Logout"
      [:> mui-icons/Clear]]]))

(defn not-logged-in
  "Puts up a spinning backdrop to indicate the app is loading"
  []
  [:div "User is not logged in."
   [:> mui/Backdrop
    {:open true}
    [:> mui/CircularProgress]]])
