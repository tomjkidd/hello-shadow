(ns app.graph-tests
  (:require [cljs.test :include-macros true :refer [deftest is]]
            [app.graph :as graph]))

(defn ensure-expected-graph-change!
  [{:keys [entity before-state eql-query-result expected-state]}]
  (let [after-state (graph/object-graph-merge before-state entity eql-query-result)]
    (is (= expected-state after-state)
        "Novelty is normalized into the object graph")))

(deftest object-graph-merge
  (doseq [test-case [{:entity           ::graph/site
                      :before-state     {}
                      :eql-query-result {:site/id          1
                                         :site/name        "Site 1"
                                         :site/site-trials [{:site-trial/id   1
                                                             :site-trial/name "Site Trial 1"}
                                                            {:site-trial/id   2
                                                             :site-trial/name "Site Trial 2"}]}
                      :expected-state   {:site-trail/id {1 {:site-trial/id   1
                                                            :site-trial/name "Site Trial 1"}
                                                         2 {:site-trial/id   2
                                                            :site-trial/name "Site Trial 2"}}
                                         :site/id       {1 {:site/id          1
                                                            :site/name        "Site 1"
                                                            :site/site-trials [[:site-trail/id 1] [:site-trail/id 2]]}}}}
                     {:entity           ::graph/ui-login
                      :before-state     {}
                      :eql-query-result {:ui-login/id           :logged-in-user
                                         :ui-login/user         {:user/id   "auth-provider|value"
                                                                 :user/name "Duke Caboom"}
                                         :ui-login/access-token {:token/id    :access-token
                                                                 :token/value "Not a valid token"}}
                      :expected-state   {:user/id     {"auth-provider|value" {:user/id   "auth-provider|value"
                                                                              :user/name "Duke Caboom"}}
                                         :token/id    {:access-token {:token/id    :access-token
                                                                      :token/value "Not a valid token"}}
                                         :ui-login/id {:logged-in-user {:ui-login/id           :logged-in-user
                                                                        :ui-login/user         [:user/id "auth-provider|value"]
                                                                        :ui-login/access-token [:token/id :access-token]}}}}]]
    (ensure-expected-graph-change! test-case)))
