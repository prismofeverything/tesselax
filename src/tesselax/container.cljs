(ns tesselax.container
  (:require [cljs.core.async :refer [<! >! timeout chan close! put! alts! sliding-buffer]]
            [domina :as dom]
            [domina.css :as css]
            [tesselax.shared.layout :as layout])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(enable-console-print!)

(def resize-channel (chan (sliding-buffer 1)))

(defn debouncer
  [inflow interval]
  (let [debounce (chan)]
    (go
     (loop [channels [inflow]
            event nil]
       (let [[value channel] (alts! channels)]
         (if (= channel inflow)
           (recur [inflow (timeout interval)] value)
           (do
             (put! debounce event)
             (recur [inflow] nil))))))
    debounce))

(defn dom-size
  [el]
  (let [size (goog.style.getSize (dom/single-node el))]
    {:width (.-width size)
     :height (.-height size)}))

(defn dom-position
  [el]
  (let [size (goog.style.getPosition (dom/single-node el))]
    {:x (.-x size)
     :y (.-y size)}))

(defn reflow-on-resize!
  [container opts]
  (let [debounced-resize (debouncer resize-channel 50)]
    (go
     (while true
       (<! debounced-resize) 
       (layout/layout! container opts)))))

(defn append-resize!
  [resize]
  (let [old-resize (.-onresize js/window)
        new-resize (fn [event] 
                     (if old-resize (old-resize event))
                     (resize event))]
    (set! (.-onresize js/window) new-resize)))

(defn update-position!
  [node position]
  (let [style (.-style node)]
    (set! (.-top style) (str (:y position) "px"))
    (set! (.-left style) (str (:x position) "px"))))

;; (defn update-size!
;;   [node position]
;;   (let [style (.-style node)]
;;     (set! (.-top style) (str (:y position) "px"))
;;     (set! (.-left style) (str (:x position) "px"))))

(defrecord GoogleNode [node]
  layout/Sized
  (size [this] (dom-size node))
  (position [this] (dom-position node))
  (reposition! [this position] (update-position! node position))
  (resize! [this size]))

(defn element 
  [container] 
  (dom/single-node (css/sel (:selector container))))

(defrecord GoogleContainer [selector]
  layout/Sized
  (size [this] (dom-size (element this)))
  (position [this] (dom-position (element this)))
  (reposition! [this position] (update-position! (element this) position))
  (resize! [this size] )
  ;; (resize! [this size] (update-size! (element this) position))

  layout/LayoutContainer
  (setup! [this opts]
    (append-resize! (partial put! resize-channel))
    (reflow-on-resize! this opts))

  (children [this]
    (map #(GoogleNode. %) (dom/nodes (dom/children (css/sel selector))))))

(defn init-google-layout
  ([selector] 
     (layout/init! (GoogleContainer. selector)))
  ([selector opts]
     (let [google (GoogleContainer. selector)]
       (layout/init! google opts))))
