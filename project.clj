(defproject quiz-compile "0.1.0-SNAPSHOT"
  :description "Compiles a custom text DSL to mp3 audio"
  :url "https://github.com/christianromney/quiz-compile"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/spec.alpha "0.2.168"]]
  :main ^:skip-aot quiz-compile.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
