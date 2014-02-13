(defproject tesselax "0.0.1"
  :description "Box packing in Clojure and ClojureScript"
  :url "https://github.com/prismofeverything/tesselax"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [domina "1.0.2"]
                 [org.clojure/google-closure-library-third-party "0.0-2029"]]
  :dev-dependencies [[ring "1.2.0"]]
  :plugins [[lein-ring "0.8.6"]
            [lein-cljsbuild "1.0.2"]
            [com.keminglabs/cljx "0.3.2"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n" "-Xmx2g"]
  :source-paths ["src/clj" "target/generated/clj"]
  :resource-paths ["resources/"]
  :min-lein-version "2.0.0"
  :migration-namespace tesselax.migrations
  :main tesselax.server
  :ring {:handler tesselax.server/handler
         :init tesselax.server/init
         :port 12321
         :auto-reload? false
         :servlet-name "tesselax-frontend"}
  :cljx 
  {:builds
   [{:source-paths ["src/cljx"]
     :output-path "target/generated/clj"
     :rules :clj}
    {:source-paths ["src/cljx"]
     :output-path "target/generated/cljs"
     :rules :cljs}]}
  :cljsbuild {
    :builds {
      :dev {
        :source-paths ["src/cljs" "target/generated/cljs"]
        :compiler {
          :optimizations :none
          :output-to "resources/public/js/app/tesselax.js"
          :output-dir "resources/public/js/app/out"
          :source-map true 
          }}
      :prod {
        :source-paths ["src/cljs" "target/generated/cljs"]
        :compiler {
          :output-to "resources/public/js/tesselax.min.js"
          :optimizations :advanced
          :pretty-print false}}}})
