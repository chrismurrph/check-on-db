(defproject default-db-format "0.1.1-SNAPSHOT"
  :description "Visual feedback if normalized data is not in 'default db format'."
  :url "https://github.com/chrismurrph/default-db-format"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]
                 [org.clojure/clojurescript "1.9.854" :scope "provided"]
                 [fulcrologic/fulcro "2.0.0-beta5" :scope "provided"]
                 [fulcrologic/fulcro-css "2.0.0-beta1" :scope "provided"]
                 [org.clojure/tools.namespace "0.3.0-alpha4" :scope "provided"]
                 [org.clojure/core.async "0.3.443" :scope "provided"]
                 [lein-figwheel "0.5.14" :scope "provided"]
                 [figwheel-sidecar "0.5.11" :exclusions [org.clojure/tools.reader] :scope "provided"]
                 [devcards "0.2.3" :exclusions [cljsjs/react-dom cljsjs/react] :scope "provided"]
                 [binaryage/devtools "0.9.4" :scope "provided"]
                 [fulcrologic/fulcro-inspect "2.0.0-alpha2" :scope "provided"]
                 ]

  :jar-exclusions [#"config" #"examples" #"public" #"figwheel.clj" #"user.clj" #"test_core.clj" #"test_helpers.clj"]

  :scm {:name "git"
        :url  "https://github.com/chrismurrph/default-db-format"}

  :plugins [[lein-cljsbuild "1.1.2"]]

  :clean-targets ^{:protect false} ["resources/public/js/"
                                    "target"]

  :source-paths ["src/main" "dev" "script" "test"]

  :cljsbuild {:builds
              [{:id           "cards"
                :source-paths ["src/main" "src/cards" "dev"]
                :figwheel     {:devcards true}
                :compiler     {:main                 default-db-format.card-ui
                               :output-to            "resources/public/js/cards.js"
                               :output-dir           "resources/public/js/cards"
                               :asset-path           "js/cards"
                               :preloads             [devtools.preload
                                                      default-db-format.preload
                                                      fulcro.inspect.preload
                                                      ]
                               :external-config      {:fulcro.inspect/config    {:launch-keystroke "ctrl-v"}
                                                      :default-db-format/config {:collapse-keystroke "ctrl-q"
                                                                                 :debounce-timeout   500}}
                               :parallel-build       true
                               :source-map-timestamp true
                               :optimizations        :none}}
               ]}
  )