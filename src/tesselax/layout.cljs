(ns tesselax.layout  
  (:require [tesselax.space :as space]))

(def infinity (/ 1.0 0.0))
(enable-console-print!)

(defprotocol Sized
  (size [this])
  (position [this])
  (resize! [this size])
  (reposition! [this position]))

(defprotocol LayoutContainer
  (setup! [this opts])
  (children [this]))

(defrecord Rect [x y width height node])

(defn rect 
  [sized]
  (map->Rect (merge {:node sized} (size sized) (position sized))))

(defn horizontal-limit-layout
  [pile container]
  (let [x-limit (:width (size container))]
    (loop [pile pile
           out-pile []
           spaces [(Rect. 0 0 x-limit infinity nil)]]
      (if (empty? pile)
        [out-pile spaces]
        (let [next-rect (first pile)
              [grid spaces] (space/add-rect out-pile next-rect spaces)]
          (recur
           (rest pile)
           grid
           spaces))))))

(defn layout!
  [container opts]
  (let [pile (map rect (children container))
        layout (:layout opts)
        [pile spaces] (layout pile container)]
    (doseq [rect pile]
      (reposition! (:node rect) {:x (:x rect) :y (:y rect)}))
    [pile spaces]))

(defn init!
  "container must implement Sized and LayoutContainer,
   whose children must implement Sized as well."
  ([container] (init! container {:layout horizontal-limit-layout}))
  ([container opts]
     (setup! container opts)
     (layout! container opts)))
