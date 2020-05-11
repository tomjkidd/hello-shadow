(ns app.graph
  "Defines the data model for the re-frame app-db (:db)

  The desire is to leverage the work of fulcro to get an EQL-based graph
  database, but not to buy into actually building UI elements with it.

  Each domain entity is represented by a Fulcro component.
  Each component defines `ident` and `query`
    - `query` provides the full set of available properties for the entity
    - `ident` provides the property that is used as the entity's ident in the graph."
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

(declare Site)

(defsc User [_ props]
  {:ident [:user/id :user/id]
   :query [:user/id :user/name :user/email
           {:user/sites (comp/get-query Site)}]})

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

;; Domain entities
(defsc Patient [_ props]
  {:ident [:patient/id :patient/id]
   :query [:patient/id
           :patient/name
           :patient/gender
           :patient/age]})

(defsc SitePatient [_ props]
  {:ident [:site-patient/id :site-patient/id]
   :query [:site-patient/id
           :site-patient/mrn
           {:site-patient/patient (comp/get-query Patient)}]})

(defsc SiteTrialPatient [_ props]
  {:ident [:site-trail-patient/id :site-trial-patient/id]
   :query [:site-trial-patient/id
           :site-trial-patient/stage
           {:site-trial-patient/site-patient (comp/get-query SitePatient)}]})

(defsc SiteTrial [_ props]
  {:ident [:site-trail/id :site-trial/id]
   :query [:site-trial/id
           :site-trial/name
           {:site-trial/site-trial-patients (comp/get-query SiteTrialPatient)}]})

(defsc Site [_ props]
  {:ident [:site/id :site/id]
   :query [:site/id
           :site/name
           {:site/site-trials (comp/get-query SiteTrial)}]})

(def entity-type-map
  "A mapping from entity keywords to the Fulcro components that define them"
  {::token Token
   ::user  User
   ::ui-login UiLogin

   ::site Site
   ::site-trial SiteTrial
   ::site-trial-patient SiteTrialPatient
   ::site-patient SitePatient
   ::patient Patient})

(def get-in-graph nsh/get-in-graph)

(defn object-graph-query
  "The function used to get data out of the object graph

  Useful for arbitrary query in the graph with a known ident to root the query."
  [object-graph query-root-ident eql]
  (fdn/db->tree eql query-root-ident object-graph))

(defn object-graph-merge
  "The fuction used to put data into the object graph

  Useful for direct loading of an eql-result, rooted at `entity-type`

  `object-graph`: re-frame app-db snapshot, @re-frame.db/app-db or :db in event/effect handlers
  `entity-type`: namespaced-keyword, `:app.graph/user`
  `entity`: The eql query-result to merge into the graph, rooted at an entity of type `entity-type`"
  [object-graph entity-type entity]
  (merge/merge-component object-graph (get entity-type-map entity-type) entity))
