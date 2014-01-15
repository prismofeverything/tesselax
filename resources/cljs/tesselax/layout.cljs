(ns tesselax.layout  
  (:require [cljs.core.async :refer [<! >! timeout chan close! put! alts! sliding-buffer]]
            [tesselax.space :as space]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as events])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(defrecord Rect [x y width height div])

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

(defn horizontal-limit-layout
  [pile x-limit]
  (loop [pile pile
         out-pile []
         spaces [(Rect. 0 0 x-limit js/Infinity [0 0 0])]]
    (if (empty? pile)
      [out-pile spaces]
      (let [next-rect (first pile)
            [grid spaces] (space/add-rect out-pile next-rect spaces)]
        (.log js/console (count spaces))
        (recur
         (rest pile)
         grid
         spaces)))))

(defn animate-pile!
  [pile ms update]
  (go
   (doseq [rect pile]
     (update rect)
     (<! (timeout ms)))))

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

(defn dom->rect
  [el]
  (let [size (dom-size el)
        position (dom-position el)
        div {:div el}]
    (map->Rect (merge size position div))))

(defn animate-grid!
  [selector update]
  (let [container (css/sel selector)
        elements (dom/nodes (dom/children container))
        width (:width (dom-size container))
        pile (map dom->rect elements)
        [pile spaces] (horizontal-limit-layout pile width)]
    (animate-pile! pile 0 update)
    pile))

(defn resize-grid
  [event selector update]
  (.log js/console event)
  (animate-grid! selector update))

(defn resize!
  [event]
  (put! resize-channel event))

(defn reflow-on-resize
  [selector update]
  (let [debounced-resize (debouncer resize-channel 50)]
    (go
     (while true
       (resize-grid (<! debounced-resize) selector update)))))

(defn append-resize!
  [resize]
  (let [old-resize (.-onresize js/window)
        new-resize (fn [event] 
                     (if old-resize (old-resize event))
                     (resize event))]
    (set! (.-onresize js/window) new-resize)))

(defn update-position!
  [rect]
  (let [div (:div rect)
        style (.-style div)]
    (set! (.-top style) (str (:y rect) "px"))
    (set! (.-left style) (str (:x rect) "px"))))

(defn init!
  ([selector] 
     (init! selector 
            {:debounce-interval 50 
             :fixed? (constantly false)
             :update-rect update-position!}))
  ([selector config]
     (append-resize! resize!)
     (reflow-on-resize selector (:update-rect config))
     (animate-grid! selector (:update-rect config))))
