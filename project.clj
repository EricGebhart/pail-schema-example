(defproject pail-schema-example "0.1.0-SNAPSHOT"
  :description "Example of using Prismatic Schema, Pail and Cascalog."
  :url "http://github.com/EricGebhart/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"

  :source-paths ["src"]


  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cascalog "2.0.0" ]
                 [prismatic/schema "0.2.0"]
                 [pail-cascalog "0.1.0"]
                 [pail-schema "0.1.0-SNAPSHOT"]]

  :aot [pail-schema.data-unit-pail-structure]

  :profiles {:1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0-master-SNAPSHOT"]]}

             :dev {:dependencies [[midje "1.5.1"]]
                   :plugins [[lein-midje "3.0.1"]]}}


  :deploy-repositories [["releases" {:url "https://clojars.org/repo" :username :gpg :password :gpg}]
                        ["snapshots" {:url "https://clojars.org/repo" :username :gpg :password :gpg}]])
