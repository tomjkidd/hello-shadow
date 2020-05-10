(ns app.graph
  "Defines the data model for the :object-graph key in the re-frame app db

  The desire is to leverage the work of fulcro to get an EQL-based graph
  database, but not to buy into actually building UI elements with it."
  (:require [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
            [com.fulcrologic.fulcro.algorithms.normalized-state :as nsh]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.networking.http-remote :as net]
            [re-frame.db :refer [app-db]]))

;; NOTE: This is the fulcro app, which currently provides EQL and object-graph capabilities
;;       It is explicitly not being used to control render!
;;       It should be capable of everything that fulcro knows how to do, aside from render related things
;;       See this [fulcro-commit][fulcro-commit] for more details
;; [fulcro-commit]:https://github.com/awkay/fulcro-with-reframe/commit/07ea925b0296915a4babb4592cb133a51c1e93fd
(defonce fulcro-app
  (assoc
   (app/fulcro-app {:optimized-render! identity
                    :render-root!      identity
                    :hydrate-root!     identity
                    :remotes           {:remote (net/fulcro-http-remote {:url "/api"})}})
   ;; Fulcro and re-frame will share their db!
   ::app/state-atom app-db))

(defn get-page-query
  [page]
  ;; TODO: The app db should be all that is required to determine what needs to render.
  ;;       Right now, just the page is sufficient
  ;; Construct an EQL query that provides the data in the shape of the UI
  (case page
    :home
    [[:ui-login/id :logged-in-user] [{:ui-login/access-token [:token/value]}]]
    :profile
    [[:ui-login/id :logged-in-user] [{:ui-login/user [:user/id :user/name :user/email]}]]

    ;; else
    []))

(defsc User [_ props]
  {:ident [:user/id :user/id]
   :query [:user/id :user/name :user/email]})

(defsc Token [_ props]
  {:ident [:token/id :token/id]
   :query [:token/id :token/value]})

(defsc UiLogin [_ props]
  {:ident [:ui-login/id :ui-login/id]
   :query [:ui-login/id
           {:ui-login/user (comp/get-query User)}
           {:ui-login/access-token (comp/get-query Token)}]})

(def ui-login-id
  "The :ui-login/id value to use to represent the logged-in user"
  :logged-in-user)

(defsc UiPage [_ props]
  {:ident [:ui-page/id :ui-page/id]
   :query [:ui-page/id :ui-page/name :ui-page/query]})

(defsc UiRoot [_ props]
  {:ident [:ui-root/id :ui-root/id]
   :query [:ui-root/id
           {:ui-root/ui-login (comp/get-query UiLogin)}
           {:ui-root/ui-page (comp/get-query UiPage)}]})

(def entity-type-map
  "Create an indirect mapping so that the entities can be used without knowledge of defsc components"
  {::token Token
   ::user  User
   ::ui-login UiLogin})

(def get-in-graph nsh/get-in-graph)

(defn object-graph-query
  "The function used to get data out of the object graph"
  [object-graph query-root-ident eql]
  (fdn/db->tree eql query-root-ident object-graph))

(defn object-graph-merge
  "The fuction used to put data into the object graph"
  [object-graph entity-type entity]
  (merge/merge-component object-graph (get entity-type-map entity-type) entity))
