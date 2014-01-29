(ns tesselax.shared.layout  
  (:require [tesselax.shared.space :as space]))

(def infinity (/ 1.0 0.0))

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

(defn layout-child!
  [rect]
  {:pre [(satisfies? Sized (:node rect))]}
  (reposition! (:node rect) {:x (:x rect) :y (:y rect)}))

(defn layout!
  [container opts]
  (let [pile (map rect (children container))
        layout (:layout opts)
        [updated-pile spaces] (layout pile container)]
    (doseq [rect updated-pile] (layout-child! rect))
    [pile spaces]))

(defn init!
  "container must implement Sized and LayoutContainer,
   whose children must implement Sized as well."
  ([container] (init! container {:layout horizontal-limit-layout}))
  ([container opts]
    {:pre [(satisfies? LayoutContainer container)
           (satisfies? Sized container)]}
    (setup! container opts)
    (layout! container opts)))
