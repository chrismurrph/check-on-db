(ns default-db-format.test-core
  (:require [clojure.test :refer :all]
            [default-db-format.core :as core]
            [examples.gases :as gases]
            [examples.fulcro-template :as template]
            [default-db-format.dev :as dev]))

(def expected-gas-issues
  {:categories                  #{"graph" "app"},
   :known-names                 #{"drop-info" "line" "graph-point"},
   :not-normalized-join-entries #{{:text "Expect Idents", :problem :app/system-gases}},
   :not-normalized-table-entries
                                #{[:drop-info/by-id {:x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}]
                                  [:line/by-id {:intersect {:id 302}, :colour {:r 255, :g 0, :b 0}}]},
   })

(deftest gas-problems
  (let [res (dissoc (core/check gases/gas-norm-state) :version)]
    (is (= expected-gas-issues
           res))))

(deftest gas-problem-no-id
  (let [res (dissoc (core/check gases/include-non-id-problem) :version)]
    (is (= 2
           (-> res :not-normalized-join-entries count)))))

(deftest joins-and-bad-rgb
  (let [res (dissoc (core/check {:bad-join             [:graph/init :graph/translators]
                                 :acceptable-map-value [:r :g :b]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 1
           (-> res :not-normalized-table-entries count)))))

(deftest joins-and-proper-rgb
  (let [res (dissoc (core/check {:by-id-ending         "id"
                                 :bad-join             [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 19
           (-> res :not-normalized-join-entries count)))
    (is (= 0
           (-> res :not-normalized-table-entries count)))))

(deftest single-bad-join
  (let [res (dissoc (core/check {:by-id-ending         "id"
                                 :bad-join             :graph/translators
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 20
           (-> res :not-normalized-join-entries count)))))

;;
;; button/id is a table name that won't be supported
;; The table :button/id will have to be thought of as a join, and won't have idents in it
;; The top level join :app/buttons will of course have idents, but as they are [:button/id ?]
;; rather than [:button/by-id ?], they won't be recognised as idents.
;;
(deftest joins-and-missing-id
  (let [res (dissoc (core/check {:bad-join             [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= #{{:text "Expect Idents", :problem :button/id} {:text "Expect Idents", :problem :app/buttons}}
           (-> res :not-normalized-join-entries)))
    (is (= 0
           (-> res :not-normalized-table-entries count)))
    #_(dev/pp res)))

;;
;; Shows that the :id is ignored. The tool will tell about this problem.
;;
(deftest joins-and-bad-id
  (let [res (dissoc (core/check {:by-id-ending         [:id "by-id"]
                                 :bad-join             [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 2
           (-> res :not-normalized-join-entries count)))
    (is (= 0
           (-> res :not-normalized-table-entries count)))
    #_(dev/pp res)))

;;
;; Perversely recognise only :button/id as a table
;;
(deftest joins-and-one-good-id
  (let [res (dissoc (core/check {:by-id-ending         "id"
                                 :bad-join             [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 0
           (-> res :not-normalized-table-entries count)))
    (is (= 19
           (-> res :not-normalized-join-entries count)))
    #_(dev/pp res)))

(def expected-template-1-res
  {:categories                  #{"ui" "root" "fulcro.inspect.core"},
   :known-names                 #{"fulcro.ui.boostrap3.modal" "fulcro.client.routing.routers" "user"},
   :not-normalized-join-entries #{{:text "Expect Idents", :problem :root/modals}},
   :not-normalized-table-entries
                                #{[:fulcro.client.routing.routers/by-id #:fulcro.client.routing{:current-route [:login :page]}]}})

(deftest fulcro-template-1
  (let [res (dissoc (core/check {}
                                template/initial-state) :version)]
    (is (= expected-template-1-res
           res))
    (dev/pp res)))

(def expected-template-2-res
  {:categories                   #{"ui" "root" "fulcro.inspect.core"},
   :known-names                  #{"fulcro.ui.boostrap3.modal" "fulcro.client.routing.routers" "user" "login"},
   :not-normalized-join-entries  #{{:text "Expect Idents", :problem :root/modals}},
   :not-normalized-table-entries #{}})

(deftest fulcro-template-2
  (let [res (dissoc (core/check {:routing-table [:login]}
                                template/initial-state) :version)]
    (is (= expected-template-2-res
           res))
    #_(dev/pp res)))

(deftest fulcro-template-3
  (let [res (dissoc (core/check {:routing-table [:login]
                                 :bad-join      :root/modals}
                                template/initial-state) :version)]
    (is (= true
           (core/ok? res)))
    #_(dev/pp res)))