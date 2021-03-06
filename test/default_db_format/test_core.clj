(ns default-db-format.test-core
  (:require [clojure.test :refer :all]
            [default-db-format.core :as core]
            [examples.gases :as gases]
            [examples.medical-records :as medical]
            [examples.fulcro-template :as template]
            [examples.kanban :as kanban]
            [examples.so-question :as so-question]
            [examples.websockets-demo :as websockets]
            [default-db-format.dev :as dev]
            [default-db-format.help :as help]
            [default-db-format.helpers :as helpers]))

(def expected-gas-issues
  {:categories            #{"graph" "app"},
   :table-names           #{"drop-info" "line" "graph-point"}
   :poor-table-structures #{}
   :non-vector-root-joins #{}
   :non-vector-table-fields #{}
   :skip-root-joins       #{{:text    helpers/expect-idents,
                             :problem :app/system-gases
                             :problem-value
                                      [{:id 200, :short-name "Methane"}
                                       {:id 201, :short-name "Oxygen"}
                                       {:id 202, :short-name "Carbon Monoxide"}
                                       {:id 203, :short-name "Carbon Dioxide"}]}}
   ;;
   ;; Hmm - its a set of tuples rather than a map. Fix when going for perfection...
   ;;
   :skip-table-fields
                          #{[:drop-info/by-id {:x-gas-details [{:id 10100} {:id 10101} {:id 10102}]}]
                            [:line/by-id {:intersect {:id 302}, :colour {:r 255, :g 0, :b 0}}]}
   })

(deftest gas-problems
  (let [res (dissoc (core/check gases/gas-norm-state) :version)]
    (is (= expected-gas-issues
           res))))

(deftest gas-problem-no-id
  (let [res (dissoc (core/check gases/include-non-id-problem) :version)]
    (is (= 2
           (-> res :skip-root-joins count)))))

(deftest joins-and-bad-rgb
  (let [res (dissoc (core/check {:skip-field-join      [:graph/init :graph/translators]
                                 :acceptable-map-value [:r :g :b]}
                                gases/real-project-fixed-component-idents) :version)]
    ;(dev/pp res)
    (is (= 1
           (-> res :skip-table-fields count)))))

;;
;; Almost all are /by-id, so expect to get lots (19) of problems
;;
(deftest joins-and-proper-rgb
  (let [res (dissoc (core/check {
                                 :table-ending         "/id"
                                 :skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 19
           (-> res :skip-root-joins count)))
    (is (= 0
           (-> res :skip-table-fields count)))))

(deftest single-bad-join
  (let [res (dissoc (core/check {:table-ending         "/id"
                                 :skip-link            :graph/translators
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 20
           (-> res :skip-root-joins count)))))

;;
;; button/id is a table name that won't be supported
;; The table :button/id will have to be thought of as a join, and won't have idents in it
;; The top level join :app/buttons will of course have idents, but as they are [:button/id ?]
;; rather than [:button/by-id ?], they won't be recognised as idents.
;;
(deftest joins-and-missing-id
  (let [res (dissoc (core/check {:skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 2
           (-> res :skip-root-joins count)))
    (is (= 0
           (-> res :skip-table-fields count)))
    #_(dev/pp res)))

;;
;; Shows that the :id is ignored. The tool will tell about this problem.
;;
(deftest joins-and-bad-id
  (let [res (dissoc (core/check {:table-ending         [:id "/by-id"]
                                 :skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 2
           (-> res :skip-root-joins count)))
    (is (= 0
           (-> res :skip-table-fields count)))
    ))

;;
;; Perversely recognise only :button/id as a table
;;
(deftest joins-and-one-good-id
  (let [res (dissoc (core/check {:table-ending         "/id"
                                 :skip-link            [:graph/init :graph/translators]
                                 :acceptable-map-value [[:r :g :b]]}
                                gases/real-project-fixed-component-idents) :version)]
    (is (= 0
           (-> res :skip-table-fields count)))
    (is (= 19
           (-> res :skip-root-joins count)))
    #_(dev/pp res)))

(def expected-template-1-res
  {:categories            #{"ui" "root" "fulcro.inspect.core"}
   :table-names           #{"fulcro.ui.bootstrap3.modal" "fulcro.client.routing.routers" "user"}
   :non-vector-root-joins #{}
   :non-vector-table-fields #{}
   :skip-root-joins       #{{:text          helpers/expect-idents, :problem :root/modals
                             :problem-value {:welcome-modal [:fulcro.ui.bootstrap3.modal/by-id :welcome]}}}
   :skip-table-fields
                          #{[:fulcro.client.routing.routers/by-id #:fulcro.client.routing{:current-route [:login :page]}]}
   :poor-table-structures #{}
   })

(deftest fulcro-template-1
  (let [res (dissoc (core/check {}
                                template/initial-state) :version)]
    (is (= expected-template-1-res
           res))
    #_(dev/pp res)
    ))

(def expected-template-2-res
  {:categories            #{"ui" "root" "fulcro.inspect.core"}
   :table-names           #{"fulcro.ui.bootstrap3.modal" "fulcro.client.routing.routers" "user" "login"}
   :non-vector-root-joins #{}
   :non-vector-table-fields #{}
   :skip-root-joins       #{{:text          helpers/expect-idents, :problem :root/modals
                             :problem-value {:welcome-modal [:fulcro.ui.bootstrap3.modal/by-id :welcome]}}}
   :skip-table-fields     #{}
   :poor-table-structures #{}})

(deftest fulcro-template-2
  (let [res (dissoc (core/check {:routing-table-name [:login]}
                                template/initial-state) :version)]
    (is (= expected-template-2-res
           res))
    #_(dev/pp res)))

(deftest fulcro-template-3
  (let [res (dissoc (core/check {:routing-table-name [:login]
                                 :skip-link          :root/modals}
                                template/initial-state)
                    :version)]
    (is (= true
           (core/ok? res)))
    #_(dev/pp res)))

(deftest link-ident
  (let [res (dissoc (core/check
                      {:skip-link medical/irrelevant-keys}
                      medical/norm-state)
                    :version)]
    (is (= true
           (core/ok? res)))
    #_(dev/pp res)))

;;
;; Field tester is expecting "Guy" to be a map, so can have a look at all its fields.
;; Needs to recognise that it is not and just leave it alone. Are in a link and links
;; never refer to the normalized world, they only contain scalar values.
;; (My assumption, prolly correct)
;;
(def link-map-entry [:current-person/by-id #:person{:first-name "Guy", :last-name "Rundle"}])
(def usual-map-entry [:clinic/by-id {1 #:db{:id nil}}])
(def bad-join-map-entry [:clinic/by-id {1 #:db{:id {:a "Join s/be Ident/s"}}}])
(def not-a-table-liar [:fulcro.inspect.core/app-id 'default-db-format.baby-sharks/AdultRoot])

(defn create-field-tester [config]
  (let [{:keys [acceptable-map-value acceptable-vector-value ignore-skip-field-joins] :as init-map}
        (helpers/config->init (merge helpers/default-edn-config config))
        ident-like? (helpers/-ident-like-hof? init-map)
        conformance-predicates
        {:ident-like?               ident-like?
         :acceptable-table-value-f? (constantly false)}
        ]
    (core/field-join->error-hof
      conformance-predicates
      acceptable-map-value
      acceptable-vector-value
      ignore-skip-field-joins)))

;;
;; Without `(map? (second m))` (just search in code) check calling check where a
;; link is referred to will output this error message:
;; UnsupportedOperationException nth not supported on this type: Character  clojure.lang.RT.nthFrom (RT.java:962)
;; Here test only needs to make sure that there's no failure.
;;
(deftest dont-investigate-link
  (let [field-tester (create-field-tester {:skip-link medical/irrelevant-keys})]
    (is (= nil
           (field-tester link-map-entry)))))

(deftest no-link-to-not-investigate
  (let [field-tester (create-field-tester {:skip-link medical/irrelevant-keys})]
    (is (= nil
           (field-tester usual-map-entry)))))

(deftest bad-join-output
  (let [field-tester (create-field-tester {:skip-link medical/irrelevant-keys})]
    (is (= #:clinic{:by-id #:db{:id {:a "Join s/be Ident/s"}}}
           (field-tester bad-join-map-entry)))))

(deftest matched-but-not-a-table
  (is (= :fulcro.inspect.core/app-id
         (-> not-a-table-liar core/table-structure->error :problem))))

;;
;; The only errors we report are the ones that can be turned off.
;; 'no recognised table names' can't be turned off, so is no longer
;; an error
;;
(deftest no-recognised-table-names
  (let [res (dissoc (core/check
                      {}
                      kanban/kanban-norm-state-1)
                    :version)]
    (is (= core/healthy-state
           res))
    #_(dev/pp res)))

(deftest recognise-uppercase-table-names
  (let [res (dissoc (core/check
                      {:table-pattern core/upper-minus-regex}
                      kanban/kanban-norm-state-1)
                    :version)]
    (is (= 5
           (-> res :table-names count)))
    #_(dev/pp res)))

(deftest perfect-state
  (let [res (dissoc (core/check
                      {}
                      so-question/state)
                    :version)]
    (is (= true (core/ok? res)))))

;;
;; core/bad-container-of-idents? is function need to apply to all the vals here, turning one list
;; into two: 'expect vectors' and the traditional 'expect Idents'.
;;
(def joins-with-issues
  [[:session/by-id #:session{:messages '([:message/by-id 100]), :users [[:bad-table 200][:bad-table 201]]}]
   [:user/by-id #:user{:messages '([:message/by-id 100][:message/by-id 101])}]])

(deftest different-bad-field-joins
  (let [res (core/separate-out-bad-joins (fn [x] (and (vector? x) (= 2 (count x))))
                                         joins-with-issues)]
    (is (= 1 (-> res :expected-idents count)))
    (is (= 2 (-> res :expected-vectors count)))))

(deftest non-vector-mix
  (let [field-join-1 [:session/by-id 1 :session/messages]
        field-join-2 [:user/by-id 200 :user/messages]
        field-join-3 [:session/by-id 1 :session/users]
        root-join [:app/messages]
        res (dissoc (core/check
                      {}
                      (-> so-question/state
                          (help/many-join-becomes-list root-join)
                          (help/many-join-becomes-list field-join-1)
                          (help/many-join-becomes-list field-join-2)
                          (help/many-join-becomes-bad-idents field-join-3 :bad-table)))
                    :version)]
    (is (zero? (-> res :skip-root-joins count)))
    (is (= 1 (-> res :non-vector-root-joins count)))
    (is (= 1 (-> res :skip-table-fields count)))
    (is (= 2 (-> res :non-vector-table-fields count)))
    ;(dev/pp res)
    ))

(deftest singleton-passes?
  (is (= true
         (-> (core/check websockets/config websockets/state)
             :skip-table-fields
             empty?))))