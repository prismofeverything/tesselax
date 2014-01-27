(defproject tesselax "0.0.1"
  :description "Automatic animated layouts in clojurescript"
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [domina "1.0.2"]
                 [ring "1.2.0"]
                 [org.clojure/google-closure-library-third-party "0.0-2029"]]
  :plugins [[lein-ring "0.8.6"]
            [lein-cljsbuild "1.0.1"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n" "-Xmx2g"]
  :source-paths ["src"]
  :resource-paths ["resources/"]
  :min-lein-version "2.0.0"
  :migration-namespace tesselax.migrations
  :main tesselax.server
  :ring {:handler tesselax.server/handler
         :init tesselax.server/init
         :port 12321
         :auto-reload? false
         :servlet-name "tesselax-frontend"}
  :cljsbuild {
    :builds {
      :dev {
        :source-paths ["src"]  
        :compiler {
          :optimizations :none
          :output-to "resources/public/js/app/tesselax.js"
          :output-dir "resources/public/js/app/out"
          :source-map true}}
       :prod {
        :source-paths ["src"]
        :compiler {
          :output-to "resources/public/js/tesselax.min.js"
          :optimizations :advanced
          :pretty-print false}}}})
