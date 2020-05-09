(ns app.graph
  "Defines the data model for the :object-graph key in the re-frame app db

  The desire is to leverage the work of fulcro to get an EQL-based graph
  database, but not to buy into actually building UI elements with it."
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
            [com.fulcrologic.fulcro.algorithms.normalized-state :as nsh]))

(comment
  "lifted from https://github.com/fulcrologic/fulcro/blob/develop/src/test/com/fulcrologic/fulcro/algorithms/merge_spec.cljc,
  and kept around for reference so that more complex usage can evolve"
  (defsc Score
    [_ {::keys []}]
    {:ident [::score-id ::score-id]
     :query [::score-id ::points :ui/expanded?]})

  (defsc Scoreboard [_ props]
    {:ident [::scoreboard-id ::scoreboard-id]
     :query [::scoreboard-id
             {::scores (comp/get-query Score)}]} "")

  (merge/merge-component {} Scoreboard {::scoreboard-id :a ::scores [{::score-id :s1 ::points 10}]}))

(defsc User [_ props]
  {:ident [:user/id :user/id]
   :query [:user/id :user/name :user/email]})

(defsc Token [_ props]
  {:ident [:token/id :token/id]
   :query [:token/id :token/value]})

(def entity-type-map
  "Create an indirect mapping so that the entities can be used without knowledge of defsc components"
  {::token Token
   ::user  User})

(def get-in-graph nsh/get-in-graph)

(defn object-graph-query
  "The function used to get data out of the object graph"
  [object-graph query-root-ident eql]
  (fdn/db->tree eql query-root-ident object-graph))

(defn object-graph-merge
  "The fuction used to put data into the object graph"
  [object-graph entity-type entity]
  (merge/merge-component object-graph (get entity-type-map entity-type) entity))
